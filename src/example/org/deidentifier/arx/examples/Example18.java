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
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Fanout;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.DynamicAdjustment;
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
   
        redaction();
        interval();
        order();
    }

    /**
     * Exemplifies the use of the order-based builder
     */
    private static void order() {

        // Create the builder
        HierarchyBuilderOrderBased<Long> builder = new HierarchyBuilderOrderBased<Long>(
                getExampleData(), DataType.INTEGER, false);

        // Define grouping fanouts
        builder.getLevel(0).addFanout(10, AggregateFunction.INTERVAL(DataType.INTEGER));
        builder.getLevel(1).addFanout(2, AggregateFunction.INTERVAL(DataType.INTEGER));

        // Alternatively
        // builder.setAggregateFunction(AggregateFunction.INTERVAL(DataType.INTEGER));
        // builder.getLevel(0).addFanout(10);
        // builder.getLevel(1).addFanout(2);
        
        // Print specification
        for (HierarchyBuilderIntervalBased<Long>.Level level : builder.getLevels()) {
            System.out.println("Level " + level.getLevel());
            for (Fanout<Long> fanout : level.getFanouts()){
                System.out.println(" - Fanout: " + fanout.getFanout());
            }
        }
        
        // Print info about resulting groups
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare()));
        
        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
    }

    /**
     * Exemplifies the use of the interval-based builder
     */
    private static void interval() {


        // Create the builder
        HierarchyBuilderIntervalBased<Long> builder = new HierarchyBuilderIntervalBased<Long>(
                getExampleData(), 0l, 99l, DataType.INTEGER, DynamicAdjustment.OUT_OF_BOUNDS_LABEL);
        
        // Define base intervals
        builder.setAggregateFunction(AggregateFunction.INTERVAL(DataType.INTEGER));
        builder.addInterval(0l, 20l);
        builder.addInterval(20l, 33l);
        
        // Define grouping fanouts
        builder.getLevel(0).addFanout(2);
        builder.getLevel(1).addFanout(3);

        // Print specification
        for (HierarchyBuilderIntervalBased<Long>.Interval<Long> interval : builder.getIntervals()){
            System.out.println("Interval: " + "["+interval.getMin()+":"+interval.getMax()+"]");
        }
        for (HierarchyBuilderIntervalBased<Long>.Level level : builder.getLevels()) {
            System.out.println("Level " + level.getLevel());
            for (Fanout<Long> fanout : level.getFanouts()){
                System.out.println(" - Fanout: " + fanout.getFanout());
            }
        }
        
        // Print info about resulting levels
//        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare()));
        
        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
    }

    /**
     * Exemplifies the use of the redaction-based builder
     */
    private static void redaction() {

        // Create the builder
        HierarchyBuilderRedactionBased builder = new HierarchyBuilderRedactionBased(getExampleData(),
                                                                                    Order.RIGHT_TO_LEFT,
                                                                                    Order.RIGHT_TO_LEFT,
                                                                                    ' ', '*');
        
        // Print info about resulting groups
        System.out.println("Resulting levels: "+Arrays.toString(builder.prepare()));
        
        // Print resulting hierarchy
        printArray(builder.create().getHierarchy());
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
}
