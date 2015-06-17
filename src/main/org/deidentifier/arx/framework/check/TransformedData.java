/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.framework.check.groupify.HashGroupify.EquivalenceClassStatistics;
import org.deidentifier.arx.framework.data.Data;

/**
 * Helper class to convey buffers.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *
 */
public class TransformedData {
    
    /** The GH buffer */
    public Data            bufferGeneralized;
    
    /** The OT buffer */
    public Data            bufferMicroaggregated;
    
    /** The group statistic */
    public EquivalenceClassStatistics statistics;
    
    /** The properties*/
    public NodeChecker.Result properties;
    
    /**
     * Instantiate the helper object.
     * 
     * @param bufferGH
     * @param bufferOT
     * @param statistics
     */
    public TransformedData(Data bufferGH, Data bufferOT, EquivalenceClassStatistics statistics,
                           NodeChecker.Result properties) {
        this.bufferGeneralized = bufferGH;
        this.bufferMicroaggregated = bufferOT;
        this.statistics = statistics;
        this.properties = properties;
    }
}
