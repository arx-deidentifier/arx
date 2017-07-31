package org.deidentifier.arx.algorithm.transactions;


import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;

import java.util.Arrays;
import java.util.BitSet;

//TODO come up with way to efficiently walk hierarchy
public class Hierarchy {

    private int[][] hierarchy;
    private int[] domainItems;
    private int[] leafsUnderItem;
    // [level][item, start, end]
    protected int[][] zoningInfo;
    private int[][] extendedHierarchy;


    /**
     * Converts the given generalization hierarchy to an integer representation according to the dictionary
     *
     * @param h the string generalization hierarchy
     * @param d the dictionary that maps the string values to integers
     */
    public Hierarchy(String[][] h, Dict d) {
        hierarchy = new int[h.length][h[0].length];
        domainItems = new int[h.length];

        BitSet nodeCount = new BitSet(h.length * h[0].length);

        // map string hierarchy to integer hierarchy
        for (int i = 0; i < h.length; i++) {
            for (int j = 0; j < h[0].length; j++) {
                hierarchy[i][j] = d.getRepresentation(h[i][j]);
                nodeCount.set(hierarchy[i][j]);
            }
            this.domainItems[i] = d.getRepresentation(h[i][0]);
        }

        // Counting the leafs under each item in the generalization hierarchy
        leafsUnderItem = new int[nodeCount.cardinality()];
        extendedHierarchy = new int[nodeCount.cardinality() - domainItems.length][];

        createZoningInfo();

        for (int[] ints : zoningInfo) {
            for (int i = 0; i < ints.length; i += 3) {
                int diff = ints[i + 2] - ints[i + 1];
                leafsUnderItem[ints[i]] = diff == 0 ? 0 : diff + 1;
            }
        }
    }

    private void createZoningInfo() {
        zoningInfo = new int[hierarchy[0].length][];
        IntArrayList groupInfo = new IntArrayList(hierarchy.length * 2);
        for (int level = 0; level < hierarchy[0].length; level++) {
            int currentPointer = 0;
            int groupStart = 0;
            while (currentPointer < hierarchy.length) { // iterate complete level in hierarchy
                int groupingItem = hierarchy[currentPointer][level];
                while (currentPointer < hierarchy.length - 1 && groupingItem == hierarchy[currentPointer + 1][level]) // grouping operation
                    currentPointer++;
                groupInfo.add(groupingItem, groupStart, currentPointer);
                groupStart = ++currentPointer;
            }
            zoningInfo[level] = groupInfo.toArray();
            groupInfo.clear();
        }
    }


    /**
     * @param leaf  a leaf in the hierarchy
     * @param level the item that generalizes the leaf at the given generalization level
     * @return returns the item that
     */
    protected int generalizationAtLevel(int leaf, int level) {
        return hierarchy[leaf][level];
    }

    /**
     * @param i an item in the hierarchy
     * @return the count of leafs that are generalized by i
     */
    protected int getLeafsUnderGeneralization(int i) {
        return leafsUnderItem[i];
    }

    /**
     * Copies the path from the provided item in the  hierarchy to the root node
     *
     * @param item the item for which the generalizations should be returned
     * @return All generalizations of item, inclusively the item itself at index 0
     */

    protected int[] toRoot(int item) {
        // item is a leaf, so we can return a column
        if (item < this.hierarchy.length) {
            return this.hierarchy[item];
        } else if (extendedHierarchy(item) != null) { // item is not a leaf, but we already computed its path to the root
            return extendedHierarchy(item);
        } else {
            // the item is not a leaf, so we have to search where the item starts
            // and return the slice from the starting position to the root
            for (int[] path : this.hierarchy) {
                for (int j = 0; j < path.length; j++) {
                    if (path[j] == item) {
                        //TODO dont copy the array, but instead save two integers. The leaf where item is a gener. from and the level at which this item starts so extendedHierarchy is int[nodecount-|I|][2]
                        setExtendedHierarchyItem(item, Arrays.copyOfRange(path, j, path.length));
                        return extendedHierarchy(item);
                    }
                }
            }
        }
        return null;
    }

    private int[] extendedHierarchy(int i) {
        return this.extendedHierarchy[i - this.hierarchy[0][1]];
    }

    private void setExtendedHierarchyItem(int i, int[] ii) {
        this.extendedHierarchy[i - this.hierarchy[0][1]] = ii;
    }


    /**
     * @param item           an item
     * @param generalization an item
     * @return true, if generalization is a generalization of item, or the item itself
     */
    protected boolean generalizes(int item, int generalization) {
        return generalizesAtLevel(item, generalization) >= 0;
    }

    /**
     * @param item           an item
     * @param generalization an item
     * @return the level where the generalization of item is. returns -1 if generalization does not generalize item
     */
    protected int generalizesAtLevel(int item, int generalization) {
        int[] pathToRoot = toRoot(item);
        for (int i = 0; i < pathToRoot.length; i++) {
            if (pathToRoot[i] == generalization)
                return i;
        }
        return -1;
    }

    /**
     * @return the leafes of this hierarchy
     */
    protected int[] getDomainItems() {
        return domainItems;
    }

    protected int[][] getHierarchy() {
        return hierarchy;
    }

    /**
     * @param set an array of items
     * @return true, if set contains at least two items a and b, where a is a generalization of b, else false
     */
    protected boolean containsGeneralizedItems(int[] set) {
        for (int a : set) {
            for (int b : set) {
                if (a != b && this.generalizes(a, b))
                    return true;
            }
        }
        return false;
    }

    /**
     * @param t a transaction
     * @return the expanded transaction. Strict set semantics
     */
    public int[] expandTransaction(int[] t) {
        IntOpenHashSet etran = new IntOpenHashSet();
        for (int i : t) {
            int[] generalizations = toRoot(i);
            for (int j = 0; j < generalizations.length - 1; j++) { // omit root element
                etran.add(generalizations[j]);
            }
        }

        return etran.toArray();
    }
}

