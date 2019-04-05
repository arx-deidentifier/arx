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
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.metric.InformationLoss;

import com.carrotsearch.hppc.LongObjectOpenHashMap;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public class SolutionSpaceLong extends SolutionSpace<Long> {

    /** Information loss */
    private LongObjectOpenHashMap<Object>             data                        = new LongObjectOpenHashMap<Object>();
    /** The backing JHPL lattice */
    private final Lattice<Integer, Integer>           lattice;
    /** Information loss */
    private LongObjectOpenHashMap<InformationLoss<?>> lowerBound                  = new LongObjectOpenHashMap<InformationLoss<?>>();
    /** The offsets for indices */
    private final int[]                               offsetIndices;
    /** The offset the level */
    private final int                                 offsetLevel;
    /** Information loss */
    private LongObjectOpenHashMap<InformationLoss<?>> utility                     = new LongObjectOpenHashMap<InformationLoss<?>>();

    /**
     * For de-serialization
     * @param lattice
     * @param config
     */
    public SolutionSpaceLong(ARXLattice lattice, ARXConfiguration config) {
        this(lattice.getBottom().getTransformation(), lattice.getTop().getTransformation());
        setMonotonicity(config);
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                int[] index = toJHPL(node.getTransformation());
                int lvl = getLevel(index);
                long id = this.lattice.space().toId(index);
                if (node.getAnonymity() == Anonymity.ANONYMOUS) {
                    this.lattice.putProperty(index, lvl, this.getPropertyAnonymous());
                } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                    this.lattice.putProperty(index, lvl, this.getPropertyNotAnonymous());
                }
                if (node.isChecked()) {
                    this.lattice.putProperty(index, lvl, this.getPropertyChecked());
                    this.setInformationLoss(id, node.getHighestScore());
                }
            }
        }
    }

    /**
     * Creates a new solution space
     * @param minLevels
     * @param maxLevels
     */
    public SolutionSpaceLong(int[] minLevels, int[] maxLevels) {
        
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
        this.lattice = new Lattice<Integer, Integer>(elements);
    }
    
    /**
     * Returns the bottom transformation
     * @return
     */
    public Transformation<Long> getBottom() {
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
    public ObjectIterator<Long> getMaterializedTransformations() {
        return ObjectIterator.create(lattice.listNodesAsIdentifiers());
    }

    /**
     * Returns the overall number of transformations in the solution space
     * @return
     */
    public BigInteger getSize() {
        // TODO: Cache?
        return new BigInteger(String.valueOf(lattice.numNodes()));
    }
    
    /**
     * Returns the top-transformation
     * @return
     */
    public Transformation<Long> getTop() {
        return getTransformation(fromJHPL(lattice.nodes().getTop()));
    }
    
    /**
     * Returns a wrapper object with access to all properties about the transformation
     * @param transformation
     * @return
     */
    public Transformation<Long> getTransformation(int[] transformation) {
        return new TransformationLong(transformation, lattice, this);
    }
    
    /**
     * Returns the transformation with the given identifier
     * @param _identifier
     * @return
     */
    public Transformation<Long> getTransformation(Object _identifier) {
        long identifier = (Long)_identifier;
        int[] transformationJHPL = lattice.space().toIndex(identifier);
        return new TransformationLong(transformationJHPL, identifier, lattice, this);
    }

    /**
     * Returns the utility of the transformation with the given identifier
     * @param identifier
     * @return
     */
    public InformationLoss<?> getUtility(Object _identifier) {
        long identifier = (Long)_identifier;
        return utility.getOrDefault(identifier, null);
    }
    
    /**
     * Returns whether a node has a given property
     * @param transformation
     * @param property
     * @return
     */
    public boolean hasProperty(int[] transformation, PredictiveProperty property) {
        int[] index = toJHPL(transformation);
        int level = getLevel(index);
        return lattice.hasProperty(index, level, property);
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
     * Returns all transformations in the solution space
     * @return
     */
    public ObjectIterator<Long> unsafeGetAllTransformations() {
        return ObjectIterator.create(lattice.unsafe().listAllNodesAsIdentifiers());
    }

    /**
     * Returns *all* nodes on the given level. This is an unsafe operation that only performs well for "small" spaces.
     * @param level
     * @return
     */
    public ObjectIterator<Long> unsafeGetLevel(int level) {
        return ObjectIterator.create(lattice.unsafe().listAllNodesAsIdentifiers(toJHPL(level)));
    }

    /**
     * Reverses the given array
     * @param input
     * @return
     */
    private int[] reverse(int[] input) {
        int[] result = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[input.length - i - 1];
        }
        return result;
    }

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
        int[] result = new int[transformation.length];
        for (int i=0; i<result.length; i++) {
            result[i]=transformation[transformation.length - i - 1] - offsetIndices[i];
        }
        return result;
    }
}
