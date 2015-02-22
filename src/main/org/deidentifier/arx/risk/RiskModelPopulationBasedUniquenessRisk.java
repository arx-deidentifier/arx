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

package org.deidentifier.arx.risk;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

/**
 * Class for risks based on uniqueness
 * @author Fabian Prasser
 */
public class RiskModelPopulationBasedUniquenessRisk extends RiskModelPopulationBased{

    /** 
     * The statistical model used for computing Dankar's estimate
     * @author Fabian Prasser
     */
    public static enum StatisticalModel {
        PITMAN,
        ZAYATZ,
        SNB,
        DANKAR,
        DANKAR_WITHOUT_SNB
    }
    
    /** Convergence threshold for the Newton-Raphson algorithm. */
    public static final double     DEFAULT_ACCURACY       = 1.0e-9;

    /** Maximum number of iterations for the Newton-Raphson algorithm. */
    public static final int        DEFAULT_MAX_ITERATIONS = 300;

    /** Estimate */
    private final double           numUniquesZayatz;
    /** Estimate */
    private final double           numUniquesSNB;
    /** Estimate */
    private final double           numUniquesPitman;
    /** Estimate */
    private final double           numUniquesDankar;
    /** Estimate */
    private final double           numUniquesDankarWithoutSNB;
    /** Model */
    private final StatisticalModel dankarModel;
    /** Model */
    private final StatisticalModel dankarModelWithoutSNB;

    /**
     * Creates a new instance
     * @param model
     * @param classes
     */
    public RiskModelPopulationBasedUniquenessRisk(ARXPopulationModel model, 
                                                  RiskModelEquivalenceClasses classes) {
        this(model, classes, new WrappedBoolean());
    }
    
    /**
     * Creates a new instance
     * @param model
     * @param classes
     */
    RiskModelPopulationBasedUniquenessRisk(ARXPopulationModel model, 
                                           RiskModelEquivalenceClasses classes,
                                           WrappedBoolean stop) {
        super(classes, model, stop);
        
        // Init
        int numClassesOfSize1 = (int)super.getNumClassesOfSize(1);
        int numClassesOfSize2 = (int)super.getNumClassesOfSize(2);
        double sampleFraction = super.getSampleFraction();
    
        // Handle where there are not sample uniques 
        if (numClassesOfSize1 == 0) {
            numUniquesZayatz = 0d;
            numUniquesSNB = 0d;
            numUniquesPitman = 0d;
            numUniquesDankar = 0d;
            numUniquesDankarWithoutSNB = 0d;
            dankarModelWithoutSNB = null;
            dankarModel = null;
            return;
        }
        
        // Estimate with Zayatz's model 
        numUniquesZayatz = new ModelZayatz(model, classes, stop).getNumUniques();
        
        // Estimate with Pitman's model
        numUniquesPitman = numClassesOfSize2 != 0 ? new ModelPitman(model, classes, DEFAULT_ACCURACY, DEFAULT_MAX_ITERATIONS, stop).getNumUniques() : 0d;
        
        // Estimate with SNB model
        numUniquesSNB = new ModelSNB(model, classes, DEFAULT_ACCURACY, DEFAULT_MAX_ITERATIONS, stop).getNumUniques();
        
        // Estimate with Dankar's model. TODO: Check against the paper
        if (numClassesOfSize2 == 0) {
            numUniquesDankar = numUniquesZayatz;
            dankarModel = StatisticalModel.ZAYATZ;
        } else if (sampleFraction <= 0.1) {
            if (isValid(numUniquesPitman)) {
                numUniquesDankar = numUniquesPitman;
                dankarModel = StatisticalModel.PITMAN;
            } else {
                numUniquesDankar = numUniquesZayatz;
                dankarModel = StatisticalModel.ZAYATZ;
            }
        } else {
            if (!isValid(numUniquesZayatz)) {
                if (!isValid(numUniquesSNB)) {
                    numUniquesDankar = numUniquesPitman;
                    dankarModel = StatisticalModel.PITMAN;
                } else {
                    numUniquesDankar = numUniquesSNB;
                    dankarModel = StatisticalModel.SNB;
                }
            } else {
                if (isValid(numUniquesSNB)) {
                    if (numUniquesZayatz < numUniquesSNB) {
                        numUniquesDankar = numUniquesZayatz;
                        dankarModel = StatisticalModel.ZAYATZ;
                    } else {
                        numUniquesDankar = numUniquesSNB;
                        dankarModel = StatisticalModel.SNB;
                    }
                } else {
                    numUniquesDankar = numUniquesZayatz;
                    dankarModel = StatisticalModel.ZAYATZ;
                }
            }
        } 

        // Estimate with Dankar's model, ignoring the SNB model. TODO: Check against the paper
        if (numClassesOfSize2 == 0) {
            numUniquesDankarWithoutSNB = numUniquesZayatz;
            dankarModelWithoutSNB = StatisticalModel.ZAYATZ;
        } else if (sampleFraction <= 0.1) {
            if (isValid(numUniquesPitman)) {
                numUniquesDankarWithoutSNB = numUniquesPitman;
                dankarModelWithoutSNB = StatisticalModel.PITMAN;
            } else {
                numUniquesDankarWithoutSNB = numUniquesZayatz;
                dankarModelWithoutSNB = StatisticalModel.ZAYATZ;
            }
        } else {
            if (!isValid(numUniquesZayatz)) {
                numUniquesDankarWithoutSNB = numUniquesPitman; 
                dankarModelWithoutSNB = StatisticalModel.PITMAN;
            } else {
                numUniquesDankarWithoutSNB = numUniquesZayatz;
                dankarModelWithoutSNB = StatisticalModel.ZAYATZ;
            }
        } 
    }

