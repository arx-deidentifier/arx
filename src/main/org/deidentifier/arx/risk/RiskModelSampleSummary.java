/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * This class implements risk measures as proposed by El Emam in
 * "Guide to the De-Identification of Personal Health Information",
 * "Measuring the Probability of Re-Identification"
 * 
 * @author Fabian Prasser
 */
public class RiskModelSampleSummary {
    
    /** 
     * Journalist risk
     * @author Fabian Prasser
     */
    public static class JournalistRisk extends RiskSummary {

        /**
         * Creates a new instance
         * @param rA
         * @param rB
         * @param rC
         */
        protected JournalistRisk(double rA, double rB, double rC) {
            super(rA, rB, rC);
        }
    }
    
    /**
     * Marketer risk
     * 
     * @author Fabian Prasser
     */
    public static class MarketerRisk {
        
        /** Proportion of records that can be re-identified on average*/
        private final double rC;
        
        /**
         * Creates a new instance
         * @param rC
         */
        protected MarketerRisk(double rC) {
            this.rC = rC;
        }

        /**
         * Proportion of records that can be re-identified on average
         * @return
         */
        public double getProportionOfRecordsThatCanBeReIdentifiedOnAverage() {
            return rC;
        }
    }

    /** 
     * Prosecutor risk
     * @author Fabian Prasser
     */
    public static class ProsecutorRisk extends RiskSummary {

        /**
         * Creates a new instance
         * @param rA
         * @param rB
         * @param rC
         */
        protected ProsecutorRisk(double rA, double rB, double rC) {
            super(rA, rB, rC);
        }
    }
    /**
     * A set of derived risk estimates
     * 
     * @author Fabian Prasser
     */
    public static class RiskSummary {
        
        /** Proportion of records with risk above threshold*/
        private final double rA;
        /** Maximum probability of re-identification*/
        private final double rB;
        /** Proportion of records that can be re-identified on average*/
        private final double rC;
        
        /**
         * Creates a new instance
         * @param rA
         * @param rB
         * @param rC
         */
        protected RiskSummary(double rA, double rB, double rC) {
            this.rA = rA;
            this.rB = rB;
            this.rC = rC;
        }

        /**
         * Maximum probability of re-identification
         * @return
         */
        public double getMaximumProbabilityOfReIdentification() {
            return rB;
        }

        /**
         * Proportion of records that can be re-identified on average
         * @return
         */
        public double getProportionOfRecordsThatCanBeReIdentifiedOnAverage() {
            return rC;
        }

        /**
         * Proportion of records with risk above threshold
         * @return
         */
        public double getProportionOfRecordsWithRiskAboveThreshold() {
            return rA;
        }
    }

    /** Prosecutor risk */
    private final ProsecutorRisk prosecutorRisk;
    /** Journalist risk */
    private final JournalistRisk journalistRisk;
    /** Marketer risk */
    private final MarketerRisk   marketerRisk;

    /** Acceptable highest probability of re-identification for a single record */
    private final double         threshold;

    /**
     * Creates a new instance
     * @param handle Handle
     * @param identifiers Identifiers
     * @param threshold Acceptable highest probability of re-identification for a single record
     * @param stop Stop flag
     * @param progress Progress
     */
    public RiskModelSampleSummary(DataHandleInternal handle,
                                  Set<String> identifiers,
                                  double threshold,
                                  WrappedBoolean stop,
                                  WrappedInteger progress) {

        // Init
        this.threshold = threshold;
        
        // Prepare
        Groupify<TupleWrapper> sample;
        Groupify<TupleWrapper> population;
        if (handle.getSuperset() != null) {
            sample = getGroups(handle, identifiers, 0d, 0.45d, stop, progress);
            population = getGroups(handle.getSuperset(), identifiers,  0.45d, 0.45d, stop, progress);
        } else {
            sample = getGroups(handle, identifiers, 0d, 0.9d, stop, progress);
            population = sample;
        }
        
        this.prosecutorRisk = getProsecutorRisk(population, sample, 0.9d, stop, progress);
        this.journalistRisk = getJournalistRisk(population, sample, 0.933d, stop, progress);
        this.marketerRisk = getMarketerRisk(population, sample, 0.966d, stop, progress);
    }

