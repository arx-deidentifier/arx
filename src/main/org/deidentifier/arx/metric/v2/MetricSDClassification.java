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

package org.deidentifier.arx.metric.v2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.MetaHashGroupify;
import org.deidentifier.arx.framework.check.groupify.MetaHashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the classification metric.
 * 
 * @author Fabian Prasser
 * @author Johanna Eicher
 * @author Raffael Bild
 */
public class MetricSDClassification extends AbstractMetricSingleDimensional {

    /** SVUID. */
    private static final long serialVersionUID                     = -7940144844158472876L;

    /** Indices of response variables in distributions */
    private int[]             responseVariablesNonQI               = null;
    /** Indices of response variables in quasi-identifiers */
    private int[]             responseVariablesQI                  = null;
    /** Scale factors for QI target variables */
    private double[][]        responseVariablesQIScaleFactors      = null;

    /** Penalty */
    private double            penaltySuppressed                    = 1d;
    /** Penalty */
    private double            penaltyInfrequentResponse            = 1d;
    /** Penalty */
    private double            penaltyNoMajorityResponse            = 1d;
    /** Maximal penality */
    private double            penaltyMax;
    /** Maximal penality */
    private double            penaltyMaxScale;

    /** Sensitivity of the score function */
    private BigInteger        sensitivity                          = null;

