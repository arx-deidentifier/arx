/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.util.Map;

/**
 * This class abstracts the different population uniqueness scenarios
 * 
 * @author Michael Schneider
 * @version 1.0
 */
abstract class AbstractModelUniqueness extends AbstractModelPopulation {

    /**
     * Model based on the number of population uniques, estimating the
     * population based on the sample
     * 
     * @param pi
     *            sampling fraction
     * @param eqClasses
     *            Map containing the equivalence class sizes (as keys) of the
     *            data set and the corresponding frequency (as values) e.g. if
     *            the key 2 has value 3 then there are 3 equivalence classes of
     *            size two.
     */
    public AbstractModelUniqueness(final double pi,
                           final Map<Integer, Integer> eqClasses) {
        super(pi, eqClasses);
    }

    /**
     * @return Population Uniqueness estimate as total number of individuals in a population
     * 
     */
    protected abstract double getPopulationUniques();

}
