/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyMatrix;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyMatrix.PrivacyCondition;

/**
 * Abstract class for criteria based on minimal keys
 * 
 * @author Fabian Prasser
 */
public abstract class KeyBasedCriterion extends MatrixBasedCriterion{

    
    /** SVUID */
    private static final long serialVersionUID = 3089514724590941748L;

    /**
     * Creates a new instance of this criterion.
     *  
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     */
    public KeyBasedCriterion(boolean monotonicWithSuppression, 
                             boolean monotonicWithGeneralization){
        super(monotonicWithSuppression, monotonicWithGeneralization);
    }
    
    @Override
    public void enforce(final HashGroupifyMatrix matrix,
                        final int numMaxSuppressedOutliers) {
        
        // Early abort
        if (KeyBasedCriterion.this.isFulfilled(matrix)) {
            return;
        }
       
        // Binary search
        matrix.suppressWhileNotFulfilledBinary(new PrivacyCondition(){
            public State isFulfilled(HashGroupifyMatrix matrix) {
                boolean fulfilled = KeyBasedCriterion.this.isFulfilled(matrix);
                
                // Early abort
                if (!fulfilled && matrix.getNumSuppressedRecords() > numMaxSuppressedOutliers) {
                    return State.ABORT;
                    
                // Go on
                } else {
                    return fulfilled ? State.FULFILLED : State.NOT_FULFILLED;
                }
            }
        });
    }
    
    @Override
    public int getRequirements(){
        // Requires only one counter
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }

    /**
     * To be implemented by matrix-based criteria
     * @param matrix
     * @return
     */
    protected abstract boolean isFulfilled(HashGroupifyMatrix matrix);
}