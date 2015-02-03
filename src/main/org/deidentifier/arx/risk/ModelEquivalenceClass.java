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
 * This class allows to estimate the disclosure risk of a given data set based solely on the sample information
 * using the information about size and frequency of equivalence classes to give a worst case estimate for the disclosure risk
 * 
 * @author Michael Schneider
 * @version 1.0
 */
class ModelEquivalenceClass extends AbstractModelPopulation {

    /**
     * The equivalence class model makes a worst case disclosure risk estimation
     * for the data set as a whole based solely on the sample.
     * 
     * @param eqClasses
     *            Map containing the equivalence class sizes (as keys) of the
     *            data set and the corresponding frequency (as values) e.g. if
     *            the key 2 has value 3 then there are 3 equivalence classes of
     *            size two.
     */
    protected ModelEquivalenceClass(final Map<Integer, Integer> eqClasses) {
        super(0, eqClasses);
    }

    @Override
    protected double getRisk() {
        double result = 0;

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            result += entry.getValue();
        }

        return (result / sampleSize);
    }

}
