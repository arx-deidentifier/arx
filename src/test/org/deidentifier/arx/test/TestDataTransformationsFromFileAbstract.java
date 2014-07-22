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
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.ARXLattice.ARXNode;
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
public abstract class TestDataTransformationsFromFileAbstract extends AbstractTest {

    public static class TestCaseResult {

        public ARXConfiguration config;
        public String           dataset;
        public String           sensitiveAttribute;
        public double           optimalInformationLoss;
        public int[]            bestResult;
        public boolean          practical;

        public TestCaseResult(final ARXConfiguration config, final String sensitiveAttribute, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.bestResult = bestResult;
            this.practical = practical;
        }

        public TestCaseResult(final ARXConfiguration config, final String dataset, final double optimalInformationLoss, final int[] bestResult, final boolean practical) {
            this(config, "", dataset, optimalInformationLoss, bestResult, practical);
        }
    }

    protected final TestCaseResult testCase;

    public TestDataTransformationsFromFileAbstract(final TestCaseResult testCase) {
        this.testCase = testCase;
    }

    public Data createDataObject(final TestCaseResult testCase) throws IOException {

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
    public void testTestCases() throws IOException {

        final Data data = createDataObject(testCase);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        testCase.config.setPracticalMonotonicity(testCase.practical);

        ARXResult result = anonymizer.anonymize(data, testCase.config);

        // check if no solution possible
        if (testCase.bestResult == null) {
            assertTrue(result.getGlobalOptimum() == null);
        } else {
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

            assertTrue(testCase.dataset + "-should: " + Arrays.toString(testCase.bestResult) + " is: " + Arrays.toString(result.getGlobalOptimum().getTransformation()), Arrays.equals(result.getGlobalOptimum().getTransformation(), testCase.bestResult));

            assertEquals(testCase.dataset + "-should: " + testCase.optimalInformationLoss + " is: " + result.getGlobalOptimum().getMinimumInformationLoss().getValue(), testCase.optimalInformationLoss, result.getGlobalOptimum().getMinimumInformationLoss().getValue());
        }

    }

}
