/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for utility transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestUtilityMetricsPrecomputation extends TestUtilityMetricsPrecomputationAbstract {

    /**  TODO */
    private final static double threshold = 1d;

    /**
     * 
     *
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {

                // entropy: criterion monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },

                // entropy: criterion monotone metric non-monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },

                // loss: criterion monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },

                // entropy: criterion non-monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },

                // entropy: criterion non-monotone metric non-monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },

                // loss: criterion non-monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },

        });
    }

    /**
     * 
     *
     * @param testCase
     */
    public TestUtilityMetricsPrecomputation(final ARXUtilityMetricsTestCase testCase) {
        super(testCase);
    }
}
