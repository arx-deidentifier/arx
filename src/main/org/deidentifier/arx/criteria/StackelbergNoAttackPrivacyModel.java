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
import org.deidentifier.arx.framework.data.DataManager;
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

    /** The underlying k-map privacy model */
    private KMap                              kMap;
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
        // -> adversaryGain * successProbability < adversaryCost
        // -> successProbability < adversaryCost / adversaryGain
        // With successProbability = 1 / (size of population group of r), we have:
        // -> 1 / (size of population group of r) < adversaryCost / adversaryGain
        // -> (size of population group of r) > adversaryGain / adversaryCost
        this.k = (int)Math.ceil(config.getAdversaryGain() / config.getAdversaryCost());
        
        // Decide whether to use k-anonymity or k-map
        if (config.isJournalistAttackerModel()) {
            this.kMap = new KMap(k, config.getDataSubset());
        } else {
            this.kMap = null;
        }
    }
    
    @Override
    public PrivacyCriterion clone() {
        StackelbergNoAttackPrivacyModel result = new StackelbergNoAttackPrivacyModel(this.config.clone());
        result.k = this.k;
        result.kMap = this.kMap != null ? this.kMap.clone() : null;
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
        return kMap != null ? kMap.getRequirements() : ARXConfiguration.REQUIREMENT_COUNTER;
    }
    
    @Override
    public double getRiskThresholdJournalist() {
        return kMap != null ? kMap.getRiskThresholdJournalist() :  getRiskThresholdProsecutor();
    }
    
    @Override
    public double getRiskThresholdMarketer() {
        return kMap != null ? kMap.getRiskThresholdMarketer() :  getRiskThresholdProsecutor();
    }
    
    @Override
    public double getRiskThresholdProsecutor() {
        return kMap != null ? kMap.getRiskThresholdProsecutor() : 1d / (double)k;
    }
    
    @Override
    public void initialize(DataManager manager) {
        if (kMap != null) {
            kMap.initialize(manager);
        }
    }
    
    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        return kMap != null ? kMap.isAnonymous(node, entry) : entry.count >= k;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return kMap != null ? kMap.isLocalRecodingSupported() : true;
    }
    
    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        if (!isLocalRecodingSupported()) {
            throw new UnsupportedOperationException("Local recoding is not supported by this model");
        }
        if (kMap != null) {
            return kMap.clone(subset);
        }  else {
            return new KAnonymity(this.k);
        }
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
