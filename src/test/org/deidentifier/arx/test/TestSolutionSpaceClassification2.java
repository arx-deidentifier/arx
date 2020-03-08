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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
 * Tests the classification of the solution space
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestSolutionSpaceClassification2 extends AbstractAnonymizationTest {
   
    /**
     * Returns test cases
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addPrivacyModel(new EntropyLDiversity("occupation", 5)), "occupation", "./data/adult.csv", 228878.2039109517, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 }, false, new int[] { 4320, 2326, 397, 3407, 0, 0, 397 }, new String[0]) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addPrivacyModel(new RecursiveCLDiversity("Highest level of school completed", 4d, 5)), "Highest level of school completed", "./data/atus.csv", 3536911.5162082445, new int[] { 0, 4, 0, 0, 2, 0, 1, 2 }, true, new int[] { 8748, 150, 78, 72, 684, 7914, 78 }, new String[0]) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addPrivacyModel(new KAnonymity(100)), "./data/cup.csv", 1994002.8308631124, new int[] { 3, 4, 1, 1, 0, 4, 4, 4 }, false, new int[] { 45000, 2041, 2733, 41577, 0, 0, 1809 }) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createEntropyMetric(false)).addPrivacyModel(new DPresence(0.0, 0.2, DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))), "RAMNTALL", "./data/cup.csv", 128068.07605943311, new int[] { 2, 4, 1, 1, 0, 3, 4 }, false, new int[] { 9000, 8992, 1862, 7130, 0, 0, 1862 }, new String[0]) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric(false)).addPrivacyModel(new EqualDistanceTCloseness("EDUC", 0.2d)).addPrivacyModel(new KAnonymity(5)), "EDUC", "./data/ihis.csv", "1.4719292081181683E7", new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true, new int[] { 12960, 28, 6, 22, 102, 12830, 6 }) },
        });
    }
    
    /**
     * Creates a new instance
     *
     * @param testCase
     */
    public TestSolutionSpaceClassification2(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
