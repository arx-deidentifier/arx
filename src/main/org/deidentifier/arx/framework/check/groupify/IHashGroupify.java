/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.framework.check.distribution.Distribution;

public interface IHashGroupify {

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

    /**
     * Generic adder for all combinations of criteria in mode transform ALL
     * @param outtuple
     * @param representant
     * @param count
     * @param sensitive
     * @param pcount
     */
    public abstract void addAll(int[] outtuple, int representant, int count, int sensitive, int pcount);
    
    /**
     * Generic adder for all combinations of criteria in mode transform GROUPIFY
     * @param outtuple
     * @param representant
     * @param count
     * @param distribution
     * @param pcount
     */
    public abstract void addGroupify(int[] outtuple, int representant, int count, Distribution distribution, int pcount);
    

    /**
     * Generic adder for all combinations of criteria in mode transform SNAPSHOT
     * @param outtuple
     * @param representant
     * @param count
     * @param elements
     * @param frequencies
     * @param pcount
     */
    public abstract void addSnapshot(int[] outtuple, int representant, int count, int[] elements, int[] frequencies, int pcount);
}
