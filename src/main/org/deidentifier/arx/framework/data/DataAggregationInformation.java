/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;

/**
 * Small class to encapsulate information about data that needs to be analyzed aggregated.
 * 
 * @author Fabian Prasser
 */
public class DataAggregationInformation implements Serializable {

    /** SVUID */
    private static final long                     serialVersionUID = 6666226136889537126L;

    /** Name of all attributes */
    private final String[]                        header;

    /** Columns in original dataset */
    private final int[]                           columns;

    /** Index of first column that is cold. All columns from <code>0</code> to <code>hotThreshold - 1</code> must be analyzed in hot mode. */
    private final int                             hotThreshold;

    /** Indices of attributes in <code>columns</code> which must be aggregated during the anonymization process but which are not generalized. */
    private final int[]                           hotQIsNotGeneralized;

    /** Function of i-th attribute (in hotQIsNotGeneralized) that must be aggregated during the anonymization process but which is not generalized. */
    private final DistributionAggregateFunction[] hotQIsNotGeneralizedFunctions;

    /** Domain size of i-th attribute (in hotQIsNotGeneralized) that must be aggregated during the anonymization process but which is not generalized. */
    private final int[]                           hotQIsNotGeneralizedDomainSizes;

    /** Indices of attributes in <code>columns</code> which must be aggregated during the anonymization process and which are also generalized. */
    private final int[]                           hotQIsGeneralized;

    /** Function of i-th attribute (in hotQIsGeneralized) that must be aggregated during the anonymization process and which are is generalized. */
    private final DistributionAggregateFunction[] hotQIsGeneralizedFunctions;

    /** Domain size of i-th attribute (in hotQIsGeneralized) that must be aggregated during the anonymization process and which are is generalized. */
    private final int[]                           hotQIsGeneralizedDomainSizes;

    /** Indices of attributes in <code>columns</code> which must be aggregated only after the anonymization process. */
    private final int[]                           coldQIs;

    /** Function of i-th attribute (in coldQIs) that must be aggregated only after the anonymization process. */
    private final DistributionAggregateFunction[] coldQIsFunctions;

    /** Domain size of i-th attribute (in coldQIs) that must be aggregated only after the anonymization process. */
    private final int[]                           coldQIsDomainSizes;

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
        this.columns = data.getColumns();
        
        // Hot QIs not generalized
        this.hotQIsNotGeneralized = new int[setOfHotQIsNotGeneralized.size()];
        this.hotQIsNotGeneralizedFunctions = new DistributionAggregateFunction[setOfHotQIsNotGeneralized.size()];
        this.hotQIsNotGeneralizedDomainSizes = new int[setOfHotQIsNotGeneralized.size()];
        this.prepareQI(data, functions, definition, setOfHotQIsNotGeneralized, hotQIsNotGeneralized, hotQIsNotGeneralizedFunctions, hotQIsNotGeneralizedDomainSizes);

