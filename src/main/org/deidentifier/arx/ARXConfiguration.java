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

package org.deidentifier.arx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.SampleBasedCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.metric.Metric;

/**
 * A generic configuration for the ARX anonymizer.
 *
 * @author Fabian Prasser
 */
public class ARXConfiguration implements Serializable, Cloneable {
    
    // TODO: While in use, this configuration object should be locked, similar to, e.g., DataDefinition

    /**
     * Class for internal use that provides access to more details.
     * TODO: This class is a hack and should be removed in future releases
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

    /** Make sure that no information can be derived from associations between sensitive attributes. */
    private boolean                            protectSensitiveAssociations          = false;

    /** Relative tuple outliers. */
    private double                             relMaxOutliers                        = -1;

    /** The requirements per equivalence class. */
    private int                                requirements                          = 0x0;

    /** The snapshot length. */
    private int                                snapshotLength;

    /**
     * Defines values of which attribute type are to be replaced by the suppression string in suppressed tuples. */
    private Integer                            suppressedAttributeTypes              = 1 << AttributeType.ATTR_TYPE_QI;

    /** The string with which suppressed values are to be replaced. */
    private String                             suppressionString                     = "*";

    /**
     * Determines whether suppression is applied to the output of anonymous as well as non-anonymous transformations. */
    private Boolean                            suppressionAlwaysEnabled              = true;

    /** TODO: This is a hack and should be removed in future releases. */
    private transient ARXConfigurationInternal accessibleInstance                    = null;

    /** Are we performing optimal anonymization for sample-based criteria? */
    private boolean                            heuristicSearchForSampleBasedCriteria = false;

    /** Should we use the heuristic search algorithm? */
    private boolean                            heuristicSearchEnabled                = false;

    /** We will use the heuristic algorithm, if the size of the search space exceeds this threshold */
    private Integer                            heuristicSearchThreshold              = 100000;

    /** The heuristic algorithm will terminate after the given time limit */
    private Integer                            heuristicSearchTimeLimit              = 30000;

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
        checkArgument(c);
                
        if ((c instanceof DPresence) && 
            this.containsCriterion(DPresence.class)) {
            throw new RuntimeException("Must not add more than one d-presence criterion");
        } else if ((c instanceof KAnonymity) && 
               this.containsCriterion(KAnonymity.class)) { 
               throw new RuntimeException("Must not add more than one k-anonymity criterion"); 
        }
        criteria.add(c);
        
        if (this.containsCriterion(EDDifferentialPrivacy.class) && 
           (this.containsCriterion(DPresence.class) || this.containsCriterion(Inclusion.class))) {
            criteria.remove(c);
            throw new RuntimeException("Differential privacy must not be combined with a research subset");
        }
        
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
        result.protectSensitiveAssociations = this.protectSensitiveAssociations;
        result.suppressionString = this.suppressionString;
        result.suppressionAlwaysEnabled = this.suppressionAlwaysEnabled;
        result.suppressedAttributeTypes = this.suppressedAttributeTypes;
        result.heuristicSearchForSampleBasedCriteria = this.heuristicSearchForSampleBasedCriteria;
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
        if (metric.isMonotonic() || (this.getMaxOutliers() == 0d) || this.isPracticalMonotonicity()) {
            return Monotonicity.FULL;
        }  else {
            return Monotonicity.NONE;
        }
    }
    
    /**
     * Sets the string with which suppressed values are to be replaced. Default is <code>*</code>.
     * @return
     */
    public String getSuppressionString(){
        // Ensure backwards compatibility
        if (suppressionString == null) { return "*"; }
        return this.suppressionString;
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
     * Returns, whether the anonymizer should take associations between sensitive attributes into account.
     *
     * @return
     */
    public boolean isProtectSensitiveAssociations() {
        return this.protectSensitiveAssociations;
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
     * Do we guarantee optimality for sample-based criteria?
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
     * @param supp
     */
    public void setMaxOutliers(double supp) {
        this.relMaxOutliers = supp;
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
     * Set, whether the anonymizer should take associations between sensitive attributes into account.
     *
     * @param protect
     */
    public void setProtectSensitiveAssociations(boolean protect) {
        this.protectSensitiveAssociations = protect;
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
     * Sets the string with which suppressed values are to be replaced. Default is <code>*</code>.
     * @param suppressionString
     */
    public void setSuppressionString(String suppressionString){
    	checkArgument(suppressionString);
        this.suppressionString = suppressionString;    	
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
     * TODO: This is a hack and should be removed in future releases.
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
     * Returns the minimal size of an equivalence class induced by the contained criteria.
     * @return If k-anonymity is contained, k is returned. If l-diversity is contained, l is returned.
     * If both are contained max(k,l) is returned. Otherwise, Integer.MAX_VALUE is returned.
     */
    protected int getMinimalGroupSize() {
        int k = -1;
        int l = -1;

        if (this.containsCriterion(KAnonymity.class)) {
            k = this.getCriterion(KAnonymity.class).getK();
        }
        
        if (this.containsCriterion(EDDifferentialPrivacy.class)) {
            k = Math.max(k, this.getCriterion(EDDifferentialPrivacy.class).getK());
        }

        if (this.containsCriterion(LDiversity.class)) {
            for (LDiversity c : this.getCriteria(LDiversity.class)) {
                l = Math.max(l, c.getMinimalGroupSize());
            }
        }

        int result = Math.max(k, l);
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
            throw new RuntimeException("At least one privacy criterion must be specified!"); 
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

        // Initialize: Always make sure that d-presence is initialized first, because
        // the research subset needs to be available for initializing t-closeness
        if (this.containsCriterion(DPresence.class)) {
            this.getCriterion(DPresence.class).initialize(manager);
        }
        for (PrivacyCriterion c : criteria) {
            if (!(c instanceof DPresence)) {
                c.initialize(manager);
            }
        }

        int dataLength = 0;
        if (this.containsCriterion(DPresence.class)) {
            dataLength = this.getCriterion(DPresence.class).getSubset().getArray().length;
        } else if (this.containsCriterion(EDDifferentialPrivacy.class)) {
            dataLength = this.getCriterion(EDDifferentialPrivacy.class).getSubset().getArray().length;
        } else {
            dataLength = manager.getDataGeneralized().getDataLength();
        }

        // Compute max outliers
        if (this.containsCriterion(EDDifferentialPrivacy.class)) {
            absMaxOutliers = (int)dataLength;
        } else {
            absMaxOutliers = (int) Math.floor(this.relMaxOutliers * (double) dataLength);
        }

        // Compute optimized array with criteria, assuming complexities
        // dPresence <= lDiversity <= tCloseness and ignoring kAnonymity
        List<PrivacyCriterion> list = new ArrayList<PrivacyCriterion>();
        if (this.containsCriterion(DPresence.class)) {
            list.add(this.getCriterion(DPresence.class));
        }
        if (this.containsCriterion(LDiversity.class)) {
            list.addAll(this.getCriteria(LDiversity.class));
        }
        if (this.containsCriterion(TCloseness.class)) {
            list.addAll(this.getCriteria(TCloseness.class));
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
