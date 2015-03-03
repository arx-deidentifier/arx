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

package org.deidentifier.arx.metric;

import java.io.Serializable;

import org.deidentifier.arx.metric.Metric.AggregateFunction;

/**
 * A class a configuration of a metric.
 *
 * @author Fabian Prasser
 */
public class MetricConfiguration implements Serializable {

    /** SVUID. */
    private static final long serialVersionUID = 871854276489749340L;

    /** Monotonic variant. */
    private boolean           monotonic               = false;
    
    /** Coding model. */
    private double            gsFactor                = 0.5d;
    
    /** Precomputed. */
    private boolean           precomputed             = false;
    
    /** Precomputation threshold. */
    private double            precomputationThreshold = 0.1d;
    
    /** Aggregate function. */
    private AggregateFunction aggregateFunction       = AggregateFunction.RANK;
    
    /**
     * Constructs a new instance.
     *
     * @param monotonic
     * @param gsFactor
     * @param precomputed
     * @param precomputationThreshold
     * @param aggregateFunction
     */
    public MetricConfiguration(boolean monotonic,
                                double gsFactor,
                                boolean precomputed,
                                double precomputationThreshold,
                                AggregateFunction aggregateFunction) {
        this.monotonic = monotonic;
        this.gsFactor = gsFactor;
        this.precomputed = precomputed;
        this.precomputationThreshold = precomputationThreshold;
        this.aggregateFunction = aggregateFunction;
    }

    /**
     * @return the monotonic
     */
    public boolean isMonotonic() {
        return monotonic;
    }
    
    /**
     * @param monotonic the monotonic to set
     */
    public void setMonotonic(boolean monotonic) {
        this.monotonic = monotonic;
    }
    
    /**
     * @return the gsFactor
     */
    public double getGsFactor() {
        return gsFactor;
    }
    
    /**
     * @param gsFactor the gsFactor to set
     */
    public void setGsFactor(double gsFactor) {
        this.gsFactor = gsFactor;
    }
    
    /**
     * @return the precomputed
     */
    public boolean isPrecomputed() {
        return precomputed;
    }
    
    /**
     * @param precomputed the precomputed to set
     */
    public void setPrecomputed(boolean precomputed) {
        this.precomputed = precomputed;
    }
    
    /**
     * @return the precomputationThreshold
     */
    public double getPrecomputationThreshold() {
        return precomputationThreshold;
    }
    
    /**
     * @param precomputationThreshold the precomputationThreshold to set
     */
    public void setPrecomputationThreshold(double precomputationThreshold) {
        this.precomputationThreshold = precomputationThreshold;
    }
    
    /**
     * @return the aggregateFunction
     */
    public AggregateFunction getAggregateFunction() {
        return aggregateFunction;
    }
    
    /**
     * @param aggregateFunction the aggregateFunction to set
     */
    public void setAggregateFunction(AggregateFunction aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }
}