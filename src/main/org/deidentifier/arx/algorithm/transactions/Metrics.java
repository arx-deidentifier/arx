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
     * @param g  the cut for which the information loss is computed
     * @param h  the generalization hierarchy which g belongs to
     * @param ct the count-tree for tran
     * @return the information loss inflicted when applying cut g on database tran
     */
    public static double NCP(Cut g, Hierarchy h, CountTree ct) {
        double sum = 0;
        int[] Cp = ct.itemFrequencies();

        for (int i = 0; i < h.getDomainItems().length; i++) {
            if (g.generalization[i] > 0) // item is generalized. If it is not, loss = 0, so multiplication = 0 so we can optimize this by ourselves
                sum += Cp[i] * NCP(i, g.getGeneralization(i), h);
        }

        return sum / ct.getCountDomainItems();
    }
}
