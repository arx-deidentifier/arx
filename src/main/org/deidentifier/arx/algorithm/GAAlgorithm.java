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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformationChecker.ScoreType;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * Implementation of the genetic algorithm described in "Expanding Access to
 * Large-Scale Genomic Data While Promoting Privacy: A Game Theoretic Approach"
 * by Wan et al. DOI: 10.1016/j.ajhg.2016.12.002
 * 
 * @author Kieu-Mi Do
 * @author Fabian Prasser
 */
public class GAAlgorithm extends AbstractAlgorithm {

	/**
	 * Returns a new instance
	 * 
	 * @param solutionSpace
	 * @param checker
	 * @return
	 */
	public static AbstractAlgorithm create(SolutionSpace<?> solutionSpace, TransformationChecker checker,
			int heuristicSearchStepLimit, double geneticAlgorithmCrossoverPercent,
			boolean geneticAlgorithmDeterministic, double geneticAlgorithmElitePercent,
			int geneticAlgorithmImmigrationFraction, int geneticAlgorithmImmigrationInterval,
			double geneticAlgorithmMutationProbability, int geneticAlgorithmSubpopulationSize) {
		return new GAAlgorithm(solutionSpace, checker, heuristicSearchStepLimit, geneticAlgorithmCrossoverPercent,
				geneticAlgorithmDeterministic, geneticAlgorithmElitePercent, geneticAlgorithmImmigrationFraction,
				geneticAlgorithmImmigrationInterval, geneticAlgorithmMutationProbability,
				geneticAlgorithmSubpopulationSize);
	}

	/** RNG */
	private final Random random;
	

	/** Max values */
	private final int[] maxLevels;
	/** Min values */
	private final int[] minLevels;
	/** Checker */
	private final TransformationChecker checker;
	/** Progress tracking */
	private int checks = 0;
	/** Progress tracking */
	private int maxChecks = 0;

	/** Configuration */
	//TODO - currently the iterations are simply derived from the heuristicSearchStepLimit
	private int geneticAlgorithmIterations;

	private double geneticAlgorithmCrossoverPercent;

	private double geneticAlgorithmElitePercent;

	private int geneticAlgorithmImmigrationFraction;

	private int geneticAlgorithmImmigrationInterval;

	private double geneticAlgorithmMutationProbability;

	private int geneticAlgorithmSubpopulationSize;

	/**
	 * Creates a new instance
	 * 
	 * @param solutionSpace
	 * @param checker
	 */
	public GAAlgorithm(SolutionSpace<?> solutionSpace, TransformationChecker checker, int geneticAlgorithmIterations,
			double geneticAlgorithmCrossoverPercent, boolean geneticAlgorithmDeterministic,
			double geneticAlgorithmElitePercent, int geneticAlgorithmImmigrationFraction,
			int geneticAlgorithmImmigrationInterval, double geneticAlgorithmMutationProbability,
			int geneticAlgorithmSubpopulationSize) {
		super(solutionSpace, checker);
		this.checker = checker;
		this.checker.getHistory().setStorageStrategy(StorageStrategy.ALL);
		this.maxLevels = solutionSpace.getTop().getGeneralization();
		this.minLevels = solutionSpace.getBottom().getGeneralization();
		this.geneticAlgorithmIterations = geneticAlgorithmIterations;

		this.geneticAlgorithmCrossoverPercent = geneticAlgorithmCrossoverPercent;
		this.geneticAlgorithmElitePercent = geneticAlgorithmElitePercent;
		this.geneticAlgorithmImmigrationFraction = geneticAlgorithmImmigrationFraction;
		this.geneticAlgorithmImmigrationInterval = geneticAlgorithmImmigrationInterval;
		this.geneticAlgorithmMutationProbability = geneticAlgorithmMutationProbability;
		this.geneticAlgorithmSubpopulationSize = geneticAlgorithmSubpopulationSize;	
		this.random = geneticAlgorithmDeterministic ? new Random(0xDEADBEEF) : new Random();
	}

