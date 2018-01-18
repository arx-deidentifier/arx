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

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Abstract base class for population-based uniqueness models
 * 
 * @author Fabian Prasser
 */
abstract class RiskModelPopulation {

    /** The classes */
    private final RiskModelHistogram histogram;
    /** The population model */
    private final ARXPopulationModel populationModel;
    /** Stop flag */
    private final WrappedBoolean     stop;
    /** Progress */
    private final WrappedInteger     progress;

    /**
     * Creates a new instance
     * 
     * @param histogram
     * @param populationModel
     * @param stop
     * @param progress
     */
    RiskModelPopulation(RiskModelHistogram histogram,
                        ARXPopulationModel populationModel,
                        WrappedBoolean stop,
                        WrappedInteger progress) {
        this.histogram = histogram;
        this.populationModel = populationModel;
        this.stop = stop;
        this.progress = progress;
    }

    /**
     * Checks for interrupts
     */
    protected void checkInterrupt() {
        if (stop.value) { throw new ComputationInterruptedException(); }
    }

    /**
     * @return the classes
     */
    protected RiskModelHistogram getHistogram() {
        return histogram;
    }

    /**
     * Returns the number of classes
     * 
     * @return
     */
    protected double getNumClasses() {
        return this.histogram.getNumClasses();
    }

    /**
     * Returns the number of classes of the given size. This method is only
     * efficient for smaller class sizes.
     * 
     * @param size
     * @return
     */
    protected double getNumClassesOfSize(int size) {
        int[] classes = this.histogram.getHistogram();
        for (int i = 0; i < classes.length; i += 2) {
            if (classes[i] == size) {
                return classes[i + 1];
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
     * 
     * @return
     */
    protected double getPopulationSize() {
        return this.populationModel.getPopulationSize();
    }

    /**
     * Returns the sample size
     * 
     * @return
     */
    protected double getSampleSize() {
        return this.histogram.getNumRecords();
    }

    /**
     * Returns the sample fraction
     * 
     * @return
     */
    protected double getSamplingFraction() {
        return this.getSampleSize() / this.getPopulationSize();
    }

    /**
     * Sets the progress
     * 
     * @param progress
     */
    protected void setProgress(int progress) {
        this.progress.value = progress;
    }
}
