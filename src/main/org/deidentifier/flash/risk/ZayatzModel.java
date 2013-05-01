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
* This class implements the ZayatzModel based on Equivalence Class, for details see the paper 
* ESTIMATION OF THE NUMBER OF UNIQUE POPULATION ELEMENTS USING A SAMPLE, Zayatz, 1991
* @author Michael Schneider
* @version 1.0
*/

import java.util.Map;
import org.apache.commons.math3.distribution.*;

public class ZayatzModel extends UniquenessModel {

	/**
	 * Zayatz model, based on Zayatz, 1991
	 * @param pi sampling fraction
	 * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	public ZayatzModel(double Pi, Map<Integer, Integer> eqClasses) {
		super(Pi, eqClasses);
	}

	@Override
	public double computeUniquenessTotal(){
		double condUniqPercentage = computeConditionalUniquenessPercentage();
		return ( (this.eqClasses.get(1) * condUniqPercentage) / this.pi );
	}
	
	@Override
	public double computeRisk(){
		return (computeConditionalUniquenessTotal() / (double) this.n);
	}
	
	/**
	 * 
	 * @return estimate of the the probability that an equivalence class
	 * of size 1 in the sample was chosen from an equivalence class of size 1 in the population 
	 */
	public double computeConditionalUniquenessPercentage(){
		double temp = 0;
			
		for (Map.Entry<Integer, Integer> entry : this.eqClasses.entrySet()) {
			HypergeometricDistribution distribution = new HypergeometricDistribution(N, entry.getKey(), n);
			temp += (entry.getValue() / ((double) this.u)) * distribution.probability(1);
		}
		
		HypergeometricDistribution distribution = new HypergeometricDistribution(N, 1, n);
		double probCond = ( (this.eqClasses.get(1) / ((double) this.u)) * (distribution.probability(1)) )   / temp; 
		
		return probCond;
	}
	
	/**
	 * 
	 * @return estimate of the total number of sample uniques that are also population uniques
	 */
	public double computeConditionalUniquenessTotal(){
		return (this.eqClasses.get(1) * computeConditionalUniquenessPercentage());
	}
	
}
