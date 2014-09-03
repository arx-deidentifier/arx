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
public class TestAnonymizationKAnonymity extends TestAnonymizationAbstract {

    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 255559.854557311d, new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 379417.3460570992d, new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 407289.53889252973d, new int[] { 1, 2, 1, 1, 3, 2, 2, 1, 1 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 453196.8932458746d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1, 1 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/adult.csv", 255559.854557311d, new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 }, true) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/adult.csv", 379417.3460570992d, new int[] { 1, 1, 1, 1, 3, 2, 2, 1, 1 }, true) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 1764006.4033760326d, new int[] { 2, 4, 0, 1, 0, 4, 4, 4 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 1994002.8308631128d, new int[] { 3, 4, 1, 1, 0, 4, 4, 4 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 2445878.4248346775d, new int[] { 4, 4, 1, 1, 1, 4, 4, 4 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 2517471.5816586106d, new int[] { 5, 4, 1, 0, 1, 4, 4, 4 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/cup.csv", 1764006.4033760326d, new int[] { 2, 4, 0, 1, 0, 4, 4, 4 }, true) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/cup.csv", 2001343.4737485615d, new int[] { 3, 4, 1, 1, 0, 1, 2, 1 }, true) },
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 4469271.0d, new int[] { 0, 2, 2, 2, 1, 2, 1, 0 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 5.6052481E7d, new int[] { 0, 2, 3, 3, 1, 2, 2, 2 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 1.42377891E8d, new int[] { 1, 2, 3, 3, 1, 2, 1, 2 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 4.36925397E8d, new int[] { 5, 2, 3, 3, 1, 2, 0, 2 }, false) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "../arx-data/data-junit/fars.csv", 4469271.0d, new int[] { 0, 2, 2, 2, 1, 2, 1, 0 }, true) }, 
        { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "../arx-data/data-junit/fars.csv", 5.6052481E7d, new int[] { 0, 2, 3, 3, 1, 2, 2, 2 }, true) },

        });
    }

    public TestAnonymizationKAnonymity(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }

}
