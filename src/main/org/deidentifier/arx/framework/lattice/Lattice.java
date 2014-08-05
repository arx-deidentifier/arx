/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Lattice {

    /** The levels. */
    private final Node[][] levels;

    /** The maximal transformation levels for each attribute */
    private final int[]    maxLevels;

    /** The size */
    private final int      size;

    /** A listener */
    private ARXListener    listener = null;

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
        this.size = numNodes;
    }

    /**
     * Returns the bottom node
     */
    public Node getBottom() {
        for (int i = 0; i<levels.length; i++) {
            if (levels[i].length==1){
                return levels[i][0];
            } else if (levels[i].length > 1) { 
                throw new RuntimeException("Multiple bottom nodes!"); 
            }
        }
        throw new RuntimeException("Empty lattice!");
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
     * Returns the top node
     */
    public Node getTop() {
        for (int i = levels.length - 1; i>=0; i--) {
            if (levels[i].length==1){
                return levels[i][0];
            } else if (levels[i].length > 1) { 
                throw new RuntimeException("Multiple top nodes!"); 
            }
        }
        throw new RuntimeException("Empty lattice!");
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
     * Sets the property to all predecessors of the given node
     * 
     * @param node the node
     * @param include should the property also be set for the starting node
     * @param property the property
     */
    public void setPropertyDownwards(Node node, boolean include, int property) {
        
        if (include) {
            setProperty(node, property);
        }

        for (final Node down : node.getPredecessors()) {
            if (!down.hasProperty(property)) {
                setPropertyDownwards(down, true, property);
            }
        }
    }

    /**
     * Sets the property to all successors of the given node
     * 
     * @param node the node
     * @param include should the property also be set for the starting node
     * @param property the property
     */
    public void setPropertyUpwards(Node node, boolean include, int property) {

        if (include) {
            setProperty(node, property);
        }
        
        for (final Node up : node.getSuccessors()) {
            if (!up.hasProperty(property)) {
                setPropertyUpwards(up, true, property);
            }
        }
    }

    /**
     * Sets the property to the given node
     * 
     * @param node the node
     * @param property the property
     */
    private void setProperty(Node node, int property) {
        
        if (!node.hasProperty(property)) {
            node.setProperty(property);
            triggerTagged();
        }
    }

    /**
     * Triggers a tagged event at the listener
     */
    private void triggerTagged() {
        if (this.listener != null) this.listener.nodeTagged(size);
    }
}
