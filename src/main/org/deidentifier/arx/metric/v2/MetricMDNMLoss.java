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
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
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
        this(0.5d, AggregateFunction.GEOMETRIC_MEAN);
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

    @Override
    public String toString() {
        return "Loss ("+gsFactor+"/"+gFactor+"/"+sFactor+")";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupify g) {
        
        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int dimensionsAggregated = getDimensionsAggregated();
        int microaggregationStart = getMicroaggregationStartIndex();
        DistributionAggregateFunction[] microaggregationFunctions = getMicroaggregationFunctions();
        
        int[] transformation = node.getGeneralization();
        double[] result = new double[dimensions];
        double[] bound = new double[dimensions];

        // Compute NDS and lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.getCount()>0) {
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.getKey()[dimension];
                    int level = transformation[dimension];
                    double share = (double)m.getCount() * shares[dimension].getShare(value, level);
                    result[dimension] += m.isNotOutlier() ? share * gFactor :
                                         (sFactor == 1d ? m.getCount() : share + sFactor * ((double)m.getCount() - share));
                    bound[dimension] += share * gFactor;
                }
                for (int dimension=0; dimension<dimensionsAggregated; dimension++){
                    
                    double share = (double)m.getCount() * microaggregationFunctions[dimension].getMeanError(m.getDistributions()[microaggregationStart + dimension]);
                    result[dimensionsGeneralized + dimension] += m.isNotOutlier() ? share * gFactor :
                                         (sFactor == 1d ? m.getCount() : share + sFactor * ((double)m.getCount() - share));
                    // Note: we ignore the bound, as we cannot compute it
                }
            }
            m = m.getNextOrdered();
        }
        
        // Normalize
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
            result[dimension] = normalizeGeneralized(result[dimension], dimension);
            bound[dimension] = normalizeGeneralized(bound[dimension], dimension);
        }
        
        // Normalize
        for (int dimension=dimensionsGeneralized; dimension<dimensionsGeneralized + dimensionsAggregated; dimension++){
            result[dimension] = normalizeAggregated(result[dimension]);
        }
        
        // Return information loss and lower bound
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(bound));
        
    }
    
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {

        // Init
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int dimensionsAggregated = getDimensionsAggregated();
        int microaggregationStart = getMicroaggregationStartIndex();
        DistributionAggregateFunction[] microaggregationFunctions = getMicroaggregationFunctions();
        
        double[] result = new double[dimensions];
        int[] transformation = node.getGeneralization();

        // Compute
        for (int dimension = 0; dimension < dimensionsGeneralized; dimension++) {
            int value = entry.getKey()[dimension];
            int level = transformation[dimension];
            result[dimension] = (double) entry.getCount() * shares[dimension].getShare(value, level);
        }

        // Compute
        for (int dimension=0; dimension<dimensionsAggregated; dimension++){
            result[dimensionsGeneralized + dimension] = (double)entry.getCount() * microaggregationFunctions[dimension].getMeanError(entry.getDistributions()[microaggregationStart + dimension]);
        }
        
        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                               HashGroupify g) {
        
        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int[] transformation = node.getGeneralization();
        double[] bound = new double[dimensions];

        // Compute lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.getCount()>0) {
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.getKey()[dimension];
                    int level = transformation[dimension];
                    double share = (double)m.getCount() * shares[dimension].getShare(value, level);
                    bound[dimension] += share * gFactor;
                }
                // Note: we ignore microaggregation, as we cannot compute a bound for it
            }
            m = m.getNextOrdered();
        }
        
        // Normalize
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
            bound[dimension] = normalizeGeneralized(bound[dimension], dimension);
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
    
    @SuppressWarnings("unchecked")
    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(manager, definition, input, hierarchies, config);

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
        double[] min = new double[getDimensions()];
        Arrays.fill(min, 0d);
        double[] max = new double[getDimensions()];
        Arrays.fill(max, 1d);
        super.setMin(min);
        super.setMax(max);
    }

    /**
     * Returns whether this metric handles microaggregation
     * @return
     */
    protected boolean isAbleToHandleMicroaggregation() {
        return true;
    }

    /**
     * Normalizes the aggregate.
     *
     * @param aggregate
     * @param dimension
     * @return
     */
    protected double normalizeGeneralized(double aggregate, int dimension) {

        double min = gFactor * tuples / shares[dimension].getDomainSize();
        double max = tuples;
        double result = (aggregate - min) / (max - min);
        result = result >= 0d ? result : 0d;
        return round(result);
    }

    /**
     * Normalizes the aggregate.
     *
     * @param aggregate
     * @param dimension
     * @return
     */
    protected double normalizeAggregated(double aggregate) {
        double result = aggregate / tuples;
        result = result >= 0d ? result : 0d;
        return round(result);
    }
}
