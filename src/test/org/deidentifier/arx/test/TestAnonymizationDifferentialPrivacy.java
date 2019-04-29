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
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -387.20969001288375, new int[] { 1, 4, 0, 2, 2, 1, 1, 2, 0 }, false) },
                                              /* 20 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -716.0950435310734, new int[] { 4, 3, 0, 1, 0, 3, 2, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -1013.1767351034068, new int[] { 4, 1, 2, 2, 0, 0, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -364.91072721704967, new int[] { 0, 3, 1, 0, 2, 1, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -683.2773579000898, new int[] { 4, 3, 0, 1, 1, 2, 4, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createLossMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -1001.9815803947275, new int[] { 4, 0, 2, 2, 0, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -347.1666666666667, new int[] { 0, 4, 0, 1, 1, 1, 2, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -731.4550204918032, new int[] { 4, 4, 0, 1, 0, 4, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -1040.6372950819673, new int[] { 5, 0, 1, 3, 0, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -349.6703096539162, new int[] { 0, 4, 0, 0, 2, 2, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -711.9860655737705, new int[] { 4, 4, 0, 1, 0, 1, 2, 3 }, false) },
                                              /* 30 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createPrecisionMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -1053.669262295082, new int[] { 4, 0, 2, 1, 0, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -354.6331995737269, new int[] { 1, 4, 0, 1, 2, 2, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -641.4992301617475, new int[] { 4, 3, 0, 1, 0, 3, 3, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -970.5635388966206, new int[] { 4, 1, 2, 2, 0, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -337.4125485059108, new int[] { 0, 3, 1, 1, 2, 1, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -657.9236664285716, new int[] { 4, 4, 0, 1, 0, 3, 3, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createEntropyMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -960.4723936491008, new int[] { 3, 0, 2, 2, 0, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", "46.0", new int[] { 0, 4, 0, 0, 2, 2, 2, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", "202.0", new int[] { 3, 3, 0, 1, 0, 4, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", "39.0", new int[] { 4, 2, 3, 2, 0, 1, 3, 2 }, false) },
                                              /* 40 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", "88.0", new int[] { 1, 3, 1, 1, 1, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", "219.0", new int[] { 3, 2, 1, 1, 0, 3, 2, 3 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createAECSMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", "227.0", new int[] { 1, 2, 2, 3, 0, 1, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", -189.1673041207017, new int[] { 1, 3, 1, 2, 3, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", -381.3900275253479, new int[] { 5, 4, 1, 1, 0, 4, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", -609.6640978436658, new int[] { 5, 2, 3, 2, 0, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", -184.58604046896227, new int[] { 0, 4, 1, 1, 3, 2, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", -373.7703579163081, new int[] { 3, 4, 1, 2, 0, 3, 3, 4 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createDiscernabilityMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", -615.06832486135, new int[] { 5, 1, 3, 2, 1, 2, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/adult.csv", 231.53225806451613, new int[] { 1, 4, 1, 2, 3, 2, 2, 2, 0 }, false, null, new String[] {"salary-class"}) },
                                              /* 50 */ { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/cup.csv", 353.61290322580646, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/fars.csv", 316.58870967741933, new int[] { 5, 2, 3, 3, 1, 1, 3, 2 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", 246.40322580645162, new int[] { 0, 4, 1, 1, 2, 2, 2, 2, 0 }, false, null, new String[] {"salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", 353.61290322580646, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", 960.0967741935484, new int[] { 5, 1, 2, 3, 1, 0, 3, 0 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", 246.56451612903226, new int[] { 1, 4, 1, 1, 1, 2, 2, 2, 0 }, false, null, new String[] {"salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", 353.3709677419355, new int[] { 5, 4, 0, 2, 1, 4, 4, 1 }, false, null, new String[] {"GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", 960.7258064516129, new int[] { 5, 0, 3, 3, 1, 0, 3, 2 }, false, null, new String[] {"ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/adult.csv", 115.76612903225806, new int[] { 1, 4, 1, 2, 3, 2, 2, 2, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/cup.csv", 176.80645161290323, new int[] { 5, 4, 0, 2, 1, 4, 4, 4 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              /* 60 */{ new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 1), "", "./data/fars.csv", 336.68548387096774, new int[] { 5, 2, 3, 3, 0, 2, 3, 2 }, false, null, new String[] {"isex", "ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/adult.csv", 152.8508064516129, new int[] { 1, 4, 1, 1, 3, 2, 1, 1, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/cup.csv", 274.2782258064516, new int[] { 5, 3, 0, 1, 1, 4, 4, 3 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 10), "", "./data/fars.csv", 809.766129032258, new int[] { 5, 1, 3, 3, 0, 0, 2, 1 }, false, null, new String[] {"isex", "ihispanic"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/adult.csv", 153.91935483870967, new int[] { 0, 4, 1, 2, 2, 2, 2, 0, 0 }, false, null, new String[] {"occupation", "salary-class"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/cup.csv", 274.28225806451616, new int[] { 5, 3, 0, 1, 1, 4, 4, 4 }, false, null, new String[] {"INCOME", "GENDER"}) },
                                              { new ARXAnonymizationTestCase(createDataDependentConfiguration(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 100), "", "./data/fars.csv", 806.7741935483871, new int[] { 5, 1, 1, 3, 0, 0, 3, 0 }, false, null, new String[] {"isex", "ihispanic"}) },
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
