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

package org.deidentifier.arx.metric.v2;

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataAggregationInformation;
import org.deidentifier.arx.framework.data.DataManager;
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
    private static final long          serialVersionUID = 3909752748519119689L;

    /** The weights. */
    private double[]                   weights;

    /** Number of dimensions. */
    private int                        dimensions;

    /** Number of dimensions with generalization */
    private int                        dimensionsGeneralized;

    /** Number of dimensions with aggregation */
    private int                        dimensionsAggregated;

    /** Min. */
    private double[]                   min;

    /** Max. */
    private double[]                   max;

    /** The aggregate function. */
    private AggregateFunction          function;

    /** The microaggregation functions. */
    private DataAggregationInformation aggregation;

    /** Minimal size of equivalence classes enforced by the differential privacy model */
    protected int                      k                = -1;

    /**
     * Creates a new instance.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     * @param independent
     * @param function
     */
    AbstractMetricMultiDimensional(final boolean monotonicWithGeneralization,
                                   final boolean monotonicWithSuppression,
                                   final boolean independent,
                                   final AggregateFunction function) {
        super(monotonicWithGeneralization, monotonicWithSuppression, independent, 0.5d);
        this.function = function;
    }
    
    /**
     * Creates a new instance.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     * @param independent
     * @param gsFactor
     * @param function
     */
    AbstractMetricMultiDimensional(final boolean monotonicWithGeneralization,
                                   final boolean monotonicWithSuppression,
                                   final boolean independent,
                                   final double gsFactor,
                                   final AggregateFunction function) {
        super(monotonicWithGeneralization, monotonicWithSuppression, independent, gsFactor);
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
            throw new IllegalStateException("Metric must be intialized first. "+this.getClass().getSimpleName());
        } else {
            return createInformationLoss(min);
        }
    }
    
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
     * Returns relevant aggregation functions
     * @return
     */
    protected DistributionAggregateFunction[] getAggregationFunctionsGeneralized() {
        if (aggregation != null) {
            return aggregation.getHotQIsGeneralizedFunctions();
        } else {
            return null;
        }
    }

    /**
     * Returns relevant aggregation functions
     * @return
     */
    protected DistributionAggregateFunction[] getAggregationFunctionsNonGeneralized() {
        if (aggregation != null) {
            return aggregation.getHotQIsNotGeneralizedFunctions();
        } else {
            return null;
        }
    }

    /**
     * Returns the indicies of aggregated variables
     * @return
     */
    protected int[] getAggregationIndicesGeneralized() {
        if (aggregation != null) {
            return aggregation.getHotQIsGeneralized();
        } else {
            return null;
        }
    }
    
    /**
     * Returns the indicies of aggregated variables
     * @return
     */
    protected int[] getAggregationIndicesNonGeneralized() {
        if (aggregation != null) {
            return aggregation.getHotQIsNotGeneralized();
        } else {
            return null;
        }
    }
  
    /**
     * Needed for microaggregation
     * @return
     */
    protected DataAggregationInformation getAggregationInformation() {
        return this.aggregation;
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
     * Returns the number of dimensions.
     *
     * @return
     */
    protected int getDimensionsAggregated() {
        return dimensionsAggregated;
    }

    /**
     * Returns the number of dimensions.
     *
     * @return
     */
    protected int getDimensionsGeneralized() {
        return dimensionsGeneralized;
    }

    /**
     * For backwards compatibility only.
     *
     * @param dimensions
     */
    protected void initialize(int dimensions){
        this.weights = new double[dimensions];
        Arrays.fill(weights, 1d);
        this.dimensions = dimensions;
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {

        // Initialize
        this.aggregation = manager.getAggregationInformation();
        this.dimensionsGeneralized = hierarchies.length;
        this.dimensionsAggregated = this.aggregation.getHotQIsNotGeneralized().length;
        this.dimensions = this.dimensionsGeneralized + this.dimensionsAggregated;
        
        // Initialize weights
        this.weights = new double[this.dimensions];
        double maximum = 0d;
        for (int i = 0; i < this.dimensionsGeneralized; i++) {
            String attribute = hierarchies[i].getName();
            double weight = config.getAttributeWeight(attribute);
            this.weights[i] = weight;
            maximum = Math.max(maximum, weight);
        }
        for (int i = 0; i < this.dimensionsAggregated; i++) {
            String attribute = this.aggregation.getHeader()[this.aggregation.getHotQIsNotGeneralized()[i]];
            double weight = config.getAttributeWeight(attribute);
            this.weights[this.dimensionsGeneralized + i] = weight;
            maximum = Math.max(maximum, weight);
        }

        // Normalize: default case
        if (maximum == 0d) {
            Arrays.fill(this.weights, 1d);
        // Weighted case
        } else {
            for (int i=0; i<this.weights.length; i++){
                this.weights[i] /= maximum;
            }
        }
        
        // Min and max
        this.min = new double[this.dimensions];
        Arrays.fill(min, 0d);
        this.max = new double[this.dimensions];
        Arrays.fill(max, Double.MAX_VALUE);
        
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            // Store minimal size of equivalence classes
            EDDifferentialPrivacy dpCriterion = config.getPrivacyModel(EDDifferentialPrivacy.class);
            this.k = dpCriterion.getMinimalClassSize();
        }
    }

    /**
     * Sets the maximal information loss.
     *
     * @param max
     */
    protected void setMax(double[] max) {
        if (max.length != dimensions) {
            throw new IllegalArgumentException("Invalid number of dimensions");
        }
        this.max = max;
    }

    /**
     * Sets the minimal information loss.
     *
     * @param min
     */
    protected void setMin(double[] min) {
        if (min.length != dimensions) {
            throw new IllegalArgumentException("Invalid number of dimensions");
        }
        this.min = min;
    }
}
