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

package org.deidentifier.arx.framework.data;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.metric.v2.DomainShare;

/**
 * A data manager that is projected onto a given subset. Used for post-optimization.
 * 
 * @author Fabian Prasser
 */
public class DataManagerSubset extends DataManager {

    /** The source manager*/
    private final DataManager source;
    
    /** 
     * Constructor
     * @param source
     * @param dataAnalyzed
     * @param dataGeneralized
     * @param dataInput
     * @param definition
     * @param shares
     * @param header
     * @param hierarchiesGeneralized
     * @param hierarchiesAnalyzed
     * @param generalizationLevelsMinimum
     * @param generalizationLevelsMaximum
     * @param microaggregationData
     */
    protected DataManagerSubset(DataManager source,
                                Data dataAnalyzed,
                                Data dataGeneralized,
                                Data dataInput,
                                DataDefinition definition,
                                DomainShare[] shares,
                                String[] header,
                                GeneralizationHierarchy[] hierarchiesGeneralized,
                                GeneralizationHierarchy[] hierarchiesAnalyzed,
                                int[] generalizationLevelsMinimum,
                                int[] generalizationLevelsMaximum,
                                DataAggregationInformation microaggregationData) {
        
        super(dataAnalyzed,
              dataGeneralized,
              dataInput,
              source.getDataDefinition(),
              shares,
              header,
              hierarchiesGeneralized,
              hierarchiesAnalyzed,
              generalizationLevelsMinimum,
              generalizationLevelsMaximum,
              microaggregationData);
        
        this.source = source;
    }

    @Override
    public double[] getDistribution(DataMatrix data, int index, int distinctValues) {
        // Delegate to source
        return source.getDistribution(data, index, distinctValues);
    }

    @Override
    public double[] getDistribution(String attribute) {
        // Delegate to source
        return source.getDistribution(attribute);
    }

    @Override
    public int[] getOrder(String attribute) {
        // Delegate to source
        return source.getOrder(attribute);
    }

    @Override
    public int[] getTree(DataMatrix data, int index, int[][] hierarchy) {
        // Delegate to source
        return source.getTree(data, index, hierarchy);
    }

    @Override
    public int[] getTree(String attribute) {
        // Delegate to source
        return source.getTree(attribute);
    }
}
