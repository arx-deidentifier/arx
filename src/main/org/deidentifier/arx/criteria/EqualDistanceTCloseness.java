package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.data.DataManager;

/**
 * The t-closeness criterion with equal-distance EMD
 * @author Fabian Prasser
 */
public class EqualDistanceTCloseness extends TCloseness {

    private static final long serialVersionUID = -1383357036299011323L;

    /** The original distribution*/
    protected double[]        distribution;

    /**
     * Creates a new instance
     * @param t
     */
    public EqualDistanceTCloseness(double t) {
        super(t);
    }

    @Override
    public void initialize(DataManager manager) {
        distribution = manager.getDistribution();
    }
}
