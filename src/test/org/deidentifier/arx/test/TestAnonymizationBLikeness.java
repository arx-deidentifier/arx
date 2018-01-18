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
import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for beta-likeness
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationBLikeness extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.5d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("occupation", 3)), "occupation", "./data/adult.csv", 0.34883856157237925, new int[]{ 0, 3, 0, 0, 2, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0d, Metric.createLossMetric()).addPrivacyModel(new EnhancedBLikeness("occupation", 2)), "occupation", "./data/adult.csv", 0.759176305345683, new int[]{ 1, 4, 1, 1, 3, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("occupation", 1)), "occupation", "./data/adult.csv", 0.6081345989904172, new int[]{ 1, 4, 0, 1, 3, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EnhancedBLikeness("occupation", 10)), "occupation", "./data/adult.csv", 0.3419039849540606, new int[]{ 0, 3, 0, 0, 3, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.4d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("occupation", 5)), "occupation", "./data/adult.csv", 0.23350465152356303, new int[]{ 0, 2, 0, 0, 2, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EnhancedBLikeness("occupation", 10)), "occupation", "./data/adult.csv", 0.3419039849540606, new int[]{ 0, 3, 0, 0, 3, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("Highest level of school completed", 3)), "Highest level of school completed", "./data/atus.csv", 0.6084288906930164, new int[]{ 1, 5, 0, 2, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EnhancedBLikeness("Highest level of school completed", 1)), "Highest level of school completed", "./data/atus.csv", 0.4597797188541255, new int[]{ 0, 5, 0, 0, 2, 0, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("Highest level of school completed", 2)), "Highest level of school completed", "./data/atus.csv", 0.636221748452025, new int[]{ 0, 5, 0, 1, 2, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new BasicBLikeness("Highest level of school completed", 1)), "Highest level of school completed", "./data/atus.csv", 0.4597797188541255, new int[]{ 0, 5, 0, 0, 2, 0, 0, 1 }, false) },
        });
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationBLikeness(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
