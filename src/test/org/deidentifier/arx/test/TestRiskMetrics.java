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
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.risk.RiskEstimator;
import org.junit.Assert;
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
                if (name.matches(dataset.substring(dataset.lastIndexOf("/") + 1, dataset.length() - 4) +
                                 "_hierarchy_(.)+.csv")) {
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
        assertTrue(provider.getData().getHandle().getRiskEstimator().getEquivalenceClassRisk() == 1.0d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(provider.getData()).getRiskEstimator().getEquivalenceClassRisk()  == 0.42857142857142855d);
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
        assertTrue(data.getHandle().getRiskEstimator().getEquivalenceClassRisk() == 0.6465751607983555d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(data).getRiskEstimator().getEquivalenceClassRisk() == 0.001922949406538028);
    }

    /**
     * Test decision rule using the test dataset.
     */
    @Test
    public void testDecisionRule() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        DataHandle handle = provider.getData().getHandle();

        RiskEstimator estimator = new RiskEstimator(handle);
        double risk = estimator.getPopulationUniquesRisk();

        // Risk before anonymization
        assertTrue(risk == 1.0d);

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

        RiskEstimator estimator2 = new RiskEstimator(outHandle);
        try {
            estimator2.getPopulationUniquesRisk();
        } catch (final IllegalStateException e) {
            return;
        }
        Assert.fail();

    }

    /**
     * Test decision rule using the adult dataset.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDecissionRule2() throws IOException {

        Data data = getDataObject("../arx-data/data-junit/adult.csv");
        DataHandle handle = data.getHandle();

        double risk = handle.getRiskEstimator().getPopulationUniquesRisk();
        assertTrue(risk == 0.27684993883831804);

        risk = handle.getRiskEstimator(0.2).getPopulationUniquesRisk();
        assertTrue(risk == 0.3577099234829125d);

        risk = handle.getRiskEstimator(0.01).getPopulationUniquesRisk();
        assertTrue(risk == 0.14460835311745512);

        risk = handle.getRiskEstimator(1).getPopulationUniquesRisk();
        assertTrue(risk == 0.5142895033485844d);
    }

    /**
     * Test highest individual risk using the test dataset.
     */
    @Test
    public void testHighestIndividualRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        // Risk before anonymization
        assertTrue(provider.getData().getHandle().getRiskEstimator().getHighestIndividualRisk() == 1.0d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(provider.getData()).getRiskEstimator().getHighestIndividualRisk() == 0.5d);
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
        assertTrue(data.getHandle().getRiskEstimator().getHighestIndividualRisk() == 1.0d);

        // Risk after anonymization
        assertTrue(getAnonymizedData(data).getRiskEstimator().getHighestIndividualRisk() == 0.5d);
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
