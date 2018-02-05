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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;
import org.junit.Test;

/**
 * Test for risk metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Helmut Spengler
 */
public class TestRiskMetrics {
	
	private static String MAGIC_NULL_VALUE = "*";
    
    /**
     * Returns the data object for a given dataset
     *
     * @param dataset the dataset
     * @return the data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Data getDataObject(final String dataset) throws IOException {
        
        final Data data = Data.create(dataset, StandardCharsets.UTF_8, ';');
        
        // Read generalization hierachies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(dataset.substring(dataset.lastIndexOf("/") + 1, dataset.length() - 4) + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        final File testDir = new File(dataset.substring(0, dataset.lastIndexOf("/")));
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        
        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                final String attributeName = matcher.group(1);
                
                // use all found attribute hierarchies as qis
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
                
            }
        }
        
        return data;
    }
    
    /**
     * Test average risk using the example dataset.
     */
    @Test
    public void testAverageRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        // Risk before anonymization
        double risk = provider.getData().getHandle().getRiskEstimator(ARXPopulationModel.create(provider.getData().getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: " + risk, risk == 1.0d);
        
        // Risk after anonymization
        risk = getAnonymizedData(provider.getData()).getRiskEstimator(ARXPopulationModel.create(provider.getData().getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: " + risk, risk == 0.42857142857142855);
    }
    
    /**
     * Test average risk using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAverageRisk2() throws IOException {
        Data data = getDataObject("./data/adult.csv");
        // Risk before anonymization
        double risk = data.getHandle().getRiskEstimator(ARXPopulationModel.create(data.getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: " + risk, risk == 0.6465751607983555d);
        
        // Risk after anonymization
        risk = getAnonymizedData(data).getRiskEstimator(ARXPopulationModel.create(data.getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: " + risk, risk == 0.001922949406538028);
    }
    
    /**
     * Test decision rule using the test dataset.
     */
    @Test
    public void testDecisionRule() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        DataHandle handle = provider.getData().getHandle();
        
        RiskModelPopulationUniqueness model = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.2d)).getPopulationBasedUniquenessRisk();
        double populationUniqueness = model.getFractionOfUniqueTuplesDankar();
        double sampleUniqueness = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueTuples();
        
        // Risk before anonymization
        assertTrue(sampleUniqueness + " / " + populationUniqueness, compareUniqueness(populationUniqueness, 1.0d) == 0);
        assertTrue(sampleUniqueness + " / " + populationUniqueness, compareUniqueness(populationUniqueness, sampleUniqueness) <= 0);
        
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);
        
