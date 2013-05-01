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

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.io.CSVDataOutput;

/**
 * This class implements an example on how to use the API by providing CSV files
 * as input
 * 
 * @author Prasser, Kohlmayer
 */
public class Example2 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

        try {

            final Data data = Data.create("data/test.csv", ';');

            // Define input files
            data.getDefinition()
                .setAttributeType("age",
                                  Hierarchy.create("data/test_hierarchy_age.csv",
                                                   ';'));
            data.getDefinition()
                .setAttributeType("gender",
                                  Hierarchy.create("data/test_hierarchy_gender.csv",
                                                   ';'));
            data.getDefinition()
                .setAttributeType("zipcode",
                                  Hierarchy.create("data/test_hierarchy_zipcode.csv",
                                                   ';'));

            // Create an instance of the anonymizer
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

            // Execute the algorithm
            final FLASHResult result = anonymizer.kAnonymize(data, 2, 0.0d);

            // Print info
            printResult(result, data);

            // Write results
            System.out.print(" - Writing data...");
            final CSVDataOutput out = new CSVDataOutput("data/test_anonymized.csv",
                                                        ';');
            out.write(result.getHandle().iterator());
            System.out.println("Done!");

        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
