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

package org.deidentifier.flash.framework.lattice;

/**
 * The class LatticeBuilder.
 * 
 * @author Prasser, Kohlmayer
 */
public class LatticeBuilder {

    /** The levels. */
    private Node[][] levels     = null;

    /** The maxlevels. */
    private int[]    maxLevels  = null;

    /** The minlevels */
    private int[]    minLevels  = null;

    /** The maxheights */
    private int[]    maxHeights = null;

    /**
     * Instantiates a new lattice builder.
     * 
     * @param maxLevels
     *            the maxlevels
     * @param minLevels
     *            the minlevels
     */
    public LatticeBuilder(final int[] maxLevels,
                          final int[] minLevels,
                          final int[] maxHeights) {
        this.maxLevels = maxLevels;
        this.minLevels = minLevels;
        this.maxHeights = maxHeights;
    }

    /**
     * Builds the.
     * 
     * @return the lattice
     */
    public Lattice build() {
        final int numNodes = buildLevelsAndMap();
        return new Lattice(levels, maxHeights, numNodes);
    }

    /**
     * Builds the levels and map.
     * 
     * @return the int
     */
    private int buildLevelsAndMap() {

        final IDGenerator generator = new IDGenerator();

        // Init
        final int numQIs = maxLevels.length;
        int numNodes = 1;
        final int[] offsets = new int[numQIs];
        final int[] maxIndices = new int[numQIs];
        int maxLevel = 1;

        // Step 1
        for (int i = 0; i < numQIs; i++) {
            // curlelemsize needs to be maxheight
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
            nodeArray[i] = new Node(generator);
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
