/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

    /** SVUID*/
    private static final long serialVersionUID = 3909752748519119689L;

    /** The weights */
    private double[]                weights;

    /** Number of dimensions */
    private int                     dimensions;

    /** Min */
    private double[]                min;

    /** Max */
    private double[]                max;

    /** The aggregate function*/
    private final AggregateFunction function;

    /**
     * Creates a new instance
     * @param monotonic
     * @param independent
     */
    AbstractMetricMultiDimensional(final boolean monotonic,
                                     final boolean independent,
                                     final AggregateFunction function) {
        super(monotonic, independent);
        this.function = function;
    }
    

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (max == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return createInformationLoss(max);
        }
    }
    
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (min == null) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return createInformationLoss(min);
        }
    }
    
    @Override
    public AggregateFunction getAggregateFunction() {
        return this.function;
    }
  
    /**
     * Helper method for creating information loss
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
     * Helper method for creating information loss
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
     * Helper method for creating information loss
     * @param values
     * @return
     */
    protected ILMultiDimensionalWithBound createInformationLossWithoutBound(double[] values){
        return new ILMultiDimensionalWithBound(createInformationLoss(values));
    }

    /**
     * Returns the number of dimensions
     * @return
     */
    protected int getDimensions() {
        return dimensions;
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Initialize weights
        weights = new double[hierarchies.length];
        double total = 0d;
        for (int i = 0; i < hierarchies.length; i++) {
            String attribute = hierarchies[i].getName();
            double weight = config.getAttributeWeight(attribute);
            weights[i] = weight;
            total += weight;
        }
        
        // Normalize: default case
        if (total == 0d) {
            Arrays.fill(weights, 1d);
        // Weighted case
        } else {
            for (int i=0; i<weights.length; i++){
                weights[i] /= total;
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
     * Sets the maximal information loss
     * @param max
     */
    protected void setMax(double[] max) {
        this.max = max;
    }

    /**
     * Sets the minimal information loss
     * @param min
     */
    protected void setMin(double[] min) {
        this.min = min;
    }
}
