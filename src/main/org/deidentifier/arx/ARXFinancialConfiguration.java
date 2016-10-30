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
 * Basic configuration of financial variables, such as the publisher's benefit
 * per record or the per-record cost of an attack. Required to analyze re-identification
 * risks on a financial basis, e.g. using a game-theoretic approach.
 * @author Fabian Prasser
 *
 */
public class ARXFinancialConfiguration implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -560679186676701860L;

    /**
     * Creates a new instance
     * @return
     */
    public static ARXFinancialConfiguration create() {
        return new ARXFinancialConfiguration();
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
    protected ARXFinancialConfiguration() {
        // Empty by design
    }

    @Override
    public ARXFinancialConfiguration clone() {
        ARXFinancialConfiguration result = new ARXFinancialConfiguration();
        result.publisherBenefit = this.publisherBenefit;
        result.publisherLoss = this.publisherLoss;
        result.adversaryGain = this.adversaryGain;
        result.adversaryCost = this.adversaryCost;
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
     * @param adversaryCost the adversaryCost to set
     */
    public ARXFinancialConfiguration setAdversaryCost(double adversaryCost) {
        this.adversaryCost = adversaryCost;
        return this;
    }

    /**
     * @param adversaryGain the adversaryGain to set
     */
    public ARXFinancialConfiguration setAdversaryGain(double adversaryGain) {
        this.adversaryGain = adversaryGain;
        return this;
    }

    /**
     * @param publisherBenefit the publisherBenefit to set
     */
    public ARXFinancialConfiguration setPublisherBenefit(double publisherBenefit) {
        this.publisherBenefit = publisherBenefit;
        return this;
    }
    
    /**
     * @param publisherLoss the publisherLoss to set
     */
    public ARXFinancialConfiguration setPublisherLoss(double publisherLoss) {
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
}
