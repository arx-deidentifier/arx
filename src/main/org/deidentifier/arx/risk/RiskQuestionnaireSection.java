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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a section from the checklist
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaireSection extends RiskQuestionnaireItem implements Serializable {

    /** SVUID*/
    private static final long serialVersionUID = -6574573674768245518L;
    
    /** The items this section contains */
    private List<RiskQuestionnaireQuestion> items = new ArrayList<>();

    /**
     * Create a new section from a line
     * 
     * @param weightConfiguration the current weight configuration
     * @param line the line to parse
     * @throws IOException 
     */
    public RiskQuestionnaireSection(String line) throws IOException {
        super(line.trim());
        this.items = new ArrayList<RiskQuestionnaireQuestion>();
    }

    /**
     * Add a question to the section and updates the maximumWeight
     * 
     * @param item the question to add
     */
    public void addItem(RiskQuestionnaireQuestion item) {
        items.add(item);
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
     * Calculates the score based on the section's question's scores
     * 
     * @return
     */
    public double getScore() {
        
        double result = 0d;
        double max = 0d;
        for (RiskQuestionnaireQuestion q : items) {
            result += q.getScore();
            max += q.getWeight();
        }
        result /= max;
        return result;
    }
}
