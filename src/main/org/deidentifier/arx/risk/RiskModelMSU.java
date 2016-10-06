/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataMatrix;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;
import org.deidentifier.arx.risk.msu.SUDA2;

/**
 * A risk model based on MSUs in the data set
 * @author Fabian Prasser
 *
 */
public class RiskModelMSU {
    
    /** The data matrix */
    private final DataMatrix     matrix;
    /** Progress stuff */
    private final WrappedInteger progress;
    /** Progress stuff */
    private final WrappedBoolean stop;

    /**
     * Creates a new instance
     * @param handle
     * @param identifiers
     * @param stop 
     * @param progress 
     */
    RiskModelMSU(DataHandleInternal handle, Set<String> identifiers, WrappedInteger progress, WrappedBoolean stop) {

        // Store
        this.stop = stop;
        this.progress = progress;
        
        // Add all attributes, if none were specified
        if (identifiers == null || identifiers.isEmpty()) {
            identifiers = new HashSet<String>();
            for (int column = 0; column < handle.getNumColumns(); column++) {
                identifiers.add(handle.getAttributeName(column));
            }
        }
        
        // Build column array
        int[] columns = getColumns(handle, identifiers);
        
        // Build matrix
        this.matrix = handle.getDataMatrix(columns);
        
        // Update progress
        progress.value = 10;
        checkInterrupt();
        
        // Do something
        SUDA2 suda2 = new SUDA2(matrix.getMatrix());
        suda2.suda2(0);
    }

    /**
     * Checks for interrupts
     */
    private void checkInterrupt() {
        if (stop.value) { throw new ComputationInterruptedException(); }
    }


    /**
     * Returns the column array
     * @param handle
     * @param identifiers
     * @return
     */
    private int[] getColumns(DataHandleInternal handle, Set<String> identifiers) {
        int[] result = new int[identifiers.size()];
        int index = 0;
        for (String attribute : identifiers) {
            int column = handle.getColumnIndexOf(attribute);
            if (column == -1) {
                throw new IllegalArgumentException("Unknown attribute '" + attribute+"'");
            }
            result[index++] = column;
        }
        return result;
    }

}
