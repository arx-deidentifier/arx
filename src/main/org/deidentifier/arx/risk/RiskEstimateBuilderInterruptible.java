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

import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * A builder for risk estimates, interruptible
 * 
 * @author Fabian Prasser
 * @author Maximilian Zitzmann
 */
public class RiskEstimateBuilderInterruptible {
    
    /** The wrapped instance */
    private final RiskEstimateBuilder parent;
    
    /**
     * Creates a new instance
     * 
     * @param parent
     */
    RiskEstimateBuilderInterruptible(RiskEstimateBuilder parent) {
        this.parent = parent;
    }

    /**
     * Returns a class providing access to an analysis of potential quasi-identifiers using
     * the concepts of alpha distinction and alpha separation.
     *
     * @return the RiskModelAttributes data from risk analysis
     */
    public RiskModelAttributes getAttributeRisks() throws InterruptedException {
        try {
            return parent.getAttributeRisks();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the equivalence classes in this data set
     * 
     * @return
     * @throws InterruptedException
     */
    public RiskModelHistogram getEquivalenceClassModel() throws InterruptedException {
        try {
            return parent.getEquivalenceClassModel();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    /**
     * Returns a class providing access to the identifier HIPAA identifiers.
     * 
     * @return
     * @throws InterruptedException
     */
    public HIPAAIdentifierMatch[] getHIPAAIdentifiers() throws InterruptedException {
        try {
            return parent.getHIPAAIdentifiers();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a model of the MSUs in this data set
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUKeyStatistics getMSUKeyStatistics() throws InterruptedException {
        try {
            return parent.getMSUKeyStatistics();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the MSUs in this data set
     * @param maxK The maximal size of an MSU considered
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUKeyStatistics getMSUKeyStatistics(int maxK) throws InterruptedException {
        try {
            return parent.getMSUKeyStatistics(maxK);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the MSUs in this data set
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUColumnStatistics getMSUColumnStatistics() throws InterruptedException {
        try {
            return parent.getMSUColumnStatistics();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the MSUs in this data set
     * @param maxK The maximal size of an MSU considered
     * @param sdcMicroScores Mimic sdcMicro or use definition by Elliot
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUColumnStatistics getMSUColumnStatistics(int maxK, boolean sdcMicroScores) throws InterruptedException {
        try {
            return parent.getMSUColumnStatistics(maxK, sdcMicroScores);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the MSUs in this data set
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUScoreStatistics getMSUScoreStatistics() throws InterruptedException {
        try {
            return parent.getMSUScoreStatistics();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a model of the MSUs in this data set
     * @param maxK The maximal size of an MSU considered
     * @param sdcMicroScores Mimic sdcMicro or use definition by Elliot
     * @return
     * @throws InterruptedException 
     */
    public RiskModelMSUScoreStatistics getMSUScoreStatistics(int maxK, boolean sdcMicroScores) throws InterruptedException {
        try {
            return parent.getMSUScoreStatistics(maxK, sdcMicroScores);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    /**
     * Returns a class providing population-based uniqueness estimates
     * 
     * @return
     */
    public RiskModelPopulationUniqueness
           getPopulationBasedUniquenessRisk() throws InterruptedException {
        try {
            return parent.getPopulationBasedUniquenessRiskInterruptible();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * If supported by the according builder, this method will report a progress
     * value in [0,100]. Otherwise, it will always return 0
     * 
     * @return
     */
    public int getProgress() {
        return parent.getProgress();
    }
    
    /**
     * Returns a class providing sample-based re-identification risk estimates
     * 
     * @return
     */
    public RiskModelSampleRisks getSampleBasedReidentificationRisk() throws InterruptedException {
        try {
            return parent.getSampleBasedReidentificationRisk();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a class representing the distribution of prosecutor risks in the sample
     * 
     * @return
     */
    public RiskModelSampleRiskDistribution getSampleBasedRiskDistribution() throws InterruptedException {
        try {
            return parent.getSampleBasedRiskDistribution();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a risk summary
     * @param threshold Acceptable highest probability of re-identification for a single record. Please note that this
     *                  threshold may be exceeded by up to 1% due to rounding issues.
     * @return
     * @throws InterruptedException 
     */
    public RiskModelSampleSummary getSampleBasedRiskSummary(double threshold) throws InterruptedException {
        try {
            return parent.getSampleBasedRiskSummary(threshold);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a risk summary
     *
     * @param threshold Acceptable highest probability of re-identification for a single record. Please note that this
     *                  threshold may be exceeded by up to 1% due to rounding issues.
     * @param suppressed
     * @return
     * @throws InterruptedException 
     */
    public RiskModelSampleSummary getSampleBasedRiskSummary(double threshold, String suppressed) throws InterruptedException {
        try {
            return parent.getSampleBasedRiskSummary(threshold, suppressed);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a risk summary, using wildcard matching. "*" will be interpreted as a wildcard
     *
     * @param threshold Acceptable highest probability of re-identification for a single record. Please note that this
     *                  threshold may be exceeded by up to 1% due to rounding issues.
     * @return
     * @throws InterruptedException 
     */
    public RiskModelSampleWildcard getSampleBasedRiskSummaryWildcard(double threshold) throws InterruptedException {
        try {
            return parent.getSampleBasedRiskSummaryWildcard(threshold);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a risk summary, using wildcard matching
     *
     * @param threshold Acceptable highest probability of re-identification for a single record. Please note that this
     *                  threshold may be exceeded by up to 1% due to rounding issues.
     * @param wildcard String to interpret as a wildcard
     * @return
     * @throws InterruptedException 
     */
    public RiskModelSampleWildcard getSampleBasedRiskSummaryWildcard(double threshold, String wildcard) throws InterruptedException {
        try {
            return parent.getSampleBasedRiskSummaryWildcard(threshold, wildcard);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a class providing sample-based uniqueness estimates
     * 
     * @return
     */
    public RiskModelSampleUniqueness getSampleBasedUniquenessRisk() throws InterruptedException {
        try {
            return parent.getSampleBasedUniquenessRisk();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Interrupts all computations. Raises an InterruptedException.
     */
    public void interrupt() {
        parent.interrupt();
    }
}
