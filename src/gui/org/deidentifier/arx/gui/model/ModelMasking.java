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

    private static final long serialVersionUID = 2058280257707159023L;

    private List<RandomVariable> randomVariables = new ArrayList<>();

    private MaskingConfiguration maskingConfiguration = new MaskingConfiguration();

    public MaskingConfiguration getMaskingConfiguration() {

        return maskingConfiguration;

    }

    public void addRandomVariable(RandomVariable randomVariable) {

        randomVariables.add(randomVariable);

    }

    public void removeRandomVariable(RandomVariable randomVariable) {

        randomVariables.remove(randomVariable);

    }

    public List<RandomVariable> getRandomVariables() {

        return randomVariables;

    }

}
