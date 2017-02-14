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

package org.deidentifier.arx.masking.variable;

import java.io.Serializable;

/**
 * Class describing a random variable
 *
 * @author Karol Babioch
 */
public class RandomVariable implements Serializable {

    private static final long serialVersionUID = 5890088358051823161L;

    private String name;

    private Distribution<Integer> distribution;

    public RandomVariable(String name, Distribution<Integer> distribution) {

        this.name = name;
        this.distribution = distribution;

    }

    public RandomVariable(String name) {

        this.name = name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public String getName() {

        return this.name;

    }

    public void setDistribution(Distribution<Integer> distribution) {

        this.distribution = distribution;

    }

    public Distribution<Integer> getDistribution() {

        return this.distribution;

    }

}
