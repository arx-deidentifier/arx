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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunctionDescription;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an input or output configuration.
 *
 * @author Fabian Prasser
 */
public class ModelConfiguration implements Serializable, Cloneable {

    /** SVUID. */
    private static final long                                serialVersionUID                  = -2887699232096897527L;

    /** Minimum generalization. */
    private Map<String, Integer>                             min                               = new HashMap<String, Integer>();

    /** Maximum generalization. */
    private Map<String, Integer>                             max                               = new HashMap<String, Integer>();

    /** Input data. */
    private transient Data                                   input                             = null;

    /** Associated ARXConfiguration. */
    private ARXConfiguration                                 config                            = ARXConfiguration.create();

    /** Is this model modified. */
    private boolean                                          modified                          = false;

    /** The associated hierarchies. */
    private Map<String, Hierarchy>                           hierarchies                       = new HashMap<String, Hierarchy>();

    /** The associated microaggregation functions. */
    private Map<String, MicroAggregationFunctionDescription> microAggregationFunctions         = new HashMap<String, MicroAggregationFunctionDescription>();

    /** The associated handling of null values. */
    private Map<String, Boolean>                             microAggregationIgnoreMissingData = new HashMap<String, Boolean>();

    /** The associated mode */
    private Map<String, ModelTransformationMode>             transformationModes               = new HashMap<String, ModelTransformationMode>();

    /** The associated research subset. */
    private RowSet                                           researchSubset                    = null;

    /** The suppression weight. */
    private Double                                           suppressionWeight                 = null;

