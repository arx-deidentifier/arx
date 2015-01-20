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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * Holds all data needed for the anonymization process.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataManager {

    /** The data. */
    protected final Data                                 dataQI;

    /** The data. */
    protected final Data                                 dataSE;

    /** The data. */
    protected final Data                                 dataIS;

    /** The generalization hierarchiesQI. */
    protected final GeneralizationHierarchy[]            hierarchiesQI;

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
    protected RowSet                           subset     = null;

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
     */
    public DataManager(final String[] header, final int[][] data, final Dictionary dictionary, final DataDefinition definition, final Set<PrivacyCriterion> criteria) {

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

        Set<String> id = getIdentifiers(header, definition);
        Set<String> qi = definition.getQuasiIdentifyingAttributes();
        Set<String> se = definition.getSensitiveAttributes();
        Set<String> is = definition.getInsensitiveAttributes();

        // Init dictionary
        final Dictionary dictionaryQI = new Dictionary(qi.size());
        final Dictionary dictionarySE = new Dictionary(se.size());
        final Dictionary dictionaryIS = new Dictionary(is.size());

        // Init maps for reordering the output
        final int[] mapQI = new int[dictionaryQI.getNumDimensions()];
        final int[] mapSE = new int[dictionarySE.getNumDimensions()];
        final int[] mapIS = new int[dictionaryIS.getNumDimensions()];

        // Indexes
        int indexQI = 0;
        int indexSE = 0;
        int indexIS = 0;
        int counter = 0;

        // Build map
        final int[] map = new int[header.length];
        final String[] headerQI = new String[dictionaryQI.getNumDimensions()];
        final String[] headerSE = new String[dictionarySE.getNumDimensions()];
        final String[] headerIS = new String[dictionaryIS.getNumDimensions()];
        for (final String column : header) {
            if (qi.contains(column)) {
                map[counter] = indexQI + 1;
                mapQI[indexQI] = counter;
                dictionaryQI.registerAll(indexQI, dictionary, counter);
                headerQI[indexQI] = header[counter];
                indexQI++;
            } else if (is.contains(column)) {
                map[counter] = indexIS + 1000;
                mapIS[indexIS] = counter;
                dictionaryIS.registerAll(indexIS, dictionary, counter);
                headerIS[indexIS] = header[counter];
                indexIS++;
            } else if (!id.contains(column)) {
                map[counter] = -indexSE - 1;
                mapSE[indexSE] = counter;
                dictionarySE.registerAll(indexSE, dictionary, counter);
                headerSE[indexSE] = header[counter];
                indexSE++;
            }
            counter++;
        }

        // encode Data
        final Data[] ddata = encode(data, map, mapQI, mapSE, mapIS, dictionaryQI, dictionarySE, dictionaryIS, headerQI, headerSE, headerIS);
        dataQI = ddata[0];
        dataSE = ddata[1];
        dataIS = ddata[2];

        // Initialize minlevels
        minLevels = new int[qi.size()];
        hierarchyHeights = new int[qi.size()];
        maxLevels = new int[qi.size()];

        // Build qi generalisation hierarchiesQI
        hierarchiesQI = new GeneralizationHierarchy[qi.size()];
        for (int i = 0; i < header.length; i++) {
            if (qi.contains(header[i])) {
                final int dictionaryIndex = map[i] - 1;
                if ((dictionaryIndex >= 0) && (dictionaryIndex < 999)) {
                    final String name = header[i];

                    if (definition.getAttributeType(name) instanceof Hierarchy) {
                        hierarchiesQI[dictionaryIndex] = new GeneralizationHierarchy(name, definition.getHierarchy(name), dictionaryIndex, dictionaryQI);
                    } else {
                        throw new IllegalStateException("No hierarchy available for attribute ("+header[i]+")");
                    }
                    // Initialize hierarchy height and minimum / maximum generalization
                    hierarchyHeights[dictionaryIndex] = hierarchiesQI[dictionaryIndex].getArray()[0].length;
                    final Integer minGenLevel = definition.getMinimumGeneralization(name);
                    minLevels[dictionaryIndex] = minGenLevel == null ? 0 : minGenLevel;
                    final Integer maxGenLevel = definition.getMaximumGeneralization(name);
                    maxLevels[dictionaryIndex] = maxGenLevel == null ? hierarchyHeights[dictionaryIndex] - 1 : maxGenLevel;
                }
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

        // Build sa generalisation hierarchies
        hierarchiesSE = new HashMap<String, GeneralizationHierarchy>();
        indexesSE = new HashMap<String, Integer>();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            final String name = header[i];
            if (sensitiveHierarchies.containsKey(name)) {
                final int dictionaryIndex = -map[i] - 1;
                if ((dictionaryIndex >= 0) && (dictionaryIndex < 999)) {
                    final String[][] hiers = sensitiveHierarchies.get(name);
                    if (hiers != null) {
                        hierarchiesSE.put(name, new GeneralizationHierarchy(name, hiers, dictionaryIndex, dictionarySE));
                    }
                } else {
                    throw new RuntimeException("Internal error: Invalid dictionary index!");
                }
            }

            // Store index for sensitive attributes
            if (se.contains(header[i])) {
                indexesSE.put(name, index);
                index++;
            }
        }

        // finalize dictionary
        dictionaryQI.finalizeAll();
        dictionarySE.finalizeAll();
        dictionaryIS.finalizeAll();

    }

    /**
     * 
     *
     * @param dataQI
     * @param dataSE
     * @param dataIS
     * @param hierarchiesQI
     * @param hierarchiesSE
     * @param indexesSE
     * @param hierarchyHeights
     * @param maxLevels
     * @param minLevels
     * @param header
     */
    protected DataManager(final Data dataQI,
                          final Data dataSE,
                          final Data dataIS,
                          final GeneralizationHierarchy[] hierarchiesQI,
                          final Map<String, GeneralizationHierarchy> hierarchiesSE,
                          final Map<String, Integer> indexesSE,
                          final int[] hierarchyHeights,
                          final int[] maxLevels,
                          final int[] minLevels,
                          final String[] header) {
        this.dataQI = dataQI;
        this.dataSE = dataSE;
        this.dataIS = dataIS;
        this.hierarchiesQI = hierarchiesQI;
        this.hierarchiesSE = hierarchiesSE;
        this.hierarchyHeights = hierarchyHeights;
        this.maxLevels = maxLevels;
        this.minLevels = minLevels;
        this.indexesSE = indexesSE;
        this.header = header;
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
     * Returns the data.
     *
     * @return the data
     */
    public Data getDataQI() {
        return dataQI;
    }

    /**
     * Returns the data.
     *
     * @return the data
     */
    public Data getDataSE() {
        return dataSE;
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
        final int distinct = dataSE.getDictionary().getMapping()[index].length;
        final int[][] data = dataSE.getArray();

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
     * Returns the tree for the given sensitive attribute, if a generalization hierarchy
     * is associated. Required for t-closeness with hierarchical distance EMD
     * 
     * @param attribute
     * @return tree
     */
    public int[] getTree(String attribute) {

        final int[][] data = dataSE.getArray();
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

                    final int [] keys = node.children.keys;
                    final boolean [] allocated = node.children.allocated;
                    for (int i=0; i<allocated.length; i++){
                        if (allocated[i]) {
                            treeList.add(node.level==1 ? keys[i] + offsetsExtras : nodes.get(keys[i]).offset);
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
     * @param mapQI
     * @param mapSE
     * @param mapIS
     * @param dictionaryQI
     * @param dictionarySE
     * @param dictionaryIS
     * @param headerQI
     * @param headerSE
     * @param headerIS
     * @return
     */
    private Data[] encode(final int[][] data,
                          final int[] map,
                          final int[] mapQI,
                          final int[] mapSE,
                          final int[] mapIS,
                          final Dictionary dictionaryQI,
                          final Dictionary dictionarySE,
                          final Dictionary dictionaryIS,
                          final String[] headerQI,
                          final String[] headerSE,
                          final String[] headerIS) {

        // Parse the dataset
        final int[][] valsQI = new int[data.length][];
        final int[][] valsSE = new int[data.length][];
        final int[][] valsIS = new int[data.length][];

        int index = 0;
        for (final int[] tuple : data) {

            // Process a tuple
            final int[] tupleQI = new int[headerQI.length];
            final int[] tupleSE = new int[headerSE.length];
            final int[] tupleIS = new int[headerIS.length];

            for (int i = 0; i < tuple.length; i++) {
                if (map[i] >= 1000) {
                    tupleIS[map[i] - 1000] = tuple[i];
                } else if (map[i] > 0) {
                    tupleQI[map[i] - 1] = tuple[i];
                } else if (map[i] < 0) {
                    tupleSE[-map[i] - 1] = tuple[i];
                }
            }
            valsQI[index] = tupleQI;
            valsIS[index] = tupleIS;
            valsSE[index] = tupleSE;
            index++;
        }

        // Build data object
        final Data[] result = { new Data(valsQI, headerQI, mapQI, dictionaryQI), new Data(valsSE, headerSE, mapSE, dictionarySE), new Data(valsIS, headerIS, mapIS, dictionaryIS) };
        return result;
    }

    /**
     * Performs a sanity check and returns all identifying attributes.
     *
     * @param columns
     * @param definition
     * @return
     */
    private Set<String> getIdentifiers(final String[] columns, final DataDefinition definition) {

        Set<String> result = new HashSet<String>();
        result.addAll(definition.getIdentifyingAttributes());

        final int mappedColumnsCount = definition.getQuasiIdentifyingAttributes().size() + definition.getSensitiveAttributes().size() + definition.getInsensitiveAttributes().size() +
                                       definition.getIdentifyingAttributes().size();

        // We always treat undefined attributes as identifiers
        if (mappedColumnsCount < columns.length) {
            for (int i = 0; i < columns.length; i++) {

                final String attribute = columns[i];

                // Check
                if (!(definition.getQuasiIdentifyingAttributes().contains(attribute) || definition.getSensitiveAttributes().contains(attribute) || definition.getInsensitiveAttributes()
                                                                                                                                                             .contains(attribute))) {

                    // Add to identifiers
                    result.add(attribute);
                }
            }
        }

        return result;
    }
}
