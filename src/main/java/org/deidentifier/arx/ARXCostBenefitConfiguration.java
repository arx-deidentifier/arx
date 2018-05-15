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

package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Basic configuration of monetary amounts, such as the publisher's benefit
 * per record or the per-record fine fine for a successful re-identification attack.
 * These parameters can be used to perform monetary cost/benefit analyses, e.g. 
 * using a game-theoretic approach.
 * 
 * @author Fabian Prasser
 */
public class ARXCostBenefitConfiguration implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -560679186676701860L;

    /**
     * Creates a new instance
     * @return
     */
    public static ARXCostBenefitConfiguration create() {
        return new ARXCostBenefitConfiguration();
    }

    /** Basic parameters */
    private double             publisherBenefit  = 1200d;
    /** Basic parameters */
    private double             publisherLoss     = 300d;
    /** Basic parameters */
    private double             adversaryGain     = 300d;
    /** Basic parameters */
    private double             adversaryCost     = 4d;
    
    /**
     * Hide constructor
     */
    protected ARXCostBenefitConfiguration() {
        // Empty by design
    }

    @Override
    public ARXCostBenefitConfiguration clone() {
        ARXCostBenefitConfiguration result = new ARXCostBenefitConfiguration();
        result.publisherBenefit = this.publisherBenefit;
        result.publisherLoss = this.publisherLoss;
        result.adversaryGain = this.adversaryGain;
        result.adversaryCost = this.adversaryCost;
        return result;
    }

    /**
     * Returns the amount of money needed by an attacker for trying to re-identify a single record
     * @return the adversaryCost
     */
    public double getAdversaryCost() {
        return adversaryCost;
    }

    /**
     * Returns the amount of money earned by an attacker for successfully re-identifying a single record
     * @return the adversaryGain
     */
    public double getAdversaryGain() {
        return adversaryGain;
    }
    
    /**
     * Returns the amount of money earned by the data publisher for publishing a single record
     * @return the publisherBenefit
     */
    public double getPublisherBenefit() {
        return publisherBenefit;
    }

    /**
     * Returns the amount of money lost by the data publisher, e.g. due to a fine, if a single record is attacked successfully
     * @return the publisherLoss
     */
    public double getPublisherLoss() {
        return publisherLoss;
    }

    /**
     * Sets the amount of money needed by an attacker for trying to re-identify a single record
     * @param adversaryCost the adversaryCost to set
     */
    public ARXCostBenefitConfiguration setAdversaryCost(double adversaryCost) {
        checkParameter("adversary cost", adversaryCost);
        this.adversaryCost = adversaryCost;
        return this;
    }

    /**
     * Sets the amount of money earned by an attacker for successfully re-identifying a single record
     * @param adversaryGain the adversaryGain to set
     */
    public ARXCostBenefitConfiguration setAdversaryGain(double adversaryGain) {
        checkParameter("adversary gain", adversaryGain);
        this.adversaryGain = adversaryGain;
        return this;
    }

    /**
     * Sets the amount of money earned by the data publisher for publishing a single record
     * @param publisherBenefit the publisherBenefit to set
     */
    public ARXCostBenefitConfiguration setPublisherBenefit(double publisherBenefit) {
        checkParameter("publisher benefit", publisherBenefit);
        this.publisherBenefit = publisherBenefit;
        return this;
    }
    
    /**
     * Sets the amount of money lost by the data publisher, e.g. due to a fine, if a single record is attacked successfully
     * @param publisherLoss the publisherLoss to set
     */
    public ARXCostBenefitConfiguration setPublisherLoss(double publisherLoss) {
        checkParameter("publisher loss", publisherLoss);
        this.publisherLoss = publisherLoss;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();   
        builder.append("[benefit=").append(publisherBenefit).append(", loss=");
        builder.append(publisherLoss).append(", gain=").append(adversaryGain).append(", cost=").append(adversaryCost).append("]");
        return builder.toString();
    }
    
    /**
     * Checks a given parameter
     * @param parameter
     * @param value
     */
    private void checkParameter(String parameter, double value) {
        if (value < 0d || Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Parameter '" + parameter + "' out of range!");
        }
    }
}
