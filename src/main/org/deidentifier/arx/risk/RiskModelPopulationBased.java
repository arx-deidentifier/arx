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
import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

/**
 * Abstract base class for population-based models
 * @author Fabian Prasser
 */
public abstract class RiskModelPopulationBased {

    /** The classes*/
    private final RiskModelEquivalenceClasses classes;
    /** The population model*/
    private final ARXPopulationModel          populationModel;
    /** Stop flag */
    private final WrappedBoolean              stop;

    /**
     * Creates a new instance
     * @param classes
     */
    public RiskModelPopulationBased(RiskModelEquivalenceClasses classes,
                                    ARXPopulationModel populationModel,
                                    WrappedBoolean stop) {
        this.classes = classes;
        this.populationModel = populationModel;
        this.stop = stop;
    }
    
    /**
     * Checks for interrupts
     */
    protected void checkInterrupt() {
        if (stop.value) {
            throw new ComputationInterruptedException();
        }
    }
    
    /**
     * @return the classes
     */
    protected RiskModelEquivalenceClasses getClasses() {
        return classes;
    }
    
    /**
     * Returns the number of classes
     * @return
     */
    protected double getNumClasses() {
        return this.classes.getNumClasses();
    }
    
    /**
     * Returns the number of classes of the given size
     * @param size
     * @return
     */
    protected double getNumClassesOfSize(int size) {
        // TODO: Use binary search
        int[] classes = this.classes.getEquivalenceClasses();
        for (int i = 0; i < classes.length; i += 2) {
            if (classes[i] == size) {
                return classes[i+1];
            } else if (classes[i] > size) {
                break;
            }
        }
        return 0;
    }
    
    /**
     * @return the populationModel
     */
    protected ARXPopulationModel getPopulationModel() {
        return populationModel;
    }

    /**
     * Returns the population size
     * @return
     */
    protected double getPopulationSize() {
        return this.populationModel.getPopulationSize(this.classes.getHandle());
    }

    /**
     * Returns the sample fraction
     * @return
     */
    protected double getSampleFraction() {
        return this.populationModel.getSampleFraction(this.classes.getHandle());
    }

    /**
     * Returns the sample size
     * @return
     */
    protected double getSampleSize() {
        return this.classes.getHandle().getNumRows();
    }
}
