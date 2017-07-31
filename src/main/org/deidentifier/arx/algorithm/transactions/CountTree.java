package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CountTree {

    private Node root = null;
    private int[][] transactions;
    private int m; // The m in k^m-anonymity
    private Hierarchy hierarchy;
    private int[] itemFrequencies;
    private int countDomainItems;

    private int c = 0; // for graphviz

    public CountTree(int m, int[][] transactions, Hierarchy hierarchy) {
        root = new Node(-1, null, Integer.MAX_VALUE);
        root.id = c++;
        this.transactions = transactions;
        this.m = m;
        this.hierarchy = hierarchy;
        initTree();
    }

    public CountTree(int m, Hierarchy hierarchy) {
        this.m = m;
        this.hierarchy = hierarchy;
        root = new Node(-1, null, Integer.MAX_VALUE);
        root.id = c++;
    }

    // Builds the tree from expanded transactions
    private void initTree() {
        for (int[] transaction : transactions) {
            int[] etran = hierarchy.expandTransaction(transaction);
            insert(etran);
        }
        root.sortRecursive();
    }

    public void insert(int[] a) {
        for (int i = 1; i <= m; i++) {
            SubsetIterator it = new SubsetIterator(a, i);
            while (it.hasNext()) {
                int[] next = it.next();
                if (!hierarchy.containsGeneralizedItems(next)) {
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
    protected int[] itemFrequencies() {
        if (itemFrequencies != null) // itemFrequencies aren't cached
            return itemFrequencies;

        int[] c = new int[hierarchy.getDomainItems().length];
        for (Node child : root.children) {
            if (child.value < hierarchy.getDomainItems().length)
                c[child.value] = child.count;
        }
        itemFrequencies = c;
        for (int itemFrequency : itemFrequencies) {
            countDomainItems += itemFrequency;
        }
        return c;
    }

    protected int getCountDomainItems() {
        return countDomainItems;
    }

    // solely for debugging purposes. will be deleted
    protected String dot(Dict d) {
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

    protected String dot() throws IOException {
        if (this.root != null) {
            StringBuilder s = new StringBuilder("digraph ctree {\n");
            for (Node child : root.children) {
                s.append(root.id).append(" [label=\"").append("ROOT").append("\"]\n");
                s.append(child.id).append("[label=\"ID").append(child.value).append(" ")
                        .append(child.count).append("\"]\n");

                s.append(root.id).append(" -> ").append(child.id).append("\n");
                s.append(child.dot());
            }
            s.append("}");
            File f = new File("graph.dot");
            FileWriter w = new FileWriter(f, false);
            w.write(s.toString());
            w.flush();
            w.close();
            return s + "\n}";
        }
        return "";
    }

    public boolean providesKAnonymity(int[] path, int k) {
        Arrays.sort(path);
        reverse(path);
        Node n = root;
        int uu = 0;
        for (int i : path) {
            for (Node node : n.getChildren()) {
                if (node.getValue() == i) {
                    uu++;
                    n = node;
                    break;
                }
            }
        }

        return uu == path.length && n.count >= k-1;
    }

    // A node in the count-tree
    protected class Node {
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
                    return Integer.compare(o1.count, o2.count) * -1;
                }
            });
        }

        void insert(int[] set, int current) {

            if (current == set.length) {
                this.count++;
                return;
            }

            Arrays.sort(set);
            reverse(set);

            Node n = null;

            for (Node child : children) {
                if (child.value == set[current])
                    n = child;
            }

            if (n != null) {
                n.insert(set, current + 1);
            } else {
                Node newNode = new Node(set[current], this, 0);
                newNode.id = c++;
                this.children.add(newNode);
                newNode.insert(set, current + 1);
            }

        }


        protected void sortRecursive() {
            this.sort();
            for (Node child : children) {
                child.sortRecursive();
            }
        }


        @Override
        public String toString() {
            return String.valueOf(this.value);
        }

        public boolean kmanonymous(int k) {
            boolean nodeKanonymous = this.count >= k;

            if (!nodeKanonymous)
                return false;

            for (Node child : children) {
                nodeKanonymous &= child.kmanonymous(k);
            }
            return nodeKanonymous;
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

        public String dot() {
            StringBuilder s = new StringBuilder();
            for (Node child : children) {
                s.append(child.id).append("[label=\"ID: ").append(child.value).append(" ")
                        .append(child.count).append("\"]\n");
                s.append(this.id).append("->").append(child.id).append("\n");
            }
            for (Node child : children) {
                s.append(child.dot());
            }

            return s.toString();
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


        private IntArrayList getPath(IntArrayList a) {
            if (this.parent == null) {
                return a;
            } else {
                parent.getPath(a);
                a.add(this.getValue());
                return a;
            }
        }

        public int[] getPath() {
            return getPath(new IntArrayList()).toArray();
        }
    }

    private static void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int temp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = temp;
        }
    }
}
