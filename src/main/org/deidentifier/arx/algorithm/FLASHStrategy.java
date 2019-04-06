/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.math.BigInteger;
import java.util.Comparator;

import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * This class implements a total order on all transformations in the search space. It is
 * used by the Flash algorithm to achieve stable execution times.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHStrategy implements Comparator<Integer> {

    /** The distinct values. */
    private final int[][]       distinct;

    /** The maximal level in the lattice. */
    private final int           maxlevel;

    /** The maximal level for each quasi-identifier. */
    private final int[]         maxLevels;

    /** The cached values for a node with id 'index'. */
    private final double[][]    cache;

    /** The solution space */
    private final SolutionSpace<?> solutionSpace;

    /**
     * Creates a new instance.
     * 
     * @param solutionSpace the solution space
     * @param hierarchies the hierarchies
     */
    public FLASHStrategy(final SolutionSpace<?> solutionSpace,
                         final GeneralizationHierarchy[] hierarchies) {

        // Check
        if (solutionSpace.getSize().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("Solution space is too large for running Flash. Choose another algorithm.");
        }
        
        // Store
        this.solutionSpace = solutionSpace;
        
        // Prepare information about levels
        int level = 0;
        this.maxLevels = solutionSpace.getTop().getGeneralization().clone();
        for (int i=0; i < this.maxLevels.length; i++) {
            level += this.maxLevels[i];
            this.maxLevels[i] ++;
        }
        this.maxlevel = level;
        
        // Prepare information about distinct values
        this.distinct = new int[hierarchies.length][];
        for (int i = 0; i < hierarchies.length; i++) {
            this.distinct[i] = hierarchies[i].getDistinctValues();
        }
        
        // Prepare cache
        this.cache = new double[solutionSpace.getSize().intValue()][];
    }

    /**
     * Compares transformations.
     * 
     * @param n1
     *            the n1
     * @param n2
     *            the n2
     * @return the int
     */
    @Override
    public int compare(final Integer n1, final Integer n2) {

        // Obtain vals
        if (cache[n1] == null) {
            cache[n1] = getCriteria(n1);
        }
        if (cache[n2] == null) {
            cache[n2] = getCriteria(n2);
        }
        final double[] m1 = cache[n1];
        final double[] m2 = cache[n2];

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
     * Returns the criteria that determines a transformations's position.
     * 
     * @param id the id
     * @return the value
     */
    private final double[] getCriteria(final int id) {
        
        // Prepare
        double level = 0;
        double prec = 0;
        double ddistinct = 0;
        Transformation<?> transformation = solutionSpace.getTransformation((long)id);
        int[] generalization = transformation.getGeneralization();
        
        // Compute
        level = ((double) transformation.getLevel() / (double) maxlevel);
        for (int i = 0; i < generalization.length; i++) {
            ddistinct += ((double) distinct[i][generalization[i]] / (double) (distinct[i][0]));
            prec += ((double) generalization[i] / (double) (maxLevels[i] - 1));
        }
        ddistinct /= generalization.length;
        prec /= generalization.length;
        ddistinct = 1d - ddistinct;
        
        // Return
        return new double[] { level, prec, ddistinct };
    }
}