	@Override
	public boolean traverse() {

		// Prepare
		// k is defined in a way that stops very small sub-populations
		// from breaking the algorithm, as very small values fail to
		// solve. The GA requires diversity, and small sub-populations do not
		// provide enough information to satisfy that
		int k = this.maxLevels.length + geneticAlgorithmSubpopulationSize;
		int itr = geneticAlgorithmIterations;
		int imm = geneticAlgorithmImmigrationInterval;
		int immf = geneticAlgorithmImmigrationFraction;

		// Progress
		this.maxChecks = (2 * k + itr * 2 * (int) Math.ceil(geneticAlgorithmCrossoverPercent * k)
				+ itr * 2 * (int) Math.ceil(geneticAlgorithmElitePercent * k));

		// Build sub-populations
		GASubpopulation z1 = new GASubpopulation();
		GASubpopulation z2 = new GASubpopulation();

		// Fill sub-population 1
		for (int i = 0; i < k; i++) {

			// Prepare
			int[] generalization = new int[maxLevels.length];

			// Create "triangle" structure to cover the solution space
			if (i < this.maxLevels.length) {

				// Fill 0 .. i with max generalization levels
				for (int j = 0; j <= i; j++) {
					generalization[j] = maxLevels[j];
				}

				// Fill the rest with min generalization levels
				for (int j = i + 1; j < maxLevels.length; j++) {
					generalization[j] = minLevels[j];
				}

			} else {

				// Generate random individual
				for (int j = 0; j < maxLevels.length; j++) {
					generalization[j] = getRandomGeneralizationLevel(j);
				}
			}
			z1.addIndividual(getIndividual(generalization));
		}

		// Fill sub-population 2
		for (int i = 0; i < k; i++) {

			// Prepare
			int[] generalization = new int[maxLevels.length];

			// Generate random individual
			for (int j = 0; j < maxLevels.length; j++) {
				generalization[j] = getRandomGeneralizationLevel(j);
			}
			z2.addIndividual(getIndividual(generalization));
		}

		// Main iterator
		for (int t = 0; t < itr; t++) {

			// Sort by fitness descending
			z1.sort();
			z2.sort();

			// Swap individuals between GASubpopulations periodically
			if (t % imm == 0) {

				// Moves the imff fittest individuals between groups
				z1.moveFittestIndividuals(z2, immf);
				z2.moveFittestIndividuals(z1, immf);

				// Sort by fitness descending
				z1.sort();
				z2.sort();
			}

			// Iterate
			iterateSubpopulation(z1);
			iterateSubpopulation(z2);
		}

		// Check whether we found a solution
		return getGlobalOptimum() != null;
	}

	/**
	 * Returns an individual
	 * 
	 * @param generalization
	 * @return
	 */
	private Transformation<?> getIndividual(int[] generalization) {
		Transformation<?> transformation = this.solutionSpace.getTransformation(generalization);
		if (!transformation.hasProperty(this.solutionSpace.getPropertyChecked())) {
			transformation.setChecked(this.checker.check(transformation, true, ScoreType.INFORMATION_LOSS));
		}
		trackOptimum(transformation);
		progress((double) (checks++) / (double) maxChecks);
		return transformation;
	}

	/**
	 * Returns a mutated transformation, which means that a random parent is
	 * selected. <br>
	 * - Randomly generate an integer r, representing the number of mutated places
	 * (from 1 to ceil (upper bound on mutation probability * m)) <br>
	 * - Randomly generate r unrepeated integers (within the range [1, m]),
	 * representing the locations of mutated places <br>
	 * - Replace selected places with random levels
	 * 
	 * @return
	 */
	private Transformation<?> getMutatedIndividual(Transformation<?> transformation) {

		// Prepare
		int[] generalization = transformation.getGeneralization().clone();

		// Randomly generate an integer r, representing the number of
		// mutated places (from 1 to ceil (upper bound on mutation probability * m))
		int max = (int) Math.ceil(geneticAlgorithmMutationProbability * generalization.length);
		int numMutations = random.nextInt(max + 1);

		// Randomly generate r unrepeated integers (within the range [1, m]),
		// representing the locations of mutated places
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < generalization.length; i++) {
			list.add(i);
		}
		Collections.shuffle(list, random);
		List<Integer> mutationIndices = list.subList(0, numMutations);

