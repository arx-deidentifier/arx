/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds all data needed for the anonymization process.
 * 
 * @author Prasser, Kohlmayer
 */
public class DataManager {

    /** The data. */
    protected final Data                      dataQI;

    /** The data. */
    protected final Data                      dataSE;

    /** The data. */
    protected final Data                      dataIS;

    /** The generalization hierarchiesQI. */
    protected final GeneralizationHierarchy[] hierarchiesQI;

    /** The sensitive attribute */
    protected final GeneralizationHierarchy[] hierarchiesSE;

    /** The hierarchy heights for each QI. */
    protected final int[]                     hierarchyHeights;

    /** The maximum level for each QI. */
    protected final int[]                     maxLevels;

    /** The minimum level for each QI. */
    protected final int[]                     minLevels;

    /** The original input header */
    protected final String[]                  header;

    /**
     * Can be used to create a copy of the datamanager
     * 
     * @param dataQI
     * @param dataSE
     * @param dataIS
     * @param hierarchiesQI
     * @param hierarchyHeights
     * @param maxLevels
     * @param minLevels
     * @param header
     * @param strictMode
     */

    protected DataManager(final Data dataQI,
                          final Data dataSE,
                          final Data dataIS,
                          final GeneralizationHierarchy[] hierarchiesQI,
                          final GeneralizationHierarchy[] hierarchiesSE,
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
        this.header = header;
    }

    /**
     * Creates a new data manager from pre-encoded data
     * 
     * @param header
     * @param data
     * @param dictionary
     * @param generalizationHierarchies
     * @param minGeneralization
     * @param maxGeneralization
     * @param sensitiveAttributes
     * @param insensitiveAttributes
     * @param identifiers
     * @param strictMode
     */
    public DataManager(final String[] header,
                       final int[][] data,
                       final Dictionary dictionary,
                       final Map<String, String[][]> generalizationHierarchies,
                       final Map<String, Integer> minGeneralization,
                       final Map<String, Integer> maxGeneralization,
                       final Map<String, String[][]> sensitiveHierarchies,
                       final Set<String> insensitiveAttributes,
                       final Set<String> identifiers) {

        // Store columns for reordering the output
        this.header = header;

        sanityCheck(header,
                    generalizationHierarchies,
                    minGeneralization,
                    sensitiveHierarchies,
                    insensitiveAttributes,
                    identifiers);

        // Init dictionary
        final Dictionary dictionaryQI = new Dictionary(generalizationHierarchies.size());
        final Dictionary dictionarySE = new Dictionary(sensitiveHierarchies.size());
        final Dictionary dictionaryIS = new Dictionary(insensitiveAttributes.size());

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
            if (generalizationHierarchies.containsKey(column)) {
                map[counter] = indexQI + 1;
                mapQI[indexQI] = counter;
                dictionaryQI.registerAll(indexQI, dictionary, counter);
                headerQI[indexQI] = header[counter];
                indexQI++;
            } else if (insensitiveAttributes.contains(column)) {
                map[counter] = indexIS + 1000;
                mapIS[indexIS] = counter;
                dictionaryIS.registerAll(indexIS, dictionary, counter);
                headerIS[indexIS] = header[counter];
                indexIS++;
            } else if (!identifiers.contains(column)) {
                map[counter] = -indexSE - 1;
                mapSE[indexSE] = counter;
                dictionarySE.registerAll(indexSE, dictionary, counter);
                headerSE[indexSE] = header[counter];
                indexSE++;
            }
            counter++;
        }

        // encode Data
        final Data[] ddata = encode(data,
                                    map,
                                    mapQI,
                                    mapSE,
                                    mapIS,
                                    dictionaryQI,
                                    dictionarySE,
                                    dictionaryIS,
                                    headerQI,
                                    headerSE,
                                    headerIS);
        dataQI = ddata[0];
        dataSE = ddata[1];
        dataIS = ddata[2];

        // Initialize minlevels
        minLevels = new int[generalizationHierarchies.size()];
        hierarchyHeights = new int[generalizationHierarchies.size()];
        maxLevels = new int[generalizationHierarchies.size()];

        // Build qi generalisation hierarchiesQI
        hierarchiesQI = new GeneralizationHierarchy[generalizationHierarchies.size()];
        for (int i = 0; i < header.length; i++) {
            if (generalizationHierarchies.containsKey(header[i])) {
                final int dictionaryIndex = map[i] - 1;
                if ((dictionaryIndex >= 0) && (dictionaryIndex < 999)) {
                    final String name = header[i];
                    hierarchiesQI[dictionaryIndex] = new GeneralizationHierarchy(name,
                                                                                 generalizationHierarchies.get(name),
                                                                                 dictionaryIndex,
                                                                                 dictionaryQI);

                    // initialize min/max level and hierarhy height array
                    hierarchyHeights[dictionaryIndex] = hierarchiesQI[dictionaryIndex].getArray()[0].length;
                    final Integer minGenLevel = minGeneralization.get(name);
                    minLevels[dictionaryIndex] = minGenLevel == null ? 0
                            : minGenLevel;
                    final Integer maxGenLevel = maxGeneralization.get(name);
                    maxLevels[dictionaryIndex] = maxGenLevel == null ? hierarchyHeights[dictionaryIndex] - 1
                            : maxGenLevel;
                }
            }
        }

