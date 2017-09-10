/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsClassification;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Test;

/**
 * Test for statistical classification
 * 
 * @author Johanna Eicher
 */
public class TestClassification {

    /** Result */
    private ARXResult result;

    /**
     * @return the class
     */
    private String getClazz() {
        return "marital-status";
    }

    /**
     * Loads a dataset from disk
     * 
     * @param dataset
     * @return
     * @throws IOException
     */
    private Data getData(final String dataset) throws IOException {

        // Load data
        Data data = Data.create("data/" + dataset + ".csv", StandardCharsets.UTF_8, ';');

        // Read generalization hierarchies
        FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        // Create definition
        File testDir = new File("data/");
        File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        for (File file : genHierFiles) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName,
                                                      Hierarchy.create(hier.getHierarchy()));
            }
        }

        return data;
    }

    /**
     * @return the features
     */
    private String[] getFeatures() {
        return new String[] { "sex",
                              "age",
                              "race",
                              "marital-status",
                              "education",
                              "native-country",
                              "workclass",
                              "occupation",
                              "salary-class" };
    }

    /**
     * Performs anonymization and returns result.
     * 
     * @return
     * @throws IOException
     */
    private ARXResult getResult() throws IOException {
        
        if (result == null) {

            // Data
            Data data = getData("adult");
            data.getDefinition().setAttributeType("marital-status", AttributeType.INSENSITIVE_ATTRIBUTE);
            data.getDefinition().setDataType("age", DataType.INTEGER);

            // Config
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(5));
            config.setMaxOutliers(1d);

            ARXAnonymizer anonymizer = new ARXAnonymizer();
            result = anonymizer.anonymize(data, config);

        }
        return result;
    }

    @Test
    public void testLogisticRegression() throws IOException, ParseException {
        // Config
        ARXLogisticRegressionConfiguration config = ARXLogisticRegressionConfiguration.create();

        // Classify
        StatisticsClassification classResult = getResult().getOutput().getStatistics().getClassificationPerformance(getFeatures(), getClazz(), config);

        // Accuracy
        assertEquals(0.6953119819640607, classResult.getOriginalAccuracy(), 0d);
        assertEquals(0.4663152310854718, classResult.getZeroRAccuracy(), 0d);
        assertEquals(0.6625555334526888, classResult.getAccuracy(), 0d);

        // Average error
        assertEquals(0.4301404399320268, classResult.getOriginalAverageError(), 0d);
        assertEquals(0.5336847689145282, classResult.getZeroRAverageError(), 0d);
        assertEquals(0.458083337747312, classResult.getAverageError(), 0d);

        // Precision
        double[] precision = { 0.6625555334526888, 0.6625555334526888, 0.6625555334526888, 0.6631795689282987, 0.6692636000270984, 0.7165816003711983, 0.7897122823984526, 0.862657419237087, 0.9030765671987045, 0.9154850484346483, 1.0 };
        assertTrue(Arrays.equals(precision, classResult.getPrecisionRecall().getPrecision()));

        // Recall
        double[] recall = { 1.0, 1.0, 1.0, 0.9983091306942511, 0.9787812479278563, 0.7859889927723626, 0.5485047410649161, 0.3633048206352364, 0.28665207877461707, 0.23615807970293748, 0.0 };
        assertTrue(Arrays.equals(recall, classResult.getPrecisionRecall().getRecall()));

        // F-score
        double[] fscore = { 0.7970326646193115, 0.7970326646193115, 0.7970326646193115, 0.7969457981885784, 0.7949573246627055, 0.7496822483882504, 0.6473702297579599, 0.5112842610849505, 0.4351728037419361, 0.3754621301560845, 0.0 };
        assertTrue(Arrays.equals(fscore, classResult.getPrecisionRecall().getFscore()));

        // Other properties
        assertEquals(7, classResult.getNumClasses(), 0d);
        assertEquals(30162, classResult.getNumMeasurements(), 0d);
    }
}
