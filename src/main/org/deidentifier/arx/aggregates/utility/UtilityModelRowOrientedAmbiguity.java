/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.aggregates.utility;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Ambiguity measure, as described in:<br>
 * <br>
 * Goldberger, Tassa: Efficient Anonymizations with Enhanced Utility
 * 
 * @author Fabian Prasser
 */
class UtilityModelRowOrientedAmbiguity extends UtilityModel<UtilityMeasureRowOriented> {
    
    /** Header */
    private final int[]                indices;
    /** Domain shares */
    private final UtilityDomainShare[] shares;
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    UtilityModelRowOrientedAmbiguity(WrappedBoolean interrupt,
                                     DataHandleInternal input,
                                     UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.shares = getHelper().getDomainShares(input, indices);
    }
    
    @Override
    UtilityMeasureRowOriented evaluate(DataHandleInternal output) {
        
        double min = 0d;
        double result = 0d;
        double max = 0d;
        try {
            for (int row = 0; row < output.getNumRows(); row++) {
                double rowMin = 1d;
                double rowResult = 1d;
                double rowMax = 1d;
                for (int i = 0; i < indices.length; i++) {
                    int column = indices[i];
                    rowResult *= shares[i].getShare(output.getValue(row, column), 0) * shares[i].getDomainSize();
                    rowMin *= 1d;
                    rowMax *= shares[i].getDomainSize();
                }
                min += rowMin;
                result += rowResult;
                max += rowMax;
            }
            return new UtilityMeasureRowOriented(min, result, max);
        } catch (Exception e) {
            // Silently catch exceptions
            return new UtilityMeasureRowOriented(Double.NaN, Double.NaN, Double.NaN);
        }
    }
}
