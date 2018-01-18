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

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example of how to use local recoding with ARX
 *
 * @author Fabian Prasser
 */
public class Example38 extends Example {

    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws IOException 
     */
    public static void main(final String[] args) throws IOException {

        // Define data
        final DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("73", "male", "92922");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");
        age.add("73", ">=50", "*");

        final DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");
        zipcode.add("92922", "9292*", "929**", "92***", "9****", "*****");
        
        data.getDefinition().setAttributeType("age", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("gender", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("zipcode", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setHierarchy("age", age);
        data.getDefinition().setHierarchy("gender", gender);
        data.getDefinition().setHierarchy("zipcode", zipcode);

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Create a config which favors suppression over generalization
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createLossMetric(0.25d));

        // Print input
        System.out.println(" - Input data:");
        printHandle(data.getHandle());

        // Compute the result
        ARXResult result = anonymizer.anonymize(data, config);
        

        // Print result of global recoding
        DataHandle optimum = result.getOutput();
        System.out.println(" - Global recoding:");
        printHandle(optimum);

        try {
            
            // Now apply local recoding to the result
            result.optimizeIterative(optimum, 0.05d, 100, 0.05d);

            // Print result of local recoding
            System.out.println(" - Local recoding:");
            printHandle(optimum);
            
        } catch (RollbackRequiredException e) {
            
            // This part is important to ensure that privacy is preserved, even in case of exceptions
            optimum = result.getOutput();
        }
    }
}
