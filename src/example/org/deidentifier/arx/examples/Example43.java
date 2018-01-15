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
import java.util.HashSet;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleSummary;

/**
 * This class implements an example of how to evaluate combined risk metrics
 *
 * @author Fabian Prasser
 */
public class Example43 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     * @throws IOException 
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

        // Define research subset
        DataSubset subset = DataSubset.create(data, new HashSet<Integer>(Arrays.asList(1, 2, 5)));

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
        
        // Perform risk analysis
 
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new Inclusion(subset));
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(1d);

        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);

        // Perform risk analysis
        System.out.println("\n - Input data");
        print(data.getHandle().getView());
        System.out.println("\n - Risk analysis:");
        analyzeData(data.getHandle());
        
        // Perform risk analysis
        System.out.println("\n - Output data");
        print(result.getOutput().getView());
        System.out.println("\n - Risk analysis:");
        analyzeData(result.getOutput());
    }
        
    /**
     * Perform risk analysis
     * @param handle
     */
    private static void analyzeData(DataHandle handle) {
        
        double THRESHOLD = 0.5d;
        
        ARXPopulationModel populationmodel = ARXPopulationModel.create(Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelSampleSummary risks = builder.getSampleBasedRiskSummary(THRESHOLD);
        
        System.out.println(" * Baseline risk threshold: " + getPrecent(THRESHOLD));
        System.out.println(" * Prosecutor attacker model");
        System.out.println("   - Records at risk: " + getPrecent(risks.getProsecutorRisk().getRecordsAtRisk()));
        System.out.println("   - Highest risk: " + getPrecent(risks.getProsecutorRisk().getHighestRisk()));
        System.out.println("   - Success rate: " + getPrecent(risks.getProsecutorRisk().getSuccessRate()));
        System.out.println(" * Journalist attacker model");
        System.out.println("   - Records at risk: " + getPrecent(risks.getJournalistRisk().getRecordsAtRisk()));
        System.out.println("   - Highest risk: " + getPrecent(risks.getJournalistRisk().getHighestRisk()));
        System.out.println("   - Success rate: " + getPrecent(risks.getJournalistRisk().getSuccessRate()));
        System.out.println(" * Marketer attacker model");
        System.out.println("   - Success rate: " + getPrecent(risks.getMarketerRisk().getSuccessRate()));
    }

    /**
     * Returns a formatted string
     * @param value
     * @return
     */
    private static String getPrecent(double value) {
        return (int)(Math.round(value * 100)) + "%";
    }
}
