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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.junit.Test;

/**
 * Test for risk metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestRiskMetrics {
    
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
        double sampleUniqueness = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueRecords();
        
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
        double sampleUniqueness = handle.getRiskEstimator(ARXPopulationModel.create(handle.getNumRows(), 0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueRecords();
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
}
