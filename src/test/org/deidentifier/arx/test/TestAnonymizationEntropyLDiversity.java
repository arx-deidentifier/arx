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
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity.EntropyEstimator;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for entropy l-diversity using the Grassberger and Shannon estimators.
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationEntropyLDiversity extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] { /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 5, EntropyEstimator.GRASSBERGER)), "occupation", "./data/adult.csv", 216092.124036387, new int[]{ 1, 0, 1, 0, 3, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 100, EntropyEstimator.SHANNON)), "occupation", "./data/adult.csv", 0.0d, null, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 5, EntropyEstimator.GRASSBERGER)), "occupation", "./data/adult.csv", 324620.5269918692, new int[]{ 1, 1, 1, 1, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 3, EntropyEstimator.GRASSBERGER)), "occupation", "./data/adult.csv", 180347.4325366015, new int[]{ 0, 0, 1, 1, 2, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 5, EntropyEstimator.SHANNON)), "occupation", "./data/adult.csv", 228878.2039109517, new int[]{ 1, 0, 1, 1, 2, 2, 2, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.1d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new EntropyLDiversity("occupation", 100, EntropyEstimator.GRASSBERGER)), "occupation", "./data/adult.csv", 0.0d, null, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 5, EntropyEstimator.GRASSBERGER)), "RAMNTALL", "./data/cup.csv", 1833435.0, new int[]{ 4, 0, 1, 0, 1, 3, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.03d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 100, EntropyEstimator.GRASSBERGER)), "RAMNTALL", "./data/cup.csv", 4.5168281E7, new int[]{ 4, 4, 0, 0, 1, 3, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "./data/cup.csv", 3.01506905E8, new int[]{ 4, 4, 1, 1, 1, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 3)), "RAMNTALL", "./data/cup.csv", 9.2264547E7, new int[]{ 4, 4, 1, 0, 1, 4, 4 }, false) },
                                              /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 5, EntropyEstimator.SHANNON)), "RAMNTALL", "./data/cup.csv", 2823649.0, new int[]{ 4, 0, 0, 1, 1, 3, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.1d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new EntropyLDiversity("RAMNTALL", 100, EntropyEstimator.GRASSBERGER)), "RAMNTALL", "./data/cup.csv", 3.4459973E7, new int[]{ 5, 0, 0, 2, 1, 2, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 5, EntropyEstimator.GRASSBERGER)), "EDUC", "./data/ihis.csv", 7735322.29514608, new int[]{ 0, 0, 0, 1, 3, 0, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 2, EntropyEstimator.GRASSBERGER)), "EDUC", "./data/ihis.csv", 5428093.534997522, new int[]{ 0, 0, 0, 0, 2, 0, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 5, EntropyEstimator.SHANNON)), "EDUC", "./data/ihis.csv", 1.2258628558792587E7, new int[]{ 0, 0, 0, 3, 3, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 100, EntropyEstimator.GRASSBERGER)), "EDUC", "./data/ihis.csv", 0.0d, null, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 5, EntropyEstimator.GRASSBERGER)), "EDUC", "./data/ihis.csv", 7735322.29514608, new int[]{ 0, 0, 0, 1, 3, 0, 0, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.02d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new EntropyLDiversity("EDUC", 3, EntropyEstimator.SHANNON)), "EDUC", "./data/ihis.csv", 7578152.206004559, new int[]{ 0, 0, 0, 2, 2, 0, 0, 1 }, true) },
        });
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationEntropyLDiversity(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
