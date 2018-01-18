/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.metric;


/**
 * This class implements an information loss which can be represented as
 * a single decimal number.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class InformationLossDefault extends InformationLoss<Double> {

    /** SVUID */
    private static final long           serialVersionUID = -4341081298410703417L;

    /** Current value. */
    private double                      value;
    
    /**
     * Creates a new instance.
     *
     * @param value
     */
    InformationLossDefault(final double value){
        this.value = value;
    }
    
    @Override
    public InformationLoss<Double> clone() {
        return new InformationLossDefault(value);
    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        InformationLossDefault o = convert(other);
        if (other == null) return +1;
        else return Double.valueOf(value).compareTo(o.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
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
    public int hashCode() {
        return Double.valueOf(value).hashCode();
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
     * Converter method.
     *
     * @param other
     * @return
     */
    private InformationLossDefault convert(InformationLoss<?> other){
        if (other == null) return null;
        if (!(other instanceof InformationLossDefault)) {
            throw new IllegalStateException("Information loss must be of the same type. This: " +
                                            this.getClass().getSimpleName() +
                                            ". Other: " +
                                            other.getClass().getSimpleName());
        } else {
            return (InformationLossDefault)other;
        }
    }
}