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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for data transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TestAnonymizationAbstract extends AbstractTest {

    /**
     * Represents a test case.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class ARXAnonymizationTestCase {

        /**  TODO */
        public ARXConfiguration config;
        
        /**  TODO */
        public String           dataset;
        
        /**  TODO */
        public String           sensitiveAttribute;
        
        /**  TODO */
        public String           optimalInformationLoss;
        
        /**  TODO */
        public int[]            optimalTransformation;
        
        /**  TODO */
        public boolean          practical;
        
        /**  TODO */
        public int[]            statistics;

        /**
         * Creates a new instance.
         *
         * @param config
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical) {
            this(config, "", dataset, optimalInformationLoss, optimalTransformation, practical, null);
        }

        /**
         * Creates a new instance.
         *
         * @param config
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         * @param statistics
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical,
                                        int[] statistics) {
            this(config, "", dataset, optimalInformationLoss, optimalTransformation, practical, statistics);
        }

        /**
         * Creates a new instance.
         *
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String sensitiveAttribute,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical) {
            this(config, sensitiveAttribute, dataset, optimalInformationLoss, optimalTransformation, practical, null);
        }

        /**
         * Creates a new instance.
         *
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         * @param statistics
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String sensitiveAttribute,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical,
                                        int[] statistics) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.optimalInformationLoss = String.valueOf(optimalInformationLoss);
            this.optimalTransformation = optimalTransformation;
            this.practical = practical;
            this.statistics = statistics;
        }

        /**
         * Creates a new instance.
         *
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String sensitiveAttribute,
                                        final String dataset,
                                        final String optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical) {
            this(config, sensitiveAttribute, dataset, optimalInformationLoss, optimalTransformation, practical, null);
        }

        /**
         * Creates a new instance.
         *
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         * @param statistics
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String sensitiveAttribute,
                                        final String dataset,
                                        final String optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical,
                                        int[] statistics) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.optimalTransformation = optimalTransformation;
            this.practical = practical;
            this.statistics = statistics;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return config.getCriteria() + "-" + config.getMaxOutliers() + "-" + config.getMetric() + "-" + dataset + "-PM:" +
                   config.isPracticalMonotonicity();
        }
    }

    /** The test case. */
    protected final ARXAnonymizationTestCase testCase;

    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public TestAnonymizationAbstract(final ARXAnonymizationTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Returns the data object for the test case.
     *
     * @param testCase
     * @return
     * @throws IOException
     */
    public static Data getDataObject(final ARXAnonymizationTestCase testCase) throws IOException {

        final Data data = Data.create(testCase.dataset, ';');

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

                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
                final String attributeName = matcher.group(1);

                if (!attributeName.equalsIgnoreCase(testCase.sensitiveAttribute)) {
                    data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
                } else { // sensitive attribute
                    if (testCase.config.containsCriterion(LDiversity.class) || testCase.config.containsCriterion(TCloseness.class)) {
                        data.getDefinition().setAttributeType(attributeName, AttributeType.SENSITIVE_ATTRIBUTE);
                    }
                }

            }
        }

        return data;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.test.AbstractTest#setUp()
     */
    @Override
    @Before
    public void setUp() {
        // Empty by design
        // We also intentionally don't call super.setUp()
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void test() throws IOException {

        final Data data = getDataObject(testCase);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        testCase.config.setPracticalMonotonicity(testCase.practical);

        ARXResult result = anonymizer.anonymize(data, testCase.config);

        // check if no solution
        if (testCase.optimalTransformation == null) {
            assertTrue(result.getGlobalOptimum() == null);
        } else {

            String lossActual = result.getGlobalOptimum().getMaximumInformationLoss().toString();
            String lossExpected = testCase.optimalInformationLoss;

            assertEquals(testCase.dataset + "-should: " + lossExpected + " is: " +
                         lossActual + "(" + result.getGlobalOptimum().getMinimumInformationLoss().toString() + ")",
                         lossExpected,
                         lossActual);

            if (!Arrays.equals(result.getGlobalOptimum().getTransformation(), testCase.optimalTransformation)) {
                System.err.println("Note: Information loss equals, but the optimum differs:");
                System.err.println("Should: " + Arrays.toString(testCase.optimalTransformation) + " is: " +
                                   Arrays.toString(result.getGlobalOptimum().getTransformation()));
                System.err.println("Test case: " + testCase.toString());
            }
        }

        if (testCase.statistics != null) {

            // Collect statistics
            int[] statistics = new int[7];
            for (ARXNode[] level : result.getLattice().getLevels()) {
                for (ARXNode arxNode : level) {
                    statistics[0]++;
                    if (arxNode.isChecked()) {
                        statistics[1]++;
                    }
                    if (arxNode.getAnonymity() == Anonymity.ANONYMOUS) {
                        statistics[2]++;
                    }
                    if (arxNode.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                        statistics[3]++;
                    }
                    if (arxNode.getAnonymity() == Anonymity.PROBABLY_ANONYMOUS) {
                        statistics[4]++;
                    }
                    if (arxNode.getAnonymity() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
                        statistics[5]++;
                    }
                    if (arxNode.getMaximumInformationLoss() == arxNode.getMinimumInformationLoss()) {
                        statistics[6]++;
                    }
                }
            }

            // Compare
            String algorithmConfiguration = getAlgorithmConfiguration(testCase.config);
            assertEquals(algorithmConfiguration + ". Mismatch: number of transformations", testCase.statistics[0], statistics[0]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of checks", testCase.statistics[1], statistics[1]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of anonymous transformations", testCase.statistics[2], statistics[2]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of non-anonymous transformations", testCase.statistics[3], statistics[3]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of probably anonymous transformations", testCase.statistics[4], statistics[4]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of probably non-anonymous transformations", testCase.statistics[5], statistics[5]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of transformations with utility available", testCase.statistics[6], statistics[6]);
        }
    }

    /**
     * Returns the configuration of FLASH.
     *
     * @param config
     * @return
     */
    private String getAlgorithmConfiguration(ARXConfiguration config) {

        final String metric;
        if (config.getMetric().isMonotonic() || config.getMaxOutliers() == 0d || config.isPracticalMonotonicity()) {
            metric = "monotonic";
        } else {
            metric = "non-monotonic";
        }
        final String criterion;
        if (config.isCriterionMonotonic() || config.isPracticalMonotonicity()) {
            criterion = "Fully-monotonic";
        } else if (!config.isCriterionMonotonic() &&
                   (config.containsCriterion(KAnonymity.class) || config.containsCriterion(LDiversity.class))) {
            criterion = "Partially-monotonic";
        } else {
            criterion = "Non-monotonic";
        }
        return criterion + " criteria with " + metric + " metric";
    }

    /**
     * Transforms it into a string representation.
     *
     * @param classification
     * @return
     */
    @SuppressWarnings("unused")
    private String getClassification(int[] classification) {
        StringBuilder builder = new StringBuilder();
        builder.append("Classification {\n");
        builder.append(" Transformations: ").append(classification[0]).append("\n");
        builder.append(" Checked: ").append(classification[1]).append("\n");
        builder.append(" Anonymous: ").append(classification[2]).append("\n");
        builder.append(" Non-anonymous: ").append(classification[3]).append("\n");
        builder.append(" Probably anonymous: ").append(classification[4]).append("\n");
        builder.append(" Probably non-anonymous: ").append(classification[5]).append("\n");
        builder.append(" Utility available: ").append(classification[6]).append("\n");
        builder.append("}");
        return builder.toString();
    }
}
