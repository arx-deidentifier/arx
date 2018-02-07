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

import org.apache.commons.math3.random.RandomDataGenerator;
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
import org.deidentifier.arx.io.CSVDataOutput;
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
    private class Risks {
        
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
        public Risks(double averageRisk, double highestRisk, double recordsAtRisk, Set<String> qis) {
            this.qis = qis;
            this.averageRisk   = averageRisk;
            this.highestRisk   = highestRisk;
            this.recordsAtRisk = recordsAtRisk;
        }

        @Override
        public String toString() {
            return "Risks [averageRisk=" + averageRisk + ", highestRisk=" + highestRisk +
                   ", recordsAtRisk=" + recordsAtRisk + ", qis=" + qis + "]";
        }
    }
    
    /**
     * This class encapsulates a number of concatenation scenarios.
     * 
     * @author Helmut Spengler
     *
     */
    private class TestCase {

        /** The name of the input data file. */
        private final String dataset;
        /** The concatenation scenarios */
        private final Risks[] scenarios;
        
        /**
         * Creates a new instance.
         * @param dataset
         * @param scenarios
         */
        public TestCase(String dataset, Risks... scenarios) {
            this.dataset = dataset;
            this.scenarios = scenarios;
        }
    }
    
    
    /**
     * Test the influence of concatenating anonymizations with overlapping qi definitions on the satisfaction of risk thresholds using the adult
     * dataset.
     * 
     * @throws IOException
     */
    @Test
    public void testStaticAnonymizationConcatenation () throws IOException {

        TestCase[] tests = new TestCase[] { new TestCase("./data/adult.csv",
                                                         new Risks(0.2, 0.05, 0.05, new HashSet<String>(Arrays.asList("sex", "age", "race", "marital-status"))),
                                                         new Risks(0.3, 0.1, 0.1, new HashSet<String>(Arrays.asList("marital-status", "education", "native-country"))),
                                                         new Risks(0.2, 0.05, 0.05, new HashSet<String>(Arrays.asList("native-country", "workclass", "occupation", "salary-class"))),
                                                         new Risks(0.1, 0.3, 0.1, new HashSet<String>(Arrays.asList("age", "race", "marital-status", "education")))),
                                            new TestCase("./data/adult.csv",
                                                         new Risks(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("sex", "age", "race", "marital-status"))),
                                                         new Risks(0.3, 0.1, 0, new HashSet<String>(Arrays.asList("marital-status", "education", "native-country"))),
                                                         new Risks(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("native-country", "workclass", "occupation", "salary-class"))),
                                                         new Risks(0.1, 0.3, 0, new HashSet<String>(Arrays.asList("age", "race", "marital-status", "education")))),
                                            new TestCase("./data/adult.csv", new Risks(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("sex", "age", "race"))),
                                                         new Risks(0.2, 0.05, 0.2, new HashSet<String>(Arrays.asList("age", "race", "marital-status"))),
                                                         new Risks(0.2, 0.05, 0, new HashSet<String>(Arrays.asList("race", "marital-status", "education"))),
                                                         new Risks(0.3, 0.1, 0.05, new HashSet<String>(Arrays.asList("marital-status", "education", "native-country"))),
                                                         new Risks(0.3, 0.1, 0, new HashSet<String>(Arrays.asList("education", "native-country", "workclass"))),
                                                         new Risks(0.2, 0.05, 0.3, new HashSet<String>(Arrays.asList("native-country", "workclass", "occupation"))),
                                                         new Risks(0.1, 0.3, 0.1, new HashSet<String>(Arrays.asList("workclass", "occupation", "salary-class")))) };

        for (TestCase test : tests) {
            performConcatenatedAnonymizations(test);
        }
    }
    
    @Test
    public void testRandomizedAnonymizationConcatenation() throws IOException {
        
        // Parameters
        Set<String> qis = new HashSet<String>(Arrays.asList("sex", "age", "race", "marital-status", "education", "native-country", "workclass", "occupation", "salary-class"));
        int numAnons = 5;
        int numQisPerAnon = 3;
        double[] rangeHighestRisk  = { 0.0d, 0.5d };
        double[] rangeAverageRisk  = { 0.0d, 0.3d };
        double[] rangeRecordsAtRisk= { 0.0d, 0.3d };

        // Initialize random data generator
        RandomDataGenerator rdg = new RandomDataGenerator();
        rdg.reSeed(42);
        
        // Initialize test case
        TestCase tc = new TestCase("./data/adult.csv", new Risks[numAnons]);
        for (int i = 0; i < numAnons; i++) {
            // Get random values
            Set<String> randomQis = new HashSet<String>(Arrays.asList(Arrays.stream(rdg.nextSample(qis, numQisPerAnon)).toArray(String[]::new)));
            
            // TODO ggf. Normalverteilung mit bspw. Mittelwert = 0.1 verwenden
            // TODO ggf. mit Hilfe der Binomialverteilung ab und zu einen risk-value von 0 erzeugen
            double randomHighestRisk = rdg.nextUniform(rangeHighestRisk[0], rangeHighestRisk[1], true);
            double randomAverageRisk = rdg.nextUniform(rangeAverageRisk[0], rangeAverageRisk[1], true);
            double randomRecordsAtRisk = rdg.nextUniform(rangeRecordsAtRisk[0], rangeRecordsAtRisk[1], true);
            
            // Configure test case
            tc.scenarios[i] = new Risks(randomAverageRisk, randomHighestRisk, randomRecordsAtRisk, randomQis);
        }
        
        // Perform tests
        performConcatenatedAnonymizations(tc);
    }
    
    /**
     * Concatenate anonymizations and check if the privacy guarantees of preceeding anonymizations are sustained.
     * 
     * @param dataset
     * @param anonymizations at least two have to be specified
     * @throws IOException if the input dataset cannot be read
     */
    private void performConcatenatedAnonymizations (TestCase test) throws IOException {
        
        // Read input data
        Data inputData = Data.create(test.dataset, StandardCharsets.UTF_8, ';');
        
        // Iterate over anonymizations
        for (int i = 0; i < test.scenarios.length; i++) {
            
            System.out.println("Step: " + i);
            
            // Perform cell suppression
            Data outputData = anonymize(inputData, test.scenarios[i]);
            
            // Validate results
            assessRisks(outputData, Arrays.copyOfRange(test.scenarios, 0, i + 1));

            // Prepare for next iteration
            inputData = outputData;
            
            new CSVDataOutput("tmp.csv").write(outputData.getHandle().iterator());
        }
    }
    
    /**
     * Anonymize
     * @param data
     * @param risks
     * @return
     * @throws IOException 
     */
    private Data anonymize(Data data, Risks risks) throws IOException {
        
        // Configure QIs
        configureQIs(data, risks.qis);

        // Setup anonymization
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        double o_min = 0.01d;
        double maxOutliers = 1.0d - o_min;
        config.setSuppressionLimit(maxOutliers);
        config.setQualityModel(Metric.createLossMetric(0d));
        if (risks.recordsAtRisk == 0d) {
            int k = getSizeThreshold(risks.highestRisk);
            if (k != 1) {
                config.addPrivacyModel(new KAnonymity(k));
            }
            config.addPrivacyModel(new AverageReidentificationRisk(risks.averageRisk));
        } else {
            config.addPrivacyModel(new AverageReidentificationRisk(risks.averageRisk, risks.highestRisk, risks.recordsAtRisk));
        }
        config.setHeuristicSearchEnabled(false);
        
        // Perform anonymization
        ARXResult result = anonymizer.anonymize(data, config);
        
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
        Data anonData =  Data.create(output.iterator());
        anonData.getHandle();
        output.release();
        
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
    private void assessRisks(Data data, Risks... anonymizations) throws IOException {

        // For each concatenated anonymization
        for (int i = 0; i < anonymizations.length; i++) {
            
            // Configure QIs
            configureQIs(data, anonymizations[i].qis);
            RiskEstimateBuilder builder = data.getHandle().getRiskEstimator();

            System.out.println(" - Substep: " + i);
            System.out.println(" - " + anonymizations[i]);

            // Check wildcard risk
            RiskModelSampleWildcard riskModel = builder.getSampleBasedRiskSummaryWildcard(anonymizations[i].highestRisk, DataType.ANY_VALUE);
            checkRisk("Wildcard", riskModel.getHighestRisk(), riskModel.getAverageRisk(), riskModel.getRecordsAtRisk(), anonymizations[i]);

            // Check own category
            if (i == anonymizations.length - 1) {
                ProsecutorRisk riskModel2 = builder.getSampleBasedRiskSummary(anonymizations[i].highestRisk).getProsecutorRisk();
                checkRisk("Own category", riskModel2.getHighestRisk(), riskModel2.getSuccessRate(), riskModel2.getRecordsAtRisk(), anonymizations[i]);
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
                           Risks parametersRisk) {
        
        assertTrue("Average risk (" + message + ") - actual vs. specified: " + averageRisk + "/" + parametersRisk.averageRisk, averageRisk <= parametersRisk.averageRisk);
        if (recordsAtRisk == 0d) {
            assertTrue("Highest risk (" + message + ")", highestRisk <= parametersRisk.highestRisk);
        }
        assertTrue("Records at risk (" + message + ")", recordsAtRisk <= parametersRisk.recordsAtRisk);
        
        System.out.println(message + "  " + highestRisk + ", " + averageRisk + ", " + recordsAtRisk);
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
