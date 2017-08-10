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
 * Implementation of the Loss measure, as proposed in:<br>
 * <br>
 * Iyengar, V.: Transforming data to satisfy privacy constraints. 
 * Proc Int Conf Knowl Disc Data Mining, p. 279-288 (2002)
 * 
 * @author Fabian Prasser
 */
public class UtilityModelColumnOrientedLoss extends UtilityModel<UtilityMeasureColumnOriented> {
    
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
    public UtilityModelColumnOrientedLoss(WrappedBoolean interrupt,
                                          DataHandleInternal input,
                                          UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.shares = getHelper().getDomainShares(input, indices);
    }
    
    @Override
    public UtilityMeasureColumnOriented evaluate(DataHandleInternal output) {
        
        // Prepare
        double[] result = new double[indices.length];
        double[] min = new double[indices.length];
        double[] max = new double[indices.length];
        
        // For each column
        for (int i = 0; i < result.length; i++) {
            
            // Map
            int column = indices[i];
            
            // For each row
            for (int row = 0; row < output.getNumRows(); row++) {
                
                try {
                    double share = 1d;
                    if (!isSuppressed(output, indices, row)) {
                        share = shares[i].getShare(output.getValue(row, column), 0);
                    }
                    result[i] += share;
                } catch (Exception e) {
                    // Silently catch exceptions
                    result[i] = Double.NaN;
                    break;
                }
                
                // Check
                checkInterrupt();
            }
        }

        // For each column
        for (int i = 0; i < result.length; i++) {
            result[i] /= (double)output.getNumRows();
            min[i] = shares[i].getDomainSize() == 0d ? 0d : 1d / shares[i].getDomainSize();
            max[i] = 1d;
        }

        // Return
        return new UtilityMeasureColumnOriented(output, indices, min, result, max);
    }
}
