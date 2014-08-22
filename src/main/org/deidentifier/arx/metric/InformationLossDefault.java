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

package org.deidentifier.arx.metric;

/**
 * This class implements an information loss which can be represented as
 * a single decimal number.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class InformationLossDefault extends InformationLoss<Double> {

    /** serialVersionUID */
    private static final long           serialVersionUID = -4341081298410703417L;

    /** Current value */
    private double                      value;
    
    /**
     * Creates a new instance
     * @param value
     */
    InformationLossDefault(final double value){
        super(null);
        this.value = value;
    }
    
    /**
     * Creates a new instance
     * @param value
     * @param lowerBound
     */
    InformationLossDefault(final double value, final double lowerBound) {
        super(new InformationLossDefault(lowerBound));
        this.value = value;
    }

    @Override
    public InformationLoss<Double> clone() {
        if (this.getLowerBound() != null) {
            return new InformationLossDefault(value, this.getLowerBound().getValue());
        } else {
            return new InformationLossDefault(value);
        }
    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        InformationLossDefault o = convert(other);
        if (other == null) return +1;
        else return Double.valueOf(value).compareTo(o.getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        InformationLossDefault other = (InformationLossDefault) obj;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
        return true;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void max(final InformationLoss<?> other) {
        InformationLossDefault o = convert(other);
        if (other == null) { return; }
        if (o.getValue() > getValue()) {
            value = o.getValue();
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        InformationLossDefault o = convert(other);
        if (other == null) { return; }
        if (o.getValue() < getValue()) {
            value = o.getValue();
        }
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        if (min == null) {
            throw new IllegalArgumentException("Minimum is null");
        } else if (max == null) {
            throw new IllegalArgumentException("Maximum is null");
        }
        InformationLossDefault _min = convert(min);
        InformationLossDefault _max = convert(max);
        if (_max.value - _min.value == 0d) return 0d;
        else return (this.value - _min.value) / (_max.value - _min.value);
    }

    @Override
    public String toString() {
        return Double.valueOf(this.value).toString();
    }

    /**
     * Converter method
     * @param other
     * @return
     */
    private InformationLossDefault convert(InformationLoss<?> other){
        if (other == null) return null;
        if (!(other instanceof InformationLossDefault)) {
            throw new IllegalStateException("Information loss must be of the same type");
        } else {
            return (InformationLossDefault)other;
        }
    }
}
