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
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk.StatisticalPopulationModel;

/**
 * A builder for risk estimates
 * @author Fabian Prasser
 *
 */
public class RiskEstimateBuilder {
    
    /**
     * Helper class
     * @author Fabian Prasser
     *
     */
    static final class ComputationInterruptedException extends RuntimeException {
        private static final long serialVersionUID = -4553285212475615392L;
    }
    
    /**
     * Helper class
     * @author Fabian Prasser
     */
    static final class WrappedBoolean {
        public boolean value = false;
    }

    /**
     * Helper class
     * @author Fabian Prasser
     */
    static final class WrappedInteger {
        public int value = 0;
    }

    /** Convergence threshold for the Newton-Raphson algorithm. */
    public static final double          DEFAULT_ACCURACY       = 1.0e-9;

    /** Maximum number of iterations for the Newton-Raphson algorithm. */
    public static final int             DEFAULT_MAX_ITERATIONS = 300;

    /** Fields */
    private final ARXPopulationModel    population;
    /** Fields */
    private final DataHandle            handle;
    /** Fields */
    private final Set<String>           identifiers;
    /** Classes */
    private RiskModelEquivalenceClasses classes;
    /** Asynchronous computation */
    private final WrappedBoolean        stop;
    /** Model */
    private final int                   maxIterations;
    /** Model */
    private final double                accuracy;
    /** Model */
    private final WrappedInteger        progress         = new WrappedInteger();

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param classes
     */
    public RiskEstimateBuilder(ARXPopulationModel population, DataHandle handle, RiskModelEquivalenceClasses classes) {
        this(population, handle, null, classes, DEFAULT_ACCURACY, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param classes
     * @param accuracy
     * @param maxIterations
     */
    public RiskEstimateBuilder(ARXPopulationModel population, DataHandle handle, RiskModelEquivalenceClasses classes,
                               double accuracy, int maxIterations) {
        this(population, handle, null, classes, accuracy, maxIterations);
    }

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param identifiers
     */
    public RiskEstimateBuilder(ARXPopulationModel population, DataHandle handle, Set<String> identifiers) {
        this(population, handle, identifiers, (RiskModelEquivalenceClasses)null, DEFAULT_ACCURACY, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param identifiers
     * @param accuracy
     * @param maxIterations
     */
    public RiskEstimateBuilder(ARXPopulationModel population, DataHandle handle, Set<String> identifiers,
                               double accuracy, int maxIterations) {
        this(population, handle, identifiers, (RiskModelEquivalenceClasses)null, accuracy, maxIterations);
    }
    
    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param identifiers
     */
    private RiskEstimateBuilder(ARXPopulationModel population, DataHandle handle, RiskModelEquivalenceClasses classes, WrappedBoolean stop,
                                double accuracy, int maxIterations) {
        this.population = population;
        this.handle = handle;
        this.identifiers = null;
        this.classes = classes;
        this.accuracy = accuracy;
        this.maxIterations = maxIterations;
        synchronized(this) {
            this.stop = stop;
        }
    }
    
    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param qi
     * @param classes
     */
    private RiskEstimateBuilder(ARXPopulationModel population, 
                                DataHandle handle, 
                                Set<String> identifiers, 
                                RiskModelEquivalenceClasses classes,
                                double accuracy, int maxIterations) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = classes;
        this.accuracy = accuracy;
        this.maxIterations = maxIterations;
        synchronized(this) {
            stop = new WrappedBoolean();
        }
    }

    /**
     * Creates a new instance
     * @param population
     * @param handle
     * @param identifiers
     * @param accuracy
     * @param maxIterations
     */
    private RiskEstimateBuilder(ARXPopulationModel population,
                                DataHandle handle,
                                Set<String> identifiers,
                                WrappedBoolean stop,
                                double accuracy,
                                int maxIterations) {
        this.population = population;
        this.handle = handle;
        this.identifiers = identifiers;
        this.classes = null;
        this.accuracy = accuracy;
        this.maxIterations = maxIterations;
        synchronized(this) {
            this.stop = stop;
        }
    }
    
    /**
     * Returns a model of the equivalence classes in this data set
     * @return
     */
    public RiskModelEquivalenceClasses getEquivalenceClassModel() {
        return getEquivalenceClassModel(1.0d);
    }

    /**
     * Returns an interruptible instance of this object.
     *
     * @return
     */
    public RiskEstimateBuilderInterruptible getInterruptibleInstance(){
        progress.value = 0;
        return new RiskEstimateBuilderInterruptible(this);
    }
    
    /**
     * Returns a class providing access to population-based risk estimates about the attributes.
     * Uses the decision rule by Dankar et al.
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks() {
       return getAttributeRisks(StatisticalPopulationModel.DANKAR);
    }

    /**
     * Returns a class providing access to population-based risk estimates about the attributes.
     * @param model Uses the given statistical model
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks(StatisticalPopulationModel model) {
       return getAttributeRisks(model);
    }

    /**
     * Returns a class providing population-based uniqueness estimates
     * @return
     */
    public RiskModelPopulationBasedUniquenessRisk getPopulationBasedUniquenessRisk(){
        progress.value = 0;
        return new RiskModelPopulationBasedUniquenessRisk(population, getEquivalenceClassModel(0.25), handle.getNumRows(), stop, progress, accuracy, maxIterations, false);
    }

    /**
     * Returns a class providing access to sample-based risk estimates about the attributes
     * @return
     */
    public RiskModelAttributes getSampleBasedAttributeRisks() {
       return getAttributeRisks(null);
    }
    
    /**
     * Returns a class providing sample-based re-identification risk estimates
     * @return
     */
    public RiskModelSampleBasedReidentificationRisk getSampleBasedReidentificationRisk(){
        progress.value = 0;
        return new RiskModelSampleBasedReidentificationRisk(getEquivalenceClassModel());
    }
    
    /**
     * Returns a class providing sample-based uniqueness estimates
     * @return
     */
    public RiskModelSampleBasedUniquenessRisk getSampleBasedUniquenessRisk(){
        progress.value = 0;
        return new RiskModelSampleBasedUniquenessRisk(getEquivalenceClassModel());
    }

    /**
     * Returns a class providing access to population- or sample-based risk estimates about the attributes
     * @param model null for sample-based model
     * @return
     */
    private RiskModelAttributes getAttributeRisks(final StatisticalPopulationModel model) {
        progress.value = 0;
        return new RiskModelAttributes(this.identifiers, this.stop, progress) {
            @Override
            protected RiskProvider getRiskProvider(final Set<String> attributes, 
                                                   final WrappedBoolean stop) {
                
                // Compute classes
                RiskEstimateBuilder builder = new RiskEstimateBuilder(population, handle,
                                                                      attributes, stop,
                                                                      accuracy, maxIterations);
                RiskModelEquivalenceClasses classes = builder.getEquivalenceClassModel();
                builder = new RiskEstimateBuilder(population, handle, classes, stop,
                                                  accuracy, maxIterations);
                
                
                // Use classes to compute risks
                final RiskModelSampleBasedReidentificationRisk reidentificationRisks = builder.getSampleBasedReidentificationRisk();
                final double highestRisk = reidentificationRisks.getHighestRisk();
                final double averageRisk = reidentificationRisks.getAverageRisk();
                final double fractionOfUniqueTuples;
                if (model == null) {
                    fractionOfUniqueTuples = builder.getSampleBasedUniquenessRisk().getFractionOfUniqueTuples();
                } else {
                    fractionOfUniqueTuples = builder.getPopulationBasedUniquenessRisk().getFractionOfUniqueTuples(model);
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
     * @return
     */
    private RiskModelEquivalenceClasses getEquivalenceClassModel(double factor) {
        synchronized(this) {
            if (classes == null) {
                progress.value = 0;
                classes = new RiskModelEquivalenceClasses(handle, identifiers, stop, progress, factor);
            }
            return classes;
        }
    }

    /**
     * Returns a class providing population-based uniqueness estimates
     * @return
     */
    protected RiskModelPopulationBasedUniquenessRisk getPopulationBasedUniquenessRiskInterruptible(){
        progress.value = 0;
        return new RiskModelPopulationBasedUniquenessRisk(population, getEquivalenceClassModel(0.25), handle.getNumRows(), stop, progress, accuracy, maxIterations, true);
    }
    
    /**
     * Returns progress data, if available
     * @return
     */
    int getProgress() {
        return this.progress.value;
    }
    
    /**
     * Interrupts this instance
     */
    void interrupt() {
        synchronized(this) {
            this.stop.value = true;
        }
    }
}
