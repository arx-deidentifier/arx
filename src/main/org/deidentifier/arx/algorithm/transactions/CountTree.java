package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.google.common.primitives.Ints;

import java.math.BigInteger;
import java.util.*;

public class CountTree {

    private Node root = null;
    private int[][] transactions;
    private int m; // The m in k^m-anonymity
    private GenHierarchy hierarchy;

    private int c = 0;

    public CountTree(int m, int[][] transactions, GenHierarchy hierarchy) {
        root = new Node(-1, null, -1);
        root.id = c++;
        root.count = transactions.length;
        this.transactions = transactions;
        this.m = m;
        this.hierarchy = hierarchy;
        initTree();
    }

    // Builds the tree from expanded transactions
    private void initTree() {
        for (int[] transaction : transactions) {
            int[] etran = expandTransaction(transaction);
            List<int[]> permutations = subsets(etran, m);
            for (int[] permutation : permutations) {
                root.insert(permutation, 0);
            }
        }
        root.sort();
    }

    /**
     * @param t a transaction
     * @return the expanded transaction. Strict set semantics
     */
    private int[] expandTransaction(int[] t) {
        Vector<Integer> etran = new Vector<>(t.length * 2);
        for (int i : t) {
            int[] generalizations = hierarchy.toRoot(i);
            for (int j = 0; j < generalizations.length - 1; j++) { // omit root element
                etran.add(generalizations[j]);
            }
        }
        return Ints.toArray(etran);
    }


