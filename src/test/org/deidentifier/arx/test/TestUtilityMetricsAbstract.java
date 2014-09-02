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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for utility metrics
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TestUtilityMetricsAbstract extends AbstractTest {

    /**
     * Represents a test case
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class ARXUtilityMetricsTestCase {

		public ARXConfiguration config;
		public String dataset;
		public String sensitiveAttribute;
		public Map<String, String> informationLoss;

        /**
         * Creates a new instance
         * 
         * @param config
         * @param dataset
         * @param sensitiveAttribute
         * @param informationLoss pairs of (Arrays.toString(transformation), informationLoss.toString())
         */
        public ARXUtilityMetricsTestCase(final ARXConfiguration config,
        								 final String sensitiveAttribute,
        								 final String dataset,
                                         final String... informationLoss) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.informationLoss = new HashMap<String, String>();
            if (informationLoss != null) {
	            for (int i=0; i<informationLoss.length; i+=2) {
	            	this.informationLoss.put(informationLoss[i], informationLoss[i+1]);
	            }
            }
        }
        
        /**
         * Returns a string description
         * @return
         */
        public String getDescription(){
        	StringBuilder builder = new StringBuilder();
        	builder.append("TestCase{\n");
        	builder.append(" - Dataset: ").append(dataset).append("\n");
        	builder.append(" - Sensitive: ").append(sensitiveAttribute).append("\n");
        	builder.append(" - Suppression: ").append(config.getMaxOutliers()).append("\n");
        	builder.append(" - Metric: ").append(config.getMetric().toString()).append("\n");
        	builder.append(" - Criteria:\n");
        	for (PrivacyCriterion c : config.getCriteria()) {
        		builder.append("   * ").append(c.toString()).append("\n");
        	}
        	builder.append("}");
        	return builder.toString();
        }
    }

    /** The test case */
    protected final ARXUtilityMetricsTestCase testcase;

    /**
     * Creates a new instance
     * 
     * @param testCase
     */
    public TestUtilityMetricsAbstract(final ARXUtilityMetricsTestCase testCase) {
        this.testcase = testCase;
    }

    /**
     * Returns the data object for the test case
     * 
     * @param testCase
     * @return
     * @throws IOException
     */
    public Data getDataObject(final ARXUtilityMetricsTestCase testCase) throws IOException {

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

    @Override
    @Before
    public void setUp() {
        // Empty by design
    }

    @Test
    public void test() throws IOException {

    	// Anonymize
        Data data = getDataObject(testcase);
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(data, testcase.config);

        // Test information loss for some transformations
        for (ARXNode[] level : result.getLattice().getLevels()) {
        	for (ARXNode node : level){
        		
        		String label = Arrays.toString(node.getTransformation());
        		String loss = testcase.informationLoss.get(label);

        		if (loss != null) {
        			if (node.getMaximumInformationLoss().compareTo(node.getMinimumInformationLoss())!=0) {
            			result.getOutput(node);
            		}
        			assertEquals(label, loss, node.getMaximumInformationLoss().toString());
        		}
        	}
        }
    }
}
