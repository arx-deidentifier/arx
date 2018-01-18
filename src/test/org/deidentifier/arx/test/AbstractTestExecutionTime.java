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

import java.io.IOException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.junit.Test;

/**
 * Test for data transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractTestExecutionTime extends AbstractAnonymizationTest {

    /**
     * Creates a new instance.
     *
     * @param testCase
     */
    public AbstractTestExecutionTime(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }

    /**
     * Performs the test
     * 
     * @throws IOException
     */
    @Test
    public void test() throws IOException {

        final Data data = getDataObject(testCase);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        testCase.config.setPracticalMonotonicity(testCase.practical);

        // Warm up
        System.out.println("Experiment:");
        System.out.println(" - Dataset: " + testCase.dataset);
        System.out.println(" - Utility measure: " + testCase.config.getQualityModel().toString());
        System.out.println(" - Practical monotonicity: " + testCase.practical);
        System.out.println(" - Suppression limit: " + testCase.config.getSuppressionLimit());
        System.out.println(" - Privacy model: " + getPrivacyModel(testCase.config));
        System.out.println(" - Performing experiment:");
        System.out.println("   * Warmup");
        long time = System.currentTimeMillis();
        ARXResult result = anonymizer.anonymize(data, testCase.config);
        System.out.println("   * Performed in: " + (System.currentTimeMillis() - time) + " [ms]");

        // Collect statistics
        int[] statistics = new int[7];
        for (ARXNode[] level : result.getLattice().getLevels()) {
            for (ARXNode arxNode : level) {
                statistics[0]++;
                if (arxNode.isChecked()) {
                    statistics[1]++;
                }
                if (arxNode.getAnonymity() == Anonymity.ANONYMOUS) {
                    statistics[2]++;
                }
                if (arxNode.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                    statistics[3]++;
                }
                if (arxNode.getAnonymity() == Anonymity.PROBABLY_ANONYMOUS) {
                    statistics[4]++;
                }
                if (arxNode.getAnonymity() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
                    statistics[5]++;
                }
                if (arxNode.getHighestScore() == arxNode.getLowestScore()) {
                    statistics[6]++;
                }
            }
        }
        System.out.println(getClassification(statistics));
        
        // Repeat
        time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            System.out.println("   * Repetition " + (i + 1) + " of 10");
            data.getHandle().release();
            result = anonymizer.anonymize(data, testCase.config);
        }
        time = (System.currentTimeMillis() - time) / 10;


        System.out.println("     -> Anonymization performed in: " + time + " [ms]");
    }

    /**
     * Returns a string representing the privacy model
     */
    private String getPrivacyModel(ARXConfiguration config) {
        StringBuilder result = new StringBuilder();
        result.append("{");
        int num = config.getPrivacyModels().size();
        int count = 0;
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            result.append(c.toString());
            if (++count < num) {
                result.append(", ");
            }
        }
        result.append("}");
        return result.toString();
    }
}
