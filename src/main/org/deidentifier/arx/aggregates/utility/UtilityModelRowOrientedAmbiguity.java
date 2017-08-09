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
import org.deidentifier.arx.metric.v2.DomainShare;

/**
 * Implementation of the Ambiguity measure, as described in:<br>
 * <br>
 * "Goldberger, Tassa: Efficient Anonymizations with Enhanced Utility
 * 
 * @author Fabian Prasser
 */
class UtilityModelRowOrientedAmbiguity extends UtilityModelRowOriented {
    
    /** Domain shares */
    private final DomainShare shares;
    /** Header */
    private final String[]    header;
    

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     */
    UtilityModelRowOrientedAmbiguity(WrappedBoolean interrupt, DataHandleInternal input) {
        super(interrupt, input);
    
        this.shares = new DomainShare(hierarchies, header);
    }
    
    @Override
    double evaluate(DataHandleInternal output, int[] transformation) {
        
        double result = 0d;
        for (String[] row : input) {
            double resultRow = 1d;
            for (int i = 0; i < row.length; i++) {
                resultRow *= shares.getShare(header[i], row[i], transformation[i]) * shares.domainSize[i];
            }
            result += resultRow;
        }
        return result;
    }
    
}
