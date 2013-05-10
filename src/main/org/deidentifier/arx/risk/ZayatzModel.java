package org.deidentifier.arx.risk;

/**
 * This class implements the ZayatzModel based on Equivalence Class, for details see the paper
 * ESTIMATION OF THE NUMBER OF UNIQUE POPULATION ELEMENTS USING A SAMPLE, Zayatz, 1991
 * 
 * @author Michael Schneider
 * @version 1.0
 */

import java.util.Map;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

public class ZayatzModel extends UniquenessModel {

    /**
     * Zayatz model, based on Zayatz, 1991
     * @param pi sampling fraction
     * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
     */
    public ZayatzModel(final double Pi, final Map<Integer, Integer> eqClasses) {
        super(Pi, eqClasses);
    }

    /**
     * 
     * @return estimate of the the probability that an equivalence class
     * of size 1 in the sample was chosen from an equivalence class of size 1 in the population 
     */
    public double computeConditionalUniquenessPercentage() {
        double temp = 0;

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            final HypergeometricDistribution distribution = new HypergeometricDistribution(N, entry.getKey(), n);
            temp += (entry.getValue() / ((double) u)) * distribution.probability(1);
        }

        final HypergeometricDistribution distribution = new HypergeometricDistribution(N, 1, n);
        final double probCond = ((eqClasses.get(1) / ((double) u)) * (distribution.probability(1))) / temp;

        return probCond;
    }

    /**
     * 
     * @return estimate of the total number of sample uniques that are also population uniques
     */
    public double computeConditionalUniquenessTotal() {
        return (eqClasses.get(1) * computeConditionalUniquenessPercentage());
    }

    @Override
    public double computeRisk() {
        return (computeConditionalUniquenessTotal() / n);
    }

    @Override
    public double computeUniquenessTotal() {
        final double condUniqPercentage = computeConditionalUniquenessPercentage();
        return ((eqClasses.get(1) * condUniqPercentage) / pi);
    }

}
