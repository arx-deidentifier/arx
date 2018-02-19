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
 * Class supporting parameter calculations and translations
 * 
 * @author Fabian Prasser
 */
public class ParameterTranslation {

    /**
     * Returns a minimal class size for the given risk threshold
     * TODO: There are similar issues in multiple privacy models, e.g. in the game-theoretic model
     * TODO: This should be fixed once and for all
     * @param threshold
     * @return
     */
    private Integer getSizeThreshold(double riskThreshold) {
        double size = 1d / riskThreshold;
        double floor = Math.floor(size);
        if ((1d / floor) - (1d / size) >= 0.01d * riskThreshold) {
            floor += 1d;
        }
        return (int)floor;
    }
    
    if (this.config.getAdversaryGain() == 0) {
        this.k = 1;
    } else if (Double.isInfinite(threshold)) {
        this.k = Integer.MAX_VALUE;
    } else if ((threshold == Math.floor(threshold))) {
        this.k = (int) threshold + 1;
    } else {
        this.k = (int)Math.ceil(threshold);
    }
    
    // See also floating point issues in
    MetricSDNMEntropyBasedInformationLoss
}
