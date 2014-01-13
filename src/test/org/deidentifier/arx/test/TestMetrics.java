/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.Enclosure;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.Test;

public class TestMetrics extends AbstractTest {

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
        config.addCriterion(new Enclosure(subset));
        config.setMaxOutliers(0.02d);
        config.setMetric(Metric.createNMEntropyMetric());

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(data, config);
        
        result.getHandle().sort(true, new int[]{0,1,2,3,4});
        
        ARXLattice lattice = result.getLattice();
        
        for (ARXNode[] level : lattice.getLevels()){
        	for (ARXNode node : level) {
        		if (String.valueOf(node.getMinimumInformationLoss().getValue()).equals("NaN") ||
        			String.valueOf(node.getMaximumInformationLoss().getValue()).equals("NaN")){
        			fail();
        		}
        	}
        }
    }
}
