/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.Set;

import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.FinancialJournalistNoAttackPrivacy;
import org.deidentifier.arx.criteria.FinancialJournalistPrivacy;
import org.deidentifier.arx.criteria.FinancialProsecutorNoAttackPrivacy;
import org.deidentifier.arx.criteria.FinancialProsecutorPrivacy;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
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
         * @param clazz
         * @return
         * @see org.deidentifier.arx.ARXConfiguration#containsCriterion(java.lang.Class)
         */
        public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
            return config.containsCriterion(clazz);
        }

        /**
         * Returns the maximum number of allowed outliers.
         *
         * @return
         */
        public final int getAbsoluteMaxOutliers() {
            return config.getAbsoluteMaxOutliers();
        }

        /**
         * Returns all class-based criteria (except k-anonymity) as an array. 
         * Only used internally. If k-anonymity is included the minimal
         * group size should be obtained and enforced 
         * @return
         */
        public PrivacyCriterion[] getClassBasedCriteriaAsArray() {
            return config.getCriteriaAsArray();
        }

        /**
         * Returns all criteria.
         * @return
         */
        public Set<PrivacyCriterion> getCriteria() {
            return config.getCriteria();
        }
        
        /**
         * 
         *
         * @param <T>
         * @param clazz
         * @return
         * @see org.deidentifier.arx.ARXConfiguration#getCriterion(java.lang.Class)
         */
        public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
            return config.getCriterion(clazz);
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
         * Returns the metric used for measuring information loss.
         *
         * @return
         */
        public Metric<?> getMetric() {
            return config.getMetric();
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
        public SampleBasedCriterion[] getSampleBasedCriteriaAsArray() {
            return config.getSampleBasedCriteriaAsArray();
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

    /** Absolute tuple outliers. */
    private int                                absMaxOutliers                        = 0;

    /** Criteria. */
    private PrivacyCriterion[]                 aCriteria                             = new PrivacyCriterion[0];

    /** Criteria. */
    private SampleBasedCriterion[]             bCriteria                             = new SampleBasedCriterion[0];

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

    /**
     * Defines values of which attribute type are to be replaced by the
     * suppression string in suppressed tuples.
     */
    private Integer                            suppressedAttributeTypes              = 1 << AttributeType.ATTR_TYPE_QI;

    /**
     * Determines whether suppression is applied to the output of anonymous as
     * well as non-anonymous transformations.
     */
    private Boolean                            suppressionAlwaysEnabled              = true;

    /** Should microaggregation be based on data utility measurements */
    private boolean                            utilityBasedMicroaggregation          = false;

    /** Internal variant of the class providing a broader interface. */
    private transient ARXConfigurationInternal accessibleInstance                    = null;

    /** Are we performing optimal anonymization for sample-based criteria? */
    private boolean                            heuristicSearchForSampleBasedCriteria = false;

    /** Should we use the heuristic search algorithm? */
    private boolean                            heuristicSearchEnabled                = false;

    /**
     * We will use the heuristic algorithm, if the size of the search space
     * exceeds this threshold
     */
    private Integer                            heuristicSearchThreshold              = 100000;

    /** The heuristic algorithm will terminate after the given time limit */
    private Integer                            heuristicSearchTimeLimit              = 30000;

    /** Financial configuration*/
    private ARXFinancialConfiguration          financialConfiguration                = ARXFinancialConfiguration.create();

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
     * Adds a criterion to the configuration.
     *
     * @param c
     * @return
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        
        // Check
        checkArgument(c);
                
        // Check models for which only one instance is supported
        if ((c instanceof DPresence) && this.containsCriterion(DPresence.class)) {
            throw new RuntimeException("You must not add more than one d-presence criterion");
        }
        if ((c instanceof KMap) && this.containsCriterion(KMap.class)) { 
            throw new RuntimeException("You must not add more than one k-map criterion"); 
        } 
        if ((c instanceof KAnonymity) && this.containsCriterion(KAnonymity.class)) { 
               throw new RuntimeException("You must not add more than one k-anonymity criterion"); 
        }
        if ((c instanceof EDDifferentialPrivacy) && this.containsCriterion(EDDifferentialPrivacy.class)) { 
            throw new RuntimeException("You must not add more than one differential privacy criterion"); 
        }
        
        // Check whether different subsets have been defined
        if (c.isSubsetAvailable()) {

            // Collect all subsets
            List<int[]> subsets = new ArrayList<int[]>();
            subsets.add(c.getDataSubset().getArray());
            for (PrivacyCriterion other : this.getCriteria()) {
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
        if (this.containsCriterion(EDDifferentialPrivacy.class)) {
            for (PrivacyCriterion other : this.getCriteria()) {
                if (other != c && other.isSubsetAvailable()) {
                    
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
        result.utilityBasedMicroaggregation = this.utilityBasedMicroaggregation;
        result.financialConfiguration = this.getFinancialConfiguration().clone();
        if (this.attributeWeights != null) {
            result.attributeWeights = new HashMap<String, Double>(this.attributeWeights);
        } else {
            result.attributeWeights = null;
        }
        return result;
    }
    
    /**
     * Returns whether the configuration contains a criterion of the given class.
     *
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        checkArgument(clazz);
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) { return true; }
        }
        return false;
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
     * Returns all criteria.
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return this.criteria;
    }
    
    /**
     * Returns all privacy criteria that are instances of the given class.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
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
     * Returns an instance of the class, if any. Throws an exception if more than one such criterion exists.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
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
     * Returns the financial configuration
     */
    public ARXFinancialConfiguration getFinancialConfiguration() {
        if (this.financialConfiguration == null) {
            this.financialConfiguration = ARXFinancialConfiguration.create();
        }
        return this.financialConfiguration;
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
     * @param timeInMillis
     */
    public int getHeuristicSearchTimeLimit() {
        if (this.heuristicSearchTimeLimit == null) {
            this.heuristicSearchTimeLimit = 30000;
        }
        return this.heuristicSearchTimeLimit;
    }

    /**
     * Returns the maximum number of allowed outliers.
     *
     * @return
     */
    public final double getMaxOutliers() {
        return relMaxOutliers;
    }

    /**
     * Returns the metric used for measuring information loss.
     *
     * @return
     */
    public Metric<?> getMetric() {
        return this.metric;
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
            for (PrivacyCriterion criterion : this.getCriteria()) {
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
            for (PrivacyCriterion criterion : this.getCriteria()) {
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
     * Returns whether microaggregation is based on utility measures
     * @return
     */
    public boolean isUtilityBasedMicroaggregation() {
        return this.utilityBasedMicroaggregation;
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
     * Sets the financial configuration
     * @param config
     */
    public ARXConfiguration setFinancialConfiguration(ARXFinancialConfiguration config) {
        if (config == null) {
            throw new NullPointerException("Argument must not be null");
        }
        this.financialConfiguration = config;
        return this;
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
     * When the size of the solution space exceeds the given number of transformations,
     * ARX will use a heuristic search strategy. The default is 100.000.
     * @param numberOfTransformations
     * @return
     */
    public void setHeuristicSearchThreshold(int numberOfTransformations) {
        if (numberOfTransformations <= 0) { throw new IllegalArgumentException("Parameter must be >= 0"); }
        this.heuristicSearchThreshold = numberOfTransformations;
    }

    /**
     * The heuristic search algorithm will terminate after the given number of milliseconds.
     * The default is 30 seconds.
     * @param timeInMillis
     */
    public void setHeuristicSearchTimeLimit(int timeInMillis) {
        if (timeInMillis <= 0) { throw new IllegalArgumentException("Parameter must be >= 0"); }
        this.heuristicSearchTimeLimit = timeInMillis;
    }
    
    /**
     * Allows for a certain percentage of outliers and thus
     * triggers tuple suppression.
     *
     * @param max
     */
    public void setMaxOutliers(double max) {
        this.relMaxOutliers = max;
    }

    /**
     * Sets the utility metric for measuring information loss .
     *
     * @param metric
     */
    public void setMetric(Metric<?> metric) {
        if (metric == null) { throw new NullPointerException("Metric must not be null"); }
        this.metric = metric;
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
     * Sets whether microaggregation should be based on utility measures
     * @return
     */
    public void setUtilityBasedMicroaggregation(boolean value) {
        this.utilityBasedMicroaggregation = value;
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
     * Returns the maximum number of allowed outliers.
     *
     * @return
     */
    protected final int getAbsoluteMaxOutliers() {
        return this.absMaxOutliers;
    }
    
    /**
     * Returns all criteria (except k-anonymity) as an array. Only used internally. If k-anonymity is included the minimal
     * group size should be obtained and enforced 
     * @return
     */
    protected PrivacyCriterion[] getCriteriaAsArray() {
        return this.aCriteria;
    }

    /**
     * Clones this config and projects everything onto the given subset.<br>
     * - All privacy models will be cloned<br>
     * - Subsets in d-presence will be projected accordingly<br>
     * - Utility measures will be cloned<br>
     * - Replaces estimated k-map with according k-anonymity<br>
     * @param gsFactor 
     *
     * @return
     */
    protected ARXConfiguration getInstanceForLocalRecoding(RowSet rowset, double gsFactor) {

        // Check, if we can do this
        for (PrivacyCriterion criterion : this.getCriteria()) {
            if (!criterion.isLocalRecodingSupported()) {
                throw new IllegalStateException("Local recoding not supported.");
            }
        }
        
        // Prepare a subset
        DataSubset subset = this.getSubset();
        if (subset != null) {
            subset = subset.getSubsetInstance(rowset);
        } else {
            subset = DataSubset.create(rowset.length(), rowset);
        }
        
        // Clone all criteria
        boolean subsetAdded = false;
        HashSet<PrivacyCriterion> criteria = new HashSet<PrivacyCriterion>();
        for (PrivacyCriterion criterion : this.getCriteria()) {
            
            // Clone and store
            PrivacyCriterion clone = criterion.clone(subset);
            subsetAdded |= criterion.isSubsetAvailable();
            
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
        
        // Make sure that we have added the subset
        if (!subsetAdded) {
            criteria.add(new Inclusion(subset));
        }
        
        // Clone the config
        ARXConfiguration result = this.clone();
        result.aCriteria = null;
        result.criteria = criteria;
        MetricConfiguration utilityConfig = result.getMetric().getConfiguration();
        utilityConfig.setGsFactor(gsFactor);
        result.metric = result.getMetric().getDescription().createInstance(utilityConfig);
        
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
     * Returns the minimal size of an equivalence class induced by the defined privacy models.
     * @return If k-anonymity is contained, k is returned. If l-diversity is contained, l is returned.
     * If both are contained max(k,l) is returned. Otherwise, Integer.MAX_VALUE is returned.
     */
    protected int getMinimalGroupSize() {

        // Init
        int result = -1;

        // For each
        for (PrivacyCriterion c : this.getCriteria()) {
            if (c.isMinimalClassSizeAvailable()) {
                result = Math.max(result, c.getMinimalClassSize());
            }
        }

        // Check & return
        if (result == -1) return Integer.MAX_VALUE;
        else return result;
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
    protected SampleBasedCriterion[] getSampleBasedCriteriaAsArray() {
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
        if (manager.getDataAnalyzed().getArray() != null) {
            this.requirements |= ARXConfiguration.REQUIREMENT_DISTRIBUTION;
        }

        // Initialize
        for (PrivacyCriterion c : criteria) {
            c.initialize(manager, this);
        }

        int dataLength = 0;
        if (this.getSubset() != null) {
            dataLength = getSubset().getArray().length;
        } else {
            dataLength = manager.getDataGeneralized().getDataLength();
        }

        // Compute max outliers
        if (this.containsCriterion(EDDifferentialPrivacy.class)) {
            absMaxOutliers = (int) dataLength;
        } else {
            absMaxOutliers = (int) Math.floor(this.relMaxOutliers * (double) dataLength);
        }

        // Compute optimized array with criteria, assuming complexities
        // dPresence <= dDisclosurePrivacy <= lDiversity <= tCloseness and ignoring kAnonymity
        List<PrivacyCriterion> list = new ArrayList<PrivacyCriterion>();
        if (this.containsCriterion(DPresence.class)) {
            list.add(this.getCriterion(DPresence.class));
        }
        if (this.containsCriterion(KMap.class)) {
            list.add(this.getCriterion(KMap.class));
        }
        if (this.containsCriterion(DDisclosurePrivacy.class)) {
            list.addAll(this.getCriteria(DDisclosurePrivacy.class));
        }
        if (this.containsCriterion(LDiversity.class)) {
            list.addAll(this.getCriteria(LDiversity.class));
        }
        if (this.containsCriterion(TCloseness.class)) {
            list.addAll(this.getCriteria(TCloseness.class));
        }
        if (this.containsCriterion(FinancialProsecutorPrivacy.class)) {
            list.addAll(this.getCriteria(FinancialProsecutorPrivacy.class));
        }
        if (this.containsCriterion(FinancialProsecutorNoAttackPrivacy.class)) {
            list.addAll(this.getCriteria(FinancialProsecutorNoAttackPrivacy.class));
        }
        if (this.containsCriterion(FinancialJournalistPrivacy.class)) {
            list.addAll(this.getCriteria(FinancialJournalistPrivacy.class));
        }
        if (this.containsCriterion(FinancialJournalistNoAttackPrivacy.class)) {
            list.addAll(this.getCriteria(FinancialJournalistNoAttackPrivacy.class));
        }
        this.aCriteria = list.toArray(new PrivacyCriterion[0]);
        
        // Compute array of sample-based criteria
        this.bCriteria = new SampleBasedCriterion[0];
        if (this.containsCriterion(SampleBasedCriterion.class)) {
            this.bCriteria = this.getCriteria(SampleBasedCriterion.class).toArray(new SampleBasedCriterion[0]);
        }

        // Compute snapshot length
        this.snapshotLength = 2;
        if (this.requires(REQUIREMENT_DISTRIBUTION)) {
            this.snapshotLength += 2 * manager.getDataAnalyzed().getHeader().length;
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
