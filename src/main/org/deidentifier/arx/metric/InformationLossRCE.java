/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * This class implements an information loss which is represented as a combination
 * of individual losses for each attribute. Instances are compared by Repeated
 * Conservative Estimation (RCE).
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class InformationLossRCE extends InformationLoss<double[]> {

    private static final long   serialVersionUID = -602019669402453406L;

    /** The precision in number of digits per attribute */
    private static final int    PRECISION        = 6;

    /** The precision as a multiplier */
    private static final double MULTIPLIER       = 10 ^ PRECISION;

    /** The integer representation */
    private BigInteger          ints;

    /** Current value */
    private double[]            value;

    /**
     * Clone constructor
     * @param value
     * @param ints
     */
    InformationLossRCE(final double[] value, final BigInteger ints) {
        this.value = value;
        this.ints = ints;
    }
    
    /**
     * Creates a new instance
     * @param value
     */
    InformationLossRCE(final double[] value) {
        
        // Create sorted array in descending order
        this.value = value;
        Arrays.sort(value);
        for (int i = 0; i < value.length / 2; i++) {
            int other = value.length - (i + 1);
            double temp = value[i];
            value[i] = value[other];
            value[other] = temp;
        }
        
        // Create integer representation
        StringBuilder digits = new StringBuilder();
        for (double v : value) {
            long ival = (long)Math.round(v * MULTIPLIER);
            StringBuilder sval = new StringBuilder();
            sval.append(ival);
            while (sval.length() != PRECISION) {
                sval.insert(0, "0");
            }
            digits.append(sval);
        }
        this.ints = new BigInteger(digits.toString());
    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) return +1;
        else return ints.compareTo(o.ints);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        InformationLossRCE other = (InformationLossRCE) obj;
        if (!Arrays.equals(value, other.value)) return false;
        return true;
    }

    @Override
    public double[] getValue() {
        return value;
    }

    @Override
    public void max(final InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) { return; }
        if (o.compareTo(this)>0) {
            value = o.value;
            ints = o.ints;
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) { return; }
        if (o.compareTo(this)<0) {
            value = o.value;
            ints = o.ints;
        }
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        if (min == null) {
            throw new IllegalArgumentException("Minimum is null");
        } else if (max == null) {
            throw new IllegalArgumentException("Maximum is null");
        }
        InformationLossRCE _min = convert(min);
        InformationLossRCE _max = convert(max);
        
        BigDecimal a = new BigDecimal(this.ints.subtract(_min.ints), PRECISION);
        BigDecimal b = new BigDecimal(_max.ints.subtract(_min.ints), PRECISION);
        return a.divide(b).doubleValue();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.value);
    }

    /**
     * Converter method
     * @param other
     * @return
     */
    private InformationLossRCE convert(InformationLoss<?> other){
        if (other == null) return null;
        if (!(other instanceof InformationLossRCE)) {
            throw new IllegalStateException("Information loss must be of the same type");
        } else {
            return (InformationLossRCE)other;
        }
    }

    @Override
    protected InformationLoss<double[]> clone() {
        return new InformationLossRCE(value, ints);
    }
}
