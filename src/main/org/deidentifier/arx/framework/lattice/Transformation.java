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

import org.deidentifier.arx.framework.check.TransformationResult;
import org.deidentifier.arx.metric.InformationLoss;

import de.linearbits.jhpl.Lattice;
import de.linearbits.jhpl.PredictiveProperty;

/**
 * The interface Transformation.
 * 
 * @author Fabian Prasser
 */
public abstract class Transformation<T> {

    /** The lattice */
    protected final Lattice<Integer, Integer> lattice;

    /** The solution space */
    protected final SolutionSpace<T>          solutionSpace;

    /** The level */
    protected int                             levelARX          = -1;

    /** The level */
    protected int                             levelJHPL         = -1;

    /** Transformation in ARX's space */
    protected int[]                           transformationARX = null;

    /** Transformation in JHPL's space */
    protected final int[]                     transformationJHPL;

    /**
     * Instantiates a new transformation.
     * @param transformationARX In ARX space
     * @param lattice
     * @param solutionSpace 
     */
    protected Transformation(int[] transformationARX, Lattice<Integer, Integer> lattice, SolutionSpace<T> solutionSpace) {
        this.lattice = lattice;
        this.solutionSpace = solutionSpace;
        this.transformationARX = transformationARX;
        this.transformationJHPL = solutionSpace.toJHPL(transformationARX);
    }
    
    /**
     * Returns associated data
     * @return
     */
    public abstract Object getData();

    /**
     * Returns the generalization
     * @return
     */
    public abstract int[] getGeneralization();
    
    /**
     * Returns the id
     * @return
     */
    public abstract T getIdentifier();

    /**
     * Returns the information loss
     * @return
     */
    public abstract InformationLoss<?> getInformationLoss() ;

    /**
     * Return level
     * @return
     */
    public abstract int getLevel();
    
    /**
     * Returns the lower bound on information loss
     * @return
     */
    public abstract InformationLoss<?> getLowerBound();

    /**
     * Returns all predeccessors of the transformation with the given identifier
     * @param transformation
     * @return
     */
    public abstract TransformationList<T> getPredecessors();

    /**
     * Returns all successors
     * @return
     */
    public abstract TransformationList<T> getSuccessors();

    /**
     * Returns whether this transformation has a given property
     * @param property
     * @return
     */
    public abstract boolean hasProperty(PredictiveProperty property);

    /**
     * Sets the properties to the given node.
     *
     * @param node the node
     * @param result the result
     */
    public void setChecked(TransformationResult result) {
        
        // Set checked
        this.setProperty(solutionSpace.getPropertyChecked());
        
        // Anonymous
        if (result.privacyModelFulfilled){
            this.setProperty(solutionSpace.getPropertyAnonymous());
        } else {
            this.setProperty(solutionSpace.getPropertyNotAnonymous());
        }

        // k-Anonymous
        if (result.minimalClassSizeFulfilled != null) {
            if (result.minimalClassSizeFulfilled){
                this.setProperty(solutionSpace.getPropertyKAnonymous());
            } else {
                this.setProperty(solutionSpace.getPropertyNotKAnonymous());
            }
        }

        // Infoloss
        this.setInformationLoss(result.informationLoss);
        this.setLowerBound(result.lowerBound);
    }

    /**
     * Sets a data
     * @param object
     */
    public abstract void setData(Object object);

    /**
     * Sets the information loss
     * @param informationLoss
     */
    public abstract void setInformationLoss(InformationLoss<?> informationLoss);
    

    /**
     * Sets the lower bound
     * @param lowerBound
     */
    public abstract void setLowerBound(InformationLoss<?> lowerBound);

    /**
     * Sets a property
     * @param property
     */
    public abstract void setProperty(PredictiveProperty property);
    
    /**
     * Sets the property to all neighbors
     * @param property
     */
    public abstract void setPropertyToNeighbours(PredictiveProperty property);

    /**
     * Returns a string representation
     */
    public abstract String toString();
}
