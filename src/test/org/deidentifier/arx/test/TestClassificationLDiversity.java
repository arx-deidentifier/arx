/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
public class TestClassificationLDiversity extends TestAnonymizationAbstract {

    @Parameters
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 228878.2039109519d, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, false, new int[] { 4320, 2781, 595, 3725, 0, 0, 596 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, false, new int[] { 4320, 606, 0, 4320, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 324620.5269918695d, new int[] { 1, 1, 1, 1, 3, 2, 2, 1 }, false, new int[] { 4320, 100, 33, 4287, 0, 0, 25 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, false, new int[] { 4320, 6, 0, 4320, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 228878.2039109519d, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, true, new int[] { 4320, 468, 221, 263, 367, 3469, 222 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EntropyLDiversity("occupation", 100)), "occupation", "../arx-data/data-junit/adult.csv", 0.0d, null, true, new int[] { 4320, 6, 0, 44, 0, 4276, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 9.123049999E9d, new int[] { 0, 3, 0, 0, 2, 0, 1, 0 }, false, new int[] { 8748, 190, 1501, 7247, 0, 0, 98 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false, new int[] { 8748, 6, 0, 8748, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 3536911.5162082445d, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, false, new int[] { 8748, 8347, 764, 7984, 0, 0, 765 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false, new int[] { 8748, 4106, 0, 8748, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 2.0146560117E10d, new int[] { 0, 3, 0, 2, 2, 2, 2, 1 }, false, new int[] { 8748, 64, 59, 8689, 0, 0, 21 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false, new int[] { 8748, 6, 0, 8748, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 4912828.240033204d, new int[] { 0, 4, 0, 2, 2, 2, 2, 2 }, false, new int[] { 8748, 42, 29, 8719, 0, 0, 11 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, false, new int[] { 8748, 6, 0, 8748, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 9.123049999E9d, new int[] { 0, 3, 0, 0, 2, 0, 1, 0 }, true, new int[] { 8748, 190, 97, 96, 1404, 7151, 98 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new DistinctLDiversity("Highest level of school completed", 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, true, new int[] { 8748, 6, 0, 58, 0, 8690, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 3536911.5162082445d, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, true, new int[] { 8748, 150, 78, 75, 684, 7911, 79 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 0.0d, null, true, new int[] { 8748, 6, 0, 58, 0, 8690, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2823649.0d, new int[] { 4, 0, 0, 1, 1, 3, 1 }, false, new int[] { 9000, 967, 464, 6762, 1774, 0, 465 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 8.8290083E7d, new int[] { 3, 4, 1, 2, 1, 2, 1 }, false, new int[] { 9000, 791, 224, 8684, 92, 0, 225 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1244350.703466948d, new int[] { 2, 4, 0, 1, 0, 3, 1 }, false, new int[] { 9000, 511, 2458, 6542, 0, 0, 273 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1552136.4930241709d, new int[] { 3, 4, 1, 2, 0, 2, 1 }, false, new int[] { 9000, 238, 570, 8430, 0, 0, 121 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 3.01506905E8d, new int[] { 4, 4, 1, 1, 1, 4, 4 }, false, new int[] { 9000, 34, 9, 8991, 0, 0, 6 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 6.19637215E8d, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false, new int[] { 9000, 25, 3, 8997, 0, 0, 3 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1961244.482255955d, new int[] { 4, 4, 1, 1, 1, 4, 4 }, false, new int[] { 9000, 34, 9, 8991, 0, 0, 6 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2032837.6390798881d, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false, new int[] { 9000, 25, 3, 8997, 0, 0, 3 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2823649.0d, new int[] { 4, 0, 0, 1, 1, 3, 1 }, true, new int[] { 9000, 375, 197, 4424, 2041, 2338, 198 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EntropyLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 8.8290083E7d, new int[] { 3, 4, 1, 2, 1, 2, 1 }, true, new int[] { 9000, 128, 52, 2283, 264, 6401, 53 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1244350.703466948d, new int[] { 2, 4, 0, 1, 0, 3, 1 }, true, new int[] { 9000, 511, 272, 5861, 2186, 681, 273 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new DistinctLDiversity("RAMNTALL", 100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1552136.4930241709d, new int[] { 3, 4, 1, 2, 0, 2, 1 }, true, new int[] { 9000, 238, 120, 4987, 450, 3443, 121 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 590389.0228300439d, new int[] { 0, 2, 1, 1, 0, 2, 0 }, false, new int[] { 5184, 1867, 919, 1818, 2447, 0, 920 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false, new int[] { 5184, 1870, 0, 5184, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 629604.8933555635d, new int[] { 0, 2, 1, 1, 0, 2, 0 }, false, new int[] { 5184, 4314, 3648, 1536, 0, 0, 3649 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false, new int[] { 5184, 1870, 0, 5184, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 1201007.0880104564d, new int[] { 0, 2, 3, 3, 1, 2, 2 }, false, new int[] { 5184, 38, 12, 5172, 0, 0, 10 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false, new int[] { 5184, 6, 0, 5184, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 1201007.0880104564d, new int[] { 0, 2, 3, 3, 1, 2, 2 }, false, new int[] { 5184, 64, 29, 5155, 0, 0, 18 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, false, new int[] { 5184, 6, 0, 5184, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 5)), "istatenum", "../arx-data/data-junit/fars.csv", 590389.0228300439d, new int[] { 0, 2, 1, 1, 0, 2, 0 }, true, new int[] { 5184, 720, 346, 439, 3020, 1379, 347 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new RecursiveCLDiversity("istatenum", 4d, 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, true, new int[] { 5184, 6, 0, 6, 0, 5178, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 5)), "istatenum", "../arx-data/data-junit/fars.csv", 629604.8933555635d, new int[] { 0, 2, 1, 1, 0, 2, 0 }, true, new int[] { 5184, 681, 368, 451, 3280, 1085, 369 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new DistinctLDiversity("istatenum", 100)), "istatenum", "../arx-data/data-junit/fars.csv", 0.0d, null, true, new int[] { 5184, 6, 0, 6, 0, 5178, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 3.53744964E8d, new int[] { 0, 0, 0, 1, 3, 0, 0, 1 }, false, new int[] { 12960, 9478, 2079, 7868, 3013, 0, 2080 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false, new int[] { 12960, 8229, 0, 12960, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 8730993.835884217d, new int[] { 0, 0, 0, 2, 3, 0, 0, 1 }, false, new int[] { 12960, 9527, 2072, 7924, 2964, 0, 2073 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false, new int[] { 12960, 8229, 0, 12960, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 9.85795222E8d, new int[] { 0, 0, 0, 3, 3, 2, 0, 1 }, false, new int[] { 12960, 128, 423, 12537, 0, 0, 54 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false, new int[] { 12960, 6, 0, 12960, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.2258628558792587E7d, new int[] { 0, 0, 0, 3, 3, 2, 0, 1 }, false, new int[] { 12960, 124, 404, 12556, 0, 0, 51 }) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, false, new int[] { 12960, 6, 0, 12960, 0, 0, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 3.53744964E8d, new int[] { 0, 0, 0, 1, 3, 0, 0, 1 }, true, new int[] { 12960, 256, 181, 86, 4911, 7782, 182 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new RecursiveCLDiversity("EDUC", 4d, 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, true, new int[] { 12960, 6, 0, 64, 0, 12896, 1 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 5)), "EDUC", "../arx-data/data-junit/ihis.csv", 8730993.835884217d, new int[] { 0, 0, 0, 2, 3, 0, 0, 1 }, true, new int[] { 12960, 331, 228, 106, 4808, 7818, 229 }) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EntropyLDiversity("EDUC", 100)), "EDUC", "../arx-data/data-junit/ihis.csv", 0.0d, null, true, new int[] { 12960, 6, 0, 64, 0, 12896, 1 }) },

        });
    }

    public TestClassificationLDiversity(final ARXTestCase testCase) {
        super(testCase);
    }

}
