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
public abstract class AbstractILMultiDimensionalReduced extends AbstractILMultiDimensional {

    /** SVUID. */
    private static final long serialVersionUID = 7228258212711188233L;

    /** Aggregate. */
    private double            aggregate        = 0d;

    /**
     * Creates a new instance.
     *
     * @param values
     * @param weights
     */
    AbstractILMultiDimensionalReduced(final double[] values, final double[] weights) {
        super(values, weights);
        this.aggregate = getAggregate();
    }

    /**
     * Override this to implement a variant.
     *
     * @return
     */
    public abstract InformationLoss<double[]> clone();

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#compareTo(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public int compareTo(InformationLoss<?> other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument must not be null");
        } else {
            double otherValue = convert(other).aggregate;
            double thisValue = aggregate;
            return thisValue == otherValue ? 0 : (thisValue < otherValue ? -1
                    : +1);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#getValue()
     */
    @Override
    public double[] getValue() {
        return this.getValues();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#hashCode()
     */
    @Override
    public int hashCode() {
        return Double.valueOf(aggregate).hashCode();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#relativeTo(org.deidentifier.arx.metric.InformationLoss, org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).aggregate;
        double _max = convert(max).aggregate;
        if (_max - _min == 0d) return 0d;
        else return (this.aggregate - _min) / (_max - _min);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.aggregate);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    protected AbstractILMultiDimensionalReduced convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class. Should ("+
                                               this.getClass().getSimpleName()
                                               +") but is (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (AbstractILMultiDimensionalReduced) other;
        }
    }

    /**
     * Override this to implement a variant.
     *
     * @return
     */
    protected abstract double getAggregate();

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#setValues(double[])
     */
    @Override
    protected void setValues(double[] values) {
        super.setValues(values);
        this.aggregate = getAggregate();
    }
}
