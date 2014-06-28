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

    /** The precision in number of digits per attribute */
    private static final int    PRECISION        = 6;

    /** The precision in number of digits in the string representation */
    private static final int    PRECISION_STRING = 2;

    /** The precision as a multiplier */
    private static final double MULTIPLIER       = Math.pow(10, PRECISION);

    /** The precision as a multiplier */
    private static final double MULTIPLIER_STRING = Math.pow(10, PRECISION_STRING);

    private static final long   serialVersionUID = -602019669402453406L;

    /** The integer representation */
    private BigInteger    ints;

    /** double representation */
    private double        perc;

    /** String representation */
    private String        string;

    /** Value */
    private double[]      value;
    
    /**
     * Clone constructor
     * @param value
     * @param ints
     * @param string
     * @param perc
     */
    private InformationLossRCE(final double[] value, final BigInteger ints, final String string, final double perc) {
        this.value = value;
        this.ints = ints;
        this.string = string;
        this.perc = perc;
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
            while (sval.length() < (PRECISION + 1)) {
                sval.insert(0, "0");
            }
            digits.append(sval);
        }
        this.ints = new BigInteger(digits.toString());
        
        // Create string representation
        digits.setLength(0);
        digits.append("[");
        for (double v : value) {
            long ival = (long)Math.round(v * MULTIPLIER_STRING);
            digits.append(ival);
            digits.append(",");
        }
        digits.setCharAt(digits.length()-1, ']');
        this.string = digits.toString();
        
        // 70% percentile
        double mean = getMean(value);
        this.perc = mean + getStandardDeviation(mean, value);
    }

    @Override
    public InformationLoss<double[]> clone() {
        return new InformationLossRCE(value, ints, string, perc);
    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) return +1;
        else return ints.compareTo(o.ints);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public void max(final InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) { return; }
        if (o.compareTo(this)>0) {
            value = o.value;
            ints = o.ints;
            string = o.string;
            perc = o.perc;
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        InformationLossRCE o = convert(other);
        if (other == null) { return; }
        if (o.compareTo(this)<0) {
            value = o.value;
            ints = o.ints;
            string = o.string;
            perc = o.perc;
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
        
        // TODO
//      BigDecimal a = new BigDecimal(this.ints.subtract(_min.ints));
//      BigDecimal b = new BigDecimal(_max.ints.subtract(_min.ints));
//      return a.divide(b, MathContext.DECIMAL64).doubleValue();
        return (perc - _min.perc) / (_max.perc - _min.perc);
    }

    @Override
    public String toString() {
        return string;
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
    
    /**
     * Returns the arithmetic mean of the given values
     * @param values
     * @return
     */
    private double getMean(double... values) {

        double mean = 0d;
        for (int i=0; i<values.length; i++){
            mean += values[i];
        }
        mean /= (double)values.length;
        return mean;
    }

    /**
     * Returns the standard deviation of the given values
     * @param values
     * @return
     */
    private double getStandardDeviation(double mean, double... values) {
        double dev = 0;
        for (int i=0; i<values.length; i++){
            dev += Math.pow(values[i] - mean, 2.0d);
        }
        
        return Math.sqrt(dev / (double)values.length);
    }
}
