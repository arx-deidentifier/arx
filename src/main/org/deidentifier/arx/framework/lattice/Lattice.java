/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.framework.lattice;

import org.deidentifier.arx.ARXListener;

/**
 * The class Lattice.
 * 
 * @author Prasser, Kohlmayer
 */
public class Lattice {

    /** The levels. */
    private Node[][]    levels        = null;

    /** The levelsize. */
    private int[]       untaggedCount = null;

    /** The max states. */
    private final int[] maxLevels;

    /** The size. */
    private int         size          = 0;

    /** A listener */
    private ARXListener listener      = null;

    /** A multiplier for the listener*/
    private int         multiplier    = 1;

    /**
     * Initializes a lattice.
     * 
     * @param levels
     *            the levels
     * @param nodesMap
     *            the nodes map
     * @param maxLevels
     *            the max levels
     * @param numNodes
     *            the num nodes
     */
    public Lattice(final Node[][] levels, final int[] maxLevels, final int numNodes) {

        this.maxLevels = maxLevels;
        this.levels = levels;
        size = numNodes;

        untaggedCount = new int[levels.length];
        for (int i = 0; i < levels.length; i++) {
            untaggedCount[i] = levels[i].length;
        }
    }

    /**
     * Clears all tags
     */
    public void clearTags() {
        for (int i = 0; i < levels.length; i++) {
            untaggedCount[i] = levels[i].length;
            for (final Node n : levels[i]) {
                n.setNotTagged();
            }
        }
    }

    /**
     * Does the tagging.
     * 
     * @param node
     *            the node
     * @param anonymous
     *            the anonymous
     */
    private void doTagAnonymous(final Node node, final boolean anonymous, final boolean kAnonymous) {

        // Tag
        node.setTagged();
        node.setAnonymous(anonymous);
        node.setKAnonymous(kAnonymous);


        // Count
        untaggedCount[node.getLevel()]--;

        // Call listener
        if (listener != null) {
            listener.nodeTagged(size * multiplier);
        }

        // Traverse
        if (anonymous) {
            for (final Node up : node.getSuccessors()) {
                if (!up.isTagged()) {
                    doTagAnonymous(up, anonymous, kAnonymous);
                }
            }
        } else {
            for (final Node down : node.getPredecessors()) {
                if (!down.isTagged()) {
                    doTagAnonymous(down, anonymous, kAnonymous);
                }
            }
        }
    }

    /**
     * Does the tagging.
     * 
     * @param node
     *            the node
     * @param anonymous
     *            the anonymous
     */
    private void doTagKAnonymous(final Node node, final boolean kAnonymous) {

        // Tag
        node.setTagged();
        node.setKAnonymous(kAnonymous);

        // Count
        untaggedCount[node.getLevel()]--;

        // Traverse
        if (kAnonymous) {
            for (final Node up : node.getSuccessors()) {
                if (!up.isTagged()) {
                    doTagKAnonymous(up, kAnonymous);
                }
            }
        } else {
            for (final Node down : node.getPredecessors()) {
                if (!down.isTagged()) {
                    doTagKAnonymous(down, kAnonymous);
                }
            }
        }
    }

    /**
     * Tag all successor nodes.
     * 
     * @param node
     *            the node
     */
    public void doTagUpwards(final Node node) {
        // Tag
        node.setTagged();
        for (final Node up : node.getSuccessors()) {
            if (!up.isTagged()) {
                doTagUpwards(up);
            }
        }

    }

    public void doUnTagUpwards(final Node node) {
        // UnTag
        node.setNotTagged();
        untaggedCount[node.getLevel()]++;

        for (final Node up : node.getSuccessors()) {
            if (up.isTagged()) {
                doUnTagUpwards(up);
            }
        }
    }

    /**
     * Returns all levels in the lattice
     * 
     * @return
     */
    public Node[][] getLevels() {
        return levels;
    }

    /**
     * Returns the maximal levels for each quasi identifier
     * 
     * @return
     */
    public int[] getMaximumGeneralizationLevels() {
        return maxLevels;
    }

    /**
     * Returns the number of nodes in the lattice
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Return the number of untagged nodes on the given lattice
     * 
     * @param level
     * @return
     */
    public int getUntaggedCount(final int level) {
        return untaggedCount[level];
    }

    /**
     * Decrement the counter for the number of untagged nodes on the given level
     * @param level
     * @return
     */
    public void decUntaggedCount(final int level) {
        untaggedCount[level]--;
    }

    /**
     * Attaches a listener
     * 
     * @param listener
     */
    public void setListener(final ARXListener listener) {
        this.listener = listener;
    }

    /**
     * Tag nodes.
     * 
     * @param node
     *            the node
     * @param anonymous
     *            the anonymous
     */
    public void tagAnonymous(final Node node, final boolean anonymous) {
        if (!node.isTagged()) {
            doTagAnonymous(node, anonymous, node.isKAnonymous());
        }
    }

    /**
     * Tag nodes.
     * 
     * @param node
     *            the node
     * @param anonymous
     *            the anonymous
     */
    public void tagKAnonymous(final Node node, final boolean kAnonymous) {
        if (!node.isTagged()) {
            doTagKAnonymous(node, kAnonymous);
        }
    }

    /**
     * Sets a multiplier, which is used to multiply the number of nodes in the lattice when calling a
     * listener. Needed to return the correct progress information when anonymizing with multiple
     * sensitive attributes
     * @param multiplier
     */
    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Triggers a tagged event at the listener
     */
    public void triggerTagged() {
        if (this.listener != null) this.listener.nodeTagged(size * multiplier);
    }

  
}
