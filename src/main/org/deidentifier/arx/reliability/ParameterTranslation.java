/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
