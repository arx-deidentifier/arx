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
import java.util.Date;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;

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
     * @throws ParseException 
     */
    @SuppressWarnings("unused")
    public static void main(final String[] args) throws ParseException {
        
        // 1. List all data types
        for (DataTypeDescription<?> type : DataType.LIST){
            
            // Print basic information
            System.out.println(" - Label : " + type.getLabel());
            System.out.println("   * Class: " + type.getWrappedClass());
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
        
        // 2. Obtain specific data type
        DataTypeDescription<Double> entry = DataType.LIST(Double.class);
        

        // 3. Obtain data in specific formats
        final DefaultData data = Data.create();
        data.add("identifier", "name", "zip", "age", "nationality", "sen");
        data.add("a", "Alice", "47906", "35", "USA", "1.1.2013");
        data.add("b", "Bob", "47903", "59", "Canada", "1.1.2013");
        data.add("c", "Christine", "47906", "42", "USA", "1.1.2013");
        data.add("d", "Dirk", "47630", "18", "Brazil", "1.1.2013");
        data.add("e", "Eunice", "47630", "22", "Brazil", "1.1.2013");
        data.add("f", "Frank", "47633", "63", "Peru", "1.1.2013");
        data.add("g", "Gail", "48973", "33", "Spain", "1.1.2013");
        data.add("h", "Harry", "48972", "47", "Bulgaria", "1.1.2013");
        data.add("i", "Iris", "48970", "52", "France", "1.1.2013");
        
        data.getDefinition().setDataType("zip", DataType.DECIMAL("#,##0"));
        data.getDefinition().setDataType("sen", DataType.DATE("dd.MM.yyyy"));
        
        DataHandle handle = data.getHandle();
        double value1 = handle.getDouble(2, 2);
        Date value2 = handle.getDate(2, 5);
        
        System.out.println("Double: "+value1);
        System.out.println("Date: "+value2);
    }
}
