/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements an information loss which can be represented as a
 * decimal number per quasi-identifier
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractILMultiDimensionalReduced extends AbstractILMultiDimensional {

    /** SVUID */
    private static final long serialVersionUID = 7228258212711188233L;

    /** Aggregate */
    private double            aggregate        = 0d;

    /**
     * Creates a new instance
     * 
     * @param values
     * @param weights
     */
    AbstractILMultiDimensionalReduced(final double[] values, final double[] weights) {
        super(values, weights);
        this.aggregate = getAggregate();
    }

    /**
     * Override this to implement a variant
     */
    public abstract InformationLoss<double[]> clone();

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

    @Override
    public double[] getValue() {
        return this.getValues();
    }

    @Override
    public int hashCode() {
        return Double.valueOf(aggregate).hashCode();
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).aggregate;
        double _max = convert(max).aggregate;
        if (_max - _min == 0d) return 0d;
        else return (this.aggregate - _min) / (_max - _min);
    }

    @Override
    public String toString() {
        return String.valueOf(this.aggregate);
    }

    /**
     * Converter method
     * 
     * @param other
     * @return
     */
    protected AbstractILMultiDimensionalReduced convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (AbstractILMultiDimensionalReduced) other;
        }
    }

    /**
     * Override this to implement a variant
     * 
     * @return
     */
    protected abstract double getAggregate();

    @Override
    protected void setValues(double[] values) {
        super.setValues(values);
        this.aggregate = getAggregate();
    }
}
