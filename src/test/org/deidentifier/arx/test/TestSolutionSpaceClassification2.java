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
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestSolutionSpaceClassification2 extends TestAnonymizationAbstract {

    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EntropyLDiversity("occupation", 5)), "occupation", "../arx-data/data-junit/adult.csv", 228878.2039109517, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, false, new int[] {4320, 2249, 367, 3374, 0, 0, 367}) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 3536911.5162082445, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, true, new int[] {8748, 148, 77, 71, 685, 7915, 77}) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 1994002.8308631124, new int[] { 3, 4, 1, 1, 0, 4, 4, 4 }, false, new int[] {45000, 2041, 2733, 41577, 0, 0, 1809}) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createEntropyMetric(false)).addCriterion(new DPresence(0.0, 0.2, DataSubset.create(Data.create("../arx-data/data-junit/cup.csv", ';'), Data.create("../arx-data/data-junit/cup_subset.csv", ';')))), "RAMNTALL", "../arx-data/data-junit/cup.csv", 128068.07605943311, new int[] { 2, 4, 1, 1, 0, 3, 4 }, false, new int[] {9000, 8992, 1862, 7130, 0, 0, 1862}) },
                { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true, new int[] {12960, 27, 6, 21, 102, 12831, 6}) },                
        });
    }

    /**
     * 
     *
     * @param testCase
     */
    public TestSolutionSpaceClassification2(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
