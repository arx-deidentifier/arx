/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.exceptions.ReliabilityException;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * This criterion ensures that an estimate for the average re-identification risk falls
 * below a given threshold.
 * 
 * @author Fabian Prasser
 */
public class AverageReidentificationRisk extends RiskBasedCriterion {

    /** SVUID*/
    private static final long serialVersionUID = -2953252206954936045L;

    /**
     * Creates a new instance of this criterion.
     *  
     * @param riskThreshold
     */
    public AverageReidentificationRisk(double riskThreshold){
        super(true, true, riskThreshold);
    }

    @Override
    public AverageReidentificationRisk clone() {
        return new AverageReidentificationRisk(this.getRiskThreshold());
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
    public boolean isReliableAnonymizationSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Average re-identification risk");
        result.addProperty("Reliable", isReliableAnonymizationSupported());
        result.addProperty("Threshold", this.getRiskThreshold());
        return result;
    }

    @Override
    public String toString() {
        return "(" + getRiskThreshold() + ")-avg-reidentification-risk";
    }

    @Override
    protected boolean isFulfilled(HashGroupifyDistribution distribution) {
        return 1.0d / (double)distribution.getAverageClassSize() <= getRiskThreshold();
    }

    @Override
    protected boolean isReliablyFulfilled(HashGroupifyDistribution distribution) {
        try {
            IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
            return ia.lessThanOrEqual(ia.div(ia.ONE, distribution.getReliableAverageClassSize()), ia.createInterval(getRiskThreshold()));
        } catch (IntervalArithmeticException | ReliabilityException e) {
            return false;
        }
    }
}
