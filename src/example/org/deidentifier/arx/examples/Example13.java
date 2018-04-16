/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an simple example for using multiple sensitive attributes and
 * different privacy models.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example13 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) throws IOException {

        // Define data
        Data data = getData();

        // Define attribute types
        data.getDefinition().setAttributeType("age", getHierarchyAge());
        data.getDefinition().setAttributeType("zipcode", getHierarchyZipcode());
        data.getDefinition().setAttributeType("disease1", AttributeType.SENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("disease2", AttributeType.SENSITIVE_ATTRIBUTE);

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(3));
        config.addPrivacyModel(new HierarchicalDistanceTCloseness("disease1", 0.6d, getHierarchyDisease()));
        config.addPrivacyModel(new RecursiveCLDiversity("disease2", 3d, 2));
        config.setSuppressionLimit(0d);
        config.setQualityModel(Metric.createEntropyMetric());

        // Now anonymize
        ARXResult result = anonymizer.anonymize(data, config);

        // Print info
        printResult(result, data);

        // Process results
        if (result.getGlobalOptimum() != null) {
            System.out.println(" - Transformed data:");
            Iterator<String[]> transformed = result.getOutput(false).iterator();
            while (transformed.hasNext()) {
                System.out.print("   ");
                System.out.println(Arrays.toString(transformed.next()));
            }
        }
    }

    /**
     * 
     *
     * @return
     */
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

    /**
     * 
     *
     * @return
     */
    private static Hierarchy getHierarchyAge() {
        DefaultHierarchy age = Hierarchy.create();
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

    /**
     * 
     *
     * @return
     */
    private static Hierarchy getHierarchyDisease() {
        DefaultHierarchy disease = Hierarchy.create();
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

    /**
     * 
     *
     * @return
     */
    private static Hierarchy getHierarchyZipcode() {
        DefaultHierarchy zipcode = Hierarchy.create();
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
}
