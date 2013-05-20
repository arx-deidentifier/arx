/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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
import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.ARXConfiguration.TClosenessCriterion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for data transformations
 * 
 * @author Prasser, Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestDataTransformationsFromFileTCloseness extends
        TestDataTransformationsFromFileAbstract {

    @Parameters
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     3.11880088E8d,
                                     new int[] { 1, 4, 1, 0, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     3.11880088E8d,
                                     new int[] { 1, 4, 1, 0, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     377622.78458729334d,
                                     new int[] { 1, 4, 0, 0, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     377622.78458729334d,
                                     new int[] { 1, 4, 0, 0, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     4.56853172E8d,
                                     new int[] { 1, 4, 1, 1, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     4.56853172E8d,
                                     new int[] { 1, 4, 1, 1, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     398400.07418064494d,
                                     new int[] { 0, 4, 1, 1, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     398400.07418064494d,
                                     new int[] { 0, 4, 1, 1, 3, 2, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     3.11880088E8d,
                                     new int[] { 1, 4, 1, 0, 3, 2, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.DMSTAR,
                                     3.11880088E8d,
                                     new int[] { 1, 4, 1, 0, 3, 2, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     390436.40440636495d,
                                     new int[] { 1, 4, 1, 0, 3, 1, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "occupation",
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     390436.40440636495d,
                                     new int[] { 1, 4, 1, 0, 3, 1, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.04d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5267622.788737194d,
                                     new int[] { 0, 5, 0, 1, 2, 2, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.04d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5267622.788737194d,
                                     new int[] { 0, 5, 0, 1, 2, 2, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.0d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5760138.103541854d,
                                     new int[] { 0, 5, 0, 2, 2, 2, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.0d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5760138.103541854d,
                                     new int[] { 0, 5, 0, 2, 2, 2, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.04d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5267622.788737194d,
                                     new int[] { 0, 5, 0, 1, 2, 2, 2, 2 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "Highest level of school completed",
                                     0.04d,
                                     "data/atus.csv",
                                     Metric.ENTROPY,
                                     5267622.788737194d,
                                     new int[] { 0, 5, 0, 1, 2, 2, 2, 2 },
                                     true) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1407619.3716609066d,
                                     new int[] { 3, 4, 1, 0, 0, 4, 4 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1498155.6562763213d,
                                     new int[] { 3, 4, 1, 1, 0, 4, 4 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.0d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     2023751.2434216265d,
                                     new int[] { 4, 4, 1, 2, 1, 4, 4 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.0d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     2032837.6390798881d,
                                     new int[] { 5, 4, 1, 0, 1, 4, 4 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1407669.5060439722d,
                                     new int[] { 3, 4, 1, 0, 0, 4, 2 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "RAMNTALL",
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1502663.8003313122d,
                                     new int[] { 3, 4, 1, 1, 0, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     4.2929731E7d,
                                     new int[] { 0, 2, 3, 2, 1, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     5.7765525E7d,
                                     new int[] { 0, 2, 3, 3, 1, 2, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.0d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     7.80794309E8d,
                                     new int[] { 1, 2, 3, 3, 1, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.0d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     7.80794309E8d,
                                     new int[] { 1, 2, 3, 3, 1, 2, 2 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     4.2929731E7d,
                                     new int[] { 0, 2, 3, 2, 1, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_HIERARCHICAL,
                                     "istatenum",
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     5.7765525E7d,
                                     new int[] { 0, 2, 3, 3, 1, 2, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.04d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.04d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.0d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.0d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     false) },
                { new TestCaseResult(0.2,
                                     5,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.04d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     true) },
                { new TestCaseResult(0.2,
                                     100,
                                     EqualDistanceTCloseness.EMD_EQUAL,
                                     "EDUC",
                                     0.04d,
                                     "data/ihis.csv",
                                     Metric.NMENTROPY,
                                     1.4719292081181683E7d,
                                     new int[] { 0, 0, 0, 3, 4, 2, 0, 1 },
                                     true) },

        });
    }

    public TestDataTransformationsFromFileTCloseness(final TestCaseResult testCase) {
        super(testCase);
    }

    // old:
    // { new TestCaseResult(0.2, 5, "occupation", 0.04d, "data/adult",
    // Metric.ENTROPY, 400997.81985981413d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1
    // }) },
    // { new TestCaseResult(0.2, 100, TClosenessCriterion.EMD_EQUAL, "EDUC",
    // 0.04, "data/ihis.csv", Metric.ENTROPY, 1.4719292081181683E7d, new int[] {
    // 0, 0, 0, 3, 4, 2, 0, 1 }) }

    @Override
    @Test
    public void testTestCases() throws IOException {

        final Data data = createDataObject(testCase);
        final org.deidentifier.arx.metric.Metric<?> metric = createMetric(testCase);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer(metric);
        anonymizer.setPracticalMonotonicity(testCase.practical);

        ARXResult result = null;

        // Execute the algorithm
        switch (testCase.tClosenessCriterion) {
        case EMD_EQUAL:
            result = anonymizer.tClosify(data,
                                         testCase.k,
                                         testCase.t,
                                         testCase.relativeMaxOutliers);
            break;

        case EMD_HIERARCHICAL:
            final Hierarchy hierarchy = Hierarchy.create(testCase.dataset.substring(0,
                                                                                    testCase.dataset.length() - 4) +
                                                                 "_hierarchy_" +
                                                                 testCase.senstitiveAttribute +
                                                                 ".csv",
                                                         ';');
            result = anonymizer.tClosify(data,
                                         testCase.k,
                                         testCase.t,
                                         testCase.relativeMaxOutliers,
                                         hierarchy);
            break;

        }

        // check if no solution possible
        if (testCase.bestResult == null) {
            assertTrue(result.getGlobalOptimum() == null);
        } else {

            assertEquals(testCase.dataset +
                                 "-should: " +
                                 testCase.optimalInformationLoss +
                                 "is: " +
                                 result.getGlobalOptimum()
                                       .getMinimumInformationLoss()
                                       .getValue(),
                         result.getGlobalOptimum()
                               .getMinimumInformationLoss()
                               .getValue(),
                         testCase.optimalInformationLoss);
            assertTrue(testCase.dataset +
                               "-should: " +
                               Arrays.toString(testCase.bestResult) +
                               "is: " +
                               Arrays.toString(result.getGlobalOptimum()
                                                     .getTransformation()),
                       Arrays.equals(result.getGlobalOptimum()
                                           .getTransformation(),
                                     testCase.bestResult));
        }
    }
}
