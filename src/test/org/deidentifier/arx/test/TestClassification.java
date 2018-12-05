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
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXConfiguration;
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
            config.setSuppressionLimit(1d);

            ARXAnonymizer anonymizer = new ARXAnonymizer();
            result = anonymizer.anonymize(data, config);

        }
        return result;
    }

    @Test
    public void testLogisticRegression() throws IOException, ParseException {
        
        // Config
        ARXClassificationConfiguration<?> config = ARXClassificationConfiguration.createLogisticRegression();

        // Classify
        StatisticsClassification classResult = getResult().getOutput().getStatistics().getClassificationPerformance(getFeatures(), getClazz(), config);

        // Accuracy
        assertEquals(0.6953119819640607, classResult.getOriginalAccuracy(), 0d);
        assertEquals(0.4663152310854718, classResult.getZeroRAccuracy(), 0d);
        assertEquals(0.6625555334526888, classResult.getAccuracy(), 0d);

        // Average error
        assertEquals(0.43014671841651053, classResult.getOriginalAverageError(), 0d);
        assertEquals(0.5336847689145282, classResult.getZeroRAverageError(), 0d);
        assertEquals(0.458087061525274, classResult.getAverageError(), 0d);
        
        // Sensitivity
        assertEquals(0.28713811105837683, classResult.getROCCurve("Divorced").getSensitivity(), 0d);
        assertEquals(0d, classResult.getROCCurve("Married-spouse-absent").getSensitivity(), 0d);
        assertEquals(0.3349455864570738, classResult.getROCCurve("Widowed").getSensitivity(), 0d);
        assertEquals(0d, classResult.getROCCurve("Separated").getSensitivity(), 0d);
        assertEquals(0d, classResult.getROCCurve("Married-AF-spouse").getSensitivity(), 0d);
        assertEquals(0.8457163170991824, classResult.getROCCurve("Married-civ-spouse").getSensitivity(), 0d);
        assertEquals(0.678799095208719, classResult.getROCCurve("Never-married").getSensitivity(), 0d);
        
        // Specificity
        assertEquals(0.9507091105287498, classResult.getROCCurve("Divorced").getSpecificity(), 0d);
        assertEquals(0.9999328678839957, classResult.getROCCurve("Married-spouse-absent").getSpecificity(), 0d);
        assertEquals(0.991545934890063, classResult.getROCCurve("Widowed").getSpecificity(), 0d);
        assertEquals(0.9998973411354071, classResult.getROCCurve("Separated").getSpecificity(), 0d);
        assertEquals(1d, classResult.getROCCurve("Married-AF-spouse").getSpecificity(), 0d);
        assertEquals(0.6962166863390694, classResult.getROCCurve("Married-civ-spouse").getSpecificity(), 0d);
        assertEquals(0.8162066940692895, classResult.getROCCurve("Never-married").getSpecificity(), 0d);
        
        // Brier score
        assertEquals(0.10443452758431408, classResult.getROCCurve("Divorced").getBrierScore(), 0d);
        assertEquals(0.012016040652422648, classResult.getROCCurve("Married-spouse-absent").getBrierScore(), 0d);
        assertEquals(0.02128820838095078, classResult.getROCCurve("Widowed").getBrierScore(), 0d);
        assertEquals(0.02923421938927171, classResult.getROCCurve("Separated").getBrierScore(), 0d);
        assertEquals(6.961954804848942E-4, classResult.getROCCurve("Married-AF-spouse").getBrierScore(), 0d);
        assertEquals(0.15298256377665254, classResult.getROCCurve("Married-civ-spouse").getBrierScore(), 0d);
        assertEquals(0.14548728696298602, classResult.getROCCurve("Never-married").getBrierScore(), 0d);

        // AUC
        assertEquals(0.7610124597337793, classResult.getROCCurve("Divorced").getAUC(), 0d);
        assertEquals(0.7158230397421533, classResult.getROCCurve("Married-spouse-absent").getAUC(), 0d);
        assertEquals(0.9062487311956317, classResult.getROCCurve("Widowed").getAUC(), 0d);
        assertEquals(0.7386316240248749, classResult.getROCCurve("Separated").getAUC(), 0d);
        assertEquals(0.5357992040583639, classResult.getROCCurve("Married-AF-spouse").getAUC(), 0d);
        assertEquals(0.8556273433051558, classResult.getROCCurve("Married-civ-spouse").getAUC(), 0d);
        assertEquals(0.8405758072618743, classResult.getROCCurve("Never-married").getAUC(), 0d);

        // Other properties
        assertEquals(7, classResult.getNumClasses(), 0d);
        assertEquals(30162, classResult.getNumMeasurements(), 0d);
    }

    @Test
    public void testNaiveBayes() throws IOException, ParseException {

        // Config
        ARXClassificationConfiguration<?> config = ARXClassificationConfiguration.createNaiveBayes();

        // Classify
        StatisticsClassification classResult = getResult().getOutput().getStatistics().getClassificationPerformance(getFeatures(), getClazz(), config);

        // Accuracy
        assertEquals(0.6447516742921557, classResult.getOriginalAccuracy(), 0d);
        assertEquals(0.4663152310854718, classResult.getZeroRAccuracy(), 0d);
        assertEquals(0.6271798952324117, classResult.getAccuracy(), 0d);

        // Average error
        assertEquals(0.38050937350272185, classResult.getOriginalAverageError(), 0d);
        assertEquals(0.5336847689145282, classResult.getZeroRAverageError(), 0d);
        assertEquals(0.39543922724482766, classResult.getAverageError(), 0d);
        
        // Sensitivity
        assertEquals(0.23706691979117228, classResult.getROCCurve("Divorced").getSensitivity(), 0d);
        assertEquals(0.05675675675675676, classResult.getROCCurve("Married-spouse-absent").getSensitivity(), 0d);
        assertEquals(0.4195888754534462, classResult.getROCCurve("Widowed").getSensitivity(), 0d);
        assertEquals(0.12566560170394037, classResult.getROCCurve("Separated").getSensitivity(), 0d);
        assertEquals(0d, classResult.getROCCurve("Married-AF-spouse").getSensitivity(), 0d);
        assertEquals(0.6993956629932456, classResult.getROCCurve("Married-civ-spouse").getSensitivity(), 0d);
        assertEquals(0.7808965659058195, classResult.getROCCurve("Never-married").getSensitivity(), 0d);
        
        // Specificity
        assertEquals(0.952597502697703, classResult.getROCCurve("Divorced").getSpecificity(), 0d);
        assertEquals(0.990702201933405, classResult.getROCCurve("Married-spouse-absent").getSpecificity(), 0d);
        assertEquals(0.9767513209476735, classResult.getROCCurve("Widowed").getSpecificity(), 0d);
        assertEquals(0.973342914827362, classResult.getROCCurve("Separated").getSpecificity(), 0d);
        assertEquals(1d, classResult.getROCCurve("Married-AF-spouse").getSpecificity(), 0d);
        assertEquals(0.8227620053426105, classResult.getROCCurve("Married-civ-spouse").getSpecificity(), 0d);
        assertEquals(0.7345860246623606, classResult.getROCCurve("Never-married").getSpecificity(), 0d);
        
        // Brier score
        assertEquals(0.12097803188289097, classResult.getROCCurve("Divorced").getBrierScore(), 0d);
        assertEquals(0.017706173395676122, classResult.getROCCurve("Married-spouse-absent").getBrierScore(), 0d);
        assertEquals(0.028104806010520456, classResult.getROCCurve("Widowed").getBrierScore(), 0d);
        assertEquals(0.03838086078373676, classResult.getROCCurve("Separated").getBrierScore(), 0d);
        assertEquals(6.962403023672171E-4, classResult.getROCCurve("Married-AF-spouse").getBrierScore(), 0d);
        assertEquals(0.18835167736742914, classResult.getROCCurve("Married-civ-spouse").getBrierScore(), 0d);
        assertEquals(0.18555815408265927, classResult.getROCCurve("Never-married").getBrierScore(), 0d);

        // AUC
        assertEquals(0.74388359062692, classResult.getROCCurve("Divorced").getAUC(), 0d);
        assertEquals(0.6968275539234446, classResult.getROCCurve("Married-spouse-absent").getAUC(), 0d);
        assertEquals(0.8900178462158798, classResult.getROCCurve("Widowed").getAUC(), 0d);
        assertEquals(0.7291579637131129, classResult.getROCCurve("Separated").getAUC(), 0d);
        assertEquals(0.5805491965539002, classResult.getROCCurve("Married-AF-spouse").getAUC(), 0d);
        assertEquals(0.8467890484679544, classResult.getROCCurve("Married-civ-spouse").getAUC(), 0d);
        assertEquals(0.8265624922645384, classResult.getROCCurve("Never-married").getAUC(), 0d);

        // Other properties
        assertEquals(7, classResult.getNumClasses(), 0d);
        assertEquals(30162, classResult.getNumMeasurements(), 0d);
    }

}
