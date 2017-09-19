package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Cut {
    int[] generalization;
    private Hierarchy hierarchy;

    /**
     * Initializes a cut. The cut does not generalize by default
     *
     * @param hierarchy the hierarchy this cut belongs to
     */
    public Cut(Hierarchy hierarchy) {
        generalization = new int[hierarchy.getDomainItems().length];
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
        this.hierarchy = hierarchy;
        Arrays.fill(generalization, level);
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
            item = this.hierarchy.rangeInfo[item][1];
        }

        if (level < generalization[item]) // we don't allow lowering the cuts levels
            return;

        // ensure all leafs under the generalization at level are generalized
        int generalizationItem = hierarchy.getHierarchy()[item][level];
        int start = hierarchy.rangeInfo[generalizationItem][1];
        int end = hierarchy.rangeInfo[generalizationItem][2];
        Arrays.fill(this.generalization, start, end + 1, level);
    }

    /**
     * @param l an item in the generalization hierarchy
     * @return the generalizing item for l according to this cut
     */
    public int getGeneralization(int l) {
        if (l < this.generalization.length)
            return this.hierarchy.getHierarchy()[l][generalization[l]];
        else { // search where the inner leaf l is generalized and to which level
            int firstleaf = this.hierarchy.rangeInfo[l][1];
            int levelNode = this.hierarchy.rangeInfo[l][0];
            int levelFirstLeaf = this.generalization[firstleaf];

            if (levelFirstLeaf <= levelNode)
                return l;
            else
                return this.hierarchy.getHierarchy()[firstleaf][this.generalization[firstleaf]];
        }
    }


    /**
     * @param i an item in the generalization hierarchy. Can bei either leaf or inner node
     * @return true if the item is generalized by this cut, otherwise false
     */
    public boolean isGeneralized(int i) {
        if (i < generalization.length)
            return generalization[i] > 0;
        else { // i is a inner node of the hierarchy. Return true if i is generalized to a node above i
            return generalization[hierarchy.rangeInfo[i][1]] > hierarchy.rangeInfo[i][0];
        }
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
     * Computes the immediate ancestors in the hierarchy of cuts. See Figure 4 in
     * "M. Terrovitis, N. Mamoulis, and P. Kalnis, “Privacy-preserving anonymization of set-valued data,”" for illustration
     *
     * @return all immediate ancestors in the hierarchy of cuts.
     */
    public List<Cut> ancestors() {
        List<Cut> ancestors = new ArrayList<>();
        IntArrayList nodes = new IntArrayList();

        // add leafs that have not been generalized  to the list
        for (int i = 0; i < this.generalization.length; ) {
            if (this.generalization[i] > 0) {
                i++;
                continue;
            }
            int level = 1;
            int generalization = hierarchy.toRoot(i)[level];

            nodes.add(generalization);
            i = hierarchy.rangeInfo[generalization][2] + 1;
        }

        // search for sets of nodes that can be generalized to the same node
        for (int i = 0; i < this.generalization.length; ) {
            if (this.generalization[i] == 0) {
                i++;
                continue;
            }
            int level = this.generalization[i];
            int generalization = hierarchy.toRoot(i)[level + 1];
            int nodeAboveEnd = hierarchy.rangeInfo[generalization][2];
            int nodeAboveStart = hierarchy.rangeInfo[generalization][1];


            boolean allEqual = true;
            for (int j = nodeAboveStart; j <= nodeAboveEnd; j++) {
                if (this.generalization[j] != level) {
                    allEqual = false;
                    break;
                }
            }
            i = hierarchy.rangeInfo[generalization][2] + 1;
            if (!allEqual)
                continue;
            nodes.add(generalization);
        }

        for (IntCursor node : nodes) {
            Cut c = new Cut(this.hierarchy);
            int start = hierarchy.rangeInfo[node.value][1];
            int end = hierarchy.rangeInfo[node.value][2];
            int level = hierarchy.rangeInfo[node.value][0];
            System.arraycopy(this.generalization, 0, c.generalization, 0, this.generalization.length);
            Arrays.fill(c.generalization, start, end + 1, level);
            ancestors.add(c);
        }


        return ancestors;
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

}
