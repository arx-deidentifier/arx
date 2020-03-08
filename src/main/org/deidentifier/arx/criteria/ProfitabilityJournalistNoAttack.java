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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXCostBenefitConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
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
public class ProfitabilityJournalistNoAttack extends ProfitabilityProsecutorNoAttack {

    /** SVUID */
    private static final long serialVersionUID = 1073520003237793563L;

    /** The data subset */
    private final DataSubset  subset;

    /**
     * Creates a new instance
     * @param subset
     */
    public ProfitabilityJournalistNoAttack(DataSubset subset) {
        super();
        this.subset = subset;
    }
    
    @Override
    public PrivacyCriterion clone() {
        return new ProfitabilityJournalistNoAttack(this.subset.clone());
    }
    
    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        throw new UnsupportedOperationException("Local recoding is not supported by this model");
    }
    
    @Override
    public DataSubset getDataSubset() {
        return subset;
    }
    
    @Override
    public int getMinimalClassSize() {
        return 0;
    }
    
    @Override
    public int getRequirements() {
        return ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }
    
    @Override
    public double getRiskThresholdJournalist() {
        return super.getRiskThresholdProsecutor();
    }
    
    @Override
    public double getRiskThresholdMarketer() {
        return super.getRiskThresholdJournalist();
    }
    
    @Override
    public double getRiskThresholdProsecutor() {
        return 0d;
    }

    @Override
    public boolean isAnonymous(Transformation<?> node, HashGroupifyEntry entry) {
        return entry.pcount >= super.getK();
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return false;
    }

    @Override
    public boolean isMinimalClassSizeAvailable() {
        return false;
    }

    @Override
    public boolean isSubsetAvailable() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("No-attack profitability");
        result.addProperty("Attacker model", "Journalist");
        ARXCostBenefitConfiguration config = super.getConfiguration();
        if (config != null) {
            result.addProperty("Threshold", super.getK());
            result.addProperty("Adversary cost", config.getAdversaryCost());
            result.addProperty("Adversary gain", config.getAdversaryGain());
            result.addProperty("Publisher loss", config.getPublisherLoss());
            result.addProperty("Publisher benefit", config.getPublisherBenefit());
        }
        return result;
    }

    @Override
    public String toString() {
        return toString("journalist");
    }

}
