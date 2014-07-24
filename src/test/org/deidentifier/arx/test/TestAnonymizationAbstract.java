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
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for data transformations
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TestAnonymizationAbstract extends AbstractTest {

    /**
     * Represents a test case
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class ARXTestCase {

        public ARXConfiguration config;
        public String           dataset;
        public String           sensitiveAttribute;
        public double           optimalInformationLoss;
        public int[]            bestResult;
        public boolean          practical;
        public int[]            statistics;

        /**
         * Creates a new instance
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param bestResult
         * @param practical
         * @param statistics
         */
        public ARXTestCase(final ARXConfiguration config, final String sensitiveAttribute, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical, int[] statistics) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.bestResult = bestResult;
            this.practical = practical;
            this.statistics = statistics;
        }
        
        /**
         * Creates a new instance
         * @param config
         * @param dataset
         * @param optimalInformationLoss
         * @param bestResult
         * @param practical
         * @param statistics
         */
        public ARXTestCase(final ARXConfiguration config, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical, int[] statistics) {
            this(config, "", dataset, optimalInformationLoss, bestResult, practical, statistics);
        }

        /**
         * Creates a new instance
         * @param config
         * @param dataset
         * @param optimalInformationLoss
         * @param bestResult
         * @param practical
         */
        public ARXTestCase(final ARXConfiguration config, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical) {
            this(config, "", dataset, optimalInformationLoss, bestResult, practical, null);
        }

        /**
         * Creates a new instance
         * @param config
         * @param sensitiveAttribute
         * @param dataset
         * @param optimalInformationLoss
         * @param bestResult
         * @param practical
         */
        public ARXTestCase(final ARXConfiguration config, final String sensitiveAttribute, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical) {
            this(config, sensitiveAttribute, dataset, optimalInformationLoss, bestResult, practical, null);
        }
    }

    /** The test case*/
    protected final ARXTestCase testCase;

    /**
     * Creates a new instance
     * @param testCase
     */
    public TestAnonymizationAbstract(final ARXTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Returns the data object for the test case
     * @param testCase
     * @return
     * @throws IOException
     */
    public Data getDataObject(final ARXTestCase testCase) throws IOException {

        final Data data = Data.create(testCase.dataset, ';');

        // Read generalization hierachies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(testCase.dataset.substring(testCase.dataset.lastIndexOf("/") + 1, testCase.dataset.length() - 4) + "_hierarchy_(.)+.csv")) {
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

    @Override
    @Before
    public void setUp() {
        // Empty by design
        // We also intentionally don't call super.setUp()
    }

    @Test
    public void test() throws IOException {

        final Data data = getDataObject(testCase);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        testCase.config.setPracticalMonotonicity(testCase.practical);

        ARXResult result = anonymizer.anonymize(data, testCase.config);

        // check if no solution
        if (testCase.bestResult == null) {
            assertTrue(result.getGlobalOptimum() == null);
        } else {
            assertTrue(testCase.dataset + "-should: " + Arrays.toString(testCase.bestResult) + " is: " + Arrays.toString(result.getGlobalOptimum().getTransformation()), Arrays.equals(result.getGlobalOptimum().getTransformation(), testCase.bestResult));
            assertEquals(testCase.dataset + "-should: " + testCase.optimalInformationLoss + " is: " + result.getGlobalOptimum().getMinimumInformationLoss().getValue(), testCase.optimalInformationLoss, result.getGlobalOptimum().getMinimumInformationLoss().getValue());
        }

        // check if all anonymous nodes are checked if outliers are present and the metric is non-monotonic and no-practical monotonicity is assumed
        if (!testCase.practical && testCase.config.getAbsoluteMaxOutliers() != 0 && !testCase.config.getMetric().isMonotonic()) {
            for (ARXNode[] level : result.getLattice().getLevels()) {
                for (ARXNode arxNode : level) {
                    if (arxNode.isAnonymous() == Anonymity.ANONYMOUS) {
                        assertTrue(arxNode.isChecked());
                    }
                }
            }
        }
        
        if (testCase.statistics != null) {
            
            // collect statistics
            int[] statistics = new int[7];
            for (ARXNode[] level : result.getLattice().getLevels()) {
                for (ARXNode arxNode : level) {
                    statistics[0]++;
                    if (arxNode.isChecked()) {
                        statistics[1]++;
                    }
                    if (arxNode.isAnonymous() == Anonymity.ANONYMOUS) {
                        statistics[2]++;
                    }
                    if (arxNode.isAnonymous() == Anonymity.NOT_ANONYMOUS) {
                        statistics[3]++;
                    }
                    if (arxNode.isAnonymous() == Anonymity.PROBABLY_ANONYMOUS) {
                        statistics[4]++;
                    }
                    if (arxNode.isAnonymous() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
                        statistics[5]++;
                    }
                    if (arxNode.getMaximumInformationLoss() == arxNode.getMinimumInformationLoss()) {
                        statistics[6]++;
                    }
                }
            }
            
            // Print statistics
            // System.out.println("new int[] {" + Arrays.toString(statistics).substring(1, Arrays.toString(statistics).length() - 1) + "}");
            
            String algorithmConfiguration = getAlgorithmConfiguration(testCase.config);
            int diff = statistics[1] - testCase.statistics[1];
            assertEquals(algorithmConfiguration + ". Mismatch: number of transformations", testCase.statistics[0], statistics[0]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of anonymous transformations", testCase.statistics[2], statistics[2]);
            assertEquals(algorithmConfiguration + ". Mismatch: number of non-anonymous transformations", testCase.statistics[3], statistics[3]);
            assertTrue(algorithmConfiguration + ". Too many or too few checks. Expected: " + testCase.statistics[1] + " but was: " + statistics[1], diff <= 0 && diff >= -5);
        }
    }
    
    /**
     * Returns the configuration of FLASH
     * @param config
     * @return
     */
    private String getAlgorithmConfiguration(ARXConfiguration config){
        
        final String metric;
        if (config.getMetric().isMonotonic() || config.getMaxOutliers()==0d || config.isPracticalMonotonicity()) {
            metric = "monotonic";
        } else {
            metric = "non-monotonic";
        }
        final String criterion;
        if (config.isCriterionMonotonic() || config.isPracticalMonotonicity()) {
            criterion = "Fully-monotonic";
        } else if (!config.isCriterionMonotonic() && config.getMinimalGroupSize() != Integer.MAX_VALUE) {
            criterion = "Partially-monotonic";
        } else {
            criterion = "Non-monotonic";
        }
        return criterion+" criteria with "+metric+" metric";
    }
}
