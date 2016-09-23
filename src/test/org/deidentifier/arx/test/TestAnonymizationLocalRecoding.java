/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.KMap.CellSizeEstimator;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for data transformations.
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationLocalRecoding extends AbstractAnonymizationTest {
    
    /**
     * Collection
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addCriterion(new EntropyLDiversity("occupation", 5)), "./data/adult.csv", "occupation", -527345078) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new DistinctLDiversity("Highest level of school completed", 5)), "./data/atus.csv", "Highest level of school completed", 23802617) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addCriterion(new RecursiveCLDiversity("Highest level of school completed", 4d, 3)), "./data/atus.csv", "Highest level of school completed", 1660794343) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(5)), "./data/adult.csv", "occupation", 471897083) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addCriterion(new EqualDistanceTCloseness("occupation", 0.2d)).addCriterion(new KAnonymity(100)), "./data/adult.csv", "occupation", 471897083) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addCriterion(new KAnonymity(100)), "./data/adult.csv", "occupation", 867928905) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addCriterion(new KAnonymity(5)), "./data/adult.csv", "occupation", 741013573) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addCriterion(new KMap(3, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "./data/adult.csv", "occupation", -1491241251) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.05d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addCriterion(new KMap(1000, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "./data/adult.csv", "occupation", 1709539182) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addCriterion(new EDDifferentialPrivacy(1.0d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/fars.csv", "", 482534106) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addCriterion(new DPresence(0.0, 0.2, DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))), "./data/fars.csv", "istatenum", -2090597576) },
                                              
        });
    }
    
    /**
     * Test
     * @param testCase
     */
    public TestAnonymizationLocalRecoding(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
