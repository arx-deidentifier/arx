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
import org.apache.commons.math3.special.Gamma;


/**
* This class implements the PitmanModel, for details see Hoshino, 2001
* @author Michael Schneider
* @version 1.0
*/

public class PitmanModel extends UniquenessModel {

	/**
	 * number of equivalence classes of size one in the sample
	 */
	private int c1;
	
	/**
	 * number of equivalence classes of size two in the sample
	 */
	private int c2;
	
	/**
	 * Population model according to Pitman, 1996
	 * @param pi sampling fraction
	 * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
	 */
	public PitmanModel(double pi, Map<Integer, Integer> eqClasses) {
		super(pi, eqClasses);
		
		this.c1 = this.eqClasses.get(1);
		this.c2 = this.eqClasses.get(2);
	}
	
	
	
	@Override
	public double computeUniquenessTotal() throws IllegalArgumentException {
		
		// initial guess
		double c = ( c1 * (c1 - 1)) / c2;
		double thetaGuess = ( (n * u * c) - ( c1*(n - 1) * (2*u + c) ) ) / ( (2 * c1 * u) + (c1 * c) - (n * c)  );
		double alphaGuess = ( (thetaGuess * (c1 - n)) + ((n - 1) * c1) ) / (n * u);
		
		// apply Newton-Rhapson algorithm to solve the Maximum Likelihood Estimates
		NewtonRaphsonPitman pitmanNewton = new NewtonRaphsonPitman(this.u , this.n , this.eqClasses);
		double[] initialGuess = {thetaGuess, alphaGuess};
		double[] output = pitmanNewton.getSolution(initialGuess);
			
		double theta = output[0];
		double alpha = output[1];
		double result;
		if(alpha != 0)
			//result = ( (Gamma.gamma(theta + 1) / Gamma.gamma(theta + alpha)) * Math.pow(N, alpha) );
			result = Math.exp(Gamma.logGamma(theta + 1) - Gamma.logGamma(theta + alpha)) * Math.pow(N, alpha);
		else
			result = Double.NaN;
		
		return result;
	}

	@Override
	public double computeRisk() {
		return (computeUniquenessTotal() / (double) this.N);
	}

	
}
