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
 * decimal number per quasi-identifier.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ILMultiDimensionalArithmeticMean extends AbstractILMultiDimensionalReduced {

    /** SVUID. */
    private static final long serialVersionUID = 5142553922311764185L;
   
    /**
     * Creates a new instance. For backwards compatibility
     * 
     * @param value
     */
    ILMultiDimensionalArithmeticMean(double value) {
        super(new double[]{value}, new double[]{1.0d});
    }

    /**
     * Creates a new instance.
     *
     * @param values
     * @param weights
     */
    ILMultiDimensionalArithmeticMean(final double[] values,
                                     final double[] weights) {
        super(values, weights);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensionalReduced#clone()
     */
    @Override
    public InformationLoss<double[]> clone() {
        return new ILMultiDimensionalArithmeticMean(getValues(),
                                                    getWeights());
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensionalReduced#getAggregate()
     */
    @Override
    protected double getAggregate() {
        double[] values = getValues();
        double[] weights = getWeights();
        double result = 0d;
        for (int i = 0; i < values.length; i++) {
            result += (values[i] / (double) values.length) * weights[i];
        }
        return result;
    }
}
