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

import cern.colt.list.LongArrayList;
import de.linearbits.jhpl.JHPLIterator.LongIterator;
import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * The class Transformation.
 * 
 * @author Fabian Prasser
 */
public class TransformationLong extends Transformation<Long> {

    /**
     * Instantiates a new transformation.
     * @param transformation In ARX space
     * @param lattice
     * @param solutionSpace 
     */
    protected TransformationLong(int[] transformation, Lattice<Integer, Integer> lattice, SolutionSpaceLong solutionSpace) {
        super(transformation, lattice, solutionSpace);
        this.identifier = lattice.space().toId(transformationJHPL);
    }

    /**
     * Instantiates a new transformation.
     * @param transformationJHPL
     * @param identifier
     * @param lattice
     * @param solutionSpace
     */
    protected TransformationLong(int[] transformationJHPL,
                              long identifier,
                              Lattice<Integer, Integer> lattice,
                              SolutionSpaceLong solutionSpace) {
        super(solutionSpace.fromJHPL(transformationJHPL), lattice, solutionSpace);
        this.identifier = identifier;
    }

    /**
     * Returns all predeccessors of the transformation with the given identifier
     * @param transformation
     * @return
     */
    public TransformationList<Long> getPredecessors() {
        
        LongArrayList result = new LongArrayList();
        for (LongIterator iter = lattice.nodes().listPredecessorsAsIdentifiers(transformationJHPL, identifier); iter.hasNext();) {
            result.add(iter.next());
        }
        return TransformationList.create(result);
    }

    /**
     * Returns all successors
     * @return
     */
    public TransformationList<Long> getSuccessors() {
        cern.colt.list.LongArrayList result = new cern.colt.list.LongArrayList();
        for (LongIterator iter = lattice.nodes().listSuccessorsAsIdentifiers(transformationJHPL, identifier); iter.hasNext();) {
            result.add(iter.next());
        }
        int lower = 0;
        int upper = result.size() - 1;
        while (lower < upper) {
            long temp = result.get(lower);
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
        LongIterator neighbors;
        if (property.getDirection() == Direction.UP) {
            neighbors = lattice.nodes().listSuccessorsAsIdentifiers(transformationJHPL, identifier);
        } else if (property.getDirection() == Direction.DOWN) {
            neighbors = lattice.nodes().listPredecessorsAsIdentifiers(transformationJHPL, identifier);
        } else {
            return;
        }
        LongArrayList list = new LongArrayList();
        for (;neighbors.hasNext();) {
            list.add(neighbors.next());
        }
        for (int i=0; i<list.size(); i++) {
            int[] index = lattice.space().toIndex(list.getQuick(i));
            int level = lattice.nodes().getLevel(index);
            lattice.putProperty(index, level, property);
        }
    }
}
