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

/**
 * This class models population properties for risk estimation
 * 
 * @author Fabian Prasser
 */
public class ARXPopulationModel implements Serializable {

    /** Regions*/
    public static enum Region implements Serializable{

        NONE("None", 0l),

        AFRICA("Africa", 1100000000l),
        AUSTRALIA("Australia", 23130900l),
        EUROPE("European Union", 740000000l),
        NORTH_AMERICA("North America", 565265000l),
        SOUTH_AMERICA("South America", 385742554l),

        EUROPEAN_UNION("European Union", 507420000l),
        
        BRASIL("Brasil", 202656788l),
        CANADA("Canada", 34834841l),
        CHINA("China (PRC)", 1366040000l),
        FRANCE("France", 65820916l),
        GERMANY("Germany", 80767000l),
        INDIA("India", 1210569573l),
        UK("UK", 63705000l),
        USA("USA", 317238626l);

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

    /** SVUID */
    private static final long serialVersionUID = 6331644478717881214L;

    /**
     * Creates a new instance
     * @param sampleFraction
     * @return
     */
    public static ARXPopulationModel create(double sampleFraction){
        return new ARXPopulationModel(sampleFraction);
    }

    /**
     * Creates a new instance
     * @param region
     * @return
     */
    public static ARXPopulationModel create(Region region){
        return new ARXPopulationModel(region);
    }
    
    /** The region */
    private Region            region           = Region.NONE;
    
    /** The sample fraction */
    private double            sampleFraction   = 0.1;
    

    /**
     * Creates a new instance
     * @param handle
     * @param populationSize
     */
    public ARXPopulationModel(DataHandle handle, double populationSize) {
        this.region = Region.NONE;
        this.sampleFraction = (double)handle.getNumRows() / populationSize;
    }
    
    /**
     * Creates a new instance
     * @param sampleFraction
     */
    public ARXPopulationModel(double sampleFraction) {
        this.region = Region.NONE;
        this.sampleFraction = sampleFraction;
    }

    /**
     * Creates a new instance
     * @param region
     */
    public ARXPopulationModel(Region region) {
        this.region = region;
    }

    /**
     * Returns the population size
     * @param handle
     * @return
     */
    public double getPopulationSize(DataHandle handle) {
        return getPopulationSize(handle.getNumRows());
    }
    
    /**
     * Returns the population size
     * @param sampleSize
     * @return
     */
    public double getPopulationSize(double sampleSize) {
        if (region == Region.NONE) {
            return 1.0d / this.sampleFraction * sampleSize;
        } else {
            return (double)region.getPopulation();
        }
    }

    /**
     * @return the region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Returns the sample fraction
     * @param handle
     * @return
     */
    public double getSampleFraction(DataHandle handle) {
        return getSampleFraction(handle.getNumRows());
    }

    /**
     * Returns the sample fraction
     * @param sampleSize
     * @return the sampleFraction
     */
    public double getSampleFraction(double sampleSize) {
        if (region == Region.NONE) {
            return this.sampleFraction;
        } else {
            return (double)sampleSize / (double)region.getPopulation();
        }
    }
}
