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
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedInteger;

/**
 * This class implements the SNBModel, for details see Chen, 1998
 * 
 * @author Fabian Prasser
 * @author Michael Schneider
 * @version 1.09
 */
class ModelSNB extends RiskModelPopulationBased {

    /** The result*/
    private final double uniques;
    
    /**
     * Creates a new instance
     * 
     * @param classes
     * @param model
     */
    ModelSNB(final ARXPopulationModel model, 
             final RiskModelEquivalenceClasses classes,
             final double accuracy,
             final int maxIterations,
             final WrappedBoolean stop) {
        super(classes, model, stop, new WrappedInteger());
        
        int[] _classes = super.getClasses().getEquivalenceClasses();
        double numClassesOfSize1 = super.getNumClassesOfSize(1);
        
        double numNonEmptyClasses = estimateNonEmptyEquivalenceClasses(_classes,
                                                                       super.getNumClasses(),
                                                                       numClassesOfSize1,
                                                                       super.getSampleFraction());

        AlgorithmNewtonSNB snbModel = new AlgorithmNewtonSNB(numNonEmptyClasses, 
                                                             super.getSampleFraction(), 
                                                             (int)numClassesOfSize1,
                                                             (int)super.getNumClassesOfSize(2),
                                                             maxIterations,
                                                             accuracy,
                                                             stop);

        // Use Newton Raphson Algorithm to compute solution for the nonlinear multivariate equations
        // Start values are initialized randomly
        double[] initialGuess = { Math.random(), Math.random() };
        double[] output = snbModel.getSolution(initialGuess);
        this.uniques = numNonEmptyClasses * Math.pow(output[1], output[0]);
    }

    /**
     * Returns the number of uniques
     * @return
     */
    public double getNumUniques() {
        return this.uniques;
    }

    /**
     * @return Shlosser estimator for variable K, giving number of non zero
     *         classes in the population estimated according to Haas, 1998 and Shlosser
     */
    private double estimateNonEmptyEquivalenceClasses(int[] classes, 
                                                      double numClasses,
                                                      double numClassesOfSize1, 
                                                      double samplingFraction) {
        double var1 = 0, var2 = 0, var3 = 0, var4 = 0;

        for (int i = 0; i < classes.length; i+=2) {
            int size = classes[i];
            int count = classes[i + 1];
            var1 += size * samplingFraction * samplingFraction * Math.pow((1 - (samplingFraction * samplingFraction)), size - 1) * count;
            var2 += Math.pow((1 - samplingFraction), size) * (Math.pow((1 + samplingFraction), size) - 1) * count;
            var3 += Math.pow((1 - samplingFraction), size) * count;
            var4 += size * samplingFraction * Math.pow((1 - samplingFraction), (size - 1)) * count;
            checkInterrupt();
        }

        final double K = numClasses + (numClassesOfSize1 * (var1 / var2) * ((var3 / var4) * (var3 / var4)));
        return K;
    }
}
