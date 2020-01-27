/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.algorithm;

/**
 * Configuration properties for how the GA should alter the data.
 * 
 * @author Kieu-Mi Do
 * @author Fabian Prasser
 */
public class GAAlgorithmConfiguration {

    /** Sub-population size */
    private int     subpopulationSize   = 100;
    /** Iterations */
    private int     iterations          = 50;
    /** Immigration interval */
    private int     immigrationInterval = 10;
    /** Immigration fraction */
    private int     immigrationFraction = 10;
    /** Size of the elite */
    private double  elitePercent        = 0.2d;
    /** Fraction of individuals crossed-over */
    private double  crossoverPercent    = 0.2d;
    /** Deterministic */
    private boolean deterministic       = false;
    /** Mutation probability */
    private double  mutationProbability = 0.2d;
	
	/**
	 * Returns the fraction of individuals crossed over
	 * @return
	 */
	public double getCrossoverPercent() {
		return crossoverPercent;
	}

	/**
	 * Returns the size of the elite group
	 * @return
	 */
	public double getElitePercent() {
		return elitePercent;
	}

	/**
	 * Returns the fraction to immigrate
	 * @return
	 */
	public int getImmigrationFraction() {
		return immigrationFraction;
	}

	/**
	 * Returns the immigration interval
	 * @return
	 */
	public int getImmigrationInterval() {
		return immigrationInterval;
	}

	/**
	 * Returns the number of iterations
	 * @return
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * Returns the mutation probability
	 * @return
	 */
	public double getMutationProbability() {
	    return mutationProbability;
	}
	
	/**
	 * Returns the size of the sub-population
	 * @return
	 */
	public int getSubpopulationSize() {
		return subpopulationSize;
	}

	/**
	 * Deterministic execution
	 * @return
	 */
	public boolean isDeterministic() {
		return deterministic;
	}
}
