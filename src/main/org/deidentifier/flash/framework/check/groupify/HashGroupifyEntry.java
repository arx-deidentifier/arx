/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.check.groupify;

import org.deidentifier.flash.framework.check.distribution.Distribution;

/**
 * Implements an equivalence class.
 * 
 * @author Prasser, Kohlmayer
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

    /** Frequency set for l-diveryity **/
    public Distribution      distribution;

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
