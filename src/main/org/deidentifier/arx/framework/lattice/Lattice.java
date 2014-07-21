/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

    /** The number of untagged nodes on each level */
    private final int[][]  untagged;

    /** The maximal transformation levels for each attribute */
    private final int[]    maxLevels;

    /** The size */
    private final int      size;

    /** A listener */
    private ARXListener   listener = null;

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
        this.untagged = new int[Node.NUM_PROPERTIES][levels.length];
        for (int i = 0; i < untagged.length; i++) {
            this.untagged[i] = new int[levels.length];
            for (int j = 0; j < levels.length; j++) {
                this.untagged[i][j] = levels[j].length;
            }
        }
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
     * Return the number of nodes for which the given property has not been set on the given level
     * 
     * @param property
     * @param level
     * @return
     */
    public int getUnsetPropertyCount(int property, int level) {
        return untagged[property][level];
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
        
        if (include && !node.hasProperty(property)) {
            node.setProperty(property);
            untagged[property][node.getLevel()]--;
            triggerTagged();
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
        
        if (include && !node.hasProperty(property)) {
            node.setProperty(property);
            untagged[property][node.getLevel()]--;
            triggerTagged();
        }

        for (final Node up : node.getSuccessors()) {
            if (!up.hasProperty(property)) {
                setPropertyUpwards(up, true, property);
            }
        }
    }

    /**
     * Triggers a tagged event at the listener
     */
    private void triggerTagged() {
        if (this.listener != null) this.listener.nodeTagged(size);
    }
}
