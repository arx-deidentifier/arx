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
import java.util.Arrays;
import java.util.Iterator;

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
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example on how to use the API for tools such as GUIs.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example4 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) throws IOException {

        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Obtain a handle
        DataHandle inHandle = data.getHandle();

        // Read the encoded data
        inHandle.getNumRows();
        inHandle.getNumColumns();
        inHandle.getAttributeName(0);
        inHandle.getValue(0, 0);

        // Define hierarchy. Only excerpts for readability
        DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        // Create a data definition
        data.getDefinition()
            .setAttributeType("age", AttributeType.IDENTIFYING_ATTRIBUTE);
        data.getDefinition()
            .setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("zipcode", zipcode);

        data.getDefinition().setDataType("zipcode", DataType.DECIMAL);

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);

        // Now anonymize the data utilizing the pre-encoded data
        ARXResult result = anonymizer.anonymize(data, config);

        // Obtain a handle for the transformed data
        DataHandle outHandle = result.getOutput(false);

        // Sort the data. This operation is implicitly performed on both
        // representations of the dataset.
        outHandle.sort(false, 2);

        // Print info
        printResult(result, data);

        // Process results
        System.out.println(" - Transformed data:");
        Iterator<String[]> transformed = result.getOutput(false).iterator();
        while (transformed.hasNext()) {
            System.out.print("   ");
            System.out.println(Arrays.toString(transformed.next()));
        }
    }
}
