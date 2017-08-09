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
import org.deidentifier.arx.common.Groupify.Group;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Base class for utility models
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
abstract class UtilityModel<T> {
    
    /** Log */
    private static final double        LOG2              = Math.log(2);
    
    /** Input */
    private final DataHandleInternal input;

    /** Helper */
    private final UtilityHelper      helper;

    /** Flag */
    private final WrappedBoolean     interrupt;

    /** Value */
    private final String             suppressedValue;

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    UtilityModel(WrappedBoolean interrupt, 
                 DataHandleInternal input,
                 UtilityConfiguration config) {
        this.input = input;
        this.interrupt = interrupt;
        this.helper = new UtilityHelper(interrupt, config);
        this.suppressedValue = config.getSuppressedValue();
    }
    
    /**
     * Checks whether an interruption happened.
     */
    void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Evaluates the utility measure
     * @param output
     * @return
     */
    abstract T evaluate(DataHandleInternal output);
    
    UtilityHelper getHelper() {
        return this.helper;
    }

    DataHandleInternal getInput() {
        return this.input;
    }

    /**
     * Returns whether a value is suppressed
     * @param handle
     * @param row
     * @param column
     * @return
     */
    boolean isSuppressed(DataHandleInternal handle, int row, int column) {

        // Check flag
        if (handle.isOutlier(row)) {
            return true;
        } else {
            return handle.getValue(row, column).equals(suppressedValue);
        }
    }

    /**
     * We assume that an entry is suppressed, if all values are equal
     * @param entry
     * @return
     */
    boolean isSuppressed(DataHandleInternal handle, int[] indices, int row) {
        
        // Check flag
        if (handle.isOutlier(row)) {
            return true;
        }
        
        // Check values
        for (int i = 1; i < indices.length; i++) {
            if (!handle.getValue(row, indices[i - 1]).equals(handle.getValue(row, indices[i]))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * We assume that an entry is suppressed, if all values are equal
     * @param entry
     * @return
     */
    boolean isSuppressed(Group<TupleWrapper> entry) {
        
        // Check flag
        if (entry.getElement().isOutlier()) {
            return true;
        }
        
        // Check values
        String[] array = entry.getElement().getValues();
        for (int i = 1; i < array.length; i++) {
            if (!array[i - 1].equals(array[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a value is suppressed
     * @param value
     * @return
     */
    boolean isSuppressed(String value) {
        return value.equals(suppressedValue);
    }

    /**
     * Log base-2
     * @param d
     * @return
     */
    double log2(double d) {
        return Math.log(d) / LOG2;
    }
}
