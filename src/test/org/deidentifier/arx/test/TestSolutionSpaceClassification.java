/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
 * 
 */
public class TestSolutionSpaceClassification extends AbstractTest {

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testNMEntropy() throws IllegalArgumentException, IOException {

    	Data data = Data.create("data/adult.csv", ';');
    	data.getDefinition().setAttributeType("sex", Hierarchy.create("data/adult_hierarchy_sex.csv", ';'));
    	data.getDefinition().setAttributeType("age", Hierarchy.create("data/adult_hierarchy_age.csv", ';'));
    	data.getDefinition().setAttributeType("race", Hierarchy.create("data/adult_hierarchy_race.csv", ';'));
    	data.getDefinition().setAttributeType("education", Hierarchy.create("data/adult_hierarchy_education.csv", ';'));
    	data.getDefinition().setAttributeType("marital-status", Hierarchy.create("data/adult_hierarchy_marital-status.csv", ';'));

        DataSelector selector = DataSelector.create(data).field("sex").equals("Male");
        DataSubset subset = DataSubset.create(data, selector);
        
        ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(5));
        config.addCriterion(new Inclusion(subset));
        config.setMaxOutliers(0.02d);
        config.setMetric(Metric.createEntropyMetric(false));

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(data, config);
        
        result.getOutput(false).sort(true, new int[]{0,1,2,3,4});
        
        ARXLattice lattice = result.getLattice();
        
        for (ARXNode[] level : lattice.getLevels()){
        	for (ARXNode node : level) {
        		if (Double.compare((Double.valueOf(node.getMinimumInformationLoss().toString())), Double.NaN)==0 ||
        			Double.compare((Double.valueOf(node.getMaximumInformationLoss().toString())), Double.NaN)==0){
        			fail();
        		}
        	}
        }
    }
}
