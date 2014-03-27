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
import java.util.Arrays;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.junit.Test;

public class TestDataHandle extends AbstractTest {

    @Test
    public void testSubset1() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        final DataHandle inHandle = provider.getData().getHandle();

        // Alter the definition
        provider.getData().getDefinition().setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);

        DataSelector selector = DataSelector.create(provider.getData()).field("age").equals("70").or().equals("34");

        DataSubset subset = DataSubset.create(provider.getData(), selector);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new DPresence(0, 1, subset));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        outHandle.sort(false, 2);

        outHandle.getView().sort(true, 0);

        String[][] given = iteratorToArray(inHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { "70", "female", "81931" }, { "70", "male", "81931" }, { "34", "male", "81667" }, { "34", "female", "81931" } };

        assertTrue(Arrays.deepEquals(given, expected));

    }

    @Test
    public void testSubset2() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        // Alter the definition
        provider.getData().getDefinition().setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);

        DataSelector selector = DataSelector.create(provider.getData()).field("age").equals("70").or().equals("34");

        DataSubset subset = DataSubset.create(provider.getData(), selector);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new DPresence(0, 1, subset));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        outHandle.sort(false, 2);

        outHandle.getView().sort(true, 0);

        String[][] given = iteratorToArray(outHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { "70", "*", "81***" }, { "70", "*", "81***" }, { "34", "*", "81***" }, { "34", "*", "81***" } };

        assertTrue(Arrays.deepEquals(given, expected));

    }

    @Test
    public void testSubset3() throws IllegalArgumentException, IOException {

        Data data = Data.create("data/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("data/dis_hierarchy_age.csv", ';'));
        data.getDefinition().setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);

        DataSelector selector = DataSelector.create(data).field("gender").equals("male");
        DataSubset subset = DataSubset.create(data, selector);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new Inclusion(subset));

        final ARXResult result = anonymizer.anonymize(data, config);
        final DataHandle outHandle = result.getOutput(false);

        data.getHandle().sort(true, 0);

        String[][] given = iteratorToArray(outHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { ">=61", "male", "81825" }, { ">=61", "male", "81925" }, { "20-60", "male", "82667" }, { "20-60", "male", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));
    }

    @Test
    public void testSubset4() throws IllegalArgumentException, IOException {

        Data data = Data.create("data/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("data/dis_hierarchy_age.csv", ';'));
        data.getDefinition().setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);

        DataSelector selector = DataSelector.create(data).field("gender").equals("male");
        DataSubset subset = DataSubset.create(data, selector);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new Inclusion(subset));

        anonymizer.anonymize(data, config);

        String[][] given = iteratorToArray(data.getHandle().getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { "34", "male", "82667" }, { "66", "male", "81925" }, { "70", "male", "81825" }, { "21", "male", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));
    }

    @Test
    public void testSubset5() throws IllegalArgumentException, IOException {

        Data data = Data.create("data/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("data/dis_hierarchy_age.csv", ';'));
        data.getDefinition().setAttributeType("gender", Hierarchy.create("data/dis_hierarchy_gender.csv", ';'));
        data.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);

        DataSelector selector = DataSelector.create(data).field("gender").equals("male");
        DataSubset subset = DataSubset.create(data, selector);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new Inclusion(subset));

        // Transform
        ARXResult result = anonymizer.anonymize(data, config);
        
        // Sort
        data.getHandle().sort(true, 0, 1, 2);
        
        // Transform
        ARXNode n = result.getLattice().getLevels()[2][1];
        DataHandle h = result.getOutput(n, false);
        
        String[][] given = iteratorToArray(h.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, 
                                { ">=61", "*", "81825" }, 
                                { ">=61", "*", "81925" }, 
                                { "20-60", "*", "82667" }, 
                                { "20-60", "*", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));
    }

    @Test
    public void testGetters() throws IllegalArgumentException, IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        final DataHandle inHandle = provider.getData().getHandle();

        // Read the encoded data
        assertTrue(inHandle.getNumRows() == 7);
        assertTrue(inHandle.getNumColumns() == 3);
        assertTrue(inHandle.getAttributeName(0).equals("age"));
        assertTrue(inHandle.getValue(3, 2).equals("81931"));

    }

    @Test
    public void testSorting() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        final DataHandle inHandle = provider.getData().getHandle();
        inHandle.sort(false, 0);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] resultArray = iteratorToArray(outHandle.iterator());

        final String[][] expected = { { "age", "gender", "zipcode" },
                { "<50", "*", "816**" },
                { "<50", "*", "819**" },
                { "<50", "*", "816**" },
                { "<50", "*", "819**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" } };

        final String[][] expectedIn = { { "age", "gender", "zipcode" },
                { "34", "male", "81667" },
                { "34", "female", "81931" },
                { "45", "female", "81675" },
                { "45", "male", "81931" },
                { "66", "male", "81925" },
                { "70", "female", "81931" },
                { "70", "male", "81931" } };

        assertTrue(Arrays.deepEquals(inArray, expectedIn));
        assertTrue(Arrays.deepEquals(resultArray, expected));

    }

    @Test
    public void testStableSorting() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        final DataHandle inHandle = provider.getData().getHandle();

        // Alter the definition
        provider.getData().getDefinition().setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        outHandle.sort(false, 2);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] resultArray = iteratorToArray(outHandle.iterator());

        final String[][] expected = { { "age", "gender", "zipcode" },
                { "<50", "*", "816**" },
                { "<50", "*", "816**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" } };

        final String[][] expectedIn = { { "age", "gender", "zipcode" },
                { "34", "male", "81667" },
                { "45", "female", "81675" },
                { "66", "male", "81925" },
                { "70", "female", "81931" },
                { "34", "female", "81931" },
                { "70", "male", "81931" },
                { "45", "male", "81931" } };

        assertTrue(Arrays.deepEquals(resultArray, expected));
        assertTrue(Arrays.deepEquals(inArray, expectedIn));

    }
}
