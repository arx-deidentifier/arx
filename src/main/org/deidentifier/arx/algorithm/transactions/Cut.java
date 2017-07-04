package org.deidentifier.arx.algorithm.transactions;

import com.carrotsearch.hppc.IntOpenHashSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Cut {
    int[] generalization;
    private boolean horizontal;
    private int level;

    public Cut(int domainsize) {
        generalization = new int[domainsize];
        for (int i = 0; i < generalization.length; i++) {
            generalization[i] = i;
        }
        horizontal = true;
    }

    public void generalize(int a, int b) {
        generalization[a] = b;
        int mustLevel = this.generalization[0];
        for (int gen : this.generalization) {
            if (gen != mustLevel) {
                this.horizontal = false;
                return;
            }
        }
        this.horizontal = true;
    }

    public int getGeneralization(int a) {
        return generalization[a];
    }

    /**
     * Generalizes the database according to the cut. Returns a <strong>COPY</strong> of the database
     * TODO do not return copy but directly generalize the array
     *
     * @param d a database
     * @return a copy of d, generalized according to this cut
     */
    public int[][] generalize(int[][] d) {
        int[][] dp = new int[d.length][];
        for (int i = 0; i < d.length; i++) {
            dp[i] = new int[d[i].length];
        }
        arrayCopy(d, dp);
        for (int i = 0; i < dp.length; i++) {
            for (int j = 0; j < dp[i].length; j++) {
                dp[i][j] = generalization[dp[i][j]];
            }
            // remove duplicates TODO replace with own implementation based on boolean arrays/Bitsets?
            IntOpenHashSet h = new IntOpenHashSet(dp[i].length);
            h.add(dp[i]);
            dp[i] = h.toArray();
        }
        return dp;
    }

  /*
    Functionaly of anchestors():
    +---+---+---+---+
    | 6 | 6 | 6 | 6 |
    +---+---+---+---+
    | 4 | 4 | 5 | 5 |
    +---+---+---+---+
    | 0 | 1 | 2 | 3 |
    +---+---+---+---+
    If the anchestors of cut  <0, 1, 2, 3> should be computed, we group the values in the row above to one anchestor.
    With the above generalization. We group the values 4, 4 and 5, 5. We then get the cuts <4, 4, 2, 3> and <0, 1, 5, 5>.
    The computation of anchestors of one of these groups the four sixes in the top row to one cut and returns  <6, 6, 6, 6>
  */

    /**
     * Computes the anchestors in the hierarchy of cuts. See Figure 4 in
     * "M. Terrovitis, N. Mamoulis, and P. Kalnis, “Privacy-preserving anonymization of set-valued data,”" for illustration
     *
     * @param g the generalization hierarchy this cut is derived from
     * @return all anchestors in the hierarchy of cuts.
     */
    public List<Cut> anchestors(GenHierarchy g) {
        List<Cut> cuts = new LinkedList<>();
        int[][] gtable = g.getHierarchy();
        // this is not a horizontal cut, so the next higher cut is a horizontal one
        if (!horizontal) {
            level++;
            Cut horizontal = new Cut(this.generalization.length);
            for (int i = 0; i < this.generalization.length; i++) {
                horizontal.generalize(i, gtable[i][level]);
            }
            horizontal.level = level;
            return Collections.singletonList(horizontal);
        }
        int glevel = level + 1;
        // Do we really have to loop over all items? The ones under this cut should be sufficient?
        int group = 0;
        for (int i = 0; i < this.generalization.length; ) {
            if (g.toRoot(this.generalization[i]).length == 1)
                return cuts;

            int groupVal = gtable[i][glevel];
            Cut anAncestor = new Cut(this.generalization.length);


            while (group < gtable.length) {
                if (groupVal == gtable[group][glevel])
                    group++;
                else
                    break;
            }
            for (int j = 0; j < group-i; j++) {
                anAncestor.generalize(i+j, gtable[i+j][glevel]);
            }
            anAncestor.horizontal = false;
            cuts.add(anAncestor);
            i = group;
        }
        return cuts;
    }


    @Override
    public String toString() {
        return "Cut{" + "generalization=" + Arrays.toString(generalization) + '}';
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

    private static void arrayCopy(int[][] aSource, int[][] aDestination) {
        for (int i = 0; i < aSource.length; i++) {
            System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
        }
    }
}
