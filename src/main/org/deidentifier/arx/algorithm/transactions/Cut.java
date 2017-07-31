package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;

import java.util.*;


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
    }


    /**
     * Generalizes the item a to the item b
     *
     * @param a an item. Can be either leaf or generalization
     * @param b a generalization
     */
    @Deprecated
    protected void generalize(int a, int b) {
        generalization[a] = getLevel(a, b);
        this.level = generalization[a];

        // check if this cut is a horizontal one i.e. all generalizations are on the same level
        this.horizontal = isHorizontal();
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
     * leaves are set to level 3.
     *
     * @param item  the item to be generalized
     * @param level the level the item is generalized to
     */
    protected void generalizeToLevel(int item, int level) {
        if (item >= this.generalization.length) {
            for (int i = 0; i < this.generalization.length; i++) {
                int[] r = this.hierarchy.toRoot(i);
                for (int j = 0; j < r.length; j++) {
                    if (r[j] == item) {
                        item = i; //TODO break out if found
                    }
                }
            }
        }

        if(level < generalization[item]) // we don't allow lowering the cuts levels
            return;

        // ensure all leafs under the generalization at level are generalized
        int generalizationItem = hierarchy.getHierarchy()[item][level];
        for (int i = 0; i < hierarchy.zoningInfo[level].length; i += 3) {
            if (hierarchy.zoningInfo[level][i] == generalizationItem) {
                int start = hierarchy.zoningInfo[level][i + 1];
                int end = hierarchy.zoningInfo[level][i + 2];
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
     * @param a a leaf
     * @param b a generalization
     * @return the level at which the item b is occuring above a i.e. the generalization level
     */
    private int getLevel(int a, int b) {
        int level = 0;
        while (hierarchy.toRoot(a)[level] != b)
            level++;
        return level;
    }

    /**
     * @param l an item in the generalization hierarchy
     * @return the generalizing item for l according to this cut
     */
    public int getGeneralization(int l) {
        if (l < this.generalization.length)
            return this.hierarchy.getHierarchy()[l][generalization[l]];
        else { // search where the inner leaf l is generalized and to what
            // TODO search can be replaced with calculation. We know the order the hierarchy is enumerated, so we might be able to calculate the leaf where l is generlizing.
            for (int i = 0; i < this.generalization.length; i++) {
                int[] r = this.hierarchy.toRoot(i);
                for (int j = 0; j < r.length; j++) {
                    if (r[j] == l) {
                        if (j < this.generalization[i])
                            return this.hierarchy.getHierarchy()[i][this.generalization[i]];

                        else
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
    protected boolean isGeneralized(int i) {
        if (i < generalization.length)
            return generalization[i] > 0;
        else {
            for (int j = 0; j < generalization.length; j++) {
                int[] a = hierarchy.toRoot(j);
                for (int k = 0; k < a.length; k++) {
                    if (a[k] == i)
                        return generalization[j] > k;
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
            // remove duplicates TODO replace with own implementation based on boolean arrays/Bitsets?
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
     * TODO this method sucks big time
     */
    public List<Cut> ancestors() {
        List<Cut> cuts = new LinkedList<>();
        int[][] gtable = hierarchy.getHierarchy();


        if (!horizontal) {

            IntArrayList groupSet = new IntArrayList(gtable.length / 2);
            int[] groupInfo = hierarchy.zoningInfo[level];
            for (int i = 0; i < groupInfo.length; i += 3) {
                groupSet.add(i / 3);
            }

            // Generate Subsets
            Set<IntArrayList> generatedCuts = new HashSet<>();
            for (int i = 1; i <= groupSet.size(); i++) {
                SubsetIterator parentGroups = new SubsetIterator(groupSet.toArray(), i);
                while (parentGroups.hasNext()) {
                    int[] next = parentGroups.next();
                    // expand subsets back into cut.
                    Arrays.sort(next);

                    // generated subset to cut
                    int[] genCut = new int[this.generalization.length];
                    for (int j = 0; j < next.length; j++) {
                        int group = next[j];
                        int gStart = groupInfo[group * 3 + 1];
                        int gEnd = groupInfo[group * 3 + 2];
                        Arrays.fill(genCut, gStart, gEnd + 1, level);
                    }

                    // merge with this cut to get this level into the generated cut
                    merge(genCut, this.generalization);

                    // The generated cut is equal to the current cut. This is not an ancestor.
                    if (Arrays.equals(this.generalization, genCut))
                        continue;

                    generatedCuts.add(IntArrayList.from(genCut));
                }
            }
            for (IntArrayList gs : generatedCuts) {
                int[] generatedCut = gs.toArray();
                Cut c = new Cut(this.hierarchy);
                cuts.add(c);
                c.generalization = generatedCut;
                c.horizontal = c.isHorizontal();
                c.level = this.level;
            }

            return cuts;
        } else {
            return ancestorsHorizontalCut();
        }
    }

    private List<Cut> ancestorsHorizontalCut() {
        List<Cut> cuts = new LinkedList<>();
        int glevel = level + 1; // the current cut is a horizontal one, so we have to find all cuts above this one -> increment level
        int[] groups = hierarchy.zoningInfo[glevel];

        for (int i = 0; i < groups.length; i = i + 3) {
            Cut anc = new Cut(hierarchy);
            Arrays.fill(anc.generalization, groups[i + 1], groups[i + 2] + 1, glevel); // Arrays.fill 2nd argument is exclusive, but we store the last position inclusively, thus +1
            anc.horizontal = false;
            anc.level = glevel;
            cuts.add(anc);
        }
        return cuts;
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
        return "Cut{" +
                "generalization=" + Arrays.toString(generalization) +
                '}';
    }

    private static void arrayCopy(int[][] aSource, int[][] aDestination) {
        for (int i = 0; i < aSource.length; i++) {
            System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
        }
    }

    protected void merge(int[] other) {
        merge(this.generalization, other);
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

    protected void merge(Cut other) {
        merge(other.generalization);
    }

    protected int getGeneralizationLevel(int k) {
        if (k < this.generalization.length) {
            return this.generalization[k];
        } else {
            for (int i = 0; i < this.generalization.length; i++) {
                int[] r = this.hierarchy.toRoot(i);
                for (int j = 0; j < r.length; j++) {
                    if (r[j] == k) {
                        if (j < this.generalization[i])
                            return this.generalization[i];

                        else
                            return j;
                    }
                }
            }
        }
        throw new RuntimeException();
    }
}
