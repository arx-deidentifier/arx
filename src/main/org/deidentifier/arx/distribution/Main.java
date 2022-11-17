/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

package org.deidentifier.arx.distribution;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.distribution.ARXDistributedAnonymizer.DistributionStrategy;
import org.deidentifier.arx.distribution.ARXDistributedAnonymizer.PartitioningStrategy;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

/**
 * Example
 *
 * @author Fabian Prasser
 */
public class Main {

    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {

        Data data = Data.create("data/" + dataset + ".csv", StandardCharsets.UTF_8, ';');

        // Read generalization hierarchies
        FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        // Create definition
        File testDir = new File("data/");
        File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        for (File file : genHierFiles) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }

        return data;
    }

    /**
     * Entry point.
     *
     * @param args the arguments
     * @throws IOException
     * @throws RollbackRequiredException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, RollbackRequiredException, InterruptedException, ExecutionException {
        
        Data data = createData("ihis");
        
        for (int threads = 1; threads <= 4; threads++) {
            for (int i=0; i < 3; i++) {
                ARXConfiguration config = ARXConfiguration.create();
                config.addPrivacyModel(new KAnonymity(5));
                config.setQualityModel(Metric.createLossMetric(0d));
                
                // Anonymize
                ARXDistributedAnonymizer anonymizer = new ARXDistributedAnonymizer(threads, PartitioningStrategy.SORTED, DistributionStrategy.LOCAL, false);
                ARXDistributedResult result = anonymizer.anonymize(data, config);
                
                // Print
                System.out.println("--------------------------");
                System.out.println("Number of threads: " + threads);
                for (Entry<String, List<Double>> entry : result.getQuality().entrySet()) {
                    System.out.println(entry.getKey()+": " + getAverage(entry.getValue()));
                }
                
                // Timing
                System.out.println("Preparation time: " + result.getTimePrepare());
                System.out.println("Anonymization time: " + result.getTimeAnonymize());
                System.out.println("Postprocessing time: " + result.getTimePostprocess());
            }
        }
    }
    
    /**
     * Returns the average of the given values
     * @param values
     * @return
     */
    private static double getAverage(List<Double> values) {
        double result = 0d;
        for (Double value : values) {
            result += value;
        }
        return result / (double)values.size();
    }
}
