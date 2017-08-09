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

package org.deidentifier.arx.aggregates.utility;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;


/**
 * Implementation of the Discernibility measure, as proposed in:<br>
 * <br>
 * R. Bayardo, R. Agrawal, Data privacy through optimal k-anonymization.
 * Proc Int Conf Data Engineering, 2005, pp. 217-228
 * 
 * @author Fabian Prasser
 */
class UtilityModelRowOrientedDiscernibility extends UtilityModel<UtilityMeasureRowOriented> {

    /** Header */
    private final int[] indices;
    
    /** Minimum */
    private final double min;

    /** Maximum */
    private final double max;
    
    /** Rows*/
    private final double rows;
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    UtilityModelRowOrientedDiscernibility(WrappedBoolean interrupt,
                                          DataHandleInternal input,
                                          UtilityConfiguration config) {
        super(interrupt, input, config);
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.min = getDiscernibility(getHelper().getGroupify(input, indices));
        this.rows = input.getNumRows();
        this.max = rows * rows;
    }
    
    /**
     * Get discernibility
     * @param groupify
     * @return
     */
    private double getDiscernibility(Groupify<TupleWrapper> groupify) {
        Group<TupleWrapper> e = groupify.first();
        double sum = getPenalty(e);
        while (e.hasNext()) {
            e = e.next();
            sum += getPenalty(e);

            // Check
            checkInterrupt();
        }
        return sum;
    }

    /**
     * Returns the penalty for the given table
     * @param entry
     * @return
     */
    private double getPenalty(Group<TupleWrapper> entry) {

        double count = entry.getCount();
        if (isSuppressed(entry)) {
            return count * rows;
        } else {            
            return count * count;
        }
    }

    @Override
    UtilityMeasureRowOriented evaluate(DataHandleInternal output) {

        try {
            // Prepare
            Groupify<TupleWrapper> groupify = getHelper().getGroupify(output, indices);
            double result = getDiscernibility(groupify);
            return new UtilityMeasureRowOriented(min, result, max);
        } catch (Exception e) {
            // Silently catch exceptions
            return new UtilityMeasureRowOriented(min, Double.NaN, max);
        }
    }
}
