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
import org.deidentifier.arx.reliability.ParameterTranslation;

/**
 * This criterion ensures that an estimate for the average re-identification risk falls
 * below a given threshold. Furthermore a relaxed variant of this model is also implemented,
 * supporting average risk, highest risk and a fraction of records allowed to exceed the highest risk
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
     * Creates a new instance of this model.
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
     * Creates a new instance of a relaxed variant of this model with average risk, 
     * highest risk and the fraction of records with a risk exceeding the highest risk.
     * Note: Due to rounding issues, the highest risk may be exceeded by up to 1%. To see the
     * threshold that is actually being used, invoke <code>getEffectiveHighestRisk()</code>
     *  
     * @param averageRisk
     * @param highestRisk 
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
        this.smallestSize = ParameterTranslation.getSizeThreshold(highestRisk);
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
     * Returns the effective highest risk, i.e. the threshold that is actually being used by the software.
     * @return the highest risk
     */
    public double getEffectiveHighestRisk() {
        return ParameterTranslation.getEffectiveRiskThreshold(highestRisk);
    }
    
    /**
     * Returns the threshold set by the user. Please consider the effective threshold, 
     * i.e. the threshold that is actually being used by the software.
     * 
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
            result.addProperty("Effective highest risk", getEffectiveHighestRisk());
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
            return "("+getRiskThreshold()+", " + getEffectiveHighestRisk() + " (" + highestRisk + "), " + recordsAtRisk + ")-avg-reidentification-risk";
        }
    }

    @Override
    protected boolean isFulfilled(HashGroupifyDistribution distribution) {
        
        // Check average class size
        boolean result = 1.0d / (double)distribution.getAverageClassSize() <= getRiskThreshold();
        if (highestRisk == null || !result) {
            return result;
        }
        
        // Check records at risk
        double fraction = 0d;
        for (int size = 1; size < smallestSize; size++) {
            fraction += distribution.getFractionOfRecordsInClassesOfSize(size);
        }
        return result && (fraction <= recordsAtRisk);
    }
}