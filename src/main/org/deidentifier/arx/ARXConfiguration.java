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

package org.deidentifier.arx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.MatrixBasedCriterion;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.ProfitabilityJournalist;
import org.deidentifier.arx.criteria.ProfitabilityJournalistNoAttack;
import org.deidentifier.arx.criteria.ProfitabilityProsecutor;
import org.deidentifier.arx.criteria.ProfitabilityProsecutorNoAttack;
import org.deidentifier.arx.criteria.SampleBasedCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * A generic configuration for the ARX anonymizer.
 *
 * @author Fabian Prasser
 */
public class ARXConfiguration implements Serializable, Cloneable {
    
    // TODO: While in use, this configuration object should be locked, similar to, e.g., DataDefinition

    /**
     * Class for internal use that provides access to more parameters and functionality.
     */
    public static class ARXConfigurationInternal {
        
        /** The wrapped object. */
        private final ARXConfiguration config;
        
        /**
         * Creates a new instance.
         *
         * @param config
         */
        protected ARXConfigurationInternal(ARXConfiguration config){
            this.config = config;
        }

        /**
         * Returns the maximum number of allowed outliers.
         *
         * @return
         */
        public final int getAbsoluteSuppressionLimit() {
            return config.getAbsoluteSuppressionLimit();
        }

        /**
         * Returns all class-based criteria (except k-anonymity) as an array. 
         * Only used internally. If k-anonymity is included the minimal
         * group size should be obtained and enforced 
         * @return
         */
        public PrivacyCriterion[] getClassBasedPrivacyModelsAsArray() {
            return config.getPrivacyModelsAsArray();
        }

        /**
         * Returns the max relative number of outliers.
         *
         * @return
         */
        public double getMaxOutliers() {
            return config.getMaxOutliers();
        }

        /**
         * Returns the minimal size of an equivalence class induced by the contained criteria.
         * @return If k-anonymity is contained, k is returned. If l-diversity is contained, l is returned.
         * If both are contained max(k,l) is returned. Otherwise, Integer.MAX_VALUE is returned.
         */
        public int getMinimalGroupSize() {
            return config.getMinimalGroupSize();
        }
        
        /**
         * Returns a monotonicity property
         * @return
         */
        public Monotonicity getMonotonicityOfPrivacy() {
            return config.getMonotonicityOfPrivacy();
        }

        /**
         * Returns a monotonicity property
         * @return
         */
        public Monotonicity getMonotonicityOfUtility() {
            return config.getMonotonicityOfUtility();
        }

        /**
         * 
         *
         * @param <T>
         * @param clazz
         * @return
         * @see org.deidentifier.arx.ARXConfiguration#getPrivacyModel(java.lang.Class)
         */
        public <T extends PrivacyCriterion> T getPrivacyModel(Class<T> clazz) {
            return config.getPrivacyModel(clazz);
        }

        /**
         * Returns all criteria.
         * @return
         */
        public Set<PrivacyCriterion> getPrivacyModels() {
            return config.getPrivacyModels();
        }

        /**
         * Returns the quality model to be used for optimizing output data.
         *
         * @return
         */
        public Metric<?> getQualityModel() {
            return config.getQualityModel();
        }
        
        /**
         * Returns the criteria's requirements.
         *
         * @return
         */
        public int getRequirements() {
            return config.getRequirements();
        }

        /**
         * Returns all sample-based criteria as an array.
         * @return
         */
        public SampleBasedCriterion[] getSampleBasedPrivacyModelsAsArray() {
            return config.getSampleBasedPrivacyModelsAsArray();
        }

        /**
         * Returns the specific length of each entry in a snapshot.
         *
         * @return
         */
        public int getSnapshotLength() {
            return config.getSnapshotLength();
        }

        /**
         * Returns the data subset, if any
         * @return
         */
        public DataSubset getSubset() {
            return config.getSubset();
        }

        /**
         * Returns an integer representing all attribute types that must be suppressed.
         *
         * @return
         */
        public int getSuppressedAttributeTypes() {
            return config.getSuppressedAttributeTypes();
        }

        /**
         * Is practical monotonicity assumed.
         *
         * @return
         */
        public boolean isPracticalMonotonicity() {
            return config.isPracticalMonotonicity();
        }

        /**
         * @param clazz
         * @return
         * @see org.deidentifier.arx.ARXConfiguration#isPrivacyModelSpecified(java.lang.Class)
         */
        public boolean isPrivacyModelSpecified(Class<? extends PrivacyCriterion> clazz) {
            return config.isPrivacyModelSpecified(clazz);
        }

        /**
         * Returns whether suppression is applied to the output of anonymous as 
         * well as non-anonymous transformations. If this flag is set to true, 
         * suppression will be applied to the output of non-anonymous transformations 
         * to make them anonymous (if possible). Default is true.
         * @return
         */
        public boolean isSuppressionAlwaysEnabled() {
            return config.isSuppressionAlwaysEnabled();
        }

        /**
         * Do we guarantee optimality for sample-based criteria?
         */
        public boolean isUseHeuristicForSampleBasedCriteria() {
            return config.isUseHeuristicSearchForSampleBasedCriteria();
        }

        /**
         * Convenience method for checking the requirements.
         *
         * @param requirement
         * @return
         */
        public boolean requires(int requirement) {
            return config.requires(requirement);
        }

