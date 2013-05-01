/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.risk;

import java.util.Map;

/**
* This class is the basis for estimating both population uniqueness and equivalence class risk.
* @author Michael Schneider
* @version 1.0
*/

public abstract class PopulationModel {
	
	/**
	 * sampling fraction
	 */
	protected double pi;
	
	/**
	 * size of the data set aka sample
	 */
	protected int n; 
	
	/**
	 * size of the population, this value is estimated using the sample size and the sampling fraction
	 */
	protected int N; 
	
	/**
	 * Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	protected Map<Integer, Integer> eqClasses;
	
	/**
	 * Number of equivalence classes that exist in the sample
	 */
	protected int u;
	
	/**
	 * size of biggest equivalence class in the data set
	 */
	protected int Cmax;
	
	/**
	 * size of smallest equivalence class in the data set
	 */
	protected int Cmin;
		
	/**
	 * Creates a model of the data set that allows for estimating the disclosure risk of the data
	 * @param pi sampling fraction
	 * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	public PopulationModel(double pi, Map<Integer, Integer> eqClasses){
		this.pi = pi;
		this.eqClasses = eqClasses;
		this.n = 0; this.u = 0;
		
		// set the class attributes
		this.Cmax = 0;
		this.Cmin = Integer.MAX_VALUE;
		for (Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
			this.Cmin = entry.getKey();
			this.n += entry.getKey() * entry.getValue();
			this.u += entry.getValue();
			if(entry.getKey() > this.Cmax){
				this.Cmax = entry.getKey();
			}
			if(entry.getKey() < this.Cmin){
				this.Cmin = entry.getKey();
			}
		}
		if(this.Cmin == Integer.MAX_VALUE){
			this.Cmin = 0;
		}
		this.N =  (int) (this.n / this.pi);
	}
	
	/**
	 * computes the re-identification risk on a file level for a given data set and a given measurement scenario 
	 * (population unique vs. equivalence class based)
	 * @return A single number representing the disclosure risk on a file level,
	 * depending on the model this estimate is based on population uniqueness or equivalence class size and frequency
	 */
	abstract public double computeRisk();

}
