/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Basic configuration for the Stackelberg game
 * @author Fabian Prasser
 *
 */
public class ARXStackelbergConfiguration implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -560679186676701860L;

    /**
     * Creates a new instance
     * @return
     */
    public static ARXStackelbergConfiguration create() {
        return new ARXStackelbergConfiguration();
    }

    /** Basic parameters */
    private double             publisherBenefit  = 1200d;
    /** Basic parameters */
    private double             publisherLoss     = 300d;
    /** Basic parameters */
    private double             adversaryGain     = 300d;
    /** Basic parameters */
    private double             adversaryCost     = 4d;
    /** Parameter for the journalist model */
    private DataSubset         subset            = null;
    
    /**
     * Hide constructor
     */
    private ARXStackelbergConfiguration() {
        // Empty by design
    }

    @Override
    public ARXStackelbergConfiguration clone() {
        ARXStackelbergConfiguration result = new ARXStackelbergConfiguration();
        result.publisherBenefit = this.publisherBenefit;
        result.publisherLoss = this.publisherLoss;
        result.adversaryGain = this.adversaryGain;
        result.adversaryCost = this.adversaryCost;
        result.subset = this.subset != null ? this.subset.clone() : null;
        return result;
    }

    /**
     * @return the adversaryCost
     */
    public double getAdversaryCost() {
        return adversaryCost;
    }

    /**
     * @return the adversaryGain
     */
    public double getAdversaryGain() {
        return adversaryGain;
    }
    
    /**
     * @return the subset
     */
    public DataSubset getDataSubset() {
        return subset;
    }

    /**
     * @return the publisherBenefit
     */
    public double getPublisherBenefit() {
        return publisherBenefit;
    }

    /**
     * @return the publisherLoss
     */
    public double getPublisherLoss() {
        return publisherLoss;
    }

    /**
     * @return whether we assume the journalist attacker model
     */
    public boolean isJournalistAttackerModel() {
        return this.subset != null;
    }

    /**
     * @return whether we assume the journalist attacker model
     */
    public boolean isProsecutorAttackerModel() {
        return this.subset == null;
    }

    /**
     * @param adversaryCost the adversaryCost to set
     */
    public ARXStackelbergConfiguration setAdversaryCost(double adversaryCost) {
        this.adversaryCost = adversaryCost;
        return this;
    }

    /**
     * @param adversaryGain the adversaryGain to set
     */
    public ARXStackelbergConfiguration setAdversaryGain(double adversaryGain) {
        this.adversaryGain = adversaryGain;
        return this;
    }

    /**
     * Set the journalist attacker model with an explicit sub-/superset
     * @param subset
     * @return
     */
    public ARXStackelbergConfiguration setJournalistAttackerModel(DataSubset subset){
        this.subset = subset;
        return this;
    }

    /**
     * Set the prosecutor attacker model
     * @return
     */
    public ARXStackelbergConfiguration setProsecutorAttackerModel(){
        this.subset = null;
        return this;
    }
    
    /**
     * @param publisherBenefit the publisherBenefit to set
     */
    public ARXStackelbergConfiguration setPublisherBenefit(double publisherBenefit) {
        this.publisherBenefit = publisherBenefit;
        return this;
    }
    
    /**
     * @param publisherLoss the publisherLoss to set
     */
    public ARXStackelbergConfiguration setPublisherLoss(double publisherLoss) {
        this.publisherLoss = publisherLoss;
        return this;
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        builder.append(isProsecutorAttackerModel() ? "[prosecutor, " : "[journalist, ");   
        builder.append("benefit=").append(publisherBenefit).append(", loss=");
        builder.append(publisherLoss).append(", gain=").append(adversaryGain).append(", cost=").append(adversaryCost).append("]");
        return builder.toString();
    }
}
