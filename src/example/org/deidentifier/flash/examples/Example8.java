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
public class Example8 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

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

        // Create an instance of the anonymizer
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer(Metric.createEntropyMetric());
        try {
            final FLASHResult result = anonymizer.tClosify(data,
                                                           3,
                                                           0.6d,
                                                           0.0d,
                                                           disease);

            // Print info
            printResult(result, data);

            // Process results
            System.out.println(" - Transformed data:");
            final Iterator<String[]> transformed = result.getHandle()
                                                         .iterator();
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