    /** Hierarchy builder. */
    private Map<String, HierarchyBuilder<?>>                 hierarchyBuilders                 = null;

    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param c
     * @return
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        setModified();
        return config.addPrivacyModel(c);
    }
    
    @Override
    public ModelConfiguration clone() {
        
        final ModelConfiguration c = new ModelConfiguration();
        c.input = input;
        c.min = new HashMap<String, Integer>(min);
        c.max = new HashMap<String, Integer>(max);
        c.config = config.clone();
        c.hierarchies = new HashMap<String, Hierarchy>(hierarchies);
        
        // Clone subset
        boolean found = false;
        for (PrivacyCriterion pc : this.getCriteria()) {
            if (pc.isSubsetAvailable()) {
                DataSubset subset = pc.getDataSubset();
                if (subset != null) {
                    c.researchSubset = subset.getSet();
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            c.researchSubset = this.researchSubset.clone();
        }
        
        c.suppressionWeight = this.suppressionWeight;
        c.microAggregationFunctions = new HashMap<String, MicroAggregationFunctionDescription>(microAggregationFunctions);
        c.microAggregationIgnoreMissingData = new HashMap<String, Boolean>(microAggregationIgnoreMissingData);
        c.transformationModes = new HashMap<String, ModelTransformationMode>(transformationModes);
        return c;
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        return config.isPrivacyModelSpecified(clazz);
    }
    
    /**
     * @return the adversaryCost
     */
    public double getAdversaryCost() {
        return this.config.getCostBenefitConfiguration().getAdversaryCost();
    }
    
    /**
     * @return the adversaryGain
     */
    public double getAdversaryGain() {
        return this.config.getCostBenefitConfiguration().getAdversaryGain();
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public double getSuppressionLimit() {
        return config.getSuppressionLimit();
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
    public ARXConfiguration getConfig() {
        return config;
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return config.getPrivacyModels();
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
        return config.getPrivacyModels(clazz);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
        return config.getPrivacyModel(clazz);
    }
    
    /**
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#getHeuristicSearchThreshold()
     */
    public int getHeuristicSearchThreshold() {
        return config.getHeuristicSearchThreshold();
    }
    

    /**
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#getHeuristicSearchTimeLimit()
     */
    public int getHeuristicSearchTimeLimit() {
        return config.getHeuristicSearchTimeLimit();
    }
    
    /**
     * Returns the set of all assigned hierarchies.
     *
     * @return
     */
    public Map<String, Hierarchy> getHierarchies() {
        return this.hierarchies;
    }

    /**
     * Returns the assigned hierarchy, if any. Else null.
     *
     * @param attribute
     * @return
     */
    public Hierarchy getHierarchy(String attribute) {
        return this.hierarchies.get(attribute);
    }

    /**
     * Returns the according builder.
     *
     * @param attr
     * @return
     */
    public HierarchyBuilder<?> getHierarchyBuilder(String attr) {
        if (hierarchyBuilders == null) return null;
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
    public Integer getMaximumGeneralization(String attribute) {
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
        return config.getQualityModel();
    }

    /**
     * Returns the microaggregation function.
     *
     * @param attribute
     * @return
     */
    public MicroAggregationFunctionDescription getMicroAggregationFunction(String attribute) {
        if (this.microAggregationFunctions == null) {
            this.microAggregationFunctions = new HashMap<String, MicroAggregationFunctionDescription>();
        }
        return this.microAggregationFunctions.get(attribute);
    }
    
    /**
     * Returns the associated handling of missing data
     * @param attribute
     * @return
     */
    public Boolean getMicroAggregationIgnoreMissingData(String attribute) {
        if (this.microAggregationIgnoreMissingData == null) {
            this.microAggregationIgnoreMissingData = new HashMap<String, Boolean>();
        }
        Boolean ignore = this.microAggregationIgnoreMissingData.get(attribute);
        if (ignore == null) {
            return true;
        } else {
            return ignore;
        }
    }
    
    /**
     * Minimum generalization.
     *
     * @param attribute
     * @return
     */
    public Integer getMinimumGeneralization(String attribute) {
        if (this.min == null) {
            return null;
        }
        return this.min.get(attribute);
    }
    
    /**
     * @return the publisherBenefit
     */
    public double getPublisherBenefit() {
        return this.config.getCostBenefitConfiguration().getPublisherBenefit();
    }
    
    /**
     * @return the publisherLoss
     */
    public double getPublisherLoss() {
        return this.config.getCostBenefitConfiguration().getPublisherLoss();
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
     * Returns the suppression/generalization weight, that will be respected by
     * the NDS metric.
     *
     * @return
     */
    public double getSuppressionWeight() {
        
        // For backwards compatibility
        if (this.suppressionWeight == null) {
            this.suppressionWeight = 0.5d;
        }
        return suppressionWeight;
    }
    
    /**
     * Returns the transformation mode for the given attribute. Returns ModelTransformationMode.GENERALIZATION
     * if no entry was found, for backwards compatibility
     * @param attribute
     * @return
     */
    public ModelTransformationMode getTransformationMode(String attribute) {
        if (this.transformationModes == null) {
            this.transformationModes = new HashMap<String, ModelTransformationMode>();
        }
        ModelTransformationMode result = this.transformationModes.get(attribute);
        if (result != null) {
            return result;
        } else {
            return ModelTransformationMode.GENERALIZATION;
        }
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
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#isUseHeuristicSearchForSampleBasedCriteria()
     */
    public boolean isHeuristicForSampleBasedCriteria() {
        return config.isUseHeuristicSearchForSampleBasedCriteria();
    }
    
    /**
     * @return
     * @see org.deidentifier.arx.ARXConfiguration#isHeuristicSearchEnabled()
     */
    public boolean isHeuristicSearchEnabled() {
        return config.isHeuristicSearchEnabled();
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
     * Removes a hierarchy.
     *
     * @param attribute
     */
    public void removeHierarchy(String attribute) {
        this.hierarchies.remove(attribute);
        this.setModified();
    }
    
    /**
     * Removes the builder for the given attribute.
     *
     * @param attr
     */
    public void removeHierarchyBuilder(String attr) {
        if (hierarchyBuilders == null) return;
        setModified();
        hierarchyBuilders.remove(attr);
    }
    
    /**
     * @param adversaryCost the adversaryCost to set
     */
    public void setAdversaryCost(double adversaryCost) {
        if (this.getAdversaryCost() != adversaryCost) {
            this.setModified();
        }
        this.config.getCostBenefitConfiguration().setAdversaryCost(adversaryCost);
    }
    
    /**
     * @param adversaryGain the adversaryGain to set
     */
    public void setAdversaryGain(double adversaryGain) {
        if (this.getAdversaryGain() != adversaryGain) {
            this.setModified();
        }
        this.config.getCostBenefitConfiguration().setAdversaryGain(adversaryGain);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration.
     *
     * @param supp
     */
    public void setSuppressionLimit(double supp) {
        setModified();
        config.setSuppressionLimit(supp);
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
     * @param value
     * @see org.deidentifier.arx.ARXConfiguration#setUseHeuristicSearchForSampleBasedCriteria(boolean)
     */
    public void setHeuristicForSampleBasedCriteria(boolean value) {
        config.setUseHeuristicSearchForSampleBasedCriteria(value);
    }
    
    /**
     * @param heuristicSearchEnabled
     * @see org.deidentifier.arx.ARXConfiguration#setHeuristicSearchEnabled(boolean)
     */
    public void setHeuristicSearchEnabled(boolean heuristicSearchEnabled) {
        config.setHeuristicSearchEnabled(heuristicSearchEnabled);
    }
    
    /**
     * @param numberOfTransformations
     * @see org.deidentifier.arx.ARXConfiguration#setHeuristicSearchThreshold(int)
     */
    public void setHeuristicSearchThreshold(int numberOfTransformations) {
        config.setHeuristicSearchThreshold(numberOfTransformations);
    }
    
    /**
     * @param timeInMillis
     * @see org.deidentifier.arx.ARXConfiguration#setHeuristicSearchTimeLimit(int)
     */
    public void setHeuristicSearchTimeLimit(int timeInMillis) {
        config.setHeuristicSearchTimeLimit(timeInMillis);
    }
    
    /**
     * Assigns a hierarchy.
     *
     * @param attribute
     * @param hierarchy
     */
    public void setHierarchy(String attribute, Hierarchy hierarchy) {
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
        if (hierarchyBuilders == null) {
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
    public void setMaximumGeneralization(String attribute, Integer max) {
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
        config.setQualityModel(metric);
    }
    
    /**
     * Assigns a microaggregation function.
     *
     * @param attribute
     * @param microaggregation
     */
    public void setMicroAggregationFunction(String attribute, MicroAggregationFunctionDescription microaggregation) {
        if (this.microAggregationFunctions == null) {
            this.microAggregationFunctions = new HashMap<String, MicroAggregationFunctionDescription>();
        }
        this.microAggregationFunctions.put(attribute, microaggregation);
        this.setModified();
    }
    
    /**
     * Determines whether or not to ignore missing data
     * 
     * @param attribute
     * @param ignoreNullValues
     */
    public void setMicroAggregationIgnoreMissingData(String attribute, boolean ignoreMissingData) {
        if (this.microAggregationIgnoreMissingData == null) {
            this.microAggregationIgnoreMissingData = new HashMap<String, Boolean>();
        }
        this.microAggregationIgnoreMissingData.put(attribute, ignoreMissingData);
        this.setModified();
    }
    
    /**
     * Minimum generalization.
     *
     * @param attribute
     * @param min
     */
    public void setMinimumGeneralization(String attribute, Integer min) {
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
     * @param publisherBenefit the publisherBenefit to set
     */
    public void setPublisherBenefit(double publisherBenefit) {
        if (this.getPublisherBenefit() != publisherBenefit) {
            this.setModified();
        }
        this.config.getCostBenefitConfiguration().setPublisherBenefit(publisherBenefit);
    }


    /**
     * @param publisherLoss the publisherLoss to set
     */
    public void setPublisherLoss(double publisherLoss) {
        if (this.getPublisherLoss() != publisherLoss) {
            this.setModified();
        }
        this.config.getCostBenefitConfiguration().setPublisherLoss(publisherLoss);
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
     * Sets the transformation mode
     * @param attribute
     * @param mode
     */
    public void setTransformationMode(String attribute, ModelTransformationMode mode) {
        if (this.transformationModes == null) {
            this.transformationModes = new HashMap<String, ModelTransformationMode>();
        }
        this.transformationModes.put(attribute, mode);
        setModified();
    }

    /**
     * Sets the config unmodified.
     */
    public void setUnmodified() {
        this.modified = false;
    }

    /**
     * Mark as modified.
     */
    private void setModified() {
        modified = true;
    }
}
