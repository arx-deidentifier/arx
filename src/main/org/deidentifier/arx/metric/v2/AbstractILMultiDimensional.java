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
public abstract class AbstractILMultiDimensional extends InformationLoss<double[]> {

    /** SVUID */
    private static final long serialVersionUID = 4600789773980813693L;

    /** Values */
    private double[]          values;
    /** Weights */
    private double[]          weights;

    /**
     * Creates a new instance
     * 
     * @param values
     * @param weights
     */
    AbstractILMultiDimensional(final double[] values, final double[] weights) {
        this.values = values;
        this.weights = weights;
    }

    @Override
    public abstract InformationLoss<double[]> clone();

    @Override
    public abstract int compareTo(InformationLoss<?> other);

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (!other.getClass().equals(this.getClass())) {
            return false;
        } else {
            return compareTo((InformationLoss<?>) other) == 0;
        }
    }

    @Override
    public abstract double[] getValue();

    @Override
    public void max(final InformationLoss<?> other) {
        if (this.compareTo(other) < 0) {
            this.setValues(convert(other).getValues());
        }
    }

    @Override
    public void min(final InformationLoss<?> other) {
        if (this.compareTo(other) > 0) {
            this.setValues(convert(other).getValues());
        }
    }

    @Override
    public abstract double relativeTo(InformationLoss<?> min,
                                      InformationLoss<?> max);

    @Override
    public abstract String toString();

    /**
     * Converter method
     * 
     * @param other
     * @return
     */
    protected abstract AbstractILMultiDimensional convert(InformationLoss<?> other);

    /**
     * @return the values
     */
    protected double[] getValues() {
        return values;
    }

    /**
     * @return the weights
     */
    protected double[] getWeights() {
        return weights;
    }

    /**
     * @param values
     *            the values to set
     */
    protected void setValues(double[] values) {
        this.values = values;
    }
}
