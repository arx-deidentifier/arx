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
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;


/**
 * Implementation of the Discernibility measure, as proposed in:<br>
 * <br>
 * R. Bayardo, R. Agrawal: "Data privacy through optimal k-anonymization"
 * Proc Int Conf Data Engineering, 2005, pp. 217-228
 * 
 * @author Fabian Prasser
 */
public class QualityModelRowOrientedDiscernibility extends QualityModel<QualityMeasureRowOriented> {

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
    public QualityModelRowOrientedDiscernibility(WrappedBoolean interrupt,
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
        
        try {

            // Progress
            setSteps(2);
            
            // Calculate
            double rows = getInput().getNumRows();
            double min = getDiscernibility(getGroupedInput(), rows);
            min += (double)getSuppressedRecordsInInput() * rows;

            // Progress
            setStepPerformed();
            
            double max = rows * rows;
            double result = getDiscernibility(getGroupedOutput(), rows);
            result += (double)getSuppressedRecordsInOutput() * rows;

            // Progress
            setStepsDone();
            
            return new QualityMeasureRowOriented(min, result, max);
            
        } catch (Exception e) {

            // Progress
            setStepsDone();
            
            // Silently catch exceptions
            return new QualityMeasureRowOriented();
        }
    }

    /**
     * Get discernibility
     * @param groupify
     * @param rows
     * @return
     */
    private double getDiscernibility(Groupify<TupleWrapper> groupify, double rows) {
        Group<TupleWrapper> e = groupify.first();
        double sum = e.getCount() * e.getCount();
        while (e.hasNext()) {
            
            // Compute
            e = e.next();
            sum += e.getCount() * e.getCount();

            // Check
            checkInterrupt();
        }
        return sum;
    }
}
