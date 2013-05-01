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

import java.io.IOException;

import junit.framework.Assert;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHAnonymizer;
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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            final Data data = Data.create();

            anonymizer.kAnonymize(data, 2, 1.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEmptyDefinition() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), -1, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testKRangeTooLarge() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 8, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testKRangeZero() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 0, 0.0d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersEqualsOne() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 2, 1d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersNegative() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

        try {
            anonymizer.kAnonymize(provider.getData(), 2, -0.2d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testMaxOutliersTooLarge() throws IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();

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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(-1);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSnapshotSizeTooLarge() {

        try {
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(1.01d);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSnapshotSizeZero() {

        try {
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.setMaximumSnapshotSizeDataset(0);
        } catch (final IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSuppressionString() {

        try {
            final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
            anonymizer.setSuppressionString(null);
        } catch (final NullPointerException e) {
            return;
        }
        Assert.fail();
    }
}
