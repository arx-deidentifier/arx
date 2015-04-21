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
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Microaggregation;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Test;

/**
 * Test for risk metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestMicroaggregation extends AbstractTest {
    
    /**
     * Returns the data object for a given dataset
     *
     * @param dataset the dataset
     * @return the data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Data getDataObject(final String dataset) throws IOException {
        
        final Data data = Data.create(dataset, ';');
        
        // Read generalization hierachies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(dataset.substring(dataset.lastIndexOf("/") + 1, dataset.length() - 4) + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        final File testDir = new File(dataset.substring(0, dataset.lastIndexOf("/")));
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        
        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
                final String attributeName = matcher.group(1);
                
                // use all found attribute hierarchies as qis
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
                
            }
        }
        
        return data;
    }
    
    /**
     * Test microaggregation arithmetic mean with larger dataset
     * @throws IOException
     */
    @Test
    public void testMicroaggregationAdult() throws IOException {
        Data data = getDataObject("../arx-data/data-junit/adult.csv");
        
        data.getDefinition().setAttributeType("age", Microaggregation.createArithmeticMean());
        data.getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(5));
        config.setMaxOutliers(1d);
        
        ARXResult result = anonymizer.anonymize(data, config);
        DataHandle exptectedOutput = Data.create("../arx-data/data-junit/adult_age_microaggregated.csv", ';').getHandle();
        
        DataHandle output = result.getOutput();
        for (int i = 0; i < output.getNumRows(); i++) {
            for (int j = 0; j < output.getNumColumns(); j++) {
                assertEquals(exptectedOutput.getValue(i, j), output.getValue(i, j));
            }
        }
        
    }
    
    /**
     * Test microaggregation arithmetic mean
     * @throws IOException
     */
    @Test
    public void testMicroaggregationArithmeticMean() throws IOException {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        provider.data.getDefinition().setAttributeType("age", Microaggregation.createArithmeticMean());
        provider.data.getDefinition().setAttributeType("gender", provider.getGender());
        provider.data.getDefinition().setAttributeType("zipcode", provider.getZipcode());
        
        provider.data.getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        
        ARXResult result = anonymizer.anonymize(provider.data, config);
        
        final String[][] resultArray = resultToArray(result);
        
        final String[][] expectedArray =
        { { "age", "gender", "zipcode" },
                { "54", "male", "81***" },
                { "50", "female", "81***" },
                { "54", "male", "81***" },
                { "50", "female", "81***" },
                { "50", "female", "81***" },
                { "54", "male", "81***" },
                { "54", "male", "81***" } };
        
        assertTrue(Arrays.deepEquals(resultArray, expectedArray));
    }
    
    /**
     * Test microaggregation geometric mean
     * @throws IOException
     */
    @Test
    public void testMicroaggregationGeometricMean() throws IOException {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        provider.data.getDefinition().setAttributeType("age", Microaggregation.createGeometricMean());
        provider.data.getDefinition().setAttributeType("gender", provider.getGender());
        provider.data.getDefinition().setAttributeType("zipcode", provider.getZipcode());
        
        provider.data.getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        
        ARXResult result = anonymizer.anonymize(provider.data, config);
        
        final String[][] resultArray = resultToArray(result);
        
        final String[][] expectedArray =
        { { "age", "gender", "zipcode" },
                { "52", "male", "81***" },
                { "47", "female", "81***" },
                { "52", "male", "81***" },
                { "47", "female", "81***" },
                { "47", "female", "81***" },
                { "52", "male", "81***" },
                { "52", "male", "81***" } };
        
        assertTrue(Arrays.deepEquals(resultArray, expectedArray));
    }
}
