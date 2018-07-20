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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.deidentifier.arx.masking.variable.DistributionType.DiscreteBinomial;
import org.deidentifier.arx.masking.variable.DistributionType.DiscreteGeometric;

/**
 * Class describing a random variable
 *
 * @author Karol Babioch
 */
public class RandomVariable {

    private String name;

    private DistributionType type;

    private List<DistributionParameter<?>> parameters = new ArrayList<>();

    public RandomVariable(String name) {

        this.name = name;

    }

    public RandomVariable(String name, DistributionType type) {

        this.name = name;
        this.type = type;

    }

    public void setName(String name) {

        this.name = name;

    }

    public String getName() {

        return this.name;

    }

    public void setDistributionType(DistributionType type) {

        this.type = type;

    }

    public DistributionType getDistributionType() {

        return this.type;

    }

    public void addParameter(DistributionParameter<?> parameter) {

        parameters.add(parameter);

    }

    public DistributionParameter<?> getParameter(String name) {

        for (DistributionParameter<?> parameter : this.parameters) {

            if (parameter.getName().equals(name)) {

                return parameter;

            }

        }

        return null;

    }

    public void removeParameter(DistributionParameter<?> parameter) {

        parameters.remove(parameter);

    }

    public void removeParameter(String parameterName) {

        for (DistributionParameter<?> parameter : parameters) {

            if (parameter.getName().equals(parameterName)) {

                removeParameter(parameter);
                return;

            }

        }

    }

    // TODO: Check for null, update distribution if parameters are updated, etc.
    public Distribution<Integer> getDistribution() {

        if (this.type instanceof DiscreteBinomial) {

            Integer number = ((DistributionParameter.Int)getParameter("number")).getValue();
            Double probability = ((DistributionParameter.Dou)getParameter("probability")).getValue();
            return new DiscreteDistribution(0, number, new BinomialDistribution(number, probability));

        }
        if (this.type instanceof DiscreteGeometric) {

            Double probability = ((DistributionParameter.Dou)getParameter("probability")).getValue();
            return new DiscreteDistribution(0, (int)(5/probability), new GeometricDistribution(probability));

        }

        return null;

    }

}
