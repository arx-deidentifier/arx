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

package org.deidentifier.arx.examples;

import java.text.ParseException;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

import cern.colt.Arrays;

/**
 * This class implements examples of how to use the builders for generalization hierarchies
 * 
 * @author Prasser, Kohlmayer
 */
public class Example18 extends Example {

    /**
     * Entry point.
     * 
     * @param args The arguments
     * @throws ParseException 
     */
    public static void main(final String[] args) {
   
        redactionBased();
        intervalBased();
        intervalBased2();
        orderBased();
        ldlCholesterol();
        ldlCholesterolOOB();
        loadStore();
    }

    /**
     * Shows how to load and store hierarchy specifications
     */
    private static void loadStore() {
//        try {
//            HierarchyBuilderRedactionBased builder1 = null;
//            builder1.save("test.spec");
//            HierarchyBuilder<?> builder2 = HierarchyBuilder.create("test.spec");
//            if (builder2.getType() == Type.REDACTION_BASED) {
//                HierarchyBuilderRedactionBased builder3 = (HierarchyBuilderRedactionBased)builder2;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Exemplifies the use of the order-based builder
     */
    private static void orderBased() {

        // Create the builder
        HierarchyBuilderOrderBased<Long> builder = new HierarchyBuilderOrderBased<Long>(DataType.INTEGER, false);

        // Define grouping fanouts
        builder.getLevel(0).addGroup(10, DataType.INTEGER.createAggregate().createIntervalFunction());
        builder.getLevel(1).addGroup(2, DataType.INTEGER.createAggregate().createIntervalFunction());
        // builder.getLevel(2).addFanout(5, AggregateFunction.INTERVAL(DataType.INTEGER));
        // builder.getLevel(3).addFanout(5, AggregateFunction.INTERVAL(DataType.INTEGER));

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
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the interval-based builder
     */
    private static void intervalBased() {


        // Create the builder
        HierarchyBuilderIntervalBased<Long> builder = new HierarchyBuilderIntervalBased<Long>(
                                                          DataType.INTEGER,
                                                          new Range<Long>(0l,-5l,Long.MIN_VALUE),
                                                          new Range<Long>(94l,97l,Long.MAX_VALUE));
        
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
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleDataOOD())));

        System.out.println("");
        System.out.println("RESULT");

        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the interval-based builder
     */
    private static void intervalBased2() {


        // Create the builder
        HierarchyBuilderIntervalBased<Long> builder = new HierarchyBuilderIntervalBased<Long>(
                                                          DataType.INTEGER,
                                                          new Range<Long>(0l,0l,Long.MIN_VALUE),
                                                          new Range<Long>(100l,100l,Long.MAX_VALUE));
        
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
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleDataOOD())));

        System.out.println("");
        System.out.println("RESULT");

        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the redaction-based builder
     */
    private static void redactionBased() {

        // Create the builder
        HierarchyBuilderRedactionBased builder = new HierarchyBuilderRedactionBased(Order.RIGHT_TO_LEFT,
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
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }
    
    /**
     * Exemplifies the use of the interval-based builder for LDL cholesterol
     * in mmol/l
     */
    private static void ldlCholesterol() {


        // Create the builder
        HierarchyBuilderIntervalBased<Double> builder = new HierarchyBuilderIntervalBased<Double>(DataType.DECIMAL);
        
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
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }

    /**
     * Exemplifies the use of the interval-based builder for LDL cholesterol
     * in mmol/l in which the bound is dynamically extended to include all values
     */
    private static void ldlCholesterolOOB() {


        // Create the builder
        HierarchyBuilderIntervalBased<Double> builder = new HierarchyBuilderIntervalBased<Double>(DataType.DECIMAL,
                                                                                new Range<Double>(0d, -2d, -5d),
                                                                                new Range<Double>(10d, 13d, 15d));
        
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
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare(getExampleLDLDataOOB())));
        
        System.out.println("");
        System.out.println("RESULT");
        
        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
        System.out.println("");
    }
    
    /**
     * Returns example data
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
     * Returns example data
     * @return
     */
    private static String[] getExampleDataOOD(){

        String[] result = new String[110];
        for (int i=0; i< result.length; i++){
            result[i] = String.valueOf(i-10);
        }
        return result;
    }
    
    /**
     * Returns example data
     * @return
     */
    private static String[] getExampleLDLData() {

        String[] result = new String[100];
        for (int i=0; i< result.length; i++){
            result[i] = String.valueOf(Math.random() * 5d + 0.5d);
        }
        return result;
    }

    /**
     * Returns example data
     * @return
     */
    private static String[] getExampleLDLDataOOB() {

        String[] result = new String[100];
        for (int i=0; i< result.length; i++){
            result[i] = String.valueOf(Math.random() * 20d + -5d);
        }
        result[99] = "14.999999999";
        result[98] = "-5";
        return result;
    }
}
