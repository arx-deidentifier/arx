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
 * decimal number per quasi-identifier
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class ILMultiDimensionalRank extends AbstractILMultiDimensional {

    /** SVUID */
    private static final long serialVersionUID = 591145071792293317L;

    /** Aggregate */
    private double[]          aggregate        = null;

    /** Geometric mean */
    private double            mean             = 0d;

    /**
     * Clone constructor
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
     * Creates a new instance
     * 
     * @param values
     * @param weights
     */
    ILMultiDimensionalRank(final double[] values, final double[] weights) {
        super(values, weights);
        this.aggregate = getAggregate();
        this.mean = getMean();
    }
    

    @Override
    public InformationLoss<double[]> clone() {
        return new ILMultiDimensionalRank(mean,
                                          aggregate,
                                          getValues(),
                                          getWeights());
    }
    
    @Override
    public int compareTo(InformationLoss<?> other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument must not be null");
        } else {
            double[] otherValue = convert(other).aggregate;
            double[] thisValue = aggregate;
            for (int i = 0; i < otherValue.length; i++) {
                int cmp = compareWithTolerance(thisValue[i], otherValue[i]);
                if (cmp != 0) return cmp;
                
            }
            return 0;
        }
    }

    @Override
    public double[] getValue() {
        return this.getValues();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(aggregate);
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).mean;
        double _max = convert(max).mean;
        if (_max - _min == 0d) return 0d;
        else return (this.mean - _min) / (_max - _min);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.aggregate);
    }

    /**
     * Compares double for "equality" with a tolerance of 1 ulp
     * @param d1
     * @param d2
     * @return
     */
    private boolean closeEnough(double d1, double d2) {
        return Math.abs(d2 - d1) <= Math.max(Math.ulp(d1), Math.ulp(d2));
    }

    /**
     * Compares two doubles with tolerance
     * @param d1
     * @param d2
     * @return
     */
    private int compareWithTolerance(double d1, double d2) {
        if (closeEnough(d1, d2)) return 0;
        else return Double.compare(d1, d2);
    }

    /**
     * Implements the aggregation function
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
     * Returns the geometric mean
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
     * Sorts the array in descending order
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

    @Override
    protected void setValues(double[] values) {
        super.setValues(values);
        this.aggregate = getAggregate();
        this.mean = getMean();
    }
}
