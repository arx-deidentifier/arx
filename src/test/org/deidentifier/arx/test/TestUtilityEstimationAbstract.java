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

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.metric.InformationLoss;
import org.junit.Test;

/**
 * Test for utility metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TestUtilityEstimationAbstract extends TestUtilityMetricsAbstract {

    /**
     * Creates a new instance.
     *
     * @param testcase
     */
    public TestUtilityEstimationAbstract(final ARXUtilityMetricsTestCase testcase) {
        super(testcase);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.test.TestUtilityMetricsAbstract#test()
     */
    @Test
    public void test() throws IOException {

        // Anonymize
        Data data = getDataObject(testcase);
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        
        // With suppression for anonymous transformation
        ARXResult result = anonymizer.anonymize(data, testcase.config);
        checkResult(testcase, result);
        data.getHandle().release();
        
        // Without suppression for non-anonymous transformations
        testcase.config.setSuppressionAlwaysEnabled(false);
        result = anonymizer.anonymize(data, testcase.config);
        checkResult(testcase, result);
    }

    /**
     * Tests the result.
     *
     * @param testcase
     * @param result
     */
    private void checkResult(ARXUtilityMetricsTestCase testcase, ARXResult result) {

        // First check the initial estimates
        checkLattice(result.getLattice());

        // Test estimates after checking some transformations
        for (ARXNode[] level : result.getLattice().getLevels()) {
            for (ARXNode node : level) {

                String label = Arrays.toString(node.getTransformation());
                if (testcase.informationLoss.containsKey(label)) {
                    if (compareWithTolerance(node.getMaximumInformationLoss(), node.getMinimumInformationLoss()) != 0) {
                        
                        // Check transformation and test bounds
                        checkTransformation(testcase, result, node);
                        
                        // Check the whole lattice
                        checkLattice(result.getLattice());
                    }
                }
            }
        }
    }

    /**
     * Applies and checks a transformation.
     *
     * @param testcase
     * @param result
     * @param node
     */
    private void checkTransformation(ARXUtilityMetricsTestCase testcase, ARXResult result, ARXNode node) {
        InformationLoss<?> min = node.getMinimumInformationLoss();
        InformationLoss<?> max = node.getMaximumInformationLoss();
        result.getOutput(node, false);
        assertTrue("Min != max", compareWithTolerance(node.getMinimumInformationLoss(), node.getMaximumInformationLoss())==0);
        assertTrue("Actual < min", compareWithTolerance(min, node.getMaximumInformationLoss())<=0);
        assertTrue("Actual > max", compareWithTolerance(max, node.getMaximumInformationLoss())>=0);
    }

    /**
     * Tests all estimates within a lattice.
     *
     * @param lattice
     */
    private void checkLattice(ARXLattice lattice) {
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                assertTrue("Min > max", compareWithTolerance(node.getMinimumInformationLoss(), node.getMaximumInformationLoss())<=0);
            }
        }
    }

    /**
     * Compares two losses with tolerance.
     *
     * @param loss1
     * @param loss2
     * @return
     */
    private int compareWithTolerance(InformationLoss<?> loss1, InformationLoss<?> loss2) {
        String s1 = loss1.toString();
        String s2 = loss2.toString();
        if (isNumeric(s1)) {
            Double d1 = Double.valueOf(s1);
            Double d2 = Double.valueOf(s2);
            return compareWithTolerance(d1, d2);
        } else {
            return loss1.compareTo(loss2);
        }
    }
    
    /**
     * Returns whether the given string is numeric.
     *
     * @param str
     * @return
     */
    private boolean isNumeric(String str) {
      return str.matches("-?\\d+(\\.\\d+)?");
    }
    
    /**
     * Compares two doubles with tolerance.
     *
     * @param d1
     * @param d2
     * @return
     */
    private int compareWithTolerance(double d1, double d2) {
        if (closeEnough(d1, d2)) return 0;
        else return Double.compare(d1, d2);
    }
    
    /**
     * Compares double for "equality" with a tolerance of 1 ulp.
     *
     * @param d1
     * @param d2
     * @return
     */
    private boolean closeEnough(double d1, double d2) {
        return Math.abs(d2 - d1) <= Math.max(Math.ulp(d1), Math.ulp(d2));
    }
}
