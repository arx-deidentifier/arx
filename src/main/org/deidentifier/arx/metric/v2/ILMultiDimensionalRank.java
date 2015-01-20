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

import java.io.IOException;
import java.io.ObjectInputStream;
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
     * Returns the geometric mean. Handles zero values by adding 1 to each component
     * and subtracting 1 from the result.
     *
     * @return
     */
    private double getMean() {
        double[] values = getValues();
        double result = 1.0d;
        for (int i = 0; i < values.length; i++) {
            result *= Math.pow(values[i] + 1.0d, 1.0d / (double) values.length);
        }
        return result - 1d;
    }

    /**
     * Overwritten to handle changes in how the mean is computed.
     * 
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream stream) throws IOException,
                                                     ClassNotFoundException {
        stream.defaultReadObject();
        mean = getMean();
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
