package org.deidentifier.arx.algorithm.transactions;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class OptimalAnonymization {

    /**
     * OA as in "M. Terrovitis, N. Mamoulis, and P. Kalnis, “Privacy-preserving anonymization of set-valued data,”"
     *
     * @param D the database to be anonymized
     * @param I the domain
     * @param k k in k^m-anonymity
     * @param m m in k^m-anonymity
     * @param h the generalization hierarchy for I
     * @return the cut that provides k^m-anonymity and inflicts minimal information loss
     */
    public static Cut anon(int[][] D, int[] I, int k, int m, GenHierarchy h) {
        Cut copt = null;
        double coptCost = Double.POSITIVE_INFINITY;
        Queue<Cut> Q = new LinkedBlockingQueue<>();
        Q.add(new Cut(I.length));
        HashSet<Cut> H = new HashSet<>();
        CountTree origTree = new CountTree(m, D, h);

        while (!Q.isEmpty()) {
            Cut c = Q.poll();
            int[][] generalizedTableByC = c.generalize(D);
            CountTree ct = new CountTree(m, generalizedTableByC, h);
            List<Cut> anchestors = c.anchestors(h);
            if (!ct.isKManonymous(k)) {
                for (Cut anchestor : anchestors) {
                    if (!H.contains(anchestor))
                        Q.add(anchestor);
                }
            } else {
                for (Cut anchestor : anchestors) {
                    H.add(anchestor);
                    if (Q.contains(anchestor))
                        Q.remove(anchestor);
                }
                if (Metrics.NCP(c, D, h, I.length, ct) < coptCost) {
                    copt = c;
                    coptCost = Metrics.NCP(c, D, h, I.length, origTree);
                }
            }
        }
        return copt;
    }
}
