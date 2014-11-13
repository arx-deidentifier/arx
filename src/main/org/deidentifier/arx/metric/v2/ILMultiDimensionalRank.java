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

import java.util.Arrays;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements an information loss which can be represented as a
 * decimal number per quasi-identifier.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ILMultiDimensionalRank extends AbstractILMultiDimensional {

    /** SVUID. */
    private static final long   serialVersionUID = 591145071792293317L;

    /** Aggregate. */
    private double[]            aggregate        = null;

    /** Geometric mean. */
    private double              mean             = 0d;

    /**
     * Clone constructor.
     *
     * @param mean
     * @param aggregate
     * @param values
     * @param weights
     */
    private ILMultiDimensionalRank(final double mean,
                                   final double[] aggregate,
                                   final double[] values,
                                   final double[] weights) {
        super(values, weights);
        this.mean = mean;
        this.aggregate = aggregate;
    }

    /**
     * Creates a new instance.
     *
     * @param values
     * @param weights
     */
    ILMultiDimensionalRank(final double[] values, final double[] weights) {
        super(values, weights);
        this.aggregate = getAggregate();
        this.mean = getMean();
    }
    

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#clone()
     */
    @Override
    public InformationLoss<double[]> clone() {
        return new ILMultiDimensionalRank(mean,
                                          aggregate,
                                          getValues(),
                                          getWeights());
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#compareTo(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public int compareTo(InformationLoss<?> other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument must not be null");
        } else {
            double[] otherValue = convert(other).aggregate;
            double[] thisValue = aggregate;
            for (int i = 0; i < otherValue.length; i++) {
                int cmp = Double.compare(thisValue[i], otherValue[i]);
                if (cmp != 0) return cmp;
                
            }
            return 0;
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
        return Arrays.hashCode(this.aggregate);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#relativeTo(org.deidentifier.arx.metric.InformationLoss, org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        
        // TODO: Fix this crap
        double _min = convert(min).mean;
        double _max = convert(max).mean;
        if (_max - _min == 0d) return 0d;
        double result = (this.mean - _min) / (_max - _min);
        return result < 0d ? 0d : (result > 1d ? 1d : result);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(this.aggregate);
    }

    /**
     * Implements the aggregation function.
     *
     * @return
     */
    private double[] getAggregate() {
        double[] values = getValues();
        double[] weights = getWeights();
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i] * weights[i];
        }
        sortDescending(result);
        return result;
    }

    /**
     * Returns the geometric mean.
     *
     * @return
     */
    private double getMean() {
        double[] values = getValues();
        double result = 1.0d;
        for (int i = 0; i < values.length; i++) {
            result *= Math.pow(values[i], 1.0d / (double) values.length);
        }
        return result;
    }

    /**
     * Sorts the array in descending order.
     *
     * @param value
     */
    private void sortDescending(double[] value) {
        Arrays.sort(value);
        for (int i = 0; i < value.length / 2; i++) {
            int other = value.length - (i + 1);
            double temp = value[i];
            value[i] = value[other];
            value[other] = temp;
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#convert(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    protected ILMultiDimensionalRank convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (ILMultiDimensionalRank) other;
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractILMultiDimensional#setValues(double[])
     */
    @Override
    protected void setValues(double[] values) {
        super.setValues(values);
        this.aggregate = getAggregate();
        this.mean = getMean();
    }
}