    /**
     * Returns the journalist risk
     * @return
     */
    public JournalistRisk getJournalistRisk() {
        return journalistRisk;
    }

    /**
     * Returns the marketer risk
     * @return
     */
    public MarketerRisk getMarketerRisk() {
        return marketerRisk;
    }

    /**
     * Returns the prosecutor risk
     * @return
     */
    public ProsecutorRisk getProsecutorRisk() {
        return prosecutorRisk;
    }

    /**
     * Returns the user-defined risk threshold for individual records
     * @return
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Computes the equivalence classes
     * @param handle
     * @param qis
     * @param offset
     * @param factor
     * @param stop
     * @param progress
     * @return
     */
    private Groupify<TupleWrapper> getGroups(DataHandleInternal handle,
                                             Set<String> qis,
                                             double offset,
                                             double factor,
                                             WrappedBoolean stop,
                                             WrappedInteger progress) {

        /* ********************************
         * Check 
         * ********************************/
        if (handle == null) { throw new NullPointerException("Handle is null"); }
        if (qis == null) { throw new NullPointerException("Quasi identifiers must not be null"); }
        for (String q : qis) {
            if (handle.getColumnIndexOf(q) == -1) { throw new IllegalArgumentException(q + " is not an attribute"); }
        }

        /* ********************************
         * Build equivalence classes 
         * ********************************/
        final int[] indices = new int[qis.size()];
        int index = 0;
        for (final String attribute : qis) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        Arrays.sort(indices);

        // Calculate equivalence classes
        int capacity = handle.getNumRows() / 10;
        capacity = capacity > 10 ? capacity : 10;
        Groupify<TupleWrapper> map = new Groupify<TupleWrapper>(capacity);
        int numRows = handle.getNumRows();
        for (int row = 0; row < numRows; row++) {

            int prog = (int) Math.round(offset + (double) row / (double) numRows * factor);
            if (prog != progress.value) {
                progress.value = prog;
            }

            TupleWrapper tuple = new TupleWrapper(handle, indices, row, true);
            map.add(tuple);
            if (stop.value) { throw new ComputationInterruptedException(); }
        }

        // Return
        return map;
    }

    /**
     * Computes risks
     * @param population
     * @param sample
     * @param offset
     * @param progress 
     * @param stop 
     * @return
     */
    private JournalistRisk getJournalistRisk(Groupify<TupleWrapper> population,
                                             Groupify<TupleWrapper> sample,
                                             double offset,
                                             WrappedBoolean stop,
                                             WrappedInteger progress) {
        // Init
        double rA = 0d;
        double rB = 0d;
        double rC = 0d;
        double rC1 = 0d;
        double rC2 = 0d;
        double numRecordsInSample = 0d;
        double numClassesInSample = 0d;
        double smallestClassSizeInPopulation = Integer.MAX_VALUE;
        int sampleSize = sample.size();
        int index = 0;
        
        // For each group
        Group<TupleWrapper> element = sample.first();
        while (element != null) {
            
            // Track progress
            int prog = (int) Math.round(offset + (double) index++ / (double) sampleSize * 3.3d);
            if (prog != progress.value) {
                progress.value = prog;
            }
            
            // Only process unsuppressed records
            if (!element.getElement().isOutlier()) {
                
                int groupSizeInSample = element.getCount();
                int groupSizeInPopulation = groupSizeInSample;
                if (population != sample) {
                    groupSizeInPopulation = population.get(element.getElement()).getCount();
                }
                
                // Compute rA
                if (1d / groupSizeInPopulation > threshold) {
                    rA += groupSizeInSample;
                }
                // Compute rB
                if (groupSizeInPopulation < smallestClassSizeInPopulation) {
                    smallestClassSizeInPopulation = groupSizeInPopulation;
                }
                // Compute rC
                numClassesInSample++;
                numRecordsInSample += groupSizeInSample;
                rC1 += groupSizeInPopulation;
                rC2 += (double)groupSizeInSample / (double)groupSizeInPopulation;
            }
                
            // Next element
            element = element.next();
            
            // Stop, if required
            if (stop.value) { throw new ComputationInterruptedException(); }
        }
        
        // Finalize rA
        rA /= numRecordsInSample;
        
        // Compute rB: smallest class is first class in the histogram
        rB = 1d / smallestClassSizeInPopulation;

        // Compute rC
        rC1 = numClassesInSample / rC1;
        rC2 = rC2 / numRecordsInSample;
        rC = Math.max(rC1,  rC2);
        
        // Return
        return new JournalistRisk(rA, rB, rC); 
    }

