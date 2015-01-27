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
import java.util.Arrays;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
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
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class TestDataHandle extends AbstractTest {

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSubset1() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
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
        outHandle.sort(true, 2);

        outHandle.getView().sort(false, 0);

        String[][] given = iteratorToArray(inHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { "70", "female", "81931" }, { "70", "male", "81931" }, { "34", "male", "81667" }, { "34", "female", "81931" } };

        assertTrue(Arrays.deepEquals(given, expected));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSubset2() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();

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
        outHandle.sort(true, 2);

        outHandle.getView().sort(false, 0);

        String[][] given = iteratorToArray(outHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { "70", "*", "81***" }, { "70", "*", "81***" }, { "34", "*", "81***" }, { "34", "*", "81***" } };

        assertTrue(Arrays.deepEquals(given, expected));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSubset3() throws IllegalArgumentException, IOException {

        Data data = Data.create("../arx-data/data-junit/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("../arx-data/data-junit/dis_hierarchy_age.csv", ';'));
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

        data.getHandle().sort(false, 0);

        String[][] given = iteratorToArray(outHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { ">=61", "male", "81825" }, { ">=61", "male", "81925" }, { "20-60", "male", "82667" }, { "20-60", "male", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));
    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSubset4() throws IllegalArgumentException, IOException {

        Data data = Data.create("../arx-data/data-junit/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("../arx-data/data-junit/dis_hierarchy_age.csv", ';'));
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

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSubset5() throws IllegalArgumentException, IOException {

        Data data = Data.create("../arx-data/data-junit/dis.csv", ';');
        data.getDefinition().setAttributeType("age", Hierarchy.create("../arx-data/data-junit/dis_hierarchy_age.csv", ';'));
        data.getDefinition().setAttributeType("gender", Hierarchy.create("../arx-data/data-junit/dis_hierarchy_gender.csv", ';'));
        data.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);

        DataSelector selector = DataSelector.create(data).field("gender").equals("male");
        DataSubset subset = DataSubset.create(data, selector);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new Inclusion(subset));

        // Transform
        ARXResult result = anonymizer.anonymize(data, config);

        // Sort
        data.getHandle().sort(false, 0, 1, 2);

        // Transform
        ARXNode n = result.getLattice().getLevels()[2][1];
        DataHandle h = result.getOutput(n, false);

        String[][] given = iteratorToArray(h.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { ">=61", "*", "81825" }, { ">=61", "*", "81925" }, { "20-60", "*", "82667" }, { "20-60", "*", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));
    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testGetters() throws IllegalArgumentException, IOException {

        final DataHandle inHandle = provider.getData().getHandle();

        // Read the encoded data
        assertTrue(inHandle.getNumRows() == 7);
        assertTrue(inHandle.getNumColumns() == 3);
        assertTrue(inHandle.getAttributeName(0).equals("age"));
        assertTrue(inHandle.getValue(3, 2).equals("81931"));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testSorting() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        final DataHandle inHandle = provider.getData().getHandle();
        inHandle.sort(true, 0);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] resultArray = iteratorToArray(outHandle.iterator());

        final String[][] expected = { { "age", "gender", "zipcode" }, { "<50", "*", "816**" }, { "<50", "*", "819**" }, { "<50", "*", "816**" }, { "<50", "*", "819**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" } };

        final String[][] expectedIn = { { "age", "gender", "zipcode" }, { "34", "male", "81667" }, { "34", "female", "81931" }, { "45", "female", "81675" }, { "45", "male", "81931" }, { "66", "male", "81925" }, { "70", "female", "81931" }, { "70", "male", "81931" } };

        assertTrue(Arrays.deepEquals(inArray, expectedIn));
        assertTrue(Arrays.deepEquals(resultArray, expected));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testStableSorting() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final DataHandle inHandle = provider.getData().getHandle();

        // Alter the definition
        provider.getData().getDefinition().setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);
        final DataHandle outHandle = result.getOutput(false);
        outHandle.sort(true, 2);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] resultArray = iteratorToArray(outHandle.iterator());

        final String[][] expected = { { "age", "gender", "zipcode" }, { "<50", "*", "816**" }, { "<50", "*", "816**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" }, { "<50", "*", "819**" }, { ">=50", "*", "819**" }, { "<50", "*", "819**" } };

        final String[][] expectedIn = { { "age", "gender", "zipcode" }, { "34", "male", "81667" }, { "45", "female", "81675" }, { "66", "male", "81925" }, { "70", "female", "81931" }, { "34", "female", "81931" }, { "70", "male", "81931" }, { "45", "male", "81931" } };

        assertTrue(Arrays.deepEquals(resultArray, expected));
        assertTrue(Arrays.deepEquals(inArray, expectedIn));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testMultipleDataHandlesFork() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final DataHandle inHandle = provider.getData().getHandle();

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        config.setSuppressionAlwaysEnabled(false);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);

        // get top and bottom node
        ARXLattice lattice = result.getLattice();
        ARXNode topNode = lattice.getTop();
        ARXNode bottomNode = lattice.getBottom();

        // get various handle copies
        DataHandle optimal = result.getOutput();
        DataHandle top = result.getOutput(topNode);
        DataHandle bottom = result.getOutput(bottomNode);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] optimalArray = iteratorToArray(optimal.iterator());
        final String[][] topArray = iteratorToArray(top.iterator());
        final String[][] bottomArray = iteratorToArray(bottom.iterator());

        final String[][] topExpected = { { "age", "gender", "zipcode" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" } };
        final String[][] bottomExpected = { { "age", "gender", "zipcode" }, { "34", "male", "81667" }, { "45", "female", "81675" }, { "66", "male", "81925" }, { "70", "female", "81931" }, { "34", "female", "81931" }, { "70", "male", "81931" }, { "45", "male", "81931" } };
        final String[][] optimalExpected = { { "age", "gender", "zipcode" }, { "<50", "*", "816**" }, { "<50", "*", "816**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" }, { "<50", "*", "819**" }, { ">=50", "*", "819**" }, { "<50", "*", "819**" } };

        assertTrue(Arrays.deepEquals(optimalArray, optimalExpected));
        assertTrue(Arrays.deepEquals(topArray, topExpected));
        assertTrue(Arrays.deepEquals(inArray, bottomExpected));
        assertTrue(Arrays.deepEquals(bottomArray, bottomExpected));

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testMultipleDataHandlesNoForkLocked() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);

        // get top and bottom node
        ARXLattice lattice = result.getLattice();
        ARXNode topNode = lattice.getTop();

        // get various handle copies
        @SuppressWarnings("unused")
        DataHandle optimal = result.getOutput(false);

        try {
            @SuppressWarnings("unused")
            DataHandle top = result.getOutput(topNode);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("locked")) {
                return;
            }
        }
        Assert.fail();

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testMultipleDataHandlesNoForkOrphaned() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);

        // get top and bottom node
        ARXLattice lattice = result.getLattice();
        ARXNode topNode = lattice.getTop();
        ARXNode bottomNode = lattice.getBottom();

        // get various handle copies
        @SuppressWarnings("unused")
        DataHandle optimal = result.getOutput();

        DataHandle top = result.getOutput(topNode, false);
        @SuppressWarnings("unused")
        DataHandle bottom = result.getOutput(bottomNode, false);

        try {
            top.getValue(0, 0);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("orphaned")) {
                return;
            }
        }
        Assert.fail();

    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testMultipleDataHandlesForkSync() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final DataHandle inHandle = provider.getData().getHandle();

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        config.setSuppressionAlwaysEnabled(false);

        final ARXResult result = anonymizer.anonymize(provider.getData(), config);

        // get top and bottom node
        ARXLattice lattice = result.getLattice();
        ARXNode topNode = lattice.getTop();
        ARXNode bottomNode = lattice.getBottom();

        // get various handle copies
        DataHandle optimal = result.getOutput();
        DataHandle top = result.getOutput(topNode);
        DataHandle bottom = result.getOutput(bottomNode);

        // sort input data
        optimal.sort(false, 0);

        // sort bottom handle
        bottom.sort(true, 2);

        final String[][] inArray = iteratorToArray(inHandle.iterator());
        final String[][] optimalArray = iteratorToArray(optimal.iterator());
        final String[][] topArray = iteratorToArray(top.iterator());
        final String[][] bottomArray = iteratorToArray(bottom.iterator());

        final String[][] topExpected = { { "age", "gender", "zipcode" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" }, { "*", "*", "*****" } };
        final String[][] bottomExpected = { { "age", "gender", "zipcode" }, { "34", "male", "81667" }, { "45", "female", "81675" }, { "66", "male", "81925" }, { "70", "female", "81931" }, { "70", "male", "81931" }, { "34", "female", "81931" }, { "45", "male", "81931" } };
        final String[][] optimalExpected = { { "age", "gender", "zipcode" }, { "<50", "*", "816**" }, { "<50", "*", "816**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" }, { ">=50", "*", "819**" }, { "<50", "*", "819**" }, { "<50", "*", "819**" } };

        assertTrue(Arrays.deepEquals(optimalArray, optimalExpected));
        assertTrue(Arrays.deepEquals(topArray, topExpected));
        assertTrue(Arrays.deepEquals(bottomArray, bottomExpected));
        assertTrue(Arrays.deepEquals(inArray, bottomExpected));

    }
}
