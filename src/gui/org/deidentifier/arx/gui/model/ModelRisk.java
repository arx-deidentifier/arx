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
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;

/**
 * A model for risk analysis
 *
 * @author Fabian Prasser
 */
public class ModelRisk implements Serializable {
    
    /**
     * A enum for views
     * @author Fabian Prasser
     *
     */
    public static enum ViewRisk {
        CLASSES_PLOT,
        CLASSES_TABLE,
        ATTRIBUTES,
        UNIQUES_DANKAR,
        UNIQUES_ALL,
        OVERVIEW
    }

    /** SVUID */
    private static final long      serialVersionUID     = 5405871228130041796L;
    /** Modified */
    private boolean                modified             = false;
    /** Model */
    private ARXPopulationModel     populationModel;
    /** Model */
    private int                    maxIterations        = 300;
    /** Model */
    private double                 accuracy             = 1.0e-9;
    /** Model */
    private int                    maxQiSize            = 10;
    /** Model */
    private Map<ViewRisk, Boolean> viewEnabledForInput  = new HashMap<ViewRisk, Boolean>();
    /** Model */
    private Map<ViewRisk, Boolean> viewEnabledForOutput = new HashMap<ViewRisk, Boolean>();

    /**
     * Creates a new instance
     */
    public ModelRisk() {
        this.populationModel = new ARXPopulationModel(0.1d);
    }
    
    /**
     * @return the accuracy
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * @return the maxIterations
     */
    public int getMaxIterations() {
        return maxIterations;
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
     * Returns the sample fraction
     * @param handle
     * @return
     */
    public double getSampleFraction(DataHandle handle) {
        return this.populationModel.getSampleFraction(handle);
    }
    
    /**
     * @param sampleSize
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getSampleFraction(double)
     */
    public double getSampleFraction(double sampleSize) {
        return populationModel.getSampleFraction(sampleSize);
    }
    
    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /***
     * Returns whether a view is enabled
     * @param view
     * @return
     */
    public boolean isViewEnabledForInput(ViewRisk view) {
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
    public boolean isViewEnabledForOutput(ViewRisk view) {
        if (!viewEnabledForOutput.containsKey(view)) {
            return true;
        } else {
            return viewEnabledForOutput.get(view);
        }
    }
    
    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(double accuracy) {
        if (accuracy != this.accuracy) {
            this.modified = true;
        }
        this.accuracy = accuracy;
    }

    /**
     * @param maxIterations the maxIterations to set
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations != this.maxIterations) {
            this.modified = true;
        }
        this.maxIterations = maxIterations;
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
    }
    
    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForInput(ViewRisk view, boolean value) {
        this.viewEnabledForInput.put(view, value);
    }

    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForOutput(ViewRisk view, boolean value) {
        this.viewEnabledForOutput.put(view, value);
    }
}
