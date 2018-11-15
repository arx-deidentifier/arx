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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.masking.MaskingConfiguration;
import org.deidentifier.arx.masking.variable.RandomVariable;

/**
 * Model for masking variables
 *
 * @author Karol Babioch
 */
public class ModelMasking implements Serializable {

    /** SVUID */
    private static final long    serialVersionUID     = 2058280257707159023L;

    /** Masking configuration */
    private MaskingConfiguration maskingConfiguration = new MaskingConfiguration();

    /** List of random variables */
    private List<RandomVariable> randomVariables      = new ArrayList<>();

    /**
     * Adds a random variable.
     * 
     * @param randomVariable
     */
    public void addRandomVariable(RandomVariable randomVariable) {
        randomVariables.add(randomVariable);
    }

    /**
     * Returns the masking configuration.
     * @return
     */
    public MaskingConfiguration getMaskingConfiguration() {
        return maskingConfiguration;
    }

    /**
     * Returns the list of random variables.
     * @return
     */
    public List<RandomVariable> getRandomVariables() {
        return randomVariables;
    }

    /**
     * Removes a random variable.
     * @param randomVariable
     */
    public void removeRandomVariable(RandomVariable randomVariable) {
        randomVariables.remove(randomVariable);
    }

}
