/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.KMap;

/**
 * This class implements an example of how to use the distribution of risks
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example46 extends Example {
    
    /**
     * Entry point.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) throws IOException {
        
        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("45", "female", "81675");
        data.add("34", "male", "81667");
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
        
        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        
        ARXPopulationModel populationmodel = ARXPopulationModel.create(data.getHandle().getNumRows(), 0.01d);
        
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KMap(5, 0.1d, populationmodel));
        config.setSuppressionLimit(1d);
        
        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);

        // Perform risk analysis
        System.out.println("- Input data");
        print(data.getHandle());
        System.out.print("\n- Records at 50% risk: " + data.getHandle().getRiskEstimator(populationmodel).getSampleBasedRiskDistribution().getFractionOfRecordsAtRisk(0.5d));
        System.out.println("\n- Records at <=50% risk: " + data.getHandle().getRiskEstimator(populationmodel).getSampleBasedRiskDistribution().getFractionOfRecordsAtCumulativeRisk(0.5d));
        
        // Perform risk analysis
        System.out.println("\n- Output data");
        print(result.getOutput());
        System.out.print("\n- Records at 50% risk: " + result.getOutput().getRiskEstimator(populationmodel).getSampleBasedRiskDistribution().getFractionOfRecordsAtRisk(0.5d));
        System.out.print("\n- Records at <=50% risk: " + result.getOutput().getRiskEstimator(populationmodel).getSampleBasedRiskDistribution().getFractionOfRecordsAtCumulativeRisk(0.5d));
    }
}
