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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Represents a sub-population.
 * 
 * @author Kieu-Mi Do
 */
public class GASubpopulation {

	/** List of individuals */
	private List<Transformation> individuals = new ArrayList<>();

	/**
	 * Adds an individual to the sub-population.
	 * 
	 * @param individual
	 */
	public void addIndividual(Transformation individual) {
		individuals.add(individual);
	}

	/**
	 * Gets the individual at index.
	 * 
	 * @param index
	 * @return
	 */
	public Transformation getIndividual(int index) {
		return individuals.get(index);
	}

	/**
	 * Gets the size of the sub-population.
	 * 
	 * @return
	 */
	public int individualCount() {
		return individuals.size();
	}

	/**
	 * Moves 'count' Individuals from this sub-population to 'other'
	 * 
	 * @param other
	 * @param count
	 */
	public void moveFittestIndividuals(GASubpopulation other, int count) {
		int size = this.individualCount();
		int min = Math.min(count, size);
		for (int i = 0; i < min; i++) {
			other.individuals.add(individuals.remove(0));
		}
	}

	/**
	 * Replaces the individual at a certain index
	 * 
	 * @param index
	 * @param individual
	 */
	public void setIndividual(int index, Transformation individual) {
		this.individuals.set(index, individual);
	}

	/**
	 * Sorts the individuals descending by fitness, which means ascending in
	 * terms of information loss.
	 */
	public void sort() {

		// Sort descending by fitness, ascending in terms of information loss.
		individuals.sort((a, b) -> {
			if (a == null) {
				return -1;
			}
			if (b == null) {
				return +1;
			}
			return a.getInformationLoss().compareTo(b.getInformationLoss());
		});
	}
}