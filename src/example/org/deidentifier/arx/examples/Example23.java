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
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to apply multiple l-diversity criteria without protecting sensitive assocations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example23 extends Example {

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
        data.add("47677", "22", "gastric ulcer");
        data.add("47602", "23", "gastritis");
        data.add("47678", "24", "stomach cancer");
        data.add("47605", "25", "bronchitis");
        data.add("47607", "26", "pneumonia");
        data.add("47673", "26", "pneumonia");
        data.add("47905", "23", "gastritis");
        data.add("47909", "27", "flu");
        data.add("47906", "25", "bronchitis");

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

        data.getDefinition().setAttributeType("zipcode", zipcode);
        data.getDefinition().setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("age", AttributeType.SENSITIVE_ATTRIBUTE);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(3));
        config.addCriterion(new DistinctLDiversity("disease", 2));
        config.addCriterion(new DistinctLDiversity("age", 2));
        config.setMaxOutliers(0.1d);
        config.setProtectSensitiveAssociations(false);
        config.setMetric(Metric.createEntropyMetric());
        try {

            // Now anonymize
            final ARXResult result = anonymizer.anonymize(data, config);
        
            // Print info
            printResult(result, data);

            // Process results
            if (result.isResultAvailable()) {
                System.out.println(" - Transformed data:");
                final Iterator<String[]> transformed = result.getOutput(false).iterator();
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
}