        // Build sa generalisation hierarchies
        hierarchiesSE = new GeneralizationHierarchy[sensitiveHierarchies.size()];
        for (int i = 0; i < header.length; i++) {
            if (sensitiveHierarchies.containsKey(header[i])) { // consider only
                                                               // sensitve
                                                               // attributes
                final int dictionaryIndex = -map[i] - 1;
                if ((dictionaryIndex >= 0) && (dictionaryIndex < 999)) {
                    final String name = header[i];
                    final String[][] hiers = sensitiveHierarchies.get(name);
                    if (hiers != null) {
                        hierarchiesSE[dictionaryIndex] = new GeneralizationHierarchy(name,
                                                                                     hiers,
                                                                                     dictionaryIndex,
                                                                                     dictionarySE);
                    }
                }
            }
        }

        // finalize dictionary
        dictionaryQI.finalizeAll();
        dictionarySE.finalizeAll();
        dictionaryIS.finalizeAll();

    }

    /**
     * Encodes the data
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
        final Data[] result = { new Data(valsQI, headerQI, mapQI, dictionaryQI),
                new Data(valsSE, headerSE, mapSE, dictionarySE),
                new Data(valsIS, headerIS, mapIS, dictionaryIS) };
        return result;
    }

    /**
     * Returns the data
     * 
     * @return the data
     */
    public Data getDataIS() {
        return dataIS;
    }

    /**
     * Returns the data
     * 
     * @return the data
     */
    public Data getDataQI() {
        return dataQI;
    }

    /**
     * Returns the data
     * 
     * @return the data
     */
    public Data getDataSE() {
        return dataSE;
    }

    /**
     * Returns the distribution for t-closeness with equal distance
     * 
     * @return
     */
    public double[] getDistribution() {

        final int distinct = dataSE.getDictionary().getMapping()[0].length;
        final int[][] data = dataSE.getArray();

        // Initialize counts
        final int[] cardinalities = new int[distinct];
        for (int i = 0; i < data.length; i++) { // iterate over all rows
            cardinalities[data[i][0]]++;
        }

        // compute distribution
        final double[] distribution = new double[cardinalities.length];
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = ((double) cardinalities[i]) /
                              ((double) data.length);
        }
        return distribution;
    }

    /**
     * The original data header
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
     * Returns the tree required for t-closeness with EMD_HIERARCHICAL
     * 
     * @return
     */
    public int[] getTree() {

        final int[][] data = dataSE.getArray();
        final int[][] hierarchy = hierarchiesSE[0].map;
        final int totalElementsP = data.length;
        final int height = hierarchy[0].length - 1;
        final int numLeafs = hierarchy.length;

        // size could be calculated?!
        final ArrayList<Integer> treeList = new ArrayList<Integer>();
        treeList.add(totalElementsP);
        treeList.add(numLeafs);
        treeList.add(height);

        // init all freq to 0
        for (int i = 0; i < numLeafs; i++) {
            treeList.add(0);
        }

        // count frequnecies
        final int offsetLeafs = 3;
        for (int i = 0; i < data.length; i++) {
            int previousFreq = treeList.get(data[i][0] + offsetLeafs);
            previousFreq++;
            treeList.set(data[i][0] + offsetLeafs, previousFreq);

        }

        // init extras
        for (int i = 0; i < numLeafs; i++) {
            treeList.add(-1);
        }

        class TNode {
            HashSet<Integer> children = new HashSet<Integer>();
            int              offset   = 0;
            int              level    = 0;
        }

        final int offsetsExtras = offsetLeafs + numLeafs;

        final HashMap<Integer, TNode> nodes = new HashMap<Integer, TNode>();
        final ArrayList<ArrayList<TNode>> levels = new ArrayList<ArrayList<TNode>>();
        // init levels
        for (int i = 0; i < hierarchy[0].length; i++) {
            levels.add(new ArrayList<TNode>());
        }

        // build nodes
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

        // for all nodes
        for (final ArrayList<TNode> level : levels) {
            for (final TNode node : level) {

                if (node.level > 0) { // only inner nodes
                    node.offset = treeList.size();

                    treeList.add(node.children.size());
                    treeList.add(node.level);

                    for (final int child : node.children) {
                        if (node.level == 1) { // level 1
                            treeList.add(child + offsetsExtras);
                        } else {
                            treeList.add(nodes.get(child).offset);
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
     * Performs a sanity check
     * 
     * @param columns
     * @param generalizationHierarchies
     * @param minGeneralization
     * @param sensitiveAttributes
     * @param insensitiveAttributes
     * @param identifiers
     */
    private void
            sanityCheck(final String[] columns,
                        final Map<String, String[][]> generalizationHierarchies,
                        final Map<String, Integer> minGeneralization,
                        final Map<String, String[][]> sensitiveHierarchy,
                        final Set<String> insensitiveAttributes,
                        final Set<String> identifiers) {

        final int mappedColumnsCount = generalizationHierarchies.size() +
                                       sensitiveHierarchy.size() +
                                       insensitiveAttributes.size() +
                                       identifiers.size();
        if (mappedColumnsCount < columns.length) {
            // TODO: We always treat undefined attributes as identifiers
            for (int i = 0; i < columns.length; i++) {
                final String columnName = columns[i];
                if (!(generalizationHierarchies.containsKey(columnName) ||
                      sensitiveHierarchy.containsKey(columnName) || insensitiveAttributes.contains(columnName))) {
                    identifiers.add(columnName);
                }
            }
        }
    }

}
