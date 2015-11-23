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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to compare data mining performance
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example39 extends Example {

    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {

        final Data data = Data.create("data/" + dataset + ".csv", ';');

        // Read generalization hierarchies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        // Create definition
        final File testDir = new File("data/");
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
                final String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }

        return data;
    }

    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws ParseException 
     * @throws IOException 
     */
    public static void main(final String[] args) throws ParseException, IOException {

        String[] features = new String[] {
                "sex", "age", "race", "marital-status", "education", "native-country", "workclass", "occupation", "salary-class"
        };
        
        String clazz = "marital-status";
        
        Data data = createData("adult");
        data.getDefinition().setAttributeType("marital-status", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setDataType("age", DataType.INTEGER);

        double inputAccuracy = data.getHandle().getStatistics()
                                    .getClassificationPerformance(features, clazz, Integer.MAX_VALUE, true, 1d)
                                    .getFractionCorrect();
        
        System.out.println("Input dataset");
        System.out.println(" - Classification accuracy: " + inputAccuracy);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(5));
        config.setMaxOutliers(1d);
        config.setMetric(Metric.createLossMetric());
        
        ARXResult result = anonymizer.anonymize(data, config);

        double outputAccuracy = result.getOutput().getStatistics()
                                        .getClassificationPerformance(features, clazz, Integer.MAX_VALUE, true, 1d)
                                        .getFractionCorrect();
        
        System.out.println("5-anonymous dataset");
        System.out.println(" - Classification accuracy: " + outputAccuracy);
        System.out.println(" - Difference to input: " + (outputAccuracy / inputAccuracy - 1d) * 100d + "[%]");
    }
}
