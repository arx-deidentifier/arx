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

import java.io.IOException;

/**
 * This is the base class for the Question as well as the Section.
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public abstract class RiskQuestionnaireItem {

    /** Identifier (used for the weight configuration) */
    private String identifier = null;

    /** Item's title */
    private String title      = null;

    /** Weight */
    private double weight     = 1d;
    
    /**
     * Creates a new instance
     * @param line
     * @throws IOException
     */
    public RiskQuestionnaireItem(String line) throws IOException {
        parse(line);
    }

    /**
     * Parse the item
     * @param line
     * @throws IOException 
     */
    private void parse(String line) throws IOException {
        if (line == null) {
            throw new IOException("Invalid questionnaire definition: " + line);
        }
        String components[] = line.split(":", 2);
        if (components == null ||components.length != 2) {
            throw new IOException("Invalid questionnaire definition: " + line);
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
        return this.weight;
    }

    /**
     * Updates the weight configurations weight for this item
     * 
     * @param weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
