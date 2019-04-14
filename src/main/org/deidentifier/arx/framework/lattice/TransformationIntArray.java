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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.framework.lattice.SolutionSpaceIntArray.IntArrayWrapper;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * The class Transformation.
 * 
 * @author Fabian Prasser
 */
public class TransformationIntArray extends Transformation<IntArrayWrapper> {
    
    /**
     * Delegate constructor
     * @param transformationARX
     * @param lattice
     * @param solutionSpace
     */
    protected TransformationIntArray(IntArrayWrapper transformationARX, Lattice<Integer, Integer> lattice, SolutionSpace<IntArrayWrapper> solutionSpace) {
        super(transformationARX.array, lattice, solutionSpace);
        this.identifier = transformationARX;
    }

    /**
     * Returns all predeccessors of the transformation with the given identifier
     * @param transformation
     * @return
     */
    public TransformationList<IntArrayWrapper> getPredecessors() {
        
        List<IntArrayWrapper> result = new ArrayList<>();
        for (Iterator<int[]> iter = lattice.nodes().listPredecessors(transformationJHPL); iter.hasNext();) {
            result.add(new IntArrayWrapper(solutionSpace.fromJHPL(iter.next())));
        }
        return TransformationList.create(result);
    }

    /**
     * Returns all successors
     * @return
     */
    public TransformationList<IntArrayWrapper> getSuccessors() {
        List<IntArrayWrapper> result = new ArrayList<>();
        for (Iterator<int[]> iter = lattice.nodes().listSuccessors(transformationJHPL); iter.hasNext();) {
            result.add(new IntArrayWrapper(solutionSpace.fromJHPL(iter.next())));
        }
        int lower = 0;
        int upper = result.size() - 1;
        while (lower < upper) {
            IntArrayWrapper temp = result.get(lower);
            result.set(lower, result.get(upper));
            result.set(upper, temp);
            lower++;
            upper--;
        }
        return TransformationList.create(result);
    }

    /**
     * Sets the property to all neighbors
     * @param property
     */
    public void setPropertyToNeighbours(PredictiveProperty property) {
        Iterator<int[]> neighbors;
        if (property.getDirection() == Direction.UP) {
            neighbors = lattice.nodes().listSuccessors(transformationJHPL);
        } else if (property.getDirection() == Direction.DOWN) {
            neighbors = lattice.nodes().listPredecessors(transformationJHPL);
        } else {
            return;
        }
        List<int[]> list = new ArrayList<>();
        for (;neighbors.hasNext();) {
            list.add(neighbors.next().clone());
        }
        for (int i=0; i<list.size(); i++) {
            int[] index = list.get(i);
            int level = lattice.nodes().getLevel(index);
            lattice.putProperty(index, level, property);
        }
    }
}
