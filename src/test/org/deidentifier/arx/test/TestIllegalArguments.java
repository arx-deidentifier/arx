/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.test;

import java.io.IOException;

import junit.framework.Assert;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.junit.Before;
import org.junit.Test;

/**
 * A test case for illegal arguments
 * 
 * @author Prasser, Kohlmayer
 */
public class TestIllegalArguments extends TestAnonymizer {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testEmptyDatasetWithAttributeDefinition() throws IOException {
        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = Data.create();

            data.getDefinition()
                .setAttributeType("age", AttributeType.IDENTIFYING_ATTRIBUTE);
            anonymizer.kAnonymize(data, 2, 1.2d);

        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();

    }

    @Test
    public void testEmptyDatasetWithoutAttributeDefinition() throws IOException {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = Data.create();

            anonymizer.kAnonymize(data, 2, 1.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEmptyDefinition() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final Data data = provider.getData();
        try {
            anonymizer.kAnonymize(data, 2, 1.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEmptyHierarchy() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final Data data = provider.getData();
        data.getDefinition().setAttributeType("age", Hierarchy.create());
        try {
            anonymizer.kAnonymize(data, 2, 1.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

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

    @Test
    public void testInvalidHierarchies() throws IOException {
        provider.createWrongDataDefinition();
        final Data data = provider.getData();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        try {
            anonymizer.kAnonymize(data, 2, 0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testKRangeNegative() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), -1, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testKRangeTooLarge() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 8, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testKRangeZero() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 0, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersEqualsOne() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 2, 1d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersNegative() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 2, -0.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersTooLarge() throws IOException {

        final ARXAnonymizer anonymizer = new ARXAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 2, 1.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMetric() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setMetric(null);
        } catch (final NullPointerException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMissingHierarchyValue() throws IOException {
        provider.createDataDefinitionMissing();
        final Data data = provider.getData();

        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setSuppressionString("*");

        try {
            anonymizer.kAnonymize(data, 2, 0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testNullHierarchy() throws IOException {
        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            final Data data = provider.getData();
            data.getDefinition().setAttributeType("age", null);
            anonymizer.kAnonymize(data, 2, 1.2d);
        } catch (final NullPointerException e) {
            return;
        }
        Assert.fail();
    }

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

    @Test
    public void testSuppressionString() {

        try {
            final ARXAnonymizer anonymizer = new ARXAnonymizer();
            anonymizer.setSuppressionString(null);
        } catch (final NullPointerException e) {
            return;
        }
        Assert.fail();
    }
}