        /**
         * Returns matrix-based models
         * @return
         */
        public MatrixBasedCriterion[] getMatrixBasedPrivacyModelsAsArray() {
            return config.getMatrixBasedPrivacyModelsAsArray();
        }
    }

    /**
     * Monotonicity.
     */
    public static enum Monotonicity {
        
        /**  Fully monotonic */
        FULL,
        
        /**  Partially monotonic */
        PARTIAL,
        
        /**  Non-monotonic */
        NONE
    }

    /** Do the criteria require a counter per equivalence class. */
    public static final int       REQUIREMENT_COUNTER           = 0x1;

    /** Do the criteria require distributions of sensitive values in the equivalence classes. */
    public static final int       REQUIREMENT_DISTRIBUTION      = 0x4;

    /** Do the criteria require a second counter. */
    public static final int       REQUIREMENT_SECONDARY_COUNTER = 0x2;
    
    /** For serialization. */
    private static final long     serialVersionUID              = -6713510386735241964L;

    /**
     * Creates a new configuration without tuple suppression.
     *
     * @return
     */
    public static ARXConfiguration create() {
        return new ARXConfiguration();
    }


    /**
     * Creates a new configuration that allows the given percentage of outliers and
     * thus implements tuple suppression.
     *
     * @param suppressionLimit
     * @return
     */
    public static ARXConfiguration create(double suppressionLimit) {
        return new ARXConfiguration(suppressionLimit);
    }

    /**
     * Creates a new configuration that allows the given percentage of outliers and
     * thus implements tuple suppression. Defines the metric for measuring information loss.
     *
     * @param suppressionLimit
     * @param metric
     * @return
     */
    public static ARXConfiguration create(double suppressionLimit, Metric<?> metric) {
        return new ARXConfiguration(suppressionLimit, metric);
    }

    /**
     * Creates a new configuration that allows to define the metric for measuring information loss.
     *
     * @param metric
     * @return
     */
    public static ARXConfiguration create(Metric<?> metric) {
        return new ARXConfiguration(metric);
    }
    
    /**
     * The semantics of heuristic search steps.
     */
    public static enum SearchStepSemantics {
        
        /** Steps correspond to checks */
        CHECKS,
        
        /** Steps correspond to expansions */
        EXPANSIONS
    }

    /** Absolute suppression limit. */
    private int                                absMaxOutliers                        = 0;

    /** Criteria. */
    private PrivacyCriterion[]                 aCriteria                             = new PrivacyCriterion[0];

    /** Criteria. */
    private SampleBasedCriterion[]             bCriteria                             = new SampleBasedCriterion[0];

    /** Criteria. */
    private MatrixBasedCriterion[]             mCriteria                             = new MatrixBasedCriterion[0];

    /** A map of weights per attribute. */
    private Map<String, Double>                attributeWeights                      = null;

    /** The criteria. */
    private Set<PrivacyCriterion>              criteria                              = new HashSet<PrivacyCriterion>();

    /** The metric. */
    private Metric<?>                          metric                                = Metric.createLossMetric();

    /** Do we assume practical monotonicity. */
    private boolean                            practicalMonotonicity                 = false;

    /** Relative tuple outliers. */
    private double                             relMaxOutliers                        = -1;

    /** The requirements per equivalence class. */
    private int                                requirements                          = 0x0;

    /** The snapshot length. */
    private int                                snapshotLength;

    /** Defines values of which attribute type are to be replaced by the suppression string in suppressed tuples. */
    private Integer                            suppressedAttributeTypes              = 1 << AttributeType.ATTR_TYPE_QI;

    /** Determines whether suppression is applied to the output of anonymous as well as non-anonymous transformations. */
    private Boolean                            suppressionAlwaysEnabled              = true;

    /** Internal variant of the class providing a broader interface. */
    private transient ARXConfigurationInternal accessibleInstance                    = null;

    /** Are we performing optimal anonymization for sample-based criteria? */
    private boolean                            heuristicSearchForSampleBasedCriteria = false;

    /** Should we use the heuristic search algorithm? */
    private boolean                            heuristicSearchEnabled                = false;

    /** We will use the heuristic algorithm, if the size of the search space exceeds this threshold */
    private Integer                            heuristicSearchThreshold              = 100000;

    /** The heuristic algorithm will terminate after the given time limit */
    private Integer                            heuristicSearchTimeLimit              = 30000;

    /** The heuristic algorithm will terminate after the given number of search steps */
    private Integer                            heuristicSearchStepLimit              = Integer.MAX_VALUE;

    /** Cost/benefit configuration */
    private ARXCostBenefitConfiguration        costBenefitConfiguration              = ARXCostBenefitConfiguration.create();

    /** The privacy budget to use for the data-dependent differential privacy search algorithm */
    private Double                             dpSearchBudget                        = 0.1d;

    /** Number of output records */
    private int                                numOutputRecords                      = 0;
    
    /** Semantics of heuristic search steps */
    private SearchStepSemantics                searchStepSemantics                   = SearchStepSemantics.CHECKS;

