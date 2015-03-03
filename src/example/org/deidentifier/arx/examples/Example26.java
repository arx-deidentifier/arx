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

import java.util.Arrays;
import java.util.Locale;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;

/**
 * This class implements examples of how to use an interval-based hierarchy builder
 * with high precision .
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Igor Vujosevic
 */
public class Example26 extends Example {
    
    /**
     * 
     *
     * @param args
     */
    public static void main(final String[] args) {
        intervalBased(0.001d);
    }

    /**
     * 
     *
     * @param interval
     */
    private static void intervalBased(double interval) {

        DataType<Double> dataType = DataType.createDecimal("#.####", Locale.ENGLISH);

        Double lower = new Double(40d);
        Double upper = new Double(41d);

        // Create the builder
        HierarchyBuilderIntervalBased<Double> builder = HierarchyBuilderIntervalBased.create(
                                                            dataType,
                                                          new Range<Double>(lower, lower, lower),
                                                          new Range<Double>(upper, upper, upper));

        // Define base intervals
        builder.setAggregateFunction(dataType.createAggregate().createIntervalFunction(true, false));
        builder.addInterval(new Double(0d), interval);

        // Define grouping fanouts
        builder.getLevel(0).addGroup(2);
        builder.getLevel(1).addGroup(3);


        System.out.println("------------------------");
        System.out.println("INTERVAL-BASED HIERARCHY");
        System.out.println("------------------------");
        System.out.println("");
        System.out.println("SPECIFICATION");

        // Print specification
        for (Interval<Double> interval1 : builder.getIntervals()){
            System.out.println(interval1);
        }

        // Print specification
        for (Level<Double> level : builder.getLevels()) {
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
     * 
     *
     * @return
     */
    private static String[] getExampleData() {

        String[] data = new String[]{
                "40.764725",
                "40.646866",
                "40.786007",
                "40.812",     
                "40.644527",
                "40.749702",
                "40.764137",

        };

        return data;
    }
}
