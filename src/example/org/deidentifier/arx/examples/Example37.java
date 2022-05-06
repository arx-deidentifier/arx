/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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
import java.text.ParseException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;

/**
 * This class implements an example of how to use data-dependent and data-independent (e,d)-DP
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Ibraheem Al-Dhamari
 */
public class Example37 extends Example {

    
    /**
     * Data Dependent DP.
     * 
     * @param config: the anonymization configuration 
     *
     */
    public static void dataDependentDP (DefaultData data) throws IOException {
        
        System.out.println("--------- Data dependent DP ------------ ");

        // The parameter epsilon
        double epsilon = 2d;
        
        // The parameter delta
        // Notes: 1. It is recommended that it is less than 1/number_of_records
        //        2. For this small dataset, all records will be suppressed
        double delta = 0.1d;
        
        // Create a data-dependent differential privacy criterion
        EDDifferentialPrivacy criterion = new EDDifferentialPrivacy(epsilon, delta); 

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Additional epsilon for search process, default is 0.1
        double dpSearchBudget = 0.1;              

        // Create anonymization configuration
        ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionLimit(1d);        
        config.setDPSearchBudget(dpSearchBudget);  

        // Number of steps to search
        // Number of steps should not be more than the product of height of all hierarchies e.g. for this dataset 3 * 2 * 6.
        int steps = 36;               
        config.setHeuristicSearchStepLimit(steps);
        
        config.addPrivacyModel(criterion);

        ARXResult result = anonymizer.anonymize(data, config);

        // Access output
        DataHandle optimal = result.getOutput();

        System.out.println("Is the result available? " + result.isResultAvailable());

        // Print input
        System.out.println(" - Input data:");
        printHandle(data.getHandle());

        System.out.println(" - Result:");
        printHandle(optimal);
    }
    
    /**
     *  Data independent differential privacy
     * 
     * @param config: the anonymization configuration 
     *
     */
    public static void dataIndependentDP(DefaultData data) throws IOException {

        System.out.println("--------- Data independent DP ------------ ");
        
        // The parameter epsilon
        double epsilon = 2d;
        
        // The parameter delta
        // Notes: 1. It is recommended that delta is less than 1/number_of_records
        //        2. For this small dataset, all records will be suppressed
        double delta = 0.1d;
        
        // Create a data-independent differential privacy criterion
        EDDifferentialPrivacy criterion = new EDDifferentialPrivacy(epsilon, delta,
                                                                    DataGeneralizationScheme.create(data,GeneralizationDegree.MEDIUM));

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Additional epsilon for search process, default is 0.1
        double dpSearchBudget = 0.1;              

        // Create anonymization configuration
        ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionLimit(1d);        
        config.setDPSearchBudget(dpSearchBudget);  

        // Number of steps to search
        // Number of steps should not be more than the product of height of all hierarchies e.g. for this dataset 3 * 2 * 6.
        int steps = 36;               
        config.setHeuristicSearchStepLimit(steps);
        
        config.addPrivacyModel(criterion);
        
        ARXResult result = anonymizer.anonymize(data, config);

        // Access output
        DataHandle optimal = result.getOutput();

        System.out.println("Is the result available? " + result.isResultAvailable());

        // Print input
        System.out.println(" - Input data:");
        printHandle(data.getHandle());

        System.out.println(" - Result:");
        printHandle(optimal);
    }
    
    /**
     *  Differential privacy example
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

        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        data.getDefinition().setAttributeType("age", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("gender", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("zipcode", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setHierarchy("age", age);
        data.getDefinition().setHierarchy("gender", gender);
        data.getDefinition().setHierarchy("zipcode", zipcode);

       // Anonymize using data independent DP         
       dataIndependentDP(data);
       
       data.getHandle().release();
       // Anonymize using data dependent DP         
       dataDependentDP(data); 
    }
}