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