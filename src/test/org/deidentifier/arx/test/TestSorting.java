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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;
import org.junit.Assert;
import org.junit.Test;

import cern.colt.Swapper;

/**
 * Tests use of sorting within complex configurations
 *
 * @author Fabian Prasser
 */
public class TestSorting extends AbstractTest {

    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    private Data createData(final String dataset) throws IOException {

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
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }

        return data;
    }
    
    /**
     * Entry point.
     *
     * @param args the arguments
     * @throws IOException
     */
    @Test
    public void doSortingTest() throws IOException {
        
        List<Integer> fields = new ArrayList<>();
        fields.add(0);
        fields.add(1);
        fields.add(2);
        fields.add(3);
        fields.add(4);
        fields.add(5);
        fields.add(6);
        fields.add(7);
        fields.add(8);
        
        int seed = 0xDEADBEEF;

        // Load the dataset
        Data data = createData("adult");
        data.getDefinition().setAttributeType("marital-status", AttributeType.SENSITIVE_ATTRIBUTE);
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setMicroAggregationFunction("age", MicroAggregationFunction.createArithmeticMean(), true);
        
        // We select all records with "sex=Male" to serve as the dataset which we will de-identify.
        // The overall dataset serves as our population table.
        DataSubset subset = DataSubset.create(data, DataSelector.create(data).field("sex").equals("Male"));
        RowSet rows = subset.getSet();
        
        double value = Double.NaN;
        
        for (int i=0; i<100; i++) {
         
            seed += i;
            
            // Configure
            ARXConfiguration config = ARXConfiguration.create();
            config.setSuppressionLimit(1d);
            config.setQualityModel(Metric.createLossMetric());
            config.addPrivacyModel(new KAnonymity(5));
            config.addPrivacyModel(new Inclusion(DataSubset.create(data, rows)));
            config.addPrivacyModel(new DistinctLDiversity("marital-status", 3));
    
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            ARXResult result = anonymizer.anonymize(data, config);
            
            result.getOutput();
            result.getOutput(result.getGlobalOptimum().getSuccessors()[1].getSuccessors()[2]);
            double value1 = Double.valueOf(result.getGlobalOptimum().getSuccessors()[1].getSuccessors()[2].getHighestScore().toString());
            
            Collections.shuffle(fields, new Random(seed));
            
            if (i%2==0) {
                
                final RowSet _subset = rows;
                result.getOutput().sort(new Swapper() {

                    @Override
                    public void swap(int arg0, int arg1) {
                        _subset.swap(arg0, arg1);
                    }
                    
                }, i%4==0, fields.get(0), fields.get(1));
                
            } else if (i%3 == 0) {

                final RowSet _subset = rows;
                data.getHandle().getView().sort(new Swapper() {

                    @Override
                    public void swap(int arg0, int arg1) {
                        _subset.swap(arg0, arg1);
                    }
                    
                }, i%6==0, fields.get(0));
                
            } else if (i%5 == 0) {

                final RowSet _subset = rows;
                result.getOutput().getView().sort(new Swapper() {

                    @Override
                    public void swap(int arg0, int arg1) {
                        _subset.swap(arg0, arg1);
                    }
                    
                }, i%10==0, fields.get(0), fields.get(1));
                
            } else if (i%7 == 0) {

                final RowSet _subset = rows;
                data.getHandle().sort(new Swapper() {

                    @Override
                    public void swap(int arg0, int arg1) {
                        _subset.swap(arg0, arg1);
                    }
                    
                }, i%14==0, fields.get(0), fields.get(1), fields.get(2));
            }

            result.getOutput();
            result.getOutput(result.getGlobalOptimum().getSuccessors()[1].getSuccessors()[2]);
            double value2 = Double.valueOf(result.getGlobalOptimum().getSuccessors()[1].getSuccessors()[2].getHighestScore().toString());
            
            data.getHandle().release();
            
            if (Double.isNaN(value)) {
                value = value1;
            }
            
            if (value1 != value || value2 != value || value1 != value2) {
                Assert.fail("Value mismatch: " + value + ", " + value1 + ", " + value2);
            }
        }
    }
}