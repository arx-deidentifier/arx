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

package org.deidentifier.flash;

import org.deidentifier.flash.FLASHLattice.FLASHNode;

/**
 * Encapsulates the results of an execution of the FLASH algorithm
 * 
 * @author Prasser, Kohlmayer
 */
public interface FLASHResult {

    /**
     * Returns the configuration of the previous run
     * 
     * @return
     */
    public abstract FLASHConfiguration getConfiguration();

    /**
     * Gets the global optimum.
     * 
     * @return the global optimum
     */
    public abstract FLASHNode getGlobalOptimum();

    /**
     * Returns the number of equivalence classes in the currently selected data
     * representation TODO: Should be in data handle
     * 
     * @return
     */
    public abstract int getGroupCount();

    /**
     * Returns the number of outlying groups in the currently selected data
     * representation TODO: Should be in data handle
     * 
     * @return
     */
    public abstract int getGroupOutliersCount();

    /**
     * Returns a handle to the data induced by the optimal transformation
     * 
     * @return
     */
    public abstract DataHandle getHandle();

    /**
     * Returns a handle to data induced by the given transformation
     * 
     * @param node
     *            the transformation
     * 
     * @return
     */
    public abstract DataHandle getHandle(FLASHNode node);

    /**
     * Returns the lattice
     * 
     * @return
     */
    public abstract FLASHLattice getLattice();

    /**
     * Returns the execution time (wall clock)
     * 
     * @return
     */
    public abstract long getTime();

    /**
     * Returns the number of outliers in the currently selected data
     * representation TODO: Should be in data handle
     * 
     * @return
     */
    public abstract int getTupleOutliersCount();

    /**
     * Indicates if a result is available
     * 
     * @return
     */
    public abstract boolean isResultAvailable();
}
