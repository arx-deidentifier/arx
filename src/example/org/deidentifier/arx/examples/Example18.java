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

import org.deidentifier.arx.AttributeType.Hierarchy;
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
    public static void main(final String[] args) throws ParseException {
        
        redaction();
    }

    /**
     * Exemplifies the use of the redaction-based builder
     */
    private static void redaction() {

        String[] input = new String[100];
        for (int i=0; i< input.length; i++){
            input[i] = String.valueOf(i);
        }
        
        HierarchyBuilderRedactionBased builder = new HierarchyBuilderRedactionBased(Order.RIGHT_TO_LEFT,
                                                                                    Order.RIGHT_TO_LEFT,
                                                                                    ' ', '*');
        
        Hierarchy h = builder.create(input);
        
        for (String[] line : h.getHierarchy()) {
            System.out.println(Arrays.toString(line));
        }
    }
}
