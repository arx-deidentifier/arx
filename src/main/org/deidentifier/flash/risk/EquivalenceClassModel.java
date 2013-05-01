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
* This class allows to estimate the disclosure risk of a given data set based solely on the sample information
* using the information about size and frequency of equivalence classes to give a worst case estimate for the disclosure risk
* @author Michael Schneider
* @version 1.0
*/

import java.util.Map;

public class EquivalenceClassModel extends PopulationModel {
	
	/**
	 * The equivalence class model makes a worst case disclosure risk estimation for the data set as a whole based solely on the sample.
	 * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	public EquivalenceClassModel(Map<Integer, Integer> eqClasses) {
		super(0, eqClasses);
	}

	@Override
	public double computeRisk() {
		double result = 0;
		
		for (Map.Entry<Integer, Integer> entry : this.eqClasses.entrySet()) {
		    double temp = 0;
		    temp = 1 / (double) entry.getKey();
		    result += temp * entry.getValue() * entry.getKey();
		}
		
		return (result / this.n);
	}


}
