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

package org.deidentifier.arx.masking.variable;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.deidentifier.arx.masking.variable.DistributionType.DiscreteBinomial;
import org.deidentifier.arx.masking.variable.DistributionType.DiscreteGeometric;

/**
 * Class describing a random variable
 *
 * @author Karol Babioch
 */
public class RandomVariable {

    /** Name */
    private String                         name;

    /** List of parameters */
    private List<DistributionParameter<?>> parameters = new ArrayList<>();

    /** Distribution type */
    private DistributionType               type;

    /**
     * Creates an instance.
     * @param name
     */
    public RandomVariable(String name) {
        this.name = name;
    }

    /**
     * Creates an instance.
     * 
     * @param name
     * @param type
     */
    public RandomVariable(String name, DistributionType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Adds a parameter.
     * @param parameter
     */
    public void addParameter(DistributionParameter<?> parameter) {
        parameters.add(parameter);
    }

    // TODO: Check for null, update distribution if parameters are updated, etc.
    /**
     * Returns the distribution
     * @return
     */
    public Distribution<Integer> getDistribution() {

        if (this.type instanceof DiscreteBinomial) {
            Integer number = ((DistributionParameter.IntegerParameter) getParameter("number")).getValue();
            Double probability = ((DistributionParameter.DoubleParameter) getParameter("probability")).getValue();
            return new DiscreteDistribution(0, number, new BinomialDistribution(number, probability));
        }
        if (this.type instanceof DiscreteGeometric) {
            Double probability = ((DistributionParameter.DoubleParameter) getParameter("probability")).getValue();
            return new DiscreteDistribution(0, (int) (5 / probability), new GeometricDistribution(probability));
        }
        return null;
    }

    /**
     * Returns the distribution type.
     * @return
     */
    public DistributionType getDistributionType() {
        return this.type;
    }

    /**
     * Returns the name.
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the distribution parameter with this name.
     * @param name
     * @return
     */
    public DistributionParameter<?> getParameter(String name) {

        for (DistributionParameter<?> parameter : this.parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Removes this parameter.
     * @param parameter
     */
    public void removeParameter(DistributionParameter<?> parameter) {
        parameters.remove(parameter);
    }

    /**
     * Removes the paramter with this name.
     * @param parameterName
     */
    public void removeParameter(String parameterName) {

        for (DistributionParameter<?> parameter : parameters) {
            if (parameter.getName().equals(parameterName)) {
                removeParameter(parameter);
                return;
            }
        }
    }

    /**
     * Sets the distribution.
     * @param type
     */
    public void setDistributionType(DistributionType type) {
        this.type = type;
    }

    /**
     * Sets the name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

}
