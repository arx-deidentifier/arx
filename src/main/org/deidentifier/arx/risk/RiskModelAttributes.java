/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.risk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * A class for attribute-related risks
 *
 * This class is based on the work stated in R. Motwani et al., “Efficient algorithms for masking and finding quasi-identifiers,” Proc. VLDB Conf., 2007.
 * Interdisciplinary training
 *
 * In this class we always analyze a given specific set of attributes, which form a quasi-identifier (QI).
 *
 * @author Fabian Prasser, Maximilian Zitzmann
 */
public class RiskModelAttributes {

    private final WrappedBoolean stop;
    private final QuasiIdentifierRisk[] risks;
    private ARXPopulationModel population;
    private DataHandleInternal handle;
    private ARXSolverConfiguration solverconfig;
    private ARXConfiguration arxconfig;

    /**
     * Creates a new instance
     */
    RiskModelAttributes(ARXPopulationModel population, DataHandleInternal handle, Set<String> identifiers, WrappedBoolean stop, WrappedInteger percentageDone, ARXSolverConfiguration solverconfig, ARXConfiguration arxconfig) {
        this.population = population;
        this.handle = handle;
        this.stop = stop;
        this.solverconfig = solverconfig;
        this.arxconfig = arxconfig;

        // Compute risk estimates for all elements in the power set
        Set<Set<String>> powerset = getPowerSet(identifiers);
        Map<Set<String>, QuasiIdentifierRisk> scores = new HashMap<>();
        int done = 0;
        for (Set<String> set : powerset) {
            checkInterrupt();
            if (!set.isEmpty()) {
                scores.put(set, new QuasiIdentifierRisk(set));
                percentageDone.value = (int) Math.round((double) done++ /
                        (double) (powerset.size() - 1) *
                        100d);
            }
        }

        // Now create sorted array
        risks = new QuasiIdentifierRisk[scores.size()];
        int idx = 0;
        for (QuasiIdentifierRisk value : scores.values()) {
            risks[idx++] = value;
        }
        Arrays.sort(risks);
    }

    /**
     * Calculates the gaussian sum formula
     * @param n the number to sum to
     * @return the sum from 1 to n
     */
    private double sum(double n) {
        return (n * (n + 1d)) / 2d;
    }

    /**
     * Returns the quasi-identifiers, sorted by risk
     *
     * @return
     */
    public QuasiIdentifierRisk[] getAttributeRisks() {
        return this.risks;
    }

    /**
     * Checks for interrupts
     */
    private void checkInterrupt() {
        if (stop.value) {
            throw new ComputationInterruptedException();
        }
    }

    /**
     * Returns the power set
     *
     * @param originalSet
     * @return
     */
    private <T> Set<Set<T>> getPowerSet(Set<T> originalSet) {
        checkInterrupt();
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : getPowerSet(rest)) {
            checkInterrupt();
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     * Provides the calculations for risks for each attribute set
     */
    private RiskProvider getRiskProvider(final Set<String> attributes,
                                         final WrappedBoolean stop) {

        // get new Histogram from attributes
        RiskEstimateBuilder builder = new RiskEstimateBuilder(population, handle, attributes, stop, solverconfig, arxconfig);

        // get new Histogram
        RiskModelHistogram newEqClasses = builder.getEquivalenceClassModel();

        final double alphaDistinction = getAlphaDistinction(newEqClasses);
        final double alphaSeparation = getAlphaSeparation(newEqClasses);

        // Return a provider
        return new RiskProvider() {
            public double getAlphaDistinction() {
                return alphaDistinction;
            }

            public double getAlphaSeparation() {
                return alphaSeparation;
            }
        };
    }

    /**
     * We calculate a value alpha in [0,1] such that the set of attributes becomes a key
     * after the removal of a fraction of at most 1-alpha of the records in the table.
     * This equals the number of distinct combinations of values (= number of eqClasses) / number of all records.
     *
     * @return the calculated alpha distinction
     */
    private double getAlphaDistinction(RiskModelHistogram histogramm) {
        return histogramm.getNumClasses() / histogramm.getNumRecords();
    }

    /**
     * Two records are separated by the QI if they do not share the same quasi-identifying values.
     * From the set of all possible combinations of records, this method returns the fraction alpha (in [0, 1])
     * of all combinations which are separated by the current QI.
     *
     * @return the calculated alpha separation
     */
    private double getAlphaSeparation(RiskModelHistogram histogram) {
        int[] classes = histogram.getHistogram();

        // when we want to compare 4 values (only in one direction this means we compare "a" to "b" but not "b" to "a")
        // we have 3 + 2 + 1 comparisons
        // => numberComparisons = number of values - 1
        double totalNumberOfComparisons = sum(histogram.getNumRecords() - 1d);

        // a record separates another record when it has on at least one attribute a different value
        double separatedRecords = 0;

        // no record have been compared yet
        double numberRecordsLeft = histogram.getNumRecords();

        // For each class-size
        for (int i = 0; i < classes.length; i += 2) {

            // Obtain size and multiplicity of that class
            double classSize = classes[i];
            double classMultiplicity = classes[i + 1];

            // Calculate records remaining in all classes of a size larger than the current one
            numberRecordsLeft -= classSize * classMultiplicity;

            // All records in classes of the current size are different from all remaining records
            double separatedRecordsCurrentClass = classMultiplicity * classSize * numberRecordsLeft;

            // Moreover, all records in each class of the current size are different from all other records
            // in other classes of the same size
            separatedRecordsCurrentClass += ((classMultiplicity - 1d) * classMultiplicity * (classSize * classSize)) / 2d;

            // add number of separated classes to result
            separatedRecords += separatedRecordsCurrentClass;
        }

        // alpha separation indicates a value alpha [0,1] such that a subset of attributes separates
        // at least an alpha fraction of all record pairs
        return separatedRecords / totalNumberOfComparisons;
    }

    /**
     * Helper interface
     */
    interface RiskProvider {
        double getAlphaDistinction();

        double getAlphaSeparation();
    }

    /**
     * Risks associated to a certain quasi-identifier
     *
     * @author Fabian Prasser
     */
    public final class QuasiIdentifierRisk implements
            Comparable<QuasiIdentifierRisk> {

        /**
         * Field
         */
        private final Set<String> identifier;
        /**
         * Field
         */
        private final double alphaDistinction;
        /**
         * Field
         */
        private final double alphaSeparation;

        /**
         * Creates a new instance
         *
         * @param identifier
         */
        private QuasiIdentifierRisk(Set<String> identifier) {
            RiskProvider provider = getRiskProvider(identifier, stop);
            this.identifier = identifier;
            this.alphaDistinction = provider.getAlphaDistinction();
            this.alphaSeparation = provider.getAlphaSeparation();
        }

        @Override
        public int compareTo(QuasiIdentifierRisk other) {
            int cmp = Integer.compare(this.identifier.size(), other.identifier.size());
            if (cmp != 0) {
                return cmp;
            }

            cmp = Double.compare(this.alphaDistinction, other.alphaDistinction);
            if (cmp != 0) {
                return cmp;
            }

            cmp = Double.compare(this.alphaSeparation, other.alphaSeparation);
            if (cmp != 0) {
                return cmp;
            }
            return 0;
        }

        /**
         * @return the alpha distinction
         */
        public double getDistinction() {
            return alphaDistinction;
        }

        /**
         * @return the alpha separation
         */
        public double getSeparation() {
            return alphaSeparation;
        }

        /**
         * @return the identifier
         */
        public Set<String> getIdentifier() {
            return identifier;
        }
    }
}
