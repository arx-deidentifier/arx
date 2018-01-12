/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.Test;

/**
 * Tests the classification of the solution space.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestSolutionSpaceClassification extends AbstractTest {
    
    /**
     * Performs a test.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testNMEntropy() throws IllegalArgumentException, IOException {
        
        Data data = Data.create("data/adult.csv", StandardCharsets.UTF_8, ';');
        data.getDefinition().setAttributeType("sex", Hierarchy.create("data/adult_hierarchy_sex.csv", StandardCharsets.UTF_8, ';'));
        data.getDefinition().setAttributeType("age", Hierarchy.create("data/adult_hierarchy_age.csv", StandardCharsets.UTF_8, ';'));
        data.getDefinition().setAttributeType("race", Hierarchy.create("data/adult_hierarchy_race.csv", StandardCharsets.UTF_8, ';'));
        data.getDefinition().setAttributeType("education", Hierarchy.create("data/adult_hierarchy_education.csv", StandardCharsets.UTF_8, ';'));
        data.getDefinition().setAttributeType("marital-status", Hierarchy.create("data/adult_hierarchy_marital-status.csv", StandardCharsets.UTF_8, ';'));
        
        DataSelector selector = DataSelector.create(data).field("sex").equals("Male");
        DataSubset subset = DataSubset.create(data, selector);
        
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(5));
        config.addPrivacyModel(new Inclusion(subset));
        config.setSuppressionLimit(0.02d);
        config.setQualityModel(Metric.createEntropyMetric(false));
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(data, config);
        
        result.getOutput(false).sort(true, new int[] { 0, 1, 2, 3, 4 });
        
        ARXLattice lattice = result.getLattice();
        
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                if (Double.compare((Double.valueOf(node.getLowestScore().toString())), Double.NaN) == 0 ||
                    Double.compare((Double.valueOf(node.getHighestScore().toString())), Double.NaN) == 0) {
                    fail();
                }
            }
        }
    }
}