    /**
     * Creates a new configuration without tuple suppression.
     */
    private ARXConfiguration() {
        this.relMaxOutliers = 0d;
    }
    
    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression.
     *
     * @param supp
     */
    private ARXConfiguration(double supp) {
        if (supp < 0d || supp >= 1d) { throw new NullPointerException("Suppression must be >=0 and <1"); }
        this.relMaxOutliers = supp;
    }

    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression. Defines the metric for measuring information loss.
     * @param supp
     * @param metric
     */
    private ARXConfiguration(double supp, Metric<?> metric) {
        if (supp < 0d || supp > 1d) { throw new NullPointerException("Suppression must be >=0 and <=1"); }
        this.relMaxOutliers = supp;
        if (metric == null) { throw new NullPointerException("Metric must not be null"); }
        this.metric = metric;
    }

    /**
     * Creates a new config that allows to define the metric for measuring information loss.
     * @param metric
     */
    private ARXConfiguration(Metric<?> metric) {
        if (metric == null) { throw new NullPointerException("Metric must not be null"); }
        this.metric = metric;
    }

    /**
     * Adds a privacy model to the configuration.
     *
     * @param c
     * @return
     */
    public ARXConfiguration addPrivacyModel(PrivacyCriterion c) {
        
        // Check
        checkArgument(c);
                
        // Check models for which only one instance is supported
        if ((c instanceof DPresence) && this.isPrivacyModelSpecified(DPresence.class)) {
            throw new IllegalArgumentException("You must not add more than one instance of the d-presence model");
        }
        if ((c instanceof KMap) && this.isPrivacyModelSpecified(KMap.class)) { 
            throw new IllegalArgumentException("You must not add more than one instance of the k-map model"); 
        } 
        if ((c instanceof KAnonymity) && this.isPrivacyModelSpecified(KAnonymity.class)) { 
               throw new IllegalArgumentException("You must not add more than one instance of the k-anonymity model"); 
        }
        if ((c instanceof EDDifferentialPrivacy) && this.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) { 
            throw new IllegalArgumentException("You must not add more than one instance of the differential privacy model"); 
        }
        
        // Check whether different subsets have been defined
        if (c.isSubsetAvailable()) {

            // Collect all subsets
            List<int[]> subsets = new ArrayList<int[]>();
            subsets.add(c.getDataSubset().getArray());
            for (PrivacyCriterion other : this.getPrivacyModels()) {
                if (other.isSubsetAvailable()) {
                    subsets.add(other.getDataSubset().getArray());
                }
            }

            // Compare
            for (int i = 0; i < subsets.size() - 1; i++) {
                if (!Arrays.equals(subsets.get(i), subsets.get(i + 1))) {
                    throw new IllegalArgumentException("Using different research subsets is not supported");
                }
            }
        }
        
        // Add
        criteria.add(c);
        
        // Check DP has been combined with a subset
        if (this.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            for (PrivacyCriterion other : this.getPrivacyModels()) {
                if (!(other instanceof EDDifferentialPrivacy) && other.isSubsetAvailable()) {
                    
                    // Remove and complain
                    criteria.remove(c);
                    throw new RuntimeException("Combining differential privacy with a research subset is not supported");        
                }
            }
        }
        
        // Everything is fine
        return this;
    }
    
    /**
     * Clones this config.
     *
     * @return
     */
    public ARXConfiguration clone() {
        ARXConfiguration result = new ARXConfiguration();
        result.practicalMonotonicity = this.practicalMonotonicity;
        result.relMaxOutliers = this.relMaxOutliers;
        result.absMaxOutliers = this.absMaxOutliers;
        result.aCriteria = this.aCriteria.clone();
        result.bCriteria = this.bCriteria.clone();
        result.mCriteria = this.mCriteria.clone();
        result.criteria = new HashSet<PrivacyCriterion>(this.criteria);
        result.requirements = this.requirements;
        result.metric = this.metric;
        result.snapshotLength = this.snapshotLength;
        result.suppressionAlwaysEnabled = this.suppressionAlwaysEnabled;
        result.suppressedAttributeTypes = this.suppressedAttributeTypes;
        result.heuristicSearchForSampleBasedCriteria = this.heuristicSearchForSampleBasedCriteria;
        result.heuristicSearchEnabled = this.heuristicSearchEnabled;
        result.heuristicSearchThreshold = this.heuristicSearchThreshold;
        result.heuristicSearchTimeLimit = this.heuristicSearchTimeLimit;
        result.costBenefitConfiguration = this.getCostBenefitConfiguration().clone();
        result.dpSearchBudget = this.dpSearchBudget;
        result.searchStepSemantics = this.searchStepSemantics;
        if (this.attributeWeights != null) {
            result.attributeWeights = new HashMap<String, Double>(this.attributeWeights);
        } else {
            result.attributeWeights = null;
        }
        return result;
    }

    /**
     * Returns the weight for the given attribute.
     *
     * @param attribute
     * @return
     */
    public double getAttributeWeight(String attribute) {
        
        // For backwards compatibility
        if (this.attributeWeights==null) {
            this.attributeWeights = new HashMap<String, Double>();
        }
        Double value = this.attributeWeights.get(attribute);
        if (value == null) return 0.5d;
        else return value;
    }
    
