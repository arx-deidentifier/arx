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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.metric.InformationLoss;

import com.carrotsearch.hppc.LongObjectOpenHashMap;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public class SolutionSpace {

    /** Information loss */
    private LongObjectOpenHashMap<Object>             data                        = new LongObjectOpenHashMap<Object>();
    /** The backing JHPL lattice */
    private final Lattice<Integer, Integer>           lattice;
    /** Listener */
    private ARXListener                               listener;
    /** Information loss */
    private LongObjectOpenHashMap<InformationLoss<?>> lowerBound                  = new LongObjectOpenHashMap<InformationLoss<?>>();
    /** The offsets for indices */
    private final int[]                               offsetIndices;
    /** The offset the level */
    private final int                                 offsetLevel;
    /** Potentially changing property */
    private PredictiveProperty                        propertyAnonymous           = new PredictiveProperty("Anonymous",
                                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty                  propertyChecked             = new PredictiveProperty("Checked",
                                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty                  propertyForceSnapshot       = new PredictiveProperty("Force snapshot",
                                                                                                           Direction.NONE);
    /** Static property */
    private final PredictiveProperty                  propertyInsufficientUtility = new PredictiveProperty("Insufficient utility",
                                                                                                           Direction.UP);
    /** Static property */
    private final PredictiveProperty                  propertyKAnonymous          = new PredictiveProperty("K-Anonymous",
                                                                                                           Direction.UP);
    /** Potentially changing property */
    private PredictiveProperty                        propertyNotAnonymous        = new PredictiveProperty("Not anonymous",
                                                                                                           Direction.NONE);

    /** Static property */
    private final PredictiveProperty                  propertyNotKAnonymous       = new PredictiveProperty("Not k-anonymous",
                                                                                                           Direction.DOWN);
    /** Static property */
    private final PredictiveProperty                  propertySuccessorsPruned    = new PredictiveProperty("Successors pruned",
                                                                                                           Direction.UP); // TODO: Was NONE?

    /** Static property */
    private final PredictiveProperty                  propertyVisited             = new PredictiveProperty("Visited",
                                                                                                           Direction.NONE);

    /** Trigger that fires a change event */
    private NodeAction                                trigger;

    /** Information loss */
    private LongObjectOpenHashMap<InformationLoss<?>> utility                     = new LongObjectOpenHashMap<InformationLoss<?>>();

    /**
     * For de-serialization
     * @param lattice
     * @param config
     */
    public SolutionSpace(ARXLattice lattice, ARXConfiguration config) {
        this(lattice.getBottom().getTransformation(), lattice.getTop().getTransformation());
        setMonotonicity(config);
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                int[] index = toJHPL(node.getTransformation());
                long id = this.lattice.space().toId(index);
                if (node.getAnonymity() == Anonymity.ANONYMOUS) {
                    this.lattice.putProperty(index, this.getPropertyAnonymous());
                } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                    this.lattice.putProperty(index, this.getPropertyNotAnonymous());
                }
                if (node.isChecked()) {
                    this.lattice.putProperty(index, this.getPropertyChecked());
                    this.setInformationLoss(id, node.getMaximumInformationLoss());
                }
            }
        }
    }

    /**
     * Creates a new solution space
     * @param minLevels
     * @param maxLevels
     */
    public SolutionSpace(int[] minLevels, int[] maxLevels) {
        
        // Create offsets
        this.offsetIndices = minLevels.clone();
        int lvl = 0; for (int i : offsetIndices) lvl+=i;
        this.offsetLevel = lvl;
        
        
        // Create lattice
        Integer[][] elements = new Integer[minLevels.length][];
        for (int i = 0; i < elements.length; i++) {
            Integer[] element = new Integer[maxLevels[i] - minLevels[i] + 1];
            int idx = 0;
            for (int j = minLevels[i]; j <= maxLevels[i]; j++) {
                element[idx++] = j;
            }
            elements[i] = element;
        }
        this.lattice = new Lattice<Integer, Integer>(elements);
    }
    
    /**
     * Returns the bottom transformation
     * @return
     */
    public Transformation getBottom() {
        return getTransformation(fromJHPL(lattice.nodes().getBottom()));
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
     * Returns all materialized transformations
     * @return
     */
    public Iterator<Long> getMaterializedTransformations() {
        return lattice.space().indexIteratorToIdIterator(lattice.listNodes());
    }

    /**
     * Returns all predeccessors of the transformation with the given identifier
     * @param transformation
     * @return
     */
    public Iterator<Long> getPredecessors(long transformation) {
        return lattice.space().indexIteratorToIdIterator(lattice.nodes().listPredecessors(lattice.space().toIndex(transformation)));
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
    public PredictiveProperty getPropertyChecked() {
        return propertyChecked;
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
    public PredictiveProperty getPropertyInsufficientUtility() {
        return propertyInsufficientUtility;
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
    public PredictiveProperty getPropertyNotAnonymous() {
        return propertyNotAnonymous;
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
    public PredictiveProperty getPropertySuccessorsPruned() {
        return propertySuccessorsPruned;
    }

    /**
     * Returns a property
     * @return
     */
    public PredictiveProperty getPropertyVisited() {
        return propertyVisited;
    }

    /**
     * Returns the number of transformations for which information has been materialized in the space
     * @return
     */
    public long getSize() {
        return lattice.numNodes();
    }

    /**
     * Returns all successors of the transformation with the given identifier
     * @param identifier
     * @return
     */
    public Iterator<Long> getSuccessors(long identifier) {
        return lattice.space().indexIteratorToIdIterator(lattice.nodes().listSuccessors(lattice.space().toIndex(identifier)));
    }

    /**
     * Returns the top-transformation
     * @return
     */
    public Transformation getTop() {
        return getTransformation(fromJHPL(lattice.nodes().getTop()));
    }

    /**
     * Returns a wrapper object with access to all properties about the transformation
     * @param transformation
     * @return
     */
    public Transformation getTransformation(int[] transformation) {
        return new Transformation(transformation, lattice, this);
    }
    
    /**
     * Returns the transformation with the given identifier
     * @param identifier
     * @return
     */
    public Transformation getTransformation(long identifier) {
        return getTransformation(fromJHPL(lattice.space().toIndex(identifier)));
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
     * Sets a listener
     * @param listener
     */
    public void setListener(ARXListener listener) {
        this.listener = listener;
    }

    /**
     * When this trigger executed, a tagged event will be fired.
     *
     * @param trigger
     */
    public void setListenerTrigger(NodeAction trigger){
        this.trigger = trigger;
//      if (this.listener != null && !transformation.hasProperty(Transformation.PROPERTY_EVENT_FIRED)){
//      if (trigger == null || trigger.appliesTo(transformation)) {
//          transformation.setProperty(Transformation.PROPERTY_EVENT_FIRED);
//          this.listener.nodeTagged(size);
//      }
//  }
    }

    /**
     * Returns *all* nodes on the given level. This is an unsafe operation that only performs well for "small" spaces.
     * @param level
     * @return
     */
    public Iterator<Long> unsafeGetLevel(int level) {
        return lattice.space().indexIteratorToIdIterator(lattice.unsafe().listAllNodes(toJHPL(level)));
    }

    /**
     * Sets the monotonicity of the anonymity property
     * @param config
     */
    private void setMonotonicity(ARXConfiguration config) {

        // Determine whether the overall set of criteria is monotonic
        for (PrivacyCriterion criterion : config.getCriteria()) {
            if (!(criterion.isMonotonic() || (config.getMaxOutliers() == 0d) || config.isPracticalMonotonicity())) {
                setAnonymityPropertyPredictable(false);
            }
        }
        setAnonymityPropertyPredictable(true);
    }

    /**
     * Internal method that adds the offsets
     * @param transformation
     * @return
     */
    protected int[] fromJHPL(int[] transformation) {
        int[] result = transformation.clone();
        for (int i=0; i<result.length; i++) {
            result[i]+=offsetIndices[i];
        }
        return result;
    }

    /**
     * Returns data
     * @param id
     * @return
     */
    protected Object getData(long id) {
        return data.getOrDefault(id, null);
    }

    /**
     * Returns the information loss
     * @param identifier
     * @return
     */
    protected InformationLoss<?> getInformationLoss(long identifier) {
        return utility.getOrDefault(identifier, null);
    }

    /**
     * Returns the lower bound
     * @param identifier
     * @return
     */
    protected InformationLoss<?> getLowerBound(long identifier) {
        return lowerBound.getOrDefault(identifier, null);
    }

    /**
     * Sets data
     * @param id
     * @param object
     */
    protected void setData(long id, Object object) {
        data.put(id, object);
    }
    
    /**
     * Sets the information loss
     * @param identifier
     * @param loss
     */
    protected void setInformationLoss(long identifier, InformationLoss<?> loss) {
        utility.put(identifier, loss);
    }
    

    /**
     * Sets the lower bound
     * @param identifier
     * @param loss
     */
    protected void setLowerBound(long identifier, InformationLoss<?> loss) {
        lowerBound.put(identifier, loss);
    }
    
    /**
     * Internal method that subtracts the offset
     * @param level
     * @return
     */
    protected int toJHPL(int level) {
        return level - offsetLevel;
    }

    /**
     * Internal method that subtracts the offsets
     * @param transformation
     * @return
     */
    protected int[] toJHPL(int[] transformation) {
        int[] result = transformation.clone();
        for (int i=0; i<result.length; i++) {
            result[i]-=offsetIndices[i];
        }
        return result;
    }
}
