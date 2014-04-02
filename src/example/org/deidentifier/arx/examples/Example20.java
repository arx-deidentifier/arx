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
import java.util.Arrays;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;

/**
 * This class implements examples of how to use aggregate functions
 * 
 * @author Prasser, Kohlmayer
 */
public class Example20 extends Example {

    /**
     * Entry point.
     * 
     * @param args The arguments
     * @throws ParseException 
     */
    public static void main(final String[] args) {

        aggregate(new String[]{"xaaa", "xxxbbb", "xxcccc"}, DataType.STRING);
        aggregate(new String[]{"xaaa", "xxxbbb", "xxcccc"}, DataType.STRING);
        aggregate(new String[]{"1", "2", "5", "11", "12", "3"}, DataType.STRING);
        aggregate(new String[]{"1", "2", "5", "11", "12", "3"}, DataType.INTEGER);
    }
    
    private static void aggregate(String[] args, DataType<?> type){
        System.out.println("Input: "+Arrays.toString(args) + " as "+type.getDescription().getLabel()+"s");
        System.out.println(" - Set                         :"+AggregateFunction.SET(type).aggregate(args));
        System.out.println(" - Set of prefixes             :"+AggregateFunction.SET_OF_PREFIXES(type).aggregate(args));
        System.out.println(" - Set of prefixes of length 2 :"+AggregateFunction.SET_OF_PREFIXES(type, 2).aggregate(args));
        System.out.println(" - Common prefix               :"+AggregateFunction.COMMON_PREFIX(type).aggregate(args));
        System.out.println(" - Common prefix with redaction:"+AggregateFunction.COMMON_PREFIX(type, '*').aggregate(args));
        System.out.println(" - Bounds                      :"+AggregateFunction.BOUNDS(type).aggregate(args));
        System.out.println(" - Interval                    :"+AggregateFunction.INTERVAL(type).aggregate(args));
        System.out.println("\n");

    }
}
