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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk.StatisticalPopulationModel;

/**
 * This criterion ensures that the fraction of population uniques falls below a given threshold.
 * 
 * @author Fabian Prasser
 */
public class RiskBasedThresholdPopulationUniques extends RiskBasedPrivacyCriterion{

    /** SVUID */
    private static final long   serialVersionUID       = 618039085843721351L;

    /** Constant */
    private static final int    DEFAULT_MAX_ITERATIONS = 1000;
    
    /** Constant */
    private static final double DEFAULT_ACCURACY       = 10e-6;

    /** The statistical model */
    private StatisticalPopulationModel    statisticalModel;

    /** The population model */
    private ARXPopulationModel  populationModel;

    /**
     * Creates a new instance of this criterion. Uses Dankar's method for estimating population uniqueness.
     * This constructor will clone the population model, making further changes to it will not influence
     * the results. The default accuracy is 10e-6 and the default maximal number of iterations is 1000.
     *  
     * @param riskThreshold
     * @param populationModel
     */
    public RiskBasedThresholdPopulationUniques(double riskThreshold, ARXPopulationModel populationModel){
        this(riskThreshold, StatisticalPopulationModel.DANKAR, populationModel);
    }

    /**
     * Creates a new instance of this criterion. Uses Dankar's method for estimating population uniqueness.
     * This constructor will clone the population model, making further changes to it will not influence
     * the results. The default accuracy is 10e-6 and the default maximal number of iterations is 1000.
     *  
     * @param riskThreshold
     * @param populationModel
     * @param accuracy
     * @param maxIterations
     */
    public RiskBasedThresholdPopulationUniques(double riskThreshold,
                                               ARXPopulationModel populationModel,
                                               double accuracy,
                                               int maxIterations) {
        this(riskThreshold, StatisticalPopulationModel.DANKAR, populationModel, accuracy, maxIterations);
    }

    /**
     * Creates a new instance of this criterion. Uses the specified method for estimating population uniqueness.
     * This constructor will clone the population model, making further changes to it will not influence
     * the results. The default accuracy is 10e-6 and the default maximal number of iterations is 1000.
     * 
     * @param riskThreshold
     * @param statisticalModel
     * @param populationModel
     */
    public RiskBasedThresholdPopulationUniques(double riskThreshold,
                                               StatisticalPopulationModel statisticalModel, 
                                               ARXPopulationModel populationModel){
        this(riskThreshold, statisticalModel, populationModel, DEFAULT_ACCURACY, DEFAULT_MAX_ITERATIONS);
    }
    /**
     * Creates a new instance of this criterion. Uses the specified method for estimating population uniqueness.
     * This constructor will clone the population model, making further changes to it will not influence
     * the results. The default accuracy is 10e-6 and the default maximal number of iterations is 1000.
     * 
     * @param riskThreshold
     * @param statisticalModel
     * @param populationModel
     * @param accuracy
     * @param maxIterations
     */
    public RiskBasedThresholdPopulationUniques(double riskThreshold,
                                               StatisticalPopulationModel statisticalModel, 
                                               ARXPopulationModel populationModel,
                                               double accuracy,
                                               int maxIterations){
        super(false, riskThreshold);
        this.statisticalModel = statisticalModel;
        this.populationModel = populationModel.clone();
    }

    /**
     * @return the populationModel
     */
    public ARXPopulationModel getPopulationModel() {
        return populationModel;
    }

    /**
     * @return the statisticalModel
     */
    public StatisticalPopulationModel getStatisticalModel() {
        return statisticalModel;
    }

    @Override
    public String toString() {
        return "(<" + getRiskThreshold() + "-population-uniques (" + statisticalModel.toString().toLowerCase() + ")";
    }

    /**
     * We currently assume that at any time, at least one statistical model converges.
     * This might not be the case, and 0 may be returned instead. That's why we only
     * accept estimates of 0, if the number of equivalence classes of size 1 in the sample is also zero
     * 
     * @param distribution
     * @return
     */
    protected boolean isFulfilled(HashGroupifyDistribution distribution) {

        RiskModelPopulationBasedUniquenessRisk riskModel = new RiskModelPopulationBasedUniquenessRisk(this.populationModel, 
                                                                                                      distribution.getEquivalenceClasses(), 
                                                                                                      distribution.getNumberOfTuples());
        
        double populationUniques = riskModel.getFractionOfUniqueTuples(this.statisticalModel);
        if (populationUniques > 0d && populationUniques < getRiskThreshold()) {
            return true;
        } else if (populationUniques == 0d && distribution.getFractionOfTuplesInClassesOfSize(1) == 0d) {
            return true;
        } else {
            return false;
        }
    }
}