    /**
     * Returns all configured attribute weights. For attributes which are not a key in this
     * set the default attribute weight will be assumed by ARX. This default value is 
     * currently set to 0.5.
     * 
     * @return
     */
    public Map<String, Double> getAttributeWeights() {
        // For backwards compatibility
        if (this.attributeWeights==null) {
            this.attributeWeights = new HashMap<String, Double>();
        }
        return new HashMap<String, Double>(this.attributeWeights);
    }
    
    /**
     * Returns the cost/benefit configuration
     */
    public ARXCostBenefitConfiguration getCostBenefitConfiguration() {
        if (this.costBenefitConfiguration == null) {
            this.costBenefitConfiguration = ARXCostBenefitConfiguration.create();
        }
        return this.costBenefitConfiguration;
    }
    
    /**
     * Returns the privacy budget to use for the data-dependent
     * differential privacy search algorithm. The default is 0.1.
     * @return
     */
    public double getDPSearchBudget() {
        if (this.dpSearchBudget == null) {
            this.dpSearchBudget = 0.1d;
        }
        return this.dpSearchBudget;
    }
    
    /**
     * The heuristic search algorithm will terminate after the returned number of steps.
     * The default is <code>Integer.MAX_VALUE</code>, i.e. no limit.
     * @param requestedSemantics the semantics of the number of search steps to be returned
     * @param numQIs the number of QIs
     * @return
     */
    public int getHeuristicSearchStepLimit(SearchStepSemantics requestedSemantics, int numQIs) {
        
        if (this.heuristicSearchStepLimit == null) {
            this.heuristicSearchStepLimit = Integer.MAX_VALUE;
        }
        
        if (this.heuristicSearchStepLimit == Integer.MAX_VALUE || requestedSemantics == this.searchStepSemantics) {
            return this.heuristicSearchStepLimit;
        }
        
        switch (requestedSemantics) {
        case CHECKS:
            // Convert the limit of expansions which has been set to the requested limit of checks
            return this.heuristicSearchStepLimit * numQIs;
        case EXPANSIONS:
            // Convert the limit of checks which has been set to the requested limit of expansions
            return this.heuristicSearchStepLimit / numQIs;
        default:
            throw new RuntimeException("The search step semantic " + requestedSemantics + " is not supported");
        }
     }
    
    /**
     * The semantics of heuristic search steps.
     * The default is <code>SearchStepSemantics.CHECKS</code>.
     * @return
     */
    public SearchStepSemantics getHeuristicSearchStepSemantics() {
        if (this.searchStepSemantics == null) {
            this.searchStepSemantics = SearchStepSemantics.CHECKS;
        }
        return this.searchStepSemantics;
    }
    
    /**
     * When the size of the solution space exceeds the returned number of transformations,
     * ARX will use a heuristic search strategy. The default is 100.000.
     * @return
     */
    public int getHeuristicSearchThreshold() {
        if (this.heuristicSearchThreshold == null) {
            this.heuristicSearchThreshold = 100000;
        }
        return this.heuristicSearchThreshold;
    }
  
    /**
     * The heuristic search algorithm will terminate after the returned number of milliseconds.
     * The default is 30 seconds.
     * @return
     */
    public int getHeuristicSearchTimeLimit() {
        if (this.heuristicSearchTimeLimit == null) {
            this.heuristicSearchTimeLimit = 30000;
        }
        return this.heuristicSearchTimeLimit;
    }

    /**
     * Returns the maximum number of allowed outliers.
     * Deprecated. Use <code>getSuppressionLimit()</code> instead.
     *
     * @return
     */
    @Deprecated
    public double getMaxOutliers() {
        return relMaxOutliers;
    }
    
    /**
     * Returns whether the privacy model is monotonic
     * @return
     */
    public Monotonicity getMonotonicityOfPrivacy() {
        
        // Practical monotonicity
        if (this.isPracticalMonotonicity()) {
            return Monotonicity.FULL;
        }
        
        // Without suppression
        if (this.getMaxOutliers() == 0d) {
            for (PrivacyCriterion criterion : this.getPrivacyModels()) {
                if (!criterion.isMonotonicWithGeneralization()) {
                    if (this.getMinimalGroupSize() != Integer.MAX_VALUE) {
                        return Monotonicity.PARTIAL;
                    } else {
                        return Monotonicity.NONE;
                    }
                }
            }
        // With suppression
        } else {
            for (PrivacyCriterion criterion : this.getPrivacyModels()) {
                if (!criterion.isMonotonicWithSuppression() || 
                    !criterion.isMonotonicWithGeneralization()) {
                    if (this.getMinimalGroupSize() != Integer.MAX_VALUE) {
                        return Monotonicity.PARTIAL;
                    } else {
                        return Monotonicity.NONE;
                    }
                }
            }
        }
        
        // Full
        return Monotonicity.FULL;
    }
    
    /**
     * Returns whether the utility measure is monotonic
     * @return
     */
    public Monotonicity getMonotonicityOfUtility() {
        if (metric.isMonotonic(this.getMaxOutliers()) || 
            this.isPracticalMonotonicity()) {
            return Monotonicity.FULL;
        }  else {
            return Monotonicity.NONE;
        }
    }

