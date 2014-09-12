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
 * single decimal number
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ILSingleDimensional extends InformationLoss<Double> {

    /** SVUID */
    private static final long serialVersionUID = 8045076435539841773L;
    /** Values */
    private double            value;

    /**
     * Creates a new instance
     * 
     * @param value
     */
    ILSingleDimensional(final double value) {
        this.value = value;
    }

    @Override
    public InformationLoss<Double> clone() {
        return new ILSingleDimensional(value);

    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ILSingleDimensional other = (ILSingleDimensional) obj;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
        return true;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void max(final InformationLoss<?> other) {
        if (this.compareTo(other) < 0) {
            this.value = convert(other).value;
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        if (this.compareTo(other) > 0) {
            this.value = convert(other).value;
        }
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).value;
        double _max = convert(max).value;
        if (_max - _min == 0d) return 0d;
        else return (this.value - _min) / (_max - _min);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    /**
     * Converter method
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
