package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements a score value in the form of an information loss implemented using the Double class
 * 
 * @author Raffael Bild
 *
 */
public class ILScoreDouble extends ILScore<Double> {

    /** SVUID. */
    private static final long serialVersionUID = -3147729786197441688L;

    /**
     * Creates a new instance.
     *
     * @param value
     */
    public ILScoreDouble(Double value) {
        super(value);
    }

    @Override
    public InformationLoss<Double> clone() {
        return new ILScoreDouble(getValue());
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        Double _min = convert(min).getValue();
        Double _max = convert(max).getValue();
        if (_max - _min == 0d) return 0d;
        else return (this.getValue() - _min) / (_max - _min);
    }
}
