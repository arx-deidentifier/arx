/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.criteria.KAnonymity;


/**
 * This class implements an example on how to use data cleansing capabilities
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example27 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode", "dob");
        data.add("34", "male", "81667", "3.2.1913");
        data.add("45", "female", "81675", "5.5.1955");
        data.add("66", "male", "81925", "3.3.1967");
        data.add("70", "female", "81931", "1.1.1992");
        data.add("34", "female", "81931", "25.11.1988");
        data.add("70", "male", "81931", "13.3.1955");
        data.add("45", "male", "81931", "28.6.2013");

        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");
        age.add("99", ">=50", "*");

        DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        data.getDefinition().setAttributeType("dob", AttributeType.INSENSITIVE_ATTRIBUTE);

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(3));
        config.setSuppressionLimit(0d);
        
        // Process results
        System.out.println("Input:");
        print(data.getHandle());
        
        System.out.println("Determining data types:");
        determineDataType(data.getHandle(), 0);
        determineDataType(data.getHandle(), 1);
        determineDataType(data.getHandle(), 2);
        determineDataType(data.getHandle(), 3);
        
        System.out.println("Replacing 34 with 99");
        data.getHandle().replace(0, "34", "99");
        
        System.out.println("New input:");
        print(data.getHandle());
        
        ARXResult result = anonymizer.anonymize(data, config);

        // Process results
        System.out.println("Output:");
        print(result.getOutput(false));
        
        System.out.println("Replacing female with f");
        data.getHandle().replace(1, "female", "f");
        
        System.out.println("New output:");
        print(result.getOutput(false));

        System.out.println("New input:");
        print(data.getHandle());
        
        System.out.println("Replacing 81*** with 81xxx");
        data.getHandle().replace(2, "81***", "81xxx");

        System.out.println("New output:");
        print(result.getOutput(false));

    }

    /**
     * Prints a list of matching data types
     * @param handle
     * @param column
     */
    private static void determineDataType(DataHandle handle, int column) {
        System.out.println(" - Potential data types for attribute: "+handle.getAttributeName(column));
        List<Pair<DataType<?>, Double>> types = handle.getMatchingDataTypes(column);

        // Print entries sorted by match percentage
        for (Pair<DataType<?>, Double> entry : types) {
            System.out.print("   * ");
            System.out.print(entry.getKey().getDescription().getLabel());
            if (entry.getKey().getDescription().hasFormat()) {
                System.out.print("[");
                System.out.print(((DataTypeWithFormat) entry.getKey()).getFormat());
                System.out.print("]");
            }
            System.out.print(": ");
            System.out.println(entry.getValue());
        }
    }
}
