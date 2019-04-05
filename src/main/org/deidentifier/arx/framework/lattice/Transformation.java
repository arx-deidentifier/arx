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

import de.linearbits.jhpl.PredictiveProperty;

/**
 * The interface Transformation.
 * 
 * @author Fabian Prasser
 */
public interface Transformation<T> {

    /**
     * Returns associated data
     * @return
     */
    public Object getData();

    /**
     * Returns the generalization
     * @return
     */
    public int[] getGeneralization();
    
    /**
     * Returns the id
     * @return
     */
    public T getIdentifier();

    /**
     * Returns the information loss
     * @return
     */
    public InformationLoss<?> getInformationLoss() ;

    /**
     * Return level
     * @return
     */
    public int getLevel();
    
    /**
     * Returns the lower bound on information loss
     * @return
     */
    public InformationLoss<?> getLowerBound();

    /**
     * Returns all predeccessors of the transformation with the given identifier
     * @param transformation
     * @return
     */
    public TransformationList<T> getPredecessors();

    /**
     * Returns all successors
     * @return
     */
    public TransformationList<T> getSuccessors();

    /**
     * Returns whether this transformation has a given property
     * @param property
     * @return
     */
    public boolean hasProperty(PredictiveProperty property);

    /**
     * Sets the properties to the given node.
     *
     * @param node the node
     * @param result the result
     */
    public void setChecked(TransformationResult result);

    /**
     * Sets a data
     * @param object
     */
    public void setData(Object object);

    /**
     * Sets the information loss
     * @param informationLoss
     */
    public void setInformationLoss(InformationLoss<?> informationLoss);

    /**
     * Sets the lower bound
     * @param lowerBound
     */
    public void setLowerBound(InformationLoss<?> lowerBound);
    

    /**
     * Sets a property
     * @param property
     */
    public void setProperty(PredictiveProperty property);
    
    /**
     * Sets the property to all neighbors
     * @param property
     */
    public void setPropertyToNeighbours(PredictiveProperty property);

    /**
     * Returns a string representation
     */
    public String toString();
}
