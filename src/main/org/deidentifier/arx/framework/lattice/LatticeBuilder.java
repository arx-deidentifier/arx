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

package org.deidentifier.arx.framework.lattice;

/**
 * The class LatticeBuilder.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class LatticeBuilder {

    /** The levels. */
    private Node[][] levels     = null;

    /** The maxlevels. */
    private int[]    maxLevels  = null;

    /** The minlevels. */
    private int[]    minLevels  = null;

    /**
     * Instantiates a new lattice builder.
     *
     * @param maxLevels the maxlevels
     * @param minLevels the minlevels
     * @param maxHeights
     */
    public LatticeBuilder(final int[] maxLevels,
                          final int[] minLevels) {
        this.maxLevels = maxLevels;
        this.minLevels = minLevels;
    }

    /**
     * Builds the.
     * 
     * @return the lattice
     */
    public Lattice build() {
        final int numNodes = buildLevelsAndMap();
        return new Lattice(levels, numNodes);
    }

    /**
     * Builds the levels and map.
     * 
     * @return the int
     */
    private int buildLevelsAndMap() {

        // Init
        final int numQIs = maxLevels.length;
        int numNodes = 1;
        final int[] offsets = new int[numQIs];
        final int[] maxIndices = new int[numQIs];
        int maxLevel = 1;
        int id = 0;

        // Step 1
        for (int i = 0; i < numQIs; i++) {
            final int curMaxGeneralizationHeight = maxLevels[i] + 1;
            offsets[i] = numNodes;
            numNodes *= (curMaxGeneralizationHeight - minLevels[i]);
            maxLevel += (curMaxGeneralizationHeight - 1);
            maxIndices[i] = curMaxGeneralizationHeight - 1;
        }

        // Step 2
        final int[] levelsize = new int[maxLevel];
        final Node[] nodeArray = new Node[numNodes];
        for (int i = 0; i < nodeArray.length; i++) {
            nodeArray[i] = new Node(id++);
        }

        // Step 3
        for (int count = 0; count < numNodes; count++) {
            final int[] state = new int[numQIs];
            int tempCount = count;
            int level = 0;
            int numUpwards = 0;
            int numDownwards = 0;
            for (int i = state.length - 1; i >= 0; i--) {
                state[i] = (tempCount / offsets[i]) + minLevels[i];
                tempCount -= (state[i] - minLevels[i]) * offsets[i];
                level += (state[i]);

                if (state[i] < maxIndices[i]) {
                    numUpwards++;
                }

                if (state[i] != minLevels[i]) {
                    numDownwards++;
                }
            }
            final Node node = nodeArray[count];
            node.setTransformation(state, level);
            node.setPredecessors(new Node[numDownwards]);
            node.setSuccessors(new Node[numUpwards]);
            levelsize[level]++;
        }

        // Generate level arrays
        final Node[][] levels = new Node[maxLevel][];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = new Node[levelsize[i]];
        }

        // Generate up and down links and initialize levels
        for (int i = 0; i < nodeArray.length; i++) {
            final Node node = nodeArray[i];
            final int level = node.getLevel();
            --levelsize[node.getLevel()];
            final int index = (levels[level].length - 1 - levelsize[level]);
            levels[level][index] = node;
            final int[] key = node.getTransformation();
            for (int j = 0; j < key.length; j++) {
                if (key[j] < maxIndices[j]) {
                    final int plusIndex = i + offsets[j];
                    final Node reachableNode = nodeArray[plusIndex];
                    node.addSuccessor(reachableNode);
                    reachableNode.addPredecessor(node);
                }
            }
        }

        // Finalize
        this.levels = levels;
        return numNodes;
    }
}
