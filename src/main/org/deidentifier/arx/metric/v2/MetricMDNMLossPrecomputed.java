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
 * This class implements a variant of the Loss metric.
 * TODO: Add reference.
 *
 * @author Fabian Prasser
 */
public class MetricMDNMLossPrecomputed extends MetricMDNMLoss {

    /** SUID. */
    private static final long serialVersionUID = -7505441444551612996L;

    /** Cardinalities. */
    private Cardinalities     cardinalities;
    
    /** Distinct values: attribute -> level -> values. */
    private int[][][]         values;
    
    /**
     * Creates a new instance.
     */
    protected MetricMDNMLossPrecomputed() {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNMLossPrecomputed(AggregateFunction function) {
        super(function);
    }

    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     */
    protected MetricMDNMLossPrecomputed(double gsFactor,
                                                       AggregateFunction function) {
        super(gsFactor, function);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       true,      // precomputed
                                       1.0d,      // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNMLoss#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNMLoss#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node, IHashGroupify g) {
        return this.getLowerBoundInternal(node);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNMLoss#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
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
}
