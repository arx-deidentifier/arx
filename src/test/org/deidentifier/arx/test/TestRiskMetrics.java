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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.risk.RiskEstimator;
import org.junit.Test;

/**
 * Test for risk metrics
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestRiskMetrics extends TestCase {

    @Test
    public void testHighestIndividualRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        DataHandle handle = provider.getData().getHandle();

        RiskEstimator estimator = new RiskEstimator(handle);
        double risk = estimator.getHighestIndividualRisk();

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
        double riskAnon = estimator2.getHighestIndividualRisk();
        // Risk after anonymization
        assertTrue(riskAnon == 0.5d);
    }

    @Test
    public void testAverageRisk() {
        DataProvider provider = new DataProvider();
        provider.createDataDefinition();
        DataHandle handle = provider.getData().getHandle();

        RiskEstimator estimator = new RiskEstimator(handle);
        double risk = estimator.getEquivalenceClassRisk();

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

        System.out.println(" - Transformed data:");
        final Iterator<String[]> transformed = outHandle.iterator();
        while (transformed.hasNext()) {
            System.out.print("   ");
            System.out.println(Arrays.toString(transformed.next()));
        }

        RiskEstimator estimator2 = new RiskEstimator(outHandle);
        double riskAnon = estimator2.getEquivalenceClassRisk();

        // Risk after anonymization
        assertTrue(riskAnon == 0.4444444d);
    }
}
