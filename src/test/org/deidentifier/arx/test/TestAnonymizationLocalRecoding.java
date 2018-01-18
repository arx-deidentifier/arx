/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
 * Test for local recoding
 *
 * @author Fabian Prasser
 */
@RunWith(Parameterized.class)
public class TestAnonymizationLocalRecoding extends AbstractAnonymizationTest {
    
    /**
     * Returns the test cases.
     * 
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new EntropyLDiversity("occupation", 5)), "./data/adult.csv", "occupation", -998962150) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new DistinctLDiversity("Highest level of school completed", 5)), "./data/atus.csv", "Highest level of school completed", 1662433089) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new RecursiveCLDiversity("Highest level of school completed", 4d, 3)), "./data/atus.csv", "Highest level of school completed", 1141779920) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new EqualDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", "occupation", 464405537) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new EqualDistanceTCloseness("occupation", 0.2d)).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", "occupation", -1306447515) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new KAnonymity(100)), "./data/adult.csv", "occupation", 484469846) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new KAnonymity(5)), "./data/adult.csv", "occupation", -1231665634) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new KMap(3, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "./data/adult.csv", "occupation", -715168499) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new KMap(1000, 0.01d, ARXPopulationModel.create(Region.USA), CellSizeEstimator.ZERO_TRUNCATED_POISSON)), "./data/adult.csv", "occupation", 2130163653) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new EDDifferentialPrivacy(1.0d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true)), "./data/fars.csv", "", 482534106) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric(0.05d)).addPrivacyModel(new DPresence(0.0, 0.2, DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))), "./data/fars.csv", "istatenum", 505248650) },
                                              
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param testCase
     */
    public TestAnonymizationLocalRecoding(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
    
}
