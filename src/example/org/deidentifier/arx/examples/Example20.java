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

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction.AggregateFunctionBuilder;

/**
 * This class implements examples of how to use aggregate functions.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example20 extends Example {

    /**
     * Entry point.
     *
     * @param args The arguments
     */
    public static void main(final String[] args) {

        aggregate(new String[]{"xaaa", "xxxbbb", "xxcccc"}, DataType.STRING);
        aggregate(new String[]{"xaaa", "xxxbbb", "xxcccc"}, DataType.STRING);
        aggregate(new String[]{"1", "2", "5", "11", "12", "3"}, DataType.STRING);
        aggregate(new String[]{"1", "2", "5", "11", "12", "3"}, DataType.INTEGER);
    }
    
    /**
     * 
     *
     * @param args
     * @param type
     */
    private static void aggregate(String[] args, DataType<?> type){
        
        AggregateFunctionBuilder<?> builder = type.createAggregate();
        System.out.println("Input: "+Arrays.toString(args) + " as "+type.getDescription().getLabel()+"s");
        System.out.println(" - Set                         :"+builder.createSetFunction().aggregate(args));
        System.out.println(" - Set of prefixes             :"+builder.createSetOfPrefixesFunction().aggregate(args));
        System.out.println(" - Set of prefixes of length 2 :"+builder.createSetOfPrefixesFunction(2).aggregate(args));
        System.out.println(" - Common prefix               :"+builder.createPrefixFunction().aggregate(args));
        System.out.println(" - Common prefix with redaction:"+builder.createPrefixFunction('*').aggregate(args));
        System.out.println(" - Bounds                      :"+builder.createBoundsFunction().aggregate(args));
        System.out.println(" - Interval                    :"+builder.createIntervalFunction().aggregate(args));
        System.out.println();
    }
}
