/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.criteria;

import java.util.Arrays;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.v2.Cardinalities;

/**
 */
public class DPresenceWOWorldKnowledge extends DPresence {
    
    private static final long serialVersionUID = 7626327802076814771L;
    
    private int populationSize;
    private int sampleSize;
    
    // column -> valueID -> level -> count
    private int[][][] distributions;
    
    public DPresenceWOWorldKnowledge(final double dMin,
                                     final double dMax,
                                     final DataSubset subset,
                                     final int populationSize,
                                     final int[][][] distributions) {
        super(dMin, dMax, subset);
        this.populationSize = populationSize;
        this.distributions = distributions;
        this.sampleSize = subset.getSet().length();
    }
    
    public DPresenceWOWorldKnowledge(final double dMin,
                                     final double dMax,
                                     final DataSubset subset,
                                     final int populationSize) {
        super(dMin, dMax, subset);
        this.populationSize = populationSize;
        this.sampleSize = subset.getSet().length();
        
    }
    
    /**
     * For building the inclusion criterion.
     *
     * @param subset
     */
    protected DPresenceWOWorldKnowledge(final DataSubset subset) {
        super(subset);
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#initialize(org.deidentifier.arx.framework.data.DataManager)
     */
    @Override
    public void initialize(final DataManager manager) {
        
        // If no distribution provided, use the distribution of the dataset.
        if (distributions == null) {
            
            // We only consider quasi identifiers
            final Data dataObject = manager.getDataGeneralized();
            
            // Calculate distributions over the whole dataset
            // column -> valueID -> level -> count
            this.distributions = new Cardinalities(dataObject, null, manager.getHierarchies()).getCardinalities();
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#getRequirements()
     */
    @Override
    public int getRequirements() {
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#isAnonymous(org.deidentifier.arx.framework.check.groupify.
     * HashGroupifyEntry)
     */
    @Override
    public boolean isAnonymous(final HashGroupifyEntry entry, final Node transformation) {
        double delta = 0;
        if (entry.count == 0 || getDMin() == 0d) {
            delta = 0d;
        } else {
            delta = l_n_x(1, entry, transformation);
            
        }
        return (delta >= getDMin()) && (delta <= getDMax());
    }
    
    private double l_n_x(int x, HashGroupifyEntry entry, Node transformation) {
        // brauch einen integer
        final int u = populationSize;
        final int n = entry.key.length;
        double[] a1 = new double[u + 1];
        double[] a2 = new double[u + 1];
        
        // t*[1] is the value on the first position of the entry
        final int idx = getF_i_t_star_i(entry.key[0], 0, transformation.getTransformation()[0]);
        a1[idx - 1] = 1;
        
        a2 = Arrays.copyOf(a1, a1.length);
        
        for (int i = 2; i <= n; i++) {
            final int m = getF_i_t_star_i(entry.key[i - 1], i - 1, transformation.getTransformation()[i - 1]);
            for (int j = 1; j <= x; j++) {
                
                double sum = 0d;
                for (int y = 1; y <= u; y++) {
                    double hyp = 0d;
                    if (j >= 0 && j <= y) {
                        hyp = (binom(m, j) * binom(u - m, y - j)) / binom(u, y);
                    }
                    sum += a1[y - 1] * hyp;
                }
                a2[j - 1] = sum;
            }
            a1 = Arrays.copyOf(a2, a2.length);
        }
        return a1[x - 1];
    }
    
    private double binom(final int a, final int b) {
        if (a >= b) {
            return ArithmeticUtils.binomialCoefficient(a, b);
        } else {
            return 0;
        }
    }
    
    /**
     * Return the cardinality (frequency) of the value t_star at column i, generalized to the given level.
     * 
     * @param t_star_i
     * @param i
     * @param level
     * @return
     */
    private int getF_i_t_star_i(final int t_star_i, final int i, final int level) {
        // f_1 is distribution[0] --> f_x == distribution[x-1]
        return distributions[i][t_star_i][level];
        // return (int)((((double)distributions[i][t_star_i][level])/ (double)sampleSize) * populationSize);
    }
}
