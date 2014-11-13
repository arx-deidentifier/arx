/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
import org.deidentifier.arx.framework.check.groupify.HashGroupify.GroupStatistics;

/**
 * 
 */
public interface IHashGroupify {

    /**
     * Generic adder for all combinations of criteria in mode transform ALL.
     *
     * @param outtuple
     * @param representant
     * @param count
     * @param sensitive
     * @param pcount
     */
    public abstract void addAll(int[] outtuple, int representant, int count, int[] sensitive, int pcount);

    /**
     * Generic adder for all combinations of criteria in mode transform GROUPIFY.
     *
     * @param outtuple
     * @param representant
     * @param count
     * @param distribution
     * @param pcount
     */
    public abstract void addGroupify(int[] outtuple, int representant, int count, Distribution[] distribution, int pcount);


    /**
     * Generic adder for all combinations of criteria in mode transform SNAPSHOT.
     *
     * @param outtuple
     * @param representant
     * @param count
     * @param elements
     * @param frequencies
     * @param pcount
     */
    public abstract void addSnapshot(int[] outtuple, int representant, int count, int[][] elements, int[][] frequencies, int pcount);
    
    /**
     * Computes the anonymity properties and suppressed tuples etc. Must be called
     * when all tuples have been passed to the operator. When the flag is set to true
     * the method will make sure that all equivalence classes that do not fulfill all
     * privacy criteria are marked as being suppressed. If the flag is set to false,
     * the operator may perform an early abort, which may lead to inconsistent classification
     * of equivalence classes.
     * 
     * @param force
     */
    public abstract void analyze(boolean force);

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
     * Returns statistics about the groups.
     *
     * @return
     */
    public abstract GroupStatistics getGroupStatistics();

    /**
     * Are all defined privacy criteria fulfilled by this transformation, given the specified limit on suppressed tuples.
     *
     * @return true, if successful
     */
    public abstract boolean isAnonymous();
    
    /**
     * Is the current transformation k-anonymous. Always returns true, if no k-anonymity (sub-)criterion was specified
     * 
     * @return
     */
    public abstract boolean isKAnonymous();
    

    /**
     * Marks all outliers.
     *
     * @param buffer
     */
    public abstract void markOutliers(int[][] buffer);

    /**
     * Resets all flags that indicate that equivalence classes are suppressed.
     */
    public abstract void resetSuppression();
    
    /**
     * Size.
     * 
     * @return the int
     */
    public abstract int size();
}
