/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.HashMap;

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.dp.ExponentialMechanismReliable;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for differential privacy
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationDifferentialPrivacy extends AbstractAnonymizationTest {
    
    /** Constant*/
    private static final double LN3 = new Log().value(3);
    /** Constant*/
    private static final double LN2 = new Log().value(2);
    
    /**
     * Create tests
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
//                                              /* Data-independent differential privacy */
//                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.6820705793543056, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/adult.csv", 0.8112222411559193, new int[] { 1, 3, 1, 2, 2, 2, 2, 2, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.7437618217405468, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/adult.csv", 0.6092780386290699, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/adult.csv", 0.5968589299712612, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.6856395441402736, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/cup.csv", 0.8261910998091719, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/cup.csv", 0.8499906952842589, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/cup.csv", 1.0000000000000004, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/cup.csv", 0.7606582708058056, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
//                                              /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/cup.csv", 1.0000000000000004, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, true) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/cup.csv", 0.839519540778112, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, true) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/fars.csv", 0.5814200080206713, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/fars.csv", 0.6800577425519756, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/fars.csv", 0.5864014933190864, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/fars.csv", 0.43090885593016726, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/fars.csv", 0.6796862034370221, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, true) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/fars.csv", 0.40463191801066123, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(0.01d, 1E-9d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.9999999999999989, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
//                                              /* Data-dependent differential privacy */
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10, false), "./data/adult.csv", 46.0, new int[] { 0, 4, 0, 0, 2, 2, 2, 1, 1 }, false) },
//                                              /* 20 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, false), "./data/adult.csv", -387.20969001288375, new int[] { 1, 4, 0, 2, 2, 1, 1, 2, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10, false), "./data/adult.csv", -347.16666666666663, new int[] { 0, 4, 0, 1, 1, 1, 2, 1, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10, false), "./data/adult.csv", -188.62462641980846, new int[] { 0, 2, 1, 1, 3, 2, 2, 2, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10, false), "./data/adult.csv", -354.63319957372687, new int[] { 1, 4, 0, 1, 2, 2, 1, 1, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10, false), "./data/cup.csv", 202.0, new int[] { 3, 3, 0, 1, 0, 4, 4, 4 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, false), "./data/cup.csv", -716.0950435310734, new int[] { 4, 3, 0, 1, 0, 3, 2, 4 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10, false), "./data/cup.csv", -765.2465163934426, new int[] { 4, 4, 0, 2, 0, 3, 2, 2 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10, false), "./data/cup.csv", -381.3900275253478, new int[] { 5, 4, 1, 1, 0, 4, 4, 4 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10, false), "./data/cup.csv", -641.4992301617474, new int[] { 4, 3, 0, 1, 0, 3, 3, 3 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10, false), "./data/fars.csv", 39.0, new int[] { 4, 2, 3, 2, 0, 1, 3, 2 }, false) },
//                                              /* 30 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, false), "./data/fars.csv", -1013.176735103407, new int[] { 4, 1, 2, 2, 0, 0, 2, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10, false), "./data/fars.csv", -1110.0924180327868, new int[] { 4, 0, 1, 3, 0, 0, 2, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10, false), "./data/fars.csv", -615.5132038230715, new int[] { 4, 2, 3, 3, 1, 2, 2, 0 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10, false), "./data/fars.csv", -970.5635388966205, new int[] { 4, 1, 2, 2, 0, 1, 1, 1 }, false) },
//                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100, false), "./data/adult.csv", 98.0, new int[] { 0, 1, 1, 2, 2, 2, 2, 1, 1 }, false) },
                                              /* Reliable and data-dependent differential privacy */
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, true), "", "./data/adult.csv", "-1095104 / 2745", new int[] { 0, 3, 0, 1, 3, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, true), "", "./data/cup.csv", "-186959735276631 / 298440428440", new int[] { 4, 4, 0, 1, 0, 1, 3, 2 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10, true), "", "./data/fars.csv", "-1393998867 / 1298080", new int[] { 4, 1, 2, 3, 0, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100, true), "", "./data/adult.csv", "-1118952113 / 3151260", new int[] { 1, 3, 0, 1, 2, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100, true), "", "./data/cup.csv", "-9120140763047 / 13565474020", new int[] { 4, 3, 0, 1, 0, 3, 3, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100, true), "", "./data/fars.csv", "-596021863 / 563640", new int[] { 3, 1, 2, 1, 0, 1, 3, 0 }, false) },
        });
    }
    
    /**
     * Creates a new test case for data-dependent differential privacy.
     * @param metric
     * @param epsilon
     * @param searchBudget
     * @param delta
     * @param steps
     * @return
     */
    private static ARXConfiguration createDataDependentConfiguration(Metric<?> metric, double epsilon, double searchBudget, double delta, int steps, boolean reliable) {
        ARXConfiguration result = ARXConfiguration.create(1d, metric);
        result.addPrivacyModel(new EDDifferentialPrivacy(epsilon, delta, null, true));
        result.setDPSearchBudget(searchBudget);
        result.setDPSearchStepNumber(steps);
        result.setReliableAnonymizationEnabled(reliable);
        return result;
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationDifferentialPrivacy(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
