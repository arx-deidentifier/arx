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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;

/**
 * This criterion ensures that an estimate for the average re-identification risk falls
 * below a given threshold.
 * 
 * @author Fabian Prasser
 */
public class AverageReidentificationRisk extends RiskBasedCriterion{

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
    public String toString() {
        return "(<"+getRiskThreshold()+")-avg-reidentification-risk";
    }

    @Override
    protected boolean isFulfilled(HashGroupifyDistribution distribution) {
        return 1.0d / (double)distribution.getAverageClassSize() <= getRiskThreshold();
    }
}