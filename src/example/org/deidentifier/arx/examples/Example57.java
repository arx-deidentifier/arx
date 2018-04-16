/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;

/**
 * This class implements an example on how to analyze risks with wildcards for data transformed with cell suppression
 *
 * @author Fabian Prasser
 */
public class Example57 extends Example {
    
    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {
        
        // Load data
        Data data = Data.create("data/" + dataset + ".csv", StandardCharsets.UTF_8, ';');
        for (int i=0; i<data.getHandle().getNumColumns(); i++) {
            String attribute = data.getHandle().getAttributeName(i);
            data.getDefinition().setAttributeType(attribute, getHierarchy(data, attribute));
        }
        return data;
    }
    
    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws ParseException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws RollbackRequiredException 
     */
    public static void main(String[] args) throws ParseException, IOException, NoSuchAlgorithmException, RollbackRequiredException {
        
        Data data = createData("ihis");
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(5));
        config.setSuppressionLimit(0.99d);
        config.setQualityModel(Metric.createLossMetric(0d));
        
        ARXResult result = anonymizer.anonymize(data, config);
        DataHandle output = result.getOutput();
        result.optimizeIterativeFast(output, 0.01d);
        System.out.println("Done anonymizing.");

        // Analyze
        System.out.println("Input");
        analyzeData(data.getHandle());
        
        // Analyze
        System.out.println("Output");
        analyzeData(output);
    }
    
   /**
    * Perform risk analysis
    * @param handle
    */
   private static void analyzeData(DataHandle handle) {
       
       double THRESHOLD = 0.1d;
       
       long time = System.currentTimeMillis();
       RiskEstimateBuilder builder = handle.getRiskEstimator();
       RiskModelSampleWildcard risks = builder.getSampleBasedRiskSummaryWildcard(THRESHOLD);
       time = System.currentTimeMillis() - time;
       
       System.out.println(" * Wildcard risk model evaluated in " + time + " ms");
       System.out.println("   - User-specified threshold: " + getPrecent(risks.getRiskThreshold()));
       System.out.println("   - Records at risk: " + getPrecent(risks.getRecordsAtRisk()));
       System.out.println("   - Highest risk: " + getPrecent(risks.getHighestRisk()));
       System.out.println("   - Average risk: " + getPrecent(risks.getAverageRisk()));
       
       RiskModelSampleSummary risks2 = builder.getSampleBasedRiskSummary(THRESHOLD);
       
       System.out.println(" * Risks without considering wildcards");
       System.out.println("   - User-specified threshold: " + getPrecent(THRESHOLD));
       System.out.println("   - Records at risk: " + getPrecent(risks2.getProsecutorRisk().getRecordsAtRisk()));
       System.out.println("   - Highest risk: " + getPrecent(risks2.getProsecutorRisk().getHighestRisk()));
       System.out.println("   - Average risk: " + getPrecent(risks2.getProsecutorRisk().getSuccessRate()));
       
   }

   /**
    * Returns a formatted string
    * @param value
    * @return
    */
   private static String getPrecent(double value) {
       return (int)(Math.round(value * 100)) + "%";
   }

   /**
    * Returns the generalization hierarchy for the dataset and attribute
    * @param data
    * @param attribute
    * @return
    * @throws IOException
    */
   private static Hierarchy getHierarchy(Data data, String attribute) throws IOException {
       DefaultHierarchy hierarchy = Hierarchy.create();
       int col = data.getHandle().getColumnIndexOf(attribute);
       String[] values = data.getHandle().getDistinctValues(col);
       for (String value : values) {
           hierarchy.add(value, DataType.ANY_VALUE);
       }
       return hierarchy;
   }
}