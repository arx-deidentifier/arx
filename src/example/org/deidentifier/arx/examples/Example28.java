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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example on how to use data cleansing using the DataSource functionality.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public class Example28 extends Example {
    
    /**
     * Main entry point.
     *
     * @param args
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException,
                                           SQLException,
                                           ClassNotFoundException {
                                           
        exampleCSV();
        buildHierarchy();
        useBuilderAndAnonymize();
    }
    
    /**
     * This method uses a hierarchy builder for an interval based hierarchy containing NULL values.
     * 
     * @throws IOException
     */
    private static void buildHierarchy() throws IOException {
        
        // Define hierarchies
        // Define hierarchies
        HierarchyBuilderIntervalBased<Long> builder1 = HierarchyBuilderIntervalBased.create(DataType.INTEGER,
                                                                                            new Range<Long>(0l, 0l, 0l),
                                                                                            new Range<Long>(99l, 99l, 99l));
                                                                                            
        // Define base intervals
        builder1.setAggregateFunction(DataType.INTEGER.createAggregate().createIntervalFunction(true, false));
        builder1.addInterval(0l, 20l);
        builder1.addInterval(20l, 33l);
        
        // Define grouping fanouts
        builder1.getLevel(0).addGroup(2);
        builder1.getLevel(1).addGroup(3);
        
        // Print hierarhcy definition
        System.out.println("------------------------");
        System.out.println("INTERVAL-BASED HIERARCHY");
        System.out.println("------------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print specification
        for (Interval<Long> interval1 : builder1.getIntervals()) {
            System.out.println(interval1);
        }
        
        // Print specification
        for (Level<Long> level : builder1.getLevels()) {
            System.out.println(level);
        }
        
        // Print info about resulting levels
        System.out.println("Resulting levels: " + Arrays.toString(builder1.prepare(getExampleData())));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder1.build().getHierarchy());
        System.out.println("");
        
    }
    
    /**
     * This method imports data from a simple CSV file, set a data type and replace all non-matching values with NULL values.
     *
     * @throws IOException
     */
    private static void exampleCSV() throws IOException {
        
        DataSource source = DataSource.createCSVSource("data/test_dirty.csv", StandardCharsets.UTF_8, ';', true);
        source.addColumn("age", DataType.INTEGER, true);
        
        // Create data object
        Data data = Data.create(source);
        
        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");
        age.add("99", ">=50", "*");
        age.add("NULL", "NULL", "*");
        
        data.getDefinition().setAttributeType("age", age);
        
        // Print to console
        print(data.getHandle());
        System.out.println("\n");
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(3));
        config.setSuppressionLimit(0d);
        ARXResult result = anonymizer.anonymize(data, config);
        
        // Print results
        System.out.println("Output:");
        print(result.getOutput(false));
        
    }
    
    private static String[] getExampleData() {
        
        String[] data = new String[] {
                                       "34",
                                       "66",
                                       "70",
                                       "34",
                                       "70",
                                       "NULL",
                                       
        };
        
        return data;
    }
    
    /**
     * This method uses a hierarchy builder for an interval based hierarchy containing NULL values.
     * 
     * @throws IOException
     */
    private static void useBuilderAndAnonymize() throws IOException {
        DataSource source = DataSource.createCSVSource("data/test_dirty.csv", StandardCharsets.UTF_8, ';', true);
        source.addColumn("age", DataType.INTEGER, true);
        
        // Create data object
        Data data = Data.create(source);
        
        // Define hierarchies
        HierarchyBuilderIntervalBased<Long> builder1 = HierarchyBuilderIntervalBased.create(
                                                                                            DataType.INTEGER,
                                                                                            new Range<Long>(0l, 0l, 0l),
                                                                                            new Range<Long>(99l, 99l, 99l));
                                                                                            
        // Define base intervals
        builder1.setAggregateFunction(DataType.INTEGER.createAggregate().createIntervalFunction(true, false));
        builder1.addInterval(0l, 20l);
        builder1.addInterval(20l, 33l);
        
        // Define grouping fanouts
        builder1.getLevel(0).addGroup(2);
        builder1.getLevel(1).addGroup(3);
        
        data.getDefinition().setAttributeType("age", builder1);
        
        // Print info
        System.out.println("Data:");
        printHandle(data.getHandle());
        System.out.println("Hierarchy:");
        printArray(builder1.build(data.getHandle().getDistinctValues(0)).getHierarchy());
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(3));
        config.setSuppressionLimit(0d);
        ARXResult result = anonymizer.anonymize(data, config);
        
        // Print results
        System.out.println("Output:");
        print(result.getOutput(false));
        
    }
    
}
