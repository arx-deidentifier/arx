/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.metric.Metric;

/**
 * A generic configuration for the ARX anonymizer
 * @author Fabian Prasser
 */
public class ARXConfiguration implements Serializable, Cloneable {

    /** For serialization*/
    private static final long     serialVersionUID              = -6713510386735241964L;

    /** Do the criteria require a counter per equivalence class*/
    public static final int       REQUIREMENT_COUNTER           = 0x1;

    /** Do the criteria require a second counter */
    public static final int       REQUIREMENT_SECONDARY_COUNTER = 0x2;

    /** Do the criteria require distributions of sensitive values in the equivalence classes */
    public static final int       REQUIREMENT_DISTRIBUTION      = 0x4;

    /**
     * Creates a new config without tuple suppression
     */
    public static ARXConfiguration create() {
        return new ARXConfiguration();
    }

    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression
     * @param supp
     */
    public static ARXConfiguration create(double supp) {
        return new ARXConfiguration(supp);
    }

    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression. Defines the metric for measuring information loss.
     * @param supp
     * @param metric
     */
    public static ARXConfiguration create(double supp, Metric<?> metric) {
        return new ARXConfiguration(supp, metric);
    }

    /**
     * Creates a new config that allows to define the metric for measuring information loss.
     * @param metric
     */
    public static ARXConfiguration create(Metric<?> metric) {
        return new ARXConfiguration(metric);
    }

    /** Do we assume practical monotonicity */
    private boolean               practicalMonotonicity         = false;

    /** Relative tuple outliers */
    private double                relMaxOutliers                = -1;

    /** Absolute tuple outliers*/
    private int                   absMaxOutliers                = 0;

    /** Criteria*/
    private PrivacyCriterion[]    aCriteria                     = new PrivacyCriterion[0];

    /** The criteria*/
    private Set<PrivacyCriterion> criteria                      = new HashSet<PrivacyCriterion>();

    /** The requirements per equivalence class*/
    private int                   requirements                  = 0x0;

    /** The metric. */
    private Metric<?>             metric                        = Metric.createDMStarMetric();

    /** The snapshot length*/
    private int                   snapshotLength;

    /** Make sure that no information can be derived from associations between sensitive attributes*/
    private boolean               protectSensitiveAssociations  = false;

    /** Perform additional dfs-phase*/
    private boolean               guaranteeOptimalityForNonMonotonicMetric = false;
    
