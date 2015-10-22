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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Test for data transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractAnonymizationTest extends AbstractTest {
    
    /**
     * Represents a test case.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class ARXAnonymizationTestCase {
        
        /** TODO */
        private static int counter;
        
        /** TODO */
        public final int id = counter++;
        
        /** TODO */
        public ARXConfiguration config;
        
        /** TODO */
        public String dataset;
        
        /** TODO */
        public String sensitiveAttribute;
        
        /** TODO */
        public String optimalInformationLoss;
        
        /** TODO */
        public int[] optimalTransformation;
        
        /** TODO */
        public boolean practical;
        
        /** TODO */
        public int[] statistics;
        
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
        
        @Override
        public String toString() {
            return config.getCriteria() + "-" + config.getMaxOutliers() + "-" + config.getMetric() + "-" + dataset + "-PM:" +
                   config.isPracticalMonotonicity();
        }
    }
    
    private static final String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
    
    /**
     * Transforms it into a string representation.
     *
     * @param classification
     * @return
     */
    public static String getClassification(int[] classification) {
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
    
    /** The test case. */
    protected final ARXAnonymizationTestCase testCase;
    
    /** To access the test name */
    @Rule
    public TestName name = new TestName();
    
    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public AbstractAnonymizationTest(final ARXAnonymizationTestCase testCase) {
        this.testCase = testCase;
    }
    
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

        System.out.println(testCase.toString());

        for (int threads = 1; threads <= 8; threads++) {

            final Data data = getDataObject(testCase);

            // Create an instance of the anonymizer
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            testCase.config.setPracticalMonotonicity(testCase.practical);
//            testCase.config.setNumThreads(threads);

            // Test or warmup
            ARXResult result = anonymizer.anonymize(data, testCase.config);

            final int REPETITIONS = 5;
            long time = System.currentTimeMillis();
            long time2 = 0;
            for (int i = 0; i < REPETITIONS; i++) {
                data.getHandle().release();
                result = anonymizer.anonymize(data, testCase.config);
                time2 += result.getTime();
            }
            time = (System.currentTimeMillis() - time) / REPETITIONS;
            time2 /= REPETITIONS;

            System.out.println(" - " + threads + ": " + time + "[ms] / " + time2 + "[ms]");
        }
    }
    
    /**
     * Appends the given value to the file
     * @param value
     * @param file
     */
    private void appendToFile(String value, String file) {
        Writer writer = null;
        try {
            createHeader(file);
            writer = new FileWriter(file, true);
            writer = new BufferedWriter(writer);
            writer.write(value);
            writer.write(System.lineSeparator());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Writes a header to the given file
     * @param file
     */
    private void createHeader(String file) {
        File f = new File(file);
        if (!f.exists()) {
            Writer writer = null;
            try {
                writer = new FileWriter(f);
                writer = new BufferedWriter(writer);
                
                StringBuilder line = new StringBuilder();
                line.append("");
                line.append(";");
                line.append("");
                line.append(";");
                line.append("");
                line.append(";");
                line.append("");
                line.append(";");
                line.append("Execution time");
                line.append(";");
                line.append("Internal execution time");
                writer.write(line.toString());
                writer.write(System.lineSeparator());
                
                line = new StringBuilder();
                line.append("Version");
                line.append(";");
                line.append("Git commit");
                line.append(";");
                line.append("Test");
                line.append(";");
                line.append("Testid");
                line.append(";");
                line.append("Arithmetic Mean");
                line.append(";");
                line.append("Arithmetic Mean");
                writer.write(line.toString());
                writer.write(System.lineSeparator());
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        }
    }
    
    /**
     * Returns the configuration of FLASH.
     *
     * @param config
     * @return
     */
    private String getAlgorithmConfiguration(ARXConfiguration config) {
        return config.getMonotonicityOfPrivacy() + " monotonicity of privacy with " + config.getMonotonicityOfUtility() + " monotonicity of utility";
    }
}
