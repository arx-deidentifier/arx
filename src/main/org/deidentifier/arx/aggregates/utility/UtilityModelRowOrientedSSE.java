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
 * Implementation of the Sum of Squared Errors introduced in:<br>
 * Soria-Comas, Jordi, et al.:
 * "t-closeness through microaggregation: Strict privacy with enhanced utility preservation."
 * IEEE Transactions on Knowledge and Data Engineering 27.11 (2015): 3098-3110.
 * 
 * @author Fabian Prasser
 */
public class UtilityModelRowOrientedSSE extends UtilityModel<UtilityMeasureRowOriented> {

    /** Header */
    private final int[]                indices;
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    public UtilityModelRowOrientedSSE(WrappedBoolean interrupt,
                                      DataHandleInternal input,
                                      UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);

    }
    
    /**
     * Output: the output dataset without header
     */
    @Override
    public UtilityMeasureRowOriented evaluate(DataHandleInternal handle) {
       
        return null;
    }
    
    /**
     * Returns the euclidean distance between records
     * 
     * @param columns1
     * @param columns2
     * @param standardDeviations
     * @param row
     * @return
     */
    private double getEuclideanDistance(double[][] columns1, 
                                        double[][] columns2,
                                        double[] standardDeviations,
                                        int row) {

        // Prepare
        row *= 2;
        double result = 0;
        
        // For each column
        for(int column=0; column<columns1.length; column++){
            
            double minimum1 = columns1[column][row];
            double maximum1 = columns1[column][row + 1];
            double minimum2 = columns2[column][row];
            double maximum2 = columns2[column][row + 1];
            maximum1 = (maximum2 - minimum1) > (minimum1 - minimum2) ? maximum2 : minimum2;
            double temp = (standardDeviations[column] == 0d) ? 0d : (minimum1 - maximum1) / standardDeviations[column];
            result += (temp * temp);
        }
        
        // Return
        return Math.sqrt(result);
    }
}
