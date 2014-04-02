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
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.DynamicAdjustment;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

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
    }
    
    /**
     * Exemplifies the use of the interval-based builder
     */
    private static void interval() {

        HierarchyBuilderIntervalBased<Long> builder = new HierarchyBuilderIntervalBased<Long>(
                0l, 99l, DataType.INTEGER, 0l, DynamicAdjustment.OUT_OF_BOUNDS_LABEL);
        
        printArray(builder.create(getExampleData()).getHierarchy());
    }

    /**
     * Exemplifies the use of the redaction-based builder
     */
    private static void redaction() {

        HierarchyBuilderRedactionBased builder = new HierarchyBuilderRedactionBased(Order.RIGHT_TO_LEFT,
                                                                                    Order.RIGHT_TO_LEFT,
                                                                                    ' ', '*');
        
        printArray(builder.create(getExampleData()).getHierarchy());
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
