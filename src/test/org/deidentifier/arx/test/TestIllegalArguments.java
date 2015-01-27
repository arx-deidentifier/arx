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
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KAnonymity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A test case for illegal arguments.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TestIllegalArguments extends AbstractTest {

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
    public void testEmptyDatasetWithAttributeDefinition() throws IOException {
        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = Data.create();

            data.getDefinition()
                .setAttributeType("age", AttributeType.IDENTIFYING_ATTRIBUTE);
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
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
    public void testEmptyDatasetWithoutAttributeDefinition() throws IOException {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = Data.create();

            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
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
    public void testEmptyDefinition() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
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
    public void testEmptyHierarchy() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final Data data = provider.getData();
        data.getDefinition().setAttributeType("age", Hierarchy.create());
        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
            anonymizer.anonymize(provider.getData(), config);
            
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     */
    @Test
    public void testHistorySize() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setHistorySize(-1);
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
    public void testInvalidHierarchies() throws IOException {
        provider.createWrongDataDefinition();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
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
    public void testKRangeNegative() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(-1));
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
    public void testKRangeTooLarge() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(8));
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
    public void testKRangeZero() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(0));
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
    public void testMaxOutliersEqualsOne() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1d);
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
    public void testMaxOutliersNegative() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
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
     * @throws IOException
     */
    @Test
    public void testMaxOutliersTooLarge() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
            anonymizer.anonymize(provider.getData(), config);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     */
    @Test
    public void testMetric() {

        try {
            final ARXConfiguration config = ARXConfiguration.create();
            config.setMetric(null);
        } catch (final NullPointerException e) {
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
    public void testMissingHierarchyValue() throws IOException {
        provider.createDataDefinitionMissing();
        final Data data = provider.getData();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
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
    public void testNullHierarchy() throws IOException {
        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("age", (AttributeType)null);
            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(1.2d);
            anonymizer.anonymize(data, config);
        } catch (final NullPointerException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     */
    @Test
    public void testSnapshotSizeNegative() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(-1);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     */
    @Test
    public void testSnapshotSizeTooLarge() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(1.01d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    /**
     * 
     */
    @Test
    public void testSnapshotSizeZero() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(0);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }
}
