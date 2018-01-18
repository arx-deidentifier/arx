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
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.ARXSolverConfiguration;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for risk-based anonymization
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationRiskBased extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addPrivacyModel(new AverageReidentificationRisk(0.01d)), "./data/adult.csv", 314637.8461904862, new int[] { 1, 0, 1, 1, 3, 2, 2, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addPrivacyModel(new SampleUniqueness(0.01d)), "./data/adult.csv", 0.1606952725863784, new int[] { 0, 3, 0, 0, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addPrivacyModel(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.DANKAR)), "./data/adult.csv", 144298.1603344462, new int[] { 0, 0, 1, 1, 1, 2, 1, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addPrivacyModel(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.ZAYATZ)), "./data/adult.csv", 0.16078200456326086, new int[] { 0, 3, 0, 0, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addPrivacyModel(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.PITMAN)), "./data/adult.csv", 144298.1603344462, new int[] { 0, 0, 1, 1, 1, 2, 1, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addPrivacyModel(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.SNB)), "./data/adult.csv", 0.17599055898432758, new int[] { 0, 3, 0, 0, 2, 1, 1, 1, 0 }, false) }
        });
    }
    
    /**
     * Constructor
     * @param testCase
     */
    public TestAnonymizationRiskBased(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
    /**
     * Returns a privacy model for population uniquqeness
     * @param threshold
     * @param model
     * @return
     */
    private static PopulationUniqueness getPopulationUniqueness(double threshold, PopulationUniquenessModel model) {
        
        return new PopulationUniqueness(threshold, model,
                                                     ARXPopulationModel.create(Region.USA),
                                                     ARXSolverConfiguration.create()
                                                                           .setDeterministic(true)
                                                                           .iterationsPerTry(15));
    }
}
