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

import java.util.Iterator;

import org.deidentifier.arx.ARXListener;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public class SolutionSpace {

    /** Static property */
    private final PredictiveProperty  propertySuccessorsPruned    = new PredictiveProperty("Successors pruned",
                                                                                           Direction.UP); // TODO: Was NONE?
    /** Static property */
    private final PredictiveProperty  propertyInsufficientUtility = new PredictiveProperty("Insufficient utility",
                                                                                           Direction.UP);
    /** Static property */
    private final PredictiveProperty  propertyForceSnapshot       = new PredictiveProperty("Force snapshot",
                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty  propertyChecked             = new PredictiveProperty("Checked",
                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty  propertyVisited             = new PredictiveProperty("Visited",
                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty  propertyKAnonymous          = new PredictiveProperty("K-Anonymous",
                                                                                           Direction.UP);
    /** Static property */
    private final PredictiveProperty  propertyNotKAnonymous       = new PredictiveProperty("Not k-anonymous",
                                                                                           Direction.DOWN);
    /** Potentially changing property */
    private PredictiveProperty        propertyAnonymous           = new PredictiveProperty("Anonymous",
                                                                                           Direction.NONE);
    /** Potentially changing property */
    private PredictiveProperty        propertyNotAnonymous        = new PredictiveProperty("Not anonymous",
                                                                                           Direction.NONE);

    private Lattice<Integer, Integer> lattice;
    private int[]                     offsets;
    private int offset; // level

    public SolutionSpace() {
        
    }

    public SolutionSpace(int[] hierarchiesMaxLevels, int[] hierarchiesMinLevels) {
        // TODO Auto-generated constructor stub
    }

    /**
     * Determines whether a parent-child relationship exists.
     * @param parent
     * @param child
     * @return
     */
    public boolean isParentChild(int[] parent, int[] child) {
        int diff = 0;
        for (int i=0; i<parent.length; i++) {
            if (parent[i] < child[i]) {
                return false;
            } else {
                diff += parent[i] - child[i];
            }
        }
        return diff != 0;
    }

    /**
     * Determines whether a direct parent-child relationship exists.
     * @param parent
     * @param child
     * @return
     */
    public boolean isDirectParentChild(int[] parent, int[] child) {
        int diff = 0;
        for (int i=0; i<parent.length; i++) {
            if (parent[i] < child[i]) {
                return false;
            } else {
                diff += parent[i] - child[i];
            }
        }
        return diff == 1;
    }
    
    /**
     * Determines whether a parent-child relationship exists, or both are equal
     * @param parent
     * @param child
     * @return
     */
    public boolean isParentChildOrEqual(int[] parent, int[] child) {
        for (int i=0; i<parent.length; i++) {
            if (parent[i] < child[i]) {
                return false;
            } 
        }
        return true;
    }
    
    /**
     * Returns whether a node has a given property
     * @param transformation
     * @param property
     * @return
     */
    public boolean hasProperty(int[] transformation, PredictiveProperty property) {
        return lattice.hasProperty(toJHPL(transformation), property);
    }

    /**
     * Internal method that subtracts the offsets
     * @param transformation
     * @return
     */
    private int[] toJHPL(int[] transformation) {
        int[] result = transformation.clone();
        for (int i=0; i<result.length; i++) {
            result[i]-=offsets[i];
        }
        return result;
    }

    /**
     * Internal method that adds the offsets
     * @param transformation
     * @return
     */
    private int[] fromJHPL(int[] transformation) {
        int[] result = transformation.clone();
        for (int i=0; i<result.length; i++) {
            result[i]+=offsets[i];
        }
        return result;
    }
    

    /**
     * Internal method that subtracts the offset
     * @param level
     * @return
     */
    private int toJHPL(int level) {
        return level - offset;
    }

    /**
     * Internal method that adds the offset
     * @param level
     * @return
     */
    private int fromJHPL(int level) {
        return level + offset;
    }
    
    /**
     * Returns the level of the given transformation
     * @param transformation
     * @return
     */
    public int getLevel(int[] transformation) {
        int level = 0;
        for (int dimension : transformation) {
            level += dimension;
        }
        return level;
    }

    /**
     * Returns a wrapper object with access to all properties about the transformation
     * @param transformation
     * @return
     */
    public Transformation getTransformation(int[] transformation) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setListener(ARXListener listener) {
        this.listener = listener;
    }

    public Transformation getTop() {
        return getTransformation(fromJHPL(lattice.nodes().getTop()));
    }

    public Transformation getBottom() {
        return getTransformation(fromJHPL(lattice.nodes().getBottom()));
    }

    public long getSize() {
        return lattice.numNodes();
    }

    public Transformation getTransformation(long transformation) {
        return getTransformation(fromJHPL(lattice.space().toIndex(transformation)));
    }

    /**
     * When this trigger executed, a tagged event will be fired.
     *
     * @param trigger
     */
    public void setTagTrigger(NodeAction trigger){
        this.trigger = trigger;
    }

    private NodeAction trigger;
    private ARXListener listener;
    
    /**
     * Triggers a tagged event at the listener.
     *
     * @param transformation
     */
    private void triggerTagged(Transformation transformation) {
//        if (this.listener != null && !transformation.hasProperty(Transformation.PROPERTY_EVENT_FIRED)){
//            if (trigger == null || trigger.appliesTo(transformation)) {
//                transformation.setProperty(Transformation.PROPERTY_EVENT_FIRED);
//                this.listener.nodeTagged(size);
//            }
//        }
    }

    public Iterator<Long> getSuccessors(long transformation) {
        return lattice.space().indexIteratorToIdIterator(lattice.nodes().listSuccessors(lattice.space().toIndex(transformation)));
    }

    public Iterator<Long> unsafeGetLevel(int level) {
        return lattice.space().indexIteratorToIdIterator(lattice.unsafe().listAllNodes(toJHPL(level)));
    }
    
    /**
     * Makes the anonymity property predictable
     * @param predictable
     */
    public void setAnonymityPropertyPredictable(boolean predictable) {
        if (predictable) {
            propertyAnonymous = new PredictiveProperty("Anonymous", Direction.UP);
            propertyNotAnonymous = new PredictiveProperty("Not anonymous", Direction.DOWN);
        } else {
            propertyAnonymous = new PredictiveProperty("Anonymous", Direction.NONE);
            propertyNotAnonymous = new PredictiveProperty("Not anonymous", Direction.NONE);
        }
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertySuccessorsPruned() {
        return propertySuccessorsPruned;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyInsufficientUtility() {
        return propertyInsufficientUtility;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyForceSnapshot() {
        return propertyForceSnapshot;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyChecked() {
        return propertyChecked;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyKAnonymous() {
        return propertyKAnonymous;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyNotKAnonymous() {
        return propertyNotKAnonymous;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyAnonymous() {
        return propertyAnonymous;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyNotAnonymous() {
        return propertyNotAnonymous;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyVisited() {
        return propertyVisited;
    }
}
