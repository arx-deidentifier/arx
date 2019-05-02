/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.util.Collections;
import java.util.Comparator;
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
 * A class for analyzing attribute-related risks. Calculates alpha-distinction and
 * alpha separation as described in R. Motwani et al.
 * "Efficient algorithms for masking and finding quasi-identifiers" Proc. VLDB Conf., 2007.
 *
 *
 * @author Fabian Prasser
 * @author Maximilian Zitzmann
 */
public class RiskModelAttributes {

    /**
     * Risks associated with a certain quasi-identifier
     *
     * @author Fabian Prasser
     * @author Maximilian Zitzmann
     */
    public final class QuasiIdentifierRisk implements Comparable<QuasiIdentifierRisk> {

        /** Field */
        private final List<String> identifier;
        /** Field */
        private final double      alphaDistinction;
        /** Field */
        private final double      alphaSeparation;

        /**
         * Creates a new instance
         *
         * @param identifier
         */
        private QuasiIdentifierRisk(List<String> identifier) {

            // Store identifier
            this.identifier = identifier;

            // Calculate distribution of class sizes
            RiskModelHistogram histogram = new RiskEstimateBuilder(population,
                                                                   handle,
                                                                   new HashSet<String>(identifier),
                                                                   stop,
                                                                   solverconfig,
                                                                   arxconfig).getEquivalenceClassModel();

            // Calculate distinction and separation
            this.alphaDistinction = getAlphaDistinction(histogram);
            this.alphaSeparation = getAlphaSeparation(histogram);
        }

        @Override
        public int compareTo(QuasiIdentifierRisk other) {
            
            // Compare size
            int cmp = Integer.compare(this.identifier.size(), other.identifier.size());
            if (cmp != 0) {
                return cmp;
            }

            // Compare distinction
            cmp = Double.compare(this.getDistinction(), other.getDistinction());
            if (cmp != 0) {
                return cmp;
            }

            // Compare separation
            cmp = Double.compare(this.getSeparation(), other.getSeparation());
            if (cmp != 0) {
                return cmp;
            }
            
            // Compare lexicographically
            return this.identifier.toString().compareTo(other.identifier.toString());
        }

        /**
         * Returns the alpha distinction parameter of this quasi-identifier
         * 
         * @return the alpha distinction
         */
        public double getDistinction() {
            return Double.isNaN(alphaDistinction) ? 0d : alphaDistinction;
        }

        /**
         * Returns the attributes in this quasi-identifier
         * 
         * @return the identifier
         */
        public List<String> getIdentifier() {
            return identifier;
        }

        /**
         * Returns the alpha separation parameter of this quasi-identifier
         * 
         * @return the alpha separation
         */
        public double getSeparation() {
            return Double.isNaN(alphaSeparation) ? 0d : alphaSeparation;
        }
    }
    /** Stop flag */
    private final WrappedBoolean        stop;
    /** Results */
    private final QuasiIdentifierRisk[] risks;
    /** Just needed for creating risk models */
    private ARXPopulationModel          population;
    /** Data handle */
    private DataHandleInternal          handle;
    /** Just needed for creating risk models */
    private ARXSolverConfiguration      solverconfig;
    /** Just needed for creating risk models */
    private ARXConfiguration            arxconfig;

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param identifiers
     * @param stop
     * @param percentageDone
     * @param solverconfig
     * @param arxconfig
     */
    RiskModelAttributes(final ARXPopulationModel population,
                        final DataHandleInternal handle,
                        final Set<String> identifiers,
                        final WrappedBoolean stop,
                        final WrappedInteger percentageDone,
                        final ARXSolverConfiguration solverconfig,
                        final ARXConfiguration arxconfig) {
        
        this.population = population;
        this.handle = handle;
        this.stop = stop;
        this.solverconfig = solverconfig;
        this.arxconfig = arxconfig;

        // Find order list of qis
        List<List<String>> qis = new ArrayList<>();
        Set<Set<String>> powerset = getPowerSet(identifiers);
        for (Set<String> set : powerset) {
            
            // Exclude empty set
            if (!set.isEmpty()) {
                
                // Create and add
                List<String> qi = new ArrayList<String>(set);
                qis.add(qi);
                
                // Sort by column index
                Collections.sort(qi, new Comparator<String>(){
                    @Override
                    public int compare(String o1, String o2) {
                        int index1 = handle.getColumnIndexOf(o1);
                        int index2 = handle.getColumnIndexOf(o2);
                        return new Integer(index1).compareTo(index2);
                    }
                });
            }
        }
        
        // Compute risk estimates for all elements in the power set
        Map<List<String>, QuasiIdentifierRisk> scores = new HashMap<>();
        int done = 0;
        for (List<String> qi : qis) {
            checkInterrupt();
            scores.put(qi, new QuasiIdentifierRisk(qi));
            percentageDone.value = (int) Math.round((double) done++ / (double) (powerset.size() - 1) * 100d);
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
     * Calculates the Gaussian sum formula
     * 
     * @param n the number to sum to
     * @return the sum from 1 to n
     */
    private double gaussianSum(double n) {
        return (n * (n + 1d)) / 2d;
    }

    /**
     * We calculate a value alpha in [0,1] such that the set of attributes becomes a key
     * after the removal of a fraction of at most 1-alpha of the records in the table.
     * This equals the number of distinct combinations of values (= number of eqClasses) / number of all records.
     *
     * @return the calculated alpha distinction
     */
    private double getAlphaDistinction(RiskModelHistogram histogramm) {
        
        // This is almost trivial
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
        
        // Obtain class sizes
        int[] classes = histogram.getHistogram();

        // when we want to compare 4 values (only in one direction this means we compare "a" to "b" but not "b" to "a")
        // we have 3 + 2 + 1 comparisons
        // => numberComparisons = number of values - 1
        double totalNumberOfComparisons = gaussianSum(histogram.getNumRecords() - 1d);

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
            
            // Check interrupt
            checkInterrupt();
        }

        // alpha separation indicates a value alpha [0,1] such that a subset of attributes separates
        // at least an alpha fraction of all record pairs
        return separatedRecords / totalNumberOfComparisons;
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
}