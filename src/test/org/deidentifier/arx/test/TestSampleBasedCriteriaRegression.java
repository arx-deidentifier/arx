/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2025 Fabian Prasser and contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.junit.Test;

/**
 * Regression tests for combinations of sample-based criteria enforced on one shared distribution.
 * Criteria are stored in a HashSet, so each configuration is run repeatedly to cover both orders.
 */
public class TestSampleBasedCriteriaRegression {

    /** Repetitions per configuration */
    private static final int RUNS = 20;

    /** 240 singletons, 1 doubleton, 3 classes of 40; one quasi-identifier pinned to level 0 */
    private static Data getData() {
        Data.DefaultData data = Data.create();
        data.add("zip");
        DefaultHierarchy hierarchy = Hierarchy.create();
        for (int i = 0; i < 240; i++) {
            data.add("1" + i);
            hierarchy.add("1" + i, "*");
        }
        data.add("2");
        data.add("2");
        hierarchy.add("2", "*");
        for (int c = 0; c < 3; c++) {
            for (int i = 0; i < 40; i++) {
                data.add("3" + c);
            }
            hierarchy.add("3" + c, "*");
        }
        data.getDefinition().setAttributeType("zip", hierarchy);
        data.getDefinition().setMaximumGeneralization("zip", 0);
        return data;
    }

    /** Returns the sizes of all unsuppressed classes */
    private static Map<String, Integer> getClasses(DataHandle output) {
        Map<String, Integer> classes = new HashMap<>();
        for (int row = 0; row < output.getNumRows(); row++) {
            if (!output.isOutlier(row)) {
                classes.merge(output.getValue(row, 0), 1, Integer::sum);
            }
        }
        return classes;
    }

    @Test
    public void testSampleUniquenessAndAverageRisk() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new SampleUniqueness(0d));
            config.addPrivacyModel(new AverageReidentificationRisk(0.03d));
            config.setSuppressionLimit(0.7d);
            ARXResult result = new ARXAnonymizer().anonymize(getData(), config);
            Map<String, Integer> classes = getClasses(result.getOutput());
            int records = 0;
            for (int size : classes.values()) {
                assertTrue("Unique record in output", size > 1);
                records += size;
            }
            assertEquals("Suppressed records", 242, 362 - records);
            assertTrue("Average risk", (double) classes.size() / records <= 0.03d);
        }
    }

    @Test
    public void testPopulationUniquenessAndAverageRisk() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            Data data = getData();
            ARXPopulationModel population = ARXPopulationModel.create(362, 0.1d);
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new PopulationUniqueness(0.05d, PopulationUniquenessModel.DANKAR, population));
            config.addPrivacyModel(new AverageReidentificationRisk(0.03d));
            config.setSuppressionLimit(0.7d);
            ARXResult result = new ARXAnonymizer().anonymize(data, config);
            DataHandle output = result.getOutput();
            Map<String, Integer> classes = getClasses(output);
            int records = 0;
            for (int size : classes.values()) {
                records += size;
            }
            assertTrue("Average risk", (double) classes.size() / records <= 0.03d);
            assertTrue("Population uniqueness",
                       output.getRiskEstimator(population).getPopulationBasedUniquenessRisk().getFractionOfUniqueTuplesDankar() <= 0.05d);
        }
    }
}
