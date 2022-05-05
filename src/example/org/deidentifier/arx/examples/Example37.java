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
 * This class implements an example of how to use data-dependent (e,d)-DP
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example37 extends Example {

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

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Setting epsilon and delta parameters as found in equation 1 in the SafePub paper
        //    https://doi.org/10.1515/popets-2018-0004
        
        // epsilon parameter
        double epsilon = 2d;
        
        // delta parameter, highest value is 0.1d
        // note: for this small dataset, all records will be suppressed
        double delta = 0.00001d;
        
        // Create a data-dependent differential privacy criterion
        EDDifferentialPrivacy criterionDataIndependentDP = new EDDifferentialPrivacy(epsilon, delta); 

        // Create a data-independent differential privacy criterion
        EDDifferentialPrivacy criterionDataDependentDP = new EDDifferentialPrivacy(epsilon, delta,
                                                                    DataGeneralizationScheme.create(data,GeneralizationDegree.MEDIUM));
               
        // parameter to control which criterion to use
        boolean useDataIndependentDP = true;

        // Number of steps to search, default is Integer.MAX_VALUE
        // Number of steps should not be more than the product of height of all hierarchies e.g. for this dataset 3 * 2 * 6.
        int steps = 36;

        // Additional epsilon for search process, default is 0.1
        double dpSearchBudget = 0.1;              
        
        // Create anonymization configuration
        ARXConfiguration config = ARXConfiguration.create();

        if (useDataIndependentDP) {
           config.addPrivacyModel(criterionDataIndependentDP);
        }  else {
           config.addPrivacyModel(criterionDataDependentDP);              
        }
        
        config.setSuppressionLimit(1d);
        
        config.setHeuristicSearchStepLimit(steps);
        
        config.setDPSearchBudget(dpSearchBudget);  
        
        ARXResult result = anonymizer.anonymize(data, config);

        // Access output
        DataHandle optimal = result.getOutput();

        System.out.println("useDataIndependentDP: " + useDataIndependentDP);
        System.out.println("isResultAvailable: " + result.isResultAvailable());

        // Print input
        System.out.println(" - Input data:");
        printHandle(data.getHandle());

        System.out.println(" - Result:");
        printHandle(optimal);
    }
}