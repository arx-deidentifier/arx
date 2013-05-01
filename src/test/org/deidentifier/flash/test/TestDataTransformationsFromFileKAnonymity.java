/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
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
public class TestDataTransformationsFromFileKAnonymity extends
        TestDataTransformationsFromFileAbstract {

    @Parameters
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

                { new TestCaseResult(5,
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     255559.854557311d,
                                     new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 },
                                     false) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     379417.3460570992d,
                                     new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 },
                                     false) },
                { new TestCaseResult(5,
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     407289.53889252973d,
                                     new int[] { 1, 2, 1, 1, 3, 2, 2, 1, 1 },
                                     false) },
                { new TestCaseResult(100,
                                     0.0d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     453196.8932458746d,
                                     new int[] { 0, 4, 1, 1, 3, 2, 2, 1, 1 },
                                     false) },
                { new TestCaseResult(5,
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     255559.854557311d,
                                     new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 },
                                     true) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/adult.csv",
                                     Metric.ENTROPY,
                                     379417.3460570992d,
                                     new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 },
                                     true) },
                { new TestCaseResult(5,
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1764006.4033760326d,
                                     new int[] { 2, 4, 0, 1, 0, 4, 4, 4 },
                                     false) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1994002.8308631128d,
                                     new int[] { 3, 4, 1, 1, 0, 4, 4, 4 },
                                     false) },
                { new TestCaseResult(5,
                                     0.0d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     2445878.4248346775d,
                                     new int[] { 4, 4, 1, 1, 1, 4, 4, 4 },
                                     false) },
                { new TestCaseResult(100,
                                     0.0d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     2517471.5816586106d,
                                     new int[] { 5, 4, 1, 0, 1, 4, 4, 4 },
                                     false) },
                { new TestCaseResult(5,
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     1764006.4033760326d,
                                     new int[] { 2, 4, 0, 1, 0, 4, 4, 4 },
                                     true) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/cup.csv",
                                     Metric.NMENTROPY,
                                     2001343.4737485615d,
                                     new int[] { 3, 4, 1, 1, 0, 1, 2, 1 },
                                     true) },
                { new TestCaseResult(5,
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     4469271.0d,
                                     new int[] { 0, 2, 2, 2, 1, 2, 1, 0 },
                                     false) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     5.6052481E7d,
                                     new int[] { 0, 2, 3, 3, 1, 2, 2, 2 },
                                     false) },
                { new TestCaseResult(5,
                                     0.0d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     1.42377891E8d,
                                     new int[] { 1, 2, 3, 3, 1, 2, 1, 2 },
                                     false) },
                { new TestCaseResult(100,
                                     0.0d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     4.36925397E8d,
                                     new int[] { 5, 2, 3, 3, 1, 2, 0, 2 },
                                     false) },
                { new TestCaseResult(5,
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     4469271.0d,
                                     new int[] { 0, 2, 2, 2, 1, 2, 1, 0 },
                                     true) },
                { new TestCaseResult(100,
                                     0.04d,
                                     "data/fars.csv",
                                     Metric.DMSTAR,
                                     5.6052481E7d,
                                     new int[] { 0, 2, 3, 3, 1, 2, 2, 2 },
                                     true) },

        });
    }

    public TestDataTransformationsFromFileKAnonymity(final TestCaseResult testCase) {
        super(testCase);
    }

    // old:
    // { new TestCaseResult(2, 0.0d, "data/test", 3, 17.0, new int[] { 1, 1, 2
    // }) },
    // { new TestCaseResult(5, 0.0d, "data/adult", 9, 3.3627534E7d, new int[] {
    // 1, 1, 1, 2, 3, 2, 2, 1, 1 }) },
    // { new TestCaseResult(6, 0.02d, "data/adult", 9, 2300532.0d, new int[] {
    // 1, 0, 1, 2, 3, 2, 2, 0, 1 }) },
    // { new TestCaseResult(7, 0.0d, "data/cup", 8, 3.01506905E8d, new int[] {
    // 4, 4, 1, 1, 1, 4, 4, 4 }) },
    // { new TestCaseResult(8, 0.04d, "data/cup", 8, 4082603.0d, new int[] { 3,
    // 1, 1, 1, 0, 2, 3, 4 }) },
    // { new TestCaseResult(9, 0.0d, "data/fars", 8, 1.42377891E8d, new int[] {
    // 1, 2, 3, 3, 1, 2, 1, 2 }) },
    // { new TestCaseResult(4, 0.0d, "data/ihis", 9, 1.88718942E8d, new int[] {
    // 0, 0, 2, 3, 0, 2, 0, 1, 1 }) },
    // { new TestCaseResult(5, 0.0d, "data/atus", 9, 3.724480741E9d, new int[] {
    // 0, 5, 0, 2, 2, 1, 2, 0, 2 }) }, // ATUS
    // // result is
    // // different
    // // than
    // // fastanon-paper;
    // // metric
    // // same

    @Override
    @Test
    public void testTestCases() throws IllegalArgumentException, IOException {

        final Data data = createDataObject(testCase);
        final org.deidentifier.flash.metric.Metric<?> metric = createMetric(testCase);

        // Create an instance of the anonymizer
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer(metric);
        anonymizer.setPracticalMonotonicity(testCase.practical);

        // Execute the algorithm
        final FLASHResult result = anonymizer.kAnonymize(data,
                                                         testCase.k,
                                                         testCase.relativeMaxOutliers);

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
