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

package org.deidentifier.arx.aggregates.quality;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Loss measure, as proposed in:<br>
 * <br>
 * Iyengar, V.: Transforming data to satisfy privacy constraints. 
 * Proc Int Conf Knowl Disc Data Mining, p. 279-288 (2002)
 * 
 * @author Fabian Prasser
 */
public class QualityModelColumnOrientedLoss extends QualityModel<QualityMeasureColumnOriented> {
    

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param input
     * @param output
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    public QualityModelColumnOrientedLoss(WrappedBoolean interrupt,
                                          DataHandleInternal input,
                                          DataHandleInternal output,
                                          Groupify<TupleWrapper> groupedInput,
                                          Groupify<TupleWrapper> groupedOutput,
                                          String[][][] hierarchies,
                                          QualityDomainShare[] shares,
                                          int[] indices,
                                          QualityConfiguration config) {
        super(interrupt,
              input,
              output,
              groupedInput,
              groupedOutput,
              hierarchies,
              shares,
              indices,
              config);
    }
    
    @Override
    public QualityMeasureColumnOriented evaluate() {
        
        // Prepare
        int[] indices = getIndices();
        DataHandleInternal output = getOutput();
        QualityDomainShare[] shares = getDomainShares();
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
        return new QualityMeasureColumnOriented(output, indices, min, result, max);
    }
}
