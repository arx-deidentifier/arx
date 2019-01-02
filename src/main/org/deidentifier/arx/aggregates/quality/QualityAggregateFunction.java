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

/**
 * Aggregate function for multi-dimensional quality measures
 * 
 * @author Fabian Prasser
 */
public abstract class QualityAggregateFunction {
    
    /** A label for this function*/
    private final String label;
    
    /**
     * Arithmetic mean
     */
    static final QualityAggregateFunction ARITHMETIC_MEAN = new QualityAggregateFunction("Arithmetic mean"){
        @Override protected double aggregate(double[] values) {
            double result = 0d;
            double count = 0d;
            for (int i = 0; i< values.length; i++) {
                if (!Double.isNaN(values[i])) {
                    result += values[i];
                    count++;
                }
            }
            result /= count;
            return result;
        }
    };

    /**
     * Geometric mean
     */
    static final QualityAggregateFunction GEOMETRIC_MEAN = new QualityAggregateFunction("Geometric mean"){
        @Override protected double aggregate(double[] values) {
            double result = 1d;
            double count = 0d;
            for (int i = 0; i< values.length; i++) {
                if (!Double.isNaN(values[i])) {
                    result *= values[i] + 1d;
                    count++;
                }
            }
            result = Math.pow(result, 1d / count) - 1d;
            return result;
        }
    };
    
    /**
     * Max
     */
    static final QualityAggregateFunction MAX = new QualityAggregateFunction("Maximum"){
        @Override protected double aggregate(double[] values) {
            double result = - Double.MAX_VALUE;
            for (int i = 0; i< values.length; i++) {
                if (!Double.isNaN(values[i])) {
                    result = Math.max(result, values[i]);
                }
            }
            return result;
        }
    };
    
    /**
     * Creates a new instance
     * @param label
     */
    QualityAggregateFunction(String label) {
        this.label = label;
    }
    
    /**
     * Implement this
     * @param values
     * @return
     */
    protected abstract double aggregate(double[] values);
    
    /**
     * Returns the label
     * @return
     */
    protected String getLabel() {
        return this.label;
    }
}
