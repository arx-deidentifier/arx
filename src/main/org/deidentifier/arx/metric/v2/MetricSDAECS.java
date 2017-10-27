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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the (normalized) average equivalence class size metric.
 * We dont normailze the metric as proposed in the original publication [1], as this would only be possible for k-anonymity.
 * [1] LeFevre K, DeWitt DJ, Ramakrishnan R. Mondrian Multidimensional K-Anonymity. IEEE; 2006:25-25.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Raffael Bild
 */
public class MetricSDAECS extends AbstractMetricSingleDimensional {

    /** SVUID. */
    private static final long serialVersionUID = 8076459507565472479L;

    /**
     * Creates a new instance.
     */
    protected MetricSDAECS() {
        super(true, false, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param gsFactor
     */
    protected MetricSDAECS(double gsFactor) {
        super(true, false, false, gsFactor);
    }

    /**
     * Creates a new instance. Preinitialized
     *
     * @param rowCount
     */
    protected MetricSDAECS(int rowCount) {
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
    public ILScore getScore(final Transformation node, final HashGroupify groupify) {
        
        // Calculate the number of all equivalence classes, regarding all suppressed records to belong to one class
        
        boolean hasSuppressed = false;
        int numberOfNonSuppressedClasses = 0;
        
        HashGroupifyEntry entry = groupify.getFirstEquivalenceClass();
        while (entry != null) {
            if (!entry.isNotOutlier && entry.count > 0 || entry.pcount > entry.count) {
                // The equivalence class is suppressed or contains records removed by sampling
                hasSuppressed = true;
            }
            if (entry.isNotOutlier && entry.count > 0) {
                // The equivalence class contains records which are not suppressed
                numberOfNonSuppressedClasses++;
            }
            // Next group
            entry = entry.nextOrdered;
        }
        
        // Return score
        return new ILScore((double)numberOfNonSuppressedClasses + (hasSuppressed ? 1d : 0d));
    }
    
    @Override
    public boolean isGSFactorSupported() {
        return true;
    }
    
    @Override
    public boolean isScoreFunctionSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Average equivalence class size");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }

    @Override
    public String toString() {
        return "Average equivalence class size";
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
}
