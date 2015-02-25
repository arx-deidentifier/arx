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

import org.apache.commons.math3.special.Gamma;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedInteger;

/**
 * This class implements the PitmanModel, for details see Hoshino, 2001
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * @version 1.0
 */
class ModelPitman extends RiskModelPopulationBased {

    /** The result */
    private final double numUniques;

    /**
     * Creates a new instance
     * @param model
     * @param classes
     * @param sampleSize
     * @param accuracy
     * @param maxIterations
     * @param stop
     */
    ModelPitman(final ARXPopulationModel model, 
                final RiskModelEquivalenceClasses classes, 
                final int sampleSize,
                final double accuracy,
                final int maxIterations,
                final WrappedBoolean stop) {
        super(classes, model, sampleSize, stop, new WrappedInteger());

        int numClassesOfSize1 = (int) super.getNumClassesOfSize(1);
        int numClassesOfSize2 = (int) super.getNumClassesOfSize(2);
        if (numClassesOfSize2 == 0) numClassesOfSize2 = 1; // Overestimate
        int numClasses = (int) super.getNumClasses();
        double populationSize = super.getPopulationSize();

        // Initial guess
        final double c = (numClassesOfSize1 * (numClassesOfSize1 - 1)) / numClassesOfSize2;
        final double thetaGuess = ((sampleSize * numClasses * c) - (numClassesOfSize1 * (sampleSize - 1) * ((2 * numClasses) + c))) /
                                  (((2 * numClassesOfSize1 * numClasses) + (numClassesOfSize1 * c)) - (sampleSize * c));
        final double alphaGuess = ((thetaGuess * (numClassesOfSize1 - sampleSize)) + ((sampleSize - 1) * numClassesOfSize1)) /
                                  (sampleSize * numClasses);

        // Apply Newton-Rhapson algorithm to solve the Maximum Likelihood Estimates
        final AlgorithmNewtonPitman pitmanNewton = new AlgorithmNewtonPitman(numClasses, sampleSize, classes.getEquivalenceClasses(), maxIterations, accuracy, stop);
        final double[] initialGuess = { thetaGuess, alphaGuess };
        final double[] output = pitmanNewton.getSolution(initialGuess);

        final double theta = output[0];
        final double alpha = output[1];
        double result;
        if (alpha != 0) {
            result = Math.exp(Gamma.logGamma(theta + 1) - Gamma.logGamma(theta + alpha)) * Math.pow(populationSize, alpha);
        } else {
            result = Double.NaN;
        }

        this.numUniques = result;
    }

    /**
     * Returns the number of uniques
     * @return
     */
    public double getNumUniques() {
        return this.numUniques;
    }
}
