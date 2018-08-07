package org.deidentifier.arx.metric.v2;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements a score value in the form of an information loss,
 * with appropriate comparison semantics (i.e. higher score values are better).
 * 
 * @author Raffael Bild
 *
 */
public class ILScore extends InformationLoss<BigFraction> {

    /** SVUID. */
    private static final long serialVersionUID = -2638719458508437194L;

    /** Value */
    private BigFraction                 value;

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
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                    other.getClass().getSimpleName() +
                    ")");
        }
        BigFraction otherValue = ((ILScore)other).getValue();
        return value.compareTo(otherValue) * -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
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
        return String.valueOf(this.value);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    protected ILScore convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (ILScore)other;
        }
    }

    @Override
    protected void addMetadata(QualityMetadata<?> metadata) {
        super.addMetadata(metadata);
    }
}
