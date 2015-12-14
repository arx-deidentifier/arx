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

package org.deidentifier.arx.metric.v2;

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of a weighted precision metric as 
 * proposed in: <br>
 * Sweeney, L. (2002). Achieving k-anonymity privacy protection using generalization and suppression.<br> 
 * International Journal of Uncertainty Fuzziness and, 10(5), 2002.<br>
 * <br>
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNMPrecision extends AbstractMetricMultiDimensional {

    /** SVUID. */
    private static final long serialVersionUID = 7972929684708525849L;

    /** Row count. */
    private double            rowCount;

    /** Hierarchy heights. */
    private int[]             heights;

    /**
     * Creates a new instance.
     */
    protected MetricMDNMPrecision() {
        super(false, false, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNMPrecision(AggregateFunction function){
        super(false, false, function);
    }

    /**
     * For subclasses.
     *
     * @param monotonic
     * @param independent
     * @param function
     */
    protected MetricMDNMPrecision(boolean monotonic, boolean independent, AggregateFunction function){
        super(monotonic, independent, function);
    }

    /**
     * For subclasses.
     *
     * @param monotonic
     * @param independent
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMPrecision(boolean monotonic, boolean independent, double gsFactor, AggregateFunction function){
        super(monotonic, independent, gsFactor, function);
    }
    
    /**
     * Creates a new instance.
     * @param gsFactor
     */
    protected MetricMDNMPrecision(double gsFactor) {
        super(false, false, gsFactor, AggregateFunction.ARITHMETIC_MEAN);
    }

    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMPrecision(double gsFactor, AggregateFunction function){
        super(false, false, gsFactor, function);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                                       // monotonic
                                       super.getGeneralizationSuppressionFactor(),  // gs-factor
                                       false,                                       // precomputed
                                       0.0d,                                        // precomputation threshold
                                       this.getAggregateFunction()                  // aggregate function
                                       );
    }

    @Override
    public String toString() {
        return "Non-monotonic precision";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        
        double gFactor = super.getGeneralizationFactor();
        double sFactor = super.getSuppressionFactor();
        int suppressedTuples = 0;
        int unsuppressedTuples = 0;
        
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            // if (m.count > 0) is given implicitly
            unsuppressedTuples += m.isNotOutlier ? m.count : 0;
            suppressedTuples += m.isNotOutlier ? 0 : m.count;
            m = m.nextOrdered;
        }
        
        double[] result = new double[getDimensions()];
        for (int i = 0; i<heights.length; i++) {
            double value = heights[i] == 0 ? 0 : (double) node.getGeneralization()[i] / (double) heights[i];
            result[i] += ((double)unsuppressedTuples * value) * gFactor + (double)suppressedTuples * sFactor;
            result[i] /= rowCount;
        }
        
        // Return
        return new ILMultiDimensionalWithBound(createInformationLoss(result), 
                                               (AbstractILMultiDimensional)getLowerBoundInternal(node).clone());
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        double[] result = new double[getDimensions()];
        Arrays.fill(result, entry.count);
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        
        double gFactor = super.getGeneralizationFactor();
        double[] result = new double[getDimensions()];
        final int[] transformation = node.getGeneralization();
        for (int i = 0; i < transformation.length; i++) {
            double level = (double) transformation[i];
            result[i] += (double)(heights[i] == 0 ? 0 : (level / (double) heights[i])) * gFactor;
        }
        return createInformationLoss(result);
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                           HashGroupify groupify) {
       return getLowerBoundInternal(node);
    }

    /**
     * For backwards compatibility only.
     *
     * @param heights
     * @param cells
     */
    protected void initialize(int[] heights, double cells){

        super.initialize(heights.length);
        this.heights = heights;
        this.rowCount = cells / heights.length;
        double gFactor = super.getGeneralizationFactor();
        double sFactor = super.getSuppressionFactor();

        // Min and max
        double[] min = new double[heights.length];
        Arrays.fill(min, 0d);
        double[] max = new double[min.length];
        Arrays.fill(max, 1d * Math.max(gFactor, sFactor));
        setMin(min);
        setMax(max);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {

        super.initializeInternal(manager, definition, input, hierarchies, config);
        double gFactor = super.getGeneralizationFactor();
        double sFactor = super.getSuppressionFactor();

        // Min and max
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        double[] max = new double[min.length];
        Arrays.fill(max, 1d * Math.max(gFactor, sFactor));
        setMin(min);
        setMax(max);
        
        // Store row count
        rowCount = (double)super.getNumRecords(config, input);
        
        // Store heights
        this.heights = new int[hierarchies.length];
        for (int j = 0; j < heights.length; j++) {
            heights[j] = hierarchies[j].getArray()[0].length - 1;
        }
    }
    
    /**
     * Does this metric handle microaggregation
     * @return
     */
    protected boolean isAbleToHandleMicroaggregation() {
        return false;
    }
}