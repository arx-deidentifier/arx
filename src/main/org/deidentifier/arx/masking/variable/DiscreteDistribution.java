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

package org.deidentifier.arx.masking.variable;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;

/**
 * Implementation of a discrete distribution
 *
 * @author Fabian Kloos
 * @author Fabian Prasser
 * @author Karol Babioch
 */
public class DiscreteDistribution extends Distribution<Integer> {

    private int min;
    private int max;
    private AbstractIntegerDistribution distribution;

    public DiscreteDistribution(int min, int max, AbstractIntegerDistribution distribution) {

        super(0d);

        this.min = min;
        this.max = max;
        this.distribution = distribution;

    }

    @Override
    public Integer getMinimum() {

        return min;

    }

    @Override
    public Integer getMaximum() {

        return max;

    }

    @Override
    public double getValue(Integer value) {

        return distribution.probability(value);

    }

}
