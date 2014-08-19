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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an input or output configuration
 * 
 * @author Fabian Prasser
 */
public class ModelConfiguration implements Serializable, Cloneable {

    /** SVUID */
    private static final long                serialVersionUID  = -2887699232096897527L;
    /** Minimum generalization */
    private Map<String, Integer>             min               = new HashMap<String, Integer>();
    /** Maximum generalization */
    private Map<String, Integer>             max               = new HashMap<String, Integer>();
    /** Input data */
    private transient Data                   input             = null;
    /** Associated ARXConfiguration */
    private ARXConfiguration                 config            = ARXConfiguration.create();
    /** Should outliers be removed */
    private boolean                          removeOutliers    = true;
    /** Is this model modified */
    private boolean                          modified          = false;
    /** The associated hierarchies */
    private Map<String, Hierarchy>           hierarchies       = new HashMap<String, Hierarchy>();
    /** The associated research subset */
    private RowSet                           researchSubset    = null;
    /** The suppression weight */
    private Double                           suppressionWeight = null;
    /** Hierarchy builder*/
    private Map<String, HierarchyBuilder<?>> hierarchyBuilders = null;

    /**
     * Delegates to an instance of ARXConfiguration
     * @param c
     * @return
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        setModified();
        return config.addCriterion(c);
    }
    
    @Override
    public ModelConfiguration clone() {

        final ModelConfiguration c = new ModelConfiguration();
        c.removeOutliers = removeOutliers;
        c.input = input;
        c.min = new HashMap<String, Integer>(min);
        c.max = new HashMap<String, Integer>(max);
        c.config = config.clone();
        c.hierarchies = new HashMap<String, Hierarchy>(hierarchies);
        if (this.containsCriterion(DPresence.class)) {
            c.researchSubset = this.getCriterion(DPresence.class).getSubset().getSet();
        } else {
            c.researchSubset = this.researchSubset.clone();
        }
        c.suppressionWeight = this.suppressionWeight;
        return c;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        return config.containsCriterion(clazz);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public double getAllowedOutliers() {
        return config.getMaxOutliers();
    }

    /**
     * Returns the associated attribute weight
     * @param attribute
     * @return
     */
    public double getAttributeWeight(String attribute) {
        return config.getAttributeWeight(attribute);
    }
    
    /**
     * Returns all weights
     * @return
     */
    public Map<String, Double> getAttributeWeights() {
        return config.getAttributeWeights();
    }

    /**
     * Returns the current config
     * @return
     */
    public ARXConfiguration getConfig(){
    	return config;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return config.getCriteria();
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
        return config.getCriteria(clazz);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
        return config.getCriterion(clazz);
    }
    
    /**
     * Returns the set of all assigned hierarchies
     * @return
     */
    public Map<String, Hierarchy> getHierarchies(){
        return this.hierarchies;
    }

    /**
     * Returns the assigned hierarchy, if any. Else null.
     * @param attribute
     */
    public Hierarchy getHierarchy(String attribute){
        return this.hierarchies.get(attribute);
    }

    /**
     * Returns the according builder
     * @param attr
     * @return
     */
    public HierarchyBuilder<?> getHierarchyBuilder(String attr) {
        if (hierarchyBuilders==null) return null;
        else return hierarchyBuilders.get(attr);
    }

    /**
     * @return the input
     */
    public Data getInput() {
        return input;
    }
    
