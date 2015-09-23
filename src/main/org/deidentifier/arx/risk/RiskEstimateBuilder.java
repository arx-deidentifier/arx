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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;

/**
 * A builder for risk estimates
 * 
 * @author Fabian Prasser
 * 
 */
public class RiskEstimateBuilder {

    /**
     * Helper class
     * 
     * @author Fabian Prasser
     * 
     */
    static final class ComputationInterruptedException extends RuntimeException {
        private static final long serialVersionUID = -4553285212475615392L;
    }

    /**
     * Helper class
     * 
     * @author Fabian Prasser
     */
    static final class WrappedBoolean {
        public boolean value = false;
    }

    /**
     * Helper class
     * 
     * @author Fabian Prasser
     */
    static final class WrappedInteger {
        public int value = 0;
    }

    /** Fields */
    private final ARXPopulationModel     population;
    /** Fields */
    private final DataHandle             handle;
    /** Fields */
    private final Set<String>            identifiers;
    /** Classes */
    private RiskModelHistogram           classes;
    /** Asynchronous computation */
    private final WrappedBoolean         stop;
    /** Model */
    private final ARXSolverConfiguration config;
    /** Model */
    private final WrappedInteger         progress = new WrappedInteger();

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param classes
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandle handle,
                               RiskModelHistogram classes) {
        this(population, handle, null, classes, ARXSolverConfiguration.create());
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param classes
     * @param config
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandle handle,
                               RiskModelHistogram classes,
                               ARXSolverConfiguration config) {
        this(population, handle, null, classes, config);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandle handle,
                               Set<String> identifiers) {
        this(population,
             handle,
             identifiers,
             (RiskModelHistogram) null,
             ARXSolverConfiguration.create());
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     * @param config
     */
    public RiskEstimateBuilder(ARXPopulationModel population,
                               DataHandle handle,
                               Set<String> identifiers,
                               ARXSolverConfiguration config) {
        this(population, handle, identifiers, (RiskModelHistogram) null, config);
    }

    /**
     * Creates a new instance
     * 
     * @param population
     * @param handle
     * @param identifiers
     * @param config
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandle handle,
                                RiskModelHistogram classes,
                                WrappedBoolean stop,
                                ARXSolverConfiguration config) {
        this.population = population;
        this.handle = handle;
        this.identifiers = null;
        this.classes = classes;
        this.config = config;
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
     * @param config
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandle handle,
                                Set<String> identifiers,
                                RiskModelHistogram classes,
                                ARXSolverConfiguration config) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = classes;
        this.config = config;
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
     * @param config
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandle handle,
                                Set<String> identifiers,
                                WrappedBoolean stop,
                                ARXSolverConfiguration config) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = null;
        this.config = config;
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
     * Returns the identified HIPAA identifiers.
     * 
     * @return
     */
    public HIPAAIdentifierMatch[] getHIPAAIdentifiers() {
        return RiskModelHIPAASafeHarbor.validate(handle, stop);
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
                                                 config,
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
        return new RiskModelSampleRisks(getEquivalenceClassModel());
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
                                                                      config);
                RiskModelHistogram classes = builder.getEquivalenceClassModel();
                builder = new RiskEstimateBuilder(population, handle, classes, stop, config);

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
                                                 config,
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
