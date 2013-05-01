/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.Data.DefaultData;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.DataType;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;

/**
 * This class implements an example on how to use the API for tools such as GUIs
 * 
 * @author Prasser, Kohlmayer
 */
public class Example4 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define data
        final DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Obtain a handle
        final DataHandle inHandle = data.getHandle();

        // Read the encoded data
        inHandle.getNumRows();
        inHandle.getNumColumns();
        inHandle.getAttributeName(0);
        inHandle.getValue(0, 0);

        // Define hierarchy. Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
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
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        try {

            // Now anonymize the data utilizing the pre-encoded data
            final FLASHResult result = anonymizer.kAnonymize(data, 2, 0.0d);

            // Obtain a handle for the transformed data
            final DataHandle outHandle = result.getHandle();

            // Sort the data. This operation is implicitly performed on both
            // representations of the dataset.
            outHandle.sort(false, 2);

            // Print info
            printResult(result, data);

            // Process results
            System.out.println(" - Transformed data:");
            final Iterator<String[]> transformed = result.getHandle()
                                                         .iterator();
            while (transformed.hasNext()) {
                System.out.print("   ");
                System.out.println(Arrays.toString(transformed.next()));
            }

        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
