/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for data transformations
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestClassificationKAnonymity extends TestAnonymizationAbstract {

    @Parameters
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 255559.854557311d, new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 }, false,new int[] {12960, 2773, 4387, 8573, 0, 0, 1366}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 379417.3460570992d, new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 }, false,new int[] {12960, 728, 467, 12493, 0, 0, 250}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 407289.53889252973d, new int[] { 1, 2, 1, 1, 3, 2, 2, 1, 1 }, false,new int[] {12960, 170, 86, 12874, 0, 0, 51}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 453196.8932458746d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1, 1 }, false,new int[] {12960, 63, 22, 12938, 0, 0, 15}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 255559.854557311d, new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 }, true,new int[] {12960, 2773, 1365, 8573, 3022, 0, 1366}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 379417.3460570992d, new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 }, true,new int[] {12960, 728, 249, 12493, 218, 0, 250}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 1764006.4033760326d, new int[] { 2, 4, 0, 1, 0, 4, 4, 4 }, false,new int[] {45000, 10408, 10075, 34925, 0, 0, 10076}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 1994002.8308631128d, new int[] { 3, 4, 1, 1, 0, 4, 4, 4 }, false,new int[] {45000, 3134, 2950, 42050, 0, 0, 2951}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 2445878.4248346775d, new int[] { 4, 4, 1, 1, 1, 4, 4, 4 }, false,new int[] {45000, 40, 9, 44991, 0, 0, 6}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 2517471.5816586106d, new int[] { 5, 4, 1, 0, 1, 4, 4, 4 }, false,new int[] {45000, 31, 3, 44997, 0, 0, 3}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 1764006.4033760326d, new int[] { 2, 4, 0, 1, 0, 4, 4, 4 }, true,new int[] {45000, 797, 464, 34925, 9611, 0, 465}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 2001343.4737485615d, new int[] { 3, 4, 1, 1, 0, 1, 2, 1 }, true,new int[] {45000, 402, 218, 42050, 2732, 0, 219}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 4469271.0d, new int[] { 0, 2, 2, 2, 1, 2, 1, 0 }, false,new int[] {20736, 2443, 11687, 9049, 0, 0, 1130}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 5.6052481E7d, new int[] { 0, 2, 3, 3, 1, 2, 2, 2 }, false,new int[] {20736, 1763, 2602, 18134, 0, 0, 725}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 1.42377891E8d, new int[] { 1, 2, 3, 3, 1, 2, 1, 2 }, false,new int[] {20736, 136, 76, 20660, 0, 0, 46}) },
            { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 4.36925397E8d, new int[] { 5, 2, 3, 3, 1, 2, 0, 2 }, false,new int[] {20736, 74, 22, 20714, 0, 0, 17}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 4469271.0d, new int[] { 0, 2, 2, 2, 1, 2, 1, 0 }, true,new int[] {20736, 2443, 1129, 9049, 10558, 0, 1130}) },
            { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 5.6052481E7d, new int[] { 0, 2, 3, 3, 1, 2, 2, 2 }, true,new int[] {20736, 1763, 724, 18134, 1878, 0, 725}) },
        });
    }

    public TestClassificationKAnonymity(final ARXTestCase testCase) {
        super(testCase);
    }

}
