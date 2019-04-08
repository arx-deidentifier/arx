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
import org.deidentifier.arx.metric.InformationLoss;

import de.linearbits.jhpl.PredictiveProperty;

/**
 * A class representing the solution space
 * @author Fabian Prasser
 */
public class SolutionSpaceLong extends SolutionSpace<Long> {

    /**
     * Delegate constructor
     * @param lattice
     * @param config
     */
    public SolutionSpaceLong(ARXLattice lattice, ARXConfiguration config) {
        super(lattice, config);
    }

    /**
     * Delegate constructor
     * @param minLevels
     * @param maxLevels
     */
    public SolutionSpaceLong(int[] minLevels, int[] maxLevels) {
        super(minLevels, maxLevels);
    }

    /**
     * Returns the bottom transformation
     * @return
     */
    public Transformation<Long> getBottom() {
        return getTransformation(fromJHPL(lattice.nodes().getBottom()));
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
        return BigInteger.valueOf(lattice.numNodes());
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
     * Returns data
     * @param id
     * @return
     */
    protected Object getData(Long id) {
        return data.getOrDefault(id, null);
    }
    
    /**
     * Returns the information loss
     * @param identifier
     * @return
     */
    protected InformationLoss<?> getInformationLoss(Long identifier) {
        return utility.getOrDefault(identifier, null);
    }
    
    /**
     * Returns the lower bound
     * @param identifier
     * @return
     */
    protected InformationLoss<?> getLowerBound(Long identifier) {
        return lowerBound.getOrDefault(identifier, null);
    }

    /**
     * Sets data
     * @param id
     * @param object
     */
    protected void setData(Long id, Object object) {
        data.put(id, object);
    }

    /**
     * Sets the information loss
     * @param node
     * @param loss
     */
    protected void setInformationLoss(int[] node, InformationLoss<?> loss) {
        int[] index = toJHPL(node);
        long id = lattice.space().toId(index);
        utility.put(id, loss);
    }

    /**
     * Sets the information loss
     * @param identifier
     * @param loss
     */
    protected void setInformationLoss(Long identifier, InformationLoss<?> loss) {
        utility.put(identifier, loss);
    }

    /**
     * Sets the lower bound
     * @param identifier
     * @param loss
     */
    protected void setLowerBound(Long identifier, InformationLoss<?> loss) {
        lowerBound.put(identifier, loss);
    }
}
