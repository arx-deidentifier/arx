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

import org.deidentifier.flash.FLASHListener;

/**
 * The class Lattice.
 * 
 * @author Prasser, Kohlmayer
 */
public class Lattice {

    /** The levels. */
    private Node[][]      levels        = null;

    /** The levelsize. */
    public int[]          untaggedCount = null;

    /** The max states. */
    private final int[]   maxLevels;

    /** The size. */
    private int           size          = 0;

    /** A listener */
    private FLASHListener listener      = null;

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
    public Lattice(final Node[][] levels,
                   final int[] maxLevels,
                   final int numNodes) {

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
    private void doTagAnonymous(final Node node, final boolean anonymous) {

        // Tag
        node.setTagged();
        node.setAnonymous(anonymous);

        // Count
        untaggedCount[node.getLevel()]--;

        // Call listener
        if (listener != null) {
            listener.nodeTagged(size);
        }

        // Traverse
        if (anonymous) {
            for (final Node up : node.getSuccessors()) {
                if (!up.isTagged()) {
                    doTagAnonymous(up, anonymous);
                }
            }
        } else {
            for (final Node down : node.getPredecessors()) {
                if (!down.isTagged()) {
                    doTagAnonymous(down, anonymous);
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

        // Call listener
        if (listener != null) {
            listener.nodeTagged(size);
        }

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
     * Returns the maximal levels for each qi
     * 
     * @return
     */
    public int[] getMaxLevels() {
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
     * Attaches a listener
     * 
     * @param listener
     */
    public void setListener(final FLASHListener listener) {
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
            doTagAnonymous(node, anonymous);
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
}
