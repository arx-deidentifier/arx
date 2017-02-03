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

import org.deidentifier.arx.gui.resources.Resources;

/**
 * Represents a single question from the checklist
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaireQuestion extends RiskQuestionnaireItem implements Serializable {

    /** SVUID*/
    private static final long serialVersionUID = 1342060103957413041L;

    /** Enum for answers */
    public enum Answer {
        YES,
        NO,
        N_A
    }

    /** Current answer */
    public Answer                    answer;

    /**
     * Creates a new question in a section, from a line
     * 
     * @param section
     * @param line
     * @throws IOException 
     */
    public RiskQuestionnaireQuestion(String line) throws IOException {
        super(line.trim());
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
}
