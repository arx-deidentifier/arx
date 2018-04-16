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

package org.deidentifier.arx.aggregates.quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataHandle;

/**
 * Quality measures for individual attributes. Results are reported in range [0, 1].
 * Higher is better.
 * 
 * @author Fabian Prasser
 */
public class QualityMeasureColumnOriented {

    /** Values */
    private final Map<String, Double> result;

    /** Values */
    private final Map<String, Double> minimum;

    /** Values */
    private final Map<String, Double> maximum;

    /**
     * Creates an empty instance
     */
    public QualityMeasureColumnOriented() {
        this.result = null;
        this.minimum = null;
        this.maximum = null;
    }

    /**
     * Creates a new instance for input data
     * @param handle
     * @param indices
     */
    public QualityMeasureColumnOriented(DataHandle handle,
                                        int[] indices) {

        // Prepare
        this.result = new HashMap<>();
        this.minimum = new HashMap<>();
        this.maximum = new HashMap<>();
        
        // Convert
        for (int i = 0; i < indices.length; i++) {
            int column = indices[i];
            String attribute = handle.getAttributeName(column);
            this.minimum.put(attribute, 0d);
            this.result.put(attribute, 0d);
            this.maximum.put(attribute, 1d);
        }
    }
    
    /**
     * Creates a new instance
     * @param handle
     * @param indices
     * @param minimum
     * @param result
     * @param maximum
     */
    public QualityMeasureColumnOriented(DataHandle handle,
                                        int[] indices,
                                        double[] minimum,
                                        double[] result,
                                        double[] maximum) {
        
        // Prepare
        this.result = new HashMap<>();
        this.minimum = new HashMap<>();
        this.maximum = new HashMap<>();
        
        // Convert
        for (int i = 0; i < indices.length; i++) {
            int column = indices[i];
            if (!Double.isNaN(result[i])) {
                String attribute = handle.getAttributeName(column);
                this.minimum.put(attribute, minimum[i]);
                this.result.put(attribute, result[i]);
                this.maximum.put(attribute, maximum[i]);
            }
        }
    }
    
    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @return
     */
    public double getArithmeticMean() {
        return this.getArithmeticMean(false);
    }

    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @param normalizeBeforeAggregation
     * @return
     */
    public double getArithmeticMean(boolean normalizeBeforeAggregation) {
        if (result == null) {
            return Double.NaN;
        }
        double val = getAggregate(QualityAggregateFunction.ARITHMETIC_MEAN, normalizeBeforeAggregation);
        val = val < 0d ? 0d : val; // Truncate
        val = val > 1d ? 1d : val; // Truncate
        return 1d - val;
    }
    
    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @return
     */
    public double getGeometricMean() {
        return this.getGeometricMean(false);
    }
    
    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @param normalizeBeforeAggregation
     * @return
     */
    public double getGeometricMean(boolean normalizeBeforeAggregation) {
        if (result == null) {
            return Double.NaN;
        }
        double val = getAggregate(QualityAggregateFunction.GEOMETRIC_MEAN, normalizeBeforeAggregation);
        val = val < 0d ? 0d : val; // Truncate
        val = val > 1d ? 1d : val; // Truncate
        return 1d - val;
    }
    
    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @return
     */
    public double getMax() {
        return this.getMax(false);
    }

    /**
     * Returns an aggregate, or NaN if not available. Higher is better.
     * @param normalizeBeforeAggregation
     * @return
     */
    public double getMax(boolean normalizeBeforeAggregation) {
        if (result == null) {
            return Double.NaN;
        }
        double val = getAggregate(QualityAggregateFunction.MAX, normalizeBeforeAggregation);
        val = val < 0d ? 0d : val; // Truncate
        val = val > 1d ? 1d : val; // Truncate
        return 1d - val;
    }

    /**
     * Returns the normalized [0, 1] value for the given attribute. Higher is better.
     * @param attribute
     * @return
     */
    public double getValue(String attribute) {
        if (result == null) {
            return Double.NaN;
        }
        Double val = result.get(attribute);
        Double min = minimum.get(attribute);
        Double max = maximum.get(attribute);
        val = (val != null && min != null && max != null ? ((val - min) / (max - min)) : Double.NaN);
        val = val < 0d ? 0d : val; // Truncate
        val = val > 1d ? 1d : val; // Truncate
        return 1d - val;
    }
    

    /**
     * Returns whether a value is available for the given attribute
     * @param attribute
     * @return
     */
    public boolean isAvailable(String attribute) {
        return result != null && result.containsKey(attribute) && !Double.isNaN(result.get(attribute));
    }
    
    
    /**
     * Internal aggregation
     * @param function
     * @param normalizeBeforeAggregation
     * @return
     */
    private double getAggregate(QualityAggregateFunction function,
                                boolean normalizeBeforeAggregation) {
        try {
            if (normalizeBeforeAggregation) {
                Map<String, Double> normalizedResult = new HashMap<>(result);
                for (String key : normalizedResult.keySet()) {
                    double min = minimum.get(key);
                    double max = maximum.get(key);
                    double normalized = (normalizedResult.get(key) - min) / (max - min);
                    normalizedResult.put(key, normalized);
                }
                return function.aggregate(toArray(normalizedResult));
            } else {
                double aggregated = function.aggregate(toArray(result));
                double min = function.aggregate(toArray(minimum));
                double max = function.aggregate(toArray(maximum));
                return (aggregated - min) / (max - min);
            }
        } catch (Exception e) {
            // Fail silently
            return Double.NaN;
        }
    }

    /**
     * Map to array
     * @param map
     * @return
     */
    private double[] toArray(Map<String, Double> map) {
        List<Double> list = new ArrayList<>();
        for (Double value : map.values()) {
            if (!Double.isNaN(value)) {
                list.add(value);
            }
        }
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}
