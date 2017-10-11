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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.deidentifier.arx.metric.Metric;
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

    /** Data. */
    private final Data                      dataAnalyzed;

    /** Data */
    private final Data                      dataGeneralized;

    /** Data. */
    private final Data                      dataInput;

    /** The data definition */
    private final DataDefinition            definition;

    /** The domain shares */
    private DomainShare[]                   shares;

    /** The original input header. */
    private final String[]                  header;

    /** Hierarchies for generalized attributes */
    private final GeneralizationHierarchy[] hierarchiesGeneralized;

    /** Hierarchies for analyzed attributes */
    private final GeneralizationHierarchy[] hierarchiesAnalyzed;

    /** The maximum level for each QI. */
    private final int[]                     generalizationLevelsMinimum;

    /** The minimum level for each QI. */
    private final int[]                     generalizationLevelsMaximum;

    /** Information about micro-aggregation */
    private final DataMicroAggregation      microaggregationData;

    /** The research subset, if any. */
    private RowSet                          subset     = null;

    /** The size of the research subset. */
    private int                             subsetSize = 0;

    /**
     * Creates a new data manager from pre-encoded data.
     * 
     * @param header
     * @param data
     * @param dictionary
     * @param definition
     * @param privacyModels
     * @param functions
     * @param qualityModel
     */
    public DataManager(final String[] header,
                       final DataMatrix data,
                       final Dictionary dictionary,
                       final DataDefinition definition,
                       final Set<PrivacyCriterion> privacyModels,
                       final Map<String, DistributionAggregateFunction> functions,
                       final Metric<?> qualityModel) {

        // Store basic info
        this.header = header;
        this.definition = definition;

        // Collect types of data
        Set<String> attributesGeneralized = new HashSet<>(definition.getQuasiIdentifiersWithGeneralization());
        attributesGeneralized.addAll(definition.getQuasiIdentifiersWithClusteringAndMicroaggregation());
        Set<String> attributesAnalyzed = new HashSet<>(definition.getSensitiveAttributes());
        Set<String> attributesResponse = new HashSet<>(definition.getResponseVariables());
        Set<String> attributesAggregated = new HashSet<>();
        
        // Only analyze aggregated variables, if required by the quality model
        // TODO: What about aggregated but non-clustered variables? Does this even work anymore?
        if (qualityModel.isAggregatedInputRequired()) {
            attributesAggregated.addAll(attributesGeneralized);
            throw new RuntimeException("Not implemented"); // TODO
        } 
        // Do not analyze generalized response variables
        // TODO: We should probably analyze them, if they are microaggregated, though
        attributesResponse.removeAll(attributesGeneralized);
        attributesAnalyzed.addAll(attributesAggregated);
        attributesAnalyzed.addAll(attributesResponse);
        
        // Create data objects
        this.dataGeneralized = Data.createProjection(data, header, getColumns(header, attributesGeneralized), dictionary);
        this.dataAnalyzed = Data.createProjection(data, header, getColumns(header, attributesAnalyzed, attributesAggregated), dictionary);
        this.dataInput = Data.createWrapper(data, header, getColumns(header), dictionary);
        
        // Make the dictionaries ready for additions
        this.dataGeneralized.getDictionary().definalizeAll();
        this.dataAnalyzed.getDictionary().definalizeAll();
        
        // Store information about aggregated attributes
        this.microaggregationData = new DataMicroAggregation();
        this.microaggregationData.startIndex = dataAnalyzed.getColumns().length - attributesAggregated.size();
        this.microaggregationData.header = Arrays.copyOfRange(dataAnalyzed.getHeader(), microaggregationData.startIndex, dataAnalyzed.getHeader().length);
        this.microaggregationData.columns = Arrays.copyOfRange(dataAnalyzed.getColumns(), microaggregationData.startIndex, dataAnalyzed.getColumns().length);

        // Register hierarchies used for generalization
        this.generalizationLevelsMaximum = new int[attributesGeneralized.size()];
        this.generalizationLevelsMinimum = new int[attributesGeneralized.size()];
        this.hierarchiesGeneralized = new GeneralizationHierarchy[attributesGeneralized.size()];
        int index = 0;
        
        // For each attribute
        for (final String attribute : header) {
            
            // This is a generalized quasi-identifier
            if (attributesGeneralized.contains(attribute)) {
                
                // Register at the dictionary and encode
                this.hierarchiesGeneralized[index] = new GeneralizationHierarchy(attribute,
                                                                            definition.getHierarchy(attribute),
                                                                            index,
                                                                            this.dataGeneralized.getDictionary());
                
                // Initialize hierarchy height and minimum / maximum generalization
                Integer min = definition.getMinimumGeneralization(attribute);
                Integer max = definition.getMaximumGeneralization(attribute);
                this.generalizationLevelsMaximum[index] = min == null ? 0 : min;
                this.generalizationLevelsMinimum[index] = max == null ? this.hierarchiesGeneralized[index].getArray()[0].length - 1 : max;
                
                // Next quasi-identifier
                index++;
            }
        }
        
        // Change to fixed generalization scheme when using differential privacy
        index = 0;
        for (PrivacyCriterion c : privacyModels) {
            
            // DP found
            if (c instanceof EDDifferentialPrivacy) {
                
                // Extract scheme
                DataGeneralizationScheme scheme = ((EDDifferentialPrivacy)c).getGeneralizationScheme();
                
                // For each attribute
                for (final String attribute : header) {
                    
                    // This is a generalized quasi-identifier
                    if (attributesGeneralized.contains(attribute)) {
                        this.generalizationLevelsMaximum[index] = scheme.getGeneralizationLevel(attribute, definition);
                        this.generalizationLevelsMinimum[index] = scheme.getGeneralizationLevel(attribute, definition);
                    }

                    // Next quasi-identifier
                    index++;
                }
                break;
            }
        }

        // Build map with hierarchies for sensitive attributes
        this.hierarchiesAnalyzed = new GeneralizationHierarchy[this.dataAnalyzed.getColumns().length];
        for (PrivacyCriterion c : privacyModels) {
            if (c instanceof HierarchicalDistanceTCloseness) {
                HierarchicalDistanceTCloseness t = (HierarchicalDistanceTCloseness) c;
                String attribute = t.getAttribute();
                index = dataAnalyzed.getIndexOf(attribute);
                this.hierarchiesAnalyzed[index] = new GeneralizationHierarchy(attribute, t.getHierarchy().getHierarchy(),
                                                                              index, dataAnalyzed.getDictionary());
            }
        }

        // finalize dictionary
        dataGeneralized.getDictionary().finalizeAll();
        dataAnalyzed.getDictionary().finalizeAll();

        // Initialize aggregation functions
        this.microaggregationData.functions = new DistributionAggregateFunction[this.microaggregationData.header.length];
        this.microaggregationData.domainSizes = new int[this.microaggregationData.header.length];
        index = 0;
        
        // For each attribute
        for (final String attribute : header) {
            
            // This is an aggregated quasi-identifier
            if (attributesAggregated.contains(attribute)) {
               
               int dictionaryIndex = dataAnalyzed.getIndexOf(attribute);
               this.microaggregationData.domainSizes[index] = dataAnalyzed.getDictionary().getMapping()[dictionaryIndex].length;
               this.microaggregationData.functions[index] = functions.get(attribute);
               this.microaggregationData.functions[index].initialize(dataAnalyzed.getDictionary().getMapping()[dictionaryIndex],
                                                           definition.getDataType(attribute));

               // Next aggregated quasi-identifier
               index++;               
            }
        }

        // Store research subset
        for (PrivacyCriterion c : privacyModels) {
            if (c instanceof EDDifferentialPrivacy) {
                ((EDDifferentialPrivacy) c).initialize(this, null);
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
     * @param microaggregationFunctions
     * @param microaggregationDomainSizes
     * @param microaggregationHeader
     * @param microaggregationStartIndex
     */
    protected DataManager(Data dataAnalyzed,
                          Data dataGeneralized,
                          Data dataInput,
                          DataDefinition definition,
                          DomainShare[] shares,
                          String[] header,
                          GeneralizationHierarchy[] hierarchiesGeneralized,
                          GeneralizationHierarchy[] hierarchiesAnalyzed,
                          int[] generalizationLevelsMinimum,
                          int[] generalizationLevelsMaximum,
                          DataMicroAggregation microaggregationData) {
        
        // Just store
        this.dataAnalyzed = dataAnalyzed;
        this.dataGeneralized = dataGeneralized;
        this.dataInput = dataInput;
        this.definition = definition;
        this.shares = shares;
        this.header = header;
        this.hierarchiesGeneralized = hierarchiesGeneralized;
        this.hierarchiesAnalyzed = hierarchiesAnalyzed;
        this.generalizationLevelsMinimum = generalizationLevelsMinimum;
        this.generalizationLevelsMaximum = generalizationLevelsMaximum;
        this.microaggregationData = microaggregationData;
        
        // Both variables are only used for getDistribution() and getTree()
        // The projected instance delegates these methods to the original data manager
        this.subset = null;
        this.subsetSize = 0;
    }

    /**
     * Returns centroid distances
     * @param normalized 
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DataCentroidDistances<?>[] getCentroidDistances(boolean normalized) {

        // Compute centroid distances
        DataCentroidDistances[] result = new DataCentroidDistances[dataGeneralized.getHeader().length];
        for (int column = 0; column < result.length; column++) {

            // Extract info
            String attribute = dataGeneralized.getHeader()[column];
            result[column] = new DataCentroidDistances(dataGeneralized, column,
                                                       definition.getDataType(attribute),
                                                       hierarchiesGeneralized[column].map,
                                                       normalized);
        }

        // Return
        return result;
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
     * Returns the input data.
     * 
     * @return the data
     */
    public Data getDataInput() {
        return dataInput;
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
        // Calculate and return
        int index = dataAnalyzed.getIndexOf(attribute);
        int distinctValues = dataAnalyzed.getDictionary().getMapping()[index].length;
        return getDistribution(dataAnalyzed.getArray(), index, distinctValues);
    }

    /**
     * Returns the domain shares for all generalized quasi-identifiers
     * @return
     */
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
                    
                // Create fall back option for materialized hierarchies
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
     * Returns the heights of the hierarchies used for generalizing quasi-identifiers
     * 
     * @return
     */
    public int[] getHierachiesHeights() {
        int[] result = new int[hierarchiesGeneralized.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = hierarchiesGeneralized[i].getArray()[0].length;
        }
        return result;
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
        return generalizationLevelsMinimum;
    }

    /**
     * Returns the minimum levels for the generalizations.
     * 
     * @return
     */

    public int[] getHierarchiesMinLevels() {
        return generalizationLevelsMaximum;
    }

    /**
     * Returns data configuring microaggregation
     * @return
     */
    public DataMicroAggregation getMicroAggregationData() {
        return this.microaggregationData;
    }

    /**
     * Returns the order of the given sensitive attribute in the original dataset. 
     * Required for t-closeness.
     * 
     * @param attribute
     * @return distribution
     */
    public int[] getOrder(String attribute) {

        // Prepare
        final int index = dataAnalyzed.getIndexOf(attribute);
        final String[] dictionary = dataAnalyzed.getDictionary().getMapping()[index];
        final DataType<?> type = this.definition.getDataType(attribute);
        
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
        
        return new DataManagerSubset(this,
                                     dataAnalyzed.getSubsetInstance(rowset),
                                     dataGeneralized.getSubsetInstance(rowset),
                                     dataInput.getSubsetInstance(rowset),
                                     definition,
                                     shares,
                                     header,
                                     hierarchiesGeneralized,
                                     hierarchiesAnalyzed,
                                     generalizationLevelsMinimum,
                                     generalizationLevelsMaximum,
                                     microaggregationData.getSubsetInstance());
    }

    /**
     * Returns a tree for the given attribute at the index within the given data array, using the given hierarchy.
     * The resulting tree can be used to calculate the earth mover's distance with hierarchical ground-distance.
     * @param data
     * @param index
     * @param hierarchy
     * @return tree
     */
    public int[] getTree(DataMatrix data, int index, int[][] hierarchy) {

        final int totalElementsP = subset == null ? data.getNumRows() : subsetSize;
        final int height = hierarchy[0].length - 1;
        final int numLeafs = hierarchy.length;

        // Initialize
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
        final int index = dataAnalyzed.getIndexOf(attribute);
        final DataMatrix data = dataAnalyzed.getArray();
        return getTree(data, index, hierarchiesAnalyzed[index].map);
    }
    
    /**
     * Simple returns the set of all columns
     * @param header
     * @return
     */
    private int[] getColumns(String[] header) {
        int[] result = new int[header.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    /**
     * Returns an array of indices for the given subset of strings
     * @param header
     * @param set
     * @return
     */
    private int[] getColumns(String[] header, Set<String> set) {
        int[] result = new int[set.size()];
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            if (set.contains(header[i])) {
                result[index++] = i;
            }
        }
        return result;
    }
    /**
     * Returns an array of indices for the given subset of strings. The first set will be encoded first.
     * The second must be a subset of the first.
     * @param header
     * @param first
     * @param second
     * @return
     */
    private int[] getColumns(String[] header, Set<String> first, Set<String> second) {
        int[] result = new int[first.size()];
        int index = 0;
        // First but not second
        for (int i = 0; i < header.length; i++) {
            String attribute = header[i];
            if (first.contains(attribute) && !second.contains(attribute)) {
                result[index++] = i;
            }
        }
        // First and second
        for (int i = 0; i < header.length; i++) {
            String attribute = header[i];
            if (first.contains(attribute) && second.contains(attribute)) {
                result[index++] = i;
            }
        }
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
