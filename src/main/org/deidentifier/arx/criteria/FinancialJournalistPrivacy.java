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
import org.deidentifier.arx.ARXFinancialConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * Privacy model for the game theoretic approach proposed in:
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class FinancialJournalistPrivacy extends FinancialProsecutorPrivacy {

    /** SVUID */
    private static final long serialVersionUID = 5089787798100584405L;

    /** Data subset */
    private DataSubset        subset;

    /**
     * Creates a new instance of game theoretic approach proposed in:
     * A Game Theoretic Framework for Analyzing Re-Identification Risk.
     * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton, 
     * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
     * PLOS|ONE. 2015. 
     */
    public FinancialJournalistPrivacy(DataSubset subset){
        super();
        this.subset = subset;
    }

    @Override
    public FinancialJournalistPrivacy clone() {
        return new FinancialJournalistPrivacy(this.subset.clone());
    }

    @Override
    public PrivacyCriterion clone(DataSubset subset) {
        throw new UnsupportedOperationException("Local recoding is not supported by this model");
    }
    
    @Override
    public DataSubset getDataSubset() {
        return this.subset;
    }
    
    @Override
    public int getRequirements(){
        return ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return false;
    }

    @Override
    public boolean isSubsetAvailable() {
        return true;
    }
    
    @Override
    public ElementData render() {
        ElementData result = new ElementData("Financial privacy");
        result.addProperty("Attacker model", "Journalist");
        ARXFinancialConfiguration config = super.getConfiguration();
        if (config != null) {
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

    @Override
    protected double getSuccessProbability(HashGroupifyEntry entry) {
        return entry.pcount == 0 ? 1d / entry.count : 1d / entry.pcount;
    }
}