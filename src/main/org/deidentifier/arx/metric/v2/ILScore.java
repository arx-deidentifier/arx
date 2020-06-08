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

package org.deidentifier.arx.metric.v2;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements information loss using score values for data-independent
 * differential privacy with appropriate comparison semantics
 * (i.e. higher score values are better).
 * 
 * @author Raffael Bild
 *
 */
public class ILScore extends InformationLoss<BigFraction> {

    /** SVUID. */
    private static final long serialVersionUID = -2638719458508437194L;

    /** Value */
    private BigFraction       value            = null;

    /**
     * Creates a new instance.
     *
     * @param value
     */
    ILScore(final BigFraction value) {
        this.value = value;
    }
    
    @Override
    public InformationLoss<BigFraction> clone() {
        return new ILScore(getValue());
    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        BigFraction otherValue = ((ILScore)other).getValue();
        return value.compareTo(otherValue) * -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BigFraction otherValue = ((ILScore)obj).getValue();
        return this.value.equals(otherValue);
    }
    
    @Override
    public BigFraction getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
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
        BigFraction _min = convert(min).getValue();
        BigFraction _max = convert(max).getValue();
        if (_max.subtract(_min).equals(new BigFraction(0))) return 0d;
        else return (getValue().subtract(_min)).divide(_max.subtract(_min)).doubleValue();
    }

    @Override
    public String toString() {
        return String.valueOf(this.value.doubleValue());
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    protected ILScore convert(InformationLoss<?> other) {
        if (other == null) return null;
        return (ILScore)other;
    }

    @Override
    protected void addMetadata(QualityMetadata<?> metadata) {
        super.addMetadata(metadata);
    }
}
