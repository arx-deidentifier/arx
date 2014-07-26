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

import java.util.Arrays;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * The Class Node.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Node {
    
    /** 
     * All privacy criteria are fulfilled
     */
    public static final int PROPERTY_ANONYMOUS = 0;
    
    /** 
     * Not all privacy criteria are fulfilled
     */
    public static final int PROPERTY_NOT_ANONYMOUS = 1;
    
    /** 
     * A k-anonymity sub-criterion is fulfilled
     */
    public static final int PROPERTY_K_ANONYMOUS = 2;
    
    /** 
     * A k-anonymity sub-criterion is not fulfilled
     */
    public static final int PROPERTY_NOT_K_ANONYMOUS = 3;
    
    /** 
     * The transformation results in insufficient utility
     */
    public static final int PROPERTY_INSUFFICIENT_UTILITY = 4;
    
    /** 
     * The transformation has been checked explicitly
     */
    public static final int PROPERTY_CHECKED = 5;
   
    /** 
     * A snapshot for this transformation must be created if it fits the size limits,
     * regardless of whether it triggers the storage condition
     */
    public static final int PROPERTY_FORCE_SNAPSHOT = 6;
    
    /** 
     * This node has already been visited during the second phase
     */
    public static final int PROPERTY_VISITED = 7;
    
    /** Debugging option*/
    public static final boolean DEBUG_PROPERTIES = false;

    /** The id. */
    public final int        id;

    /** Set of properties */
    private int             properties;

    /** The predecessors. */
    private Node[]          predecessors;

    /** The level. */
    private int             level;

    /** The information loss. */
    private InformationLoss<?> informationLoss;

    /** The transformation. */
    private int[]           transformation;

    /** The upwards. */
    private Node[]          successors;

    /** The down index. */
    private int             preIndex;

    /** The up index. */
    private int             sucIndex;
    
    /**
     * Instantiates a new node.
     */
    public Node(int id) {
        this.id = id;
        this.informationLoss = null;
        this.preIndex = 0;
        this.sucIndex = 0;
        this.properties = 0;
    }

    /**
     * Adds a predecessor
     * 
     * @param predecessor
     */
    public void addPredecessor(Node predecessor) {
        predecessors[preIndex++] = predecessor;
    }

    /**
     * Adds a successor
     * 
     * @param successor
     */
    public void addSuccessor(Node successor) {
        successors[sucIndex++] = successor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Node other = (Node) obj;
        if (!Arrays.equals(transformation, other.transformation)) { return false; }
        return true;
    }

    /**
     * Returns the information loss
     * 
     * @return
     */
    public InformationLoss<?> getInformationLoss() {
        return informationLoss;
    }

    /**
     * Returns the level
     * 
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the predecessors
     * 
     * @return
     */
    public Node[] getPredecessors() {
        return predecessors;
    }

    /**
     * Returns the successors
     * 
     * @return
     */
    public Node[] getSuccessors() {
        return successors;
    }

    /**
     * Returns the transformation
     * 
     * @return
     */
    public int[] getTransformation() {
        return transformation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(transformation);
        return result;
    }

    /**
     * Returns whether the node has the given property
     * @param property
     * @return
     */
    public boolean hasProperty(int property){
        return (properties & (1 << property)) != 0;
    }

    /**
     * Sets the information loss
     * 
     * @param informationLoss
     */
    public void setInformationLoss(final InformationLoss<?> informationLoss) {
        this.informationLoss = informationLoss;
    }

    
    /**
     * Sets the given property
     * @param property
     * @return
     */
    public void setProperty(int property){
        
        if (DEBUG_PROPERTIES) {
            int other = -1;
            switch(property){
                case Node.PROPERTY_ANONYMOUS:
                    other = Node.PROPERTY_NOT_ANONYMOUS;
                    break;
                case Node.PROPERTY_NOT_ANONYMOUS:
                    other = Node.PROPERTY_ANONYMOUS;
                    break;
                case Node.PROPERTY_K_ANONYMOUS:
                    other = Node.PROPERTY_NOT_K_ANONYMOUS;
                    break;
                case Node.PROPERTY_NOT_K_ANONYMOUS:
                    other = Node.PROPERTY_K_ANONYMOUS;
                    break;
            }
            if (hasProperty(other)) {
                throw new IllegalStateException("Inconsistent properties: "+property+" and "+other);
            }
        }
        
        properties |= (1 << property);
    }
    

    /**
     * Sets the properties to the given node
     * 
     * @param node the node
     * @param result the result
     */
    public void setChecked(INodeChecker.Result result) {
        
        // Set checked
        setProperty(Node.PROPERTY_CHECKED);
        
        // Anonymous
        if (result.anonymous){
            setProperty(Node.PROPERTY_ANONYMOUS);
        } else {
            setProperty(Node.PROPERTY_NOT_ANONYMOUS);
        }

        // k-Anonymous
        if (result.kAnonymous){
            setProperty(Node.PROPERTY_K_ANONYMOUS);
        } else {
            setProperty(Node.PROPERTY_NOT_K_ANONYMOUS);
        }

        // Infoloss
        if (informationLoss == null) {
            informationLoss = result.informationLoss;
        }
    }

    /**
     * Sets the predecessors
     * 
     * @param nodes
     */
    protected void setPredecessors(Node[] nodes) {
        predecessors = nodes;
    }

    /**
     * Sets the successors
     * 
     * @param nodes
     */
    protected void setSuccessors(Node[] nodes) {
        successors = nodes;
    }

    /**
     * Sets the transformation
     * 
     * @param transformation
     */
    public void setTransformation(int[] transformation, int level) {
        this.transformation = transformation;
        this.level = level;
    }
}
