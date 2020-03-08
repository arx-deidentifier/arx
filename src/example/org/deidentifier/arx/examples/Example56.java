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
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;

/**
 * This class implements an example of how to evaluate risk with wildcard matching
 *
 * @author Fabian Prasser
 */
public class Example56 extends Example {

    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws IOException 
     * @throws RollbackRequiredException 
     */
    public static void main(String[] args) throws IOException, RollbackRequiredException {

        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("45", "female", "81675");
        data.add("34", "male", "81667");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "male", "81931");
        data.add("70", "male", "81931");
        data.add("45", "female", "81931");

        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("34", "*");
        age.add("45", "*");
        age.add("66", "*");
        age.add("70", "*");

        DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "*");
        zipcode.add("81675", "*");
        zipcode.add("81925", "*");
        zipcode.add("81931", "*");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        
        // Perform risk analysis
 
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.setQualityModel(Metric.createLossMetric(0));
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0.99d);

        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);
        DataHandle output = result.getOutput();
        result.optimizeIterativeFast(output, 0.1d);

        // Perform risk analysis
        System.out.println("\n - Input data");
        print(data.getHandle());
        System.out.println("\n - Risk analysis:");
        analyzeData(data.getHandle());
        
        // Perform risk analysis
        System.out.println("\n - Output data");
        print(output);
        System.out.println("\n - Risk analysis:");
        analyzeData(output);
    }
        
    /**
     * Perform risk analysis
     * @param handle
     */
    private static void analyzeData(DataHandle handle) {
        
        double THRESHOLD = 0.5d;
        
        RiskEstimateBuilder builder = handle.getRiskEstimator();
        RiskModelSampleWildcard risks = builder.getSampleBasedRiskSummaryWildcard(THRESHOLD);
        
        System.out.println(" * Wildcard risk model");
        System.out.println("   - User-specified threshold: " + getPrecent(risks.getRiskThreshold()));
        System.out.println("   - Records at risk: " + getPrecent(risks.getRecordsAtRisk()));
        System.out.println("   - Highest risk: " + getPrecent(risks.getHighestRisk()));
        System.out.println("   - Average risk: " + getPrecent(risks.getAverageRisk()));
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