    /**
     * Creates a new instance.
     */
    protected MetricSDClassification() {
        super(false, false, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param gsFactor
     */
    protected MetricSDClassification(double gsFactor) {
        super(false, false, false, gsFactor);
    }

    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        return new ILSingleDimensional(1d);
    }
    
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(0d);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false, // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       false, // precomputed
                                       0.0d,  // precomputation threshold
                                       AggregateFunction.SUM // aggregate function
        );
    }
    
    /**
     * Penalty for records with non-majority response
     * @return the penaltyDifferentFromMajority
     */
    public double getPenaltyInfrequentResponse() {
        return penaltyInfrequentResponse;
    }
    
    /**
     * Penalty for records without a majority response
     * @return the penaltyNoMajority
     */
    public double getPenaltyNoMajorityResponse() {
        return penaltyNoMajorityResponse;
    }

    /**
     * Penalty for suppressed features
     * @return the penaltySuppressed
     */
    public double getPenaltySuppressed() {
        return penaltySuppressed;
    }
    
    @Override
    /**
     * Implements an extended version of the score function described in Section 5.5 of the article
     * 
     * Bild R, Kuhn KA, Prasser F. SafePub: A Truthful Data Anonymization Algorithm With Strong Privacy Guarantees.
     * Proceedings on Privacy Enhancing Technologies. 2018(1):67-87.
     */
    public ILScore getScore(final Transformation<?> node, final HashGroupify groupify) {
        
        if (sensitivity == null) {
            throw new RuntimeException("Parameters required for differential privacy have not been initialized yet");
        }
        
        // Prepare
        BigFraction score = BigFraction.ZERO;
        
        // Sum up weights for non-QI target variables
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0 && m.isNotOutlier) {
                for (int index : this.responseVariablesNonQI) {
                    score = score.add(BigInteger.valueOf(getStatistics(m.distributions[index])[1]));
                }
            }
            m = m.nextOrdered;
        }
        
        // Sum up scores for QI target variables
        int i = 0;
        for (int index : this.responseVariablesQI) {
            
            BigFraction scoreQI = BigFraction.ZERO;
            
            // Group equivalence classes
            MetaHashGroupify mhg = new MetaHashGroupify(groupify, index);
            m = groupify.getFirstEquivalenceClass();
            while (m != null) {
                if (m.count > 0 && m.isNotOutlier) {
                    mhg.add(m);
                }
                m = m.nextOrdered;
            }
            
            // Sum up weights
            MetaHashGroupifyEntry e = mhg.getFirstEntry();
            while (e != null) {
                scoreQI = scoreQI.add(BigInteger.valueOf(getStatistics(e.distribution)[1]));
                e = e.nextOrdered;
            }
            
            // Obtain scale between 1 (in case the target variable is not generalized) and 0 (in case the target variable is generalized to the highest level)
            int maxLevel = this.responseVariablesQIScaleFactors[i].length - 1;
            BigFraction scale = BigFraction.ONE.subtract(new BigFraction(node.getGeneralization()[index], maxLevel));
            
            // Multiply the score for this QI by scale in order to penalize high degrees of generalization.
            // This can only reduce the effects of the addition or removal of one record and hence
            // result in at most too conservative privacy guarantees.
            scoreQI = scoreQI.multiply(scale);
            
            // Add to the overall score value
            score = score.add(scoreQI);
            
            i++;
        }
        
        // Divide by sensitivity and return
        return new ILScore(score.divide(this.sensitivity));
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }
    
    @Override
    public boolean isScoreFunctionSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Classification accuracy");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Penalty for suppressed features", this.getPenaltySuppressed());
        result.addProperty("Penalty for records with non-majority response", this.getPenaltyInfrequentResponse());
        result.addProperty("Penalty for records without a majority response", this.getPenaltyNoMajorityResponse());
        return result;
    }

    @Override
    public String toString() {
        return "Classification accuracy";
    }
    
    /**
     * Returns sorted indices of response variables in the given data
     * @param definition
     * @param data
     * @return
     */
    private int[] getIndices(DataDefinition definition, Data data) {

        // Extract indices of response variables
        List<Integer> indices = new ArrayList<>();
        for (String variable : definition.getResponseVariables()){
            int index = data.getIndexOf(variable);
            if (index != -1) {
                indices.add(index);
            }
        }
        
        // Store information about response variables
        Collections.sort(indices);
        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            result[i] = indices.get(i);
        }
        
        // Return
        return result;
    }
    
    /**
     * Returns the penalty for a distribution
     * @param distribution
     * @param scaleFactor 
     * @return
     */
    private double getPenaltyDistribution(Distribution distribution, double scaleFactor) {
        
        int[] statistics = getStatistics(distribution);

        int count = statistics[0];
        int top1 = statistics[1];
        int top2 = statistics[2];
        
        if (scaleFactor == 1d) {
            return count * (penaltyMaxScale / penaltyMax);
        }
        
        // If a majority class label exists
        if (top1 != top2) {

            // Records with other than majority class label get penalized
            double penalty = penaltyInfrequentResponse * (1d - scaleFactor) + penaltyMaxScale * scaleFactor;
            return (count - top1) * (penalty / penaltyMax);
            
        } else {
            
            // All records get penalized
            double penalty = penaltyNoMajorityResponse * (1d - scaleFactor) + penaltyMaxScale * scaleFactor;
            return count * (penalty / penaltyMax);
        }
    }
    
    /**
     * Returns the penalty for an outlier
     * @param count
     * @return
     */
    private double getPenaltyOutlier(int count) {

        // According penalty for all records and response variables in this class
        return count * (penaltySuppressed / penaltyMax);
    }
    
    /**
     * Returns statistics about a distribution
     * @param distribution
     * @return an array containing the following three frequencies (in this order):
     *         - the total number of attribute values
     *         - the frequency of the most frequent attribute value
     *         - the frequency of the second most frequent attribute value
     */
    private int[] getStatistics(Distribution distribution) {
        
        // Find frequencies of most frequent and second most frequent attribute values
        
        int[] statistics = new int[] {0,-1,-1}; 
        
        // For each bucket
        int[] buckets = distribution.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            
            // bucket not empty
            if (buckets[i] != -1) { 
            
                // Get frequency
                int frequency = buckets[i + 1];
                statistics[0] += frequency;
                boolean largerThanTop1 = frequency > statistics[1];
                boolean largerThanTop2 = frequency > statistics[2];
                
                // Step 1: If frequency is > top1 
                //         --> top1 is moved down to top2
                statistics[2] = largerThanTop1 ? statistics[1] : statistics[2];

                // Step 2: If frequency is > top1 
                //         --> top1 is set to new frequency
                statistics[1] = largerThanTop1 ? frequency : statistics[1];
                
                // Step 3: If frequency is > top2 but not > top1
                //         --> top2 is set to new frequency
                statistics[2] = largerThanTop2 && !largerThanTop1 ? frequency : statistics[2];
            }
        }
        
        return statistics;
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Transformation<?> node, final HashGroupify g) {

        // Prepare
        double penalty = 0d;
        
        // Sum up penalties for non-QI target variables
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0) {
                if (!m.isNotOutlier) {
                    penalty += getPenaltyOutlier(m.count * this.responseVariablesNonQI.length);
                } else {
                    for (int index : this.responseVariablesNonQI) {
                        penalty += getPenaltyDistribution(m.distributions[index], 0d);
                    }
                }
            }
            m = m.nextOrdered;
        }
        
        // Sum up penalties for QI target variables
        int i = 0;
        for (int index : this.responseVariablesQI) {
            
            // Scale factor
            double scaleFactor = this.responseVariablesQIScaleFactors[i][node.getGeneralization()[index]];
            
            // Group equivalence classes
            MetaHashGroupify mhg = new MetaHashGroupify(g, index);
            m = g.getFirstEquivalenceClass();
            while (m != null) {
                if (m.count > 0) {
                    if (!m.isNotOutlier) {
                        penalty += getPenaltyOutlier(m.count);
                    } else {
                        mhg.add(m);
                    }
                }
                m = m.nextOrdered;
            }
            
            // Sum up penalties
            MetaHashGroupifyEntry e = mhg.getFirstEntry();
            while (e != null) {
                penalty += getPenaltyDistribution(e.distribution, scaleFactor);
                e = e.nextOrdered;
            }
            i++;
        }
        
        // Return
        // TODO: Can a lower bound be calculated for this model?
        return createInformationLoss(penalty);
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation<?> node, HashGroupifyEntry m) {

        // TODO: Can a lower bound be calculated for this model?
        // TODO: We can not consider QI target variables here...
        if (m.count > 0) {
            double penalty = 0d;
            if (!m.isNotOutlier) {
                penalty = getPenaltyOutlier(m.count);
            } else {
                for (int index : this.responseVariablesNonQI) {
                    penalty = getPenaltyDistribution(m.distributions[index], 0d);
                }
            }
            return createInformationLoss(penalty);
        } else {
            return createInformationLoss(0d);
        }
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation<?> node) {
        // TODO: Can a lower bound be calculated for this model?
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation<?> node, HashGroupify groupify) {
        // TODO: Can a lower bound be calculated for this model?
        return null;
    }

    @Override
    protected void initializeInternal(DataManager manager,
                                      DataDefinition definition,
                                      Data input,
                                      GeneralizationHierarchy[] hierarchies,
                                      ARXConfiguration config) {

        // Super
        super.initializeInternal(manager, definition, input, hierarchies, config);
        
        // Extract response variables
        this.responseVariablesNonQI = getIndices(definition, manager.getDataAnalyzed());
        this.responseVariablesQI = getIndices(definition, manager.getDataGeneralized());

        // Set penalties using the gs-factor. This is sort of a hack but should be OK for now.
        penaltySuppressed            = super.getGeneralizationSuppressionFactor();
        penaltyInfrequentResponse    = super.getGeneralizationFactor();
        penaltyNoMajorityResponse    = super.getSuppressionFactor();
        penaltyMaxScale              = Math.max(penaltySuppressed, Math.max(penaltyInfrequentResponse, penaltyNoMajorityResponse));
        penaltyMax                   = penaltyMaxScale * super.getNumTuples();
        
        // Calculate scale factors
        this.responseVariablesQIScaleFactors = new double[this.responseVariablesQI.length][];
        int i = 0;
        for (int index : responseVariablesQI) {
            
            // Obtain hierarchy
            GeneralizationHierarchy hierarchy = hierarchies[index];
            int distinct0 = hierarchy.getDistinctValues(0).length;
            int levels = hierarchy.getLevels();
            this.responseVariablesQIScaleFactors[i] = new double[levels];
            
            // For each level
            for (int level = 0; level < levels; level++) {
                
                // Special case
                if (distinct0 == 1) {
                    this.responseVariablesQIScaleFactors[i][level] = 0d;
                } else {
                    int distinctLevel = hierarchy.getDistinctValues(level).length;
                    this.responseVariablesQIScaleFactors[i][level] = 1d - ((double)(distinctLevel - 1) / (double)(distinct0 - 1));
                }
            }
            i++;
        }
        
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            EDDifferentialPrivacy dpCriterion = config.getPrivacyModel(EDDifferentialPrivacy.class);
            // The overall sensitivity is the sum of the sensitivity k for each response variable
            int k = dpCriterion.getMinimalClassSize();
            long numResponseVariables = (long)this.responseVariablesNonQI.length + (long)this.responseVariablesQI.length;
            this.sensitivity = BigInteger.valueOf(k).multiply(BigInteger.valueOf(numResponseVariables));
        }
    }
}