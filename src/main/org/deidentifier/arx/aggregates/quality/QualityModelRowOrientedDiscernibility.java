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

            // Progress
            setStepPerformed();
            
            double max = rows * rows;
            double result = getDiscernibility(getGroupedOutput(), rows);

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
        double sum = getPenalty(e, rows);
        while (e.hasNext()) {
            e = e.next();
            sum += getPenalty(e, rows);

            // Check
            checkInterrupt();
        }
        return sum;
    }

    /**
     * Returns the penalty for the given table
     * @param entry
     * @param rows
     * @return
     */
    private double getPenalty(Group<TupleWrapper> entry, double rows) {

        double count = entry.getCount();
        if (isSuppressed(entry)) {
            return count * rows;
        } else {            
            return count * count;
        }
    }
}
