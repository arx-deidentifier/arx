package org.deidentifier.arx.metric.v2;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * This class implements a score value in the form of an information loss implemented using the BigFraction class
 * 
 * @author Raffael Bild
 *
 */
public class ILScoreBigFraction extends ILScore<BigFraction> {

    /** SVUID. */
    private static final long serialVersionUID = -4704788809798088461L;

    /**
     * Creates a new instance.
     *
     * @param value
     */
    public ILScoreBigFraction(BigFraction value) {
        super(value);
    }

    @Override
    public InformationLoss<BigFraction> clone() {
        return new ILScoreBigFraction(getValue());
    }

    @Override
    public double relativeTo(InformationLoss<?> min, InformationLoss<?> max) {
        BigFraction _min = convert(min).getValue();
        BigFraction _max = convert(max).getValue();
        if (_max.subtract(_min).equals(new BigFraction(0))) return 0d;
        else return (getValue().subtract(_min)).divide(_max.subtract(_min)).doubleValue();
    }
}
