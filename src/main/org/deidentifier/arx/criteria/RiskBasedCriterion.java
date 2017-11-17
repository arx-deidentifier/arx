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
import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution.PrivacyCondition;

/**
 * Abstract class for criteria that ensure that a certain risk measure is lower than or equal to a given threshold
 * 
 * @author Fabian Prasser
 */
public abstract class RiskBasedCriterion extends SampleBasedCriterion {

    /** SVUID */
    private static final long serialVersionUID = -2711630526630937284L;
    /** The threshold */
    private final double      threshold;

    /**
     * Creates a new instance of this criterion.
     *  
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     * @param riskThreshold
     */
    public RiskBasedCriterion(boolean monotonicWithSuppression, 
                              boolean monotonicWithGeneralization,
                              double riskThreshold){
        super(monotonicWithSuppression, monotonicWithGeneralization);
        this.threshold = riskThreshold;
        if (this.threshold < 0d || this.threshold >= 1d) {
            throw new IllegalArgumentException("Threshold out of range. Must be in [0, 1[");
        }
    }
    
    @Override
    public void enforce(final HashGroupifyDistribution distribution,
                        final int numMaxSuppressedOutliers) {
        
        // Early abort
        if (RiskBasedCriterion.this.isFulfilled(distribution)) {
            return;
        }
       
        // Binary search
        distribution.suppressWhileNotFulfilledBinary(new PrivacyCondition(){
            public State isFulfilled(HashGroupifyDistribution distribution) {
                boolean fulfilled = RiskBasedCriterion.this.isFulfilled(distribution);
                
                // Early abort
                if (!fulfilled && distribution.getNumSuppressedRecords() > numMaxSuppressedOutliers) {
                    return State.ABORT;
                    
                // Go on
                } else {
                    return fulfilled ? State.FULFILLED : State.NOT_FULFILLED;
                }
            }
        });
    }

    @Override
    public void enforceReliably(final HashGroupifyDistribution distribution,
                                final int numMaxSuppressedOutliers) {
        
        // Early abort
        if (RiskBasedCriterion.this.isFulfilled(distribution)) {
            return;
        }
       
        // Binary search
        distribution.suppressWhileNotFulfilledBinary(new PrivacyCondition(){
            public State isFulfilled(HashGroupifyDistribution distribution) {
                boolean fulfilled = RiskBasedCriterion.this.isReliablyFulfilled(distribution);
                
                // Early abort
                if (!fulfilled && distribution.getNumSuppressedRecords() > numMaxSuppressedOutliers) {
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
     * Returns the risk threshold
     *
     * @return
     */
    public double getRiskThreshold() {
        return threshold;
    }

    /**
     * To be implemented by risk-based criteria
     * @param distribution
     * @return
     */
    protected abstract boolean isFulfilled(HashGroupifyDistribution distribution);
    
    /**
     * Overwrite this to implement reliable anonymization
     * @param distribution
     * @return
     */
    protected boolean isReliablyFulfilled(HashGroupifyDistribution distribution) {
        return this.isFulfilled(distribution);
    }
}