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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.metric.Metric;
import org.junit.Test;
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
public class TestAnonymizationTCloseness extends TestAnonymizationAbstract {

    /**
     * 
     *
     * @return
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

                /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "3.11880088E8", new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "3.11880088E8", new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "377622.78458729305", new int[] { 1, 4, 0, 0, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "390436.40440636466", new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "4.56853172E8", new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "4.56853172E8", new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "398400.0741806447", new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "398400.0741806447", new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "3.11880088E8", new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "3.11880088E8", new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true) },
                /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "390436.40440636466", new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", "390436.40440636466", new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5267622.788737194", new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5267622.788737194", new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5760138.103541854", new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5760138.103541854", new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5267622.788737194", new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", "5267622.788737194", new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "1407619.3716609064", new int[] { 3, 4, 1, 0, 0, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "1509368.8882843896", new int[] { 3, 4, 1, 1, 0, 2, 1 }, false) },
                /* 20 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "2023751.243421626", new int[] { 4, 4, 1, 2, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "2032837.6390798881", new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "1407669.5060439722", new int[] { 3, 4, 1, 0, 0, 4, 2 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", "1516301.8703843413", new int[] { 3, 4, 1, 1, 0, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", "4.2929731E7", new int[] { 0, 2, 3, 2, 1, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", "9.2944831E7", new int[] { 0, 2, 3, 3, 0, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", "7.80794309E8", new int[] { 1, 2, 3, 3, 1, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", "7.80794309E8", new int[] { 1, 2, 3, 3, 1, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", "4.2929731E7", new int[] { 0, 2, 3, 2, 1, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", "9.2944831E7", new int[] { 0, 2, 3, 3, 0, 2, 2 }, true) },
                /* 30 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true) },

        });
    }

    /**
     * 
     *
     * @param testCase
     */
    public TestAnonymizationTCloseness(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.test.TestAnonymizationAbstract#test()
     */
    @Override
    @Test
    public void test() throws IOException {

        // TODO: Ugly hack!
        if (!testCase.config.containsCriterion(TCloseness.class)) {
            final Hierarchy hierarchy = Hierarchy.create(testCase.dataset.substring(0, testCase.dataset.length() - 4) + "_hierarchy_" + testCase.sensitiveAttribute + ".csv", ';');
            testCase.config.addCriterion(new HierarchicalDistanceTCloseness(testCase.sensitiveAttribute, 0.2d, hierarchy));
        }
        super.test();

    }

}
