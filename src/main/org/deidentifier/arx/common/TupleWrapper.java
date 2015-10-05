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
package org.deidentifier.arx.common;

import java.util.Arrays;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleStatistics;

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

    /**
     * Constructor
     * 
     * @param handle
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
    }
    
    /**
     * Constructor
     * 
     * @param handle
     * @param row
     */
    public TupleWrapper(DataHandleStatistics handle, int[] indices, int row) {
        this.values = new String[indices.length];
        int hashcode = 1;
        int idx = 0;
        for (int index : indices) {
            String value = handle.getValue(row, index);
            hashcode = 31 * hashcode + value.hashCode();
            values[idx++] = value;
        }
        this.hashcode = hashcode;
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
}