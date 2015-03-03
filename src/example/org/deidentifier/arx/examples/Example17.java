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

import java.text.ParseException;
import java.util.Date;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;

/**
 * This class implements an example of how to list the available data types.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
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
        for (DataTypeDescription<?> type : DataType.list()){
            
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
        DataTypeDescription<Double> entry = DataType.list(Double.class);
        

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
        
        data.getDefinition().setDataType("zip", DataType.createDecimal("#,##0"));
        data.getDefinition().setDataType("sen", DataType.createDate("dd.MM.yyyy"));
        
        DataHandle handle = data.getHandle();
        double value1 = handle.getDouble(2, 2);
        Date value2 = handle.getDate(2, 5);
        
        System.out.println("Double: "+value1);
        System.out.println("Date: "+value2);
    }
}
