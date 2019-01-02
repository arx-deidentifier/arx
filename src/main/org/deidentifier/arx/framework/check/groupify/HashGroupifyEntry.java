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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.data.DataMatrix;

/**
 * Implements an equivalence class.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class HashGroupifyEntry {

    /** The number of elements in this class. Excluding elements from the public table. */
    public int               count          = 0;

    /** The number of elements in this class. Including elements from the public table */
    public int               pcount         = 0;

    /** The hashcode of this class. */
    public final int         hashcode;

    /** The key of this class. */
    public final int         row;

    /** The next element in this bucket. */
    public HashGroupifyEntry next           = null;

    /** The overall next element in original order. */
    public HashGroupifyEntry nextOrdered    = null;

    /** The index of the representative row. */
    public int               representative = -1;

    /** Is this class not an outlier?. */
    public boolean           isNotOutlier   = false;

    /** Frequency set for other attributes *. */
    public Distribution[]    distributions;
    
    /** Matrix*/
    private final DataMatrix matrix;

    /**
     * Creates a new entry.
     * 
     * @param matrix the matrix
     * @param row the row
     * @param hash the hash
     */
    public HashGroupifyEntry(DataMatrix matrix, final int row, final int hash) {
        this.hashcode = hash;
        this.row = row;
        this.matrix = matrix;
    }
    
    /**
     * Columns
     * @return
     */
    public int columns() {
        return matrix.getNumColumns();
    }
    
    /**
     * Return next value
     * @return
     */
    public int next() {
        return matrix.iterator_next();
    }

    /**
     * Initialize iterator
     */
    public void read() {
        matrix.iterator(row);
    }
}
