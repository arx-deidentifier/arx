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
import java.util.Arrays;
import java.util.Iterator;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example of how to use the API for access to basic statistics
 * about the data.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example16 extends Example {

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
        data.add("45", "female", "81675");
        data.add("34", "male", "81667");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        final DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        
        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        try {
            final ARXResult result = anonymizer.anonymize(data, config);

            // Print info
            printResult(result, data);

            // Print input
            System.out.println(" - Input data:");
            final Iterator<String[]> original = data.getHandle().iterator();
            while (original.hasNext()) {
                System.out.print("   ");
                System.out.println(Arrays.toString(original.next()));
            }
            
            // Print results
            System.out.println(" - Transformed data:");
            final Iterator<String[]> transformed = result.getOutput(false).iterator();
            while (transformed.hasNext()) {
                System.out.print("   ");
                System.out.println(Arrays.toString(transformed.next()));
            }
            
            // Print frequencies
            StatisticsFrequencyDistribution distribution;
            System.out.println(" - Distribution of attribute 'age' in input:");
            distribution = data.getHandle().getStatistics().getFrequencyDistribution(0, false);
            System.out.println("   " + Arrays.toString(distribution.values));
            System.out.println("   " + Arrays.toString(distribution.frequency));

            // Print frequencies
            System.out.println(" - Distribution of attribute 'age' in output:");
            distribution = result.getOutput(false).getStatistics().getFrequencyDistribution(0, true);
            System.out.println("   " + Arrays.toString(distribution.values));
            System.out.println("   " + Arrays.toString(distribution.frequency));
            
            // Print contingency tables
            StatisticsContingencyTable contingency;
            System.out.println(" - Contingency of attribute 'gender' and 'zipcode' in input:");
            contingency = data.getHandle().getStatistics().getContingencyTable(0, true, 2, true);
            System.out.println("   " + Arrays.toString(contingency.values1));
            System.out.println("   " + Arrays.toString(contingency.values2));
            while (contingency.iterator.hasNext()) {
                Entry e = contingency.iterator.next();
                System.out.println("   ["+e.value1+", "+e.value2+", "+e.frequency+"]");
            }
            
            // Print contingency tables
            System.out.println(" - Contingency of attribute 'gender' and 'zipcode' in output:");
            contingency = result.getOutput(false).getStatistics().getContingencyTable(0, true, 2, true);
            System.out.println("   " + Arrays.toString(contingency.values1));
            System.out.println("   " + Arrays.toString(contingency.values2));
            while (contingency.iterator.hasNext()) {
                Entry e = contingency.iterator.next();
                System.out.println("   ["+e.value1+", "+e.value2+", "+e.frequency+"]");
            }
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
