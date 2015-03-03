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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.metric.Metric.AggregateFunction;

/**
 * A class describing a metric and its configuration options.
 *
 * @author Fabian Prasser
 */
public abstract class MetricDescription implements Serializable {

    /** SVUID. */
    private static final long serialVersionUID = -2774981286637344244L;
    
    /** Name. */
    private final String  name;
    
    /** Monotonic variant supported. */
    private final boolean monotonicVariantSupported;
    
    /** Attribute weights supported. */
    private final boolean attributeWeightsSupported;
    
    /** Configurable coding model supported. */
    private final boolean configurableCodingModelSupported;
    
    /** Pre-computation supported. */
    private final boolean precomputationSupported;
    
    /** Aggregate functions supported. */
    private final boolean aggregateFunctionSupported;
    
    /**
     * Creates a new description.
     *
     * @param name
     * @param monotonicVariantSupported
     * @param attributeWeightsSupported
     * @param configurableCodingModelSupported
     * @param precomputationSupported
     * @param aggregateFunctionSupported
     */
    MetricDescription(String name,
                              boolean monotonicVariantSupported,
                              boolean attributeWeightsSupported,
                              boolean configurableCodingModelSupported,
                              boolean precomputationSupported,
                              boolean aggregateFunctionSupported) {
        this.name = name;
        this.monotonicVariantSupported = monotonicVariantSupported;
        this.attributeWeightsSupported = attributeWeightsSupported;
        this.configurableCodingModelSupported = configurableCodingModelSupported;
        this.precomputationSupported = precomputationSupported;
        this.aggregateFunctionSupported = aggregateFunctionSupported;
    }

    /**
     * Creates an instance with the given configuration options.
     *
     * @param config
     * @return
     */
    public abstract Metric<?> createInstance(MetricConfiguration config);

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a list of all supported aggregate functions.
     *
     * @return
     */
    public List<Metric.AggregateFunction> getSupportedAggregateFunctions() {
        if (!aggregateFunctionSupported) {
            return new ArrayList<Metric.AggregateFunction>(0);
        } else {
            return Arrays.asList(new Metric.AggregateFunction[] {  AggregateFunction.SUM,
                                                            AggregateFunction.MAXIMUM,
                                                            AggregateFunction.ARITHMETIC_MEAN,
                                                            AggregateFunction.GEOMETRIC_MEAN,
                                                            AggregateFunction.RANK });
        }
    }

    /**
     * @return the attributeWeightsSupported
     */
    public boolean isAttributeWeightsSupported() {
        return attributeWeightsSupported;
    }

    /**
     * @return the configurableCodingModelSupported
     */
    public boolean isConfigurableCodingModelSupported() {
        return configurableCodingModelSupported;
    }
    
    /**
     * Returns whether an aggregate function is supported by the metric.
     *
     * @return
     */
    public boolean isAggregateFunctionSupported() {
        return aggregateFunctionSupported;
    }
    
    /**
     * Returns whether the given metric is an instance of this description.
     *
     * @param metric
     * @return
     */
    public abstract boolean isInstance(Metric<?> metric);
    
    /**
     * @return the monotonicVariantSupported
     */
    public boolean isMonotonicVariantSupported() {
        return monotonicVariantSupported;
    }

    /**
     * @return the precomputationSupported
     */
    public boolean isPrecomputationSupported() {
        return precomputationSupported;
    }
}