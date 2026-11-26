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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.KAnonymity;
import org.junit.Test;

/**
 * Regression test for delta-disclosure privacy: classes that omit a sensitive value which is
 * present in the dataset must be rejected, also when outliers are merged by local recoding.
 */
public class TestDDisclosurePrivacyRegression {

    @Test
    public void testDDisclosureLocalRecoding() throws Exception {

        // Global distribution s1 = 0.25, s2 = 0.25, s3 = 0.5
        Data.DefaultData data = Data.create();
        data.add("zip", "sens");
        String[][] classes = { { "A", "s1" }, { "A", "s2" }, { "A", "s2" }, { "A", "s2" },
                               { "A", "s3" }, { "A", "s3" }, { "A", "s3" }, { "A", "s3" },
                               { "A1", "s2" }, { "A2", "s3" }, { "A3", "s3" }, { "A4", "s3" } };
        for (String[] row : classes) data.add(row);
        for (int i = 0; i < 5; i++) data.add("B", "s1");
        for (int i = 0; i < 2; i++) data.add("B", "s2");
        for (int i = 0; i < 5; i++) data.add("B", "s3");
        DefaultHierarchy hierarchy = Hierarchy.create();
        hierarchy.add("A", "A", "*");
        hierarchy.add("B", "B", "*");
        for (int i = 1; i <= 4; i++) hierarchy.add("A" + i, "A", "*");
        data.getDefinition().setAttributeType("zip", hierarchy);
        data.getDefinition().setAttributeType("sens", AttributeType.SENSITIVE_ATTRIBUTE);

        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(4));
        config.addPrivacyModel(new DDisclosurePrivacy("sens", 1.1d));
        config.setSuppressionLimit(0.4d);
        ARXResult result = new ARXAnonymizer().anonymize(data, config);
        DataHandle output = result.getOutput();
        result.optimize(output);

        // Every published class must satisfy |log2(freq(s, c) / freq(s, T))| < delta for all s in T
        Map<String, Map<String, Integer>> counts = new HashMap<>();
        Map<String, Integer> sizes = new HashMap<>();
        for (int row = 0; row < output.getNumRows(); row++) {
            if (!output.isOutlier(row)) {
                String key = output.getValue(row, 0);
                counts.computeIfAbsent(key, k -> new HashMap<>()).merge(output.getValue(row, 1), 1, Integer::sum);
                sizes.merge(key, 1, Integer::sum);
            }
        }
        double[] global = { 0.25d, 0.25d, 0.5d };
        for (String key : counts.keySet()) {
            for (int s = 0; s < 3; s++) {
                double frequency = (double) counts.get(key).getOrDefault("s" + (s + 1), 0) / sizes.get(key);
                double value = Math.abs(Math.log(frequency / global[s]) / Math.log(2));
                assertTrue("Class " + key + " violates delta-disclosure for s" + (s + 1), value < 1.1d);
            }
        }
    }
}
