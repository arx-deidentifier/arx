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
import java.util.Arrays;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
import org.junit.Test;

public class TestDataHandle extends TestAnonymizer {

    @Test
    public void testgetters() throws IllegalArgumentException, IOException {

        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
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
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final FLASHResult result = anonymizer.kAnonymize(provider.getData(),
                                                         2,
                                                         0.0d);
        final DataHandle outHandle = result.getHandle();
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
    public void testStableSorting() throws IllegalArgumentException,
                                   IOException {

        provider.createDataDefinition();
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setSuppressionString("*");

        final DataHandle inHandle = provider.getData().getHandle();

        // Alter the definition
        provider.getData()
                .getDefinition()
                .setAttributeType("gender", AttributeType.IDENTIFYING_ATTRIBUTE);

        final FLASHResult result = anonymizer.kAnonymize(provider.getData(),
                                                         2,
                                                         0.0d);
        final DataHandle outHandle = result.getHandle();
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
