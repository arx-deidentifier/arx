/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.test;

import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for data transformations
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationLDiversity extends TestAnonymizationAbstract {

    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 228878.2039109517, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 324620.5269918692, new int[] { 1, 1, 1, 1, 3, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 228878.2039109517, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 9.123049999E9, new int[] { 0, 3, 0, 0, 2, 0, 1, 0 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 3536911.5162082445, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false) },
                /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 2.0146560117E10, new int[] { 0, 3, 0, 2, 2, 2, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 4912828.240033204, new int[] { 0, 4, 0, 2, 2, 2, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 9.123049999E9, new int[] { 0, 3, 0, 0, 2, 0, 1, 0 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 3536911.5162082445, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2823649.0, new int[] { 4, 0, 0, 1, 1, 3, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 8.8290083E7, new int[] { 3, 4, 1, 2, 1, 2, 1 }, false) },
                /* 20 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1244350.7034669477, new int[] { 2, 4, 0, 1, 0, 3, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1552136.4930241706, new int[] { 3, 4, 1, 2, 0, 2, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 3.01506905E8, new int[] { 4, 4, 1, 1, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 6.19637215E8, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1961244.4822559545, new int[] { 4, 4, 1, 1, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2032837.6390798881, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2823649.0, new int[] { 4, 0, 0, 1, 1, 3, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 8.8290083E7, new int[] { 3, 4, 1, 2, 1, 2, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1244350.7034669477, new int[] { 2, 4, 0, 1, 0, 3, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1552136.4930241706, new int[] { 3, 4, 1, 2, 0, 2, 1 }, true) },
                /* 30 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 590389.0228300437, new int[] { 0, 2, 1, 1, 0, 2, 0 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 629604.893355563, new int[] { 0, 2, 1, 1, 0, 2, 0 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 1201007.0880104562, new int[] { 0, 2, 3, 3, 1, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 1201007.0880104562, new int[] { 0, 2, 3, 3, 1, 2, 2 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 590389.0228300437, new int[] { 0, 2, 1, 1, 0, 2, 0 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, true) },
                /* 40 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 629604.893355563, new int[] { 0, 2, 1, 1, 0, 2, 0 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 3.53744964E8, new int[] { 0, 0, 0, 1, 3, 0, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 8730993.835884217, new int[] { 0, 0, 0, 2, 3, 0, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 9.85795222E8, new int[] { 0, 0, 0, 3, 3, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.2258628558792587E7, new int[] { 0, 0, 0, 3, 3, 2, 0, 1 }, false) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false) },
                /* 50 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 3.53744964E8, new int[] { 0, 0, 0, 1, 3, 0, 0, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 8730993.835884217, new int[] { 0, 0, 0, 2, 3, 0, 0, 1 }, true) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(true)).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, true) },

        });
    }

    public TestAnonymizationLDiversity(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }

}
