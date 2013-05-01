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

public interface IHashGroupify {

    /**
     * Adds a entry to the operator.
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     */
    public abstract void add(int[] key, int line, int value);

    /**
     * Adds an entry to this operator having already a frequency set attached.
     * Used for l-diversity (rollup optimization).
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     * @param distribution
     *            the frequency set
     */
    public abstract void add(int[] key, int line, int value, Distribution frequencySet);

    /**
     * Adds an entry. Used for l-diverstiy (basic optimization).
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     * @param sensitiveValue
     *            the sensitive value
     */
    public abstract void add(int[] key, int line, int value, int sensitiveValue);

    /**
     * Adds an entry. Used for l-diverstiy (history optimization).
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     * @param sensitiveElements
     *            the sensitive elements
     * @param sensitiveFrequencies
     *            the sensitive frequencies
     */
    public abstract void add(int[] key, int line, int value, int[] sensitiveElements, int[] sensitiveFrequencies);

    /**
     * Adds an entry. Used for d-presence
     * @param key the entry to add
     * @param line the reference to the current row in the dataset
     * @param value the count of the occurence of key
     * @param tvalue the count of the occurence of key in the research subset
     */
    public abstract void addD(int[] key, int line, int value, int tvalue);

    /**
     * Clear.
     */
    public abstract void clear();

    /**
     * Gets the first entry.
     * 
     * @return the first entry
     */
    public abstract HashGroupifyEntry getFirstEntry();

    /**
     * Returns the number of outlying groups
     * 
     * @return
     */
    public abstract int getGroupOutliersCount();

    /**
     * Returns the number of outlying tuples
     * 
     * @return
     */
    public abstract int getTupleOutliersCount();

    /**
     * Min groupsize greater equals.
     * 
     * @return true, if successful
     */
    public abstract boolean isAnonymous();

    /**
     * Is it k-anonymous?
     * 
     * @return
     */
    public abstract boolean isKAnonymous();

    /**
     * Marks all outliers
     * 
     * @param data
     */
    public abstract void markOutliers(int[][] buffer);

    /**
     * Size.
     * 
     * @return the int
     */
    public abstract int size();

}
