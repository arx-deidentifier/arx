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

import java.io.IOException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example on how to use the API by providing CSV files
 * as input.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
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
            final ARXAnonymizer anonymizer = new ARXAnonymizer();

            // Execute the algorithm
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(0d);
            final ARXResult result = anonymizer.anonymize(data, config);
            
            // Print info
            printResult(result, data);

            // Write results
            System.out.print(" - Writing data...");
            result.getOutput(false).save("data/test_anonymized.csv", ';');
            System.out.println("Done!");

        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
