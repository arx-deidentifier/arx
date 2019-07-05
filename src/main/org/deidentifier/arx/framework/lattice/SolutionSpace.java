/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.math.BigInteger;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.Monotonicity;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.metric.InformationLoss;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.LatticeHighdimensional;
import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public abstract class SolutionSpace<T> {

    /**
     * Creates a new solution space
     * @param lattice
     * @param config
     */
    public static SolutionSpace<?> create(ARXLattice lattice, ARXConfiguration config) {
        
        // Choose implementation based on virtual size
        if (getSize(lattice.getBottom().getTransformation(), lattice.getTop().getTransformation()).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return new SolutionSpaceLong(lattice, config);
        } else {
            return new SolutionSpaceIntArray(lattice, config);
        }
    }
    /**
     * Creates a new solution space
     * @param hierarchiesMinLevels
     * @param hierarchiesMaxLevels
     * @return
     */
    public static SolutionSpace<?> create(int[] hierarchiesMinLevels, int[] hierarchiesMaxLevels) {

        // Choose implementation based on virtual size
        if (getSize(hierarchiesMinLevels, hierarchiesMaxLevels).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return new SolutionSpaceLong(hierarchiesMinLevels, hierarchiesMaxLevels);
        } else {
            return new SolutionSpaceIntArray(hierarchiesMinLevels, hierarchiesMaxLevels);
        }
    }
    
    /**
     * Returns the virtual size
     * @param hierarchiesMinLevels
     * @param hierarchiesMaxLevels
     * @return
     */
    private static BigInteger getSize(int[] hierarchiesMinLevels, int[] hierarchiesMaxLevels) {
        BigInteger size = BigInteger.valueOf(1);
        for (int i = 0; i < hierarchiesMinLevels.length; i++) {
            size = size.multiply(BigInteger.valueOf(hierarchiesMaxLevels[i] - hierarchiesMinLevels[i] + 1));
        }
        return size;
    }
    
    /** Potentially changing property */
    private PredictiveProperty                                    propertyAnonymous           = new PredictiveProperty("Anonymous",
                                                                                                                       Direction.NONE);
    /** Static property */
    private final PredictiveProperty                              propertyChecked             = new PredictiveProperty("Checked",
                                                                                                                       Direction.NONE);
    /** Static property */
    private final PredictiveProperty                              propertyForceSnapshot       = new PredictiveProperty("Force snapshot",
                                                                                                                       Direction.NONE);
    /** Static property */
    private final PredictiveProperty                              propertyInsufficientUtility = new PredictiveProperty("Insufficient utility",
                                                                                                                       Direction.UP);
    /** Static property */
    private final PredictiveProperty                              propertyKAnonymous          = new PredictiveProperty("K-Anonymous",
                                                                                                                       Direction.UP);
    /** Potentially changing property */
    private PredictiveProperty                                    propertyNotAnonymous        = new PredictiveProperty("Not anonymous",
                                                                                                                       Direction.NONE);
    /** Static property */
    private final PredictiveProperty                              propertyNotKAnonymous       = new PredictiveProperty("Not k-anonymous",
                                                                                                                       Direction.DOWN);

    /** Static property */
    private final PredictiveProperty                              propertySuccessorsPruned    = new PredictiveProperty("Successors pruned",
                                                                                                                       Direction.UP);         // TODO: Was NONE?
    /** Static property */
    private final PredictiveProperty                              propertyVisited             = new PredictiveProperty("Visited",
                                                                                                                       Direction.NONE);

    /** Static property */
    private final PredictiveProperty                              propertyExpanded            = new PredictiveProperty("Expanded",
                                                                                                                       Direction.NONE);

    /** The offsets for indices */
    private final int[]                                           offsetIndices;
    /** The offset the level */
    private final int                                             offsetLevel;
    /** The backing JHPL lattice */
    protected final Lattice<Integer, Integer>                     lattice;

    /** Information loss */
    protected ObjectObjectOpenHashMap<Object, Object>             data                        = new ObjectObjectOpenHashMap<>();

    /** Information loss */
    protected ObjectObjectOpenHashMap<Object, InformationLoss<?>> lowerBound                  = new ObjectObjectOpenHashMap<>();

    /** Information loss */
    protected ObjectObjectOpenHashMap<Object, InformationLoss<?>> utility                     = new ObjectObjectOpenHashMap<>();
   
    /**
     * For de-serialization
     * @param lattice
     * @param config
     */
    protected SolutionSpace(ARXLattice lattice, ARXConfiguration config) {
        this(lattice.getBottom().getTransformation(), lattice.getTop().getTransformation());
        setMonotonicity(config);
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                int[] index = toJHPL(node.getTransformation());
                int lvl = getLevel(index);
                if (node.getAnonymity() == Anonymity.ANONYMOUS) {
                    this.lattice.putProperty(index, lvl, this.getPropertyAnonymous());
                } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                    this.lattice.putProperty(index, lvl, this.getPropertyNotAnonymous());
                }
                if (node.isChecked()) {
                    this.lattice.putProperty(index, lvl, this.getPropertyChecked());
                    this.setInformationLoss(node.getTransformation(), node.getHighestScore());
                }
            }
        }
    }

    /**
     * Creates a new instance
     * @param minLevels
     * @param maxLevels
     */
    protected SolutionSpace(int[] minLevels, int[] maxLevels) {

        // Create offsets
        minLevels = reverse(minLevels);
        maxLevels = reverse(maxLevels);
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
        if (this instanceof SolutionSpaceLong) {
            this.lattice =  new Lattice<Integer, Integer>(elements);
        } else {
            this.lattice =  new LatticeHighdimensional<Integer, Integer>(elements);
        }
    }

    /**
     * Returns the bottom transformation
     * @return
     */
    public abstract Transformation<T> getBottom();
    
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
    public abstract ObjectIterator<T> getMaterializedTransformations();

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
     * Property for expanded transformation
     * @return
     */
    public PredictiveProperty getPropertyExpanded() {
        return this.propertyExpanded;
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
     * Returns the overall number of transformations in the solution space
     * @return
     */
    public abstract BigInteger getSize();
    
    /**
     * Returns the top-transformation
     * @return
     */
    public abstract Transformation<T> getTop();
    
    /**
     * Returns a wrapper object with access to all properties about the transformation
     * @param transformation
     * @return
     */
    public abstract Transformation<T> getTransformation(int[] transformation);

    /**
     * Returns the transformation with the given identifier
     * @param _identifier
     * @return
     */
    public abstract Transformation<T> getTransformation(Object _identifier);
    
    /**
     * Returns the utility of the transformation with the given identifier
     * @param identifier
     * @return
     */
    public abstract InformationLoss<?> getUtility(Object _identifier);

    /**
     * Returns whether a node has a given property
     * @param transformation
     * @param property
     * @return
     */
    public abstract boolean hasProperty(int[] transformation, PredictiveProperty property);
    
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
     * Returns all transformations in the solution space
     * @return
     */
    public abstract ObjectIterator<T> unsafeGetAllTransformations();

    /**
     * Returns *all* nodes on the given level. This is an unsafe operation that only performs well for "small" spaces.
     * @param level
     * @return
     */
    public abstract ObjectIterator<T> unsafeGetLevel(int level);

    /**
     * Internal method that adds the offset
     * @param level
     * @return
     */
    protected int fromJHPL(int level) {
        return level + offsetLevel;
    }

    /**
     * Internal method that adds the offsets
     * @param transformation
     * @return
     */
    protected int[] fromJHPL(int[] transformation) {
        int[] result = new int[transformation.length];
        for (int i=0; i<result.length; i++) {
            result[i] = transformation[transformation.length - i - 1] + offsetIndices[transformation.length - i - 1];
        }
        return result;
    }

    /**
     * Returns data
     * @param id
     * @return
     */
    protected abstract Object getData(T id);

    /**
     * Returns the information loss
     * @param identifier
     * @return
     */
    protected abstract InformationLoss<?> getInformationLoss(T identifier);
    
    /**
     * Returns the lower bound
     * @param identifier
     * @return
     */
    protected abstract InformationLoss<?> getLowerBound(T identifier);
    
    /**
     * Reverses the given array
     * @param input
     * @return
     */
    protected int[] reverse(int[] input) {
        int[] result = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[input.length - i - 1];
        }
        return result;
    }
    
    /**
     * Sets data
     * @param id
     * @param object
     */
    protected abstract void setData(T id, Object object);

    /**
     * Sets the information loss
     * @param transformation
     * @param loss
     */
    protected abstract void setInformationLoss(int[] transformation, InformationLoss<?> loss);

    /**
     * Sets the information loss
     * @param identifier
     * @param loss
     */
    protected abstract void setInformationLoss(T identifier, InformationLoss<?> loss);
    
    /**
     * Sets the lower bound
     * @param identifier
     * @param loss
     */
    protected abstract void setLowerBound(T identifier, InformationLoss<?> loss);

    /**
     * Sets the monotonicity of the anonymity property
     * @param config
     */
    protected void setMonotonicity(ARXConfiguration config) {
        setAnonymityPropertyPredictable(config.getMonotonicityOfPrivacy() == Monotonicity.FULL);
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
        int[] result = new int[transformation.length];
        for (int i=0; i<result.length; i++) {
            result[i]=transformation[transformation.length - i - 1] - offsetIndices[i];
        }
        return result;
    }
}
