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

import java.util.Arrays;

import org.deidentifier.arx.metric.InformationLoss;

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

    /** The id. */
    private final Long                      identifier;

    /**
     * Instantiates a new transformation.
     * @param transformation In ARX space
     * @param lattice
     * @param solutionSpace 
     */
    public TransformationLong(int[] transformation, Lattice<Integer, Integer> lattice, SolutionSpaceLong solutionSpace) {
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
    public TransformationLong(int[] transformationJHPL,
                              long identifier,
                              Lattice<Integer, Integer> lattice,
                              SolutionSpaceLong solutionSpace) {
        super(solutionSpace.fromJHPL(transformationJHPL), lattice, solutionSpace);
        this.identifier = identifier;
    }

    /**
     * Returns associated data
     * @return
     */
    public Object getData() {
        return this.solutionSpace.getData(this.identifier);
    }

    /**
     * Returns the generalization
     * @return
     */
    public int[] getGeneralization() {
        if (this.transformationARX == null) {
            this.transformationARX = solutionSpace.fromJHPL(transformationJHPL);
        }
        return this.transformationARX;
    }
    
    /**
     * Returns the id
     * @return
     */
    public Long getIdentifier() {
        return identifier;
    }

    /**
     * Returns the information loss
     * @return
     */
    public InformationLoss<?> getInformationLoss() {
        return solutionSpace.getInformationLoss(this.identifier);
    }

    /**
     * Return level
     * @return
     */
    public int getLevel() {
        if (this.levelARX == -1) {
            this.levelJHPL = getLevel(transformationJHPL);
            this.levelARX = solutionSpace.fromJHPL(levelJHPL);
        }
        return levelARX;
    }
    
    /**
     * Returns the lower bound on information loss
     * @return
     */
    public InformationLoss<?> getLowerBound() {
        return solutionSpace.getLowerBound(this.identifier);
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
     * Returns whether this transformation has a given property
     * @param property
     * @return
     */
    public boolean hasProperty(PredictiveProperty property) {
        getLevel();
        return this.lattice.hasProperty(this.transformationJHPL, this.levelJHPL, property);
    }
    /**
     * Sets a data
     * @param object
     */
    public void setData(Object object) {
        this.solutionSpace.setData(this.identifier, object);
    }

    /**
     * Sets the information loss
     * @param informationLoss
     */
    public void setInformationLoss(InformationLoss<?> informationLoss) {
        this.solutionSpace.setInformationLoss(this.identifier, informationLoss);
    }

    /**
     * Sets the lower bound
     * @param lowerBound
     */
    public void setLowerBound(InformationLoss<?> lowerBound) {
        this.solutionSpace.setLowerBound(this.identifier, lowerBound);
    }
    

    /**
     * Sets a property
     * @param property
     */
    public void setProperty(PredictiveProperty property) {
        getLevel();
        this.lattice.putProperty(this.transformationJHPL, this.levelJHPL, property);
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

    /**
     * Returns a string representation
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Transformation {\n");
        builder.append(" - Solution space: ").append(this.solutionSpace.hashCode()).append("\n");
        builder.append(" - Index: ").append(Arrays.toString(transformationJHPL)).append("\n");
        builder.append(" - Id: ").append(identifier).append("\n");
        builder.append(" - Generalization: ").append(Arrays.toString(getGeneralization())).append("\n");
        builder.append(" - Level: ").append(getLevel()).append("\n");
        builder.append(" - Properties:\n");
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyAnonymous())) {
            builder.append("   * ANONYMOUS: ").append(solutionSpace.getPropertyAnonymous().getDirection()).append("\n");    
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyNotAnonymous())) {
            builder.append("   * NOT_ANONYMOUS: ").append(solutionSpace.getPropertyNotAnonymous().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyKAnonymous())) {
            builder.append("   * K_ANONYMOUS: ").append(solutionSpace.getPropertyKAnonymous().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyNotKAnonymous())) {
            builder.append("   * NOT_K_ANONYMOUS: ").append(solutionSpace.getPropertyNotKAnonymous().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyChecked())) {
            builder.append("   * CHECKED: ").append(solutionSpace.getPropertyChecked().getDirection()).append("\n");    
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyForceSnapshot())) {
            builder.append("   * FORCE_SNAPSHOT: ").append(solutionSpace.getPropertyForceSnapshot().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyInsufficientUtility())) {
            builder.append("   * INSUFFICIENT_UTILITY: ").append(solutionSpace.getPropertyInsufficientUtility().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertySuccessorsPruned())) {
            builder.append("   * SUCCESSORS_PRUNED: ").append(solutionSpace.getPropertySuccessorsPruned().getDirection()).append("\n");
        }
        if (lattice.hasProperty(transformationJHPL, this.levelJHPL, solutionSpace.getPropertyVisited())) {
            builder.append("   * VISITED: ").append(solutionSpace.getPropertyVisited().getDirection()).append("\n");
        }
        builder.append("}");
        return builder.toString();
    }
}
