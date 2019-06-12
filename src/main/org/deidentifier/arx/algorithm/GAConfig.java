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
 */
public class GAConfig {

	private int subpopulationSize = 100;
	private int iterations = 50;
	private int immigrationInterval = 10;
	private int immigrationFraction = 10;
	private double elitePercent = 0.2f;
	private double crossoverPercent = 0.2f;
	private boolean deterministic = false;
	private double mutationProbability = 0.2f;

	public double getCrossoverPercent() {
		return crossoverPercent;
	}

	public double getElitePercent() {
		return elitePercent;
	}

	public int getImmigrationFraction() {
		return immigrationFraction;
	}

	public int getImmigrationInterval() {
		return immigrationInterval;
	}

	public int getIterations() {
		return iterations;
	}

	public int getSubpopulationSize() {
		return subpopulationSize;
	}

	public boolean isDeterministic() {
		return deterministic;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}
}