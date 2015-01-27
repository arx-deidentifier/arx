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

    /** The size. */
    private final int      size;

    /** A listener. */
    private ARXListener    listener = null;

    /** Tag trigger. */
    private NodeAction     tagTrigger = null;

    /**
     * Initializes a lattice.
     *
     * @param levels the levels
     * @param maxLevels the max levels
     */
    public Lattice(final Node[][] levels, final int numNodes) {

        this.levels = levels;
        this.size = numNodes;
    }

    /**
     * Returns the bottom node.
     *
     * @return
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
     * Returns all levels in the lattice.
     *
     * @return
     */
    public Node[][] getLevels() {
        return levels;
    }

    /**
     * Returns the number of nodes in the lattice.
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the top node.
     *
     * @return
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
     * Sets the properties to the given node.
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
        node.setLowerBound(result.lowerBound);
    }

    /**
     * Sets the information loss.
     *
     * @param node
     * @param informationLoss
     */
    public void setInformationLoss(Node node, InformationLoss<?> informationLoss) {
        node.setInformationLoss(informationLoss);
    }

    /**
     * Sets the lower bound.
     *
     * @param node
     * @param lowerBound
     */
    public void setLowerBound(Node node, InformationLoss<?> lowerBound) {
        node.setLowerBound(lowerBound);
    }

    /**
     * Attaches a listener.
     *
     * @param listener
     */
    public void setListener(final ARXListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the property to the given node.
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
     * Sets the property to all predecessors of the given node.
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
     * Sets the property to all successors of the given node.
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
     * When this trigger executed, a tagged event will be fired.
     *
     * @param trigger
     */
    public void setTagTrigger(NodeAction trigger){
        this.tagTrigger = trigger;
    }

    /**
     * Triggers a tagged event at the listener.
     *
     * @param node
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
