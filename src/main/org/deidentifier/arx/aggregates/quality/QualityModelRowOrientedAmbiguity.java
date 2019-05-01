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

package org.deidentifier.arx.aggregates.quality;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;

/**
 * Implementation of the Ambiguity measure, as described in:<br>
 * <br>
 * Goldberger, Tassa: "Efficient Anonymizations with Enhanced Utility"
 * Trans Data Priv
 * 
 * @author Fabian Prasser
 */
public class QualityModelRowOrientedAmbiguity extends QualityModel<QualityMeasureRowOriented> {

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param progress
     * @param totalWorkload
     * @param input
     * @param output
     * @param suppressedInput
     * @param suppressedOutput
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    public QualityModelRowOrientedAmbiguity(WrappedBoolean interrupt,
                                            WrappedInteger progress,
                                            int totalWorkload,
                                            DataHandle input,
                                            DataHandle output,
                                            int suppressedInput,
                                            int suppressedOutput,
                                            Groupify<TupleWrapper> groupedInput,
                                            Groupify<TupleWrapper> groupedOutput,
                                            String[][][] hierarchies,
                                            QualityDomainShare[] shares,
                                            int[] indices,
                                            QualityConfiguration config) {
          super(interrupt,
                progress,
                totalWorkload,
                input,
                output,
                suppressedInput,
                suppressedOutput,
                groupedInput,
                groupedOutput,
                hierarchies,
                shares,
                indices,
                config);
    }

    @Override
    public QualityMeasureRowOriented evaluate() {
        
        // Prepare
        int[] indices = getIndices();
        DataHandle output = getOutput();
        QualityDomainShare[] shares = getDomainShares();
        double min = 0d;
        double result = 0d;
        double max = 0d;

        // Progress
        setSteps(output.getNumRows());
        
        try {
            for (int row = 0; row < output.getNumRows(); row++) {
                double rowMin = 1d;
                double rowResult = 1d;
                double rowMax = 1d;
                for (int i = 0; i < indices.length; i++) {
                    int column = indices[i];
                    rowResult *= shares[i].getShare(output.getValue(row, column), 0) * shares[i].getDomainSize();
                    rowMin *= 1d;
                    rowMax *= shares[i].getDomainSize();
                }
                min += rowMin;
                result += rowResult;
                max += rowMax;

                // Progress
                setStepPerformed();
            }

            // Progress
            setStepsDone();
            
            return new QualityMeasureRowOriented(min, result, max);
            
        } catch (Exception e) {

            // Progress
            setStepsDone();
            
            // Silently catch exceptions
            return new QualityMeasureRowOriented(Double.NaN, Double.NaN, Double.NaN);
        }
    }
}
