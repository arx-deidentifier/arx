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

/**
 * This class implements a listener for the FLASH framework
 * 
 * @author Prasser, Kohlmayer
 */
public interface FLASHListener {

    /**
     * Is called when a node is checked
     * 
     * @param numNodes
     *            The total number of nodes in the lattice
     */
    public void nodeChecked(int numNodes);

    /**
     * Is called when a node is tagged
     * 
     * @param numNodes
     *            The total number of nodes in the lattice
     */
    public void nodeTagged(int numNodes);

}
