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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXStackelbergConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Privacy model for the "no-attack" variant of the game theoretic approach proposed in:
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class StackelbergNoAttackPrivacyModel extends ImplicitPrivacyCriterion implements _PrivacyModelWithProsecutorThreshold,
                                                                                         _PrivacyModelWithSubset {

    /** SVUID */
    private static final long                 serialVersionUID = -1283022087083117810L;

    /** The underlying k-anonymity privacy model */
    private int                               k;
    /** Config */
    private final ARXStackelbergConfiguration config;

    /**
     * Creates a new instance
     * @param config
     */
    public StackelbergNoAttackPrivacyModel(ARXStackelbergConfiguration config) {
        
        // This model is monotonic
        super(true, true);
        
        // Store
        this.config = config;
        
        // We reduce this model to k-map or k-anonymity:
        // adversaryPayoff = adversaryGain * successProbability - adversaryCost
        // To give the adversary no incentives to attack we need to ensure that the following holds:
        // adversaryGain * successProbability < adversaryCost
        // -> successProbability < adversaryCost / adversaryGain
        // With successProbability = 1 / [size of (population) group of r], we have:
        // -> 1 / [size of (population) group of r] < adversaryCost / adversaryGain
        // -> [size of (population) group of r] > adversaryGain / adversaryCost
        this.k = (int)Math.ceil(config.getAdversaryGain() / config.getAdversaryCost());
    }
    
    @Override
    public PrivacyCriterion clone() {
        StackelbergNoAttackPrivacyModel result = new StackelbergNoAttackPrivacyModel(this.config.clone());
        result.k = this.k;
        return result;
    }
    
    @Override
    public DataSubset getDataSubset() {
        return config.getDataSubset();
    }
    
    @Override
    public int getProsecutorRiskThreshold() {
        if (config.isProsecutorAttackerModel()) {
            return this.k;
        } else {
            return 0;
        }
    }
    
    @Override
    public int getRequirements() {
        int result = ARXConfiguration.REQUIREMENT_COUNTER;
        if (config.isJournalistAttackerModel()) {
            result |= ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
        }
        return result;
    }
    
    @Override
    public double getRiskThresholdJournalist() {
        return 1d / (double)k;
    }
    
    @Override
    public double getRiskThresholdMarketer() {
        return getRiskThresholdJournalist();
    }
    
    @Override
    public double getRiskThresholdProsecutor() {
        if (config.isProsecutorAttackerModel()) {
            return 1d / (double)k;
        } else {
            return 0d;
        }
    }
    
    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        return config.isProsecutorAttackerModel() ? entry.count >= k : entry.pcount >= k;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return config.isProsecutorAttackerModel();
    }
    
    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        if (!isLocalRecodingSupported()) {
            throw new UnsupportedOperationException("Local recoding is not supported by this model");
        }
        return clone();
    }

    @Override
    public boolean isProsecutorRiskThresholdAvaliable() {
        return config.isProsecutorAttackerModel();
    }

    @Override
    public boolean isSubsetAvailable() {
        return config.isJournalistAttackerModel();
    }

    @Override
    public String toString() {
        return "no-attack-stackelberg-game " + config.toString();
    }
}
