/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2020 Fabian Prasser and contributors
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
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Represents a subpopulation.
 * 
 * @author Kieu-Mi Do
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class GeneticAlgorithmSubpopulation {

    /** List of individuals */
    private List<Transformation<?>> individuals;

    /** Default constructor **/
    public GeneticAlgorithmSubpopulation() {
        this.individuals = new ArrayList<>();
    }

    /** Constructor used to create an identical copy **/
    public GeneticAlgorithmSubpopulation(GeneticAlgorithmSubpopulation pop) {
        this.individuals = new ArrayList<Transformation<?>>(pop.getIndividuals());
    }

    /**
     * Adds an individual to the subpopulation.
     * 
     * @param individual
     */
    public void addIndividual(Transformation<?> individual) {
        individuals.add(individual);
    }

    /**
     * Gets the individual at index.
     * 
     * @param index
     * @return
     */
    public Transformation<?> getIndividual(int index) {
        return individuals.get(index);
    }

    /**
     * Gets the size of the subpopulation.
     * 
     * @return
     */
    public int individualCount() {
        return individuals.size();
    }

    /**
     * Moves 'count' Individuals from this subpopulation to 'other'
     * 
     * @param other
     * @param count
     */
    public void moveFittestIndividuals(GeneticAlgorithmSubpopulation other, int immigrationCount) {
        int size = this.individualCount();
        immigrationCount = Math.min(size, immigrationCount);
        for (int i = 0; i < immigrationCount; i++) {
            other.individuals.add(individuals.remove(0));
        }
    }

    /**
     * Replaces the individual at a certain index
     * 
     * @param index
     * @param individual
     */
    public void setIndividual(int index, Transformation<?> individual) {
        this.individuals.set(index, individual);
    }

    /** Return the list containing the individuals **/
    public List<Transformation<?>> getIndividuals() {
        return individuals;
    }

    /**
     * Sorts the individuals descending by fitness, which means ascending in
     * terms of information loss.
     */
    public void sort() {

        // Sort descending by fitness, ascending in terms of information loss.
        individuals.sort(new Comparator<Transformation<?>>() {
            @Override
            public int compare(Transformation<?> t1, Transformation<?> t2) {
                if (t1 == null) { return -1; }
                if (t2 == null) { return +1; }
                return t1.getInformationLoss().compareTo(t2.getInformationLoss());
            }
        });
    }
}
