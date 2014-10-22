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

package org.deidentifier.arx.metric.v2;

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * Normalized Domain Share
 * 
 * @author Fabian Prasser
 */
public class MetricMDNMLossPrecomputed extends MetricMDNMLoss {

    /** SUID */
    private static final long serialVersionUID = -7505441444551612996L;

    /** Cardinalities */
    private Cardinalities     cardinalities;
    /** Distinct values: attribute -> level -> values */
    private int[][][]         values;
    
    /**
     * Creates a new instance
     */
    protected MetricMDNMLossPrecomputed() {
        super();
    }

    /**
     * Creates a new instance
     * @param function
     */
    protected MetricMDNMLossPrecomputed(AggregateFunction function) {
        super(function);
    }

    /**
     * Creates a new instance
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMLossPrecomputed(double gsFactor,
                                                       AggregateFunction function) {
        super(gsFactor, function);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {

        // Prepare
        int[] transformation = node.getTransformation();
        int dimensions = transformation.length;
        double[] bound = new double[dimensions];
        DomainShare[] shares = super.getShares();
        double gFactor = super.getGeneralizationFactor();
        // Column -> Id -> Level -> Count
        int[][][] cardinalities = this.cardinalities.getCardinalities();


        // For each column
        for (int column = 0; column < cardinalities.length; column++) {

            // Check for cached value
            int level = node.getTransformation()[column];
            int[][] cardinality = cardinalities[column];
            int[] values = this.values[column][level];
            
            for (int value : values) {
                double count = cardinality[value][level];
                double share = count * shares[column].getShare(value, level);
                bound[column] += share * gFactor;
            }
        }
                
        // Normalize
        for (int column=0; column<dimensions; column++){
            bound[column] = normalize(bound[column], column);
        }
        
        // Return
        return super.createInformationLoss(bound);
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node, IHashGroupify g) {
        return this.getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare super
        super.initializeInternal(definition, input, hierarchies, config);

        // Compute cardinalities
        RowSet subset = null;
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criterion = config.getCriteria(DPresence.class);
            if (criterion.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported"); }
            subset = criterion.iterator().next().getSubset().getSet();
        } 
        
        // Cardinalities
        this.cardinalities = new Cardinalities(input, subset, hierarchies);
        
        // Distinct values
        this.values = new int[hierarchies.length][][];
        for (int i=0; i<values.length; i++) {
            values[i] = new int[hierarchies[i].getHeight()][];
            for (int j=0; j<values[i].length; j++){
                values[i][j] = hierarchies[i].getDistinctValues(j);
            }
        }
    }

    /**
     * Returns the configuration of this metric
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       true,      // precomputed
                                       1.0d,      // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }
}
