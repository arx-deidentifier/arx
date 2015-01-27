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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an input or output configuration.
 *
 * @author Fabian Prasser
 */
public class ModelConfiguration implements Serializable, Cloneable {

    /** SVUID. */
    private static final long                serialVersionUID  = -2887699232096897527L;
    
    /** Minimum generalization. */
    private Map<String, Integer>             min               = new HashMap<String, Integer>();
    
    /** Maximum generalization. */
    private Map<String, Integer>             max               = new HashMap<String, Integer>();
    
    /** Input data. */
    private transient Data                   input             = null;
    
    /** Associated ARXConfiguration. */
    private ARXConfiguration                 config            = ARXConfiguration.create();
    
    /** Is this model modified. */
    private boolean                          modified          = false;
    
    /** The associated hierarchies. */
    private Map<String, Hierarchy>           hierarchies       = new HashMap<String, Hierarchy>();
    
    /** The associated research subset. */
    private RowSet                           researchSubset    = null;
    
    /** The suppression weight. */
    private Double                           suppressionWeight = null;
    
    /** Hierarchy builder. */
    private Map<String, HierarchyBuilder<?>> hierarchyBuilders = null;

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param c
     * @return
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        setModified();
        return config.addCriterion(c);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public ModelConfiguration clone() {

        final ModelConfiguration c = new ModelConfiguration();
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
     * Delegates to an instance of ARXConfiguration.
     *
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        return config.containsCriterion(clazz);
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public double getAllowedOutliers() {
        return config.getMaxOutliers();
    }

    /**
     * Returns the associated attribute weight.
     *
     * @param attribute
     * @return
     */
    public double getAttributeWeight(String attribute) {
        return config.getAttributeWeight(attribute);
    }
    
    /**
     * Returns all weights.
     *
     * @return
     */
    public Map<String, Double> getAttributeWeights() {
        return config.getAttributeWeights();
    }

    /**
     * Returns the current config.
     *
     * @return
     */
    public ARXConfiguration getConfig(){
    	return config;
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return config.getCriteria();
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
        return config.getCriteria(clazz);
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
        return config.getCriterion(clazz);
    }
    
    /**
     * Returns the set of all assigned hierarchies.
     *
     * @return
     */
    public Map<String, Hierarchy> getHierarchies(){
        return this.hierarchies;
    }

    /**
     * Returns the assigned hierarchy, if any. Else null.
     *
     * @param attribute
     * @return
     */
    public Hierarchy getHierarchy(String attribute){
        return this.hierarchies.get(attribute);
    }

    /**
     * Returns the according builder.
     *
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
     * Maximum generalization.
     *
     * @param attribute
     * @return
     */
    public Integer getMaximumGeneralization(String attribute){
        if (this.max == null) {
            return null;
        }
        return this.max.get(attribute);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public Metric<?> getMetric() {
        return config.getMetric();
    }
    
    /**
     * Minimum generalization.
     *
     * @param attribute
     * @return
     */
    public Integer getMinimumGeneralization(String attribute){
        if (this.min == null) {
            return null;
        }
        return this.min.get(attribute);
    }

    /**
     * Returns the current research subset.
     *
     * @return
     */
	public RowSet getResearchSubset() {
		return researchSubset;
	}

    /**
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#getSuppressionString()
     */
    public String getSuppressionString() {
        return config.getSuppressionString();
    }

    /**
     * Returns the suppression/generalization weight, that will be respected by
     * the NDS metric.
     *
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
     * @param type
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#isAttributeTypeSuppressed(org.deidentifier.arx.AttributeType)
     */
    public boolean isAttributeTypeSuppressed(AttributeType type) {
        return config.isAttributeTypeSuppressed(type);
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public boolean isCriterionMonotonic() {
        return config.isCriterionMonotonic();
    }

    /**
     * Has the config been modified.
     *
     * @return
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return config.isPracticalMonotonicity();
    }

    /**
     * Protect sensitive associations.
     *
     * @return
     */
	public boolean isProtectSensitiveAssociations() {
		return config.isProtectSensitiveAssociations();
	}

    /**
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#isSuppressionAlwaysEnabled()
     */
    public boolean isSuppressionAlwaysEnabled() {
        return config.isSuppressionAlwaysEnabled();
    }

    /**
     * Removes all criteria.
     */
    public void removeAllCriteria() {
        this.getCriteria().clear();
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param <T>
     * @param c
     * @return
     */
    public <T extends PrivacyCriterion> boolean removeCriterion(PrivacyCriterion c) {
        setModified();
        return config.removeCriterion(c);
    }

    /**
     * Removes the builder for the given attribute.
     *
     * @param attr
     */
    public void removeHierarchyBuilder(String attr){
        if (hierarchyBuilders==null) return;
        setModified();
        hierarchyBuilders.remove(attr);
    }
	
	/**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param supp
     */
    public void setAllowedOutliers(double supp) {
        setModified();
        config.setMaxOutliers(supp);
    }

	/**
     * @param type
     * @param enabled
     * @see org.deidentifier.arx.ARXConfiguration#setAttributeTypeSuppressed(org.deidentifier.arx.AttributeType, boolean)
     */
    public void setAttributeTypeSuppressed(AttributeType type, boolean enabled) {
        setModified();
        config.setAttributeTypeSuppressed(type, enabled);
    }
	
	/**
     * Sets the according attribute weight.
     *
     * @param attribute
     * @param weight
     */
    public void setAttributeWeight(String attribute, Double weight) {
        setModified();
        config.setAttributeWeight(attribute, weight);
    }

    /**
     * Assigns a hierarchy.
     *
     * @param attribute
     * @param hierarchy
     */
    public void setHierarchy(String attribute, Hierarchy hierarchy){
        this.hierarchies.put(attribute, hierarchy);
        this.setModified();
    }
    
    /**
     * Sets the given hierarchy builder.
     *
     * @param attr
     * @param builder
     */
    public void setHierarchyBuilder(String attr, HierarchyBuilder<?> builder) {
        if (hierarchyBuilders==null){
            hierarchyBuilders = new HashMap<String, HierarchyBuilder<?>>();
        }
        setModified();
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
     * Maximum generalization.
     *
     * @param attribute
     * @param max
     */
    public void setMaximumGeneralization(String attribute, Integer max){
        if (this.max == null) {
            this.max = new HashMap<String, Integer>();
        }
        setModified();
        this.max.put(attribute, max);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param metric
     */
    public void setMetric(Metric<?> metric) {
        setModified();
        config.setMetric(metric);
    }
    
    /**
     * Minimum generalization.
     *
     * @param attribute
     * @param min
     */
    public void setMinimumGeneralization(String attribute, Integer min){
        if (this.min == null) {
            this.min = new HashMap<String, Integer>();
        }
        setModified();
        this.min.put(attribute, min);
    }

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param assumeMonotonicity
     */
    public void setPracticalMonotonicity(boolean assumeMonotonicity) {
        setModified();
        config.setPracticalMonotonicity(assumeMonotonicity);
    }

    /**
     * Protect sensitive associations.
     *
     * @param selection
     */
	public void setProtectSensitiveAssociations(boolean selection) {
	    setModified();
		config.setProtectSensitiveAssociations(selection);
	}

    /**
     * Sets the current research subset.
     *
     * @param subset
     */
	public void setResearchSubset(RowSet subset) {
	    setModified();
	    this.researchSubset = subset;
	}

    /**
     * @param enabled
     * @see org.deidentifier.arx.ARXConfiguration#setSuppressionAlwaysEnabled(boolean)
     */
    public void setSuppressionAlwaysEnabled(boolean enabled) {
        setModified();
        config.setSuppressionAlwaysEnabled(enabled);
    }

    /**
     * @param suppressionString
     * @see org.deidentifier.arx.ARXConfiguration#setSuppressionString(java.lang.String)
     */
    public void setSuppressionString(String suppressionString) {
        setModified();
        config.setSuppressionString(suppressionString);
    }

    /**
     * Sets the suppression/generalization weight, that will be respected by
     * the NDS metric.
     *
     * @param suppressionWeight
     */
    public void setSuppressionWeight(double suppressionWeight) {
        setModified();
        this.suppressionWeight = suppressionWeight;
    }

    /**
     * Sets the config unmodified.
     */
    public void setUnmodified() {
        modified = false;
    }

    /**
     * Mark as modified.
     */
    private void setModified() {
        modified = true;
    }
}