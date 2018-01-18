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
        EUROPE("Europe", 740000000l),
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
        public long getPopulationSize() {
            return population;
        }
    }

    /** SVUID */
    private static final long serialVersionUID = 6331644478717881214L;

    /**
     * Creates a new instance
     * @param sampleSize
     * @param samplingFraction
     * @return
     */
    public static ARXPopulationModel create(int sampleSize, double samplingFraction){
        return new ARXPopulationModel(sampleSize, samplingFraction);
    }

    /**
     * Creates a new instance
     * @param populationSize
     * @return
     */
    public static ARXPopulationModel create(long populationSize){
        return new ARXPopulationModel(populationSize);
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
    private Region region         = Region.NONE;

    /** TODO: This field is here for backwards compatibility only!*/
    private double sampleFraction = 0.01d;

    /** The sample fraction */
    private Long   populationSize;
    

    /**
     * Creates a new instance
     * @param sampleSize
     * @param samplingFraction
     */
    private ARXPopulationModel(int sampleSize, double samplingFraction) {
        this.region = Region.NONE;
        this.populationSize = (long)(Math.round((double)sampleSize / samplingFraction));
    }

    /**
     * Creates a new instance
     * @param populationSize
     */
    private ARXPopulationModel(long populationSize) {
        this.region = Region.NONE;
        this.populationSize = populationSize;
    }

    /**
     * Creates a new instance
     * @param region
     */
    private ARXPopulationModel(Region region) {
        this.region = region;
        this.populationSize = region.getPopulationSize();
    }

    /**
     * Clone constructor
     * @param sampleFraction
     */
    private ARXPopulationModel(Region region, long populationSize) {
        this.region = region;
        this.populationSize = populationSize;
    }

    /**
     * Returns a clone of this object
     */
    public ARXPopulationModel clone() {
        return new ARXPopulationModel(region, populationSize);
    }
    
    /**
     * Returns the population size
     * @return
     */
    public long getPopulationSize() {
        return populationSize;
    }

    /**
     * @return the region
     */
    public Region getRegion() {
        return region;
    }
    
    @Deprecated
    public void makeBackwardsCompatible(int sampleSize) {
        if (populationSize == null) {
            if (region == null || region == Region.NONE) {
                populationSize = (long) (Math.round((double) sampleSize / this.sampleFraction));
            } else {
                populationSize = region.getPopulationSize();
            }
        }
    }
}
