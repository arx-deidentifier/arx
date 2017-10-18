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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionGeneralization;
import org.deidentifier.arx.metric.v2.DomainShare;
import org.deidentifier.arx.metric.v2.DomainShareInterval;
import org.deidentifier.arx.metric.v2.DomainShareMaterialized;
import org.deidentifier.arx.metric.v2.DomainShareRedaction;

import cern.colt.Sorting;
import cern.colt.function.IntComparator;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * Holds all data needed for the anonymization process.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataManager {

    /**
     * Internal representation of attribute types. Quasi-identifiers are split
     * into the ones to which generalization is applied and the ones to which
     * microaggregation is applied
     * 
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     * 
     */
    public static class AttributeTypeInternal {
        public static final int IDENTIFYING                       = 3;
        public static final int INSENSITIVE                       = 2;
        public static final int QUASI_IDENTIFYING_GENERALIZED     = 0;
        public static final int QUASI_IDENTIFYING_MICROAGGREGATED = 4;
        public static final int SENSITIVE                         = 1;
    }

    /** The data. */
    private final Data                                 dataAnalyzed;

    /** The data which is generalized */
    private final Data                                 dataGeneralized;

    /** The data which is insensitive */
    private final Data                                 dataStatic;

    /** The data definition */
    private final DataDefinition                       definition;

    /** The domain shares */
    private DomainShare[]                              shares;

    /** The original input header. */
    private final String[]                             header;

    /** The generalization hierarchiesQI. */
    private final GeneralizationHierarchy[]            hierarchiesGeneralized;

    /** The hierarchy heights for each QI. */
    private final int[]                                hierarchiesHeights;

    /** The sensitive attributes. */
    private final Map<String, GeneralizationHierarchy> hierarchiesSensitive;

    /** The data types of sensitive attributes. */
    private final Map<String, DataType<?>>             dataTypesSensitive;

    /** The indexes of sensitive attributes. */
    private final Map<String, Integer>                 indexesSensitive;

    /** The maximum level for each QI. */
    private final int[]                                maxLevels;

    /** The microaggregation functions. */
    private final DistributionAggregateFunction[]      microaggregationFunctions;

    /** Header for microaggregated attributes */
    private final String[]                             microaggregationHeader;

    /** Map for microaggregated attributes */
    private final int[]                                microaggregationMap;

    /** Map for microaggregated attributes */
    private final int[]                                microaggregationDomainSizes;

    /** The number of microaggregation attributes in the dataDI */
    private final int                                  microaggregationNumAttributes;

    /** The start index of the microaggregation attributes in the dataDI */
    private final int                                  microaggregationStartIndex;

    /** The minimum level for each QI. */
    private final int[]                                minLevels;

    /** The research subset, if any. */
    private RowSet                                     subset     = null;

    /** The size of the research subset. */
    private int                                        subsetSize = 0;
    
    /**
     * Creates a new data manager from pre-encoded data.
     * 
     * @param header
     * @param data
     * @param dictionary
     * @param definition
     * @param config
     * @param function
     */
    public DataManager(final String[] header,
                       final DataMatrix data,
                       final Dictionary dictionary,
                       final DataDefinition definition,
                       final ARXConfiguration config,
                       final Map<String, DistributionAggregateFunction> functions) {

        // Store columns for reordering the output
        this.header = header;
        this.definition = definition;

        Set<String> attributesGeneralized = definition.getQuasiIdentifiersWithGeneralization();
        Set<String> attributesSensitive = definition.getSensitiveAttributes();
        Set<String> attributesMicroaggregated = definition.getQuasiIdentifiersWithMicroaggregation();
        Set<String> attributesInsensitive = definition.getInsensitiveAttributes();

        // Init dictionary
        final Dictionary dictionaryGeneralized = new Dictionary(attributesGeneralized.size());
        final Dictionary dictionaryAnalyzed = new Dictionary(attributesSensitive.size() + attributesMicroaggregated.size());
        final Dictionary dictionaryStatic = new Dictionary(attributesInsensitive.size());

        // Init maps for reordering the output
        final int[] mapGeneralized = new int[dictionaryGeneralized.getNumDimensions()];
        final int[] mapAnalyzed = new int[dictionaryAnalyzed.getNumDimensions()];
        final int[] mapStatic = new int[dictionaryStatic.getNumDimensions()];
        this.microaggregationMap = new int[attributesMicroaggregated.size()];

        // Indexes
        this.microaggregationStartIndex = attributesSensitive.size();
        this.microaggregationNumAttributes = attributesMicroaggregated.size();
        int indexStatic = 0;
        int indexGeneralized = 0;
        int indexAnalyzed = 0;
        int indexSensitive = 0;
        int indexMicroaggregated = this.microaggregationStartIndex;
        int counter = 0;

        // A map for column indices. map[i*2]=attribute type, map[i*2+1]=index position.
        final int[] map = new int[header.length * 2];
        final String[] headerGH = new String[dictionaryGeneralized.getNumDimensions()];
        final String[] headerDI = new String[dictionaryAnalyzed.getNumDimensions()];
        final String[] headerIS = new String[dictionaryStatic.getNumDimensions()];
        this.microaggregationHeader = new String[attributesMicroaggregated.size()];
        this.dataTypesSensitive = new HashMap<>();

        for (final String column : header) {
            final int idx = counter * 2;
            if (attributesGeneralized.contains(column)) {
                map[idx] = AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED;
                map[idx + 1] = indexGeneralized;
                mapGeneralized[indexGeneralized] = counter;
                dictionaryGeneralized.registerAll(indexGeneralized, dictionary, counter);
                headerGH[indexGeneralized] = header[counter];
                indexGeneralized++;
            } else if (attributesMicroaggregated.contains(column)) {
                map[idx] = AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED;
                map[idx + 1] = indexMicroaggregated;
                mapAnalyzed[indexMicroaggregated] = counter;
                dictionaryAnalyzed.registerAll(indexMicroaggregated, dictionary, counter);
                headerDI[indexMicroaggregated] = header[counter];
                indexMicroaggregated++;
                microaggregationMap[indexAnalyzed] = counter;
                microaggregationHeader[indexAnalyzed] = header[counter];
                indexAnalyzed++;
            } else if (attributesInsensitive.contains(column)) {
                map[idx] = AttributeTypeInternal.INSENSITIVE;
                map[idx + 1] = indexStatic;
                mapStatic[indexStatic] = counter;
                dictionaryStatic.registerAll(indexStatic, dictionary, counter);
                headerIS[indexStatic] = header[counter];
                indexStatic++;
            } else if (attributesSensitive.contains(column)) {
                map[idx] = AttributeTypeInternal.SENSITIVE;
                map[idx + 1] = indexSensitive;
                mapAnalyzed[indexSensitive] = counter;
                dictionaryAnalyzed.registerAll(indexSensitive, dictionary, counter);
                headerDI[indexSensitive] = header[counter];
                indexSensitive++;
                dataTypesSensitive.put(column, definition.getDataType(column));
            } else {
                // TODO: CHECK: Changed default? - now all undefined attributes
                // are identifying! Previously they were considered sensitive?
                map[idx] = AttributeTypeInternal.IDENTIFYING;
                map[idx + 1] = -1;
            }
            counter++;
        }

        // encode Data
        final Data[] ddata = encode(data,
                                    map,
                                    mapGeneralized,
                                    mapAnalyzed,
                                    mapStatic,
                                    dictionaryGeneralized,
                                    dictionaryAnalyzed,
                                    dictionaryStatic,
                                    headerGH,
                                    headerDI,
                                    headerIS);
        dataGeneralized = ddata[0];
        dataAnalyzed = ddata[1];
        dataStatic = ddata[2];

        // Initialize minlevels
        minLevels = new int[attributesGeneralized.size()];
        hierarchiesHeights = new int[attributesGeneralized.size()];
        maxLevels = new int[attributesGeneralized.size()];

        // Build hierarchiesQI
        hierarchiesGeneralized = new GeneralizationHierarchy[attributesGeneralized.size()];
        for (int i = 0; i < header.length; i++) {
            final int idx = i * 2;
            if (attributesGeneralized.contains(header[i]) &&
                map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED) {
                final int dictionaryIndex = map[idx + 1];
                final String name = header[i];
                if (definition.getHierarchy(name) != null) {
                    hierarchiesGeneralized[dictionaryIndex] = new GeneralizationHierarchy(name,
                                                                                          definition.getHierarchy(name),
                                                                                          dictionaryIndex,
                                                                                          dictionaryGeneralized);
                } else {
                    throw new IllegalStateException("No hierarchy available for attribute (" + header[i] + ")");
                }
                // Initialize hierarchy height and minimum / maximum
                // generalization
                hierarchiesHeights[dictionaryIndex] = hierarchiesGeneralized[dictionaryIndex].getArray()[0].length;
                final Integer minGenLevel = definition.getMinimumGeneralization(name);
                minLevels[dictionaryIndex] = minGenLevel == null ? 0 : minGenLevel;
                final Integer maxGenLevel = definition.getMaximumGeneralization(name);
                maxLevels[dictionaryIndex] = maxGenLevel == null ? hierarchiesHeights[dictionaryIndex] - 1 : maxGenLevel;
            }
        }
        
        // Change min & max, when using data-independent (e,d)-DP
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            if (c instanceof EDDifferentialPrivacy) {
                EDDifferentialPrivacy edpModel = (EDDifferentialPrivacy)c;
                if (!edpModel.isDataDependent()) {
                    DataGeneralizationScheme scheme = edpModel.getGeneralizationScheme();
                    for (int i = 0; i < header.length; i++) {
                        final int idx = i * 2;
                        if (attributesGeneralized.contains(header[i]) &&
                            map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED) {
                            minLevels[map[idx + 1]] = scheme.getGeneralizationLevel(header[i], definition);
                            maxLevels[map[idx + 1]] = scheme.getGeneralizationLevel(header[i], definition);
                        }
                    }
                }
                break;
            }
        }

        // Build map with hierarchies for sensitive attributes
        Map<String, String[][]> sensitiveHierarchies = new HashMap<String, String[][]>();
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            if (c instanceof HierarchicalDistanceTCloseness) {
                HierarchicalDistanceTCloseness t = (HierarchicalDistanceTCloseness) c;
                sensitiveHierarchies.put(t.getAttribute(), t.getHierarchy().getHierarchy());
            }
        }

        // Build generalization hierarchies for sensitive attributes
        hierarchiesSensitive = new HashMap<String, GeneralizationHierarchy>();
        indexesSensitive = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            final String name = header[i];
            final int idx = i * 2;
            if (sensitiveHierarchies.containsKey(name) &&
                map[idx] == AttributeTypeInternal.SENSITIVE) {
                final int dictionaryIndex = map[idx + 1];
                final String[][] hiers = sensitiveHierarchies.get(name);
                if (hiers != null) {
                    hierarchiesSensitive.put(name, new GeneralizationHierarchy(name,
                                                                               hiers,
                                                                               dictionaryIndex,
                                                                               dictionaryAnalyzed));
                }
            }

            // Store index for sensitive attributes
            if (attributesSensitive.contains(header[i])) {
                indexesSensitive.put(name, index);
                index++;
            }
        }

        // Build map with hierarchies for microaggregated attributes
        Map<String, String[][]> maHierarchies = new HashMap<String, String[][]>();
        for (String attribute : functions.keySet()) {
            if (functions.get(attribute) instanceof DistributionAggregateFunctionGeneralization) {
                maHierarchies.put(attribute, definition.getHierarchy(attribute));
            }
        }

        // Build generalization hierarchies for microaggregated attributes
        Map<String, int[][]> hierarchiesMA = new HashMap<String, int[][]>();
        index = 0;
        for (int i = 0; i < header.length; i++) {
            final String name = header[i];
            final int idx = i * 2;
            if (maHierarchies.containsKey(name) &&
                map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED) {
                final int dictionaryIndex = map[idx + 1];
                final String[][] hiers = maHierarchies.get(name);
                if (hiers != null) {
                    hierarchiesMA.put(name, new GeneralizationHierarchy(name,
                                                                        hiers,
                                                                        dictionaryIndex,
                                                                        dictionaryAnalyzed).map);
                }
            }
        }

        // finalize dictionary
        dictionaryGeneralized.finalizeAll();
        dictionaryAnalyzed.finalizeAll();
        dictionaryStatic.finalizeAll();

        // Init microaggregation functions
        microaggregationFunctions = new DistributionAggregateFunction[attributesMicroaggregated.size()];
        microaggregationDomainSizes = new int[attributesMicroaggregated.size()];
        for (int i = 0; i < header.length; i++) {
            final int idx = i * 2;
            if (attributesMicroaggregated.contains(header[i]) &&
                map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED) {
                final int dictionaryIndex = map[idx + 1] - microaggregationStartIndex;
                final String name = header[i];
                if (definition.getMicroAggregationFunction(name) != null) {
                    microaggregationDomainSizes[dictionaryIndex] = dictionaryAnalyzed.getMapping()[dictionaryIndex + microaggregationStartIndex].length;
                    microaggregationFunctions[dictionaryIndex] = functions.get(name);
                    microaggregationFunctions[dictionaryIndex].initialize(dictionaryAnalyzed.getMapping()[dictionaryIndex + microaggregationStartIndex],
                                                                          definition.getDataType(name),
                                                                          hierarchiesMA.get(name));
                } else {
                    throw new IllegalStateException("No microaggregation function defined for attribute (" +
                                                    header[i] + ")");
                }
            }
        }

        // Store research subset
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            if (c instanceof EDDifferentialPrivacy) {
                ((EDDifferentialPrivacy) c).initialize(this, config);
            }
            if (c.isSubsetAvailable()) {
                DataSubset _subset = c.getDataSubset();
                if (_subset != null) {
                    subset = _subset.getSet();
                    subsetSize = _subset.getArray().length;
                    break;
                }
            }
        }
    }

    /**
     * For creating a projected instance
     * @param definition
     * @param dataAnalyzed
     * @param dataGeneralized
     * @param dataStatic
     * @param header
     * @param hierarchiesGeneralized
     * @param hierarchiesHeights
     * @param hierarchiesSensitive
     * @param indexesSensitive
     * @param maxLevels
     * @param microaggregationFunctions
     * @param microaggregationHeader
     * @param microaggregationMap
     * @param microaggregationDomainSizes
     * @param microaggregationNumAttributes
     * @param microaggregationStartIndex
     * @param minLevels
     * @param dataTypesSensitive 
     */
    protected DataManager(DataDefinition definition,
                          Data dataAnalyzed,
                          Data dataGeneralized,
                          Data dataStatic,
                          String[] header,
                          GeneralizationHierarchy[] hierarchiesGeneralized,
                          int[] hierarchiesHeights,
                          Map<String, GeneralizationHierarchy> hierarchiesSensitive,
                          Map<String, Integer> indexesSensitive,
                          int[] maxLevels,
                          DistributionAggregateFunction[] microaggregationFunctions,
                          String[] microaggregationHeader,
                          int[] microaggregationMap,
                          int[] microaggregationDomainSizes,
                          int microaggregationNumAttributes,
                          int microaggregationStartIndex,
                          int[] minLevels,
                          Map<String, DataType<?>> dataTypesSensitive) {
        this.definition = definition;
        this.dataAnalyzed = dataAnalyzed;
        this.dataGeneralized = dataGeneralized;
        this.dataStatic = dataStatic;
        this.header = header;
        this.hierarchiesGeneralized = hierarchiesGeneralized;
        this.hierarchiesHeights = hierarchiesHeights;
        this.hierarchiesSensitive = hierarchiesSensitive;
        this.indexesSensitive = indexesSensitive;
        this.maxLevels = maxLevels;
        this.microaggregationFunctions = microaggregationFunctions;
        this.microaggregationDomainSizes = microaggregationDomainSizes;
        this.microaggregationHeader = microaggregationHeader;
        this.microaggregationMap = microaggregationMap;
        this.microaggregationNumAttributes = microaggregationNumAttributes;
        this.microaggregationStartIndex = microaggregationStartIndex;
        this.minLevels = minLevels;
        this.dataTypesSensitive = dataTypesSensitive;
        
        // Both variables are only used for getDistribution() and getTree()
        // The projected instance delegates these methods to the original data manager
        this.subset = null;
        this.subsetSize = 0;
    }

    /**
     * Returns the input data that will be analyzed.
     * 
     * @return the data
     */
    public Data getDataAnalyzed() {
        return dataAnalyzed;
    }

    /**
     * Returns the input data that will be generalized.
     * 
     * @return the data
     */
    public Data getDataGeneralized() {
        return dataGeneralized;
    }

    /**
     * Returns the static input data.
     * 
     * @return the data
     */
    public Data getDataStatic() {
        return dataStatic;
    }

    /**
     * Returns the distribution of the attribute in the data array at the given index.
     * @param dataMatrix
     * @param index
     * @param distinctValues
     * @return
     */
    public double[] getDistribution(DataMatrix dataMatrix, int index, int distinctValues) {

        // Initialize counts: iterate over all rows or the subset
        final int[] cardinalities = new int[distinctValues];
        for (int i = 0; i < dataMatrix.getNumRows(); i++) {
            if (subset == null || subset.contains(i)) {
                cardinalities[dataMatrix.get(i, index)]++;
            }
        }

        // compute distribution
        final double total = subset == null ? dataMatrix.getNumRows() : subsetSize;
        final double[] distribution = new double[cardinalities.length];
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = (double) cardinalities[i] / total;
        }
        return distribution;
    }

    /**
     * Returns the distribution of the given sensitive attribute in the original dataset. 
     * Required for t-closeness.
     * 
     * @param attribute
     * @return distribution
     */
    public double[] getDistribution(String attribute) {

        // Check
        if (!indexesSensitive.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute " + attribute + " is not sensitive");
        }
        
        // Prepare
        int index = indexesSensitive.get(attribute);
        int distinctValues = dataAnalyzed.getDictionary().getMapping()[index].length;
        
        // Calculate and return
        return getDistribution(dataAnalyzed.getArray(), index, distinctValues);
    }

    public DomainShare[] getDomainShares() {

        // Build on-demand
        if (this.shares == null) {
            
            // Compute domain shares
            this.shares = new DomainShare[dataGeneralized.getHeader().length];
            for (int i=0; i<shares.length; i++) {
                
                // Extract info
                String attribute = dataGeneralized.getHeader()[i];
                String[][] hierarchy = definition.getHierarchy(attribute);
                HierarchyBuilder<?> builder = definition.getHierarchyBuilder(attribute);
                
                // Create shares for redaction-based hierarchies
                if (builder != null && (builder instanceof HierarchyBuilderRedactionBased) &&
                    ((HierarchyBuilderRedactionBased<?>)builder).isDomainPropertiesAvailable()){
                    this.shares[i] = new DomainShareRedaction((HierarchyBuilderRedactionBased<?>)builder);
                    
                 // Create shares for interval-based hierarchies
                } else if (builder != null && (builder instanceof HierarchyBuilderIntervalBased)) {
                    this.shares[i] = new DomainShareInterval<>((HierarchyBuilderIntervalBased<?>)builder,
                                                           hierarchiesGeneralized[i].getArray(),
                                                           dataGeneralized.getDictionary().getMapping()[i]);
                    
                // Create fallback-shares for materialized hierarchies
                } else {
                    this.shares[i] = new DomainShareMaterialized(hierarchy, 
                                                            dataGeneralized.getDictionary().getMapping()[i],
                                                            hierarchiesGeneralized[i].getArray());
                }
            }
        }
        
        // Return
        return this.shares;
    }

    /**
     * The original data header.
     * 
     * @return
     */
    public String[] getHeader() {
        return header;
    }

    /**
     * Returns the heights of the hierarchiesQI.
     * 
     * @return
     */

    public int[] getHierachiesHeights() {
        return hierarchiesHeights;
    }

    /**
     * Returns the generalization hierarchiesQI.
     * 
     * @return the hierarchiesQI
     */
    public GeneralizationHierarchy[] getHierarchies() {
        return hierarchiesGeneralized;
    }

    /**
     * Returns the maximum levels for the generalizaiton.
     * 
     * @return the maximum level for each QI
     */
    public int[] getHierarchiesMaxLevels() {
        return maxLevels;
    }

    /**
     * Returns the minimum levels for the generalizations.
     * 
     * @return
     */

    public int[] getHierarchiesMinLevels() {
        return minLevels;
    }

    /**
     * Returns the map for the according buffer
     * @return
     */
    public int[] getMicroaggregationDomainSizes() {
        return microaggregationDomainSizes;
    }

    /**
     * Returns the microaggregation functions.
     * 
     * @return
     */
    public DistributionAggregateFunction[] getMicroaggregationFunctions() {
        return microaggregationFunctions;
    }

    /**
     * Returns the header for the according buffer
     * @return
     */
    public String[] getMicroaggregationHeader() {
        return microaggregationHeader;
    }

    /**
     * Returns the map for the according buffer
     * @return
     */
    public int[] getMicroaggregationMap() {
        return microaggregationMap;
    }

    /**
     * Gets the number of attributes to which microaggregation will be applied
     * in dataAnalyzed.
     * 
     * @return
     */
    public int getMicroaggregationNumAttributes() {
        return microaggregationNumAttributes;
    }

    /**
     * Gets the start index of the attributes to which microaggregation will be
     * applied in dataAnalyzed.
     * 
     * @return
     */
    public int getMicroaggregationStartIndex() {
        return microaggregationStartIndex;
    }

    /**
     * Returns the order of the given sensitive attribute in the original dataset. 
     * Required for t-closeness.
     * 
     * @param attribute
     * @return distribution
     */
    public int[] getOrder(String attribute) {

        // Check
        if (!indexesSensitive.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute " + attribute + " is not sensitive");
        }
        
        // Prepare
        final String[] dictionary = dataAnalyzed.getDictionary().getMapping()[indexesSensitive.get(attribute)];
        final DataType<?> type = this.dataTypesSensitive.get(attribute);
        
        // Init
        int[] order = new int[dictionary.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        
        // Sort
        Sorting.mergeSort(order, 0, order.length, new IntComparator() {
            @Override public int compare(int arg0, int arg1) {
                String value1 = dictionary[arg0];
                String value2 = dictionary[arg1];
                try {
                    return type.compare(value1, value2);
                } catch (NumberFormatException | ParseException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        
        // Return
        return order;
    }

    /**
     * Returns an instance of this data manager, that is projected onto the given rowset
     * @param rowset
     * @return
     */
    public DataManager getSubsetInstance(RowSet rowset) {
        
        DistributionAggregateFunction[] microaggregationFunctions = new DistributionAggregateFunction[this.microaggregationFunctions.length];
        for (int i = 0; i < this.microaggregationFunctions.length; i++) {
            microaggregationFunctions[i] = this.microaggregationFunctions[i].clone();
        }
        
        return new DataManagerSubset(this,
                                     this.dataAnalyzed.getSubsetInstance(rowset),
                                     this.dataGeneralized.getSubsetInstance(rowset),
                                     this.dataStatic.getSubsetInstance(rowset),
                                     this.header,
                                     this.hierarchiesGeneralized,
                                     this.hierarchiesHeights,
                                     this.hierarchiesSensitive,
                                     this.indexesSensitive,
                                     this.maxLevels,
                                     microaggregationFunctions,
                                     this.microaggregationHeader,
                                     this.microaggregationMap,
                                     this.microaggregationDomainSizes,
                                     this.microaggregationNumAttributes,
                                     this.microaggregationStartIndex,
                                     this.minLevels,
                                     this.dataTypesSensitive);
    }
    
    /**
     * Returns a tree for the given attribute at the index within the given data array, using the given hierarchy.
     * The resulting tree can be used to calculate the earth mover's distance with hierarchical ground-distance.
     * @param data
     * @param index
     * @param hierarchy
     * @return tree
     */
    public int[] getTree(DataMatrix data,
                         int index,
                         int[][] hierarchy) {

        final int totalElementsP = subset == null ? data.getNumRows() : subsetSize;
        final int height = hierarchy[0].length - 1;
        final int numLeafs = hierarchy.length;

        // TODO: Size could be calculated?!
        final ArrayList<Integer> treeList = new ArrayList<Integer>();
        treeList.add(totalElementsP);
        treeList.add(numLeafs);
        treeList.add(height);

        // Init all freq to 0
        for (int i = 0; i < numLeafs; i++) {
            treeList.add(0);
        }

        // Count frequencies
        final int offsetLeafs = 3;
        for (int i = 0; i < data.getNumRows(); i++) {
            if (subset == null || subset.contains(i)) {
                int val = data.get(i, index);
                int previousFreq = treeList.get(val + offsetLeafs);
                previousFreq++;
                treeList.set(val + offsetLeafs, previousFreq);
            }
        }

        // Init extras
        for (int i = 0; i < numLeafs; i++) {
            treeList.add(-1);
        }

        // Temporary class for nodes
        class TNode {
            IntOpenHashSet children = new IntOpenHashSet();
            int            level    = 0;
            int            offset   = 0;
        }

        final int offsetsExtras = offsetLeafs + numLeafs;
        final IntObjectOpenHashMap<TNode> nodes = new IntObjectOpenHashMap<TNode>();
        final ArrayList<ArrayList<TNode>> levels = new ArrayList<ArrayList<TNode>>();

        // Init levels
        for (int i = 0; i < hierarchy[0].length; i++) {
            levels.add(new ArrayList<TNode>());
        }

        // Build nodes
        int offset = dataAnalyzed.getDictionary().getMapping()[index].length;
        for (int i = 0; i < hierarchy[0].length; i++) {
            for (int j = 0; j < hierarchy.length; j++) {
                final int nodeID = hierarchy[j][i] + i * offset;
                TNode curNode = null;

                if (!nodes.containsKey(nodeID)) {
                    curNode = new TNode();
                    curNode.level = i;
                    nodes.put(nodeID, curNode);
                    final ArrayList<TNode> level = levels.get(curNode.level);
                    level.add(curNode);
                } else {
                    curNode = nodes.get(nodeID);
                }

                if (i > 0) { // first add child
                    curNode.children.add(hierarchy[j][i - 1] + (i - 1) * offset);
                }
            }
        }

        // For all nodes
        for (final ArrayList<TNode> level : levels) {
            for (final TNode node : level) {

                if (node.level > 0) { // only inner nodes
                    node.offset = treeList.size();

                    treeList.add(node.children.size());
                    treeList.add(node.level);

                    final int[] keys = node.children.keys;
                    final boolean[] allocated = node.children.allocated;
                    for (int i = 0; i < allocated.length; i++) {
                        if (allocated[i]) {
                            treeList.add(node.level == 1 ? keys[i] + offsetsExtras
                                    : nodes.get(keys[i]).offset);
                        }
                    }

                    treeList.add(0); // pos_e
                    treeList.add(0); // neg_e
                }
            }
        }

        final int[] treeArray = new int[treeList.size()];
        int count = 0;
        for (final int val : treeList) {
            treeArray[count++] = val;
        }

        return treeArray;
    }

    /**
     * Returns the tree for the given sensitive attribute, if a generalization hierarchy is associated.
     * The resulting tree can be used to calculate the earth mover's distance with hierarchical ground-distance.
     * 
     * @param attribute
     * @return tree
     */
    public int[] getTree(String attribute) {
        if (!hierarchiesSensitive.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute " + attribute + " is not sensitive");
        }
        final DataMatrix data = dataAnalyzed.getArray();
        final int index = indexesSensitive.get(attribute);
        return getTree(data, index, hierarchiesSensitive.get(attribute).map);
    }
    
    /**
     * Encodes the data.
     * 
     * @param data
     * @param map
     * @param mapGeneralized
     * @param mapAnalyzed
     * @param mapStatic
     * @param dictionaryGeneralized
     * @param dictionaryAnalyzed
     * @param dictionaryStatic
     * @param headerGeneralized
     * @param headerAnalyzed
     * @param headerStatic
     * @return
     */
    private Data[] encode(final DataMatrix data,
                          final int[] map,
                          final int[] mapGeneralized,
                          final int[] mapAnalyzed,
                          final int[] mapStatic,
                          final Dictionary dictionaryGeneralized,
                          final Dictionary dictionaryAnalyzed,
                          final Dictionary dictionaryStatic,
                          final String[] headerGeneralized,
                          final String[] headerAnalyzed,
                          final String[] headerStatic) {

        // Parse the dataset
        final DataMatrix valsGH = headerGeneralized.length == 0 ? null : new DataMatrix(data.getNumRows(), headerGeneralized.length);
        final DataMatrix valsDI = headerAnalyzed.length == 0 ? null : new DataMatrix(data.getNumRows(), headerAnalyzed.length);
        final DataMatrix valsIS = headerStatic.length == 0 ? null : new DataMatrix(data.getNumRows(), headerStatic.length);

        for (int index = 0; index < data.getNumRows(); index++) {
            
            valsGH.setRow(index);
            if (valsDI != null) valsDI.setRow(index);
            if (valsIS != null) valsIS.setRow(index);

            data.iterator(index);
            int i = 0;
            while (data.iterator_hasNext()) {
                
                final int idx = i * 2;
                int aType = map[idx];
                final int iPos = map[idx + 1];
                final int iValue = data.iterator_next();
                switch (aType) {
                case AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED:
                    valsGH.setValueAtColumn(iPos, iValue);
                    break;
                case AttributeTypeInternal.IDENTIFYING:
                    // Ignore
                    break;
                case AttributeTypeInternal.INSENSITIVE:
                    valsIS.setValueAtColumn(iPos, iValue);
                    break;
                case AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED:
                    valsDI.setValueAtColumn(iPos, iValue);
                    break;
                case AttributeTypeInternal.SENSITIVE:
                    valsDI.setValueAtColumn(iPos, iValue);
                    break;
                }
                i++;
            }
        }

        // Build data object
        final Data[] result = { new Data(valsGH,
                                         headerGeneralized,
                                         mapGeneralized,
                                         dictionaryGeneralized),
                new Data(valsDI, headerAnalyzed, mapAnalyzed, dictionaryAnalyzed),
                new Data(valsIS, headerStatic, mapStatic, dictionaryStatic) };
        return result;
    }

    /**
     * Returns the data definitions
     * @return
     */
    protected DataDefinition getDataDefinition() {
        return this.definition;
    }
}
