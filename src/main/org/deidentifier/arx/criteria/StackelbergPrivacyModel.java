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
import org.deidentifier.arx.metric.v2.MetricSDNMPublisherBenefit;

/**
 * Privacy model for the game theoretic approach proposed in:
 * A Game Theoretic Framework for Analyzing Re-Identification Risk.
 * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton,
 * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
 * PLOS|ONE. 2015.
 * 
 * @author Fabian Prasser
 */
public class StackelbergPrivacyModel extends ImplicitPrivacyCriterion implements _PrivacyModelWithSubset {

    /** SVUID */
    private static final long                 serialVersionUID = -1698534839214708559L;

    /** Configuration */
    private final ARXStackelbergConfiguration config;

    /** Metric*/
    private final MetricSDNMPublisherBenefit  metric;

    /**
     * Creates a new instance of game theoretic approach proposed in:
     * A Game Theoretic Framework for Analyzing Re-Identification Risk.
     * Zhiyu Wan, Yevgeniy Vorobeychik, Weiyi Xia, Ellen Wright Clayton, 
     * Murat Kantarcioglu, Ranjit Ganta, Raymond Heatherly, Bradley A. Malin
     * PLOS|ONE. 2015. 
     */
    public StackelbergPrivacyModel(MetricSDNMPublisherBenefit metric){
        // TODO: Can we find some form of monotonicity for this model?
        super(false, false);
        this.metric = metric;
        this.config = metric.getStackelbergConfig();
    }

    @Override
    public StackelbergPrivacyModel clone() {
        return new StackelbergPrivacyModel(this.metric.clone());
    }

    /**
     * Returns the configuration
     * @return config
     */
    public ARXStackelbergConfiguration getConfig() {
        return config;
    }

    @Override
    public int getRequirements(){
        if (config.isProsecutorAttackerModel()) {
            return ARXConfiguration.REQUIREMENT_COUNTER;
        } else {
            return ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
        }
    }
    
    @Override
    public boolean isAnonymous(Transformation transformation, HashGroupifyEntry entry) {
        
        // This is a class containing only records from the population
        if (entry.count == 0) {
            return true;
        }
        
        // Calculate publisher's payoff
        double payoff = metric.getPublisherPayoff(transformation, entry)[1];
        
        // We keep the set of records if the payoff is > 0
        return (payoff > 0);
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return config.isProsecutorAttackerModel();
    }

    @Override
    public String toString() {
        return "stackelberg-game " + config.toString();
    }

    @Override
    public DataSubset getDataSubset() {
        return metric.getStackelbergConfig().getDataSubset();
    }

    @Override
    public boolean isSubsetAvailable() {
        return metric.getStackelbergConfig().isJournalistAttackerModel();
    }
}