    /**
     * Returns an instance of the class, if any. Throws an exception if more than one such model exists.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> T getPrivacyModel(Class<T> clazz) {
        checkArgument(clazz);
        Set<T> result = new HashSet<T>();
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) {
                result.add((T) c);
            }
        }
        if (result.size() > 1) {
            throw new RuntimeException("More than one matches the query!");
        } else if (result.size() == 1) {
            return result.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Returns all privacy models.
     * @return
     */
    public Set<PrivacyCriterion> getPrivacyModels() {
        return this.criteria;
    }

    /**
     * Returns all privacy models which are instances of the given class.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> Set<T> getPrivacyModels(Class<T> clazz) {
        checkArgument(clazz);
        Set<T> result = new HashSet<T>();
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) {
                result.add((T) c);
            }
        }
        return result;
    }
    
    /**
     * Returns the quality model to be used for optimizing output data.
     *
     * @return
     */
    public Metric<?> getQualityModel() {
        return this.metric;
    }
    
    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdJournalist() {
        double risk = 1d;
        for (PrivacyCriterion criterion : this.criteria) {
            risk = Math.min(risk, criterion.getRiskThresholdJournalist());
        }
        return risk;
    }

    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdMarketer() {
        double risk = 1d;
        for (PrivacyCriterion criterion : this.criteria) {
            risk = Math.min(risk, criterion.getRiskThresholdMarketer());
        }
        return risk;
    }
    
    /**
     * Return prosecutor risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdProsecutor() {
        double risk = 1d;
        for (PrivacyCriterion criterion : this.criteria) {
            risk = Math.min(risk, criterion.getRiskThresholdProsecutor());
        }
        return risk;
    }
    
    /**
     * Returns the suppression limit
     * @return
     */
    public double getSuppressionLimit() {
        return this.getMaxOutliers();
    }
    
    /**
     * Returns whether values of the given attribute type will be replaced by the suppression 
     * string in suppressed tuples.
     * @param type
     * @return
     */
    public boolean isAttributeTypeSuppressed(final AttributeType type){
        checkArgument(type);
        // Ensure backwards compatibility
        if (suppressedAttributeTypes == null) {
            suppressedAttributeTypes = 1 << AttributeType.ATTR_TYPE_QI;
        }
        return (suppressedAttributeTypes & (1 << type.getType())) != 0;
    }
    
    /**
     * Returns whether ARX will use a heuristic search strategy. The default is false.
     * @return
     */
    public boolean isHeuristicSearchEnabled() {
        return this.heuristicSearchEnabled;
    }
    
