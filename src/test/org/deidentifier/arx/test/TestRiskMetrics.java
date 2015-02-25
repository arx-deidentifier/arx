/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.junit.Test;

/**
 * Test for risk metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestRiskMetrics extends TestCase {

    /**
     * Returns the data object for a given dataset
     *
     * @param dataset the dataset
     * @return the data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Data getDataObject(final String dataset) throws IOException {

        final Data data = Data.create(dataset, ';');

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

                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
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
        double risk = provider.getData().getHandle().getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: "+risk, risk == 1.0d);

        // Risk after anonymization
        risk = getAnonymizedData(provider.getData()).getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: "+risk, risk == 0.42857142857142855);
    }

    /**
     * Test average risk using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAverageRisk2() throws IOException {
        Data data = getDataObject("../arx-data/data-junit/adult.csv");
        // Risk before anonymization
        double risk = data.getHandle().getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
        assertTrue("Is: " + risk, risk == 0.6465751607983555d);
        
        // Risk after anonymization
        risk = getAnonymizedData(data).getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getAverageRisk();
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

        double risk = handle.getRiskEstimator(ARXPopulationModel.create(0.1d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();

        // Risk before anonymization
        assertTrue("Is: "+risk, risk == 1.0d);
        assertTrue("Is: "+risk, risk <= handle.getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueTuples());

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        ARXResult result = null;
        try {
            result = anonymizer.anonymize(provider.getData(), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DataHandle outHandle = result.getOutput(false);

        risk = outHandle.getRiskEstimator(ARXPopulationModel.create(0.1d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue("Is: "+risk, risk == 0);
    }

    /**
     * Test decision rule using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDecisionRule2() throws IOException {

        Data data = getDataObject("../arx-data/data-junit/adult.csv");
        DataHandle handle = data.getHandle();

        double sampleRisk = handle.getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedUniquenessRisk().getFractionOfUniqueTuples();
        double populationRisk = handle.getRiskEstimator(ARXPopulationModel.create(0.1d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk == 0.2768499388373113);
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk <= sampleRisk);

        populationRisk = handle.getRiskEstimator(ARXPopulationModel.create(0.2d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk == 0.3577099234829125d);
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk <= sampleRisk);

        populationRisk = handle.getRiskEstimator(ARXPopulationModel.create(0.01d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk == 0.14460835311692927);
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk <= sampleRisk);
        
        populationRisk = handle.getRiskEstimator(ARXPopulationModel.create(1d)).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar();
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk == 0.5142895033485844d);
        assertTrue(populationRisk+"/"+sampleRisk, populationRisk == sampleRisk);
        
        // TODO: Include SNB model
    }

    /**
     * Test highest individual risk using the test dataset.
     */
    @Test
    public void testHighestIndividualRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        // Risk before anonymization
        assertTrue(provider.getData().getHandle().getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 1.0d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(provider.getData()).getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 0.5d);
    }

    /**
     * Test highest individual risk using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testHighestIndividualRisk2() throws IOException {
        Data data = getDataObject("../arx-data/data-junit/adult.csv");
        // Risk before anonymization
        assertTrue(data.getHandle().getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 1.0d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(data).getRiskEstimator(ARXPopulationModel.create(0.1d)).getSampleBasedReidentificationRisk().getHighestRisk() == 0.5d);
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
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

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
