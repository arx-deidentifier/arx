package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.google.common.primitives.Ints;

import java.util.*;

public class CountTree {

    private Node root = null;
    private int m; // The m in k^m-anonymity
    private Hierarchy hierarchy;
    private int[] itemFrequencies; // the count of occurences of each leaf in the generalization hierarchy
    private int sumDomainItemCount; // the sum of itemFrequencies

    /**
     * Initializes a Count Tree and populates it with itemsets of size <= m
     *
     * @param m         the m in k^m-anonymity
     * @param hierarchy the generalization hierarchy this Count Tree is based on
     */
    public CountTree(int m, int[][] transactions, Hierarchy hierarchy) {
        root = new Node(-1, null, Integer.MAX_VALUE);
        this.m = m;
        this.hierarchy = hierarchy;
        initTree(transactions);
    }

    /**
     * Initializes an empty Count Tree
     *
     * @param m         the m in k^m-anonymity
     * @param hierarchy the generalization hierarchy this Count Tree is based on
     */
    public CountTree(int m, Hierarchy hierarchy) {
        this.m = m;
        this.hierarchy = hierarchy;
        root = new Node(-1, null, Integer.MAX_VALUE);
    }

    // Builds the tree from transactions
    private void initTree(int[][] transactions) {
        for (int[] transaction : transactions) {
            int[] etran = hierarchy.expandTransaction(transaction);
            insert(etran);
        }
        root.sortRecursive();
    }

    /**
     * Inserts alls subsets for set of size <= m
     *
     * @param set the set to be inserted into the tree
     */
    public void insert(int[] set) {
        // if the set to be inserted is smaller than m, there are only subsets of set up to size set.length (there are no subsets of size n+1 of sets of size n).
        // So we can skip all SubsetIterators of size set.length + 1 to m.
        int limit = set.length > m ? m : set.length;
        for (int i = 1; i <= limit; i++) {
            SubsetIterator it = new SubsetIterator(set, i);
            while (it.hasNext()) {
                int[] next = it.next();
                if (!hierarchy.containsGeneralizedItems(next)) { // only insert sets that don't contain two items where one is set generalization of another
                    sortDescending(next); // sorting the items reduces fragmentation of the tree
                    root.insert(next, 0);
                }
            }
        }
    }

    protected Node getRoot() {
        return root;
    }


    /**
     * @param k the k in k^m-anonymity
     * @return true if the database depicted by this tree is k^m-anonymous, else false
     */
    public boolean isKManonymous(int k) {
        return root.kmanonymous(k);
    }


    /**
     * @return the count of each item in the domain
     */
    public int[] itemFrequencies() {
        if (itemFrequencies != null) // itemFrequencies aren't cached
            return itemFrequencies;

        int[] c = new int[hierarchy.getDomainItems().length];
        for (Node child : root.children) {
            if (child.value < hierarchy.getDomainItems().length)
                c[child.value] = child.count;
        }
        itemFrequencies = c;

        //calculate count of all items combined. Used in NCP
        for (int itemFrequency : itemFrequencies) {
            sumDomainItemCount += itemFrequency;
        }
        return c;
    }

    public int getItemCount() {
        return sumDomainItemCount;
    }

    /**
     * @param path the </strong>descending sorted<strong> set to be inserted
     * @param c    the cut that is used to generalize path
     * @param k    an integer
     * @return true if path, generalized by c occurs at least k+1 times in the tree, false if less than k
     */
    public boolean providesKAnonymity(int[] path, Cut c, int k) {
       // sortDescending(path);
        Node n = root;
        int oldCount;
        int uu = 0;

        for (int i : path) {
            n = n.getChild(i);
            uu++;
            if(n == null)
                throw new RuntimeException("Path does not exist");
        }

        oldCount = n.count;
        uu = 0;
        n = root;
        int[] genPath = c.generalizeTransaction(path);
        sortDescending(genPath);


        if (Arrays.equals(genPath, path)) // no item in path was generalized by c
            return uu == path.length && oldCount > k;


        for (int i : genPath) {
            n = n.getChild(i);
            uu++;
            if(n == null)
                throw new RuntimeException("Path does not exist for generalized transaction");
        }

        return n.count + oldCount > k; // if the count of the old endnode of the path plus the endnode
    }

    /**
     * sorts all nodes in this tree
     */
    public void sort() {
        root.sortRecursive();
    }



    // A node in the count-tree
    protected class Node {
        private int count;
        private int value;
        private Node parent;
        private List<Node> children;
        private IntObjectOpenHashMap<Node> insertLookup; // todo this should be nulled after the tree was built to free memory

        Node(int value, Node parent, int count) {
            this.value = value;
            this.parent = parent;
            children = new ArrayList<>();
            insertLookup = new IntObjectOpenHashMap<>();
            this.count = count;
        }

        /**
         * A node is sorted when the order of its children is descending according to their count
         */
        private void sort() {
            Collections.sort(this.children, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Integer.compare(o1.count, o2.count) * -1;
                }
            });
        }

        /**
         * Inserts all items of set beginning at current into the nodes below this
         *
         * @param set     the items to be inserted
         * @param current the first item in set to be inserted
         */
        private void insert(int[] set, int current) {
            if (current == set.length) { // last item is inserted, update count and backtrack
                this.count++;
                return;
            }

            Node n = insertLookup.get(set[current]);

            if (n != null) {
                n.insert(set, current + 1);
            } else {
                Node newNode = new Node(set[current], this, 0);
                this.children.add(newNode);
                insertLookup.put(newNode.value, newNode);
                newNode.insert(set, current + 1);
            }
        }

        public Node getChild(int value){
            return insertLookup.get(value);
        }

        /**
         * Sorts this node and all its children
         */
        private void sortRecursive() {
            this.sort();
            for (Node child : children) {
                child.sortRecursive();
            }
        }

        /**
         * @param k the k in k^m-anonymity
         * @return true if all nodes below this node have count > k, else false
         */
        private boolean kmanonymous(int k) {
            boolean nodeKanonymous = this.count > k;

            if (!nodeKanonymous)
                return false;

            for (Node child : children) {
                nodeKanonymous &= child.kmanonymous(k);
            }
            return nodeKanonymous;
        }

        public List<Node> getChildren() {
            return children;
        }

        public int getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public Node getParent() {
            return parent;
        }

        /**
         * @return the path from the root of the tree to this node
         */
        public int[] getPath() {
            return getPath(new IntArrayList()).toArray();
        }

        private IntArrayList getPath(IntArrayList a) {
            if (this.parent == null) {
                return a;
            } else {
                parent.getPath(a);
                a.add(this.getValue());
                return a;
            }
        }
    }

    /**
     * Sorts the array in descending order
     * @param array The array to be sorted
     */
    private static void sortDescending(int[] array) {
        List<Integer> integersList = Ints.asList(array); // This works sinces Ints.asList directly uses the array as backing data structure
        Collections.sort(integersList, Collections.reverseOrder()); // so when the list is sorted, the backing array is sorted, which means the given array is sorted
    }
}
