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
package org.deidentifier.arx.common;

import java.util.Arrays;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleInternal;

/**
 * For hash tables
 * 
 * @author Fabian Prasser
 */
public class TupleWrapper {

    /** Hash code */
    private final int      hashcode;
    /** Indices */
    private final String[] values;
    /** Suppressed */
    private final boolean  suppressed;

    /**
     * Creates a new instance
     * @param handle
     * @param indices
     * @param row
     */
    public TupleWrapper(DataHandle handle, int[] indices, int row) {
        this.values = new String[indices.length];
        int hashcode = 1;
        int idx = 0;
        for (int index : indices) {
            String value = handle.getValue(row, index);
            hashcode = 31 * hashcode + value.hashCode();
            values[idx++] = value;
        }
        this.hashcode = hashcode;
        this.suppressed = handle.isOutlier(row);
    }
    
    /**
     * Creates a new instance
     * @param handle
     * @param indices
     * @param row
     * @param ignoreSuppression
     */
    public TupleWrapper(DataHandleInternal handle, int[] indices, int row, boolean ignoreSuppression) {
        this.values = new String[indices.length];
        int hashcode = 1;
        int idx = 0;
        for (int index : indices) {
            String value = handle.getValue(row, index, ignoreSuppression);
            hashcode = 31 * hashcode + value.hashCode();
            values[idx++] = value;
        }
        this.hashcode = hashcode;
        this.suppressed = handle.isOutlier(row);
    }

    /**
     * Creates a new instance
     * @param handle
     * @param indices
     * @param row
     * @param ignoreSuppression
     * @param wildcard
     */
    public TupleWrapper(DataHandleInternal handle, int[] indices, int row, boolean ignoreSuppression, String wildcard) {
        this.values = new String[indices.length];
        int hashcode = 1;
        int idx = 0;
        boolean suppressed = true;
        for (int index : indices) {
            String value = handle.getValue(row, index, ignoreSuppression);
            hashcode = 31 * hashcode + value.hashCode();
            values[idx++] = value;
            suppressed = suppressed && (wildcard != null && value.equals(wildcard));
        }
        this.hashcode = hashcode;
        this.suppressed = handle.isOutlier(row) || suppressed;
    }
    
    /**
     * Creates a new instance
     * @param handle
     * @param indices
     * @param row
     * @param wildcard
     */
    public TupleWrapper(DataHandleInternal handle, int[] indices, int row, String wildcard) {
        this.values = new String[indices.length];
        int hashcode = 1;
        int idx = 0;
        boolean suppressed = true;
        for (int index : indices) {
            String value = handle.getValue(row, index, false);
            hashcode = 31 * hashcode + value.hashCode();
            values[idx++] = value;
            suppressed = suppressed && value.equals(wildcard);
        }
        this.hashcode = hashcode;
        this.suppressed = suppressed;
    }

    @Override
    public boolean equals(Object other) {
        return Arrays.equals(((TupleWrapper) other).values, this.values);
    }

    /**
     * Returns the associated entry
     * @return
     */
    public String[] getValues() {
        return values;
    }
    
    @Override
    public int hashCode() {
        return hashcode;
    }
    
    /**
     * Is this record suppressed
     * @return
     */
    public boolean isSuppressed() {
        return suppressed;
    }
}