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

package org.deidentifier.flash.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.Data.DefaultData;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.metric.Metric;

/**
 * This class implements an example on how to use the API by directly providing
 * the input datasets
 * 
 * @author Prasser, Kohlmayer
 */
public class Example9 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define Public Data P
        final DefaultData data = Data.create();
        data.add("identifier", "name", "zip", "age", "nationality", "sen");
        data.add("a", "Alice", "47906", "35", "USA", "0"); // 0
        data.add("b", "Bob", "47903", "59", "Canada", "1"); // 1
        data.add("c", "Christine", "47906", "42", "USA", "1"); // 2
        data.add("d", "Dirk", "47630", "18", "Brazil", "0"); // 3
        data.add("e", "Eunice", "47630", "22", "Brazil", "0"); // 4
        data.add("f", "Frank", "47633", "63", "Peru", "0"); // 5
        data.add("g", "Gail", "48973", "33", "Spain", "0"); // 6
        data.add("h", "Harry", "48972", "47", "Bulgaria", "1"); // 7
        data.add("i", "Iris", "48970", "52", "France", "1"); // 8

        final HashSet<Integer> researchSubset = new HashSet<Integer>();
        researchSubset.add(1);
        researchSubset.add(2);
        researchSubset.add(5);
        researchSubset.add(7);
        researchSubset.add(8);

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
        // TODO: adapt metric -> entropy and dmStar
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer(Metric.createPrecisionMetric());
        try {

            final int k = 2;
            final double supressionRate = 0.0d;
            final double dMin = 0.5d; // 1/2
            final double dMax = 0.66666666666666666d; // 2/3

            // final FLASHResult result = anonymizer.kAnonymize(data, k, supressionRate);
            final FLASHResult result = anonymizer.dpresencify(data, k, dMin, dMax, supressionRate, researchSubset);

            // Print info
            printResult(result, data);

            // Process results
            System.out.println(" - Transformed data:");
            final Iterator<String[]> transformed = result.getHandle().iterator();
            while (transformed.hasNext()) {
                System.out.print("   ");
                System.out.println(Arrays.toString(transformed.next()));
            }
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
