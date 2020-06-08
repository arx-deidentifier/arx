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
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisk;
import org.deidentifier.arx.risk.RiskModelHistogram;

/**
 * This class implements an example that shows handling of suppressed values and records in input data
 *
 * @author Fabian Prasser
 */
public class Example59 extends Example {

    /**
     * Entry point.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) throws IOException {

        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("45", "male", "81675");
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
        data.getDefinition().setMinimumGeneralization("gender", 1);
        data.getDefinition().setMaximumGeneralization("age", 0);
        
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new AverageReidentificationRisk(0.5d));
        config.setSuppressionLimit(1d);

        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);

        // Convert output to input
        Data outputAsInput = Data.create(result.getOutput().iterator());
        DataHandle handle = outputAsInput.getHandle();
        handle.getDefinition().setAttributeType("age", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        handle.getDefinition().setAttributeType("gender", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        handle.getDefinition().setAttributeType("zipcode", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        
        System.out.println(" - Data:");
        print(handle);
        
        // For each attribute combination sorted by risk ascending
        ARXPopulationModel populationmodel = ARXPopulationModel.create(Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelAttributes riskmodel = builder.getAttributeRisks();
        for (QuasiIdentifierRisk risk : riskmodel.getAttributeRisks()) {
            
            // Specify
            handle.getDefinition().setAttributeType("age", AttributeType.INSENSITIVE_ATTRIBUTE);
            handle.getDefinition().setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
            handle.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);
            for (String qi : risk.getIdentifier()) {
                handle.getDefinition().setAttributeType(qi, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
            }
            
            // Print
            System.out.println("\n - Stats for QI: " + risk.getIdentifier());
            analyzeData(handle);
        }
    }

    /**
     * Perform risk analysis
     * @param handle
     */
    private static void analyzeData(DataHandle handle) {
        
        ARXPopulationModel populationmodel = ARXPopulationModel.create(Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelHistogram classes = builder.getEquivalenceClassModel();
        System.out.println("   * Equivalence classes:");
        System.out.println("     - Average size: " + classes.getAvgClassSize());
        System.out.println("     - Num classes : " + classes.getNumClasses());
        System.out.println("     - Records : " + classes.getNumRecords());
    }
}
