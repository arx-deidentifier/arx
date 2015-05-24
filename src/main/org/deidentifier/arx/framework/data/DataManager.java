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

package org.deidentifier.arx.framework.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionGeneralization;

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
     * Internal representation of attribute types. Quasi-identifiers are split into the
     * ones to which generalization is applied and the ones to which microaggregation is applied
     * @author Florian Kohlmayer
     * @author Fabian Prasser
     *
     */
    public static class AttributeTypeInternal {
        public static final int QUASI_IDENTIFYING_GENERALIZED     = 0;
        public static final int QUASI_IDENTIFYING_MICROAGGREGATED = 4;
        public static final int IDENTIFYING                       = 3;
        public static final int INSENSITIVE                       = 2;
        public static final int SENSITIVE                         = 1;
    }
    
    /** The data which is generalized */
    protected final Data                                 dataGH;
    
    /** The data. */
    protected final Data                                 dataDI;
    
    /** The data which is insensitive */
    protected final Data                                 dataIS;
    
    /** The buffer. */
    protected final Data                                 bufferOT;
    
    /** The generalization hierarchiesQI. */
    protected final GeneralizationHierarchy[]            hierarchiesQI;
    
    /** The microaggregation functions. */
    protected final DistributionAggregateFunction[]      functionsMA;
    
    /** The start index of the microaggregation attributes in the dataDI */
    protected final int                                  startMA;
    
    /** The number of microaggregation attributes in the dataDI */
    protected final int                                  numMA;
    
    /** The sensitive attributes. */
    protected final Map<String, GeneralizationHierarchy> hierarchiesSE;
    
    /** The indexes of sensitive attributes. */
    protected final Map<String, Integer>                 indexesSE;
    
    /** The hierarchy heights for each QI. */
    protected final int[]                                hierarchyHeights;
    
    /** The maximum level for each QI. */
    protected final int[]                                maxLevels;
    
    /** The minimum level for each QI. */
    protected final int[]                                minLevels;
    
    /** The original input header. */
    protected final String[]                             header;
    
    /** The research subset, if any. */
    protected RowSet                                     subset     = null;
    
    /** The size of the research subset. */
    protected int                                        subsetSize = 0;
    
    /**
     * Creates a new data manager from pre-encoded data.
     *
     * @param header
     * @param data
     * @param dictionary
     * @param definition
     * @param criteria
     * @param function
     */
    public DataManager(final String[] header, 
                       final int[][] data, 
                       final Dictionary dictionary, 
                       final DataDefinition definition, 
                       final Set<PrivacyCriterion> criteria,
                       final Map<String, DistributionAggregateFunction> functions) {
        
        // Store research subset
        for (PrivacyCriterion c : criteria) {
            if (c instanceof DPresence) {
                subset = ((DPresence) c).getSubset().getSet();
                subsetSize = ((DPresence) c).getSubset().getArray().length;
                break;
            }
        }
        
        // Store columns for reordering the output
        this.header = header;
        
        Set<String> gh = definition.getQuasiIdentifiersWithGeneralization();
        Set<String> se = definition.getSensitiveAttributes();
        Set<String> ma = definition.getQuasiIdentifiersWithMicroaggregation();
        Set<String> is = definition.getInsensitiveAttributes();
        
        // Init dictionary
        final Dictionary dictionaryGH = new Dictionary(gh.size());              // Generalization
        final Dictionary dictionaryDI = new Dictionary(se.size() + ma.size());  // Frequency distributions
        final Dictionary dictionaryIS = new Dictionary(is.size());              // Nothing
        final Dictionary dictionaryOT = new Dictionary(ma.size());              // Anything else
        
        // Init maps for reordering the output
        final int[] mapGH = new int[dictionaryGH.getNumDimensions()];
        final int[] mapDI = new int[dictionaryDI.getNumDimensions()];
        final int[] mapIS = new int[dictionaryIS.getNumDimensions()];
        final int[] mapOT = new int[dictionaryOT.getNumDimensions()];
        
        // Indexes
        this.startMA = se.size();
        this.numMA = ma.size();
        int indexGH = 0;
        int indexSE = 0;
        int indexIS = 0;
        int indexOT = 0;
        int indexMA = this.startMA;
        int counter = 0;
        
        // Build map: DI contains SE and MA attributes. First all SE attributes, followed by all MA attributes.
        
        // A map for column indices. map[i*2]=attribute type, map[i*2+1]=index position. */
        final int[] map = new int[header.length * 2];
        final String[] headerGH = new String[dictionaryGH.getNumDimensions()];
        final String[] headerDI = new String[dictionaryDI.getNumDimensions()];
        final String[] headerIS = new String[dictionaryIS.getNumDimensions()];
        final String[] headerOT = new String[dictionaryOT.getNumDimensions()];
        
        for (final String column : header) {
            final int idx = counter * 2;
            if (gh.contains(column)) {
                map[idx] = AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED;
                map[idx + 1] = indexGH;
                mapGH[indexGH] = counter;
                dictionaryGH.registerAll(indexGH, dictionary, counter);
                headerGH[indexGH] = header[counter];
                indexGH++;
            } else if (ma.contains(column)) {
                map[idx] = AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED;
                map[idx + 1] = indexMA;
                mapDI[indexMA] = counter;
                dictionaryDI.registerAll(indexMA, dictionary, counter);
                headerDI[indexMA] = header[counter];
                indexMA++;
                mapOT[indexOT] = counter;
                dictionaryOT.registerAll(indexOT, dictionary, counter);
                headerOT[indexOT] = header[counter];
                indexOT++;
            } else if (is.contains(column)) {
                map[idx] = AttributeTypeInternal.INSENSITIVE;
                map[idx + 1] = indexIS;
                mapIS[indexIS] = counter;
                dictionaryIS.registerAll(indexIS, dictionary, counter);
                headerIS[indexIS] = header[counter];
                indexIS++;
            } else if (se.contains(column)) {
                map[idx] = AttributeTypeInternal.SENSITIVE;
                map[idx + 1] = indexSE;
                mapDI[indexSE] = counter;
                dictionaryDI.registerAll(indexSE, dictionary, counter);
                headerDI[indexSE] = header[counter];
                indexSE++;
            } else {
                // TODO: CHECK: Changed default? - now all undefined attributes are identifying! Previously they were sensitive?
                map[idx] = AttributeTypeInternal.IDENTIFYING;
                map[idx + 1] = -1;
            }
            counter++;
        }
        
        // encode Data
        final Data[] ddata = encode(data, map, mapGH, mapDI, mapIS, mapOT, dictionaryGH, dictionaryDI, dictionaryIS, dictionaryOT, headerGH, headerDI, headerIS, headerOT, startMA);
        dataGH = ddata[0];
        dataDI = ddata[1];
        dataIS = ddata[2];
        bufferOT = ddata[3];
        
        // Initialize minlevels
        minLevels = new int[gh.size()];
        hierarchyHeights = new int[gh.size()];
        maxLevels = new int[gh.size()];
        
        // Build hierarchiesQI
        hierarchiesQI = new GeneralizationHierarchy[gh.size()];
        for (int i = 0; i < header.length; i++) {
            final int idx = i * 2;
            if (gh.contains(header[i]) && map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED) {
                final int dictionaryIndex = map[idx + 1];
                final String name = header[i];
                
                if (definition.getHierarchy(name) != null) {
                    hierarchiesQI[dictionaryIndex] = new GeneralizationHierarchy(name, definition.getHierarchy(name), dictionaryIndex, dictionaryGH);
                } else {
                    throw new IllegalStateException("No hierarchy available for attribute (" + header[i] + ")");
                }
                // Initialize hierarchy height and minimum / maximum generalization
                hierarchyHeights[dictionaryIndex] = hierarchiesQI[dictionaryIndex].getArray()[0].length;
                final Integer minGenLevel = definition.getMinimumGeneralization(name);
                minLevels[dictionaryIndex] = minGenLevel == null ? 0 : minGenLevel;
                final Integer maxGenLevel = definition.getMaximumGeneralization(name);
                maxLevels[dictionaryIndex] = maxGenLevel == null ? hierarchyHeights[dictionaryIndex] - 1 : maxGenLevel;
            }
        }

        // Build map with hierarchies for sensitive attributes
        Map<String, String[][]> sensitiveHierarchies = new HashMap<String, String[][]>();
        for (PrivacyCriterion c : criteria) {
            if (c instanceof HierarchicalDistanceTCloseness) {
                HierarchicalDistanceTCloseness t = (HierarchicalDistanceTCloseness) c;
                sensitiveHierarchies.put(t.getAttribute(), t.getHierarchy().getHierarchy());
            }
        }
        
        // Build generalization hierarchies for sensitive attributes
        hierarchiesSE = new HashMap<String, GeneralizationHierarchy>();
        indexesSE = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            final String name = header[i];
            final int idx = i * 2;
            if (sensitiveHierarchies.containsKey(name) && map[idx] == AttributeTypeInternal.SENSITIVE) {
                final int dictionaryIndex = map[idx + 1];
                final String[][] hiers = sensitiveHierarchies.get(name);
                if (hiers != null) {
                    hierarchiesSE.put(name, new GeneralizationHierarchy(name, hiers, dictionaryIndex, dictionaryDI));
                }
            }
            
            // Store index for sensitive attributes
            if (se.contains(header[i])) {
                indexesSE.put(name, index);
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
            if (maHierarchies.containsKey(name) && map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED) {
                final int dictionaryIndex = map[idx + 1];
                final String[][] hiers = maHierarchies.get(name);
                if (hiers != null) {
                    hierarchiesMA.put(name, new GeneralizationHierarchy(name, hiers, dictionaryIndex, dictionaryDI).map);
                }
            }
        }

        // finalize dictionary
        dictionaryGH.finalizeAll();
        dictionaryDI.finalizeAll();
        dictionaryIS.finalizeAll();
        dictionaryOT.finalizeAll();
        
        // Init microaggregation functions
        functionsMA = new DistributionAggregateFunction[ma.size()];
        for (int i = 0; i < header.length; i++) {
            final int idx = i * 2;
            if (ma.contains(header[i]) && map[idx] == AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED) {
                final int dictionaryIndex = map[idx + 1] - startMA;
                final String name = header[i];
                if (definition.getMicroAggregationFunction(name) != null) {
                    functionsMA[dictionaryIndex] = functions.get(name);
                    functionsMA[dictionaryIndex].initialize(dictionaryDI.getMapping()[dictionaryIndex + startMA], 
                                                            definition.getDataType(name),
                                                            hierarchiesMA.get(name));
                } else {
                    throw new IllegalStateException("No microaggregation function defined for attribute (" + header[i] + ")");
                }
            }
        }
    }
    
    /**
     * 
     *
     * @param dataGH
     * @param dataDI
     * @param dataIS
     * @param hierarchiesQI
     * @param hierarchiesSE
     * @param indexesSE
     * @param hierarchyHeights
     * @param maxLevels
     * @param minLevels
     * @param header
     */
    protected DataManager(final Data dataGH,
                          final Data dataDI,
                          final Data dataIS,
                          final Data bufferOT,
                          final GeneralizationHierarchy[] hierarchiesQI,
                          final DistributionAggregateFunction[] functionsMA,
                          final int startIndexMA,
                          final int numMA,
                          final Map<String, GeneralizationHierarchy> hierarchiesSE,
                          final Map<String, Integer> indexesSE,
                          final int[] hierarchyHeights,
                          final int[] maxLevels,
                          final int[] minLevels,
                          final String[] header) {
        this.dataGH = dataGH;
        this.dataDI = dataDI;
        this.dataIS = dataIS;
        this.hierarchiesQI = hierarchiesQI;
        this.hierarchiesSE = hierarchiesSE;
        this.hierarchyHeights = hierarchyHeights;
        this.maxLevels = maxLevels;
        this.minLevels = minLevels;
        this.indexesSE = indexesSE;
        this.header = header;
        this.functionsMA = functionsMA;
        this.bufferOT = bufferOT;
        this.startMA = startIndexMA;
        this.numMA = numMA;
    }
    
    /**
     * Returns the buffer.
     * 
     * @return
     */
    public Data getBufferOT() {
        return bufferOT;
    }
    
    /**
     * Returns the data.
     *
     * @return the data
     */
    public Data getDataDI() {
        return dataDI;
    }
    
    /**
     * Returns the data.
     *
     * @return the data
     */
    public Data getDataGH() {
        return dataGH;
    }
    
    /**
     * Returns the data.
     *
     * @return the data
     */
    public Data getDataIS() {
        return dataIS;
    }
    
    /**
     * Returns the distribution of the given sensitive attribute in
     * the original dataset. Required for t-closeness.
     * 
     * @param attribute
     * @return distribution
     */
    public double[] getDistribution(String attribute) {
        
        // TODO: Distribution size equals the size of the complete dataset
        // TODO: Good idea?
        final int index = indexesSE.get(attribute);
        final int distinct = dataDI.getDictionary().getMapping()[index].length;
        final int[][] data = dataDI.getArray();
        
        // Initialize counts: iterate over all rows or the subset
        final int[] cardinalities = new int[distinct];
        for (int i = 0; i < data.length; i++) {
            if (subset == null || subset.contains(i)) {
                cardinalities[data[i][index]]++;
            }
        }
        
        // compute distribution
        final double total = subset == null ? data.length : subsetSize;
        final double[] distribution = new double[cardinalities.length];
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = (double) cardinalities[i] / total;
        }
        return distribution;
    }
    
    /**
     * Returns the microaggregation functions.
     * 
     * @return
     */
    public DistributionAggregateFunction[] getFunctionsMA() {
        return functionsMA;
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
    
    public int[] getHierachyHeights() {
        return hierarchyHeights;
    }
    
    /**
     * Returns the generalization hierarchiesQI.
     * 
     * @return the hierarchiesQI
     */
    public GeneralizationHierarchy[] getHierarchies() {
        return hierarchiesQI;
    }
    
    /**
     * Returns the maximum levels for the generalizaiton.
     * 
     * @return the maximum level for each QI
     */
    public int[] getMaxLevels() {
        return maxLevels;
    }
    
    /**
     * Returns the minimum levels for the generalizations.
     * 
     * @return
     */
    
    public int[] getMinLevels() {
        return minLevels;
    }
    
    /**
     * Gets the number of microaggregation attributes.
     * @return
     */
    public int getNumMA() {
        return numMA;
    }
    
    /**
     * Gets the start index of the microaggregation attributes in the dataDI.
     * @return
     */
    public int getStartMA() {
        return startMA;
    }
    
    /**
     * Returns the tree for the given sensitive attribute, if a generalization hierarchy
     * is associated. Required for t-closeness with hierarchical distance EMD
     * 
     * @param attribute
     * @return tree
     */
    public int[] getTree(String attribute) {
        
        final int[][] data = dataDI.getArray();
        final int index = indexesSE.get(attribute);
        final int[][] hierarchy = hierarchiesSE.get(attribute).map;
        final int totalElementsP = subset == null ? data.length : subsetSize;
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
        for (int i = 0; i < data.length; i++) {
            if (subset == null || subset.contains(i)) {
                int previousFreq = treeList.get(data[i][index] + offsetLeafs);
                previousFreq++;
                treeList.set(data[i][index] + offsetLeafs, previousFreq);
            }
        }
        
        // Init extras
        for (int i = 0; i < numLeafs; i++) {
            treeList.add(-1);
        }
        
        // Temporary class for nodes
        class TNode {
            IntOpenHashSet children = new IntOpenHashSet();
            int            offset   = 0;
            int            level    = 0;
        }
        
        final int offsetsExtras = offsetLeafs + numLeafs;
        final IntObjectOpenHashMap<TNode> nodes = new IntObjectOpenHashMap<TNode>();
        final ArrayList<ArrayList<TNode>> levels = new ArrayList<ArrayList<TNode>>();
        
        // Init levels
        for (int i = 0; i < hierarchy[0].length; i++) {
            levels.add(new ArrayList<TNode>());
        }
        
        // Build nodes
        for (int i = 0; i < hierarchy[0].length; i++) {
            for (int j = 0; j < hierarchy.length; j++) {
                final int nodeID = hierarchy[j][i];
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
                    curNode.children.add(hierarchy[j][i - 1]);
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
                            treeList.add(node.level == 1 ? keys[i] + offsetsExtras : nodes.get(keys[i]).offset);
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
     * Encodes the data.
     *
     * @param data
     * @param map
     * @param mapGH
     * @param mapDI
     * @param mapIS
     * @param dictionaryGH
     * @param dictionaryDI
     * @param dictionaryIS
     * @param headerGH
     * @param headerDI
     * @param headerIS
     * @return
     */
    private Data[] encode(final int[][] data,
                          final int[] map,
                          final int[] mapGH,
                          final int[] mapDI,
                          final int[] mapIS,
                          final int[] mapOT,
                          final Dictionary dictionaryGH,
                          final Dictionary dictionaryDI,
                          final Dictionary dictionaryIS,
                          final Dictionary dictionaryOT,
                          final String[] headerGH,
                          final String[] headerDI,
                          final String[] headerIS,
                          final String[] headerOT,
                          final int startIndexMA) {
        
        // Parse the dataset
        final int[][] valsGH = new int[data.length][];
        final int[][] valsDI = new int[data.length][];
        final int[][] valsIS = new int[data.length][];
        final int[][] valsOT = new int[data.length][];
        
        int index = 0;
        for (final int[] tuple : data) {
            
            // Process a tuple
            final int[] tupleGH = new int[headerGH.length];
            final int[] tupleDI = new int[headerDI.length];
            final int[] tupleIS = new int[headerIS.length];
            final int[] tupleOT = new int[headerOT.length];
            
            for (int i = 0; i < tuple.length; i++) {
                final int idx = i * 2;
                int aType = map[idx];
                final int iPos = map[idx + 1];
                switch (aType) {
                case AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED:
                    tupleGH[iPos] = tuple[i];
                    break;
                case AttributeTypeInternal.IDENTIFYING:
                    // Ignore
                    break;
                case AttributeTypeInternal.INSENSITIVE:
                    tupleIS[iPos] = tuple[i];
                    break;
                case AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED:
                    tupleDI[iPos] = tuple[i];
                    tupleOT[iPos - startIndexMA] = tuple[i];
                    break;
                case AttributeTypeInternal.SENSITIVE:
                    tupleDI[iPos] = tuple[i];
                    break;
                }
            }
            valsGH[index] = tupleGH;
            valsIS[index] = tupleIS;
            valsDI[index] = tupleDI;
            valsOT[index] = tupleOT;
            index++;
        }
        
        // Build data object
        final Data[] result = { new Data(valsGH, headerGH, mapGH, dictionaryGH), new Data(valsDI, headerDI, mapDI, dictionaryDI), new Data(valsIS, headerIS, mapIS, dictionaryIS), new Data(valsOT, headerOT, mapOT, dictionaryOT) };
        return result;
    }
}