    // temporary. To be replaced with efficient dedicated method for generating k-subsets
    public List<int[]> subsets(int[] s, int k) {
        s = IntOpenHashSet.from(s).toArray(); // multiple items that get equally generalized violate the set semantics
        List<int[]> l = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            if (s.length <= 64)
                l.addAll(lsub(s, i));
            else
                l.addAll(sub(s, i));
        }
        Iterator<int[]> it = l.iterator();
        while (it.hasNext()) {
            int[] next = it.next();
            if (containsGeneralizedItems(next)) {
                it.remove();
            }
            reverse(next);
        }
        return l;
    }

    // https://softwareengineering.stackexchange.com/a/67087
    private List<int[]> sub(int[] s, int k) {
        List<int[]> ret = new ArrayList<>();
        BigInteger set = new BigInteger("1").shiftLeft(k).subtract(new BigInteger("1"));
        BigInteger limit = new BigInteger("1").shiftLeft(s.length);
        while (set.compareTo(limit) == -1) {
            ret.add(pick(set, s));
            BigInteger c = set.and(set.negate());
            BigInteger r = set.add(c);
            set = r.xor(set).shiftRight(2).divide(c).or(r);
        }
        return ret;
    }

    // Same source as sub() but with longs. Called when the transaction is smaller than 65 bytes
    private List<int[]> lsub(int[] s, int k) {
        List<int[]> ret = new ArrayList<>();
        long set = (1 << k) - 1;
        long limit = (1 << s.length);
        while (set < limit) {
            ret.add( pick(set, s));

            long c = set & -set;
            long r = set + c;
            set = (((r ^ set) >>> 2) / c) | r;
        }
        return ret;
    }

    private int[] pick(long i, int[] s) {
        int[] a = new int[Long.bitCount(i)];
        int k = 0;
        int p = 0;
        for (int j = 0; j < 64; j++) {
            if ((i & 1) == 1) {
                a[k++] = s[p];
            }
            p++;
            i = i >>> 1;
        }

        return a;
    }

    private static int[] pick(BigInteger i, int[] s) {
        int[] a = new int[i.bitCount()];
        int k = 0;
        int p = 0;
        for (byte b : i.toByteArray()) {
            for (int j = 0; j < 8; j++) {
                if ((b & 1) == 1) {
                    a[k++] = s[p];
                }
                p++;
                b = (byte) (b >> 1);
            }

        }
        return a;
    }

    public static void reverse(int[] data) {
        for (int left = 0, right = data.length - 1; left < right; left++, right--) {
            int temp = data[left];
            data[left]  = data[right];
            data[right] = temp;
        }
    }


    /**
     * @param set an array of items
     * @return true, if set contains at least two items a and b, where a is a generalization of b, else false
     */
    private boolean containsGeneralizedItems(int[] set) {
        for (int a : set) {
            for (int b : set) {
                if (a != b && hierarchy.generalizes(a, b))
                    return true;
            }
        }
        return false;
    }

    /**
     * @param k the k in k^m-anonymity
     * @return true if the database depicted by this tree is k^m-anonymous
     */
    public boolean isKManonymous(int k) {
        return root.kmanonymous(k);
    }

    /**
     * @return the count of each item in the domain
     */
    public int[] itemFrequencies() {
        int[] c = new int[hierarchy.getDomainItems().length];
        for (Node child : root.children) {
            if (child.value < hierarchy.getDomainItems().length)
                c[child.value] = child.count;
        }
        return c;
    }


    // solely for debugging purposes. will be deleted
    public String dot(Dict d) {
        if (this.root != null) {
            StringBuilder s = new StringBuilder("digraph ctree {\n");
            for (Node child : root.children) {
                s.append(root.id).append(" [label=\"").append("ROOT").append("\"]\n");
                s.append(child.id).append("[label=\"").append(d.getString(child.value)).append(" ")
                        .append(child.count).append("\"]\n");

                s.append(root.id).append(" -> ").append(child.id).append("\n");
                s.append(child.dot(d));
            }
            return s + "\n}";
        }
        return "";
    }

    // A node in the count-tree
    private class Node {
        private int count;
        private int value;
        private Node parent;
        private List<Node> children;
        private int id; // id for graphviz nodes

        Node(int value, Node parent, int count) {
            this.value = value;
            this.parent = parent;
            children = new ArrayList<>();
            this.count = count;
            if (parent != null)
                parent.sort();
        }

        void sort() {
            Collections.sort(this.children, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Integer.compare(o1.count, o2.count);
                }
            });
        }

        void insert(int[] set, int current) {
            int finalCurrent = current;
            if (current == set.length) {
                this.count++;
                return;
            }
            Node n = null;
            for (Node child : children) {
                if (child.value == set[finalCurrent]) {
                    n = child;
                    break;
                }
            }
            if (n != null) {
                n.insert(set, ++current);
            } else {
                Node nn = new Node(set[current], this, 1);
                nn.id = c++;
                this.children.add(nn);
            }
            if (this.parent != null)
                this.parent.sort();
        }


        @Override
        public String toString() {
            return String.valueOf(this.value);
        }

        public boolean kmanonymous(int k) {
            boolean an = this.count >= k;
            for (Node child : children) {
                an &= child.kmanonymous(k);
            }
            return an;
        }

        // solely for debugging purposes. will be deleted
        public String dot(Dict d) {
            StringBuilder s = new StringBuilder();
            for (Node child : children) {
                s.append(child.id).append("[label=\"").append(d.getString(child.value)).append(" ")
                        .append(child.count).append("\"]\n");
                s.append(this.id).append("->").append(child.id).append("\n");
            }
            for (Node child : children) {
                s.append(child.dot(d));
            }
            return s.toString();

        }
    }


    public static void main(String[] args) {
        String[][] h = {{"a1", "A", "ALL"}, {"a2", "A", "ALL"}, {"b1", "B", "ALL"}, {"b2", "B", "ALL"}};
        Dict d = new Dict(h);
        GenHierarchy hierarchy = new GenHierarchy(h, d);

        String[][] transactions =
                //   {{"a1", "b1", "b2",}, {"a2", "b1",}, {"a2", "b1", "b2"}, {"a1", "a2", "b2"}};
                {{"a1"}, {"a2", "b1", "b2"}, {"a2", "b1", "b2"},
                        {"a2", "b1", "b2"},};

        int[][] intTran = d.convertTransactions(transactions);
        CountTree ct = new CountTree(2, intTran, hierarchy);

        System.out.println(ct.dot(d));

        Cut c = new Cut(4);
        c.generalize(0, 4);
        c.generalize(1, 4);
        System.out.println(Metrics.NCP(c, intTran, hierarchy, 4, ct));


        System.out.println(OptimalAnonymization.anon(intTran, new int[]{0, 1, 2, 3}, 2, 2, hierarchy));
    }
}

