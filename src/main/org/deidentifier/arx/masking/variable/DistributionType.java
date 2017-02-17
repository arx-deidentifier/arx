/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

abstract public class DistributionType implements Serializable {

    private static final long serialVersionUID = -6444026395316975518L;

    abstract public DistributionTypeDescription getDescription();


    public static class DiscreteBinomial extends DistributionType {

        private static final long serialVersionUID = -6131417916199322727L;

        public static final DistributionTypeDescription description = new DistributionTypeDescription("Binomial distribution (discrete)") {

            private static final long serialVersionUID = 6235972585466197027L;

            @Override
            public List<DistributionParameter<?>> getParameters() {

                // Number parameter
                DistributionParameter<Integer> paramNumber = new DistributionParameter.Int();
                paramNumber.setName("number");
                paramNumber.setMin(0);
                paramNumber.setMax(Integer.MAX_VALUE);
                paramNumber.setInitial(10);
                paramNumber.setDescription("Number of tries");

                // Probability parameter
                DistributionParameter<Double> paramProbability = new DistributionParameter.Dou();
                paramProbability.setName("probability");
                paramProbability.setMin(0.0);
                paramProbability.setMax(1.0);
                paramProbability.setInitial(0.5);
                paramProbability.setDescription("Probability of success");

                ArrayList<DistributionParameter<?>> list = new ArrayList<>();

                list.add(paramNumber);
                list.add(paramProbability);

                return list;

            }

        };

        @Override
        public DistributionTypeDescription getDescription() {

            return description;

        }

    }


    public static class DiscreteGeometric extends DistributionType {

        private static final long serialVersionUID = -6131417916199322727L;

        public static final DistributionTypeDescription description = new DistributionTypeDescription("Geometric distribution (discrete)") {

            private static final long serialVersionUID = 6235972585466197027L;

            @Override
            public List<DistributionParameter<?>> getParameters() {

                // Probability parameter
                DistributionParameter<Double> paramProbability = new DistributionParameter.Dou();
                paramProbability.setName("probability");
                paramProbability.setMin(0.0);
                paramProbability.setMax(1.0);
                paramProbability.setInitial(0.5);
                paramProbability.setDescription("Probability");

                ArrayList<DistributionParameter<?>> list = new ArrayList<>();

                list.add(paramProbability);

                return list;

            }

        };

        @Override
        public DistributionTypeDescription getDescription() {

            return description;

        }

    }


    public static abstract class DistributionTypeDescription implements Serializable {

        private static final long serialVersionUID = 2298407320134003676L;

        private String label;

        private DistributionTypeDescription(String label) {

            this.label = label;

        }

        public String getLabel() {

            return this.label;

        }

        // Returns a list of parameters with their default value
        abstract public List<DistributionParameter<?>> getParameters();

    }

    public static final DistributionType DISCRETE_BINOMIAL = new DiscreteBinomial();
    public static final DistributionType DISCRETE_GEOMETRIC = new DiscreteGeometric();

    public static final List<DistributionTypeDescription> list() {

        ArrayList<DistributionTypeDescription> list = new ArrayList<>();

        list.add(DISCRETE_BINOMIAL.getDescription());
        list.add(DISCRETE_GEOMETRIC.getDescription());

        return list;

    }

}
