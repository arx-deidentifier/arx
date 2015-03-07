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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;

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
        ATTRIBUTES,
        UNIQUES_DANKAR,
        UNIQUES_ALL,
        OVERVIEW
    }

    /** SVUID */
    private static final long          serialVersionUID          = 5405871228130041796L;
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
    private boolean                    useOutputModelIfAvailable = true;
    /** Model */
    private RiskModelForAttributes     riskModelForAttributes    = RiskModelForAttributes.POPULATION_UNIQUENESS_DANKAR;

    /**
     * Creates a new instance
     */
    public ModelRisk() {
        this.populationModel = new ARXPopulationModel(0.1d);
    }
    
    /**
     * Returns the solver configuration
     */
    public ARXSolverConfiguration getSolverConfiguration() {
        return config;
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
     * @see org.deidentifier.arx.ARXPopulationModel#getPopulationSize(org.deidentifier.arx.DataHandle)
     */
    public double getPopulationSize(DataHandle handle) {
        return populationModel.getPopulationSize(handle);
    }

    /**
     * @param sampleSize
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getPopulationSize(double)
     */
    public double getPopulationSize(double sampleSize) {
        return populationModel.getPopulationSize(sampleSize);
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
     * Returns the sample fraction
     * @param handle
     * @return
     */
    public double getSampleFraction(DataHandle handle) {
        return this.populationModel.getSamplingFraction(handle);
    }

    /**
     * @param sampleSize
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getSamplingFraction(double)
     */
    public double getSampleFraction(double sampleSize) {
        return populationModel.getSamplingFraction(sampleSize);
    }
    
    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified || config.isModified();
    }
    
    /**
     * Use the output or the input model?
     */
    public boolean isUseOutputPopulationModelIfAvailable() {
        return useOutputModelIfAvailable;
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
     * @param handle
     * @param populationSize
     */
    public void setPopulationSize(DataHandle handle, double populationSize) {
        if (populationSize != populationModel.getPopulationSize(handle)) {
            this.populationModel = new ARXPopulationModel(handle, populationSize);
            this.modified = true;
        }
    }

    /**
     * Sets the region
     * @param region
     */
    public void setRegion(Region region) {
        if (region != populationModel.getRegion()) {
            this.populationModel = new ARXPopulationModel(region);
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
     * Sets the sample fraction
     * @param sampleFraction
     */
    public void setSampleFraction(double sampleFraction) {
        this.populationModel = new ARXPopulationModel(sampleFraction);
        this.modified = true;
    }
    
    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
        this.config.setUnmodified();
    }

    /**
     * Use the output or the input model?
     */
    public void setUseOutputPopulationModelIfAvailable(boolean value) {
        this.useOutputModelIfAvailable = value;
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
