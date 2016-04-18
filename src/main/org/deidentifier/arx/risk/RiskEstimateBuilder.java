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

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;

/**
 * A builder for risk estimates
 * 
 * @author Fabian Prasser
 * 
 */
public class RiskEstimateBuilder {

    /** Fields */
    private final ARXPopulationModel     population;
    /** Fields */
    private final DataHandleInternal     handle;
    /** Fields */
    private final Set<String>            identifiers;
    /** Classes */
    private RiskModelHistogram           classes;
    /** Asynchronous computation */
    private final WrappedBoolean         stop;
    /** Model */
    private final ARXSolverConfiguration solverconfig;
    /** Model */
    private final WrappedInteger         progress = new WrappedInteger();
    /** Model */
    private final ARXConfiguration       arxconfig; 
    
    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param classes
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandleInternal handle,
                               RiskModelHistogram classes,
                               ARXConfiguration arxconfig) {
        this(population, handle, null, classes, ARXSolverConfiguration.create(), arxconfig);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param classes
     * @param solverconfig
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandleInternal handle,
                               RiskModelHistogram classes,
                               ARXSolverConfiguration solverconfig,
                               ARXConfiguration arxconfig) {
        this(population, handle, null, classes, solverconfig, arxconfig);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandleInternal handle,
                               Set<String> identifiers,
                               ARXConfiguration arxconfig) {
        this(population,
             handle,
             identifiers,
             (RiskModelHistogram) null,
             ARXSolverConfiguration.create(), arxconfig);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     * @param solverconfig
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandleInternal handle,
                               Set<String> identifiers,
                               ARXSolverConfiguration solverconfig,
                               ARXConfiguration arxconfig) {
        this(population, handle, identifiers, (RiskModelHistogram) null, solverconfig, arxconfig);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     * @param solverconfig
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandleInternal handle,
                                RiskModelHistogram classes,
                                WrappedBoolean stop,
                                ARXSolverConfiguration solverconfig,
                                ARXConfiguration arxconfig) {
        this.population = population;
        this.handle = handle;
        this.identifiers = null;
        this.classes = classes;
        this.solverconfig = solverconfig;
        this.arxconfig = arxconfig;
        synchronized (this) {
            this.stop = stop;
        }
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param qi
     * @param classes
     * @param solverconfig
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandleInternal handle,
                                Set<String> identifiers,
                                RiskModelHistogram classes,
                                ARXSolverConfiguration solverconfig,
                                ARXConfiguration arxconfig) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = classes;
        this.solverconfig = solverconfig;
        this.arxconfig = arxconfig;
        synchronized (this) {
            stop = new WrappedBoolean();
        }
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     * @param solverconfig
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandleInternal handle,
                                Set<String> identifiers,
                                WrappedBoolean stop,
                                ARXSolverConfiguration solverconfig,
                                ARXConfiguration arxconfig) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = null;
        this.solverconfig = solverconfig;
        this.arxconfig = arxconfig;
        synchronized (this) {
            this.stop = stop;
        }
    }

    /**
     * Returns a model of the equivalence classes in this data set
     * 
     * @return
     */
    public RiskModelHistogram getEquivalenceClassModel() {
        return getHistogram(1.0d);
    }

    /**
     * Returns the a set of potential HIPAA identifiers. Values are matched with a
     * confidence threshold of 50%
     * 
     * @return
     */
    public HIPAAIdentifierMatch[] getHIPAAIdentifiers() {
        return new RiskModelHIPAASafeHarbor().getMatches(handle, 0.5d, stop);
    }

    /**
     * Returns the a set of potential HIPAA identifiers. Values are matched with the
     * given confidence threshold.
     * @param threshold Confidence threshold
     * @return
     */
    public HIPAAIdentifierMatch[] getHIPAAIdentifiers(double threshold) {
        return new RiskModelHIPAASafeHarbor().getMatches(handle, threshold, stop);
    }
    /**
     * Returns an interruptible instance of this object.
     * 
     * @return
     */
    public RiskEstimateBuilderInterruptible getInterruptibleInstance() {
        progress.value = 0;
        return new RiskEstimateBuilderInterruptible(this);
    }

    /**
     * Returns a class providing access to population-based risk estimates about
     * the attributes. Uses the decision rule by Dankar et al.
     * 
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks() {
        return getAttributeRisks(PopulationUniquenessModel.DANKAR);
    }

    /**
     * Returns a class providing access to population-based risk estimates about
     * the attributes.
     * 
     * @param model
     *            Uses the given statistical model
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks(PopulationUniquenessModel model) {
        return getAttributeRisks(model);
    }

    /**
     * Returns a class providing population-based uniqueness estimates
     * 
     * @return
     */
    public RiskModelPopulationUniqueness getPopulationBasedUniquenessRisk() {
        progress.value = 0;
        return new RiskModelPopulationUniqueness(population,
                                                 getHistogram(0.25),
                                                 handle.getNumRows(),
                                                 stop,
                                                 progress,
                                                 solverconfig,
                                                 false);
    }

    /**
     * Returns a class providing access to sample-based risk estimates about the
     * attributes
     * 
     * @return
     */
    public RiskModelAttributes getSampleBasedAttributeRisks() {
        return getAttributeRisks(null);
    }

    /**
     * Returns a class providing sample-based re-identification risk estimates
     * 
     * @return
     */
    public RiskModelSampleRisks getSampleBasedReidentificationRisk() {
        progress.value = 0;
        return new RiskModelSampleRisks(getEquivalenceClassModel(), arxconfig);
    }

    /**
     * Returns a class representing the distribution of prosecutor risks in the sample
     * 
     * @return
     */
    public RiskModelSampleRiskDistribution getSampleBasedRiskDistribution() {
        progress.value = 0;
        return new RiskModelSampleRiskDistribution(getEquivalenceClassModel());
    }

    /**
     * Returns a risk summary
     * @param threshold Acceptable highest probability of re-identification for a single record
     * @return
     */
    public RiskModelSampleSummary getSampleBasedRiskSummary(double threshold) {
        progress.value = 0;
        return new RiskModelSampleSummary(handle, identifiers, threshold, stop, progress);
    }
    
    /**
     * Returns a class providing sample-based uniqueness estimates
     * 
     * @return
     */
    public RiskModelSampleUniqueness getSampleBasedUniquenessRisk() {
        progress.value = 0;
        return new RiskModelSampleUniqueness(getEquivalenceClassModel());
    }

    /**
     * Returns a class providing access to population- or sample-based risk
     * estimates about the attributes
     * 
     * @param model
     *            null for sample-based model
     * @return
     */
    private RiskModelAttributes getAttributeRisks(final PopulationUniquenessModel model) {
        progress.value = 0;
        return new RiskModelAttributes(this.identifiers, this.stop, progress) {
            @Override
            protected RiskProvider getRiskProvider(final Set<String> attributes,
                                                   final WrappedBoolean stop) {

                // Compute classes
                RiskEstimateBuilder builder = new RiskEstimateBuilder(population,
                                                                      handle,
                                                                      attributes,
                                                                      stop,
                                                                      solverconfig,
                                                                      arxconfig);
                RiskModelHistogram classes = builder.getEquivalenceClassModel();
                builder = new RiskEstimateBuilder(population, handle, classes, stop, solverconfig, arxconfig);

                // Use classes to compute risks
                final RiskModelSampleRisks reidentificationRisks = builder.getSampleBasedReidentificationRisk();
                final double highestRisk = reidentificationRisks.getHighestRisk();
                final double averageRisk = reidentificationRisks.getAverageRisk();
                final double fractionOfUniqueTuples;
                if (model == null) {
                    fractionOfUniqueTuples = builder.getSampleBasedUniquenessRisk()
                                                    .getFractionOfUniqueTuples();
                } else {
                    fractionOfUniqueTuples = builder.getPopulationBasedUniquenessRisk()
                                                    .getFractionOfUniqueTuples(model);
                }

                // Return a provider
                return new RiskProvider() {
                    public double getAverageRisk() {
                        return averageRisk;
                    }

                    public double getFractionOfUniqueTuples() {
                        return fractionOfUniqueTuples;
                    }

                    public double getHighestRisk() {
                        return highestRisk;
                    }
                };
            }
        };
    }

    /**
     * Returns a model of the equivalence classes in this data set
     * 
     * @return
     */
    private RiskModelHistogram getHistogram(double factor) {
        synchronized (this) {
            if (classes == null) {
                progress.value = 0;
                classes = new RiskModelHistogram(handle, identifiers, stop, progress, factor);
            }
            return classes;
        }
    }

    /**
     * Returns a class providing population-based uniqueness estimates
     * 
     * @return
     */
    protected RiskModelPopulationUniqueness getPopulationBasedUniquenessRiskInterruptible() {
        progress.value = 0;
        return new RiskModelPopulationUniqueness(population,
                                                 getHistogram(0.25),
                                                 handle.getNumRows(),
                                                 stop,
                                                 progress,
                                                 solverconfig,
                                                 true);
    }

    /**
     * Returns progress data, if available
     * 
     * @return
     */
    int getProgress() {
        return this.progress.value;
    }

    /**
     * Interrupts this instance
     */
    void interrupt() {
        synchronized (this) {
            this.stop.value = true;
        }
    }
}
