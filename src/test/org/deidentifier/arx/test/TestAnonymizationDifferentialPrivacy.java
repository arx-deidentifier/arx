/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for data transformations.
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
     * 
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/adult.csv", 0.5458823600135303, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "../arx-data/data-junit/adult.csv", 0.5678822604487141, new int[] { 1, 3, 1, 2, 2, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/adult.csv", 0.24946764318372394, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "../arx-data/data-junit/adult.csv", 0.31533166356032827, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "../arx-data/data-junit/adult.csv", 0.48108529511189313, new int[] { 1, 2, 1, 1, 2, 1, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/adult.csv", 0.45968085397145253, new int[] { 0, 2, 0, 1, 1, 1, 1, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN2, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "../arx-data/data-junit/cup.csv", 0.3927633276919862, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.0d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "../arx-data/data-junit/cup.csv", 0.5214350040632107, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/cup.csv", 0.8600566149273767, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.5d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "../arx-data/data-junit/cup.csv", 0.583207075673521, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, false) },
                                              /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(2d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/cup.csv", 0.8600566149273767, new int[] { 2, 2, 0, 1, 0, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN3, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "../arx-data/data-junit/cup.csv", 0.5449600312561718, new int[] { 3, 2, 1, 1, 1, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(2d, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/fars.csv", 0.48692660053630354, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN2, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "../arx-data/data-junit/fars.csv", 0.29200173854856404, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.0d, 1E-7d, DataGeneralizationScheme.create(GeneralizationDegree.LOW_MEDIUM), true)), "../arx-data/data-junit/fars.csv", 0.3265073816878079, new int[] { 2, 1, 1, 1, 0, 1, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.5d, 1E-8d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM), true)), "../arx-data/data-junit/fars.csv", 0.31414347743722537, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(LN3, 1E-5d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true)), "../arx-data/data-junit/fars.csv", 0.42130683838030025, new int[] { 4, 2, 2, 2, 1, 2, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.0d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "../arx-data/data-junit/fars.csv", 0.22028022469834752, new int[] { 3, 1, 2, 2, 1, 1, 2, 1 }, false) },
        });
    }
    
    /**
     * 
     *
     * @param testCase
     */
    public TestAnonymizationDifferentialPrivacy(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