    /**
     * Maximum generalization
     * @param attribute
     * @param min
     */
    public Integer getMaximumGeneralization(String attribute){
        if (this.max == null) {
            return null;
        }
        return this.max.get(attribute);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public Metric<?> getMetric() {
        return config.getMetric();
    }
    
    /**
     * Minimum generalization
     * @param attribute
     * @param min
     */
    public Integer getMinimumGeneralization(String attribute){
        if (this.min == null) {
            return null;
        }
        return this.min.get(attribute);
    }

    /**
     * Returns the current research subset
     * @return
     */
	public RowSet getResearchSubset() {
		return researchSubset;
	}

    /**
     * Returns the suppression/generalization weight, that will be respected by
     * the NDS metric
     * @return
     */
    public double getSuppressionWeight() {

        // For backwards compatibility
        if (this.suppressionWeight == null){
            this.suppressionWeight = 0.5d;
        }
        return suppressionWeight;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public boolean isCriterionMonotonic() {
        return config.isCriterionMonotonic();
    }

    /**
     * Has the config been modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return config.isPracticalMonotonicity();
    }

    /**
	 * Protect sensitive associations
	 * @return
	 */
	public boolean isProtectSensitiveAssociations() {
		return config.isProtectSensitiveAssociations();
	}
    
    /**
     * Should outliers be removed
     * @return
     */
    public boolean isRemoveOutliers() {
        return removeOutliers;
    }

    /**
     * Removes all criteria
     */
    public void removeAllCriteria() {
        this.getCriteria().clear();
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> boolean removeCriterion(PrivacyCriterion c) {
        setModified();
        return config.removeCriterion(c);
    }

    /**
     * Removes the builder for the given attribute
     * @param attr
     * @return
     */
    public void removeHierarchyBuilder(String attr){
        if (hierarchyBuilders==null) return;
        hierarchyBuilders.remove(attr);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param supp
     */
    public void setAllowedOutliers(double supp) {
        setModified();
        config.setMaxOutliers(supp);
    }
    
    /**
     * Sets the according attribute weight
     * @param attribute
     * @param weight
     */
    public void setAttributeWeight(String attribute, Double weight) {
        config.setAttributeWeight(attribute, weight);
    }

    /**
     * Assigns a hierarchy
     * @param attribute
     * @param hierarchy
     */
    public void setHierarchy(String attribute, Hierarchy hierarchy){
        this.hierarchies.put(attribute, hierarchy);
        this.setModified();
    }
	
	/**
     * Sets the given hierarchy builder
     * @param attr
     * @param builder
     */
    public void setHierarchyBuilder(String attr, HierarchyBuilder<?> builder) {
        if (hierarchyBuilders==null){
            hierarchyBuilders = new HashMap<String, HierarchyBuilder<?>>();
        }
        hierarchyBuilders.put(attr, builder);
    }

	/**
     * @param data
     *            the input to set
     */
    public void setInput(final Data data) {
        input = data;
        setModified();
    }
	
	/**
     * Maximum generalization
     * @param attribute
     * @param min
     */
    public void setMaximumGeneralization(String attribute, Integer max){
        if (this.max == null) {
            this.max = new HashMap<String, Integer>();
        }
        this.max.put(attribute, max);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param metric
     */
    public void setMetric(Metric<?> metric) {
        setModified();
        config.setMetric(metric);
    }
    
    /**
     * Minimum generalization
     * @param attribute
     * @param min
     */
    public void setMinimumGeneralization(String attribute, Integer min){
        if (this.min == null) {
            this.min = new HashMap<String, Integer>();
        }
        this.min.put(attribute, min);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @param assumeMonotonicity
     */
    public void setPracticalMonotonicity(boolean assumeMonotonicity) {
        setModified();
        config.setPracticalMonotonicity(assumeMonotonicity);
    }

    /**
	 * Protect sensitive associations
	 * @param selection
	 */
	public void setProtectSensitiveAssociations(boolean selection) {
		config.setProtectSensitiveAssociations(selection);
	}
    
    /**
     * Sets whether outliers should be removed
     * @param removeOutliers
     */
    public void setRemoveOutliers(final boolean removeOutliers) {
        this.removeOutliers = removeOutliers;
        setModified();
    }
    
    /**
	 * Sets the current research subset
	 * @param subset
	 */
	public void setResearchSubset(RowSet subset) {
	    this.researchSubset = subset;
	}

    /**
     * Sets the suppression/generalization weight, that will be respected by
     * the NDS metric
     * @param suppressionWeight
     */
    public void setSuppressionWeight(double suppressionWeight) {
        this.suppressionWeight = suppressionWeight;
    }

    /**
     * Sets the config unmodified
     */
    public void setUnmodified() {
        modified = false;
    }

    /**
     * Mark as modified
     */
    private void setModified() {
        modified = true;
    }
}