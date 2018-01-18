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
import java.util.HashSet;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to use the k-map model
 *
 * @author Fabian Prasser
 */
public class Example41 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        // Define public dataset
        DefaultData data = Data.create();
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
        DataSubset subset = DataSubset.create(data, new HashSet<Integer>(Arrays.asList(1, 2, 5, 7, 8)));

        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("18", "1*", "<=40", "*");
        age.add("22", "2*", "<=40", "*");
        age.add("33", "3*", "<=40", "*");
        age.add("35", "3*", "<=40", "*");
        age.add("42", "4*", ">40", "*");
        age.add("47", "4*", ">40", "*");
        age.add("52", "5*", ">40", "*");
        age.add("59", "5*", ">40", "*");
        age.add("63", "6*", ">40", "*");

        DefaultHierarchy nationality = Hierarchy.create();
        nationality.add("Canada", "N. America", "America", "*");
        nationality.add("USA", "N. America", "America", "*");
        nationality.add("Peru", "S. America", "America", "*");
        nationality.add("Brazil", "S. America", "America", "*");
        nationality.add("Bulgaria", "E. Europe", "Europe", "*");
        nationality.add("France", "W. Europe", "Europe", "*");
        nationality.add("Spain", "W. Europe", "Europe", "*");

        DefaultHierarchy zip = Hierarchy.create();
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
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KMap(3, subset));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createLossMetric());

        // Now anonymize
        ARXResult result = anonymizer.anonymize(data, config);

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
        print(result.getOutput(false).iterator());

        // Print results
        System.out.println(" - Transformed research subset:");
        print(result.getOutput(false).getView().iterator());
    }
}
