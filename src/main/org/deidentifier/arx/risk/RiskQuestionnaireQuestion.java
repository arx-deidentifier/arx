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

import org.deidentifier.arx.gui.resources.Resources;

/**
 * Represents a single question from the checklist
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaireQuestion extends RiskQuestionnaireItem {

    /** Enum for answers */
    public enum Answer {
        YES,
        NO,
        N_A
    }

    /**
     * Creates a new question from a line, in a section
     * 
     * @param section
     * @param line
     * @return the question item
     */
    public static RiskQuestionnaireQuestion itemFromLine(String line) {
        line = line.trim();
        RiskQuestionnaireQuestion item = new RiskQuestionnaireQuestion(section, line);
        return item;
    }

    /** Current answer */
    public Answer                    answer;

    /**
     * Creates a new question in a section, from a line
     * 
     * @param section
     * @param line
     */
    public RiskQuestionnaireQuestion(RiskQuestionnaireSection section, String line) {
        super(line);
        this.section = section;
        this.answer = Answer.N_A;
    }

    /**
     * Get the current answer as a string
     * 
     * @return the string
     */
    public String getAnswerString() {
        switch (answer) {
        case YES:
            return Resources.getMessage("RiskWizard.3");
        case NO:
            return Resources.getMessage("RiskWizard.4");
        default:
            return Resources.getMessage("RiskWizard.5");
        }
    }

    /**
     * Returns the identifier
     */
    @Override
    public String getIdentifier() {
        return section.getIdentifier() + "." + identifier;
    }

    /**
     * Returns the question's score using the weight and taking the answer into
     * account
     * 
     * @return the score
     */
    public double getScore() {
        switch (answer) {
        case YES:
            return this.getWeight();
        case NO:
            return -this.getWeight();
        default:
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return "\n\t\tQuestion [id=" + this.getIdentifier() + ", answer=" + this.getAnswerString() +
               ", weight=" + this.getWeight() + ", title=" + title + "]";
    }
}
