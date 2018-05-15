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
package org.deidentifier.arx.risk;

import java.io.Serializable;

import org.deidentifier.arx.ARXCostBenefitConfiguration;

/**
 * This class implements a cost/benefit analysis following the game-theoretic approach proposed in: <br>
 * 
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class RiskModelCostBenefit implements Serializable {

    /** SVUID */
    private static final long                 serialVersionUID = -6124431335607475931L;

    /** The underlying configuration */
    private final ARXCostBenefitConfiguration config;

    /**
     * Creates a new instance
     * @param configuration
     */
    public RiskModelCostBenefit(ARXCostBenefitConfiguration configuration) {
        this.config = configuration;
    }
    
    /**
     * Returns the expected adversary payout
     * @param successProbability
     * @return
     */
    public double getExpectedAdversaryPayout(double successProbability) {
        checkArgument(successProbability);
        return config.getAdversaryGain() * successProbability - config.getAdversaryCost();
    }
    
    /**
     * Returns the expected information loss
     * @param informationLoss
     * @return
     */
    public double getExpectedPublisherBenefit(double informationLoss) {
        checkArgument(informationLoss);
        return config.getPublisherBenefit() * (1d - informationLoss);
    }

    /**
     * Returns the expected publisher payout
     * @param informationLoss
     * @param adversarySuccessProbability
     * @return
     */
    public double getExpectedPublisherPayout(double informationLoss, double adversarySuccessProbability ) {
        // Arguments will be checked in subsequent method calls
        return getExpectedPublisherBenefit(informationLoss) - 
               (getExpectedAdversaryPayout(adversarySuccessProbability) > 0 ? config.getPublisherLoss() * adversarySuccessProbability : 0);
    }
    
    /**
     * Checks the provided argument
     * @param argument
     */
    private void checkArgument(double argument) {
        if (argument < 0d || argument > 1d) {
            throw new IllegalArgumentException("Argument out of range [0,1]: " + argument);
        }
    }
}