    /**
     * Creates a new config without tuple suppression
     */
    private ARXConfiguration() {
        this.relMaxOutliers = 0d;
    }

    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression
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
     * Adds a criterion to the configuration
     * @param c
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        if ((c instanceof DPresence) && 
            this.containsCriterion(DPresence.class)) {
            throw new RuntimeException("Must not add more than one d-presence criterion");
        } else if ((c instanceof KAnonymity) && 
               this.containsCriterion(KAnonymity.class)) { 
               throw new RuntimeException("Must not add more than one k-anonymity criterion"); 
        }
        criteria.add(c);
        return this;
    }

    /**
     * Clones this config
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
        return result;

    }

    /**
     * Returns whether the configuration contains a criterion of the given class
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) { return true; }
        }
        return false;
    }

    /**
     * Returns the maximum number of allowed outliers
     * @return
     */
    public final int getAbsoluteMaxOutliers() {
        return this.absMaxOutliers;
    }

    /**
     * Returns all criteria.
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return this.criteria;
    }

    /**
     * Returns all privacy criteria that are instances of the given class
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
        Set<T> result = new HashSet<T>();
        for (PrivacyCriterion c : criteria) {
            if (clazz.isInstance(c)) {
                result.add((T) c);
            }
        }
        return result;
    }

    /**
     * Returns all criteria as array. Only used internally.
     * @return
     */
    public PrivacyCriterion[] getCriteriaAsArray() {
        return this.aCriteria;
    }

    /**
     * Returns an instance of the class, if any. Throws an exception if more than one such criterion exists.
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
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
     * Returns the maximum number of allowed outliers
     * @return
     */
    public final double getMaxOutliers() {
        return relMaxOutliers;
    }

    /**
     * Returns the metric used for measuring information loss
     * @return
     */
    public Metric<?> getMetric() {
        return this.metric;
    }

    /**
     * Returns the minimal size of an equivalence class induced by the contained criteria.
     * @return If k-anonymity is contained, k is returned. If l-diversity is contained, l is returned.
     * If both are contained max(k,l) is returned. Otherwise, Integer.MAX_VALUE is returned.
     */
    public int getMinimalGroupSize() {
        int k = -1;
        int l = -1;

        if (this.containsCriterion(KAnonymity.class)) {
            k = this.getCriterion(KAnonymity.class).getK();
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
     * Returns the criterias requirements
     * @return
     */
    public int getRequirements() {
        return this.requirements;
    }

    /**
     * Returns the specific length of each entry in a snapshot
     * @return
     */
    public int getSnapshotLength() {
        return this.snapshotLength;
    }

    /**
     * Determines whether the anonymity criterion is montonic
     * 
     * @return
     */
    public final boolean isCriterionMonotonic() {

        if (relMaxOutliers == 0d) { return true; }

        for (PrivacyCriterion c : criteria) {
            if (!c.isMonotonic()) return false;
        }
        // Yes
        return true;
    }

    /**
     * Is practical monotonicity assumed
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }
    
    /**
     * Should optimality be guaranteed for non-monotonic metrics
     * @return
     */
    public boolean isGuaranteeOptimalityForNonMonotonicMetric(){
        return this.guaranteeOptimalityForNonMonotonicMetric;
    }

    /**
     * Returns, whether the anonymizer should take associations between sensitive attributes into account
     */
    public boolean isProtectSensitiveAssociations() {
        return this.protectSensitiveAssociations;
    }

    /**
     * Removes the given criterion
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> boolean removeCriterion(PrivacyCriterion arg) {
        return criteria.remove(arg);
    }

    /**
     * Convenience method for checking the requirements
     * @param requirement
     * @return
     */
    public boolean requires(int requirement) {
        return (this.requirements & requirement) != 0;
    }
    
    /**
     * Perform an additional exhaustive search to find the transformation
     * with guaranteed minimal information loss?
     * @param val
     */
    public void setGuaranteeOptimalityForNonMonotonicMetric(boolean val){
        this.guaranteeOptimalityForNonMonotonicMetric = val;
    }

    /**
     * Allows for a certain percentage of outliers and thus
     * triggers tuple suppression
     * @param supp
     */
    public void setMaxOutliers(double supp) {
        this.relMaxOutliers = supp;
    }

    public void setMetric(Metric<?> metric) {
        if (metric == null) { throw new NullPointerException("Metric must not be null"); }
        this.metric = metric;
    }

    /**
     * Set, if practical monotonicity assumed
     * @return
     */
    public void setPracticalMonotonicity(final boolean assumeMonotonicity) {
        this.practicalMonotonicity = assumeMonotonicity;
    }

    /**
     * Set, whether the anonymizer should take associations between sensitive attributes into account
     * @param protect
     */
    public void setProtectSensitiveAssociations(boolean protect) {
        this.protectSensitiveAssociations = protect;
    }

    /**
     * Initializes the configuration
     * @param manager
     */
    protected void initialize(DataManager manager) {

        // Check
        if (criteria.isEmpty()) { throw new RuntimeException("At least one privacy criterion must be specified!"); }

        // Compute requirements
        this.requirements = 0x0;
        for (PrivacyCriterion c : criteria) {
            this.requirements |= c.getRequirements();
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
        } else {
            dataLength = manager.getDataQI().getDataLength();
        }

        // Compute max outliers
        absMaxOutliers = (int) Math.floor(this.relMaxOutliers * (double) dataLength);

        // Compute optimized array with criteria, assuming complexities
        // dPresence <= lDiversity <= tCloseness and ignoring kAnonymity
        // TODO: Configuration should not know anything about them
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

        // Compute snapshot length
        this.snapshotLength = 2;
        if (this.requires(REQUIREMENT_DISTRIBUTION)) {
            this.snapshotLength += 2 * manager.getDataSE().getHeader().length;
        }
        if (this.requires(REQUIREMENT_SECONDARY_COUNTER)) {
            this.snapshotLength += 1;
        }
    }
}
