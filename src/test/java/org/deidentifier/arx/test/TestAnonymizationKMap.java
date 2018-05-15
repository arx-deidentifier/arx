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
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.KMap.CellSizeEstimator;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for k-map.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationKMap extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(3, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "occupation", "./data/adult.csv", 130804.5332092598, new int[] { 0, 0, 1, 1, 0, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(1000, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "occupation", "./data/adult.csv", 151894.1394841501, new int[] { 0, 0, 1, 1, 1, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createAECSMetric()).addPrivacyModel(new KMap(5, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 45.014925373134325, new int[] { 1, 0, 1, 2, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KMap(3, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 23387.494246375998, new int[] { 0, 0, 1, 2, 3, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(5, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 28551.7222913157, new int[] { 1, 0, 1, 2, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createAECSMetric()).addPrivacyModel(new KMap(20, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 11.424242424242424, new int[] { 1, 0, 1, 1, 3, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KMap(7, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 17075.7181747451, new int[] { 0, 0, 1, 1, 2, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(3, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 15121.633326877098, new int[] { 0, 0, 1, 1, 1, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createAECSMetric()).addPrivacyModel(new KMap(5, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 45.014925373134325, new int[] { 1, 0, 1, 2, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KMap(2, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 23108.1673304724, new int[] { 1, 0, 1, 1, 3, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(10, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 30238.2081484441, new int[] { 0, 1, 1, 2, 3, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createAECSMetric()).addPrivacyModel(new KMap(10, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 7.215311004784689, new int[] { 0, 0, 1, 1, 3, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KMap(5, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 17053.8743069776, new int[] { 0, 0, 1, 0, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KMap(3, DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))), "occupation", "./data/adult.csv", 15121.633326877098, new int[] { 0, 0, 1, 1, 1, 2, 1, 0 }, false) },
        });
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationKMap(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
