package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements a score value in the form of an information loss,
 * with appropriate comparison semantics (i.e. higher score values are better).
 * 
 * @author Raffael Bild
 *
 */
public abstract class ILScore<T extends Comparable<T>> extends InformationLoss<T> {

    /** SVUID. */
    private static final long serialVersionUID = -2638719458508437194L;

    /** Value */
    private T                 value;

    /**
     * Creates a new instance.
     *
     * @param value
     */
    ILScore(final T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(InformationLoss<?> other) {
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                    other.getClass().getSimpleName() +
                    ")");
        }
        T otherValue = ((InformationLoss<T>)other).getValue();
        return value.compareTo(otherValue) * -1;
    }

    @Override
    public boolean equals(Object obj) {
        return equals(obj);
    }
    
    @Override
    public T getValue() {
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
    public String toString() {
        return String.valueOf(this.value);
    }

    /**
     * Converter method.
     *
     * @param other
     * @return
     */
    @SuppressWarnings("unchecked")
    protected ILScore<T> convert(InformationLoss<?> other) {
        if (other == null) return null;
        if (!other.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("Incompatible class (" +
                                               other.getClass().getSimpleName() +
                                               ")");
        } else {
            return (ILScore<T>) other;
        }
    }

    @Override
    protected void addMetadata(QualityMetadata<?> metadata) {
        super.addMetadata(metadata);
    }
}
