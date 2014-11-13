/*
 * ARX: Powerful Data Anonymization
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
