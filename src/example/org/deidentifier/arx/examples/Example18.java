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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

import cern.colt.Arrays;

/**
 * This class implements examples of how to use the builders for generalization hierarchies.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example18 extends Example {

    /**
     * Entry point.
     *
     * @param args The arguments
     */
    public static void main(final String[] args) {
   
        redactionBased();
        intervalBased();
        orderBased();
        ldlCholesterol();
        dates();
        loadStore();
    }

    /**
     * Shows how to load and store hierarchy specifications.
     */
    private static void loadStore() {
        try {
            HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(Order.RIGHT_TO_LEFT,
                                                                                              Order.RIGHT_TO_LEFT,
                                                                                              ' ', '*');
            builder.save("test.ahs");
            
            HierarchyBuilder<?> loaded = HierarchyBuilder.create("test.ahs");
            if (loaded.getType() == Type.REDACTION_BASED) {
                
                builder = (HierarchyBuilderRedactionBased<?>)loaded;
                
                System.out.println("-------------------------");
                System.out.println("REDACTION-BASED HIERARCHY");
                System.out.println("-------------------------");
                System.out.println("");
                System.out.println("SPECIFICATION");
                
                // Print info about resulting groups
                System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleData())));
                
                System.out.println("");
                System.out.println("RESULT");
                
                // Print resulting hierarchy
                printArray(builder.build().getHierarchy());
                System.out.println("");
            } else {
                System.out.println("Incompatible type of builder");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exemplifies the use of the order-based builder.
     */
    private static void orderBased() {

        // Create the builder
        HierarchyBuilderOrderBased<Long> builder = HierarchyBuilderOrderBased.create(DataType.INTEGER, false);

        // Define grouping fanouts
        builder.getLevel(0).addGroup(10, DataType.INTEGER.createAggregate().createIntervalFunction());
        builder.getLevel(1).addGroup(2, DataType.INTEGER.createAggregate().createIntervalFunction());

        // Alternatively
        // builder.setAggregateFunction(AggregateFunction.INTERVAL(DataType.INTEGER));
        // builder.getLevel(0).addFanout(10);
        // builder.getLevel(1).addFanout(2);
        
        System.out.println("---------------------");
        System.out.println("ORDER-BASED HIERARCHY");
        System.out.println("---------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print specification
        for (Level<Long> level : builder.getLevels()) {
            System.out.println(level);
        }
        
        // Print info about resulting groups
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleData())));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder.build().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the interval-based builder.
     */
    private static void intervalBased() {


        // Create the builder
        HierarchyBuilderIntervalBased<Long> builder = HierarchyBuilderIntervalBased.create(
                                                          DataType.INTEGER,
                                                          new Range<Long>(0l,0l,Long.MIN_VALUE / 4),
                                                          new Range<Long>(100l,100l,Long.MAX_VALUE / 4));
        
        // Define base intervals
        builder.setAggregateFunction(DataType.INTEGER.createAggregate().createIntervalFunction(true, false));
        builder.addInterval(0l, 20l);
        builder.addInterval(20l, 33l);
        
        // Define grouping fanouts
        builder.getLevel(0).addGroup(2);
        builder.getLevel(1).addGroup(3);
        

        System.out.println("------------------------");
        System.out.println("INTERVAL-BASED HIERARCHY");
        System.out.println("------------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print specification
        for (Interval<Long> interval : builder.getIntervals()){
            System.out.println(interval);
        }

        // Print specification
        for (Level<Long> level : builder.getLevels()) {
            System.out.println(level);
        }
        
        // Print info about resulting levels
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleData())));

        System.out.println("");
        System.out.println("RESULT");

        // Print resulting hierarchy
        printArray(builder.build().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the redaction-based builder.
     */
    private static void redactionBased() {

        // Create the builder
        HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(Order.RIGHT_TO_LEFT,
                                                                                    Order.RIGHT_TO_LEFT,
                                                                                    ' ', '*');

        System.out.println("-------------------------");
        System.out.println("REDACTION-BASED HIERARCHY");
        System.out.println("-------------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print info about resulting groups
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleData())));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder.build().getHierarchy());
        System.out.println("");
    }
    
    /**
     * Exemplifies the use of the interval-based builder for LDL cholesterol
     * in mmol/l.
     */
    private static void ldlCholesterol() {


        // Create the builder
        HierarchyBuilderIntervalBased<Double> builder = HierarchyBuilderIntervalBased.create(DataType.DECIMAL);
        
        // Define base intervals
        builder.addInterval(0d, 1.8d, "very low");
        builder.addInterval(1.8d, 2.6d, "low");
        builder.addInterval(2.6d, 3.4d, "normal");
        builder.addInterval(3.4d, 4.1d, "borderline high");
        builder.addInterval(4.1d, 4.9d, "high");
        builder.addInterval(4.9d, 10d, "very high");
        
        // Define grouping fanouts
        builder.getLevel(0).addGroup(2, "low").addGroup(2, "normal").addGroup(2, "high");
        builder.getLevel(1).addGroup(2, "low-normal").addGroup(1, "high");

        System.out.println("--------------------------");
        System.out.println("LDL-CHOLESTEROL HIERARCHY");
        System.out.println("--------------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print specification
        for (Interval<Double> interval : builder.getIntervals()){
            System.out.println(interval);
        }

        // Print specification
        for (Level<Double> level : builder.getLevels()) {
            System.out.println(level);
        }
        
        // Print info about resulting levels
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleLDLData())));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder.build().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the order-based builder.
     */
    private static void dates() {

    	String stringDateFormat = "yyyy-MM-dd HH:mm";
    	
    	DataType<Date> dateType = DataType.createDate(stringDateFormat);
    	
        // Create the builder
        HierarchyBuilderOrderBased<Date> builder = HierarchyBuilderOrderBased.create(dateType, false);

        // Define grouping fanouts
        builder.getLevel(0).addGroup(10, dateType.createAggregate().createIntervalFunction());
        builder.getLevel(1).addGroup(2, dateType.createAggregate().createIntervalFunction());

        // Alternatively
        // builder.setAggregateFunction(AggregateFunction.INTERVAL(DataType.INTEGER));
        // builder.getLevel(0).addFanout(10);
        // builder.getLevel(1).addFanout(2);
        
        System.out.println("---------------------");
        System.out.println("ORDER-BASED DATE HIERARCHY");
        System.out.println("---------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");
        
        // Print specification
        for (Level<Date> level : builder.getLevels()) {
            System.out.println(level);
        }
        
        // Print info about resulting groups
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleDateData(stringDateFormat))));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder.build().getHierarchy());
        System.out.println("");
    }
    
    /**
     * Returns example data.
     *
     * @return
     */
    private static String[] getExampleData(){

        String[] result = new String[100];
        for (int i=0; i< result.length; i++){
            result[i] = String.valueOf(i);
        }
        return result;
    }
    
    /**
     * Returns example date data.
     *
     * @param stringFormat
     * @return
     */
    private static String[] getExampleDateData(String stringFormat){

    	SimpleDateFormat format = new SimpleDateFormat(stringFormat);
    	
        String[] result = new String[100];
        for (int i=0; i< result.length; i++){
        	
        	Calendar date = GregorianCalendar.getInstance();
        	date.add(Calendar.HOUR, i);
        	
            result[i] = format.format(date.getTime());
        }
        return result;
    }
    
    /**
     * Returns example data.
     *
     * @return
     */
    private static String[] getExampleLDLData() {

        String[] result = new String[100];
        for (int i=0; i< result.length; i++){
            result[i] = String.valueOf(Math.random() * 9.9d);
        }
        return result;
    }
}
