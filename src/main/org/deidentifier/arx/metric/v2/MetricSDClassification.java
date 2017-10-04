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

    /** Penalty. TODO: Make configurable via ARXConfiguration */
    private double            penaltySuppressed               = 0.5d;
    /** Penalty. TODO: Make configurable via ARXConfiguration */
    private double            penaltyDifferentFromMajority    = 1d;
    /** Penalty. TODO: Make configurable via ARXConfiguration */
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
            return new ILSingleDimensional(rows);
        }
    }
    
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(1d);
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

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {

        // The total number of groups with and without suppression
        double groupsWithSuppression = 0;
        double groupsWithoutSuppression = 0;
        double gFactor = super.getSuppressionFactor(); // Note: factors are switched on purpose
        double sFactor = super.getGeneralizationFactor(); // Note: factors are switched on purpose
        
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0) {
                groupsWithSuppression += m.isNotOutlier ? 1 : 0;
                groupsWithoutSuppression++;
            }
            m = m.nextOrdered;
        }
        
        // If there are suppressed tuples, they form one additional group
        boolean someRecordsSuppressed = (groupsWithSuppression != groupsWithoutSuppression);
        groupsWithSuppression *= gFactor;
        groupsWithSuppression = !someRecordsSuppressed ? groupsWithSuppression : groupsWithSuppression + 1 * sFactor;
        
        // Compute AECS
        return new ILSingleDimensionalWithBound(getNumTuples() / groupsWithSuppression,
                                                getNumTuples() / (groupsWithoutSuppression * gFactor));
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        return new ILSingleDimensionalWithBound(entry.count);
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node,
                                                        HashGroupify groupify) {
        // Ignore suppression for the lower bound
        int groups = 0;
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            groups += (m.count > 0) ? 1 : 0;
            m = m.nextOrdered;
        }
        
        // Compute AECS
        double gFactor = super.getSuppressionFactor(); // Note: factors are switched on purpose
        return new ILSingleDimensional(getNumTuples() / ((double)groups * gFactor));
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
                break;
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
}
