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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test domain share model
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestDomainShare extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     *
     * @return
     * @throws IOException 
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", getHierarchyBuilders(), 2.3208009999997614E-4 , new int[]{0}, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", getHierarchyBuilders(), 0.02048935740000002 , new int[]{0}, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", getHierarchyBuilders(), 2.3208009999997614E-4 , new int[]{0}, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", getHierarchyBuilders(), 0.02048935740000002 , new int[]{0}, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", getHierarchyBuilders(), 2.3208009999997614E-4 , new int[]{0}, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", getHierarchyBuilders(), 0.02048935740000002 , new int[]{0}, false) },                                                                                                                  
        });
    }
    
    /**
     * Return a map of relevant hierarchy builders
     * @return
     * @throws IOException 
     */
    private static Map<String, HierarchyBuilder<?>> getHierarchyBuilders() throws IOException {
        Map<String, HierarchyBuilder<?>> map = new HashMap<String, HierarchyBuilder<?>>();
        map.put("age", HierarchyBuilder.create(new File("data/hierarchy-age.ahs")));
        return map;
    }

    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestDomainShare(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
