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

import java.util.Arrays;
import java.util.Set;

import org.apache.poi.ss.formula.functions.T;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements a variant of the Loss metric.
 *
 * @author Fabian Prasser
 */
public class MetricMDNMLoss extends AbstractMetricMultiDimensional {

    /** SUID. */
    private static final long serialVersionUID = -573670902335136600L;

    /** Total number of tuples, depends on existence of research subset. */
    private double            tuples;

    /** Domain shares for each dimension. */
    private DomainShare[]     shares;

    /** Configuration factor. */
    private final double      gFactor;
    
    /** Configuration factor. */
    private final double      gsFactor;
    
    /** Configuration factor. */
    private final double      sFactor;
    
    /**
     * Default constructor which treats all transformation methods equally.
     */
    public MetricMDNMLoss(){
        this(0.5d, AggregateFunction.RANK);
    }

    /**
     * Default constructor which treats all transformation methods equally.
     *
     * @param function
     */
    public MetricMDNMLoss(AggregateFunction function){
        this(0.5d, function);
    }
    
    /**
     * A constructor that allows to define a factor weighting generalization and suppression.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function
     */
    public MetricMDNMLoss(double gsFactor, AggregateFunction function){
        super(false, false, function);
        this.gsFactor = gsFactor;
        this.sFactor = gsFactor <  0.5d ? 2d * gsFactor : 1d;
        this.gFactor = gsFactor <= 0.5d ? 1d            : 1d - 2d * (gsFactor - 0.5d);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                        // monotonic
                                       gsFactor,                     // gs-factor
                                       false,                        // precomputed
                                       0.0d,                         // precomputation threshold
                                       this.getAggregateFunction()   // aggregate function
                                       );
    }
    
    /**
     * Returns the factor used weight generalized values.
     *
     * @return
     */
    public double getGeneralizationFactor() {
        return gFactor;
    }

    /**
     * Returns the factor weighting generalization and suppression.
     *
     * @return A factor [0,1] weighting generalization and suppression.
     *         The default value is 0.5, which means that generalization
     *         and suppression will be treated equally. A factor of 0
     *         will favor suppression, and a factor of 1 will favor
     *         generalization. The values in between can be used for
     *         balancing both methods.
     */
    public double getGeneralizationSuppressionFactor() {
        return gsFactor;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getName()
     */
    @Override
    public String getName() {
        return "Loss";
    }
    
    /**
     * Returns the factor used to weight suppressed values.
     *
     * @return
     */
    public double getSuppressionFactor() {
        return sFactor;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Loss ("+gsFactor+"/"+gFactor+"/"+sFactor+")";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Node node, IHashGroupify g) {
        
        // Prepare
        int[] transformation = node.getTransformation();
        int dimensions = transformation.length;
        double[] result = new double[dimensions];
        double[] bound = new double[dimensions];

        // Compute NDS and lower bound
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count>0) {
                for (int dimension=0; dimension<dimensions; dimension++){
                    int value = m.key[dimension];
                    int level = transformation[dimension];
                    double share = (double)m.count * shares[dimension].getShare(value, level);
                    result[dimension] += m.isNotOutlier ? share * gFactor :
                                         (sFactor == 1d ? m.count : share + sFactor * ((double)m.count - share));
                    bound[dimension] += share * gFactor;
                }
            }
            m = m.nextOrdered;
        }
        
        // Normalize
        for (int dimension=0; dimension<dimensions; dimension++){
            result[dimension] = normalize(result[dimension], dimension);
            bound[dimension] = normalize(bound[dimension], dimension);
        }
        
        // Return information loss and lower bound
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(bound));
        
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                               IHashGroupify g) {
        
        // Prepare
        int[] transformation = node.getTransformation();
        int dimensions = transformation.length;
        double[] bound = new double[dimensions];

        // Compute lower bound
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count>0) {
                for (int dimension=0; dimension<dimensions; dimension++){
                    int value = m.key[dimension];
                    int level = transformation[dimension];
                    double share = (double)m.count * shares[dimension].getShare(value, level);
                    bound[dimension] += share * gFactor;
                }
            }
            m = m.nextOrdered;
        }
        
        // Normalize
        for (int dimension=0; dimension<dimensions; dimension++){
            bound[dimension] = normalize(bound[dimension], dimension);
        }
        
        // Return
        return super.createInformationLoss(bound);
    }
    
    /**
     * For subclasses.
     *
     * @return
     */
    protected DomainShare[] getShares(){
        return this.shares;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(definition, input, hierarchies, config);

        // Compute domain shares
        this.shares = new DomainShare[hierarchies.length];
        for (int i=0; i<shares.length; i++) {
            
            // Extract info
            String attribute = input.getHeader()[i];
            String[][] hierarchy = definition.getHierarchy(attribute);
            HierarchyBuilder<?> builder = definition.getHierarchyBuilder(attribute);
            
            // Create shares for redaction-based hierarchies
            if ((builder instanceof HierarchyBuilderRedactionBased) &&
                ((HierarchyBuilderRedactionBased<?>)builder).isDomainPropertiesAvailable()){
                shares[i] = new DomainShareRedaction((HierarchyBuilderRedactionBased<?>)builder);
                
             // Create shares for interval-based hierarchies
            } else if (builder instanceof HierarchyBuilderIntervalBased){
                shares[i] = new DomainShareInterval<T>((HierarchyBuilderIntervalBased<T>)builder,
                                                        hierarchies[i].getArray(),
                                                        input.getDictionary().getMapping()[i]);
                
            // Create fallback-shares for materialized hierarchies
            } else {
                shares[i] = new DomainShareMaterialized(hierarchy, 
                                                        input.getDictionary().getMapping()[i],
                                                        hierarchies[i].getArray());
            }
        }
   
        // Determine total number of tuples
        this.tuples = input.getDataLength();
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criteria = config.getCriteria(DPresence.class);
            if (criteria.size() > 1) { 
                throw new IllegalStateException("Only one d-presence criterion supported!"); 
            } 
            this.tuples = criteria.iterator().next().getSubset().getArray().length;   
        } 
        
        // Min and max
        double[] min = new double[shares.length];
        Arrays.fill(min, 0d);
        double[] max = new double[shares.length];
        Arrays.fill(max, 1d);
        super.setMin(min);
        super.setMax(max);
    }

    /**
     * Normalizes the aggregate.
     *
     * @param aggregate
     * @param dimension
     * @return
     */
    protected double normalize(double aggregate, int dimension) {

        double min = gFactor * tuples / shares[dimension].getDomainSize();
        double max = tuples;
        double result = (aggregate - min) / (max - min);
        result = result >= 0d ? result : 0d;
        return round(result);
    }
}
