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

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * Normalized Domain Share
 * 
 * @author Fabian Prasser
 */
public class MetricMDNMNormalizedDomainShare extends AbstractMetricMultiDimensional {

    /** SUID */
    private static final long serialVersionUID = -573670902335136600L;

    /** Total number of tuples, depends on existence of research subset */
    private double            tuples;

    /** Domain shares for each dimension */
    private DomainShare[]     shares;

    /** Configuration factor */
    private final double      gFactor;
    /** Configuration factor */
    private final double      gsFactor;
    /** Configuration factor */
    private final double      sFactor;
    
    /**
     * Default constructor which treats all transformation methods equally
     */
    public MetricMDNMNormalizedDomainShare(){
        this(0.5d, AggregateFunction.RANK);
    }

    /**
     * Default constructor which treats all transformation methods equally
     */
    public MetricMDNMNormalizedDomainShare(AggregateFunction function){
        this(0.5d, function);
    }
    
    /**
     * A constructor that allows to define a factor weighting generalization and suppression
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor generalization, and a factor of 1 will favor
     *                 suppression. The values in between can be used for
     *                 balancing both methods.
     * @param function 
     */
    public MetricMDNMNormalizedDomainShare(double gsFactor, AggregateFunction function){
        super(false, false, function);
        this.gsFactor = gsFactor;
        this.sFactor = gsFactor <0.5d ? 2d * gsFactor : 1d;
        this.gFactor = gsFactor <=0.5d ? 1d : 1d - 2d * (gsFactor - 0.5d);
    }
    
    /**
     * Returns the factor used weight generalized values
     * @return
     */
    public double getGeneralizationFactor() {
        return gFactor;
    }
    
    /**
     * Returns the factor weighting generalization and suppression
     * 
     * @return A factor [0,1] weighting generalization and suppression. 
     *         The default value is 0.5, which means that generalization
     *         and suppression will be treated equally. A factor of 0
     *         will favor generalization, and a factor of 1 will favor
     *         suppression. The values in between can be used for
     *         balancing both methods.
     */
    public double getGeneralizationSuppressionFactor() {
        return gsFactor;
    }

    @Override
    public String getName() {
        return "Normalized domain share";
    }
    
    /**
     * Returns the factor used to weight suppressed values
     * @return
     */
    public double getSuppressionFactor() {
        return sFactor;
    }
    
    @Override
    public String toString() {
        return "Normalized domain share ("+gsFactor+"/"+gFactor+"/"+sFactor+")";
    }

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
                                         (sFactor == 1d ? m.count : share + sFactor * (1d - share));
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

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return null;
    }

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
     * For subclasses
     * @return
     */
    protected DomainShare[] getShares(){
        return this.shares;
    }
    
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
            shares[i] = new DomainShare(definition.getHierarchy(input.getHeader()[i]), input.getDictionary().getMapping()[i]);
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
     * Normalizes the aggregate
     * @param aggregate
     * @param dimension
     * @return
     */
    protected double normalize(double aggregate, int dimension) {

        double min = tuples / shares[dimension].getDomainSize();
        double max = tuples;
        double result = (aggregate - min) / (max - min);
        return result >= 0d ? result : 0d;
    }
}