    /**
     * Returns the statistical model, used by Dankar et al.'s decision rule for estimating population uniqueness
     */
    public StatisticalModel getDankarModel() {
        return dankarModel;
    }

    /**
     * Returns the statistical model, used by Dankar et al.'s decision rule for estimating population uniqueness.
     * Excludes the SNB model.
     */
    public StatisticalModel getDankarModelWithoutSNB() {
        return dankarModelWithoutSNB;
    }

    /**
     * Estimated number of unique tuples in the population according to the given model
     */
    public double getFractionOfUniqueTuples(StatisticalModel model) {
        return getNumUniqueTuples(model) / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Dankar's decision rule
     */
    public double getFractionOfUniqueTuplesDankar() {
        return numUniquesDankar / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Dankar's decision rule, excluding the SNB model
     */
    public double getFractionOfUniqueTuplesDankarWithoutSNB() {
        return numUniquesDankarWithoutSNB / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's statistical model
     */
    public double getFractionOfUniqueTuplesPitman() {
        return numUniquesPitman / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB statistical model
     */
    public double getFractionOfUniqueTuplesSNB() {
        return numUniquesSNB / super.getPopulationSize();
    }
    /**
     * Estimated number of unique tuples in the population according to Zayatz's statistical model
     */
    public double getFractionOfUniqueTuplesZayatz() {
        return numUniquesZayatz / super.getPopulationSize();
    }
    
    /**
     * Estimated number of unique tuples in the population according to the given model
     */
    public double getNumUniqueTuples(StatisticalModel model) {
        switch (model) {
        case ZAYATZ:
            return getNumUniqueTuplesZayatz();
        case PITMAN:
            return getNumUniqueTuplesPitman();
        case SNB:
            return getNumUniqueTuplesSNB();
        case DANKAR:
            return getNumUniqueTuplesDankar();
        case DANKAR_WITHOUT_SNB:
            return getNumUniqueTuplesDankarWithoutSNB();
        }
        throw new IllegalArgumentException("Unknown model");
    }
    
    /**
     * Estimated number of unique tuples in the population according to Dankar's decision rule
     */
    public double getNumUniqueTuplesDankar() {
        return numUniquesDankar;
    }

    /**
     * Estimated number of unique tuples in the population according to Dankar et al.'s decision rule, excluding the SNB model
     */
    public double getNumUniqueTuplesDankarWithoutSNB() {
        return numUniquesDankarWithoutSNB;
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's statistical model
     */
    public double getNumUniqueTuplesPitman() {
        return numUniquesPitman;
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB model
     */
    public double getNumUniqueTuplesSNB() {
        return numUniquesSNB;
    }

    /**
     * Estimated number of unique tuples in the population according to Zayatz's statistical model
     */
    public double getNumUniqueTuplesZayatz() {
        return numUniquesZayatz;
    }

    /**
     * Is an estimate valid?
     * @param value
     * @return
     */
    private boolean isValid(double value) {
        return !Double.isNaN(value) && value != 0d;
    }
}
