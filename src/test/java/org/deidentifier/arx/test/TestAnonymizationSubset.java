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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for anonymization of data subsets.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationSubset extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KAnonymity(5)).addPrivacyModel(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("./data/adult_hierarchy_occupation.csv", StandardCharsets.UTF_8, ';'))).addPrivacyModel(new Inclusion(getSubset(20000))), "occupation", "./data/adult.csv", 178437.4164900378, new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new KAnonymity(5)).addPrivacyModel(new Inclusion(getSubset(10000))), "occupation", "./data/adult.csv", 70774.7774633781, new int[] { 0, 4, 1, 1, 2, 2, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("./data/adult_hierarchy_occupation.csv", StandardCharsets.UTF_8, ';'))).addPrivacyModel(new Inclusion(getSubset(30000))), "occupation", "./data/adult.csv", 250816.4033099704, new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new RecursiveCLDiversity("occupation", 4.0, 5)).addPrivacyModel(new Inclusion(getSubset(9000))), "occupation", "./data/adult.csv", 65996.221545847, new int[] { 1, 2, 1, 1, 3, 2, 2, 1 }, false) },
        });
    }
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationSubset(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
    /**
     * Returns a random subset of the given size
     * @param size
     * @return
     */
    private static DataSubset getSubset(int size) {
        Set<Integer> set = new HashSet<Integer>();
        Random random = new Random(0xDEADBEEF);
        for (int i = 0; i < size; i++) {
            set.add(random.nextInt(size));
        }
        return DataSubset.create(30162, set);
    }
}
