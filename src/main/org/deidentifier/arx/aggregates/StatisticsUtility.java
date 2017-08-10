/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.aggregates;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.utility.UtilityConfiguration;
import org.deidentifier.arx.aggregates.utility.UtilityMeasureColumnOriented;
import org.deidentifier.arx.aggregates.utility.UtilityMeasureRowOriented;
import org.deidentifier.arx.aggregates.utility.UtilityModelColumnOrientedLoss;
import org.deidentifier.arx.aggregates.utility.UtilityModelColumnOrientedNonUniformEntropy;
import org.deidentifier.arx.aggregates.utility.UtilityModelColumnOrientedPrecision;
import org.deidentifier.arx.aggregates.utility.UtilityModelRowOrientedAECS;
import org.deidentifier.arx.aggregates.utility.UtilityModelRowOrientedAmbiguity;
import org.deidentifier.arx.aggregates.utility.UtilityModelRowOrientedDiscernibility;
import org.deidentifier.arx.aggregates.utility.UtilityModelRowOrientedKLDivergence;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Encapsulates statistics obtained using various utility models
 *
 * @author Fabian Prasser
 */
public class StatisticsUtility {

    /** Column-oriented model */
    private UtilityMeasureColumnOriented loss;
    /** Column-oriented model */
    private UtilityMeasureColumnOriented entropy;
    /** Column-oriented model */
    private UtilityMeasureColumnOriented precision;

    /** Row-oriented model */
    private UtilityMeasureRowOriented    aecs;
    /** Row-oriented model */
    private UtilityMeasureRowOriented    ambiguity;
    /** Row-oriented model */
    private UtilityMeasureRowOriented    discernibility;
    /** Row-oriented model */
    private UtilityMeasureRowOriented    kldivergence;

    /** State */
    private WrappedBoolean               stop;
    /** State */
    private WrappedInteger               progress;

    /**
     * Creates a new instance
     * @param input
     * @param output
     * @param config
     * @param stop
     * @param progress
     */
    public StatisticsUtility(DataHandleInternal input,
                             DataHandleInternal output,
                             ARXConfiguration config,
                             WrappedBoolean stop,
                             WrappedInteger progress) {
     
        // State
        this.stop = stop;
        this.progress = progress;
        
        // Build config
        UtilityConfiguration configuration = new UtilityConfiguration();
        
        // TODO: Do something with ARXConfiguration here.

        // Build
        try {
            this.loss = new UtilityModelColumnOrientedLoss(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.loss = new UtilityMeasureColumnOriented();
        }
        this.progress.value = 15;

        // Build
        try {
            this.entropy = new UtilityModelColumnOrientedNonUniformEntropy(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.entropy = new UtilityMeasureColumnOriented();
        }
        this.progress.value = 30;

        // Build
        try {
            this.precision = new UtilityModelColumnOrientedPrecision(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.precision = new UtilityMeasureColumnOriented();
        }
        this.progress.value = 45;

        // Build
        try {
            this.aecs = new UtilityModelRowOrientedAECS(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.aecs = new UtilityMeasureRowOriented();
        }
        this.progress.value = 60;

        // Build
        try {
            this.ambiguity = new UtilityModelRowOrientedAmbiguity(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.ambiguity = new UtilityMeasureRowOriented();
        }
        this.progress.value = 75;

        // Build
        try {
            this.discernibility = new UtilityModelRowOrientedDiscernibility(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.discernibility = new UtilityMeasureRowOriented();
        }
        this.progress.value = 90;

        // Build
        try {
            this.kldivergence = new UtilityModelRowOrientedKLDivergence(stop, input, configuration).evaluate(output);
            this.checkInterrupt();
        } catch (Exception e) {
            // Fail silently
            this.kldivergence = new UtilityMeasureRowOriented();
        }
        this.progress.value = 100;
    }

    /**
     * Utility according to the "Ambiguity" model proposed in:<br>
     * <br>
     * Goldberger, Tassa: "Efficient Anonymizations with Enhanced Utility"
     * Trans Data Priv
     * @return utility measure
     */
    public UtilityMeasureRowOriented getAmbiguity() {
        return ambiguity;
    }

    /**
     * Utility according to the "AECS" model proposed in:<br>
     * <br>
     * K. LeFevre, D. DeWitt, R. Ramakrishnan: "Mondrian multidimensional k-anonymity"
     * Proc Int Conf Data Engineering, 2006.
     * @return utility measure
     */
    public UtilityMeasureRowOriented getAverageClassSize() {
        return aecs;
    }

    /**
     * Utility according to the "Discernibility" model proposed in:<br>
     * <br>
     * R. Bayardo, R. Agrawal: "Data privacy through optimal k-anonymization"
     * Proc Int Conf Data Engineering, 2005, pp. 217-228
     * 
     * @return utility measure
     */
    public UtilityMeasureRowOriented getDiscernibility() {
        return discernibility;
    }

    /**
     * Utility according to the "Loss" model proposed in:<br>
     * <br>
     * Iyengar, V.: "Transforming data to satisfy privacy constraints"
     * Proc Int Conf Knowl Disc Data Mining, p. 279-288 (2002)
     * 
     * @return utility measure
     */
    public UtilityMeasureColumnOriented getGranularity() {
        return loss;
    }

    /**
     * Utility according to the "KL-Divergence" model proposed in:<br>
     * <br>
     * Ashwin Machanavajjhala, Daniel Kifer, Johannes Gehrke, Muthuramakrishnan Venkitasubramaniam: <br>
     * L-diversity: Privacy beyond k-anonymity<br>
     * ACM Transactions on Knowledge Discovery from Data (TKDD), Volume 1 Issue 1, March 2007
     * @return utility measure
     */
    public UtilityMeasureRowOriented getKLDivergence() {
        return kldivergence;
    }

    /**
     * Utility according to the "Non-Uniform Entropy" model proposed in:<br>
     * <br>
     * A. De Waal and L. Willenborg: "Information loss through global recoding and local suppression"
     * Netherlands Off Stat, vol. 14, pp. 17-20, 1999.
     * 
     * @return utility measure
     */
    public UtilityMeasureColumnOriented getNonUniformEntropy() {
        return entropy;
    }

    /**
     * Utility according to the "Precision" model proposed in:<br>
     * <br>
     * L. Sweeney: "Achieving k-anonymity privacy protection using generalization and suppression"
     * J Uncertain Fuzz Knowl Sys 10 (5) (2002) 571-588.
     * @return utility measure
     */
    public UtilityMeasureColumnOriented getPrecision() {
        return precision;
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (stop.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }
}