        ARXResult result = null;
        try {
            result = anonymizer.anonymize(provider.getData(), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DataHandle outHandle = result.getOutput(false);
        
        populationUniqueness = outHandle.getRiskEstimator(ARXPopulationModel.create(provider.getData().getHandle().getNumRows(), 0.1d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue("Is: " + populationUniqueness, compareUniqueness(populationUniqueness, 0) == 0);
    }
    
    /**
     * Test decision rule using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDecisionRule2() throws IOException {
        
        Data data = getDataObject("./data/adult.csv");
        DataHandle handle = data.getHandle();
        
        RiskModelPopulationUniqueness model = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.1d)).getPopulationBasedUniquenessRisk();
        double sampleUniqueness = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueTuples();
        double populationUniqueness = model.getFractionOfUniqueTuplesDankar();
        
        if (model.getPopulationUniquenessModel() == PopulationUniquenessModel.PITMAN) {
            assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, 0.27684993883653597) == 0);
        } else if (model.getPopulationUniquenessModel() == PopulationUniquenessModel.ZAYATZ) {
            assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, 0.3207402393466189) == 0);
        } else {
            fail("Unexpected convergence of SNB");
        }
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, sampleUniqueness) <= 0);
        
        model = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.2d)).getPopulationBasedUniquenessRisk();
        populationUniqueness = model.getFractionOfUniqueTuplesDankar();
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, 0.3577099234829125d) == 0);
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, sampleUniqueness) <= 0);
        
        model = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.01d)).getPopulationBasedUniquenessRisk();
        populationUniqueness = model.getFractionOfUniqueTuplesDankar();
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, 0.1446083531167384) == 0);
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, sampleUniqueness) <= 0);
        
        model = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 1d)).getPopulationBasedUniquenessRisk();
        populationUniqueness = model.getFractionOfUniqueTuplesDankar();
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, 0.5142895033485844) == 0);
        assertTrue(populationUniqueness + "/" + sampleUniqueness, compareUniqueness(populationUniqueness, sampleUniqueness) == 0);
    }
    
    /**
     * Compares two uniqueness measures with four significant digits
     * @param val1
     * @param val2
     * @return
     */
    private int compareUniqueness(double val1, double val2) {
        return Integer.compare((int) (val1 * 10000d), (int) (val2 * 10000d));
    }
    
    /**
     * Test highest individual risk using the test dataset.
     */
    @Test
    public void testHighestIndividualRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        // Risk before anonymization
        assertTrue(provider.getData().getHandle().getRiskEstimator(ARXPopulationModel.create(provider.getData().getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 1.0d);
        
        // Risk after anonymization
        assertTrue(getAnonymizedData(provider.getData()).getRiskEstimator(ARXPopulationModel.create(provider.getData().getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 0.5d);
    }
    
    /**
     * Test highest individual risk using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testHighestIndividualRisk2() throws IOException {
        Data data = getDataObject("./data/adult.csv");
        // Risk before anonymization
        assertTrue(data.getHandle().getRiskEstimator(ARXPopulationModel.create(data.getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 1.0d);
        
        // Risk after anonymization
        assertTrue(getAnonymizedData(data).getRiskEstimator(ARXPopulationModel.create(data.getHandle().getNumRows(), 0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 0.5d);
    }

    
    /**
     * Test the influence of concatenating anonymizations with overlapping qi definitions on the satisfaction of risk thresholds using the adult
     * dataset without allowing any records at risk.
     * 
     * @throws IOException
     */
    @Test
    public void testAnonymizationConcatenationWithoutRecordsAtRisk () throws IOException {
    	performAndTestAnonymizationConcatenation(0.2d, 0.05, 0d);
    }

    
    /**
     * Test the influence of concatenating anonymizations with overlapping qi definitions on the satisfaction of risk thresholds using the adult
     * dataset allowing records at risk.
     * 
     * @throws IOException
     */
    @Test
    public void testAnonymizationConcatenationWithRecordsAtRisk () throws IOException {
    	performAndTestAnonymizationConcatenation(0.333d, 0.2, 0.1d);
    }
    
    /**
     * Perform concatenated anonymizations with overlapping qi definitions and validate the results.
     * 
     * @param recordsAtRisk
     * @throws IOException 
     */
    private void performAndTestAnonymizationConcatenation(double highestRisk, double averageRisk, double recordsAtRisk) throws IOException {
    	
    	// Configure 1st anonymization
    	String inputdata = "./data/adult.csv";
    	ParametersRisk parametersRisk1 = new ParametersRisk(new HashSet<String>(Arrays.asList("sex", "age", "race", "marital-status", "education", "native-country", "workclass")));
    	parametersRisk1.setHighestRisk(highestRisk);
    	parametersRisk1.setAverageRisk(averageRisk);
    	parametersRisk1.setRecordsAtRisk(recordsAtRisk);
    	String anondata1 = "./data/adult-anon1.csv";
    	
    	// Configure 2nd anonymization
    	ParametersRisk parametersRisk2 = new ParametersRisk(new HashSet<String>(Arrays.asList("marital-status", "education", "native-country", "workclass", "occupation", "salary-class")));
    	parametersRisk2.setHighestRisk(highestRisk);
    	parametersRisk2.setAverageRisk(averageRisk);
    	parametersRisk2.setRecordsAtRisk(recordsAtRisk);
    	String anondata2 = "./data/adult-anon2.csv";
    	
    	// Perform anonymizations
    	anonymizeData(inputdata, parametersRisk1, anondata1);    	
    	anonymizeData(anondata1, parametersRisk2, anondata2);
    	
    	// Assess with wild card method
    	parametersRisk1.setUseWcMatch(true);
    	ParametersRisk assessmentWc1 = assessRisks(anondata1, parametersRisk1);
    	ParametersRisk assessmentWc2 = assessRisks(anondata2, parametersRisk1);
    	
    	// Assess with own category method
    	parametersRisk1.setUseWcMatch(false);
    	ParametersRisk assessmentOc1 = assessRisks(anondata1, parametersRisk1);
    	ParametersRisk assessmentOc2 = assessRisks(anondata2, parametersRisk1);
    	
    	
    	if (recordsAtRisk == 0d) {
    		// Assessment after 1st anonymization
    		// Wild card
    		assertTrue (assessmentWc1.getHighestRisk() <= highestRisk);
    		assertTrue (assessmentWc1.getAverageRisk() <= averageRisk);
    		// Own category
    		assertTrue (assessmentOc1.getHighestRisk() <= highestRisk);
    		assertTrue (assessmentOc1.getAverageRisk() <= averageRisk);    		
    		
    		// Assessment after 2nd anonymization
    		// Wild card
    		assertTrue (assessmentWc2.getHighestRisk() <= assessmentWc1.getHighestRisk());
    		assertTrue (assessmentWc2.getAverageRisk() <= assessmentWc1.getAverageRisk());
    		// Own category
    		assertTrue (assessmentOc2.getHighestRisk() >  highestRisk);
    		assertTrue (assessmentOc2.getAverageRisk() >  assessmentOc1.getAverageRisk());
    	} else {
    		// Assessment after 1st anonymization
    		// Wild card
        	assertTrue (assessmentWc1.getRecordsAtRisk() <= recordsAtRisk);
    		// Own category
        	assertTrue (assessmentOc1.getRecordsAtRisk() <=  recordsAtRisk);    		

    		// Assessment after 2nd anonymization
    		// Wild card
        	assertTrue (assessmentWc2.getRecordsAtRisk() <= assessmentWc1.getRecordsAtRisk());
    		// Own category
        	assertTrue (assessmentOc2.getRecordsAtRisk() >  recordsAtRisk);
    	}
    }
    
    /**
     * 2-Anonymizes the given data. No suppression allowed.
     *
     * @param data the data
     * @return the anonymized data
     */
    private DataHandle getAnonymizedData(Data data) {
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);
        config.setQualityModel(Metric.createLossMetric(AggregateFunction.RANK));
        
        ARXResult result = null;
        try {
            result = anonymizer.anonymize(data, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DataHandle outHandle = result.getOutput(false);
        return outHandle;
    }
    
    /**
     * Anonymize a given dataset in order to satisfy given risk thresholds using the
     * quasi-identifiers defined in parametersRisk.
     *
     * @param data the input dataset
     * @param parametersRisk the parameters related to the risk thresholds
     * @param ouputdataset the output dataset
     * @throws IOException 
     */
	private void anonymizeData(String inputdataset, ParametersRisk parametersRisk, String ouputdataset) throws IOException {
		

		final Data data = readData(inputdataset, parametersRisk);
		
		// Configure anonymization
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
		ARXConfiguration config = ARXConfiguration.create();
		double o_min = 0.01d;
		double maxOutliers = 1.0d - o_min;
		config.setSuppressionLimit(maxOutliers);
		config.setQualityModel(Metric.createLossMetric(0d));
		if (parametersRisk.getRecordsAtRisk() == 0d) {
			int k = getSizeThreshold(parametersRisk.getHighestRisk());
		    if (k != 1) {
		        config.addPrivacyModel(new KAnonymity(k));
		    }
			config.addPrivacyModel(new AverageReidentificationRisk(parametersRisk.getAverageRisk()));
		} else {
			config.addPrivacyModel(new AverageReidentificationRisk(parametersRisk.getAverageRisk(), parametersRisk.getHighestRisk(), parametersRisk.getRecordsAtRisk()));
		}
		config.setHeuristicSearchEnabled(false);
        
		// Perform anonymization
        ARXResult result = null;
        try {
            result = anonymizer.anonymize(data, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataHandle output = result.getOutput();
		if (result.isOptimizable(output)) {
		    try {
                result.optimizeIterativeFast(output, o_min);
            } catch (RollbackRequiredException e) {
                throw new RuntimeException(e);
            }
		}
		
		// Write output
		Iterator<String[]> iterator = result.getOutput().iterator();
		PrintWriter writer = new PrintWriter(ouputdataset, "UTF-8");
		while (iterator.hasNext()) {
			String[] line = iterator.next();
			for (int i = 0; i < data.getHandle().getNumColumns() - 1; i++) {
				writer.print(line[i] + ";");
			}
			writer.println(line[data.getHandle().getNumColumns() - 1]);
		}
		writer.close();
	}

	/**
	 * Create ARX dataset with trivial generalization hierarchies from a file.
	 * 
	 * @param dataset
	 * @param parametersRisk
	 * @return
	 * @throws IOException
	 */
	private Data readData(String dataset, ParametersRisk parametersRisk) throws IOException {
		final Data data = Data.create(dataset, StandardCharsets.UTF_8, ';');
        int numColunns = data.getHandle().getNumColumns();
        String[] attributes = new String[numColunns];
        for (int i = 0; i < numColunns; i++) {
        	attributes[i] = data.getHandle().getAttributeName(i);
		}
		
		// Configure QI settings
		for (String attribute : attributes) {
		    data.getDefinition().setAttributeType(attribute, AttributeType.INSENSITIVE_ATTRIBUTE);
		}		
		for (String qi : parametersRisk.getQis()) {
			data.getDefinition().setAttributeType(qi, getHierarchy(data, qi));
		}
		return data;
	}
    
    /**
     * Assess the re-identification risks of a dataset.
     * 
     * @param data the data
     * @param whether to interpret missing values as wild cards or as an own category
     * @param the maximum highest risk based on which the records at risk are determined
     * @param riskSettings the risk related parameters
     * @return
     * @throws IOException 
     */
    private ParametersRisk assessRisks(String dataset, ParametersRisk parametersRisk) throws IOException {
    	
    	final Data data = readData(dataset, parametersRisk);

		RiskEstimateBuilder builder = data.getHandle().getRiskEstimator();
		ParametersRisk result = new ParametersRisk(data.getDefinition().getQuasiIdentifyingAttributes());
        
		if (parametersRisk.useWcMatch()) {
		    // Treat missing values as wild card
            RiskModelSampleWildcard riskModel = builder.getSampleBasedRiskSummaryWildcard(parametersRisk.getHighestRisk(), MAGIC_NULL_VALUE);
            result.setHighestRisk(riskModel.getHighestRisk());
            result.setRecordsAtRisk(riskModel.getRecordsAtRisk());
            result.setAverageRisk(riskModel.getAverageRisk());
            
		} else {
		    // Treat missing values as own category
            ProsecutorRisk pr = builder.getSampleBasedRiskSummary(parametersRisk.getHighestRisk()).getProsecutorRisk();
            result.setHighestRisk(pr.getHighestRisk());
            result.setRecordsAtRisk(pr.getRecordsAtRisk());
            result.setAverageRisk(pr.getSuccessRate());
            
		}
		return result;
    	
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
			hierarchy.add(value, MAGIC_NULL_VALUE);
		}
		return hierarchy;
	}
}
