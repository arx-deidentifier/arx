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
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
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

        /** Random test variable */
        private static int                      counter;
        /** Random test variable */
        public final int                        id                 = counter++;
        /** Random test variable */
        public ARXConfiguration                 config;
        /** Random test variable */
        public String                           dataset;
        /** Random test variable */
        public String                           sensitiveAttribute;
        /** Random test variable */
        public String[]                         responseAttributes = new String[0];
        /** Random test variable */
        public String                           optimalInformationLoss;
        /** Random test variable */
        public int[]                            optimalTransformation;
        /** Random test variable */
        public boolean                          practical;
        /** Random test variable */
        public int[]                            statistics;
        /** Random test variable */
        public int                              hashcode           = -1;
        /** Random test variable */
        public boolean                          optimizable        = false;
        /** Hierarchy builders */
        public Map<String, HierarchyBuilder<?>> builders;
                                 
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
            this(config, "", dataset, optimalInformationLoss, optimalTransformation, practical, null, new String[0]);
        }
        
        /**
         * Creates a new instance.
         *
         * @param config
         * @param dataset
         * @param optimalInformationLoss
         * @param optimalTransformation
         * @param practical
         * @param responseAttributes
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical,
                                        final String[] responseAttributes) {
            this(config, "", dataset, optimalInformationLoss, optimalTransformation, practical, null, responseAttributes);
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
            this(config, "", dataset, optimalInformationLoss, optimalTransformation, practical, statistics, new String[0]);
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
            this(config, sensitiveAttribute, dataset, optimalInformationLoss, optimalTransformation, practical, null, new String[0]);
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
         * @param responseAttributes
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String sensitiveAttribute,
                                        final String dataset,
                                        final double optimalInformationLoss,
                                        final int[] optimalTransformation,
                                        final boolean practical,
                                        int[] statistics,
                                        String[] responseAttributes) {
            this.config = config;
            this.sensitiveAttribute = sensitiveAttribute;
            this.dataset = dataset;
            this.optimalInformationLoss = String.valueOf(optimalInformationLoss);
            this.optimalTransformation = optimalTransformation;
            this.practical = practical;
            this.statistics = statistics;
            this.responseAttributes = responseAttributes;
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
        
        /**
         * Constructor for local recoding tests
         * @param config
         * @param dataset
         * @param sensitiveAttribute
         * @param hashcode
         */
        public ARXAnonymizationTestCase(final ARXConfiguration config,
                                        final String dataset,
                                        final String sensitiveAttribute,
                                        final int hashcode) {
            this.config = config;
            this.dataset = dataset;
            this.sensitiveAttribute = sensitiveAttribute;
            this.hashcode = hashcode;
        }
        
        /**
         * Creates a new test case with hierarchy builders
         * @param config
         * @param dataset
         * @param hierarchyBuilders
         * @param informationLoss
         * @param transformation
         * @param practicalMonotonicity
         */
        public ARXAnonymizationTestCase(ARXConfiguration config,
                                        String dataset,
                                        Map<String, HierarchyBuilder<?>> hierarchyBuilders,
                                        double informationLoss,
                                        int[] transformation,
                                        boolean practicalMonotonicity) {
            this(config, dataset, informationLoss, transformation, practicalMonotonicity, new String[0]);
            this.builders = hierarchyBuilders;
        }

        @Override
        public String toString() {
            return config.getPrivacyModels() + "-" + config.getSuppressionLimit() + "-" + config.getQualityModel() + "-" + dataset + "-PM:" +
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
                
                for (String responseAttribute : testCase.responseAttributes) {
                    if (attributeName.equalsIgnoreCase(responseAttribute)) {
                        data.getDefinition().setResponseVariable(attributeName, true);
                    }
                }
                
                if (!attributeName.equalsIgnoreCase(testCase.sensitiveAttribute)) {
                    data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
                } else { // sensitive attribute
                    if (testCase.config.isPrivacyModelSpecified(LDiversity.class) || 
                        testCase.config.isPrivacyModelSpecified(TCloseness.class) || 
                        testCase.config.isPrivacyModelSpecified(DDisclosurePrivacy.class) ||
                        testCase.config.isPrivacyModelSpecified(BasicBLikeness.class) ||
                        testCase.config.isPrivacyModelSpecified(EnhancedBLikeness.class)) {
                        data.getDefinition().setAttributeType(attributeName, AttributeType.SENSITIVE_ATTRIBUTE);
                    }
                }
                
            }
        }
        
        if (testCase.builders != null) {

            // Remove all QIs
            Set<String> qis = data.getDefinition().getQuasiIdentifyingAttributes();
            for (String qi : qis) {
                data.getDefinition().resetHierarchy(qi);
                data.getDefinition().setAttributeType(qi, AttributeType.INSENSITIVE_ATTRIBUTE);
            }
            
            // Set only builders as QIs
            for (Entry<String, HierarchyBuilder<?>> entry : testCase.builders.entrySet()){
                data.getDefinition().setAttributeType(entry.getKey(), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
                data.getDefinition().setHierarchy(entry.getKey(), entry.getValue());
            }
        }
        
        return data;
    }
    
    /** The test case. */
    protected final ARXAnonymizationTestCase testCase;
                                             
    /** To access the test name */
    @Rule
    public TestName                          name = new TestName();
                                                  
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
        
        boolean benchmark = false;
        List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String argument : arguments) {
            if (argument.startsWith("-DBenchmark")) {
                benchmark = true;
                break;
            }
        }
        
        final Data data = getDataObject(testCase);
        
        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        testCase.config.setPracticalMonotonicity(testCase.practical);
        
        // Test or warmup
        ARXResult result = anonymizer.anonymize(data, testCase.config);
        DataHandle output = null;
        if (testCase.hashcode != -1) {
            try {
                output = result.getOutput();
                result.optimizeIterative(output, 0.05d, 100, 0.05d);
            } catch (RollbackRequiredException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Benchmark
        if (benchmark) {
            
            String version = System.getProperty("Version");
            String path = System.getProperty("Benchmark");
            if (path == null || path.length() == 0) {
                path = ".";
            }
            String testClass = this.getClass().getSimpleName();
            
            final int REPETITIONS = 5;
            long time = System.currentTimeMillis();
            long time2 = 0;
            for (int i = 0; i < REPETITIONS; i++) {
                data.getHandle().release();
                result = anonymizer.anonymize(data, testCase.config);
                if (testCase.hashcode != -1) {
                    try {
                        output = result.getOutput();
                        result.optimizeIterative(output, 0.05d, 100, 0.05d);
                    } catch (RollbackRequiredException e) {
                        throw new RuntimeException(e);
                    }
                }
                time2 += result.getTime();
            }
            time = (System.currentTimeMillis() - time) / REPETITIONS;
            time2 /= REPETITIONS;
            
            StringBuilder line = new StringBuilder();
            line.append(Resources.getVersion());
            line.append(";");
            line.append(version);
            line.append(";");
            line.append(testClass);
            line.append(";");
            line.append(testCase.id);
            line.append(";");
            line.append(time);
            line.append(";");
            line.append(time2);
            output(line.toString(), path + "/benchmark_" + version + "_" + timestamp + "_" + testClass + ".csv");
        }
        
        // Check if local recoding experiment
        if (testCase.hashcode != -1) {
            
            // Compute hashcode of result
            int hashcode = 23;
            for (int row = 0; row < output.getNumRows(); row++) {
                for (int column = 0; column < output.getNumColumns(); column++) {
                    hashcode = (37 * hashcode) + output.getValue(row, column).hashCode();
                }
            }
            
            // Assert
            assertEquals("Hash code not equal", hashcode, testCase.hashcode);
            return;
        }

        // Check if no solution
        if (testCase.optimalTransformation == null) {
            assertTrue(result.getGlobalOptimum() == null);
        } else {
            
            String lossActual = result.getGlobalOptimum().getHighestScore().toString();
            String lossExpected = testCase.optimalInformationLoss;
            
            assertEquals(testCase.dataset + "-should: " + lossExpected + " is: " +
                         lossActual + "(" + result.getGlobalOptimum().getLowestScore().toString() + ")",
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
            
            // Expand
            for (int level = 0; level < result.getLattice().getLevels().length; level++) {
                for (ARXNode node : result.getLattice().getLevels()[level]) {
                    node.expand();
                }
            }
            
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
                    if (arxNode.getHighestScore() == arxNode.getLowestScore()) {
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
     * Appends the given value to the file
     * @param value
     * @param file
     */
    private void output(String value, String file) {
        Writer writer = null;
        try {
            createHeader(file);
            writer = new FileWriter(file, true);
            writer = new BufferedWriter(writer);
            writer.write(value);
            writer.write(System.lineSeparator());
            System.out.println(value);
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
                line.append("Execution Time");
                line.append(";");
                line.append("Internal Execution Time");
                writer.write(line.toString());
                writer.write(System.lineSeparator());
                
                line = new StringBuilder();
                line.append("Version");
                line.append(";");
                line.append("Git Commit");
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
