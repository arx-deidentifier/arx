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

import java.util.Arrays;

import org.deidentifier.flash.metric.InformationLoss;

/**
 * The Class Node.
 * 
 * @author Prasser, Kohlmayer
 */
public class Node {

    /** The anonymous. */
    private boolean         anonymous;

    /** The downwards. */
    private Node[]          predecessors;

    /** The id. */
    public final int        id;

    /** The level. */
    private int             level;

    /** The information loss. */
    private InformationLoss informationLoss;

    /** The transformation. */
    private int[]           transformation;

    /** The tagged. */
    private boolean         tagged;

    /** Indicates if the node has been explicitly checked by the algorithm. */
    private boolean         checked;

    /** The upwards. */
    private Node[]          successors;

    /** The down index. */
    private int             preIndex;

    /** The up index. */
    private int             sucIndex;

    /** K anonymous */
    private boolean         kAnonymous;

    /**
     * Instantiates a new node.
     */
    public Node(final IDGenerator generator) {
        id = generator.get();
        anonymous = false;
        tagged = false;
        informationLoss = null;
        preIndex = 0;
        sucIndex = 0;
        checked = false;
    }

    /**
     * Adds a predecessor
     * 
     * @param predecessor
     */
    public void addPredecessor(final Node predecessor) {
        predecessors[preIndex++] = predecessor;
    }

    /**
     * Adds a successor
     * 
     * @param successor
     */
    public void addSuccessor(final Node successor) {
        successors[sucIndex++] = successor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
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
    public InformationLoss getInformationLoss() {
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
     * Returns the state
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
     * Is it anonymous
     * 
     * @return
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * @return the checked
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Is it k-anonymous
     * 
     * @return
     */
    public boolean isKAnonymous() {
        return kAnonymous;
    }

    /**
     * Is it tagged
     * 
     * @return
     */
    public boolean isTagged() {
        return tagged;
    }

    /**
     * Marks as anonymous
     * 
     * @param anonymous2
     */
    public void setAnonymous(final boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * Marks as checked
     */
    public void setChecked() {
        checked = true;
    }

    /**
     * Sets the information loss
     * 
     * @param informationLoss
     */
    public void setInformationLoss(final InformationLoss informationLoss) {
        this.informationLoss = informationLoss;
    }

    /**
     * Is this transformation kAnonymous
     * 
     * @param kAnonymous
     */
    public void setKAnonymous(final boolean kAnonymous) {
        this.kAnonymous = kAnonymous;
    }

    /**
     * Sets a node not tagged
     */
    public void setNotTagged() {
        tagged = false;
    }

    /**
     * Sets the predecessors
     * 
     * @param nodes
     */
    public void setPredecessors(final Node[] nodes) {
        predecessors = nodes;
    }

    /**
     * Sets the successors
     * 
     * @param nodes
     */
    public void setSuccessors(final Node[] nodes) {
        successors = nodes;
    }

    /**
     * Marks as tagged
     */
    public void setTagged() {
        tagged = true;
    }

    /**
     * Sets the transformation
     * 
     * @param transformation
     */
    public void setTransformation(final int[] transformation, final int level) {
        this.transformation = transformation;
        this.level = level;
    }

}
