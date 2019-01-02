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

package org.deidentifier.arx.risk;

import java.io.Serializable;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * Class for risks based on population uniqueness. It implements Dankar et al.'s
 * decision rule.
 * 
 * @author Fabian Prasser
 */
public class RiskModelPopulationUniqueness extends RiskModelPopulation {

    /**
     * The statistical model used for computing Dankar's estimate.
     * 
     * @author Fabian Prasser
     */
    public static enum PopulationUniquenessModel implements Serializable {
        PITMAN,
        ZAYATZ,
        SNB,
        DANKAR,
    }

    /** Estimate */
    private double                    numUniquesZayatz = -1d;
    /** Estimate */
    private double                    numUniquesSNB    = -1d;
    /** Estimate */
    private double                    numUniquesPitman = -1d;
    /** Estimate */
    private double                    numUniquesDankar = -1d;
    /** Model */
    private PopulationUniquenessModel dankarModel      = null;
    /** Parameter */
    private int                       numClassesOfSize1;
    /** Parameter */
    private double                    samplingFraction;
    /** Parameter */
    private ARXPopulationModel        model;
    /** Parameter */
    private RiskModelHistogram        histogram;
    /** Parameter */
    private ARXSolverConfiguration    config;
    /** Parameter */
    private WrappedBoolean            stop;

    /**
     * Creates a new instance
     * 
     * @param model
     * @param classes
     * @param config
     */
    public RiskModelPopulationUniqueness(ARXPopulationModel model,
                                         RiskModelHistogram classes,
                                         ARXSolverConfiguration config) {
        this(model,
             classes,
             new WrappedBoolean(),
             new WrappedInteger(),
             config,
             false);
    }

    /**
     * Creates a new instance
     * 
     * @param model
     * @param histogram
     * @param stop
     * @param progress
     * @param config
     * @param precompute
     */
    RiskModelPopulationUniqueness(ARXPopulationModel model,
                                  RiskModelHistogram histogram,
                                  WrappedBoolean stop,
                                  WrappedInteger progress,
                                  ARXSolverConfiguration config,
                                  boolean precompute) {
        super(histogram, model, stop, progress);

        // Init
        this.numClassesOfSize1 = (int) super.getNumClassesOfSize(1);
        this.samplingFraction = super.getSamplingFraction();
        this.model = model;
        this.histogram = histogram;
        this.config = config;
        this.stop = stop;

        // Handle cases where there are no sample uniques
        if (numClassesOfSize1 == 0) {
            numUniquesZayatz = 0d;
            numUniquesSNB = 0d;
            numUniquesPitman = 0d;
            numUniquesDankar = 0d;
            dankarModel = PopulationUniquenessModel.DANKAR;
            progress.value = 100;
            return;
        }

        // If precomputation (for interruptible builders)
        if (precompute) {

            // Estimate with Zayatz's model
            getNumUniqueTuplesZayatz();
            progress.value = 50;

            // Estimate with Pitman's model
            getNumUniqueTuplesPitman();
            progress.value = 75;

            // Estimate with SNB model
            getNumUniqueTuplesSNB();

            // Decision rule by Dankar et al.
            getNumUniqueTuplesDankar();
            progress.value = 100;
        }
    }

