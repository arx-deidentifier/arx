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

import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;

/**
 * Small class to encapsulate information about data that needs to be analyzed aggregated.
 * 
 * @author Fabian Prasser
 */
public class DataAggregationInformation {

    /** Name of all attributes */
    public String[]                        header;

    /** Index of first column that is cold. All columns from <code>0</code> to <code>hotThreshold - 1</code> must be analyzed in hot mode. */
    public int                             hotThreshold;

    /** Indices of attributes in <code>columns</code> which must be aggregated during the anonymization process but which are not generalized. */
    public int[]                           hotQIsNotGeneralized;

    /** Function of i-th attribute (in hotQIsNotGeneralized) that must be aggregated during the anonymization process but which is not generalized. */
    public DistributionAggregateFunction[] hotQIsNotGeneralizedFunctions;

    /** Domain size of i-th attribute (in hotQIsNotGeneralized) that must be aggregated during the anonymization process but which is not generalized. */
    public int[]                           hotQIsNotGeneralizedDomainSizes;

    /** Indices of attributes in <code>columns</code> which must be aggregated during the anonymization process and which are also generalized. */
    public int[]                           hotQIsGeneralized;

    /** Function of i-th attribute (in hotQIsGeneralized) that must be aggregated during the anonymization process and which are is generalized. */
    public DistributionAggregateFunction[] hotQIsGeneralizedFunctions;

    /** Domain size of i-th attribute (in hotQIsGeneralized) that must be aggregated during the anonymization process and which are is generalized. */
    public int[]                           hotQIsGeneralizedDomainSizes;

    /** Indices of attributes in <code>columns</code> which must be aggregated only after the anonymization process. */
    public int[]                           coldQIs;

    /** Function of i-th attribute (in coldQIs) that must be aggregated only after the anonymization process. */
    public DistributionAggregateFunction[] coldQIsFunctions;

    /** Domain size of i-th attribute (in coldQIs) that must be aggregated only after the anonymization process. */
    public int[]                           coldQIsDomainSizes;

    /**
     * Clone constructor
     * @param header
     * @param hotThreshold
     * @param hotQIsNotGeneralized
     * @param hotQIsNotGeneralizedFunctions
     * @param hotQIsNotGeneralizedDomainSizes
     * @param hotQIsGeneralized
     * @param hotQIsGeneralizedFunctions
     * @param hotQIsGeneralizedDomainSizes
     * @param coldQIs
     * @param coldQIsFunctions
     * @param coldQIsDomainSizes
     */
    private DataAggregationInformation(String[] header,
                                       int hotThreshold,
                                       int[] hotQIsNotGeneralized,
                                       DistributionAggregateFunction[] hotQIsNotGeneralizedFunctions,
                                       int[] hotQIsNotGeneralizedDomainSizes,
                                       int[] hotQIsGeneralized,
                                       DistributionAggregateFunction[] hotQIsGeneralizedFunctions,
                                       int[] hotQIsGeneralizedDomainSizes,
                                       int[] coldQIs,
                                       DistributionAggregateFunction[] coldQIsFunctions,
                                       int[] coldQIsDomainSizes) {
        this.header = header;
        this.hotThreshold = hotThreshold;
        this.hotQIsNotGeneralized = hotQIsNotGeneralized;
        this.hotQIsNotGeneralizedFunctions = hotQIsNotGeneralizedFunctions;
        this.hotQIsNotGeneralizedDomainSizes = hotQIsNotGeneralizedDomainSizes;
        this.hotQIsGeneralized = hotQIsGeneralized;
        this.hotQIsGeneralizedFunctions = hotQIsGeneralizedFunctions;
        this.hotQIsGeneralizedDomainSizes = hotQIsGeneralizedDomainSizes;
        this.coldQIs = coldQIs;
        this.coldQIsFunctions = coldQIsFunctions;
        this.coldQIsDomainSizes = coldQIsDomainSizes;
    }

