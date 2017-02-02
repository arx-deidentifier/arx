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

package org.deidentifier.arx.risk;

/**
 * This is the base class for the Question as well as the Section.
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public abstract class RiskQuestionnaireItem {

    /** Current weight configuration */
    private RiskQuestionnaireWeights weightConfiguration;

    /** Identifier (used for the weight configuration) */
    protected String                 identifier;

    /** Item's title */
    protected String                 title;

    /**
     * Create a new item from a line
     * 
     * @param line
     *            the line to parse
     */
    public RiskQuestionnaireItem(String line) {
        String components[] = line.split(":", 2);
        if (components.length != 2) {
            // System.err.println("Couldn't parse item! Original line: "+line);
            return;
        }
        this.identifier = components[0].trim();
        this.title = components[1].trim();
    }

    /**
     * Returns the identifier
     * 
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the title
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the current weight for this item according to the current weight
     * configuration
     * 
     * @return
     */
    public double getWeight() {
        if (weightConfiguration == null) { return 1.0; }
        return weightConfiguration.weightForIdentifier(this.getIdentifier());
    }

    /**
     * Updates the weight configurations weight for this item
     * 
     * @param weight
     */
    public void setWeight(double weight) {
        weightConfiguration.setWeightForIdentifier(this.getIdentifier(), weight);
    }

    /**
     * Set weight configuration and update weights
     * 
     * @param weightConfiguration
     */
    public void setWeightConfiguration(RiskQuestionnaireWeights weightConfiguration) {
        this.weightConfiguration = weightConfiguration;
    }

    /**
     * Returns the config
     * 
     * @return
     */
    protected RiskQuestionnaireWeights getWeightConfiguration() {
        return this.weightConfiguration;
    }
}
