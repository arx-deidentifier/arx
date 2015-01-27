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
 * single decimal number.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ILSingleDimensional extends InformationLoss<Double> {

    /** SVUID. */
    private static final long serialVersionUID = 8045076435539841773L;
    
    /** Values. */
    private double            value;

    /**
     * Creates a new instance.
     *
     * @param value
     */
    ILSingleDimensional(final double value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#clone()
     */
    @Override
    public InformationLoss<Double> clone() {
        return new ILSingleDimensional(value);

    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#compareTo(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public int compareTo(InformationLoss<?> other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument must not be null");
        } else {
            double otherValue = convert(other).value;
            double thisValue = value;
            return thisValue == otherValue ? 0 : (thisValue < otherValue ? -1
                    : +1);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ILSingleDimensional other = (ILSingleDimensional) obj;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#getValue()
     */
    @Override
    public Double getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#max(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public void max(final InformationLoss<?> other) {
        if (this.compareTo(other) < 0) {
            this.value = convert(other).value;
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#min(org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public void min(final InformationLoss<?> other) {
        if (this.compareTo(other) > 0) {
            this.value = convert(other).value;
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#relativeTo(org.deidentifier.arx.metric.InformationLoss, org.deidentifier.arx.metric.InformationLoss)
     */
    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).value;
        double _max = convert(max).value;
        if (_max - _min == 0d) return 0d;
        else return (this.value - _min) / (_max - _min);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.InformationLoss#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    private ILSingleDimensional convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (ILSingleDimensional) other;
        }
    }

}
