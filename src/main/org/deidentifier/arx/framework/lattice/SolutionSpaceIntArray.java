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
import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.framework.lattice.SolutionSpaceIntArray.IntArrayWrapper;
import org.deidentifier.arx.metric.InformationLoss;

import de.linearbits.jhpl.PredictiveProperty;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public class SolutionSpaceIntArray extends SolutionSpace<IntArrayWrapper> {
    
    /**
     * Int array wrapper
     * @author Fabian Prasser
     */
    static class IntArrayWrapper {
        
        /** Array*/
        final int[] array;
        /** Hashcode*/
        private final int hashcode;
        /** 
         * Creates a new instance
         * @param array
         */
        protected IntArrayWrapper(int[] array) {
            this.array = array;
            this.hashcode = Arrays.hashCode(array);
        }
        
        @Override
        public boolean equals(Object other) {
            return Arrays.equals(this.array, ((IntArrayWrapper)other).array);
        }
        
        @Override
        public int hashCode() {
            return hashcode;
        }
    }

    /** Size */
    private BigInteger                                                   size       = null;

    /**
     * Delegate constructor
     * @param lattice
     * @param config
     */
    public SolutionSpaceIntArray(ARXLattice lattice, ARXConfiguration config) {
        super(lattice, config);
    }

    /**
     * Delegate constructor
     * @param minLevels
     * @param maxLevels
     */
    public SolutionSpaceIntArray(int[] minLevels, int[] maxLevels) {
        super(minLevels, maxLevels);
    }
    
    /**
     * Returns the bottom transformation
     * @return
     */
    public Transformation<IntArrayWrapper> getBottom() {
        return getTransformation(fromJHPL(lattice.nodes().getBottom()));
    }
    
    /**
     * Returns all materialized transformations
     * @return
     */
    public ObjectIterator<IntArrayWrapper> getMaterializedTransformations() {
        return ObjectIterator.create(this, lattice.listNodes());
    }

    /**
     * Returns the overall number of transformations in the solution space
     * @return
     */
    public BigInteger getSize() {
        
        // Not supported by JHPL, so we have to do this ourselves
        if (size == null) {
            size = BigInteger.valueOf(1);
            int[] bottom = lattice.nodes().getBottom();
            int[] top = lattice.nodes().getTop();
            for (int i=0; i<bottom.length; i++) {
                size = size.multiply(BigInteger.valueOf(top[i] - bottom[i] + 1));
            }
        }
        
        // Return
        return size;
    }
    
    /**
     * Returns the top-transformation
     * @return
     */
    public Transformation<IntArrayWrapper> getTop() {
        return getTransformation(fromJHPL(lattice.nodes().getTop()));
    }
    
    /**
     * Returns a wrapper object with access to all properties about the transformation
     * @param transformation - in ARX format
     * @return
     */
    public Transformation<IntArrayWrapper> getTransformation(int[] transformation) {
        return new TransformationIntArray(new IntArrayWrapper(transformation), lattice, this);
    }
    
    /**
     * Returns the transformation with the given identifier
     * @param _identifier - in ARX format
     * @return
     */
    public Transformation<IntArrayWrapper> getTransformation(Object _identifier) {
        return new TransformationIntArray((IntArrayWrapper)_identifier, lattice, this);
    }

    /**
     * Returns the utility of the transformation with the given identifier
     * @param identifier - in ARX format
     * @return
     */
    public InformationLoss<?> getUtility(Object identifier) {
        return utility.getOrDefault((IntArrayWrapper)identifier, null);
    }
    
    /**
     * Returns whether a node has a given property
     * @param transformation - in ARX format
     * @param property
     * @return
     */
    public boolean hasProperty(int[] transformation, PredictiveProperty property) {
        int[] index = toJHPL(transformation);
        int level = getLevel(index);
        return lattice.hasProperty(index, level, property);
    }

    /**
     * Returns all transformations in the solution space
     * @return
     */
    public ObjectIterator<IntArrayWrapper> unsafeGetAllTransformations() {
        // Sanity check
        throw new UnsupportedOperationException("Not supported for large solution spaces");
    }

    /**
     * Returns *all* nodes on the given level. This is an unsafe operation that only performs well for "small" spaces.
     * @param level
     * @return
     */
    public ObjectIterator<IntArrayWrapper> unsafeGetLevel(int level) {
        // Sanity check
        throw new UnsupportedOperationException("Not supported for large solution spaces");
    }

    /**
     * Returns data
     * @param id - in ARX format
     * @return
     */
    protected Object getData(IntArrayWrapper id) {
        return data.getOrDefault(id, null);
    }
    

    /**
     * Returns the information loss
     * @param identifier - in ARX format
     * @return
     */
    protected InformationLoss<?> getInformationLoss(IntArrayWrapper identifier) {
        return utility.getOrDefault(identifier, null);
    }
    
    /**
     * Returns the lower bound
     * @param identifier - in ARX format
     * @return
     */
    protected InformationLoss<?> getLowerBound(IntArrayWrapper identifier) {
        return lowerBound.getOrDefault(identifier, null);
    }

    /**
     * Sets data
     * @param id - in ARX format
     * @param object
     */
    protected void setData(IntArrayWrapper id, Object object) {
        data.put(id, object);
    }

    /**
     * Sets the information loss
     * @param identifier - in ARX format
     * @param loss
     */
    protected void setInformationLoss(int[] identifier, InformationLoss<?> loss) {
        utility.put(new IntArrayWrapper(identifier), loss);
    }

    /**
     * Sets the information loss
     * @param identifier - in ARX format
     * @param loss
     */
    protected void setInformationLoss(IntArrayWrapper identifier, InformationLoss<?> loss) {
        utility.put(identifier, loss);
    }
    
    /**
     * Sets the lower bound
     * @param identifier - in ARX format
     * @param loss
     */
    protected void setLowerBound(IntArrayWrapper identifier, InformationLoss<?> loss) {
        lowerBound.put(identifier, loss);
    }
}
