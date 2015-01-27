/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.algorithm;

import java.util.Comparator;

import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class implements the general strategy of the ARX algorithm.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHStrategy implements Comparator<Node> {

    /** The distinct values. */
    private final int[][]    distinct;

    /** The maximal level in the lattice. */
    private final int        maxlevel;

    /** The maximal level for each qi. */
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

        maxLevels = lattice.getTop().getTransformation().clone();
        for (int i=0; i < maxLevels.length; i++) {
            maxLevels[i] ++;
        }
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

        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            ddistinct += ((double) distinct[i][transformation[i]] / (double) (distinct[i][0]));
            prec += ((double) transformation[i] / (double) (maxLevels[i] - 1));
        }
        ddistinct /= transformation.length;
        prec /= transformation.length;
        ddistinct = 1d - ddistinct;

        return new double[] { level, prec, ddistinct };
    }
}
