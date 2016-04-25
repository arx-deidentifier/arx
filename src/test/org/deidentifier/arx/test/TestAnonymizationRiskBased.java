/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
 * Test for data transformations.
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationRiskBased extends AbstractAnonymizationTest {
    
    /**
     * 
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addCriterion(new AverageReidentificationRisk(0.01d)), "./data/adult.csv", 311800.214271543, new int[] { 0, 2, 1, 1, 3, 2, 2, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addCriterion(new SampleUniqueness(0.01d)), "./data/adult.csv", 0.16162285410146238, new int[] { 0, 3, 0, 0, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addCriterion(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.DANKAR)), "./data/adult.csv", 147914.19280224087, new int[] { 0, 0, 1, 1, 1, 2, 1, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addCriterion(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.ZAYATZ)), "./data/adult.csv", 0.1617095808680391, new int[] { 0, 3, 0, 0, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d)).addCriterion(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.PITMAN)), "./data/adult.csv", 147914.19280224087, new int[] { 0, 0, 1, 1, 1, 2, 1, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedLossMetric(0.1d)).addCriterion(getPopulationUniqueness(0.0001d, PopulationUniquenessModel.SNB)), "./data/adult.csv", 0.17700077019900906, new int[] { 0, 3, 0, 0, 2, 1, 1, 1, 0 }, false) }
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
        
        double[][] startValues = new double[16][];
        int index = 0;
        for (double d1 = 0d; d1 < 1d; d1 += 0.33d) {
            for (double d2 = 0d; d2 < 1d; d2 += 0.33d) {
                startValues[index++] = new double[] { d1, d2 };
            }
        }
        
        return new PopulationUniqueness(threshold, model,
                                                     ARXPopulationModel.create(Region.USA),
                                                     ARXSolverConfiguration.create().preparedStartValues(startValues).iterationsPerTry(15));
    }
}
