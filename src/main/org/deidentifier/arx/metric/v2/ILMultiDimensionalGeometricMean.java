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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements an information loss which can be represented as a
 * decimal number per quasi-identifier. As an aggregate function, the geometric mean
 * is applied. To handle zero values while not violating guarantees required for pruning
 * based on lower bounds, 1d is added to every individual value and 1d is subtracted from the
 * final result.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ILMultiDimensionalGeometricMean extends AbstractILMultiDimensionalReduced {

    /** SVUID. */
    private static final long serialVersionUID = 621501985571033348L;

    /**
     * Creates a new instance.
     *
     * @param values
     * @param weights
     */
    ILMultiDimensionalGeometricMean(final double[] values,
                                    final double[] weights) {
        super(values, weights);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensionalReduced#clone()
     */
    @Override
    public InformationLoss<double[]> clone() {
        return new ILMultiDimensionalGeometricMean(getValues(),
                                                   getWeights());
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensionalReduced#getAggregate()
     */
    @Override
    protected double getAggregate() {
        double[] values = getValues();
        double[] weights = getWeights();
        double result = 1.0d;
        for (int i = 0; i < values.length; i++) {
            result *= Math.pow((values[i] + 1d) * weights[i], 1.0d / (double) values.length);
        }
        return result - 1d;
    }
}
