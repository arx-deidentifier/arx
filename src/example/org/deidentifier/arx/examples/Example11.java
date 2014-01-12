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

package org.deidentifier.arx.examples;

import java.io.IOException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to apply the d-presence criterion
 * and create a research subset by providing a data selector
 * 
 * @author Prasser, Kohlmayer
 */
public class Example11 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define public dataset
        final DefaultData data = Data.create();
        data.add("identifier", "name", "zip", "age", "nationality", "sen");
        data.add("a", "Alice", "47906", "35", "USA", "0");
        data.add("b", "Bob", "47903", "59", "Canada", "1");
        data.add("c", "Christine", "47906", "42", "USA", "1");
        data.add("d", "Dirk", "47630", "18", "Brazil", "0");
        data.add("e", "Eunice", "47630", "22", "Brazil", "0");
        data.add("f", "Frank", "47633", "63", "Peru", "1");
        data.add("g", "Gail", "48973", "33", "Spain", "0");
        data.add("h", "Harry", "48972", "47", "Bulgaria", "1");
        data.add("i", "Iris", "48970", "52", "France", "1");

        // Define research subset
        DataSelector selector = DataSelector.create(data).field("sen").equals("1");
        final DataSubset subset = DataSubset.create(data, selector);

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
        config.setMetric(Metric.createEntropyMetric());
        try {

            // Now anonymize
            final ARXResult result = anonymizer.anonymize(data, config);

            // Print input
            System.out.println(" - Input data:");
            print(data.getHandle().iterator());

            // Print input
            System.out.println(" - Input research subset:");
            print(data.getHandle().getView().iterator());

            // Print info
            printResult(result, data);
                     
            // Print results
            System.out.println(" - Transformed data:");
            print(result.getHandle().iterator());

            // Print results
            System.out.println(" - Transformed research subset:");
            print(result.getHandle().getView().iterator());
            
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
