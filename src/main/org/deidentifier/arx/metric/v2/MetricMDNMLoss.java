/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

import com.carrotsearch.hppc.ObjectLongOpenHashMap;

/**
 * This class implements a variant of the Loss metric.
 *
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class MetricMDNMLoss extends AbstractMetricMultiDimensional {

    /** SUID. */
    private static final long         serialVersionUID = -573670902335136600L;

    /** Total number of tuples, depends on existence of research subset. */
    private double                    tuples;

    /** Domain shares for each dimension. */
    private DomainShare[]             shares;

    /** Reliable domain shares for each dimension. */
    private DomainShareMaterialized[] sharesReliable;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double              gFactor;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double              gsFactor;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double              sFactor;

    /** Minimal size of equivalence classes enforced by the differential privacy model */
    private double                    k;
    
    /**
     * Default constructor which treats all transformation methods equally.
     */
    public MetricMDNMLoss(){
        this(0.5d, AggregateFunction.GEOMETRIC_MEAN);
    }

    /** Default constructor which treats all transformation methods equally.
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
        super(true, false, false, function);
        if (gsFactor < 0d || gsFactor > 1d) {
            throw new IllegalArgumentException("Parameter must be in [0, 1]");
        }
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
    
    @Override
    public double getGeneralizationFactor() {
        return gFactor;
    }
    
    @Override
    public double getGeneralizationSuppressionFactor() {
        return gsFactor;
    }

    @Override
    public String getName() {
        return "Loss";
    }
    
    @Override
    public ILScoreDouble getScore(final Transformation node, final HashGroupify groupify) {
        // Prepare
        int[] transformation = node.getGeneralization();
        int dimensionsGeneralized = getDimensionsGeneralized();

        // Compute score
        double score = 0d;
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            m.read();
            for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                if (m.count>0) {
                    int value = m.next();
                    int level = transformation[dimension];
                    double share = (double)m.count * shares[dimension].getShare(value, level);
                    score += m.isNotOutlier ? share : m.count;
                }
                score += m.pcount - m.count;
            }
            m = m.nextOrdered;
        }

        // Divide by sensitivity and multiply with -1 so that higher values are better
        score *= -1d / dimensionsGeneralized;
        if (k > 1) score /= k - 1d;

        // Return
        return new ILScoreDouble(score);
    }

    @Override
    public ILScoreBigFraction getScoreReliable(final Transformation node, final HashGroupify groupify) {
        // Prepare
        int[] transformation = node.getGeneralization();
        int dimensionsGeneralized = getDimensionsGeneralized();
        List<ObjectLongOpenHashMap<BigFraction>> dimensionSharesToCount =
                new ArrayList<ObjectLongOpenHashMap<BigFraction>>(dimensionsGeneralized);
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
            dimensionSharesToCount.add(new ObjectLongOpenHashMap<BigFraction>());
        }

        // Calculate counts
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        long numOutliers = 0;
        while (m != null) {
            m.read();
            for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                if (m.count>0) {
                    if (!m.isNotOutlier) {
                        numOutliers += m.count;
                    } else {
                        int value = m.next();
                        int level = transformation[dimension];
                        BigFraction shareReliable = sharesReliable[dimension].getShareReliable(value, level);
                        ObjectLongOpenHashMap<BigFraction> sharesToCount = dimensionSharesToCount.get(dimension);
                        sharesToCount.putOrAdd(shareReliable, m.count, m.count);
                    }
                }
                numOutliers += m.pcount - m.count;
            }
            m = m.nextOrdered;
        }
        
        // Calculate score
        BigFraction score = new BigFraction(0);
        for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
            
            ObjectLongOpenHashMap<BigFraction> sharesToCount = dimensionSharesToCount.get(dimension);
            final boolean[] states = sharesToCount.allocated;
            final long[] counts = sharesToCount.values;
            final Object[] sharesReliable = sharesToCount.keys;
            
            for (int i=0; i<states.length; i++) {
                if (states[i]) {
                    score = score.add(((BigFraction)(sharesReliable[i])).multiply(counts[i]));
                }
            }
        }
        score = score.add(numOutliers);

        // Divide by sensitivity and multiply with -1 so that higher values are better
        score = score.multiply(new BigFraction(-1, dimensionsGeneralized));
        if (k > 1) score = score.divide(new BigFraction(k - 1d));

        // Return
        return new ILScoreBigFraction(score);
    }
    
    @Override
    public double getSuppressionFactor() {
        return sFactor;
    }

    @Override
    public boolean isAbleToHandleMicroaggregation() {
        return true;
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public boolean isReliableScoreFunctionSupported() {
        return true;
    }

    @Override
    public boolean isScoreFunctionSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Loss");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
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
        int[] microaggregationIndices = getAggregationIndicesNonGeneralized();
        DistributionAggregateFunction[] microaggregationFunctions = getAggregationFunctionsNonGeneralized();
        
        int[] transformation = node.getGeneralization();
        double[] result = new double[dimensions];
        double[] bound = new double[dimensions];

        // Compute information loss and lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                m.read();
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.next();
                    int level = transformation[dimension];
                    double share = (double)m.count * shares[dimension].getShare(value, level);
                    result[dimension] += m.isNotOutlier ? share * gFactor :
                                         (sFactor == 1d ? m.count : share + sFactor * ((double)m.count - share));
                    bound[dimension] += share * gFactor;
                }
                for (int dimension=0; dimension<dimensionsAggregated; dimension++){
                    
                    double share = (double) m.count *
                                   microaggregationFunctions[dimension].getInformationLoss(m.distributions[microaggregationIndices[dimension]]);
                    result[dimensionsGeneralized + dimension] += m.isNotOutlier ? share * gFactor :
                                         (sFactor == 1d ? m.count : share + sFactor * ((double)m.count - share));
                    // Note: we ignore a bound for microaggregation, as we cannot compute it
                    // this means that the according entries in the resulting array are not changed and remain 0d
                    // This is not a problem, as it is OK to underestimate information loss when computing lower bounds
                }
            }
            m = m.nextOrdered;
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
        int[] microaggregationIndices = getAggregationIndicesNonGeneralized();
        DistributionAggregateFunction[] microaggregationFunctions = getAggregationFunctionsNonGeneralized();
        
        double[] result = new double[dimensions];
        int[] transformation = node.getGeneralization();

        // Compute
        entry.read();
        for (int dimension = 0; dimension < dimensionsGeneralized; dimension++) {
            int value = entry.next();
            int level = transformation[dimension];
            result[dimension] = (double) entry.count * shares[dimension].getShare(value, level);
        }

        // Compute
        for (int dimension=0; dimension<dimensionsAggregated; dimension++){
            result[dimensionsGeneralized + dimension] = (double) entry.count *
                    microaggregationFunctions[dimension].getInformationLoss(entry.distributions[microaggregationIndices[dimension]]);
        }
        
        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node, HashGroupify g) {
        
        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int[] transformation = node.getGeneralization();
        double[] bound = new double[dimensions];

        // Compute lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                m.read();
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.next();
                    int level = transformation[dimension];
                    double share = (double)m.count * shares[dimension].getShare(value, level);
                    bound[dimension] += share * gFactor;
                }
                // Note: we ignore microaggregation, as we cannot compute a bound for it
                // this means that the according entries in the resulting array are not changed and remain 0d
                // This is not a problem, as it is OK to underestimate information loss when computing lower bounds
            }
            m = m.nextOrdered;
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

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Determine total number of tuples
        this.tuples = (double)super.getNumRecords(config, input);
        
        // Save domain shares
        this.shares = manager.getDomainShares();

        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            // Store minimal size of equivalence classes
            EDDifferentialPrivacy dpCriterion = config.getPrivacyModel(EDDifferentialPrivacy.class);
            this.k = (double)dpCriterion.getMinimalClassSize();
            if (config.isReliableAnonymizationEnabled()) {
                // Assure that no overflow may occur during the computation of reliable scores
                if(this.tuples * (double)getDimensionsGeneralized() > (double)Long.MAX_VALUE) {
                    throw new RuntimeException("Too many records and attributes to perform reliable computations");
                }
                // Save reliable domain shares
                this.sharesReliable = manager.getDomainSharesReliable();
            }
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
}