    /**
     * Creates a new instance
     * @param data
     * @param functions
     * @param definition
     * @param setOfHotQIsNotGeneralized
     * @param setOfHotQIsGeneralized
     * @param setOfColdQIs
     */
    public DataAggregationInformation(Data data,
                                      Map<String, DistributionAggregateFunction> functions,
                                      DataDefinition definition,
                                      Set<String> setOfHotQIsNotGeneralized,
                                      Set<String> setOfHotQIsGeneralized,
                                      Set<String> setOfColdQIs) {

        // Header
        this.header = data.getHeader();
        
        // Hot QIs not generalized
        this.hotQIsNotGeneralized = new int[setOfHotQIsNotGeneralized.size()];
        this.hotQIsNotGeneralizedFunctions = new DistributionAggregateFunction[setOfHotQIsNotGeneralized.size()];
        this.hotQIsNotGeneralizedDomainSizes = new int[setOfHotQIsNotGeneralized.size()];
        this.prepareQI(data, functions, definition, setOfHotQIsNotGeneralized, hotQIsNotGeneralized, hotQIsNotGeneralizedFunctions, hotQIsNotGeneralizedDomainSizes);

        // Hot QIs generalized
        this.hotQIsGeneralized = new int[setOfHotQIsNotGeneralized.size()];
        this.hotQIsGeneralizedFunctions = new DistributionAggregateFunction[setOfHotQIsNotGeneralized.size()];
        this.hotQIsGeneralizedDomainSizes = new int[setOfHotQIsNotGeneralized.size()];
        this.prepareQI(data, functions, definition, setOfHotQIsGeneralized, hotQIsGeneralized, hotQIsGeneralizedFunctions, hotQIsGeneralizedDomainSizes);

        // Cold QIs
        this.coldQIs = new int[setOfColdQIs.size()];
        this.coldQIsFunctions = new DistributionAggregateFunction[setOfColdQIs.size()];
        this.coldQIsDomainSizes = new int[setOfColdQIs.size()];
        this.prepareQI(data, functions, definition, setOfColdQIs, coldQIs, coldQIsFunctions, coldQIsDomainSizes);
        
        // First cold attribute is threshold
        this.hotThreshold = this.coldQIs.length == 0 ? data.getHeader().length : this.coldQIs[0];

        // TODO: Actually arrays would not even be needed for indices, as they follow a [min, max] scheme
    }
    
    /**
     * Fills all given array with required data
     * @param qis
     * @param data
     * @param map 
     * @param definition
     * @param indices
     * @param functions
     * @param domainSizes
     */
    private void prepareQI(Data data, Map<String, DistributionAggregateFunction> map, DataDefinition definition, Set<String> qis, int[] indices, DistributionAggregateFunction[] functions, int[] domainSizes) {

        int outerIndex = 0;
        int innerIndex = 0;
        for (String attribute : data.getHeader()) {
            if (qis.contains(attribute)) {
                int dictionaryIndex = data.getIndexOf(attribute);
                indices[innerIndex] = outerIndex;
                functions[innerIndex] = map.get(attribute);
                functions[innerIndex].initialize(data.getDictionary().getMapping()[dictionaryIndex], 
                                                 definition.getDataType(attribute));
                domainSizes[innerIndex] = data.getDictionary().getMapping()[dictionaryIndex].length;
                innerIndex++;
            }
            outerIndex++;
        }
        
    }

    /**
     * Returns a clone for data subsets
     * @return
     */
    public DataAggregationInformation getSubsetInstance() {
        return new DataAggregationInformation(this.header,
                                              this.hotThreshold,
                                              this.hotQIsNotGeneralized,
                                              this.clone(this.hotQIsNotGeneralizedFunctions),
                                              this.hotQIsNotGeneralizedDomainSizes,
                                              this.hotQIsGeneralized,
                                              this.clone(this.hotQIsGeneralizedFunctions),
                                              this.hotQIsGeneralizedDomainSizes,
                                              this.coldQIs,
                                              this.clone(this.coldQIsFunctions),
                                              this.coldQIsDomainSizes);
    }

    /**
     * Clones an array of functions
     * @param functions
     * @return
     */
    private DistributionAggregateFunction[] clone(DistributionAggregateFunction[] functions) {
        DistributionAggregateFunction[] result = new DistributionAggregateFunction[functions.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = functions[i].clone();
        }
        return result;
    }
}
