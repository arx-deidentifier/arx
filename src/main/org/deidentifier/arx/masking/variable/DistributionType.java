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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of classes representing all available distribution types.
 * 
 * @author Karol Babioch
 * @author Sandro Schaeffler
 * @author Peter Bock
 *
 */
abstract public class DistributionType implements Serializable {

    /**
     * Class representing a discrete binomial distribution.
     */
    public static class DiscreteBinomial extends DistributionType {

        public static final DistributionTypeDescription description      = new DistributionTypeDescription("Binomial distribution (discrete)") {

                                                                             private static final long serialVersionUID = 6235972585466197027L;

                                                                             @Override
                                                                             public List<DistributionParameter<?>> getParameters() {

                                                                                 // Number parameter
                                                                                 DistributionParameter<Integer> paramNumber = new DistributionParameter.IntegerParameter();
                                                                                 paramNumber.setName("number");
                                                                                 paramNumber.setMin(0);
                                                                                 paramNumber.setMax(Integer.MAX_VALUE);
                                                                                 paramNumber.setInitial(10);
                                                                                 paramNumber.setDescription("Number of tries");

                                                                                 // Probability parameter
                                                                                 DistributionParameter<Double> paramProbability = new DistributionParameter.DoubleParameter();
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

        /** SVUID */
        private static final long                       serialVersionUID = -6131417916199322727L;

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.variable.DistributionType#getDescription()
         */
        @Override
        public DistributionTypeDescription getDescription() {
            return description;
        }

    }

    /**
     * Class representing a discrete geometric distribution.
     */
    public static class DiscreteGeometric extends DistributionType {

        public static final DistributionTypeDescription description      = new DistributionTypeDescription("Geometric distribution (discrete)") {

                                                                             private static final long serialVersionUID = 6235972585466197027L;

                                                                             @Override
                                                                             public List<DistributionParameter<?>> getParameters() {

                                                                                 // Probability parameter
                                                                                 DistributionParameter<Double> paramProbability = new DistributionParameter.DoubleParameter();
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

        /** SVUID */
        private static final long                       serialVersionUID = -6131417916199322727L;

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.variable.DistributionType#getDescription()
         */
        @Override
        public DistributionTypeDescription getDescription() {
            return description;
        }

    }

    /**
     * Class representing a distribution type description.
     */
    public static abstract class DistributionTypeDescription implements Serializable {

        /** SVUID */
        private static final long serialVersionUID = 2298407320134003676L;

        /** Label */
        private String            label;

        /**
         * Creates an instance.
         * 
         * @param label
         */
        private DistributionTypeDescription(String label) {
            this.label = label;
        }

        /**
         * Returns the label.
         * @return
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Returns a list of parameters with their default value
         * @return
         */
        abstract public List<DistributionParameter<?>> getParameters();

    }

    /** Discrete binomial distribution */
    public static final DistributionType DISCRETE_BINOMIAL  = new DiscreteBinomial();

    /** Discrete geometric distribution */
    public static final DistributionType DISCRETE_GEOMETRIC = new DiscreteGeometric();

    /** SVUID */
    private static final long            serialVersionUID   = -6444026395316975518L;

    /**
     * Returns a list of distributions.
     * @return
     */
    public static final List<DistributionTypeDescription> list() {

        ArrayList<DistributionTypeDescription> list = new ArrayList<>();

        list.add(DISCRETE_BINOMIAL.getDescription());
        list.add(DISCRETE_GEOMETRIC.getDescription());

        return list;

    }

    /**
     * Returns the description.
     * @return
     */
    abstract public DistributionTypeDescription getDescription();

}
