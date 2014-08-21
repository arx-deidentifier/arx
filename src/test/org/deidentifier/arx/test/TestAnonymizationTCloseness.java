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
public class TestAnonymizationTCloseness extends TestAnonymizationAbstract {

    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {

        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 377622.78458729334d, new int[] { 1, 4, 0, 0, 3, 2, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 4.56853172E8d, new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false) },
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 4.56853172E8d, new int[] { 1, 4, 1, 1, 3, 2, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 398400.07418064494d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false) },
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 398400.07418064494d, new int[] { 0, 4, 1, 1, 3, 2, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 3.11880088E8d, new int[] { 1, 4, 1, 0, 3, 2, 2, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true) },
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new KAnonymity(100)), "occupation", "../arx-data/data-junit/adult.csv", 390436.40440636495d, new int[] { 1, 4, 1, 0, 3, 1, 2, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5760138.103541854d, new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5760138.103541854d, new int[] { 0, 5, 0, 2, 2, 2, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(5)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyMetric()).addCriterion(new EqualDistanceTCloseness("Highest level of school completed", 0.2d)).addCriterion(new KAnonymity(100)), "Highest level of school completed", "../arx-data/data-junit/atus.csv", 5267622.788737194d, new int[] { 0, 5, 0, 1, 2, 2, 2, 2 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1407619.3716609066d, new int[] { 3, 4, 1, 0, 0, 4, 4 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1509368.88828439d, new int[] { 3, 4, 1, 1, 0, 4, 4 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2023751.2434216265d, new int[] { 4, 4, 1, 2, 1, 4, 4 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 2032837.6390798881d, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(5)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1407669.5060439722d, new int[] { 3, 4, 1, 0, 0, 4, 2 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new KAnonymity(100)), "RAMNTALL", "../arx-data/data-junit/cup.csv", 1516301.8703843413d, new int[] { 3, 4, 1, 1, 0, 2, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 4.2929731E7d, new int[] { 0, 2, 3, 2, 1, 2, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 9.2944831E7d, new int[] { 0, 2, 3, 3, 0, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 7.80794309E8d, new int[] { 1, 2, 3, 3, 1, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 7.80794309E8d, new int[] { 1, 2, 3, 3, 1, 2, 2 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(5)), "istatenum", "../arx-data/data-junit/fars.csv", 4.2929731E7d, new int[] { 0, 2, 3, 2, 1, 2, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createDMStarMetric()).addCriterion(new KAnonymity(100)), "istatenum", "../arx-data/data-junit/fars.csv", 9.2944831E7d, new int[] { 0, 2, 3, 3, 0, 2, 2 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.0d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(5)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true) }, 
        { new ARXTestCase(ARXConfiguration.create(0.04d, Metric.createNMEntropyMetric()).addCriterion(new EqualDistanceTCloseness("EDUC", 0.2d)).addCriterion(new KAnonymity(100)), "EDUC", "../arx-data/data-junit/ihis.csv", 1.4719292081181683E7d, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, true) },

        });
    }

    public TestAnonymizationTCloseness(final ARXTestCase testCase) {
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
