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

import java.util.Map;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implementation of the Precision measure, as proposed in:<br>
 * <br>
 * L. Sweeney: "Achieving k-anonymity privacy protection using generalization and suppression"
 * J Uncertain Fuzz Knowl Sys 10 (5) (2002) 571-588.
 * 
 * @author Fabian Prasser
 */
public class UtilityModelColumnOrientedPrecision extends UtilityModel<UtilityMeasureColumnOriented> {

    /** Header */
    private final int[]                 indices;
    /** Precision */
    private final Map<String, Double>[] precisions;

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    public UtilityModelColumnOrientedPrecision(WrappedBoolean interrupt,
                                               DataHandleInternal input,
                                               UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.precisions = getHelper().getPrecision(input, indices);
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
                    double precision = 1d;
                    if (!isSuppressed(output, indices, row)) {
                        Double temp = this.precisions[i].get(output.getValue(row, column));
                        precision = temp != null ? temp : 1d;
                    }
                    result[i] += precision;
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
            min[i] = 0d;
            max[i] = 1d;
        }

        // Return
        return new UtilityMeasureColumnOriented(output, indices, min, result, max);
    }
}
