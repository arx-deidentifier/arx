/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser and Michael Schneider
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

package org.deidentifier.flash.risk;

/**
* This class abstracts the different population uniqueness scenarios 
* @author Michael Schneider
* @version 1.0
*/

import java.util.Map;

public abstract class UniquenessModel extends PopulationModel {

	/**
	 * Model based on the number of population uniques, estimating the population based on the sample
	 * @param pi sampling fraction
	 * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	public UniquenessModel(double pi, Map<Integer, Integer> eqClasses) {
		super(pi, eqClasses);
	}


	/**
	 * @return Population Uniqueness estimate as total number of individuals in a population
	 *
	 */
	abstract public double computeUniquenessTotal();


}
