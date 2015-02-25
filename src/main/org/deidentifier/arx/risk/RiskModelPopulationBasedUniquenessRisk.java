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
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedInteger;

/**
 * Class for risks based on uniqueness.
 * 
 * @author Fabian Prasser
 */
public class RiskModelPopulationBasedUniquenessRisk extends RiskModelPopulationBased{

    /** 
     * The statistical model used for computing Dankar's estimate.
     * 
     * @author Fabian Prasser
     */
    public static enum StatisticalModel {
        PITMAN,
        ZAYATZ,
        SNB,
        DANKAR,
    }
    
    /** Estimate */
    private final double           numUniquesZayatz;
    /** Estimate */
    private final double           numUniquesSNB;
    /** Estimate */
    private final double           numUniquesPitman;
    /** Estimate */
    private final double           numUniquesDankar;
    /** Model */
    private final StatisticalModel dankarModel;

    /**
     * Creates a new instance
     * @param model
     * @param classes
     * @param sampleSize
     */
    public RiskModelPopulationBasedUniquenessRisk(ARXPopulationModel model, 
                                                  RiskModelEquivalenceClasses classes,
                                                  int sampleSize) {
        this(model,
             classes,
             sampleSize,
             new WrappedBoolean(),
             new WrappedInteger(),
             RiskEstimateBuilder.DEFAULT_ACCURACY,
             RiskEstimateBuilder.DEFAULT_MAX_ITERATIONS);
    }
    
    /**
     * Creates a new instance
     * @param model
     * @param classes
     * @param sampleSize
     * @param stop
     * @param progress
     * @param accuracy
     * @param maxIterations
     */
    RiskModelPopulationBasedUniquenessRisk(ARXPopulationModel model, 
                                           RiskModelEquivalenceClasses classes,
                                           int sampleSize,
                                           WrappedBoolean stop,
                                           WrappedInteger progress,
                                           double accuracy,
                                           int maxIterations) {
        super(classes, model, sampleSize, stop, progress);
        
        // Init
        int numClassesOfSize1 = (int)super.getNumClassesOfSize(1);
        double sampleFraction = super.getSampleFraction();
    
        // Handle cases where there are no sample uniques 
        if (numClassesOfSize1 == 0) {
            numUniquesZayatz = 0d;
            numUniquesSNB = 0d;
            numUniquesPitman = 0d;
            numUniquesDankar = 0d;
            dankarModel = StatisticalModel.DANKAR;
            progress.value = 100;
            return;
        }
        
        // Estimate with Zayatz's model 
        numUniquesZayatz = new ModelZayatz(model, classes, sampleSize, stop).getNumUniques();
        progress.value = 50;
        
        // Estimate with Pitman's model
        numUniquesPitman = new ModelPitman(model, classes, sampleSize, accuracy, maxIterations, stop).getNumUniques();
        progress.value = 75;
        
        // Estimate with SNB model
        numUniquesSNB = new ModelSNB(model, classes, sampleSize, accuracy, maxIterations, stop).getNumUniques();
        progress.value = 100;
        
        // Decision rule by Dankar et al.
        if (sampleFraction <= 0.1) {
            if (isValid(numUniquesPitman)) {
                numUniquesDankar = numUniquesPitman;
                dankarModel = StatisticalModel.PITMAN;
            } else {
                numUniquesDankar = numUniquesZayatz;
                dankarModel = StatisticalModel.ZAYATZ;
            }
        } else if (isValid(numUniquesSNB)) {
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

    /**
     * Returns the statistical model, used by Dankar et al.'s decision rule for estimating population uniqueness
     */
    public StatisticalModel getDankarModel() {
        return dankarModel;
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
        return getNumUniqueTuplesDankar() / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's statistical model
     */
    public double getFractionOfUniqueTuplesPitman() {
        return getNumUniqueTuplesPitman() / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB statistical model
     */
    public double getFractionOfUniqueTuplesSNB() {
        return getNumUniqueTuplesSNB() / super.getPopulationSize();
    }
    /**
     * Estimated number of unique tuples in the population according to Zayatz's statistical model
     */
    public double getFractionOfUniqueTuplesZayatz() {
        return getNumUniqueTuplesZayatz() / super.getPopulationSize();
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
        }
        throw new IllegalArgumentException("Unknown model");
    }
    
    /**
     * Estimated number of unique tuples in the population according to Dankar's decision rule
     */
    public double getNumUniqueTuplesDankar() {
        return isValid(numUniquesDankar) ? numUniquesDankar : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's statistical model
     */
    public double getNumUniqueTuplesPitman() {
        return isValid(numUniquesPitman) ? numUniquesPitman : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB model
     */
    public double getNumUniqueTuplesSNB() {
        return isValid(numUniquesSNB) ? numUniquesSNB : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to Zayatz's statistical model
     */
    public double getNumUniqueTuplesZayatz() {
        return isValid(numUniquesZayatz) ? numUniquesZayatz : 0d;
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
