package org.deidentifier.arx.algorithm.transactions;

public class Metrics {
    /**
     * @param item the item that is generalized
     * @param g    the generalization that item is generalized to
     * @param h    the generalization hierarchy which generalization belongs to
     * @return the information loss when applying generalization item -> g
     */
    private static double NCP(int item, int g, Hierarchy h) {
        double leafCount;

        if (!h.generalizes(item, g))
            return 0;

        leafCount = h.getLeafsUnderGeneralization(g);

        if (leafCount == 1)
            return 0;
        else
            return leafCount / h.getDomainItems().length;
    }

    /**
     * @param g                  the cut for which the information loss is computed
     * @param h                  the generalization hierarchy which g belongs to
     * @param itemFrequencies    the count of each item in the database
     * @param itemFrequenciesSum the sum of itemFrequencies
     * @return the information loss inflicted when applying cut g on a database where itemFrequencies is generated from
     */
    public static double NCP(Cut g, Hierarchy h, int[] itemFrequencies, int itemFrequenciesSum) {
        double sum = 0;

        for (int i = 0; i < h.getDomainItems().length; i++) {
            if (itemFrequencies[i] > 0 && g.generalization[i] > 0)
                sum += itemFrequencies[i] * NCP(i, g.getGeneralization(i), h);
        }

        return sum / itemFrequenciesSum;
    }

}
