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

import org.deidentifier.arx.DataHandle;

/**
 * This class models population properties for risk estimation
 * 
 * @author Fabian Prasser
 */
public class ModelPopulation implements Serializable {

    /** Regions*/
    public static enum Region implements Serializable{

        // FIXME: Correct and extend list
        NONE("None", 0l),
        // FIXME: Correct and extend list
        WORLD("World", 9000000000l),
        // FIXME: Correct and extend list
        EUROPE("Europe", 400000000l),
        // FIXME: Correct and extend list
        GERMANY("Germany", 80000000l),
        // FIXME: Correct and extend list
        USA("USA", 200000000);
        
        /** Field */
        private final String name;
        /** Field */
        private final long   population;
        
        /**
         * Creates a new instance
         * @param name
         * @param population
         */
        private Region(String name, long population) {
            this.name = name;
            this.population = population;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the population
         */
        public long getPopulation() {
            return population;
        }
    }

    /** SVUID*/
    private static final long serialVersionUID = 6331644478717881214L;
    
    /** The region*/
    private Region region = Region.NONE;
    
    /** The sample fraction*/
    private double sampleFraction = 0.1;
    
    /** Modified */
    private boolean modified       = false;

    /**
     * @return the region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * @return the sampleFraction
     */
    public double getSampleFraction() {
        return sampleFraction;
    }

    /**
     * @param region the region to set
     * @param handle the input dataset
     */
    public void setRegion(Region region, DataHandle handle) {
        this.region = region;
        if (region == Region.NONE) {
            this.sampleFraction = 0.1d; // Default
        } else {
            this.sampleFraction = (double)handle.getNumRows() / (double)region.getPopulation();
        }
        this.modified = true;
    }

    /**
     * @param sampleFraction the sampleFraction to set
     */
    public void setSampleFraction(double sampleFraction) {
        this.sampleFraction = sampleFraction;
        this.region = Region.NONE;
        this.modified = true;
    }
    
    /**
     * Is it modified
     * @return
     */
    protected boolean isModified(){
        return modified;
    }
    
    /**
     * Marks the model as unmodified
     */
    protected void setUnmodified(){
        this.modified = false;
    }
}
