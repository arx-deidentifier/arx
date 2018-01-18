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
 * A measure for the complete dataset. Results are reported in range [0, 1].
 * Higher is better.
 * 
 * @author Fabian Prasser
 */
public class QualityMeasureRowOriented {

    /** Value*/
    private final double result;

    /** Value*/
    private final double minimum;

    /** Value*/
    private final double maximum;

    /**
     * Creates an empty instance
     */
    public QualityMeasureRowOriented() {
        this.result = Double.NaN;
        this.minimum = Double.NaN;
        this.maximum = Double.NaN;
    }

    /**
     * Creates a new instance
     * @param minimum
     * @param result
     * @param maximum
     */
    public QualityMeasureRowOriented(double minimum, double result, double maximum) {
        this.result = result;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Returns the normalized [0,1] value. Higher is better.
     * @return
     */
    public double getValue() {
        double val = (this.result - this.minimum) / (this.maximum - this.minimum);
        val = val < 0d ? 0d : val; // Truncate
        val = val > 1d ? 1d : val; // Truncate
        return 1d - val;
    }
    
    /**
     * Returns whether this value is available
     * @return
     */
    public boolean isAvailable() {
        return !Double.isNaN(result);
    }
}
