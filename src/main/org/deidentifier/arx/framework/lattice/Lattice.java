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
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.metric.InformationLoss;

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

    /** Tag trigger */
    private NodeAction     tagTrigger = null;

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
     * Sets the properties to the given node
     * 
     * @param node the node
     * @param result the result
     */
    public void setChecked(Node node, INodeChecker.Result result) {
        
        // Set checked
        setProperty(node, Node.PROPERTY_CHECKED);
        
        // Anonymous
        if (result.anonymous){
            setProperty(node, Node.PROPERTY_ANONYMOUS);
        } else {
            setProperty(node, Node.PROPERTY_NOT_ANONYMOUS);
        }

        // k-Anonymous
        if (result.kAnonymous){
            setProperty(node, Node.PROPERTY_K_ANONYMOUS);
        } else {
            setProperty(node, Node.PROPERTY_NOT_K_ANONYMOUS);
        }

        // Infoloss
        node.setInformationLoss(result.informationLoss);
    }

    /**
     * Sets the information loss
     * @param node
     * @param informationLoss
     */
    public void setInformationLoss(Node node, InformationLoss<?> informationLoss) {
        node.setInformationLoss(informationLoss);
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
     * Sets the property to the given node
     * 
     * @param node the node
     * @param property the property
     */
    public void setProperty(Node node, int property) {
        
        if (!node.hasProperty(property)) {
            node.setProperty(property);
            triggerTagged(node);
        }
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
     * When this trigger executed, a tagged event will be fired
     * @param trigger
     */
    public void setTagTrigger(NodeAction trigger){
        this.tagTrigger = trigger;
    }

    /**
     * Triggers a tagged event at the listener
     */
    private void triggerTagged(Node node) {
        if (this.listener != null && !node.hasProperty(Node.PROPERTY_EVENT_FIRED)){
            if (tagTrigger == null || tagTrigger.appliesTo(node)) {
                node.setProperty(Node.PROPERTY_EVENT_FIRED);
                this.listener.nodeTagged(size);
            }
        }
    }
}
