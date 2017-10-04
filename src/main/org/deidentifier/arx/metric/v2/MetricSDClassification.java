/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.metric.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the classification metric.
 * 
 * @author Fabian Prasser
 * @author Johanna Eicher
 */
public class MetricSDClassification extends AbstractMetricSingleDimensional {

    /** SVUID. */
    private static final long serialVersionUID = -7940144844158472876L;

    /** Indices of response variables in distributions */
    private int[]             responseVariables               = null;
    /** Number of response variables in quasi-identifiers */
    private int               responseVariablesNotAnalyzed = 0;

    /** Penalty. TODO: Make configurable */
    private double            penaltySuppressed               = 0.5d;
    /** Penalty. TODO: Make configurable */
    private double            penaltyDifferentFromMajority    = 1d;
    /** Penalty. TODO: Make configurable */
    private double            penaltyNoMajority               = 1d;

    /**
     * Creates a new instance.
     */
    protected MetricSDClassification() {
        super(true, false, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param gsFactor
     */
    protected MetricSDClassification(double gsFactor) {
        super(true, false, false, gsFactor);
    }

    /**
     * Creates a new instance. Preinitialized
     *
     * @param rowCount
     */
    protected MetricSDClassification(int rowCount) {
        super(true, false, false);
        super.setNumTuples((double)rowCount);
    }
    
    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            // Not analyzed response variables are only penalized if they are suppressed
            double max = rows * responseVariablesNotAnalyzed * penaltySuppressed;
            // Use maximal penalty for other response variables
            max += rows * responseVariables.length * Math.max(penaltySuppressed, Math.max(penaltyDifferentFromMajority, penaltyNoMajority));
            // Normalize by number of cells
            return new ILSingleDimensional(max);
        }
    }
    
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(0d);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false, // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       false, // precomputed
                                       0.0d, // precomputation threshold
                                       AggregateFunction.SUM // aggregate function
        );
    }
    
    @Override
    public boolean isGSFactorSupported() {
        return true;
    }
    
    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Classification metric");
        result.addProperty("Monotonic", this.isMonotonic(config.getMaxOutliers()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }

    @Override
    public String toString() {
        return "Classification metric";
    }
    
    /**
     * Returns an array of length 3: [0]: number of suppressed cells, [1]:
     * number of cells with a different class label than the majority class
     * label, [2]: number of cells in an entry without majority class label.
     * 
     * @param entry
     * @return
     */
    private int[] getCountsForEntry(HashGroupifyEntry entry, int index) {
        // [suppressed, differentThanMajority, noMajority]
        int[] counts = new int[3];

        // Suppressed
        if (!entry.isNotOutlier) {
            counts[0] = entry.count;
        } else {
            int[] buckets = entry.distributions[index].getBuckets();
            List<Integer> frequencies = new ArrayList<Integer>();
            for (int i = 0; i < buckets.length; i += 2) {
                int frequencyInC = buckets[i + 1];
                frequencies.add(frequencyInC);
            }
            Collections.sort(frequencies);
            Collections.reverse(frequencies);
            // If first (max) element exists once, this is the frequency of the majority class label
            boolean majorityClassExists = Collections.frequency(frequencies, frequencies.get(0)) == 1;

            if (majorityClassExists) {
                // Records with other than majority class label get penalized
                counts[1] = entry.count - frequencies.get(0);
            } else {
                // All records get penalized
                counts[2] = entry.count;
            }
        }
        return counts;
    }
    
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        // Counts for suppressed cells [0], cells with class label different from majority class label [1], cells in entry without majority class label [2]
        int[] counts = new int[3];

        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0) {
                updateEntryCounts(counts, m);
            }
            m = m.nextOrdered;
        }

        // Collect penalties
        double penalties = counts[0] * penaltySuppressed + counts[1] * penaltyDifferentFromMajority + counts[2] * penaltyNoMajority;

        // Return
        return new ILSingleDimensionalWithBound(penalties);
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        double result = 0;
        if (entry.count > 0) {
            // Counts for suppressed cells [0], cells with class label different from majority class label [1], cells in entry without majority class label [2]
            int[] counts = new int[3];
            updateEntryCounts(counts, entry);
            // Collect penalties
            result = counts[0] * penaltySuppressed + counts[1] * penaltyDifferentFromMajority + counts[2] * penaltyNoMajority;
        }
        return new ILSingleDimensionalWithBound(result);
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node,
                                                        HashGroupify groupify) {
        return null;
    }

    @Override
    protected void initializeInternal(DataManager manager,
                                      DataDefinition definition,
                                      Data input,
                                      GeneralizationHierarchy[] hierarchies,
                                      ARXConfiguration config) {

        // Super
        super.initializeInternal(manager, definition, input, hierarchies, config);
        
        // Extract indices of response variables
        List<Integer> indices = new ArrayList<>();
        for (String variable : definition.getResponseVariables()){
            int index = manager.getDataAnalyzed().getIndexOf(variable);
            if (index != -1) {
                indices.add(index);
            }
        }
        
        // Store
        Collections.sort(indices);
        this.responseVariables = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            responseVariables[i] = indices.get(i);
        }
        this.responseVariablesNotAnalyzed = definition.getResponseVariables().size() - responseVariables.length;
    }
    
    /**
     * For an entry, update counts for suppressed cells, cells with class label different from majority class label and cells in entry without majority class label.
     * @param counts
     * @param m
     */
    private void updateEntryCounts(int[] counts, HashGroupifyEntry m) {
        // For each response variable
        for (int index : responseVariables) {
            // Get counts for suppressed, different from majority and no majority class
            int[] countsForVar = getCountsForEntry(m, index);
            counts[0] += countsForVar[0];
            counts[1] += countsForVar[1];
            counts[2] += countsForVar[2];
        }

        // Response variables not analyzed, count cells if suppressed
        counts[0] += m.isNotOutlier ? 0 : responseVariablesNotAnalyzed;
    }
}
