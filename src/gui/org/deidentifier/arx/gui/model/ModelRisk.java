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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.ARXSolverConfiguration;

/**
 * A model for risk analysis
 *
 * @author Fabian Prasser
 */
public class ModelRisk implements Serializable {
    
    /**
     * A enum for statistical models underlying attribute analyses
     * @author Fabian Prasser
     */
    public static enum RiskModelForAttributes {
        SAMPLE_UNIQUENESS,
        POPULATION_UNIQUENESS_PITMAN,
        POPULATION_UNIQUENESS_ZAYATZ,
        POPULATION_UNIQUENESS_DANKAR,
        POPULATION_UNIQUENESS_SNB
    }
    
    /**
     * A enum for views
     * @author Fabian Prasser
     */
    public static enum ViewRiskType {
        CLASSES_PLOT,
        CLASSES_TABLE,
        CELL_BASED,
        ATTRIBUTES,
        HIPAA_ATTRIBUTES,
        UNIQUES_DANKAR,
        UNIQUES_ALL,
        OVERVIEW
    }

    /** SVUID */
    private static final long          serialVersionUID          = 5405871228130041796L;
    /** The default sample size */
    private static final Region        DEFAULT_REGION            = Region.USA;
    /** Modified */
    private boolean                    modified                  = false;
    /** Model */
    private ARXPopulationModel         populationModel           = null;
    /** Model */
    private ARXSolverConfiguration     config                    = ARXSolverConfiguration.create();
    /** Model */
    private int                        maxQiSize                 = 10;
    /** Model */
    private Map<ViewRiskType, Boolean> viewEnabledForInput       = new HashMap<ViewRiskType, Boolean>();
    /** Model */
    private Map<ViewRiskType, Boolean> viewEnabledForOutput      = new HashMap<ViewRiskType, Boolean>();
    /** Model */
    private RiskModelForAttributes     riskModelForAttributes    = RiskModelForAttributes.POPULATION_UNIQUENESS_DANKAR;
    /** Model */
    private Double                     riskThresholdRecordsAtRisk;
    /** Model */
    private Double                     riskThresholdHighestRisk;
    /** Model */
    private Double                     riskThresholdSuccessRate;

    /**
     * Creates a new instance
     */
    public ModelRisk() {
        this.populationModel = ARXPopulationModel.create(DEFAULT_REGION);
    }

    /**
     * @return the maxQiSize
     */
    public int getMaxQiSize() {
        return maxQiSize;
    }

    /**
     * Returns the backing model
     * @return
     */
    public ARXPopulationModel getPopulationModel() {
        return this.populationModel;
    }
    
    /**
     * @param handle
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getPopulationSize()
     */
    public double getPopulationSize() {
        return populationModel.getPopulationSize();
    }

    /**
     * Returns the region
     * @return
     */
    public Region getRegion() {
        return this.populationModel.getRegion();
    }

    /**
     * Returns the risk model used for attribute analyses
     * @return
     */
    public RiskModelForAttributes getRiskModelForAttributes() {
        return this.riskModelForAttributes;
    }
    
    /**
     * Returns a threshold
     * @return
     */
    public double getRiskThresholdHighestRisk() {
        if (riskThresholdHighestRisk == null) {
            riskThresholdHighestRisk = 0.2d;
        }
        return riskThresholdHighestRisk;
    }
    
    /**
     * Returns a threshold
     * @return
     */
    public double getRiskThresholdRecordsAtRisk() {
        if (riskThresholdRecordsAtRisk == null) {
            riskThresholdRecordsAtRisk = 0.05d;
        }
        return riskThresholdRecordsAtRisk;
    }

    /**
     * Returns a threshold
     * @return
     */
    public double getRiskThresholdSuccessRate() {
        if (riskThresholdSuccessRate == null) {
            riskThresholdSuccessRate = 0.05d;
        }
        return riskThresholdSuccessRate;
    }

    /**
     * Returns the solver configuration
     */
    public ARXSolverConfiguration getSolverConfiguration() {
        return config;
    }
    
    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified || config.isModified();
    }

    /***
     * Returns whether a view is enabled
     * @param view
     * @return
     */
    public boolean isViewEnabledForInput(ViewRiskType view) {
        if (!viewEnabledForInput.containsKey(view)) {
            return true;
        } else {
            return viewEnabledForInput.get(view);
        }
    }
    
    /***
     * Returns whether a view is enabled
     * @param view
     * @return
     */
    public boolean isViewEnabledForOutput(ViewRiskType view) {
        if (!viewEnabledForOutput.containsKey(view)) {
            return true;
        } else {
            return viewEnabledForOutput.get(view);
        }
    }
    
    /**
     * @param maxQiSize the maxQiSize to set
     */
    public void setMaxQiSize(int maxQiSize) {
        if (maxQiSize != this.maxQiSize) {
            this.modified = true;
        }
        this.maxQiSize = maxQiSize;
    }

    /**
     * Sets the population size
     * @param populationSize
     */
    public void setPopulationSize(long populationSize) {
        if (populationSize != populationModel.getPopulationSize()) {
            this.populationModel = ARXPopulationModel.create(populationSize);
            this.modified = true;
        }
    }

    /**
     * Sets the region
     * @param region
     */
    public void setRegion(Region region) {
        if (region != populationModel.getRegion()) {
            this.populationModel = ARXPopulationModel.create(region);
            this.modified = true;
        }
    }

    /**
     * Sets the risk model used for attribute analyses
     * @param model
     */
    public void setRiskModelForAttributes(RiskModelForAttributes model) {
        this.riskModelForAttributes = model;
    }

    /**
     * Sets a threshold
     * @param threshold
     */
    public void setRiskThresholdHighestRisk(double threshold) {
        if (this.riskThresholdHighestRisk == null ||
            this.riskThresholdHighestRisk.doubleValue() != threshold) {
            this.modified = true;
        }
        this.riskThresholdHighestRisk = threshold;
    }

    /**
     * Sets a threshold
     * @param threshold
     */
    public void setRiskThresholdRecordsAtRisk(double threshold) {
        if (this.riskThresholdRecordsAtRisk == null ||
            this.riskThresholdRecordsAtRisk.doubleValue() != threshold) {
            this.modified = true;
        }
        this.riskThresholdRecordsAtRisk = threshold;
    }

    /**
     * Sets a threshold
     * @param threshold
     */
    public void setRiskThresholdSuccessRate(double threshold) {
        if (this.riskThresholdSuccessRate == null ||
            this.riskThresholdSuccessRate.doubleValue() != threshold) {
            this.modified = true;
        }
        this.riskThresholdSuccessRate = threshold;
    }

    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
        this.config.setUnmodified();
    }

    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForInput(ViewRiskType view, boolean value) {
        this.viewEnabledForInput.put(view, value);
    }

    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForOutput(ViewRiskType view, boolean value) {
        this.viewEnabledForOutput.put(view, value);
    }
}
