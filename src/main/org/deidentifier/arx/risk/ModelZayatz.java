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

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedInteger;

/**
 * This class implements the ZayatzModel based on equivalence classes, for details see the paper
 * ESTIMATION OF THE NUMBER OF UNIQUE POPULATION ELEMENTS USING A SAMPLE, Zayatz, 1991
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * @version 1.0
 */
class ModelZayatz extends RiskModelPopulationBased {
    
    /** Resulting estimate */
    private final double numUniques;

    /**
     * Creates a new instance
     * 
     * @param classes
     * @param model
     */
    ModelZayatz(ARXPopulationModel model, final RiskModelEquivalenceClasses classes, WrappedBoolean stop) {
        super(classes, model, stop, new WrappedInteger());
        
        int[] _classes = super.getClasses().getEquivalenceClasses();
        double conditionalUniquenessPercentage =  computeConditionalUniquenessPercentage(_classes,
                                                      (int)super.getPopulationSize(), // TODO: Might overflow
                                                      (int)super.getSampleSize(),
                                                      (int)super.getNumClasses());
        
        this.numUniques = super.getNumClassesOfSize(1) * conditionalUniquenessPercentage / super.getSampleFraction();
    }

    /**
     * Returns the number of uniques
     * @return
     */
    public double getNumUniques() {
        return this.numUniques;
    }
    
    /**
     * Estimates the probability that an equivalence class of size
     * 1 in the sample was chosen from an equivalence class of size 1 in
     * the population
     * @param classes
     * @param populationSize
     * @param sampleSize
     * @param numClasses
     * @return
     */
    private double computeConditionalUniquenessPercentage(int[] classes,
                                                          int populationSize,
                                                          int sampleSize,
                                                          int numClasses) {
        
        int numClassesOfSize1 = classes[0] == 1 ? classes[1] : 0;
        double temp = 0;

        for (int i = 0; i < classes.length; i+=2) {
            int size = classes[i];
            int count = classes[i + 1];
            HypergeometricDistribution distribution = new HypergeometricDistribution(populationSize, size, sampleSize);
            temp += (count / ((double) numClasses)) * distribution.probability(1);
            checkInterrupt();
        }

        HypergeometricDistribution distribution = new HypergeometricDistribution(populationSize, 1, sampleSize);
        return (((double)numClassesOfSize1 / ((double) numClasses)) * (distribution.probability(1))) / temp;
    }
}
