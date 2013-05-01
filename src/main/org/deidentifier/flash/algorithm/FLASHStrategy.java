/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.algorithm;

import java.util.Comparator;

import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Lattice;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class implements the general strategy of the FLASH algorithm.
 * 
 * @author Prasser, Kohlmayer
 */
public class FLASHStrategy implements Comparator<Node> {

    /** The distinct values. */
    private final int[][]    distinct;

    /** The maximal level in the lattice. */
    private final int        maxlevel;

    /** The maximal level for each qi */
    private final int[]      maxLevels;

    /** The criteria for a node with id 'index'. */
    private final double[][] values;

    /**
     * Creates a new instance.
     * 
     * @param lattice
     *            the lattice
     * @param hier
     *            the hier
     */
    public FLASHStrategy(final Lattice lattice,
                         final GeneralizationHierarchy[] hier) {

        maxLevels = lattice.getMaxLevels();
        distinct = new int[hier.length][];
        for (int i = 0; i < hier.length; i++) {
            distinct[i] = hier[i].getDistinctValues();
        }
        maxlevel = lattice.getLevels().length - 1;
        values = new double[lattice.getSize()][];
    }

    /**
     * Compares nodea according to their criteria.
     * 
     * @param n1
     *            the n1
     * @param n2
     *            the n2
     * @return the int
     */
    @Override
    public int compare(final Node n1, final Node n2) {

        // Obtain vals
        if (values[n1.id] == null) {
            values[n1.id] = getValue(n1);
        }
        if (values[n2.id] == null) {
            values[n2.id] = getValue(n2);
        }
        final double[] m1 = values[n1.id];
        final double[] m2 = values[n2.id];

        // Compare vals
        if (m1[0] < m2[0]) {
            return -1;
        } else if (m1[0] > m2[0]) {
            return +1;
        } else if (m1[1] < m2[1]) {
            return -1;
        } else if (m1[1] > m2[1]) {
            return +1;
        } else if (m1[2] < m2[2]) {
            return -1;
        } else if (m1[2] > m2[2]) {
            return +1;
        } else {
            return 0;
        }
    }

    /**
     * Returns the criteria for the given node.
     * 
     * @param node
     *            the node
     * @return the value
     */
    private final double[] getValue(final Node node) {
        double level = 0;
        double prec = 0;
        double ddistinct = 0;

        level = ((double) node.getLevel() / (double) maxlevel);

        final int[] state = node.getTransformation();
        for (int i = 0; i < state.length; i++) {
            ddistinct += ((double) distinct[i][state[i]] / (double) (distinct[i][0]));
            prec += ((double) state[i] / (double) (maxLevels[i] - 1));
        }
        ddistinct /= state.length;
        prec /= state.length;
        ddistinct = 1d - ddistinct;

        return new double[] { level, prec, ddistinct };
    }
}