		// Replace selected places with random levels
		for (int index : mutationIndices) {
			generalization[index] = getRandomGeneralizationLevel(index);
		}

		// Done
		return getIndividual(generalization);
	}

	/**
	 * Returns a random generalization level
	 * 
	 * @param dimension
	 * @return
	 */
	private int getRandomGeneralizationLevel(int dimension) {
		return minLevels[dimension]
				+ (int) Math.round(random.nextDouble() * (maxLevels[dimension] - minLevels[dimension]));
	}

	/**
	 * Selects a random individual within the given range from [0, range[ with
	 * probability proportional to their scaled fitness.
	 * 
	 * @param population
	 * @param range
	 * @return
	 */
	private Transformation<?>[] getRandomIndividuals(GASubpopulation population, int range, int count) {

		// Array of transformations, min and max
		InformationLoss<?> min = null;
		InformationLoss<?> max = null;
		Transformation<?>[] individuals = new Transformation[range];
		for (int i = 0; i < range; i++) {
			individuals[i] = population.getIndividual(i);
			InformationLoss<?> loss = individuals[i].getInformationLoss();
			if (min == null) {
				min = loss;
			} else if (min.compareTo(loss) > 0) {
				min = loss;
			}
			if (max == null) {
				max = loss;
			} else if (max.compareTo(loss) < 0) {
				max = loss;
			}
		}

		// Fitness
		List<Pair<Transformation<?>, Double>> elements = new ArrayList<>();
		for (int i = 0; i < range; i++) {
			elements.add(new Pair<Transformation<?>, Double>(individuals[i],
					1d - individuals[i].getInformationLoss().relativeTo(min, max)));
		}

		// Distribution
		EnumeratedDistribution<Transformation<?>> distribution = new EnumeratedDistribution<Transformation<?>>(
				new AbstractRandomGenerator() {
					@Override
					public double nextDouble() {
						return random.nextDouble();
					}

					@Override
					public void setSeed(long arg0) {
						// Do nothing
					}
				}, elements);

		// Sample
		Transformation<?>[] result = new Transformation[count];
		for (int i = 0; i < count; i++) {
			result[i] = distribution.sample();
		}
		return result;
	}

	/**
	 * Performs one iteration on a sub-population.
	 * 
	 * @param population
	 */
	private void iterateSubpopulation(GASubpopulation population) {

		// The population (ordered by fitness descending) consists of 3 groups
		// - First: all individuals in the elite group will remain unchanged
		// - Second: all individuals in the next group will be replaced by mutated
		// instances
		// - Third: all remaining individuals will replaced by crossed-over instances

		// Calculate mutation configuration parameters
		int k = population.individualCount();
		int crossoverCount = (int) Math.ceil(geneticAlgorithmCrossoverPercent * k);
		int eliteCount = (int) Math.ceil(geneticAlgorithmElitePercent * k);

		// Mutate fittest non-elite individuals
		for (int mutation = eliteCount; mutation < k - crossoverCount; mutation++) {

			// Mutate
			Transformation<?> individual = getMutatedIndividual(population.getIndividual(mutation));
			if (individual != null) {
				population.setIndividual(mutation, individual);
			}
		}

		// Crossover worst individuals
		Transformation<?>[] parents1 = getRandomIndividuals(population, eliteCount, crossoverCount);
		Transformation<?>[] parents2 = getRandomIndividuals(population, eliteCount, crossoverCount);
		for (int crossover = 0; crossover < crossoverCount; crossover++) {

			// Create crossover child
			int[] vec = new int[maxLevels.length];
			for (int i = 0; i < maxLevels.length; i++) {
				vec[i] = (random.nextDouble() < 0.5 ? parents1[crossover] : parents2[crossover]).getGeneralization()[i];
			}

			// Replace
			population.setIndividual(k - crossover - 1, getIndividual(vec));
		}
	}

}