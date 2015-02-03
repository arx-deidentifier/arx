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
 * This class is the basis for estimating both population uniqueness and
 * equivalence class risk.
 * 
 * @author Michael Schneider
 * @version 1.0
 */

abstract class AbstractModelPopulation {

    /**
     * size of biggest equivalence class in the data set
     */
    protected int                   cMax;

    /**
     * size of smallest equivalence class in the data set
     */
    protected int                   cMin;

    /**
     * Map containing the equivalence class sizes (as keys) of the data set and
     * the corresponding frequency (as values) e.g. if the key 2 has value 3
     * then there are 3 equivalence classes of size two.
     */
    protected Map<Integer, Integer> eqClasses;

    /**
     * size of the data set aka sample
     */
    protected int                   sampleSize;

    /**
     * size of the population, this value is estimated using the sample size and
     * the sampling fraction
     */
    protected int                   populationSize;

    /**
     * sampling fraction
     */
    protected double                samplingFraction;

    /**
     * Number of equivalence classes that exist in the sample
     */
    protected int                   numberOfEquivalenceClasses;

    /**
     * Creates a model of the data set that allows for estimating the disclosure
     * risk of the data
     * 
     * @param pi
     *            sampling fraction
     * @param eqClasses
     *            Map containing the equivalence class sizes (as keys) of the
     *            data set and the corresponding frequency (as values) e.g. if
     *            the key 2 has value 3 then there are 3 equivalence classes of
     *            size two.
     */
    public AbstractModelPopulation(final double pi,
                           final Map<Integer, Integer> eqClasses) {
        this.samplingFraction = pi;
        this.eqClasses = eqClasses;
        sampleSize = 0;
        numberOfEquivalenceClasses = 0;

        // set the class attributes
        cMax = 0;
        cMin = Integer.MAX_VALUE;
        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            cMin = entry.getKey();
            sampleSize += entry.getKey() * entry.getValue();
            numberOfEquivalenceClasses += entry.getValue();
            if (entry.getKey() > cMax) {
                cMax = entry.getKey();
            }
            if (entry.getKey() < cMin) {
                cMin = entry.getKey();
            }
        }
        if (cMin == Integer.MAX_VALUE) {
            cMin = 0;
        }
        populationSize = (int) (sampleSize / this.samplingFraction);
    }

    /**
     * computes the re-identification risk on a file level for a given data set
     * and a given measurement scenario (population unique vs. equivalence class
     * based)
     * 
     * @return A single number representing the disclosure risk on a file level,
     *         depending on the model this estimate is based on population
     *         uniqueness or equivalence class size and frequency
     */
    protected abstract double computeRisk();

}
