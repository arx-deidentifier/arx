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
 * 
# JavaStats is a toolkit designed to get a 
# statistically rigorous performance evaluation for a 
# given (Java) application
#
# Copyright (C) 2007 Andy Georges
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */


/**
* This class implements the Newton-Raphson algorithm for estimating the maximum likelihood values of the Pitman model,
* modified version of JavaStat Implementation 
* @author Michael Schneider, Andy Georges
* @version 1.0
*/

import java.util.Map;
import Jama.Matrix;


public class NewtonRaphsonPitman {

	
	    /** The total number of equivalence classes in the sample data set */
		private double u;
		
		/** The total number of entries in our sample data set */
		private double n;
		
		/** The number of equivalence class sizes (keys) and corresponding frequency (values) */
		private Map<Integer, Integer> eqClasses;
		
		/**
		 * Creates an instance of the Newton-Raphson Algorithm to determine the Maximum Likelihood Estimator for the Pitman Model
		 * @param u total number of entries in the sample data set
		 * @param n size of sample
		 * @param eqClasses The number of equivalence class sizes (keys) and corresponding frequency (values) 
		 */
		public NewtonRaphsonPitman(double u, double n, Map<Integer, Integer> eqClasses){
			this.u = u; this.n = n; this.eqClasses = eqClasses;
		}
		

	    /**
	     * The method for computing the object functions evaluated at the iterated solutions.
	     * @param iteratedSolution the iterated vector of solutions.
	     * @return the object functions evaluated at the iterated solutions.
	     */

		public double[] evalFunction(double[] iteratedSolution) {
			
			// theta is at iteratedSolution[0], alpha at [1]
			double[] result = new double[iteratedSolution.length];
			double temp1 = 0, temp2 = 0, temp3 = 0;
			
			// compute theta
			for(int i = 1; i < u; i++){
				temp1 += (1 / (iteratedSolution[0] + i * iteratedSolution[1]));
			}
			for(int i = 1; i < n; i++){
				temp2 += (1 / (iteratedSolution[0] + i));
			}
			result[0] = temp1 - temp2;
			
			// compute alpha
			temp1 = 0; temp2 = 0;temp3 = 0;
			for(int i = 1; i < u; i++){
				temp1 += (i / (iteratedSolution[0] + (i * iteratedSolution[1])));
			}
			for (Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
				temp3 = 0;
				if(entry.getKey() != 1){
					for(int j = 1; j < entry.getKey(); j++){
						temp3 += (1 / ( j - iteratedSolution[1])); 
					}
					temp2 += entry.getValue() * temp3;
				}
			}
			result[1] = temp1 - temp2;
			
			return result;
		}


	    /**
	     * The method for computing the first derivatives of the object functions evaluated at the iterated
	     * solutions.
	     * @param iteratedSolution the iterated vector of solutions.
	     * @return the first derivatives of the object functions evaluated at the
	     *         iterated solutions.
	     */

		public double[][] derivativeMatrix(double[] iteratedSolution) {
			double[][] result = new double[iteratedSolution.length][iteratedSolution.length];
			double temp1 = 0, temp2 = 0, temp3 = 0;
			
			
			// compute d^2L/(dtheta)^2
			for(int i = 1; i < u; i++){
				temp1 += (1 / ( (iteratedSolution[0] + (i * iteratedSolution[1])) * (iteratedSolution[0] + i * iteratedSolution[1])));
			}
			
			for (Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
				temp2 += (1 / ((iteratedSolution[0] + entry.getKey()) * (iteratedSolution[0] + entry.getKey())));
			}
			result[0][0] = temp2 - temp1;
			
			
			// compute d^2L/(d alpha)^2
			temp1 = 0; temp2 = 0;temp3 = 0;
			for(int i = 1; i < u; i++){
				temp1 += ( (i*i) / ( (iteratedSolution[0] + (i * iteratedSolution[1])) * (iteratedSolution[0] + (i * iteratedSolution[1]))) );
			}

			for (Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
				temp3 = 0;
				if(entry.getKey() != 1){
					for(int j = 1; j < entry.getKey(); j++){
						temp3 += (1 / ( (j - iteratedSolution[1]) * (j - iteratedSolution[1]) )); 
					}
					temp2 += entry.getValue() * temp3;
				}
			}
			result[1][1] = 0 - temp1 - temp2;
			
			
			//compute d^2L/(d theta d alpha)
			temp1 = 0; temp2 = 0;temp3 = 0;
			for(int i = 1; i < u; i++){
				temp1 += ( i / ( ((i * iteratedSolution[1]) + iteratedSolution[0]) * ((i * iteratedSolution[1]) + iteratedSolution[0])));
			}
			result[0][1] = 0-temp1;
			result[1][0] = 0-temp1;
			
			
			return result;
		}


	    /**
	     * Returns the iterated solution of a non-linear equation obtained by the Newton-Raphson algorithm.
	     * @param initialValue the vector of initial values.
	     * @return the vector of solutions.
	     * @exception IllegalArgumentException the first derivative matrix of the object functions is singular.
	     */

	    public double[] getSolution(double[] initialValue) throws IllegalArgumentException {
	    
		    /** The vector of solutions. */
	        Matrix solutionVector = new Matrix(initialValue, initialValue.length);
	        /** The iterated vector of solutions. */
	        Matrix updatedSolutionVector = new Matrix(initialValue, initialValue.length);
	        /** The vector of the differences between the iterated vectors of solutions. */
	        Matrix differenceVector = new Matrix(initialValue.length, 1, 150);
	        /** The vector of first derivatives of the object functions. */
	        Matrix firstDerivativeMatrix = new Matrix(initialValue.length, initialValue.length);
	        
	        int maxIterations = 0;
	        while (differenceVector.normF() > 1.0e-9 && maxIterations++ < 150) {
	        	
	            firstDerivativeMatrix = new Matrix(derivativeMatrix(solutionVector.getColumnPackedCopy()));
	            if (firstDerivativeMatrix.det() != 0.0) {
	                differenceVector = firstDerivativeMatrix.inverse().times(new Matrix(evalFunction(solutionVector.getColumnPackedCopy()), initialValue.length));
	                updatedSolutionVector = solutionVector.minus(differenceVector); }
	            else {
	                throw new IllegalArgumentException("The first derivative matrix of the object functions is singular.");
	            }
	            solutionVector = updatedSolutionVector;
	        }
	        
	        // maximum Iterations
	        if (maxIterations == 150) {
	            System.out.println("Maximum number of iterations have been acheived.");
	        }

	        return solutionVector.getColumnPackedCopy();
	    }

	    
}
