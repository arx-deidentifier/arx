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

import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk.StatisticalModel;

/**
 * A builder for risk estimates, interruptible
 * @author Fabian Prasser
 *
 */
public class RiskEstimateBuilderInterruptible {
    
    /** The wrapped instance*/
    private final RiskEstimateBuilder parent;
    
    /**
     * Creates a new instance
     * @param builder
     */
    RiskEstimateBuilderInterruptible(RiskEstimateBuilder parent) {
        this.parent = parent;
    }
    
    /**
     * Returns a model of the equivalence classes in this data set
     * @return
     * @throws InterruptedException 
     */
    public RiskModelEquivalenceClasses getEquivalenceClassModel() throws InterruptedException {
        try {
            return parent.getEquivalenceClassModel();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a class providing access to population-based risk estimates about the attributes.
     * Uses the decision rule by Dankar et al., excluding the SNB model
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks() throws InterruptedException {
        try {
            return parent.getPopulationBasedAttributeRisks();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a class providing access to population-based risk estimates about the attributes.
     * @param model Uses the given statistical model
     * @return
     */
    public RiskModelAttributes getPopulationBasedAttributeRisks(StatisticalModel model) throws InterruptedException {
        try {
            return parent.getPopulationBasedAttributeRisks(model);
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a class providing population-based uniqueness estimates
     * @return
     */
    public RiskModelPopulationBasedUniquenessRisk getPopulationBasedUniquenessRisk() throws InterruptedException {
        try {
            return parent.getPopulationBasedUniquenessRiskInterruptible();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    /**
     * If supported by the according builder, this method will report a progress value in [0,100]. Otherwise,
     * it will always return 0
     * @return
     */
    public int getProgress() {
        return parent.getProgress();
    }

    /**
     * Returns a class providing access to sample-based risk estimates about the attributes
     * @return
     */
    public RiskModelAttributes getSampleBasedAttributeRisks() throws InterruptedException {
        try {
            return parent.getSampleBasedAttributeRisks();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }
    
    /**
     * Returns a class providing sample-based re-identification risk estimates
     * @return
     */
    public RiskModelSampleBasedReidentificationRisk getSampleBasedReidentificationRisk() throws InterruptedException {
        try {
            return parent.getSampleBasedReidentificationRisk();
        } catch (ComputationInterruptedException e) {
            throw new InterruptedException("Computation interrupted");
        }
    }

    /**
     * Returns a class providing sample-based uniqueness estimates
     * @return
     */
    public RiskModelSampleBasedUniquenessRisk getSampleBasedUniquenessRisk() throws InterruptedException {
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
