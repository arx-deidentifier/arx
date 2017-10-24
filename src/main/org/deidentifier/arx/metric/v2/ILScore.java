package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements a score value in the form of an information loss,
 * with appropriate conparison semantics (i.e. higher score values are better).
 * 
 * @author Raffael Bild
 *
 */
public class ILScore extends ILSingleDimensional {

    /** SVUID. */
    private static final long serialVersionUID = -2638719458508437194L;
    
    /**
     * Creates a new instance.
     *
     * @param value
     */
    ILScore(final double value) {
        super(value);
    }
    
    @Override
    public InformationLoss<Double> clone() {
        return new ILScore(getValue());

    }

    @Override
    public int compareTo(InformationLoss<?> other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument must not be null");
        } else {
            double otherValue = convert(other).getValue();
            double thisValue = getValue();
            return thisValue == otherValue ? 0 : (thisValue < otherValue ? +1 : -1);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ILScore other = (ILScore) obj;
        if (Double.doubleToLongBits(getValue()) != Double.doubleToLongBits(other.getValue())) return false;
        return true;
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        double _min = convert(min).getValue();
        double _max = convert(max).getValue();
        if (_min - _max == 0d) return 0d;
        else return (this.getValue() - _max) / (_min - _max);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    private ILScore convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (ILScore) other;
        }
    }
}
