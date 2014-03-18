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

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.Entry;

/**
 * This class implements an example of how to list the available data types
 * 
 * @author Prasser, Kohlmayer
 */
public class Example17 extends Example {

    /**
     * Entry point.
     * 
     * @param args The arguments
     */
    public static void main(final String[] args) {
        
        // List all data types
        for (Entry<?> type : DataType.LIST){
            
            // Print basic information
            System.out.println(" - Label : " + type.getLabel());
            System.out.println("   * Format: " + type.hasFormat());
            if (type.hasFormat()){
                System.out.println("   * Formats: " + type.getExampleFormats());
            }
            
            // Create an instance without a format string
            DataType<?> instance = type.newInstance();
            
            // Create an instance with format string
            if (type.hasFormat() && !type.getExampleFormats().isEmpty()) {
                instance = type.newInstance(type.getExampleFormats().get(0));
            }
        }
    }
}
