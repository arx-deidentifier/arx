package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Cut {
    int[] generalization;
    private boolean horizontal;
    private int level;
    private Hierarchy hierarchy;

    /**
     * Initializes a cut. The cut does not generalize by default
     *
     * @param hierarchy the hierarchy this cut belongs to
     */
    public Cut(Hierarchy hierarchy) {
        generalization = new int[hierarchy.getDomainItems().length];
        horizontal = true;
        this.hierarchy = hierarchy;
    }


    /**
     * Initializes a cut. Generalizes all items to the specified level
     *
     * @param hierarchy the hierarchy this cut belongs to
     * @param level     the level all items in this cut should be generalized to
     */
    public Cut(Hierarchy hierarchy, int level) {
        generalization = new int[hierarchy.getDomainItems().length];
        horizontal = true;
        this.hierarchy = hierarchy;
        Arrays.fill(generalization, level);
        this.level = level;
    }

    /**
     * Checks whether the cut is horizontal which means all items are generalized to the same level
     *
     * @return true if the cut is horizontal, else false
     */
    private boolean isHorizontal() {
        for (int i = 0; i < generalization.length - 1; i++) {
            if (generalization[i] != generalization[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generalizes the item to the given level. Also generalizes all leafs that are under this node to the same level.
     * If an item i is generalized to its generalization on level 3 and this generalization covers 5 leaves, all 5
     * leaves are set to level 3. <strong>Lowering of level is not possible.</strong>
     *
     * @param item  the item to be generalized
     * @param level the level the item is generalized to
     */
    public void generalizeToLevel(int item, int level) {
        // If item is a inner node, we have to find a leaf that is generalized by item and then generalize the leaf to the specified level
        if (item >= this.generalization.length) {
            for (int i = 0; i < this.generalization.length; i++) {
                int[] r = this.hierarchy.toRoot(i);
                for (int aR : r) {
                    if (aR == item) {
                        item = i;
                        break;
                    }
                }
            }
        }

        if (level < generalization[item]) // we don't allow lowering the cuts levels
            throw new IllegalArgumentException("Lowering of generalization level not supported");

        // ensure all leafs under the generalization at level are generalized
        int generalizationItem = hierarchy.getHierarchy()[item][level];
        for (int i = 0; i < hierarchy.groupInfo[level].length; i += 3) {
            if (hierarchy.groupInfo[level][i] == generalizationItem) {
                int start = hierarchy.groupInfo[level][i + 1];
                int end = hierarchy.groupInfo[level][i + 2];
                Arrays.fill(this.generalization, start, end + 1, level);
                break;
            }
        }

        this.horizontal = isHorizontal();
        this.level = maxLevel();
    }

    /**
     * @return the highest level some item in the hierarchy is generalized to by this cut
     */
    private int maxLevel() {
        int m = 0;
        for (int aGeneralization : this.generalization) {
            if (aGeneralization > m)
                m = aGeneralization;
        }
        return m;
    }

    /**
     * @param l an item in the generalization hierarchy
     * @return the generalizing item for l according to this cut
     */
    public int getGeneralization(int l) {
        if (l < this.generalization.length)
            return this.hierarchy.getHierarchy()[l][generalization[l]];
        else { // search where the inner leaf l is generalized and to which level
            for (int i = 0; i < this.generalization.length; i++) {
                int[] r = this.hierarchy.toRoot(i);
                for (int j = 0; j < r.length; j++) {
                    if (r[j] == l) {
                        // the leaves under this level are generalized to nodes above l,
                        // so the node where the leaves are generalized to is returned
                        if (j < this.generalization[i])
                            return this.hierarchy.getHierarchy()[i][this.generalization[i]];
                        else // If the leaves are generalized to a node below l, then l itself is returned
                            return this.hierarchy.getHierarchy()[i][j];
                    }
                }
            }
        }
        throw new RuntimeException("Item is not in domain");
    }


    /**
     * @param i an item in the generalization hierarchy. Can bei either leaf or inner node
     * @return true if the item is generalized by this cut, otherwise false
     */
    public boolean isGeneralized(int i) {
        if (i < generalization.length)
            return generalization[i] > 0;
        else { // Find the first leaf that is generalized by i and return if the leaf is generlized to a higher level than the level of i
            for (int j = hierarchy.groupInfo.length - 1; j >= 0; j--) {
                for (int k = 0; k < hierarchy.groupInfo[j].length; k += 3) {
                    if (hierarchy.groupInfo[j][k] == i)
                        return generalization[hierarchy.groupInfo[j][k + 1]] > j;
                }
            }
        }
        return false;
    }


    /**
     * Generalizes the database according to the cut. Returns a <strong>COPY</strong> of the database
     *
     * @param d a database
     * @return a copy of d, generalized according to this cut
     */
    public int[][] generalize(int[][] d) {
        int[][] generalizedTable = new int[d.length][];
        for (int i = 0; i < d.length; i++) {
            generalizedTable[i] = new int[d[i].length];
        }
        arrayCopy(d, generalizedTable); // copy db
        for (int i = 0; i < generalizedTable.length; i++) {
            for (int j = 0; j < generalizedTable[i].length; j++) {
                int item = generalizedTable[i][j];
                int level = generalization[item];
                generalizedTable[i][j] = hierarchy.generalizationAtLevel(item, level);
            }

            // remove duplicate resulting from generalization
            IntOpenHashSet h = new IntOpenHashSet(generalizedTable[i].length);
            h.add(generalizedTable[i]);
            generalizedTable[i] = h.toArray();
        }
        return generalizedTable;
    }

    /**
     * Generalizes a single transaction according to this cut. Set semantics, so no item is duplicate even if multiple
     * items get generalized to the same generalization
     *
     * @param i the transaction to be generalized
     * @return the generalized transaction
     */
    public int[] generalizeTransaction(int[] i) {
        IntOpenHashSet set = new IntOpenHashSet(i.length * 2);
        for (int anI : i) {
            set.add(this.getGeneralization(anI));
        }
        return set.toArray();
    }

    /**
     * Computes the ancestors in the hierarchy of cuts. See Figure 4 in
     * "M. Terrovitis, N. Mamoulis, and P. Kalnis, “Privacy-preserving anonymization of set-valued data,”" for illustration
     *
     * @return all ancestors in the hierarchy of cuts.
     */
    public List<Cut> ancestors() {
        List<Cut> cuts = new LinkedList<>();
        int[][] hierarchyArray = hierarchy.getHierarchy();

        int level = horizontal ? this.level + 1 : this.level;
        // the nodes that are in this level of the hierarchy, but not in this cut
        IntArrayList nodesOfThisLevel = new IntArrayList(hierarchyArray.length / 2);

        IntArrayList nodesOfThisCutList = new IntArrayList();
        int[] nodesOfThisCut;

        int[] groupInfo = hierarchy.groupInfo[level];

        for (int i = 0; i < groupInfo.length; i += 3) {
            if (generalization[groupInfo[i + 1]] != level)
                nodesOfThisLevel.add(i / 3);
            else
                nodesOfThisCutList.add(i / 3);
        }
        nodesOfThisCut = nodesOfThisCutList.toArray();

        // Generate sets of increasing size of all the nodes that are not in this cut
        for (int i = 1; i <= (horizontal ? 1 : nodesOfThisLevel.size()); i++) {
            SubsetIterator ancestorNodes = new SubsetIterator(nodesOfThisLevel.toArray(), i);
            while (ancestorNodes.hasNext()) {
                int[] next = ancestorNodes.next();
                int[] newCut = concat(nodesOfThisCut, next); // merge the current cut with the generated cut
                // create cut from node set
                cuts.add(cutFromGroupArray(newCut, level));
            }
        }
        cuts.remove(this);
        return cuts;
    }

    private Cut cutFromGroupArray(int[] newCut, int level) {
        Cut genCut = new Cut(this.hierarchy);
        for (int group : newCut) {
            int gStart = hierarchy.groupInfo[level][group * 3 + 1];
            int gEnd = hierarchy.groupInfo[level][group * 3 + 2];
            Arrays.fill(genCut.generalization, gStart, gEnd + 1, level);
        }
        genCut.horizontal = genCut.isHorizontal();
        genCut.level = level;
        return genCut;
    }

    private static int[] concat(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Cut cut = (Cut) o;

        return Arrays.equals(generalization, cut.generalization);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(generalization);
    }

    @Override
    public String toString() {
        return Arrays.toString(generalization);
    }

    private static void arrayCopy(int[][] aSource, int[][] aDestination) {
        for (int i = 0; i < aSource.length; i++) {
            System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
        }
    }

    /**
     * Merges two arrays according to their content. The values of the changing array are overwritten if the value is
     * less than the value at the same position of the other array
     *
     * @param changing the array that should incorporate the levels of the other array
     * @param merging  the array that is merged into the other
     */
    private void merge(int[] changing, int[] merging) {
        for (int i = 0; i < changing.length; i++) {
            if (changing[i] < merging[i]) {
                changing[i] = merging[i];
            }
        }
    }

    public void merge(Cut other) {
        merge(this.generalization, other.generalization);
    }

    public int getLevel() {
        return level;
    }
}
