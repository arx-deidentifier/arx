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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for data transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestAnonymization extends AbstractTest {

    /* (non-Javadoc)
     * @see org.deidentifier.arx.test.AbstractTest#setUp()
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testAllAttributesIdentifying() throws IOException {
        try {
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("age", AttributeType.IDENTIFYING_ATTRIBUTE);
            data.getDefinition().setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);
            data.getDefinition().setAttributeType("zipcode", AttributeType.IDENTIFYING_ATTRIBUTE);

            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final ARXConfiguration config = ARXConfiguration.create();
            config.setSuppressionString("*");
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(0d);
            anonymizer.anonymize(provider.getData(), config);

        } catch (final IllegalArgumentException e) {
            return;
        }

        Assert.fail();
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testAllAttributesInsensitive() throws IOException {
        try {
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("age", AttributeType.INSENSITIVE_ATTRIBUTE);
            data.getDefinition().setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
            data.getDefinition().setAttributeType("zipcode", AttributeType.INSENSITIVE_ATTRIBUTE);

            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(0d);
            anonymizer.anonymize(provider.getData(), config);

        } catch (final IllegalArgumentException e) {
            return;
        }

        Assert.fail();
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testAllAttributesSensitive() throws IOException {
        try {

            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition().setAttributeType("gender", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition().setAttributeType("zipcode", AttributeType.SENSITIVE_ATTRIBUTE);

            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(-0.2d);
            anonymizer.anonymize(provider.getData(), config);
        } catch (final IllegalArgumentException e) {
            return;
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
    public void testHierarchyWithHeightOne() throws IllegalArgumentException, IOException {
        provider.createDataDefinitionWithHeightOne();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        ARXResult result = anonymizer.anonymize(provider.getData(), config);
        assertFalse(result.isResultAvailable());
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testKAnonymizationWithoutOutliers() throws IOException {

        provider.createDataDefinition();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(provider.getData(), config));

        final String[][] expected = { { "age", "gender", "zipcode" },
                { "<50", "*", "816**" },
                { "<50", "*", "816**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testLDiversityDistinctWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition().setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new DistinctLDiversity("age", 2));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = { { "age", "gender", "zipcode" },
                { "34", "male", "81***" },
                { "45", "female", "81***" },
                { "66", "male", "81***" },
                { "70", "female", "81***" },
                { "34", "female", "81***" },
                { "70", "male", "81***" },
                { "45", "male", "81***" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testLDiversityEntropyWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition().setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new EntropyLDiversity("age", 2));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = { { "age", "gender", "zipcode" },
                { "34", "male", "81***" },
                { "45", "female", "81***" },
                { "66", "male", "81***" },
                { "70", "female", "81***" },
                { "34", "female", "81***" },
                { "70", "male", "81***" },
                { "45", "male", "81***" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testLDiversityWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition().setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new RecursiveCLDiversity("age", 3.0d, 2));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = { { "age", "gender", "zipcode" },
                { "34", "male", "81***" },
                { "45", "female", "81***" },
                { "66", "male", "81***" },
                { "70", "female", "81***" },
                { "34", "female", "81***" },
                { "70", "male", "81***" },
                { "45", "male", "81***" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testMoreThanOneAttributeSensitive() throws IOException {

        try {

            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("gender", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition().setAttributeType("zipcode", AttributeType.SENSITIVE_ATTRIBUTE);

            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(0d);
            anonymizer.anonymize(data, config);

        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testMultipleUsesOfDataDefinition() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));
        data.getHandle().release();

        config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(3));
        config.setMaxOutliers(0d);
        final String[][] result3 = resultToArray(anonymizer.anonymize(data, config));
        data.getHandle().release();

        config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);
        final String[][] result2 = resultToArray(anonymizer.anonymize(data, config));
        data.getHandle().release();

        final String[][] expected = { { "age", "gender", "zipcode" },
                { "<50", "*", "816**" },
                { "<50", "*", "816**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" } };

        final String[][] expected2 = { { "age", "gender", "zipcode" },
                { "*", "male", "81***" },
                { "*", "female", "81***" },
                { "*", "male", "81***" },
                { "*", "female", "81***" },
                { "*", "female", "81***" },
                { "*", "male", "81***" },
                { "*", "male", "81***" } };

        assertTrue(Arrays.deepEquals(result, expected));
        assertTrue(Arrays.deepEquals(result3, expected2));
        assertTrue(Arrays.deepEquals(result2, expected));
        assertTrue(Arrays.deepEquals(result, result2));

    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testSaveData() throws IOException {
        final Data data = provider.data;
        data.getHandle().save(new File("junit_test_data.csv"), ';');
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testSaveHierarchy() throws IOException {
        final Hierarchy hier = provider.age;
        hier.save(new File("junit_test_hierarchy_age.csv"), ';');
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testTClosenessEqualWithoutOutliers() throws IOException {

        // Define data
        final DefaultData data = Data.create();
        data.add("zipcode", "age", "disease");
        data.add("47677", "29", "gastric ulcer");
        data.add("47602", "22", "gastritis");
        data.add("47678", "27", "stomach cancer");
        data.add("47905", "43", "gastritis");
        data.add("47909", "52", "flu");
        data.add("47906", "47", "bronchitis");
        data.add("47605", "30", "bronchitis");
        data.add("47673", "36", "pneumonia");
        data.add("47607", "32", "stomach cancer");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("29", "<=40", "*");
        age.add("22", "<=40", "*");
        age.add("27", "<=40", "*");
        age.add("43", ">40", "*");
        age.add("52", ">40", "*");
        age.add("47", ">40", "*");
        age.add("30", "<=40", "*");
        age.add("36", "<=40", "*");
        age.add("32", "<=40", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("47677", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47602", "4760*", "476**", "47***", "4****", "*****");
        zipcode.add("47678", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47905", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47909", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47906", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47605", "4760*", "476**", "47***", "4****", "*****");
        zipcode.add("47673", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47607", "4760*", "476**", "47***", "4****", "*****");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        data.getDefinition().setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new EqualDistanceTCloseness("disease", 0.6d));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = { { "zipcode", "age", "disease" },
                { "4767*", "<=40", "gastric ulcer" },
                { "4760*", "<=40", "gastritis" },
                { "4767*", "<=40", "stomach cancer" },
                { "4790*", ">40", "gastritis" },
                { "4790*", ">40", "flu" },
                { "4790*", ">40", "bronchitis" },
                { "4760*", "<=40", "bronchitis" },
                { "4767*", "<=40", "pneumonia" },
                { "4760*", "<=40", "stomach cancer" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testTClosenessHierarchicalWithoutOutliers() throws IOException {

        // Define data
        final DefaultData data = Data.create();
        data.add("zipcode", "age", "disease");
        data.add("47677", "29", "gastric ulcer");
        data.add("47602", "22", "gastritis");
        data.add("47678", "27", "stomach cancer");
        data.add("47905", "43", "gastritis");
        data.add("47909", "52", "flu");
        data.add("47906", "47", "bronchitis");
        data.add("47605", "30", "bronchitis");
        data.add("47673", "36", "pneumonia");
        data.add("47607", "32", "stomach cancer");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("29", "<=40", "*");
        age.add("22", "<=40", "*");
        age.add("27", "<=40", "*");
        age.add("43", ">40", "*");
        age.add("52", ">40", "*");
        age.add("47", ">40", "*");
        age.add("30", "<=40", "*");
        age.add("36", "<=40", "*");
        age.add("32", "<=40", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("47677", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47602", "4760*", "476**", "47***", "4****", "*****");
        zipcode.add("47678", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47905", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47909", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47906", "4790*", "479**", "47***", "4****", "*****");
        zipcode.add("47605", "4760*", "476**", "47***", "4****", "*****");
        zipcode.add("47673", "4767*", "476**", "47***", "4****", "*****");
        zipcode.add("47607", "4760*", "476**", "47***", "4****", "*****");

        // Define sensitive value hierarchy
        final DefaultHierarchy disease = Hierarchy.create();
        disease.add("flu", "respiratory infection", "vascular lung disease", "respiratory&digestive system disease");
        disease.add("pneumonia", "respiratory infection", "vascular lung disease", "respiratory&digestive system disease");
        disease.add("bronchitis", "respiratory infection", "vascular lung disease", "respiratory&digestive system disease");
        disease.add("pulmonary edema", "vascular lung disease", "vascular lung disease", "respiratory&digestive system disease");
        disease.add("pulmonary embolism", "vascular lung disease", "vascular lung disease", "respiratory&digestive system disease");
        disease.add("gastric ulcer", "stomach disease", "digestive system disease", "respiratory&digestive system disease");
        disease.add("stomach cancer", "stomach disease", "digestive system disease", "respiratory&digestive system disease");
        disease.add("gastritis", "stomach disease", "digestive system disease", "respiratory&digestive system disease");
        disease.add("colitis", "colon disease", "digestive system disease", "respiratory&digestive system disease");
        disease.add("colon cancer", "colon disease", "digestive system disease", "respiratory&digestive system disease");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        data.getDefinition().setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.setSuppressionString("*");
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new HierarchicalDistanceTCloseness("disease", 0.4d, disease));
        config.setMaxOutliers(0d);
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = { { "zipcode", "age", "disease" },
                { "4767*", "<=40", "gastric ulcer" },
                { "4760*", "<=40", "gastritis" },
                { "4767*", "<=40", "stomach cancer" },
                { "4790*", ">40", "gastritis" },
                { "4790*", ">40", "flu" },
                { "4790*", ">40", "bronchitis" },
                { "4760*", "<=40", "bronchitis" },
                { "4767*", "<=40", "pneumonia" },
                { "4760*", "<=40", "stomach cancer" } };

        assertTrue(Arrays.deepEquals(result, expected));
    }

    /**
     * 
     *
     * @throws IOException
     */
    @Test
    public void testDPresenceWithoutOutliers() throws IOException {
        // Example taken from the d-presence paper

        // Define Public Data P
        final DefaultData data = Data.create();
        data.add("identifier", "name", "zip", "age", "nationality", "sen");
        data.add("a", "Alice", "47906", "35", "USA", "0"); // 0
        data.add("b", "Bob", "47903", "59", "Canada", "1"); // 1
        data.add("c", "Christine", "47906", "42", "USA", "1"); // 2
        data.add("d", "Dirk", "47630", "18", "Brazil", "0"); // 3
        data.add("e", "Eunice", "47630", "22", "Brazil", "0"); // 4
        data.add("f", "Frank", "47633", "63", "Peru", "1"); // 5
        data.add("g", "Gail", "48973", "33", "Spain", "0"); // 6
        data.add("h", "Harry", "48972", "47", "Bulgaria", "1"); // 7
        data.add("i", "Iris", "48970", "52", "France", "1"); // 8

        final HashSet<Integer> indices = new HashSet<Integer>();
        indices.add(1);
        indices.add(2);
        indices.add(5);
        indices.add(7);
        indices.add(8);
        final DataSubset subset = DataSubset.create(data, indices);

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("18", "1*", "<=40", "*");
        age.add("22", "2*", "<=40", "*");
        age.add("33", "3*", "<=40", "*");
        age.add("35", "3*", "<=40", "*");
        age.add("42", "4*", ">40", "*");
        age.add("47", "4*", ">40", "*");
        age.add("52", "5*", ">40", "*");
        age.add("59", "5*", ">40", "*");
        age.add("63", "6*", ">40", "*");

        final DefaultHierarchy nationality = Hierarchy.create();
        nationality.add("Canada", "N. America", "America", "*");
        nationality.add("USA", "N. America", "America", "*");
        nationality.add("Peru", "S. America", "America", "*");
        nationality.add("Brazil", "S. America", "America", "*");
        nationality.add("Bulgaria", "E. Europe", "Europe", "*");
        nationality.add("France", "W. Europe", "Europe", "*");
        nationality.add("Spain", "W. Europe", "Europe", "*");

        final DefaultHierarchy zip = Hierarchy.create();
        zip.add("47630", "4763*", "476*", "47*", "4*", "*");
        zip.add("47633", "4763*", "476*", "47*", "4*", "*");
        zip.add("47903", "4790*", "479*", "47*", "4*", "*");
        zip.add("47906", "4790*", "479*", "47*", "4*", "*");
        zip.add("48970", "4897*", "489*", "48*", "4*", "*");
        zip.add("48972", "4897*", "489*", "48*", "4*", "*");
        zip.add("48973", "4897*", "489*", "48*", "4*", "*");

        // Set data attribute types
        data.getDefinition().setAttributeType("identifier", AttributeType.IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("name", AttributeType.IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("zip", zip);
        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("nationality", nationality);
        data.getDefinition().setAttributeType("sen", AttributeType.INSENSITIVE_ATTRIBUTE);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new DPresence(1d / 2d, 2d / 3d, subset));
        config.setMaxOutliers(0d);
        config.setMetric(org.deidentifier.arx.metric.Metric.createPrecisionMetric());
        final String[][] result = resultToArray(anonymizer.anonymize(data, config));

        // TODO: check if result is correct!
        final String[][] expected = {

                { "identifier", "name", "zip", "age", "nationality", "sen" },
                { "*", "*", "47*", "*", "America", "0" },
                { "*", "*", "47*", "*", "America", "1" },
                { "*", "*", "47*", "*", "America", "1" },
                { "*", "*", "47*", "*", "America", "0" },
                { "*", "*", "47*", "*", "America", "0" },
                { "*", "*", "47*", "*", "America", "1" },
                { "*", "*", "48*", "*", "Europe", "0" },
                { "*", "*", "48*", "*", "Europe", "1" },
                { "*", "*", "48*", "*", "Europe", "1" }

        };

        assertTrue(Arrays.deepEquals(result, expected));
    }
}
