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
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * Abstract base-class for column-oriented models
 * 
 * @author Fabian Prasser
 */
abstract class UtilityModelColumnOriented {

    /** Input */
    private final DataHandleInternal input;

    /** Helper */
    private final UtilityHelper      helper;

    /** Flag */
    private final WrappedBoolean     interrupt;

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     */
    UtilityModelColumnOriented(WrappedBoolean interrupt, 
                               DataHandleInternal input) {
        this.input = input;
        this.interrupt = interrupt;
        this.helper = new UtilityHelper(interrupt);
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
    abstract UtilityMeasureColumnOriented evaluate(DataHandleInternal output);
    
    UtilityHelper getHelper() {
        return this.helper;
    }

    DataHandleInternal getInput() {
        return this.input;
    }
}
