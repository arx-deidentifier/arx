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
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.metric.Metric;
import org.junit.Test;
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
public class TestClassificationTCloseness extends TestAnonymizationAbstract {

    @Parameters
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false,new int[] {4320, 2780, 5, 4315, 0, 0, 6}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false,new int[] {4320, 605, 5, 4315, 0, 0, 6}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 377622.78458729334d, new int[] { 1, 4, 0, 0, 3, 2, 2, 1 }, false,new int[] {4320, 2777, 12, 4305, 3, 0, 14}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, false,new int[] {4320, 603, 12, 4306, 2, 0, 14}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 4.56853172E8d, new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false,new int[] {4320, 25, 2, 4318, 0, 0, 3}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 4.56853172E8d, new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false,new int[] {4320, 25, 2, 4318, 0, 0, 3}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 398400.07418064494d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false,new int[] {4320, 31, 5, 4315, 0, 0, 6}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 398400.07418064494d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false,new int[] {4320, 31, 5, 4315, 0, 0, 6}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true,new int[] {4320, 31, 4, 27, 1, 4288, 5}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true,new int[] {4320, 31, 4, 27, 1, 4288, 5}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true,new int[] {4320, 50, 11, 39, 3, 4267, 13}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true,new int[] {4320, 50, 11, 39, 3, 4267, 13}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false,new int[] {8748, 8340, 9, 8733, 6, 0, 11}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false,new int[] {8748, 4097, 7, 8733, 8, 0, 9}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5760138.103541854d, new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false,new int[] {8748, 32, 6, 8742, 0, 0, 4}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5760138.103541854d, new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false,new int[] {8748, 32, 6, 8742, 0, 0, 4}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true,new int[] {8748, 35, 6, 29, 9, 8704, 8}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true,new int[] {8748, 35, 6, 29, 9, 8704, 8}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1407619.3716609066d, new int[] { 3, 4, 1, 0, 0, 4, 4 }, false,new int[] {9000, 2740, 1059, 7941, 0, 0, 1060}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1509368.88828439d, new int[] { 3, 4, 1, 1, 0, 4, 4 }, false,new int[] {9000, 882, 719, 8281, 0, 0, 720}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2023751.2434216265d, new int[] { 4, 4, 1, 2, 1, 4, 4 }, false,new int[] {9000, 27, 4, 8996, 0, 0, 4}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2032837.6390798881d, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false,new int[] {9000, 24, 3, 8997, 0, 0, 3}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1407669.5060439722d, new int[] { 3, 4, 1, 0, 0, 4, 2 }, true,new int[] {9000, 276, 133, 143, 923, 7801, 134}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1516301.8703843413d, new int[] { 3, 4, 1, 1, 0, 2, 1 }, true,new int[] {9000, 259, 132, 127, 587, 8154, 133}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 4.2929731E7d, new int[] { 0, 2, 3, 2, 1, 2, 1 }, false,new int[] {5184, 4149, 195, 4825, 164, 0, 196}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 9.2944831E7d, new int[] { 0, 2, 3, 3, 0, 2, 2 }, false,new int[] {5184, 1747, 172, 4890, 122, 0, 173}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 7.80794309E8d, new int[] { 1, 2, 3, 3, 1, 2, 2 }, false,new int[] {5184, 25, 5, 5179, 0, 0, 4}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 7.80794309E8d, new int[] { 1, 2, 3, 3, 1, 2, 2 }, false,new int[] {5184, 25, 5, 5179, 0, 0, 4}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 4.2929731E7d, new int[] { 0, 2, 3, 2, 1, 2, 1 }, true,new int[] {5184, 118, 50, 68, 310, 4756, 51}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 9.2944831E7d, new int[] { 0, 2, 3, 3, 0, 2, 2 }, true,new int[] {5184, 135, 59, 76, 235, 4814, 60}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false,new int[] {12960, 12490, 108, 12852, 0, 0, 109}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false,new int[] {12960, 8228, 108, 12852, 0, 0, 109}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false,new int[] {12960, 27, 108, 12852, 0, 0, 7}) },
                { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false,new int[] {12960, 27, 108, 12852, 0, 0, 7}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true,new int[] {12960, 27, 6, 21, 102, 12831, 7}) },
                { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true,new int[] {12960, 27, 6, 21, 102, 12831, 7}) },

        });
    }

    public TestClassificationTCloseness(final ARXTestCase testCase) {
        super(testCase);
    }

    @Override
    @Test
    public void test() throws IOException {

        // TODO: Ugly hack!
        if (!testCase.config.containsCriterion(TCloseness.class)) {
            final Hierarchy hierarchy = Hierarchy.create(testCase.dataset.substring(0, testCase.dataset.length() - 4) + "_hierarchy_" + testCase.sensitiveAttribute + ".csv", ';');
            testCase.config.addCriterion(new HierarchicalDistanceTCloseness(testCase.sensitiveAttribute, 0.2d, hierarchy));
        }
        super.test();

    }

}
