/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2024 Fabian Prasser and contributors
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
import java.util.List;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the heuristic genetic algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Thierry Meurers
 */
@RunWith(Parameterized.class)
public class TestAnonymizationGenetic extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        
        // Create list
        List<Object[]> cases = Arrays.asList(new Object[][] {
             /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", 0.22041192847984292, new int[] {0, 3, 0, 0, 2, 1, 1, 1, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", 383803.4034066361, new int[] {1, 1, 1, 1, 3, 2, 2, 1, 1}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.1d)).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", 0.09257312971977383, new int[] {0, 3, 0, 0, 0, 0, 0, 0, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", 359649.67365904135, new int[] {1, 0, 1, 1, 3, 2, 2, 2, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", 0.26611081915757495, new int[] {0, 3, 0, 1, 1, 1, 1, 2, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", 359649.67365904135, new int[] {1, 0, 1, 1, 3, 2, 2, 2, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/cup.csv", 0.15089346801304226, new int[] {4, 3, 0, 0, 0, 1, 3, 1}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(100)), "./data/cup.csv", 1994002.8308631124, new int[] {3, 4, 1, 1, 0, 4, 4, 4}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/cup.csv",0.13968936091749518, new int[] {4, 3, 0, 0, 0, 1, 2, 1}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(100)), "./data/cup.csv", 1994002.8308631124, new int[] {3, 4, 1, 1, 0, 4, 4, 4}, false) },
             /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(5)), "./data/cup.csv", 1764055.2617038768, new int[] {2, 4, 0, 1, 0, 3, 4, 2}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new KAnonymity(100)), "./data/cup.csv", 1994002.8308631124, new int[] {3, 4, 1, 1, 0, 4, 4, 4}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.5d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/fars.csv", 0.15285378029058072, new int[] {3, 0, 1, 1, 0, 0, 1, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(false)).addPrivacyModel(new KAnonymity(100)), "./data/fars.csv", 2.15186141E8, new int[] {3, 2, 1, 3, 1, 2, 2, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/fars.csv", 0.15285378029058072, new int[] {3, 0, 1, 1, 0, 0, 1, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createDiscernabilityMetric(false)).addPrivacyModel(new KAnonymity(100)), "./data/fars.csv", 2.15186141E8, new int[] {3, 2, 1, 3, 1, 2, 2, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.5d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/fars.csv", 0.15285378029058072, new int[] {3, 0, 1, 1, 0, 0, 1, 0}, false) },
             { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(false)).addPrivacyModel(new KAnonymity(100)), "./data/fars.csv", 2.15186141E8, new int[] {3, 2, 1, 3, 1, 2, 2, 0}, false) },
        });
        
        // Select genetic algorithm (deterministic) and set limits
        for (Object[] testcase : cases) {
            ((ARXAnonymizationTestCase)testcase[0]).config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_GENETIC);
            ((ARXAnonymizationTestCase)testcase[0]).config.setGeneticAlgorithmDeterministic(true);
            ((ARXAnonymizationTestCase)testcase[0]).config.setHeuristicSearchTimeLimit(Integer.MAX_VALUE);
            ((ARXAnonymizationTestCase)testcase[0]).config.setHeuristicSearchStepLimit(1000);
        }
        
        // Return
        return cases;
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationGenetic(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