    /**
     * Estimated number of unique tuples in the population according to the
     * given model
     */
    public double getFractionOfUniqueTuples(PopulationUniquenessModel model) {
        return getNumUniqueTuples(model) / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Dankar's
     * decision rule
     */
    public double getFractionOfUniqueTuplesDankar() {
        return getFractionOfUniqueTuplesDankar(true);
    }

    /**
     * Estimated number of unique tuples in the population according to Dankar's
     * decision rule
     * @param useZayatzAsFallback 
     */
    public double getFractionOfUniqueTuplesDankar(boolean useZayatzAsFallback) {
        return getNumUniqueTuplesDankar(useZayatzAsFallback) / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's
     * statistical model
     */
    public double getFractionOfUniqueTuplesPitman() {
        return getNumUniqueTuplesPitman() / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB
     * statistical model
     */
    public double getFractionOfUniqueTuplesSNB() {
        return getNumUniqueTuplesSNB() / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to Zayatz's
     * statistical model
     */
    public double getFractionOfUniqueTuplesZayatz() {
        return getNumUniqueTuplesZayatz() / super.getPopulationSize();
    }

    /**
     * Estimated number of unique tuples in the population according to the
     * given model
     */
    public double getNumUniqueTuples(PopulationUniquenessModel model) {
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
     * Estimated number of unique tuples in the population according to Dankar's
     * decision rule.
     */
    public double getNumUniqueTuplesDankar() {
        return getNumUniqueTuplesDankar(true);
    }
    
    /**
     * Estimated number of unique tuples in the population according to Dankar's
     * decision rule
     * 
     * @param useZayatzAsFallback
     */
    public double getNumUniqueTuplesDankar(boolean useZayatzAsFallback) {
        if (numUniquesDankar == -1) {
            if (this.numClassesOfSize1 == 0) {
                numUniquesDankar = 0;
                dankarModel = PopulationUniquenessModel.DANKAR;
            } else {
                // Decision rule by Dankar et al.
                if (samplingFraction <= 0.1) {
                    getNumUniqueTuplesPitman();
                    if (isValid(numUniquesPitman)) {
                        numUniquesDankar = numUniquesPitman;
                        dankarModel = PopulationUniquenessModel.PITMAN;
                    } else if (useZayatzAsFallback) {
                        getNumUniqueTuplesZayatz();
                        numUniquesDankar = numUniquesZayatz;
                        dankarModel = PopulationUniquenessModel.ZAYATZ;
                    } 
                } else {
                    getNumUniqueTuplesSNB();
                    getNumUniqueTuplesZayatz();
                    if (isValid(numUniquesSNB)) {
                        if (numUniquesZayatz < numUniquesSNB) {
                            numUniquesDankar = numUniquesZayatz;
                            dankarModel = PopulationUniquenessModel.ZAYATZ;
                        } else {
                            numUniquesDankar = numUniquesSNB;
                            dankarModel = PopulationUniquenessModel.SNB;
                        }
                    } else {
                        numUniquesDankar = numUniquesZayatz;
                        dankarModel = PopulationUniquenessModel.ZAYATZ;
                    }
                }
            }
        }
        return isValid(numUniquesDankar) ? numUniquesDankar : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to Pitman's
     * statistical model
     */
    public double getNumUniqueTuplesPitman() {
        if (numUniquesPitman == -1) {
            if (this.numClassesOfSize1 == 0) {
                numUniquesPitman = 0;
            } else {
                numUniquesPitman = new ModelPitman(model,
                                                   histogram,
                                                   config,
                                                   stop).getNumUniques();
            }
        }
        return isValid(numUniquesPitman) ? numUniquesPitman : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to the SNB
     * model
     */
    public double getNumUniqueTuplesSNB() {
        if (numUniquesSNB == -1) {
            if (this.numClassesOfSize1 == 0) {
                numUniquesSNB = 0;
            } else {
                numUniquesSNB = new ModelSNB(model,
                                             histogram,
                                             config,
                                             stop).getNumUniques();
            }
        }
        return isValid(numUniquesSNB) ? numUniquesSNB : 0d;
    }

    /**
     * Estimated number of unique tuples in the population according to Zayatz's
     * statistical model
     */
    public double getNumUniqueTuplesZayatz() {
        if (numUniquesZayatz == -1) {
            if (this.numClassesOfSize1 == 0) {
                numUniquesZayatz = 0;
            } else {
                numUniquesZayatz = new ModelZayatz(model,
                                                   histogram,
                                                   stop).getNumUniques();
            }
        }
        return isValid(numUniquesZayatz) ? numUniquesZayatz : 0d;
    }

    /**
     * Returns the statistical model, used by Dankar et al.'s decision rule for
     * estimating population uniqueness
     */
    public PopulationUniquenessModel getPopulationUniquenessModel() {
        getNumUniqueTuplesDankar();
        return dankarModel;
    }

    /**
     * Returns whether the according estimate is available
     * 
     * @return
     */
    public boolean isAvailableEstimate(PopulationUniquenessModel model) {
        return getNumUniqueTuples(model) != 0d || numClassesOfSize1 == 0;
    }

    /**
     * Returns whether the according estimate is available
     * 
     * @return
     */
    public boolean isAvailableEstimateDankar() {
        return getNumUniqueTuplesDankar() != 0d || numClassesOfSize1 == 0;
    }

    /**
     * Returns whether the according estimate is available
     * 
     * @return
     */
    public boolean isAvailableEstimatePitman() {
        return getNumUniqueTuplesPitman() != 0d || numClassesOfSize1 == 0;
    }

    /**
     * Returns whether the according estimate is available
     * 
     * @return
     */
    public boolean isAvailableEstimateSNB() {
        return getNumUniqueTuplesSNB() != 0d || numClassesOfSize1 == 0;
    }

    /**
     * Returns whether the according estimate is available
     * 
     * @return
     */
    public boolean isAvailableEstimateZayatz() {
        return getNumUniqueTuplesZayatz() != 0d || numClassesOfSize1 == 0;
    }

    /**
     * Is an estimate valid?
     * 
     * @param value
     * @return
     */
    private boolean isValid(double value) {
        return !Double.isNaN(value) && value != 0d;
    }
}
