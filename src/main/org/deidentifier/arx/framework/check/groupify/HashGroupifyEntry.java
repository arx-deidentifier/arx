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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.framework.check.distribution.Distribution;

/**
 * Implements an equivalence class.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class HashGroupifyEntry {

    /** The number of elements in this class. */
    public int               count        = 0;

    /** The number of public table elements in this class. */
    public int               pcount       = 0;

    /** The hashcode of this class. */
    public final int         hashcode;

    /** The key of this class. */
    public final int[]       key;

    /** The next element in this bucket. */
    public HashGroupifyEntry next         = null;

    /** The overall next element in original order. */
    public HashGroupifyEntry nextOrdered  = null;

    /** The index of the representative row. */
    public int               representant = -1;

    /** Is this class not an outlier?. */
    public boolean           isNotOutlier = false;

    /** Frequency set for sensitive attributes *. */
    public Distribution[]    distributions;

    /**
     * Creates a new entry.
     * 
     * @param key
     *            the key
     * @param hash
     *            the hash
     */
    public HashGroupifyEntry(final int[] key, final int hash) {
        hashcode = hash;
        this.key = key;
    }
}
