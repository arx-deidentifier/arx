/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * Test for utility metrics
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TestUtilityEstimationAbstract extends TestUtilityMetricsAbstract {

    /**
     * Creates a new instance
     * 
     * @param testCase
     */
    public TestUtilityEstimationAbstract(final ARXUtilityMetricsTestCase testcase) {
        super(testcase);
    }

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
     * Tests the result
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
                    if (node.getMaximumInformationLoss().compareTo(node.getMinimumInformationLoss()) != 0) {
                        
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
     * Applies and checks a transformation
     * @param testcase 
     * @param result
     * @param node
     */
    private void checkTransformation(ARXUtilityMetricsTestCase testcase, ARXResult result, ARXNode node) {
        InformationLoss<?> min = node.getMinimumInformationLoss();
        InformationLoss<?> max = node.getMaximumInformationLoss();
        result.getOutput(node, false);
        assertTrue("Min != max", node.getMinimumInformationLoss().compareTo(node.getMaximumInformationLoss())==0);
        if (min.compareTo(node.getMaximumInformationLoss())>0) {
            System.out.println(min+"/"+node.getMaximumInformationLoss()+"/"+max);
            System.out.println("Anonymity:"+node.getAnonymity()+"/Supp:"+testcase.config.isSuppressionAlwaysEnabled());
            System.out.println(testcase.getDescription());
        }
        assertTrue("Actual < min", min.compareTo(node.getMaximumInformationLoss())<=0);
        assertTrue("Actual > max", max.compareTo(node.getMaximumInformationLoss())>=0);
    }

    /**
     * Tests all estimates within a lattice
     * @param lattice
     */
    private void checkLattice(ARXLattice lattice) {
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                assertTrue("Min > max", node.getMinimumInformationLoss().compareTo(node.getMaximumInformationLoss())<=0);
            }
        }
        
    }
}
