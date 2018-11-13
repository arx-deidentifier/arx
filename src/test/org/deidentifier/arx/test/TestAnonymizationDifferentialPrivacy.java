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

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.SearchStepSemantics;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
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
                                              /* Data-independent differential privacy */
                                              /* 0 */ { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.6820705793543056, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/adult.csv", 0.8112222411559193, new int[] { 1, 3, 1, 2, 2, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.7437618217405468, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/adult.csv", 0.6092780386290699, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/adult.csv", 0.5968589299712612, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.6856395441402736, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/cup.csv", 0.8261910998091719, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/cup.csv", 0.8499906952842589, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/cup.csv", 1.0000000000000004, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/cup.csv", 0.7606582708058056, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              /* 10 */ { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/cup.csv", 1.0000000000000004, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/cup.csv", 0.839519540778112, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/fars.csv", 0.5814200080206713, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN2, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/fars.csv", 0.6800577425519756, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/fars.csv", 0.5864014933190864, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.5d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "./data/fars.csv", 0.43090885593016726, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(LN3, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "./data/fars.csv", 0.6796862034370221, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/fars.csv", 0.40463191801066123, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new EDDifferentialPrivacy(0.01d, 1E-9d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "./data/adult.csv", 0.9999999999999989, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              /* Data-dependent differential privacy */
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -398.94499089253185, new int[] { 0, 3, 0, 1, 3, 2, 2, 0, 1 }, false) },
                                              /* 20 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -626.4557930502313, new int[] { 4, 4, 0, 1, 0, 1, 3, 2 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -1073.892877942808, new int[] { 4, 1, 2, 3, 0, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -355.0808606716044, new int[] { 1, 3, 0, 1, 2, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -672.3053503033431, new int[] { 4, 3, 0, 1, 0, 3, 3, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -1057.451321765666, new int[] { 3, 1, 2, 1, 0, 1, 3, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -368.19307832422584, new int[] { 1, 4, 0, 1, 3, 0, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -748.4215163934426, new int[] { 4, 4, 0, 1, 0, 1, 3, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -1136.2631147540983, new int[] { 4, 0, 2, 1, 0, 2, 3, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -361.40437158469945, new int[] { 0, 4, 0, 1, 2, 2, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -747.989344262295, new int[] { 4, 4, 0, 1, 0, 2, 4, 2 }, false) },
                                              /* 30 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -1142.0598360655738, new int[] { 3, 2, 2, 2, 0, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -363.6885505914479, new int[] { 0, 3, 1, 1, 3, 2, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -641.4992301617475, new int[] { 4, 3, 0, 1, 0, 3, 3, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -963.6017207428368, new int[] { 3, 1, 2, 2, 0, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -334.58577647224644, new int[] { 0, 3, 1, 1, 2, 1, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -641.4992301617475, new int[] { 4, 3, 0, 1, 0, 3, 3, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -966.3933585904429, new int[] { 4, 1, 2, 2, 0, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", "48.0", new int[] { 0, 4, 0, 1, 3, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", "203.0", new int[] { 5, 1, 1, 2, 0, 4, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", "65.0", new int[] { 4, 2, 3, 3, 0, 0, 2, 2 }, false) },
                                              /* 40 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", "83.0", new int[] { 0, 2, 1, 2, 1, 2, 2, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", "227.0", new int[] { 5, 4, 1, 1, 1, 0, 4, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", "313.0", new int[] { 2, 2, 3, 3, 1, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -183.7441467377909, new int[] { 1, 4, 1, 1, 3, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -373.539189985287, new int[] { 3, 4, 1, 2, 1, 4, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -605.1488552595091, new int[] { 4, 2, 2, 3, 0, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -185.1496336938025, new int[] { 0, 1, 1, 1, 3, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -373.3293937256593, new int[] { 4, 3, 1, 1, 1, 4, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -597.4067644125885, new int[] { 5, 2, 1, 2, 1, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/adult.csv", 231.53225806451613, new int[] { 1, 4, 1, 2, 3, 2, 2, 2, 0 }, false, null, new String[] {"salary-class"}) },
                                              /* 50 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/cup.csv", 353.61290322580646, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/fars.csv", 316.58870967741933, new int[] { 5, 2, 3, 3, 1, 1, 3, 2 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", 232.75806451612902, new int[] { 1, 4, 1, 1, 3, 2, 1, 2, 0 }, false, null, new String[] {"salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", 353.61290322580646, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", 961.4193548387096, new int[] { 5, 1, 3, 3, 1, 0, 3, 0 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", 246.41935483870967, new int[] { 1, 4, 1, 1, 2, 2, 2, 1, 0 }, false, null, new String[] {"salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", 357.11290322580646, new int[] { 5, 3, 0, 0, 1, 3, 4, 3 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", 955.3387096774194, new int[] { 5, 0, 1, 3, 1, 0, 3, 1 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/adult.csv", 115.76612903225806, new int[] { 1, 4, 1, 2, 3, 2, 2, 2, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/cup.csv", 176.80645161290323, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              /* 60 */{ new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/fars.csv", 336.68548387096774, new int[] { 5, 2, 3, 3, 0, 2, 3, 2 }, false, null, new String[] {"isex", "ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", 147.91129032258064, new int[] { 1, 3, 1, 2, 2, 2, 2, 1, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", 274.28225806451616, new int[] { 5, 3, 0, 1, 1, 4, 4, 4 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", 810.1370967741935, new int[] { 5, 1, 3, 2, 0, 0, 3, 0 }, false, null, new String[] {"isex", "ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", 156.75806451612902, new int[] { 1, 4, 1, 1, 3, 2, 1, 0, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", 274.28225806451616, new int[] { 5, 3, 0, 1, 1, 4, 4, 4 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", 791.2096774193549, new int[] { 4, 1, 2, 3, 0, 0, 2, 1 }, false, null, new String[] {"isex", "ihispanic"}) },
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
    private static ARXConfiguration createDataDependentConfiguration(Metric<?> metric, double epsilon, double searchBudget, double delta, int steps) {
        ARXConfiguration result = ARXConfiguration.create(1d, metric);
        result.addPrivacyModel(new EDDifferentialPrivacy(epsilon, delta, null, true));
        result.setDPSearchBudget(searchBudget);
        result.setHeuristicSearchStepSemantics(SearchStepSemantics.EXPANSIONS);
        result.setHeuristicSearchStepLimit(steps);
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
