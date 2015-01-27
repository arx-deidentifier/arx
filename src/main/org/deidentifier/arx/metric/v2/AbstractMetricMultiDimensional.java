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
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This class provides an abstract skeleton for the implementation of multi-dimensional metrics.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractMetricMultiDimensional extends Metric<AbstractILMultiDimensional> {

    /** SVUID. */
    private static final long serialVersionUID = 3909752748519119689L;

    /** The weights. */
    private double[]                weights;

    /** Number of dimensions. */
    private int                     dimensions;

    /** Min. */
    private double[]                min;

    /** Max. */
    private double[]                max;

    /** The aggregate function. */
    private final AggregateFunction function;

    /**
     * Creates a new instance.
     *
     * @param monotonic
     * @param independent
     * @param function
     */
    AbstractMetricMultiDimensional(final boolean monotonic,
                                   final boolean independent,
                                   final AggregateFunction function) {
        super(monotonic, independent);
        this.function = function;
    }
    

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (max == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return createInformationLoss(max);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (min == null) {
            throw new IllegalStateException("Metric must be intialized first. "+this.getClass().getSimpleName());
        } else {
            return createInformationLoss(min);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getAggregateFunction()
     */
    @Override
    public AggregateFunction getAggregateFunction() {
        return this.function;
    }
  
    /**
     * Helper method for creating information loss.
     *
     * @param values
     * @return
     */
    protected AbstractILMultiDimensional createInformationLoss(double[] values){

        switch (function){
        case ARITHMETIC_MEAN:
            return new ILMultiDimensionalArithmeticMean(values, weights);
        case GEOMETRIC_MEAN:
            return new ILMultiDimensionalGeometricMean(values, weights);
        case MAXIMUM:
            return new ILMultiDimensionalMax(values, weights);
        case RANK:
            return new ILMultiDimensionalRank(values, weights);
        case SUM:
            return new ILMultiDimensionalSum(values, weights);
        default:
            throw new IllegalStateException("Unknown aggregate function: "+function);
        }
    }


    /**
     * Helper method for creating information loss.
     *
     * @param values
     * @param bound
     * @return
     */
    protected ILMultiDimensionalWithBound createInformationLossWithBound(double[] values,
                                                                         double[] bound){
        return new ILMultiDimensionalWithBound(createInformationLoss(values),
                                               createInformationLoss(bound));
    }

    /**
     * Helper method for creating information loss.
     *
     * @param values
     * @return
     */
    protected ILMultiDimensionalWithBound createInformationLossWithoutBound(double[] values){
        return new ILMultiDimensionalWithBound(createInformationLoss(values));
    }

    /**
     * Returns the number of dimensions.
     *
     * @return
     */
    protected int getDimensions() {
        return dimensions;
    }
    
    /**
     * For backwards compatibility only.
     *
     * @param dimensions
     */
    protected void initialize(int dimensions){
        this.weights = new double[dimensions];
        Arrays.fill(weights, 1d);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Initialize weights
        weights = new double[hierarchies.length];
        double maximum = 0d;
        for (int i = 0; i < hierarchies.length; i++) {
            String attribute = hierarchies[i].getName();
            double weight = config.getAttributeWeight(attribute);
            weights[i] = weight;
            maximum = Math.max(maximum, weight);
        }
        
        // Normalize: default case
        if (maximum == 0d) {
            Arrays.fill(weights, 1d);
        // Weighted case
        } else {
            for (int i=0; i<weights.length; i++){
                weights[i] /= maximum;
            }
        }
        
        // Initialize dimensions
        dimensions = hierarchies.length;
        
        // Min and max
        this.min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        this.max = new double[min.length];
        Arrays.fill(max, Double.MAX_VALUE);
    }

    /**
     * Sets the maximal information loss.
     *
     * @param max
     */
    protected void setMax(double[] max) {
        this.max = max;
    }

    /**
     * Sets the minimal information loss.
     *
     * @param min
     */
    protected void setMin(double[] min) {
        this.min = min;
    }
}
