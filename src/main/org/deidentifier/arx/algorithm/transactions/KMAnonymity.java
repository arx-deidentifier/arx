package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;

import java.util.*;

public class KMAnonymity {
    private int k;
    private int m;
    private Hierarchy hierarchy;
    private int[][] D;
    private int[] itemFrequencies;
    private int itemFrequenciesSum;
    private Cut resultingCut;
    private Dict dict;


    public KMAnonymity(int k, int m, AttributeType.Hierarchy hierarchy, Data db){
        Hierarchy h2 = ARXHierarchyWrapper.convert(hierarchy);
        Dict d = new Dict(hierarchy.getHierarchy());
        List<String[]> transactions = new ArrayList<>();
        Iterator<String[]> it = db.getHandle().iterator();

        while (it.hasNext()) {
            String[] next = it.next();
            transactions.add(next);
        }

        int[][] intTran = d.convertTransactions(ARXDataWrapper.aggregate(transactions.toArray(new String[0][]), 0, 1));

        this.k = k;
        this.m = m;
        this.hierarchy = h2;
        this.D = intTran;
        this.dict = d;
        countItems();

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
            // here we could check if ct is already k^m-anonymous. So all m-i other calls of DA are unneccessary
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

        if (node.getChildren().size() == 0 && node.getCount() <= k) { // node is a leaf node and has count less or equal to k
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

        IntArrayList levels = new IntArrayList(height * itemset.length);

        // add all levels each item in the itemset can be generalized to
        for (int item : itemset) {
            for (int i = hierarchy.rangeInfo[item][0]; i < height; i++) {
                levels.add(i);
            }
        }

        // Generate all possible cuts that contain the items of itemset
        SubsetIterator v = new SubsetIterator(levels.toArray(), itemset.length);
        Set<IntArrayList> cuts = new HashSet<>(); // remove possible duplicates

        boolean allBelow = false;
        while (v.hasNext()) {
            int[] next = v.next();
            for (int i = 0; i < next.length; i++) {
                if (next[i] >= hierarchy.rangeInfo[itemset[i]][0])
                    break;
                if (i == next.length - 1)
                    allBelow = true;
            }
            if (!allBelow)
                cuts.add(IntArrayList.from(next));
            allBelow = false;
        }

        return cutLowestIL(cuts, ct, itemset, k);
    }

    private Cut cutLowestIL(Set<IntArrayList> cuts, CountTree ct, int[] itemset, int k) {
        int height = hierarchy.getHierarchy()[0].length - 1;
        Cut cutLowestIL = new Cut(hierarchy, height); // The cut that always works is the one that generalizes to the root node of the hierarchy
        double cutLowestILIL = 1;

        for (IntArrayList cut : cuts) {
            Cut c = new Cut(hierarchy); // generate cut from list containing levels for each item
            for (int i = 0; i < cut.size(); i++) {
                c.generalizeToLevel(itemset[i], cut.get(i));
            }
            if (ct.providesKAnonymity(itemset, c, k)) {
                double ncp = Metrics.NCP(c, hierarchy, itemFrequencies, itemFrequenciesSum);
                if (ncp < cutLowestILIL) {
                    cutLowestIL = c;
                    cutLowestILIL = ncp;
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
            throw new IllegalStateException("No algorithm has been called yet.");
        return Metrics.NCP(resultingCut, hierarchy, itemFrequencies, itemFrequenciesSum);
    }

    public Cut getResultingCut() {
        return resultingCut;
    }

    /**
     *
     * @return the generalized database according to the resulting cut
     */
    public String[][] generalizedDatabase(){
        if(resultingCut == null)
            throw new IllegalStateException("No algorithm has been called yet.");
        return dict.convertTransactions(resultingCut.generalize(D));
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
