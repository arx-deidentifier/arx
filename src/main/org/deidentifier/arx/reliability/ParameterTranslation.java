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
package org.deidentifier.arx.reliability;

/**
 * Class supporting parameter calculations and translations.
 * 
 * @author Fabian Prasser
 */
public class ParameterTranslation {
    
    /** Relative threshold precision*/
    public static final double RELATIVE_THRESHOLD_PRECISION = 0.01d;

    /**
     * Returns a minimal class size for the given risk threshold
     * @param threshold
     * @return
     */
    public static int getSizeThreshold(double riskThreshold) {
        
        // Check
        if (riskThreshold < 0d || riskThreshold >1d) {
            throw new IllegalArgumentException("Invalid threshold");
        }
        
        // Special case
        if (riskThreshold == 0d) {
            return Integer.MAX_VALUE;
        }
        
        // Calculate
        double size = 1d / riskThreshold;
        double floor = Math.floor(size);
        if ((1d / floor) - (1d / size) >= RELATIVE_THRESHOLD_PRECISION * riskThreshold) {
            floor += 1d;
        }
        
        // Return
        return (int)floor;
    }
    
    /**
     * Returns the effective risk threshold
     * @param riskThreshold
     * @return
     */
    public static double getEffectiveRiskThreshold(double riskThreshold) {
        return 1d / getSizeThreshold(riskThreshold);
    }
}
