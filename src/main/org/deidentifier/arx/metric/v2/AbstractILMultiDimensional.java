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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements an information loss which can be represented as a
 * decimal number per quasi-identifier.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractILMultiDimensional extends InformationLoss<double[]> {

    /** SVUID. */
    private static final long serialVersionUID = 4600789773980813693L;

    /** Values. */
    private double[]          values;
    
    /** Weights. */
    private double[]          weights;

    /**
     * Creates a new instance.
     *
     * @param values
     * @param weights
     */
    AbstractILMultiDimensional(final double[] values, final double[] weights) {
        this.values = values;
        this.weights = weights;
    }

    @Override
    public abstract InformationLoss<double[]> clone();

    @Override
    public abstract int compareTo(InformationLoss<?> other);

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (!other.getClass().equals(this.getClass())) {
            return false;
        } else {
            return compareTo((InformationLoss<?>) other) == 0;
        }
    }

    @Override
    public abstract double[] getValue();

    @Override
    public void max(final InformationLoss<?> other) {
        if (this.compareTo(other) < 0) {
            this.setValues(convert(other).getValues());
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        if (this.compareTo(other) > 0) {
            this.setValues(convert(other).getValues());
        }
    }

    @Override
    public abstract double relativeTo(InformationLoss<?> min,
                                      InformationLoss<?> max);

    @Override
    public abstract String toString();

    @Override
    protected void addMetadata(QualityMetadata<?> metadata) {
        super.addMetadata(metadata);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    protected abstract AbstractILMultiDimensional convert(InformationLoss<?> other);

    /**
     * @return the values
     */
    protected double[] getValues() {
        return values;
    }

    /**
     * @return the weights
     */
    protected double[] getWeights() {
        return weights;
    }

    /**
     * @param values
     *            the values to set
     */
    protected void setValues(double[] values) {
        this.values = values;
    }
}
