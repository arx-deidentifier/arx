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
package org.deidentifier.arx.criteria;

import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;

/**
 * This criterion ensures that an estimate for the average re-identification risk falls
 * below a given threshold.
 * 
 * @author Fabian Prasser
 */
public class AverageReidentificationRisk extends RiskBasedCriterion {

    /** SVUID */
    private static final long serialVersionUID = -2953252206954936045L;

    /** Smallest size, derived from highest risk */
    private final Integer     smallestSize;

    /** Highest risk */
    private final Double      highestRisk;

    /** Records with a risk higher than the highest risk */
    private final Double      recordsAtRisk;

    /**
     * Creates a new instance of this criterion.
     *  
     * @param riskThreshold
     */
    public AverageReidentificationRisk(double riskThreshold){
        super(true, true, riskThreshold);
        this.highestRisk = null;
        this.recordsAtRisk = null;
        this.smallestSize = null;
    }
    
    /**
     * Creates a new instance of a relaxed variant of this criterion with average risk, 
     * highest risk and the fraction of records with a risk exceeding the highest risk
     *  
     * @param averageRisk
     * @param highestRisk Please note: due to rounding issues, this risk may be exceeded by up to 1%
     * @param recordsAtRisk
     */
    public AverageReidentificationRisk(double averageRisk, double highestRisk, double recordsAtRisk){
        super(true, true, averageRisk);
        if (highestRisk <= 0d || highestRisk > 1d) {
            throw new IllegalArgumentException("Invalid risk threshold: " + highestRisk);
        }
        if (recordsAtRisk < 0d || highestRisk > 1d) {
            throw new IllegalArgumentException("Invalid fraction: " + recordsAtRisk);
        }
        this.highestRisk = highestRisk;
        this.recordsAtRisk = recordsAtRisk;
        this.smallestSize = getSizeThreshold(highestRisk);
    }

    @Override
    public AverageReidentificationRisk clone() {
        if (this.highestRisk == null) {
            return new AverageReidentificationRisk(this.getRiskThreshold());
        } else {
            return new AverageReidentificationRisk(this.getRiskThreshold(), highestRisk, recordsAtRisk);
        }
    }

    /**
     * @return the average risk
     */
    public double getAverageRisk() {
        return getRiskThreshold();
    }
    
    /**
     * @return the highest risk
     */
    public double getHighestRisk() {
        return highestRisk;
    }
    
    /**
     * @return the records at risk
     */
    public double getRecordsAtRisk() {
        return recordsAtRisk;
    }
    
    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdMarketer() {
        return getRiskThreshold();
    }
    
    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Average re-identification risk");
        result.addProperty("Threshold", this.getRiskThreshold());
        if (highestRisk != null) {
            result.addProperty("Highest risk", highestRisk);    
        }
        if (recordsAtRisk != null) {
            result.addProperty("Records at risk", recordsAtRisk);    
        }
        return result;
    }

    @Override
    public String toString() {
        if (highestRisk == null) {
            return "("+getRiskThreshold()+")-avg-reidentification-risk";
        } else {
            return "("+getRiskThreshold()+", " + highestRisk + ", " + recordsAtRisk + ")-avg-reidentification-risk";
        }
    }

    /**
     * Returns a minimal class size for the given risk threshold
     * TODO: There are similar issues in multiple privacy models, e.g. in the game-theoretic model
     * TODO: This should be fixed once and for all
     * @param threshold
     * @return
     */
    private Integer getSizeThreshold(double riskThreshold) {
        double size = 1d / highestRisk;
        double floor = Math.floor(size);
        if ((1d / floor) - (1d / size) >= 0.01d * highestRisk) {
            floor += 1d;
        }
        return (int)floor;
    }

    @Override
    protected boolean isFulfilled(HashGroupifyDistribution distribution) {
        
        // Check average class size
        boolean result = 1.0d / (double)distribution.getAverageClassSize() <= getRiskThreshold();
        if (highestRisk == null) {
            return result;
        }
        
        // Check records at risk
        double fraction = 0d;
        for (int size = 1; size <= smallestSize; size++) {
            fraction += distribution.getFractionOfRecordsInClassesOfSize(size);
        }
        return result && (fraction <= recordsAtRisk);
    }
}