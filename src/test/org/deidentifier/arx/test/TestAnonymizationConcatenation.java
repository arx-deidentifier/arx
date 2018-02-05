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

package org.deidentifier.arx.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;
import org.junit.Test;

/**
 * Test for risk metrics.
 *
 * @author Fabian Prasser
 * @author Helmut Spengler
 */
public class TestAnonymizationConcatenation {
    
    /**
     * This class encapsulates parameters related to risk management. It can be used
     * for threshold definitions as well as for storing/retrieving results.
     * 
     * @author Fabian Prasser
     * @author Helmut Spengler
     */
    private class ParametersRisk {
        
        /** Threshold for the average risk */
        final private double averageRisk;;
        /** Threshold for the highest risk */
        final private double highestRisk;;
        /** Threshold for records at risk. */
        final private double recordsAtRisk;

        /** The quasi-identifiers */
        final private Set<String> qis;

        /**
         * Creates a new instance
         * @param qis
         */
        public ParametersRisk(double averageRisk, double highestRisk, double recordsAtRisk, Set<String> qis) {
            this.qis = qis;
            this.averageRisk   = averageRisk;
            this.highestRisk   = highestRisk;
            this.recordsAtRisk = recordsAtRisk;
        }
    }
    
    
    /**
     * Test the influence of concatenating anonymizations with overlapping qi definitions on the satisfaction of risk thresholds using the adult
     * dataset.
     * 
     * @throws IOException
     */
    @Test
    public void testAnonymizationConcatenation () throws IOException {
        
        performConcatenatedAnonymizations(
          "./data/adult.csv",
          new ParametersRisk[] {
              new ParametersRisk(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("sex", "age", "race", "marital-status", "education", "native-country", "workclass"))),
              new ParametersRisk(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("marital-status", "education", "native-country", "workclass", "occupation", "salary-class")))
          }
        );
    }
    
    /**
     * Concatenate anonymizations and check if the privacy guarantees of preceeding anonymizations are sustained.
     * 
     * @param dataset
     * @param anonymizations at least two have to be specified
     * @throws IOException if the input dataset cannot be read
     */
    private void performConcatenatedAnonymizations (String dataset, ParametersRisk... anonymizations) throws IOException {
        
        // Check arguments
        if (anonymizations.length < 2) {
            throw new IllegalArgumentException("Need to specify at least two anonymizations");
        }
        
        // Read input data
        Data indata = Data.create(dataset, StandardCharsets.UTF_8, ';');
        
        // Iterate over anonymizations
        for (int i = 0; i < anonymizations.length; i++) {
            
            System.out.println(i);
            
            // Perform cell suppression
            Data anondata = anonymize(indata, anonymizations[i]);
            
            // Validate results
            assessRisks(anondata, Arrays.copyOfRange(anonymizations, 0, i + 1));

            // Prepare for next iteration
            indata = anondata;
        }
    }
    
    private Data anonymize(Data data, ParametersRisk parametersRisk) {
        
        // Configure QIs
        configureQIs(data, parametersRisk.qis);

        // Setup anonymization
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        double o_min = 0.01d;
        double maxOutliers = 1.0d - o_min;
        config.setSuppressionLimit(maxOutliers);
        config.setQualityModel(Metric.createLossMetric(0d));
        if (parametersRisk.recordsAtRisk == 0d) {
            int k = getSizeThreshold(parametersRisk.highestRisk);
            if (k != 1) {
                config.addPrivacyModel(new KAnonymity(k));
            }
            config.addPrivacyModel(new AverageReidentificationRisk(parametersRisk.averageRisk));
        } else {
            config.addPrivacyModel(new AverageReidentificationRisk(parametersRisk.averageRisk, parametersRisk.highestRisk, parametersRisk.recordsAtRisk));
        }
        config.setHeuristicSearchEnabled(false);
        
        // Perform anonymization
        ARXResult result = null;
        try {
            result = anonymizer.anonymize(data, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Perform cell suppression
        DataHandle output = result.getOutput();
        if (result.isOptimizable(output)) {
            try {
                result.optimizeIterativeFast(output, o_min);
            } catch (RollbackRequiredException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Release handles
        Data anonData =  Data.create(result.getOutput().iterator());
        anonData.getHandle();
        result.getOutput().release();
        
        // Return result
        return anonData;
    }

    /**
         * Assess the re-identification risks of a dataset.
         * 
         * @param data the data
         * @param anonymizations the risk setttings for the previous anonmizations
         * @return
         * @throws IOException 
         */
    private void assessRisks(Data data, ParametersRisk... anonymizations) throws IOException {

        RiskEstimateBuilder builder = data.getHandle().getRiskEstimator();

        // For each concatenated anonymization
        for (int i = 0; i < anonymizations.length - 1; i++) {
            
            // Configure QIs
            configureQIs(data, anonymizations[i].qis);
            
            // Check wildcard risk
            RiskModelSampleWildcard riskModel = builder.getSampleBasedRiskSummaryWildcard(anonymizations[i].highestRisk, DataType.ANY_VALUE);
            checkRisk("wildcard" + i, riskModel.getHighestRisk(), riskModel.getAverageRisk(), riskModel.getRecordsAtRisk(), anonymizations[i]);

            // Check own category
            if ( i == anonymizations.length-1) {
                ProsecutorRisk riskModel2 = builder.getSampleBasedRiskSummary(anonymizations[i].highestRisk).getProsecutorRisk();
                checkRisk("own category" + i, riskModel2.getHighestRisk(), riskModel2.getSuccessRate(), riskModel2.getRecordsAtRisk(), anonymizations[i]);
            }
        }
    }
    
    /**
     * Check risks
     * @param message
     * @param highestRisk
     * @param averageRisk
     * @param recordsAtRisk
     * @param parametersRisk
     */
    private void checkRisk(String message,
                           double highestRisk,
                           double averageRisk,
                           double recordsAtRisk,
                           ParametersRisk parametersRisk) {

        assertTrue("Average risk (" + message + ")", averageRisk <= parametersRisk.averageRisk);
        if (recordsAtRisk == 0d) {
            assertTrue("Highest risk (" + message + ")", highestRisk <= parametersRisk.highestRisk);
        }
        assertTrue("Records at risk (" + message + ")", recordsAtRisk <= parametersRisk.recordsAtRisk);
    }

    /**
     * Configure the dataset to the given quasi-identifiers. The remaining attributes are configured as insensitive.
     * 
     * @param data
     * @param qis
     */
    private void configureQIs(Data data, Set<String> qis) {
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.INSENSITIVE_ATTRIBUTE);
        }       
        for (String qi : qis) {
            data.getDefinition().setAttributeType(qi, getHierarchy(data, qi));
        }
    }
   
    /**
	 * Returns a minimal class size for the given risk threshold.
	 * 
	 * @param threshold
	 * @return
	 */
	private int getSizeThreshold(double riskThreshold) {
		double size = 1d / riskThreshold;
		double floor = Math.floor(size);
		if ((1d / floor) - (1d / size) >= 0.01d * riskThreshold) {
			floor += 1d;
		}
		return (int) floor;
	}

	/**
	 * Returns the generalization hierarchy for the dataset and attribute
	 * 
	 * @param data
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	private Hierarchy getHierarchy(Data data, String attribute) {
		DefaultHierarchy hierarchy = Hierarchy.create();
		int col = data.getHandle().getColumnIndexOf(attribute);
		String[] values = data.getHandle().getDistinctValues(col);
		for (String value : values) {
			hierarchy.add(value, DataType.ANY_VALUE);
		}
		return hierarchy;
	}
}
