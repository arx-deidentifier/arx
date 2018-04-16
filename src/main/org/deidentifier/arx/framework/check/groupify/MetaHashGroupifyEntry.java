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

/**
 * Implements a grouping of equivalence classes
 * 
 * @author Fabian Prasser
 */
public class MetaHashGroupifyEntry {

    /** The hashcode of this class. */
    public final int             hashcode;

    /** The key of this class. */
    public final int             row;

    /** The next element in this bucket. */
    public MetaHashGroupifyEntry next         = null;

    /** The overall next element in original order. */
    public MetaHashGroupifyEntry nextOrdered  = null;

    /** Frequency set for the value to analyze *. */
    public Distribution          distribution = new Distribution();

    /**
     * Creates a new entry.
     * 
     * @param row the row
     * @param hash the hash
     */
    public MetaHashGroupifyEntry(final int row, final int hash) {
        this.hashcode = hash;
        this.row = row;
    }
}
