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

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.AbstractRealDistribution;

/**
 * Helper wrapping a real distribution, so it can be used with discrete distribution
 *
 * This calculates the probability of a continuous distribution between two discrete points by subtracting the
 * cumulative probability of these points.
 *
 * @author Fabian Kloos
 * @author Fabian Prasser
 * @author Karol Babioch
 */
public class WrappedRealDistribution extends AbstractIntegerDistribution {

    private static final long serialVersionUID = -4186523316916292945L;

    private final AbstractRealDistribution distribution;

    @SuppressWarnings("deprecation")
    public WrappedRealDistribution(AbstractRealDistribution distribution) {

        this.distribution = distribution;

    }

    @Override
    public double cumulativeProbability(int arg0) {

        return this.distribution.cumulativeProbability(arg0);

    }

    @Override
    public double getNumericalMean() {

        return this.distribution.getNumericalMean();

    }

    @Override
    public double getNumericalVariance() {

        return this.distribution.getNumericalVariance();

    }

    @Override
    public int getSupportLowerBound() {

        return (int) Math.floor(this.distribution.getSupportLowerBound());

    }

    @Override
    public int getSupportUpperBound() {

        return (int) Math.ceil(this.distribution.getSupportUpperBound());

    }

    @Override
    public boolean isSupportConnected() {

        return this.distribution.isSupportConnected();

    }

    @Override
    public double probability(int arg0) {

        return (this.distribution.cumulativeProbability(arg0 + 1 - Double.MIN_VALUE) - this.distribution.cumulativeProbability(arg0 - Double.MIN_VALUE));

    }

}
