package org.deidentifier.arx.risk;

/**
 * This class allows to estimate the disclosure risk of a given data set based solely on the sample information
 * using the information about size and frequency of equivalence classes to give a worst case estimate for the disclosure risk
 * 
 * @author Michael Schneider
 * @version 1.0
 */

import java.util.Map;

public class EquivalenceClassModel extends PopulationModel {

    /**
     * The equivalence class model makes a worst case disclosure risk estimation for the data set as a whole based solely on the sample.
     * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
     */
    public EquivalenceClassModel(final Map<Integer, Integer> eqClasses) {
        super(0, eqClasses);
    }

    @Override
    public double computeRisk() {
        double result = 0;

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            result += entry.getValue();
        }

        return (result / n);
    }

}
