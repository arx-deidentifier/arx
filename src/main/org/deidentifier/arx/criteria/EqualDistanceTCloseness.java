package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
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

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        // calculate emd with equal distance
        final int[] calcArray = new int[distribution.length];

        int[] buckets = entry.distribution.getBuckets();
        int totalElements = 0;
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                final int value = buckets[i];
                final int frequency = buckets[i + 1];
                calcArray[value] = frequency;
                totalElements += frequency;
            }
        }

        double val = 0d;

        for (int i = 0; i < calcArray.length; i++) {
            val += Math.abs((distribution[i] - ((double) calcArray[i] / (double) totalElements)));
        }

        val /= 2;

        // check
        return val <= t;
    }
}
