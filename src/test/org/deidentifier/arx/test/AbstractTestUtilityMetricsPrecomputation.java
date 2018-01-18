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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for utility metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractTestUtilityMetricsPrecomputation extends AbstractTest {
    
    /**
     * Represents a test case.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class ARXUtilityMetricsTestCase {
        
        /** Config */
        public ARXConfiguration config;
                                
        /** Dataset */
        public String           dataset;
                                
        /** Sensitive attribute */
        public String           sensitiveAttribute;
                                
        /** First model */
        public Metric<?>        m1;
                                
        /** Second model */
        public Metric<?>        m2;
                                
        /**
         * Creates a new instance.
         *
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param m1
         * @param m2
         */
        public ARXUtilityMetricsTestCase(final ARXConfiguration config,
                                         final String sensitiveAttribute,
                                         final String dataset,
                                         final Metric<?> m1,
                                         final Metric<?> m2) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.m1 = m1;
            this.m2 = m2;
        }
        
        /**
         * Returns a string description.
         *
         * @return
         */
        public String getDescription() {
            StringBuilder builder = new StringBuilder();
            builder.append("TestCase{\n");
            builder.append(" - Dataset: ").append(dataset).append("\n");
            builder.append(" - Sensitive: ").append(sensitiveAttribute).append("\n");
            builder.append(" - Suppression: ").append(config.getSuppressionLimit()).append("\n");
            builder.append(" - Metric1: ").append(m1.toString()).append("\n");
            builder.append(" - Metric2: ").append(m2.toString()).append("\n");
            builder.append(" - Criteria:\n");
            for (PrivacyCriterion c : config.getPrivacyModels()) {
                builder.append("   * ").append(c.toString()).append("\n");
            }
            builder.append("}");
            return builder.toString();
        }
        
        @Override
        public String toString() {
            return config.getPrivacyModels() + "-" + config.getSuppressionLimit() + "-" + config.getQualityModel() + "-" + dataset + "-PM:" +
                   config.isPracticalMonotonicity();
        }
    }
    
    /**
     * Returns the data object for the test case.
     *
     * @param testCase
     * @return
     * @throws IOException
     */
    public static Data getDataObject(final ARXUtilityMetricsTestCase testCase) throws IOException {
        
        final Data data = Data.create(testCase.dataset, StandardCharsets.UTF_8, ';');
        
        // Read generalization hierachies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(testCase.dataset.substring(testCase.dataset.lastIndexOf("/") + 1, testCase.dataset.length() - 4) +
                                 "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        final File testDir = new File(testCase.dataset.substring(0, testCase.dataset.lastIndexOf("/")));
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        
        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                final String attributeName = matcher.group(1);
                
                if (!attributeName.equalsIgnoreCase(testCase.sensitiveAttribute)) {
                    data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
                } else { // sensitive attribute
                    if (testCase.config.isPrivacyModelSpecified(LDiversity.class) || testCase.config.isPrivacyModelSpecified(TCloseness.class)) {
                        data.getDefinition().setAttributeType(attributeName, AttributeType.SENSITIVE_ATTRIBUTE);
                    }
                }
                
            }
        }
        
        return data;
    }
    
    /** The test case. */
    protected final ARXUtilityMetricsTestCase testcase;
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public AbstractTestUtilityMetricsPrecomputation(final ARXUtilityMetricsTestCase testCase) {
        this.testcase = testCase;
    }
    
    @Override
    @Before
    public void setUp() {
        // Empty by design
    }
    
    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        
        // Anonymize
        
        ARXConfiguration testcaseconfig = testcase.config;
        
        // Metric 1
        testcaseconfig.setQualityModel(testcase.m1);
        Data data1 = getDataObject(testcase);
        ARXAnonymizer anonymizer1 = new ARXAnonymizer();
        ARXResult result1 = anonymizer1.anonymize(data1, testcaseconfig);
        
        // Metric 2
        testcaseconfig.setQualityModel(testcase.m2);
        Data data2 = getDataObject(testcase);
        ARXAnonymizer anonymizer2 = new ARXAnonymizer();
        ARXResult result2 = anonymizer2.anonymize(data2, testcaseconfig);
        
        String loss1 = result1.getGlobalOptimum().getHighestScore().toString();
        String loss2 = result2.getGlobalOptimum().getHighestScore().toString();
        
        assertEquals("Metric value differs", loss1, loss2);
    }
}
