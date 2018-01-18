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

package org.deidentifier.arx.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
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
public class TestUtilityMetricsPrecomputation extends AbstractTestUtilityMetricsPrecomputation {
    
    /** A threshold */
    private final static double threshold = 1d;
    
    /**
     * Returns the test cases
     * 
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              
                                              // entropy: criterion monotone metric monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                                              
                                              // entropy: criterion monotone metric non-monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                                              
                                              // loss: criterion monotone metric monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createLossMetric(AggregateFunction.RANK), Metric.createPrecomputedLossMetric(threshold, AggregateFunction.RANK)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createLossMetric(AggregateFunction.RANK), Metric.createPrecomputedLossMetric(threshold, AggregateFunction.RANK)) },
                                              
                                              // entropy: criterion non-monotone metric monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                                              
                                              // entropy: criterion non-monotone metric non-monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                                              
                                              // loss: criterion non-monotone metric monotone
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", Metric.createLossMetric(AggregateFunction.RANK), Metric.createPrecomputedLossMetric(threshold, AggregateFunction.RANK)) },
                                              { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addPrivacyModel(new DPresence(0.05, 0.15, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", Metric.createLossMetric(AggregateFunction.RANK), Metric.createPrecomputedLossMetric(threshold, AggregateFunction.RANK)) },
                                              
        });
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestUtilityMetricsPrecomputation(final ARXUtilityMetricsTestCase testCase) {
        super(testCase);
    }
}
