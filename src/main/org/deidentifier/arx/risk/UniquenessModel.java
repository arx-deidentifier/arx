package org.deidentifier.arx.risk;

/**
 * This class abstracts the different population uniqueness scenarios
 * 
 * @author Michael Schneider
 * @version 1.0
 */

import java.util.Map;

public abstract class UniquenessModel extends PopulationModel {

    /**
     * Model based on the number of population uniques, estimating the population based on the sample
     * @param pi sampling fraction
     * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
     */
    public UniquenessModel(final double pi, final Map<Integer, Integer> eqClasses) {
        super(pi, eqClasses);
    }

    /**
     * @return Population Uniqueness estimate as total number of individuals in a population
     *
     */
    abstract public double computeUniquenessTotal();

}