    /**
     * Computes risks
     * @param population
     * @param sample
     * @param offset
     * @param progress 
     * @param stop 
     * @return
     */
    private MarketerRisk getMarketerRisk(Groupify<TupleWrapper> population,
                                         Groupify<TupleWrapper> sample,
                                         double offset,
                                         WrappedBoolean stop,
                                         WrappedInteger progress) {

        // Init
        double rC = 0d;
        double numRecordsInSample = 0d;
        int sampleSize = sample.size();
        int index = 0;
        
        // For each group
        Group<TupleWrapper> element = sample.first();
        while (element != null) {
            
            // Track progress
            int prog = (int) Math.round(offset + (double) index++ / (double) sampleSize * 3.3d);
            if (prog != progress.value) {
                progress.value = prog;
            }
            
            // Only process unsuppressed records
            if (!element.getElement().isOutlier()) {
                
                int groupSizeInSample = element.getCount();
                int groupSizeInPopulation = groupSizeInSample;
                if (population != sample) {
                    groupSizeInPopulation = population.get(element.getElement()).getCount();
                }
                
                // Compute rC
                numRecordsInSample += groupSizeInSample;
                rC += (double)groupSizeInSample / (double)groupSizeInPopulation;
            }
                
            // Next element
            element = element.next();
            
            // Stop, if required
            if (stop.value) { throw new ComputationInterruptedException(); }
        }

        // Compute rC
        rC = rC / numRecordsInSample;
        
        // Return
        return new MarketerRisk(rC); 
    }

    /**
     * Computes risks
     * @param population
     * @param sample
     * @param offset
     * @param progress 
     * @param stop 
     * @return
     */
    private ProsecutorRisk getProsecutorRisk(Groupify<TupleWrapper> population,
                                             Groupify<TupleWrapper> sample,
                                             double offset,
                                             WrappedBoolean stop,
                                             WrappedInteger progress) {

        // Init
        double rA = 0d;
        double rB = 0d;
        double rC = 0d;
        double numRecords = 0d;
        double numClasses = 0d;
        double smallestClassSize = Integer.MAX_VALUE;
        int size = sample.size();
        int index = 0;
        
        // For each group
        Group<TupleWrapper> element = sample.first();
        while (element != null) {
            
            // Track progress
            int prog = (int) Math.round(offset + (double) index++ / (double) size * 3.3d);
            if (prog != progress.value) {
                progress.value = prog;
            }
            
            // Only process unsuppressed records
            if (!element.getElement().isOutlier()) {
                
                // Compute rA
                int groupSize = element.getCount();
                if (1d / groupSize > threshold) {
                    rA += groupSize;
                }
                // Compute rB
                if (groupSize < smallestClassSize) {
                    smallestClassSize = groupSize;
                }
                // Compute rC
                numClasses++;
                numRecords += groupSize;
            }
                
            // Next element
            element = element.next();
            
            // Stop, if required
            if (stop.value) { throw new ComputationInterruptedException(); }
        }
        
        // Finalize rA
        rA /= numRecords;
        
        // Compute rB: smallest class is first class in the histogram
        rB = 1d / smallestClassSize;

        // Compute rC
        rC = numClasses / numRecords;
        
        // Return
        return new ProsecutorRisk(rA, rB, rC); 
    }
}