        // Hot QIs generalized
        this.hotQIsGeneralized = new int[setOfHotQIsGeneralized.size()];
        this.hotQIsGeneralizedFunctions = new DistributionAggregateFunction[setOfHotQIsGeneralized.size()];
        this.hotQIsGeneralizedDomainSizes = new int[setOfHotQIsGeneralized.size()];
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
     * Clone constructor
     * @param header
     * @param columns
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
                                       int[] columns,
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
        this.columns = columns;
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
     * Returns a clone for data subsets
     * @return
     */
    public DataAggregationInformation clone() {
        return new DataAggregationInformation(this.header,
                                              this.columns,
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
     * @return the coldQIs
     */
    public int[] getColdQIs() {
        return coldQIs;
    }

    /**
     * @return the coldQIsDomainSizes
     */
    public int[] getColdQIsDomainSizes() {
        return coldQIsDomainSizes;
    }

    /**
     * @return the coldQIsFunctions
     */
    public DistributionAggregateFunction[] getColdQIsFunctions() {
        return coldQIsFunctions;
    }

    /**
     * @return the columns
     */
    public int[] getColumns() {
        return columns;
    }

    /**
     * @return the header
     */
    public String[] getHeader() {
        return header;
    }

    /**
     * @return the hotQIsGeneralized
     */
    public int[] getHotQIsGeneralized() {
        return hotQIsGeneralized;
    }

    /**
     * @return the hotQIsGeneralizedDomainSizes
     */
    public int[] getHotQIsGeneralizedDomainSizes() {
        return hotQIsGeneralizedDomainSizes;
    }

    /**
     * @return the hotQIsGeneralizedFunctions
     */
    public DistributionAggregateFunction[] getHotQIsGeneralizedFunctions() {
        return hotQIsGeneralizedFunctions;
    }

    /**
     * @return the hotQIsNotGeneralized
     */
    public int[] getHotQIsNotGeneralized() {
        return hotQIsNotGeneralized;
    }

    /**
     * @return the hotQIsNotGeneralizedDomainSizes
     */
    public int[] getHotQIsNotGeneralizedDomainSizes() {
        return hotQIsNotGeneralizedDomainSizes;
    }

    /**
     * @return the hotQIsNotGeneralizedFunctions
     */
    public DistributionAggregateFunction[] getHotQIsNotGeneralizedFunctions() {
        return hotQIsNotGeneralizedFunctions;
    }

    /**
     * @return the hotThreshold
     */
    public int getHotThreshold() {
        return hotThreshold;
    }

    /**
     * Returns the columns in the original array which are microaggregated
     * @return
     */
    public int[] getMicroaggregationColumns() {
        int[] indices = getMicroaggregationIndices();
        int[] result = new int[indices.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = columns[indices[i]];
        }
        return result;
    }

    /**
     * Returns the distribution aggregate functions for all microaggregated values
     * @return
     */
    public DistributionAggregateFunction[] getMicroaggregationFunctions() {

        // Prepare
        List<DistributionAggregateFunction> result = new ArrayList<DistributionAggregateFunction>();
        
        // Collect
        for (DistributionAggregateFunction element : hotQIsNotGeneralizedFunctions) {
            result.add(element);
        }
        for (DistributionAggregateFunction element : hotQIsGeneralizedFunctions) {
            result.add(element);
        }
        for (DistributionAggregateFunction element : coldQIsFunctions) {
            result.add(element);
        }
        
        // Return
        return result.toArray(new DistributionAggregateFunction[result.size()]);
    }

    /**
     * Returns the attributes in the analyzed dataset which are microaggregated
     * @return
     */
    public String[] getMicroaggregationHeader() {
        int[] indices = getMicroaggregationIndices();
        String[] result = new String[indices.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = header[indices[i]];
        }
        return result;
    }

    /**
     * Returns the indices of all attributes that are microaggregated
     * in order hotQIsNotGeneralized, hotQIsGeneralized, coldQIs.
     * @return
     */
    public int[] getMicroaggregationIndices() {
        
        // Prepare
        List<Integer> result = new ArrayList<Integer>();
        
        // Collect
        for (int element : hotQIsNotGeneralized) {
            result.add(element);
        }
        for (int element : hotQIsGeneralized) {
            result.add(element);
        }
        for (int element : coldQIs) {
            result.add(element);
        }
        
        // Return
        int[] array = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            array[i] = result.get(i);
        }
        return array;
    }

    @Override
    public String toString() {
        return "DataAggregationInformation [\n - header=" + Arrays.toString(header) + "\n - columns=" + Arrays.toString(columns) +
               "\n - hotThreshold=" + hotThreshold + "\n - hotQIsNotGeneralized=" + Arrays.toString(hotQIsNotGeneralized) +
               "\n - hotQIsGeneralized=" + Arrays.toString(hotQIsGeneralized) + "\n - coldQIs=" + Arrays.toString(coldQIs) + "\n]";
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
}
