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

import java.util.ArrayList;

/**
 * Represents a section from the checklist
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaireSection extends RiskQuestionnaireItem {

    /**
     * Create a new section from a line
     * 
     * @param weightConfiguration
     * @param line
     * @return the section item
     */
    public static RiskQuestionnaireSection
            sectionFromLine(RiskQuestionnaireWeights weightConfiguration, String line) {
        line = line.trim();
        RiskQuestionnaireSection section = new RiskQuestionnaireSection(weightConfiguration, line);
        return section;
    }

    /** The items this section contains */
    private ArrayList<RiskQuestionnaireQuestion> items;

    /** The maximum weight possible */
    private double                               maximumWeight = 0.0;

    /**
     * Create a new section from a line
     * 
     * @param weightConfiguration
     *            the current weight configuration
     * @param line
     *            the line to parse
     */
    public RiskQuestionnaireSection(RiskQuestionnaireWeights weightConfiguration, String line) {
        super(line);
        this.items = new ArrayList<RiskQuestionnaireQuestion>();
        setWeightConfiguration(weightConfiguration);
    }

    /**
     * Add a question to the section and updates the maximumWeight
     * 
     * @param item
     *            the question to add
     */
    public void addItem(RiskQuestionnaireQuestion item) {
        item.setWeightConfiguration(this.getWeightConfiguration());
        items.add(item);
        maximumWeight += Math.abs(item.getWeight());
    }

    /**
     * Get all questions in this section
     * 
     * @return
     */
    public RiskQuestionnaireQuestion[] getItems() {
        return items.toArray(new RiskQuestionnaireQuestion[items.size()]);
    }

    /**
     * Returns the maximum weight
     * 
     * @return
     */
    public double getMaximumWeight() {
        return maximumWeight;
    }

    /**
     * Calculates the score based on the section's question's scores
     * 
     * @return
     */
    public double getScore() {
        if (maximumWeight == 0.0) {
            // System.out.println("This Section has a maximum weight of 0.0, it can't calculate a score: "+this);
            return 0.0;
        }
        double result = 0.0;
        for (RiskQuestionnaireQuestion q : items) {
            result += q.getScore();
        }
        // if the weight is negative, 'flip' the answers rating (yes will be -1,
        // no will be 1)
        if (this.getWeight() < 0.0) {
            result = (result * (-1.0));
        }
        result /= maximumWeight;
        return result;
    }

    @Override
    public String toString() {
        return "\n\tSection [id=" + this.getIdentifier() + ", title=" + this.getTitle() +
               ", weight=" + this.getWeight() + ", score=" + this.getScore() + ", items=" + items +
               ", config=" + this.getWeightConfiguration() + "\n\t]";
    }

    /**
     * Updates the weights and calculates the maximum weight
     */
    public void updateWeights() {
        maximumWeight = 0.0;
        for (RiskQuestionnaireQuestion item : items) {
            item.setWeightConfiguration(this.getWeightConfiguration());
            maximumWeight += Math.abs(item.getWeight());
        }
    }
}
