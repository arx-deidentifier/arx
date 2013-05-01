/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.Data.DefaultData;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for data transformations
 * 
 * @author Prasser, Kohlmayer
 */
public class TestDataTransformations extends TestAnonymizer {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testAllAttributesIdentifying() throws IOException {
        try {
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition()
                .setAttributeType("age", AttributeType.IDENTIFYING_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("zipcode",
                                  AttributeType.IDENTIFYING_ATTRIBUTE);

            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.setSuppressionString("-");
            anonymizer.kAnonymize(data, 2, 0.0d);

        } catch (final IllegalArgumentException e) {
            return;
        }

        Assert.fail();
    }

    @Test
    public void testAllAttributesInsensitive() throws IOException {
        try {
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition()
                .setAttributeType("age", AttributeType.INSENSITIVE_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("gender", AttributeType.INSENSITIVE_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("zipcode",
                                  AttributeType.INSENSITIVE_ATTRIBUTE);

            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.kAnonymize(data, 2, 0.0d);

        } catch (final IllegalArgumentException e) {
            return;
        }

        Assert.fail();
    }

    @Test
    public void testAllAttributesSensitive() throws IOException {
        try {

            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition()
                .setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("gender", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("zipcode", AttributeType.SENSITIVE_ATTRIBUTE);

            anonymizer.kAnonymize(data, 2, -0.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testHierarchyWithHeightOne() throws IllegalArgumentException,
                                            IOException {
        provider.createDataDefinitionWithHeightOne();
        final Data data = provider.getData();

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");
        final FLASHResult result = anonymizer.kAnonymize(data, 2, 0d);
        assertFalse(result.isResultAvailable());
    }

    @Test
    public void testKAnonymizationWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.kAnonymize(data,
                                                                      2,
                                                                      0.0d));

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

    @Test
    public void testLDiversityDistinctWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition()
            .setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.lDiversify(data,
                                                                      2,
                                                                      false,
                                                                      0.0d));

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

    @Test
    public void testLDiversityEntropyWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition()
            .setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.lDiversify(data,
                                                                      2,
                                                                      true,
                                                                      0.0d));

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

    @Test
    public void testLDiversityWithoutOutliers() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();
        data.getDefinition()
            .setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.lDiversify(data,
                                                                      3.0d,
                                                                      2,
                                                                      0.0d));

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

    @Test
    public void testMoreThanOneAttributeSensitive() throws IOException {

        try {

            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            provider.createDataDefinition();
            final Data data = provider.getData();
            data.getDefinition()
                .setAttributeType("gender", AttributeType.SENSITIVE_ATTRIBUTE);
            data.getDefinition()
                .setAttributeType("zipcode", AttributeType.SENSITIVE_ATTRIBUTE);

            anonymizer.kAnonymize(data, 2, -0.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMultipleUsesOfDataDefinition() throws IOException {

        provider.createDataDefinition();
        final Data data = provider.getData();

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.kAnonymize(data,
                                                                      2,
                                                                      0.0d));
        final String[][] result3 = resultToArray(anonymizer.kAnonymize(data,
                                                                       3,
                                                                       0.0d));
        final String[][] result2 = resultToArray(anonymizer.kAnonymize(data,
                                                                       2,
                                                                       0.0d));

        final String[][] expected = { { "age", "gender", "zipcode" },
                { "<50", "*", "816**" },
                { "<50", "*", "816**" },
                { ">=50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" },
                { ">=50", "*", "819**" },
                { "<50", "*", "819**" } };

        final String[][] expected2 = { { "age", "gender", "zipcode" },
                { "*", "male", "*****" },
                { "*", "female", "*****" },
                { "*", "male", "*****" },
                { "*", "female", "*****" },
                { "*", "female", "*****" },
                { "*", "male", "*****" },
                { "*", "male", "*****" } };

        assertTrue(Arrays.deepEquals(result, expected));
        assertTrue(Arrays.deepEquals(result3, expected2));
        assertTrue(Arrays.deepEquals(result2, expected));
        assertTrue(Arrays.deepEquals(result, result2));

    }

    @Test
    public void testSaveData() throws IOException {
        final Data data = provider.data;
        data.getHandle().save(new File("junit_test_data.csv"), ';');
    }

    @Test
    public void testSaveHierarchy() throws IOException {
        final Hierarchy hier = provider.age;
        hier.save(new File("junit_test_hierarchy_age.csv"), ';');
    }

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
        data.getDefinition()
            .setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.tClosify(data,
                                                                    2,
                                                                    0.6d,
                                                                    0.0d));

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
        disease.add("flu",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory&digestive system disease");
        disease.add("pneumonia",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory&digestive system disease");
        disease.add("bronchitis",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory&digestive system disease");
        disease.add("pulmonary edema",
                    "vascular lung disease",
                    "vascular lung disease",
                    "respiratory&digestive system disease");
        disease.add("pulmonary embolism",
                    "vascular lung disease",
                    "vascular lung disease",
                    "respiratory&digestive system disease");
        disease.add("gastric ulcer",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory&digestive system disease");
        disease.add("stomach cancer",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory&digestive system disease");
        disease.add("gastritis",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory&digestive system disease");
        disease.add("colitis",
                    "colon disease",
                    "digestive system disease",
                    "respiratory&digestive system disease");
        disease.add("colon cancer",
                    "colon disease",
                    "digestive system disease",
                    "respiratory&digestive system disease");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("zipcode", zipcode);
        data.getDefinition()
            .setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final String[][] result = resultToArray(anonymizer.tClosify(data,
                                                                    2,
                                                                    0.4d,
                                                                    0.0d,
                                                                    disease));

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
}
