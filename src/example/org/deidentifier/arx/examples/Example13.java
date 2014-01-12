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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an simple example for using multiple sensitive attributes and
 * enforcing different privacy criteria
 * 
 * @author Prasser, Kohlmayer
 */
public class Example13 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define data
        final Data data = getData();

        // Define attribute types
        data.getDefinition().setAttributeType("age", getHierarchyAge());
        data.getDefinition().setAttributeType("zipcode", getHierarchyZipcode());
        data.getDefinition().setAttributeType("disease1", AttributeType.SENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("disease2", AttributeType.SENSITIVE_ATTRIBUTE);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(3));
        config.addCriterion(new HierarchicalDistanceTCloseness("disease1", 0.6d, getHierarchyDisease()));
        config.addCriterion(new RecursiveCLDiversity("disease2", 3d, 2));
        config.setProtectSensitiveAssociations(false);
        config.setMaxOutliers(0d);
        config.setMetric(Metric.createEntropyMetric());
        try {

            // Now anonymize
            final ARXResult result = anonymizer.anonymize(data, config);
        
            // Print info
            printResult(result, data);

            // Process results
            if (result.getGlobalOptimum() != null){
                System.out.println(" - Transformed data:");
                final Iterator<String[]> transformed = result.getHandle()
                                                             .iterator();
                while (transformed.hasNext()) {
                    System.out.print("   ");
                    System.out.println(Arrays.toString(transformed.next()));
                }
            }
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Hierarchy getHierarchyDisease() {
        final DefaultHierarchy disease = Hierarchy.create();
        disease.add("flu",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory & digestive system disease");
        disease.add("pneumonia",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory & digestive system disease");
        disease.add("bronchitis",
                    "respiratory infection",
                    "vascular lung disease",
                    "respiratory & digestive system disease");
        disease.add("pulmonary edema",
                    "vascular lung disease",
                    "vascular lung disease",
                    "respiratory & digestive system disease");
        disease.add("pulmonary embolism",
                    "vascular lung disease",
                    "vascular lung disease",
                    "respiratory & digestive system disease");
        disease.add("gastric ulcer",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory & digestive system disease");
        disease.add("stomach cancer",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory & digestive system disease");
        disease.add("gastritis",
                    "stomach disease",
                    "digestive system disease",
                    "respiratory & digestive system disease");
        disease.add("colitis",
                    "colon disease",
                    "digestive system disease",
                    "respiratory & digestive system disease");
        disease.add("colon cancer",
                    "colon disease",
                    "digestive system disease",
                    "respiratory & digestive system disease");
        return disease;
    }

    private static Hierarchy getHierarchyZipcode() {
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
        return zipcode;
    }

    private static Hierarchy getHierarchyAge() {
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
        return age;
    }

    private static Data getData() {
        DefaultData data = Data.create();
        data.add("zipcode", "disease1", "age", "disease2");
        data.add("47677", "gastric ulcer", "29", "gastric ulcer");
        data.add("47602", "gastritis", "22", "gastritis");
        data.add("47678", "stomach cancer", "27", "stomach cancer");
        data.add("47905", "gastritis", "43", "gastritis");
        data.add("47909", "flu", "52", "flu");
        data.add("47906", "bronchitis", "47", "bronchitis");
        data.add("47605", "bronchitis", "30", "bronchitis");
        data.add("47673", "pneumonia", "36", "pneumonia");
        data.add("47607", "stomach cancer", "32", "stomach cancer");
        return data;
    }
}
