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

import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.OrderedDistanceTCloseness;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for ordered-distance t-closeness.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationOrderedDistanceTCloseness extends AbstractAnonymizationTest {
    
    /**
     * Collection
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
            /* 0 */ { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", "2712340.0", new int[] { 0, 0, 1, 1, 2, 2, 2, 0 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(100)), "occupation", "./data/adult.csv", "1.9937246E7", new int[] { 1, 0, 1, 2, 3, 2, 2, 1 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.1d)).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", "9.786802E7", new int[] { 1, 1, 1, 2, 3, 2, 2, 1 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(100)), "occupation", "./data/adult.csv", "1.6231213E8", new int[] { 1, 4, 1, 1, 1, 2, 2, 1 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.05d)).addPrivacyModel(new KAnonymity(5)), "occupation", "./data/adult.csv", "2.01413138E8", new int[] { 1, 4, 1, 0, 3, 2, 2, 0 }, true) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new OrderedDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(100)), "occupation", "./data/adult.csv", "1.9937246E7", new int[] { 1, 0, 1, 2, 3, 2, 2, 1 }, true) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.25d)).addPrivacyModel(new KAnonymity(5)), "Highest level of school completed", "./data/atus.csv", "1999729.3356444335", new int[] { 0, 0, 0, 2, 1, 2, 2, 1 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.2d)).addPrivacyModel(new KAnonymity(100)), "Highest level of school completed", "./data/atus.csv", "3663507.668427732", new int[] { 0, 3, 0, 1, 2, 2, 2, 0 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.2d)).addPrivacyModel(new KAnonymity(5)), "Highest level of school completed", "./data/atus.csv", "4657839.672179246", new int[] { 0, 3, 0, 2, 2, 2, 2, 2 }, false) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.01d)).addPrivacyModel(new KAnonymity(100)), "Highest level of school completed", "./data/atus.csv", "7104624.912719078", new int[] { 1, 5, 1, 2, 2, 2, 2, 2 },false) },
            /* 10 */ { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.2d)).addPrivacyModel(new KAnonymity(5)), "Highest level of school completed", "./data/atus.csv", "3303937.388063534", new int[] { 0, 4, 0, 0, 2, 0, 2, 0 }, true) },
            { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new OrderedDistanceTCloseness("Highest level of school completed", 0.3d)).addPrivacyModel(new KAnonymity(100)), "Highest level of school completed", "./data/atus.csv", "2659996.2572910236", new int[] { 0, 4, 0, 1, 1, 1, 2, 1 }, true) },
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param testCase
     */
    public TestAnonymizationOrderedDistanceTCloseness(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
