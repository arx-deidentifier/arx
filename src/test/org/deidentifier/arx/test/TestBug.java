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
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.Enclosure;
import org.deidentifier.arx.criteria.KAnonymity;
import org.junit.Test;

public class TestBug extends AbstractTest {

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
        config.addCriterion(new Enclosure(subset));

        final ARXResult result = anonymizer.anonymize(data, config);
        final DataHandle outHandle = result.getHandle();

        data.getHandle().sort(true, 0);

        String[][] given = iteratorToArray(outHandle.getView().iterator());
        String[][] expected = { { "age", "gender", "zipcode" }, { ">=61", "male", "81825" }, { ">=61", "male", "81925" }, { "20-60", "male", "82667" }, { "20-60", "male", "82451" } };

        assertTrue(Arrays.deepEquals(given, expected));

    }

}
