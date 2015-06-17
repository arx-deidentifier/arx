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

import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.metric.InformationLoss;

import de.linearbits.jhpl.PredictiveProperty;
import de.linearbits.jhpl.PredictiveProperty.Direction;

/**
 * The Class Node.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Transformation {

    /** The id. */
    private long           id;
    
    public long getId() {
        
    }
    
    public long[] getPredecessors() {
        
    }
    
    public long[] getSuccessors() {
        
    }
    
    public InformationLoss<?> getInformationLoss() {
        
    }

    public InformationLoss<?> getLowerBound() {
        
    }

    public int[] getGeneralization() {
        
    }
    
    public Object getData() {
        
    }

    /**
     * Sets the properties to the given node.
     *
     * @param node the node
     * @param result the result
     */
    public void setChecked(NodeChecker.Result result) {
        
        // Set checked
        setProperty(node, Transformation.PROPERTY_CHECKED);
        
        // Anonymous
        if (result.privacyModelFulfilled){
            setProperty(node, Transformation.PROPERTY_ANONYMOUS);
        } else {
            setProperty(node, Transformation.PROPERTY_NOT_ANONYMOUS);
        }

        // k-Anonymous
        if (result.minimalClassSizeFulfilled){
            setProperty(node, Transformation.PROPERTY_K_ANONYMOUS);
        } else {
            setProperty(node, Transformation.PROPERTY_NOT_K_ANONYMOUS);
        }

        // Infoloss
        node.setInformationLoss(result.informationLoss);
        node.setLowerBound(result.lowerBound);
    }

    public int getLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setInformationLoss(InformationLoss<?> informationLoss) {
        // TODO Auto-generated method stub
        
    }

    public void setLowerBound(InformationLoss<?> lowerBound) {
        // TODO Auto-generated method stub
        
    }

    public void setData(Object object) {
        // TODO Auto-generated method stub
        
    }

    public boolean hasProperty(PredictiveProperty property) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setProperty(PredictiveProperty property) {
        // TODO Auto-generated method stub
        
    }

    public void setPropertyToNeighbours(PredictiveProperty property) {
        // Excludes the node itself
    }
}
