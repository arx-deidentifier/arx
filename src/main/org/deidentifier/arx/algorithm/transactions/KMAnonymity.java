package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;

import java.util.*;

public class KMAnonymity {
    private int k;
    private int m;
    private Hierarchy hierarchy;
    private int[][] D;
    private int[] itemFrequencies;
    private int itemFrequenciesSum;
    private Cut resultingCut;

    public KMAnonymity(int k, int m, Hierarchy h, int[][] d) {
        if (d.length < k)
            throw new IllegalArgumentException(String.format("k (%s) < database size (%s)", k, d.length));
        this.k = k;
        this.m = m;
        this.hierarchy = h;
        D = d;
        countItems(); // for calculation of information loss
    }

    /**
     * Apriori Anonymization as in
     * "Manolis Terrovitis, Nikos Mamoulis, and Panos Kalnis. 2008. Privacy-preserving anonymization of set-valued data.
     * Proc. VLDB Endow. 1, 1 (August 2008), 115-125."
     *
     * @return the cut that provides k^m-anonymity
     */
    public Cut aprioriAnonymization() {
        Cut cout = new Cut(hierarchy);

        for (int i = 1; i <= m; i++) {
            CountTree ct = new CountTree(i, hierarchy);
            for (int[] transaction : D) {
                int[] expTran = hierarchy.expandTransaction(transaction);
                expTran = cout.generalizeTransaction(expTran);
                ct.insert(expTran);
            }
            ct.sort();
            Cut c = directAnonymization(ct);
            cout.merge(c);
        }
        resultingCut = cout;
        return cout;
    }

    /**
     * Direct Anonymization as in
     * "Manolis Terrovitis, Nikos Mamoulis, and Panos Kalnis. 2008. Privacy-preserving anonymization of set-valued data.
     * Proc. VLDB Endow. 1, 1 (August 2008), 115-125."
     *
     * @return the cut that provides k^m-anonymity
     */
    public Cut directAnonymization() {
        return directAnonymization(new CountTree(m, D, hierarchy));
    }

    /**
     * @param ct The Count Tree that should be used by the DA
     * @return a cut that generalizes
     */
    private Cut directAnonymization(CountTree ct) {
        Cut cout = new Cut(hierarchy);
        CountTree.Node root = ct.getRoot();

        for (CountTree.Node node : root.getChildren()) {
            anon(cout, ct, node);
        }
        resultingCut = cout;

        return cout;
    }

    private void anon(Cut cout, CountTree ct, CountTree.Node node) {
        if (cout.isGeneralized(node.getValue()))
            return;

        if (node.getChildren().size() == 0 && node.getCount() <= k) { // node is a leaf node and has count less than k
            int[] J = node.getPath();
            Cut c = getKAnonymousCut(J, k, ct);
            cout.merge(c);
            return;
        }

        for (CountTree.Node child : node.getChildren()) {
            anon(cout, ct, child);
            if (cout.isGeneralized(node.getValue()))
                break;
        }
    }

    /**
     * @param itemset The itemset that is not "k-anonymous"
     * @param k       the desired support of itemsets equal to a (generalized)  itemset
     * @param ct      The Count Tree itemset is extracted from
     * @return a cut that generalizes the itemset to a itemset which support is at least k
     */
    private Cut getKAnonymousCut(int[] itemset, int k, final CountTree ct) {
        int height = hierarchy.getHierarchy()[0].length - 1;

        IntArrayList levels = new IntArrayList(height*itemset.length);

        // add all levels each item in the itemset can be generalized to
        for (int item : itemset) {
            for (int i = hierarchy.rangeInfo[item][0]; i < height; i++) {
                levels.add(i);
            }
        }

        // Generate all possible cuts that contain the items of itemset
        SubsetIterator v = new SubsetIterator(levels.toArray(), itemset.length);
        Set<IntArrayList> cuts = new HashSet<>();

        while (v.hasNext()) {
            cuts.add(IntArrayList.from(v.next()));
        }


        Cut cutLowestIL = new Cut(hierarchy, height); // The cut that always works is the one that generalizes to the root node of the hierarchy
        double cutLowestILIL = 1;

        for (IntArrayList cut : cuts) {
            Cut c = new Cut(hierarchy); // generate cut from list containing levels for each item
            for (int i = 0; i < cut.size(); i++) {
                c.generalizeToLevel(itemset[i], cut.get(i));
            }
            // check if cut provides k-anonymity
            if (ct.providesKAnonymity(itemset, c, k)) {
                // if it does, compare its information loss with the current best cut and replace if applicable
                double candidateIL = Metrics.NCP(c, hierarchy, itemFrequencies, itemFrequenciesSum);
                if (candidateIL < cutLowestILIL) {
                    cutLowestIL = c;
                    cutLowestILIL = candidateIL;
                }
            }
        }
        return cutLowestIL;
    }


    /**
     * OA as in
     * "Manolis Terrovitis, Nikos Mamoulis, and Panos Kalnis. 2008. Privacy-preserving anonymization of set-valued data.
     * Proc. VLDB Endow. 1, 1 (August 2008), 115-125." (slightly modified)
     *
     * @return the cut that provides k^m-anonymity and inflicts minimal information loss
     */
    public Cut optimalAnonymization() {
        Cut copt = null;
        double coptCost = Double.POSITIVE_INFINITY;
        Queue<Cut> Q = new ArrayDeque<>();
        Q.add(new Cut(hierarchy));
        HashSet<Cut> H = new HashSet<>();

        while (!Q.isEmpty()) {
            Cut c = Q.poll();
            int[][] generalizedTableByC = c.generalize(D);
            CountTree ct = new CountTree(m, generalizedTableByC, hierarchy);
            List<Cut> ancestors = c.ancestors();
            if (!ct.isKManonymous(k)) {
                for (Cut ancestor : ancestors) {
                    if (!H.contains(ancestor) && !Q.contains(ancestor))
                        Q.add(ancestor);
                }
            } else {
                for (Cut ancestor : ancestors) {
                    H.add(ancestor);
                    if (Q.contains(ancestor))
                        Q.remove(ancestor);
                }
                if (Metrics.NCP(c, hierarchy, itemFrequencies, itemFrequenciesSum) < coptCost) {
                    copt = c;
                    coptCost = Metrics.NCP(c, hierarchy, itemFrequencies, itemFrequenciesSum);
                }
            }
        }
        resultingCut = copt;
        return copt;
    }

    public double informationLoss() {
        if (resultingCut == null)
            return -1;
        return Metrics.NCP(resultingCut, hierarchy, itemFrequencies, itemFrequenciesSum);
    }

    public Cut getResultingCut() {
        return resultingCut;
    }

    private void countItems() {
        IntIntOpenHashMap count = new IntIntOpenHashMap();
        for (int[] ints : D) {
            for (int anInt : ints) {
                count.putOrAdd(anInt, 1, 1);
            }
        }
        itemFrequencies = new int[hierarchy.nodeCount];

        for (IntIntCursor next : count) {
            itemFrequencies[next.key] = next.value;
            itemFrequenciesSum += next.value;
        }
    }
}
