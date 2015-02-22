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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;

/**
 * A class for population models
 *
 * @author Fabian Prasser
 */
public class ModelPopulation implements Serializable {

    /** SVUID */
    private static final long  serialVersionUID = 5405871228130041796L;
    /** Modified */
    private boolean            modified         = false;
    /** Model */
    private ARXPopulationModel model;
    
    /**
     * Creates a new instance
     */
    public ModelPopulation() {
        this.model = new ARXPopulationModel(0.1d);
    }
    
    /**
     * @param handle
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getPopulationSize(org.deidentifier.arx.DataHandle)
     */
    public double getPopulationSize(DataHandle handle) {
        return model.getPopulationSize(handle);
    }

    /**
     * @param sampleSize
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getPopulationSize(double)
     */
    public double getPopulationSize(double sampleSize) {
        return model.getPopulationSize(sampleSize);
    }

    /**
     * Returns the region
     * @return
     */
    public Region getRegion() {
        return this.model.getRegion();
    }

    /**
     * Returns the sample fraction
     * @param handle
     * @return
     */
    public double getSampleFraction(DataHandle handle) {
        return this.model.getSampleFraction(handle);
    }
    
    /**
     * @param sampleSize
     * @return
     * @see org.deidentifier.arx.ARXPopulationModel#getSampleFraction(double)
     */
    public double getSampleFraction(double sampleSize) {
        return model.getSampleFraction(sampleSize);
    }
    
    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Sets the population size
     * @param handle
     * @param populationSize
     */
    public void setPopulationSize(DataHandle handle, double populationSize) {
        if (populationSize != model.getPopulationSize(handle)) {
            this.model = new ARXPopulationModel(handle, populationSize);
            this.modified = true;
        }
    }

    /**
     * Sets the region
     * @param region
     */
    public void setRegion(Region region) {
        if (region != model.getRegion()) {
            this.model = new ARXPopulationModel(region);
            this.modified = true;
        }
    }
    
    /**
     * Sets the sample fraction
     * @param sampleFraction
     */
    public void setSampleFraction(double sampleFraction) {
        this.model = new ARXPopulationModel(sampleFraction);
        this.modified = true;
    }
    
    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }

    /**
     * Returns the backing model
     * @return
     */
    public ARXPopulationModel getModel() {
        return this.model;
    }
}