    /**
     * Is practical monotonicity assumed.
     *
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }
    
    /**
     * Returns whether the configuration contains a privacy model which is an instance of the given class.
     *
     * @param clazz
     * @return
     */
    public boolean isPrivacyModelSpecified(Class<? extends PrivacyCriterion> clazz) {
        checkArgument(clazz);
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) { return true; }
        }
        return false;
    }

    /**
     * Returns whether suppression is applied to the output of anonymous as well as non-anonymous transformations. If
     * this flag is set to <code>true</code>, suppression will be applied to the output of non-anonymous 
     * transformations to make them anonymous (if possible). Default is <code>true</code>.
     * @return
     */
    public boolean isSuppressionAlwaysEnabled(){
        // Ensure backwards compatibility
        if (this.suppressionAlwaysEnabled == null) {
            this.suppressionAlwaysEnabled = true;
        }
        return this.suppressionAlwaysEnabled;
    }

    /**
     * Is optimality guaranteed for sample-based criteria?
     */
    public boolean isUseHeuristicSearchForSampleBasedCriteria() {
        return heuristicSearchForSampleBasedCriteria;
    }

    /**
     * Removes the given criterion.
     *
     * @param <T>
     * @param arg
     * @return
     */
    public <T extends PrivacyCriterion> boolean removeCriterion(PrivacyCriterion arg) {
        checkArgument(arg);
        return criteria.remove(arg);
    }
    
    /**
     * Renders this object 
     * @return
     */
    public List<ElementData> render() {

        // Render attribute types
        List<ElementData> result = new ArrayList<>();
        result.add(renderWeights());
        result.add(renderSettings());
        result.add(renderReidentificationThresholds());
        return result;
    }

    /**
     * Defines values of which attribute type are to be replaced by the suppression string in suppressed tuples.
     * With default settings, only quasi-identifiers will be suppressed.
     * 
     * @param type the attribute type
     * @param enabled whether suppression should be performed or not
     */
    public void setAttributeTypeSuppressed(final AttributeType type, boolean enabled) {
        checkArgument(type);
        // Ensure backwards compatibility
        if (suppressedAttributeTypes == null) {
            suppressedAttributeTypes = 1 << AttributeType.ATTR_TYPE_QI;
        }
        if (enabled) {
            suppressedAttributeTypes |= 1 << type.getType();
        } else {
            suppressedAttributeTypes &= ~(1 << type.getType());
        }
    }

    /**
     * Sets the weight for the given attribute.
     *
     * @param attribute
     * @param weight
     */
    public void setAttributeWeight(String attribute, double weight){
        checkArgument(attribute);
        setAttributeWeight(attribute, Double.valueOf(weight));
    }

    /**
     * Sets the weight for the given attribute.
     *
     * @param attribute
     * @param weight
     */
    public void setAttributeWeight(String attribute, Double weight){
        checkArgument(attribute);
        // For backwards compatibility
        if (this.attributeWeights==null) {
            this.attributeWeights = new HashMap<String, Double>();
        }
        this.attributeWeights.put(attribute, weight);
    }

    /**
     * Sets the cost/benefit configuration
     * @param config
     */
    public ARXConfiguration setCostBenefitConfiguration(ARXCostBenefitConfiguration config) {
        if (config == null) {
            throw new NullPointerException("Argument must not be null");
        }
        this.costBenefitConfiguration = config;
        return this;
    }
    
    /**
     * Sets the privacy budget to use for the data-dependent
     * differential privacy search algorithm. The default is 0.1.
     * @param budget
     */
    public void setDPSearchBudget(double budget) {
        if (budget <= 0d) { throw new IllegalArgumentException("Parameter must be > 0"); }
        this.dpSearchBudget = budget;
    }

    /**
     * Sets whether ARX will use a heuristic search strategy. The default is false.
     * @param heuristicSearchEnabled
     * @return
     */
    public void setHeuristicSearchEnabled(boolean heuristicSearchEnabled) {
        this.heuristicSearchEnabled = heuristicSearchEnabled;
    }

    /**
     * The heuristic search algorithm will terminate after the given number of transformations
     * have been checked. The default is <code>Integer.MAX_VALUE</code>, i.e. no limit.
     * @param numberOfTransformations
     */
    public void setHeuristicSearchStepLimit(int numberOfTransformations) {
        if (numberOfTransformations <= 0) { throw new IllegalArgumentException("Parameter must be > 0"); }
        this.heuristicSearchStepLimit = numberOfTransformations;
    }
    
    /**
     * Sets the semantics of heuristic search steps.
     * If the semantic <code>EXPANSIONS</code> is set, then the limit of the number of heuristic checks
     * will be calculated by multiplying the heuristic search step limit with the number of QIs.
     * @param searchStepSemantics
     */
    public void setHeuristicSearchStepSemantics(SearchStepSemantics searchStepSemantics) {
        this.searchStepSemantics = searchStepSemantics;
    }

    /**
     * When the size of the solution space exceeds the given number of transformations,
     * ARX will use a heuristic search strategy. The default is 100.000.
     * @param numberOfTransformations
     * @return
     */
    public void setHeuristicSearchThreshold(int numberOfTransformations) {
        if (numberOfTransformations <= 0) { throw new IllegalArgumentException("Parameter must be > 0"); }
        this.heuristicSearchThreshold = numberOfTransformations;
    }
    /**
     * The heuristic search algorithm will terminate after the given number of milliseconds.
     * The default is 30 seconds.
     * @param timeInMillis
     */
    public void setHeuristicSearchTimeLimit(int timeInMillis) {
        if (timeInMillis <= 0) { throw new IllegalArgumentException("Parameter must be > 0"); }
        this.heuristicSearchTimeLimit = timeInMillis;
    }
    
    /**
     * Allows for a certain percentage of outliers and thus
     * triggers tuple suppression.
     * Deprecated. Use <code>setSuppressionLimit()</code> instead.
     *
     * @param max
     */
    @Deprecated
    public void setMaxOutliers(double max) {
        this.relMaxOutliers = max;
    }

    /**
     * Set, if practical monotonicity assumed.
     *
     * @param assumeMonotonicity
     */
    public void setPracticalMonotonicity(final boolean assumeMonotonicity) {
        this.practicalMonotonicity = assumeMonotonicity;
    }
    
    /**
     * Sets the quality model to be used for optimizing output data.
     *
     * @param model
     */
    public void setQualityModel(Metric<?> model) {
        if (model == null) { throw new NullPointerException("Quality model must not be null"); }
        this.metric = model;
    }

    /**
     * Sets whether suppression is applied to the output of anonymous as well as non-anonymous transformations. If
     * this flag is set to <code>true</code>, suppression will be applied to the output of non-anonymous 
     * transformations to make them anonymous (if possible). Default is <code>true</code>. 
     * @param enabled
     */
    public void setSuppressionAlwaysEnabled(boolean enabled){
    	this.suppressionAlwaysEnabled = enabled;
    }

    /**
     * Sets the suppression limit. This is an alias for setMaxOutliers().
     * @param limit
     */
    public void setSuppressionLimit(double limit) {
        this.relMaxOutliers = limit;
    }

    /**
     * Do we guarantee optimality for sample-based criteria?
     */
    public void setUseHeuristicSearchForSampleBasedCriteria(boolean value) {
        this.heuristicSearchForSampleBasedCriteria = value;
    }
    
    /**
     * Checks an argument.
     *
     * @param argument
     */
    private void checkArgument(Object argument){
        if (argument == null) { 
            throw new IllegalArgumentException("Argument must not be null"); 
        }
    }

    /**
     * Renders stuff
     * @return
     */
    private ElementData renderReidentificationThresholds() {
        ElementData result = new ElementData("Risk thresholds");
        result.addProperty("Prosecutor risk", this.getRiskThresholdProsecutor());
        result.addProperty("Journalist risk", this.getRiskThresholdJournalist());
        result.addProperty("Marketer risk", this.getRiskThresholdMarketer());
        return result;
    }
    
    /**
     * Renders the weights
     * @return
     */
    private ElementData renderSettings() {
        ElementData result = new ElementData("Settings");
        result.addProperty("Assume monotonicity", this.practicalMonotonicity);
        result.addProperty("Suppression limit", this.relMaxOutliers);
        return result;
    }
    
    /**
     * Renders the weights
     * @return
     */
    private ElementData renderWeights() {
        ElementData result = new ElementData("Weights");
        if (attributeWeights.isEmpty()) {
            result.addItem("None specified");
        } else {
            for (Entry<String, Double> entry : attributeWeights.entrySet()) {
                result.addProperty(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Returns the absolute record suppression limit
     *
     * @return
     */
    protected int getAbsoluteSuppressionLimit() {
        return this.absMaxOutliers;
    }

    /**
     * Returns the number of output records that will be produced,
     * zero if this information is not available
     * @return
     */
    protected int getNumOutputRecords() {
        return this.numOutputRecords;
    }
    
    /**
     * Clones this config and projects everything onto the given subset.<br>
     * - All privacy models will be cloned<br>
     * - Subsets in d-presence will be projected accordingly<br>
     * - Utility measures will be cloned<br>
     * - Replaces estimated k-map with according k-anonymity<br>
     * @param rowset
     * @param gsFactor May be NaN if it should be ignored 
     *
     * @return
     */
    protected ARXConfiguration getInstanceForLocalRecoding(RowSet rowset, double gsFactor) {

        // Check, if we can do this
        for (PrivacyCriterion criterion : this.getPrivacyModels()) {
            if (!criterion.isLocalRecodingSupported()) {
                throw new IllegalStateException("Local recoding not supported.");
            }
        }
        
        // Prepare a subset
        DataSubset subset = this.getSubset();
        if (subset != null) {
            subset = subset.getSubsetInstance(rowset);
        }
        
        // Clone all criteria
        HashSet<PrivacyCriterion> criteria = new HashSet<PrivacyCriterion>();
        for (PrivacyCriterion criterion : this.getPrivacyModels()) {
            
            // Clone and store
            PrivacyCriterion clone = criterion.clone(subset);
            
            // We need to make sure that we don't add multiple instances of k-anonymity
            // because k-map can be converted into this model
            if (clone instanceof KAnonymity) {
                Iterator<PrivacyCriterion> iter = criteria.iterator();
                while (iter.hasNext()) {
                    PrivacyCriterion other = iter.next();
                    if (other instanceof KAnonymity) {
                        if (((KAnonymity)other).getK() <= ((KAnonymity)clone).getK()) {
                            iter.remove();
                        } else {
                            clone = null;
                        }
                    }
                }
                if (clone != null) {
                    criteria.add(clone);
                }
            } else {
                criteria.add(clone);
            }
        }
        
        // Clone the config
        ARXConfiguration result = this.clone();
        result.aCriteria = null;
        result.criteria = criteria;
        MetricConfiguration utilityConfig = result.getQualityModel().getConfiguration();
        if (!Double.isNaN(gsFactor)) {
            utilityConfig.setGsFactor(gsFactor);
        }
        result.metric = result.getQualityModel().getDescription().createInstance(utilityConfig);
        
        // Return
        return result;
    }

    /**
     * Returns an internal variant of the class which provides a broader interface
     *
     * @return
     */
    protected ARXConfigurationInternal getInternalConfiguration(){
        if (this.accessibleInstance == null) {
            this.accessibleInstance = new ARXConfigurationInternal(this);
        }
        return this.accessibleInstance;
    }

    /**
     * Returns all matrix-based criteria as an array. Only used internally.
     * @return
     */
    protected MatrixBasedCriterion[] getMatrixBasedPrivacyModelsAsArray() {
        return this.mCriteria;
    }
    
    /**
     * Returns the minimal size of an equivalence class induced by the defined privacy models.
     * @return If k-anonymity is contained, k is returned. If l-diversity is contained, l is returned.
     * If both are contained max(k,l) is returned. Otherwise, Integer.MAX_VALUE is returned.
     */
    protected int getMinimalGroupSize() {

        // Init
        int result = -1;

        // For each
        for (PrivacyCriterion c : this.getPrivacyModels()) {
            if (c.isMinimalClassSizeAvailable()) {
                result = Math.max(result, c.getMinimalClassSize());
            }
        }

        // Check & return
        if (result == -1) return Integer.MAX_VALUE;
        else return result;
    }

    /**
     * Returns all criteria (except k-anonymity) as an array. Only used internally. If k-anonymity is included the minimal
     * group size should be obtained and enforced 
     * @return
     */
    protected PrivacyCriterion[] getPrivacyModelsAsArray() {
        return this.aCriteria;
    }
    
    /**
     * Returns the criteria's requirements.
     *
     * @return
     */
    protected int getRequirements() {
        return this.requirements;
    }
    
    /**
     * Returns all sample-based criteria as an array. Only used internally.
     * @return
     */
    protected SampleBasedCriterion[] getSampleBasedPrivacyModelsAsArray() {
        return this.bCriteria;
    }

    /**
     * Returns the specific length of each entry in a snapshot.
     *
     * @return
     */
    protected int getSnapshotLength() {
        return this.snapshotLength;
    }
    
    /**
     * Returns the data subset, if any subset is defined.
     * You may only call this, after the configuration has be initialized.
     * @return
     */
    protected DataSubset getSubset() {
        for (PrivacyCriterion c : this.criteria) {
            if (c.isSubsetAvailable()) {
                DataSubset subset = c.getDataSubset();
                if (subset != null) {
                    return subset;    
                }
            }
        }
        return null;
    }

    /**
     * Returns an integer representing all attribute types that must be suppressed.
     *
     * @return
     */
    protected int getSuppressedAttributeTypes() {
        // Ensure backwards compatibility
        if (suppressedAttributeTypes == null) { return 1 << AttributeType.ATTR_TYPE_QI; }
        return this.suppressedAttributeTypes;
    }

    /**
     * Initializes the configuration.
     *
     * @param manager
     */
    protected void initialize(DataManager manager) {

        // Check
        if (criteria.isEmpty()) { 
            throw new RuntimeException("At least one privacy model must be specified!"); 
        }

        // Compute requirements
        this.requirements = 0x0;
        for (PrivacyCriterion c : criteria) {
            this.requirements |= c.getRequirements();
        }
        
        // Requirements for microaggregation
        if (manager.getDataAnalyzed().getArray() != null && !manager.getDataAnalyzed().isEmpty()) {
            this.requirements |= ARXConfiguration.REQUIREMENT_DISTRIBUTION;
        }

        // Initialize
        for (PrivacyCriterion c : criteria) {
            // Differential Privacy has already been initialized by the DataManager
            if (!(c instanceof EDDifferentialPrivacy)) {
                c.initialize(manager, this);
            }
        }
        
        // Calculate number of records in output data
        if (this.getSubset() != null) {
            numOutputRecords = getSubset().getArray().length;
        } else {
            numOutputRecords = manager.getDataGeneralized().getDataLength();
        }

        // Compute absolute suppression limit
        if (this.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            absMaxOutliers = (int) numOutputRecords;
        } else {
            absMaxOutliers = (int) Math.floor(this.relMaxOutliers * (double) numOutputRecords);
        }

        // Compute optimized array with privacy models,
        // in ascending order of computational complexity to evaluate
        List<PrivacyCriterion> list = new ArrayList<PrivacyCriterion>();
        if (this.isPrivacyModelSpecified(DPresence.class)) {
            list.add(this.getPrivacyModel(DPresence.class));
        }
        if (this.isPrivacyModelSpecified(KMap.class)) {
            list.add(this.getPrivacyModel(KMap.class));
        }
        if (this.isPrivacyModelSpecified(DDisclosurePrivacy.class)) {
            list.addAll(this.getPrivacyModels(DDisclosurePrivacy.class));
        }
        if (this.isPrivacyModelSpecified(BasicBLikeness.class)) {
            list.addAll(this.getPrivacyModels(BasicBLikeness.class));
        }
        if (this.isPrivacyModelSpecified(EnhancedBLikeness.class)) {
            list.addAll(this.getPrivacyModels(EnhancedBLikeness.class));
        }
        if (this.isPrivacyModelSpecified(LDiversity.class)) {
            list.addAll(this.getPrivacyModels(LDiversity.class));
        }
        if (this.isPrivacyModelSpecified(TCloseness.class)) {
            list.addAll(this.getPrivacyModels(TCloseness.class));
        }
        if (this.isPrivacyModelSpecified(ProfitabilityProsecutor.class)) {
            list.addAll(this.getPrivacyModels(ProfitabilityProsecutor.class));
        }
        if (this.isPrivacyModelSpecified(ProfitabilityProsecutorNoAttack.class)) {
            list.addAll(this.getPrivacyModels(ProfitabilityProsecutorNoAttack.class));
        }
        if (this.isPrivacyModelSpecified(ProfitabilityJournalist.class)) {
            list.addAll(this.getPrivacyModels(ProfitabilityJournalist.class));
        }
        if (this.isPrivacyModelSpecified(ProfitabilityJournalistNoAttack.class)) {
            list.addAll(this.getPrivacyModels(ProfitabilityJournalistNoAttack.class));
        }
        this.aCriteria = list.toArray(new PrivacyCriterion[0]);
        
        // Compute array of sample-based criteria
        this.bCriteria = new SampleBasedCriterion[0];
        if (this.isPrivacyModelSpecified(SampleBasedCriterion.class)) {
            this.bCriteria = this.getPrivacyModels(SampleBasedCriterion.class).toArray(new SampleBasedCriterion[0]);
        }

        // Compute array of matrix-based criteria
        this.mCriteria = new MatrixBasedCriterion[0];
        if (this.isPrivacyModelSpecified(MatrixBasedCriterion.class)) {
            this.mCriteria = this.getPrivacyModels(MatrixBasedCriterion.class).toArray(new MatrixBasedCriterion[0]);
        }
        
        // Compute snapshot length
        this.snapshotLength = 2;
        if (this.requires(REQUIREMENT_DISTRIBUTION)) {
            this.snapshotLength += 2 * manager.getAggregationInformation().getHotThreshold();
        }
        if (this.requires(REQUIREMENT_SECONDARY_COUNTER)) {
            this.snapshotLength += 1;
        }
    }

    /**
     * Convenience method for checking the requirements.
     *
     * @param requirement
     * @return
     */
    protected boolean requires(int requirement) {
        return (this.requirements & requirement) != 0;
    }
}
