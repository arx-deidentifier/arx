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
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the NDS metric
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestMetricNDS extends TestAnonymizationAbstract {

    @Parameters
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                             
                 // Default NDS
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[86,84,84,82,77,77,77,77]", new int[] {0, 3, 0, 0, 2, 1, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[30,30,13,13,13,13,13,13]", new int[] {0, 3, 0, 0, 1, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[86,84,84,82,77,77,77,77]", new int[] {0, 3, 0, 0, 2, 1, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[41,40,35,34,18,18,18,18]", new int[] {0, 3, 0, 0, 1, 1, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,31,5,5]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,30,23,4,4,4,4]", new int[] {0, 3, 0, 0, 3, 1, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,31,5,5]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,30,23,4,4,4,4]", new int[] {0, 3, 0, 0, 3, 1, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,31,5,5]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,31,5,5]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 
                 // NDS with Suppression/Generalization: 0.0d
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,27,0,0]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,27,19,0,0,0,0]", new int[] {0, 3, 0, 0, 3, 1, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,27,0,0]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,27,19,0,0,0,0]", new int[] {0, 3, 0, 0, 3, 1, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,27,0,0]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,27,0,0]", new int[] {1, 4, 0, 0, 3, 1, 2, 1}, false)},

                 // NDS with Suppression/Generalization: 0.33d
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[11,10,9,5,0,0,0,0]", new int[] {0, 2, 0, 0, 1, 0, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[17,14,14,13,13,13,8,8]", new int[] {0, 1, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[11,10,9,5,0,0,0,0]", new int[] {0, 2, 0, 0, 1, 0, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[18,15,14,13,13,11,0,0]", new int[] {0, 1, 0, 0, 0, 0, 0, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[95,95,42,36,28,27,19,0]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[95,95,42,36,28,27,19,0]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false)},

                 // NDS with Suppression/Generalization: 0.66d
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[67,67,63,61,58,56,55,45]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[23,20,18,17,1,0,0,0]", new int[] {0, 3, 0, 0, 1, 1, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[67,67,63,61,58,56,55,45]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[31,30,26,23,21,3,0,0]", new int[] {0, 3, 0, 1, 2, 1, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[39,39,27,26,21,19,16,0]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[39,39,27,26,21,19,16,0]", new int[] {1, 3, 0, 1, 2, 1, 1, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false)},

                 // NDS with Suppression/Generalization: 1.0d
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 2, 1, 1, 1, 2, 1, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false)},
                 
                 // Weighted Default NDS
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,61,55,38,38]", new int[] {1, 4, 1, 2, 2, 1, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,31,15,15,15,15,15,15]", new int[] {1, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,61,55,38,38]", new int[] {1, 4, 1, 2, 2, 1, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,35,18,18,18,18,18]", new int[] {1, 4, 0, 0, 1, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,46,40,31]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,47,40,32,31,5,5]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,46,40,31]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,47,40,32,31,5,5]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,46,40,31]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric()).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,46,40,31]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 
                 // Weighted NDS with Suppression/Generalization: 0.0d
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 0, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,43,37,27]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,43,37,29,27,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,43,37,27]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,43,37,29,27,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,43,37,27]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[100,100,100,100,100,43,37,27]", new int[] {1, 4, 1, 1, 2, 1, 2, 1}, false))},

                 // Weighted NDS with Suppression/Generalization: 0.33d
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[17,11,7,5,2,1,0,0]", new int[] {1, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[25,9,8,8,8,8,5,5]", new int[] {0, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[17,11,7,5,2,1,0,0]", new int[] {1, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[24,9,9,8,7,6,0,0]", new int[] {0, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[97,95,42,36,28,27,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[97,95,42,36,28,27,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.33d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[97,97,97,96,96,94,0,0]", new int[] {0, 4, 0, 2, 3, 2, 2, 1}, false))},

                 // Weighted NDS with Suppression/Generalization: 0.66d
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[78,75,73,57,47,44,42,10]", new int[] {1, 4, 1, 2, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[46,26,14,13,11,10,8,0]", new int[] {1, 3, 0, 0, 0, 0, 0, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[78,75,73,57,47,44,42,10]", new int[] {1, 4, 1, 2, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[69,39,28,26,22,19,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[69,39,28,26,22,19,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[69,39,28,26,22,19,0,0]", new int[] {1, 4, 0, 1, 2, 1, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(0.66d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[68,67,66,63,60,36,24,0]", new int[] {0, 4, 1, 1, 3, 2, 2, 1}, false))},

                 // Weighted NDS with Suppression/Generalization: 1.0d
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 2, 1, 1, 1, 2, 1, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(1.0d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new KAnonymity(5)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new RecursiveCLDiversity("occupation", 4.0, 5)), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {0, 1, 1, 1, 3, 2, 2, 0}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
                 { weight(new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createNDSMetric(1.0d)).addCriterion(new HierarchicalDistanceTCloseness("occupation", 0.2, Hierarchy.create("../arx-data/data-junit/adult_hierarchy_occupation.csv", ';'))), "occupation", "../arx-data/data-junit/adult.csv", "[0,0,0,0,0,0,0,0]", new int[] {1, 4, 0, 2, 3, 1, 2, 1}, false))},
        });
    }
    
    /**
     * Apply weights to the test case
     * @return the test case passed to the method
     * @throws IOException 
     */
    private static ARXAnonymizationTestCase weight(ARXAnonymizationTestCase testcase) throws IOException {
        
        // Create temporary data object
        Data data = Data.create(testcase.dataset, ';');
        DataHandle handle = data.getHandle();
        
        // Weight attributes according to their order in the dataset
        for (int i=0; i<handle.getNumColumns(); i++) {
            testcase.config.setAttributeWeight(handle.getAttributeName(i), i+1);
        }
        
        // Return argument
        return testcase;
    }

    /**
     * Creates a new instance
     * @param testCase
     */
    public TestMetricNDS(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
