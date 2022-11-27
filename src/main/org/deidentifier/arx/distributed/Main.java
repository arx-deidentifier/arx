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

package org.deidentifier.arx.distributed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.distributed.ARXDistributedAnonymizer.DistributionStrategy;
import org.deidentifier.arx.distributed.ARXDistributedAnonymizer.PartitioningStrategy;
import org.deidentifier.arx.distributed.ARXDistributedAnonymizer.TransformationStrategy;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

/**
 * Example
 *
 * @author Fabian Prasser
 */
public class Main {
    
    private static abstract class BenchmarkConfiguration {
        public abstract Data getDataset() throws IOException;
        public abstract ARXConfiguration getConfig(boolean local, int threads) throws IOException;
        public abstract String getName();
        public abstract String getDataName();
    }

    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Hierarchy createHierarchy(final String dataset,
                                            final String attribute) throws IOException {
        return Hierarchy.create(new File("data/" + dataset + "_hierarchy_" + attribute + ".csv"),
                                Charset.defaultCharset());
    }

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
        //playground();
        createHierarchy("ihis", "EDUC");
        benchmark();
    }
    
    /**
     * Benchmarking
     * @throws IOException
     * @throws RollbackRequiredException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void benchmark() throws IOException, RollbackRequiredException, InterruptedException, ExecutionException {
        
        // Prepare output file
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("result.csv")));
        out.write("Dataset;Config;Local;Sorted;Threads;Granularity;Time\n");
        out.flush();
        
        // Prepare configs
        List<BenchmarkConfiguration> configs = new ArrayList<>();
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "5-anonymity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                return createData("ihis");
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new KAnonymity(5));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "11-anonymity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                return createData("ihis");
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new KAnonymity(5));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "distinct-3-diversity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new DistinctLDiversity("EDUC", 3));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "distinct-5-diversity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new DistinctLDiversity("EDUC", 5));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "entropy-3-diversity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new EntropyLDiversity("EDUC", 3));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "entropy-5-diversity";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new EntropyLDiversity("EDUC", 5));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "0.2-closeness";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new HierarchicalDistanceTCloseness("EDUC", 0.2d, createHierarchy("ihis", "EDUC")));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "0.5-closeness";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new HierarchicalDistanceTCloseness("EDUC", 0.5d, createHierarchy("ihis", "EDUC")));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//        
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "1-disclosure-privacy";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new DDisclosurePrivacy("EDUC", 1));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "2-disclosure-privacy";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new DDisclosurePrivacy("EDUC", 2));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "1-enhanced-likeness";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new EnhancedBLikeness("EDUC", 1));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "2-enhanced-likeness";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                Data data = createData("ihis");
//                data.getDefinition().setAttributeType("EDUC", AttributeType.SENSITIVE_ATTRIBUTE);
//                return data;
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new EnhancedBLikeness("EDUC", 2));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "0.05-average-risk";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                return createData("ihis");
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new AverageReidentificationRisk(0.05d));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
//
//        configs.add(new BenchmarkConfiguration() {
//            public String getName() {return "0.01-average-risk";}
//            public String getDataName() {return "ihis";}
//            public Data getDataset() throws IOException {
//                return createData("ihis");
//            }
//            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
//                ARXConfiguration config = ARXConfiguration.create();
//                config.addPrivacyModel(new AverageReidentificationRisk(0.01d));
//                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
//                config.setSuppressionLimit(1d);
//                return config;
//            }
//        });
        
        configs.add(new BenchmarkConfiguration() {
            public String getName() {return "(e10-6, 2)-differential privacy";}
            public String getDataName() {return "ihis";}
            public Data getDataset() throws IOException {
                return createData("ihis");
            }
            public ARXConfiguration getConfig(boolean local, int threads) throws IOException {
                ARXConfiguration config = ARXConfiguration.create();
                config.addPrivacyModel(new EDDifferentialPrivacy(2d / (double) threads, 0.000001d, null, true));
                config.setDPSearchBudget(0.1d * (2d / (double) threads));
                config.setHeuristicSearchStepLimit(1000);
                config.setQualityModel(Metric.createLossMetric(local ? 0d : 0.5d));
                config.setSuppressionLimit(1d);
                return config;
            }
        });
        
        // Configs
        for (BenchmarkConfiguration benchmark : configs) {
            for (int threads = 1; threads <= 64; threads++) {
                run(benchmark, threads, false, true, out);
                if (!benchmark.getConfig(true, threads).isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
                    run(benchmark, threads, true, true, out);
                }
            }
        }
        
        // Done
        out.close();
    }
    
    /**
     * Run benchmark
     * @param threads 
     * @param config
     * @param local
     * @param sorted
     * @param out
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws RollbackRequiredException 
     * @throws IOException 
     */
    private static void run(BenchmarkConfiguration benchmark, int threads, boolean local, boolean sorted, BufferedWriter out) throws IOException, RollbackRequiredException, InterruptedException, ExecutionException {
        
        System.out.println("Config: " + benchmark.getDataName() + "." + benchmark.getName() + " local: " + local);
        
        double time = 0d;
        double granularity = 0d;
        
        int REPEAT = 5;
        int WARMUP = 2;
        
        // Repeat
        for (int i = 0; i < REPEAT; i++) {
            
            // Report
            System.out.println("- Run " + (i+1) + " of " + REPEAT);
            
            // Get
            Data data = benchmark.getDataset();
            ARXConfiguration config = benchmark.getConfig(local, threads);
            
            // Anonymize
            ARXDistributedAnonymizer anonymizer = new ARXDistributedAnonymizer(threads, 
                                                                               sorted ? PartitioningStrategy.SORTED : PartitioningStrategy.RANDOM, 
                                                                               DistributionStrategy.LOCAL,
                                                                               local ? TransformationStrategy.LOCAL : TransformationStrategy.GLOBAL_AVERAGE);
            ARXDistributedResult result = anonymizer.anonymize(data, config);
            
            // First two are warmup
            if (i >= WARMUP) {
                // TODO: This is currently only an approximation, because it ignores suppressions in step 3
                granularity += getAverage(result.getQuality().get("Granularity"));
                time += result.getTimeAnonymize();
            }
        }
        
        // Average
        time /= (double)(REPEAT-WARMUP);
        granularity /= (double)(REPEAT-WARMUP);
        
        // Store
        out.write(benchmark.getDataName() + ";");
        out.write(benchmark.getName() + ";");
        out.write(local + ";");
        out.write(sorted + ";");
        out.write(threads + ";");
        out.write(granularity + ";");
        out.write(time + "\n");
        out.flush();
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
    
    private static void playground() throws IOException, RollbackRequiredException, InterruptedException, ExecutionException {

        Data data = createData("adult");
        data.getDefinition().setAttributeType("marital-status", AttributeType.SENSITIVE_ATTRIBUTE);
        
        // K-Anonymity
        for (int threads = 1; threads < 5; threads ++) {
            for (int k : new int[] { 2 }) {
                ARXConfiguration config = ARXConfiguration.create();
                config.addPrivacyModel(new EqualDistanceTCloseness("marital-status", 0.2d));
                config.setQualityModel(Metric.createLossMetric(0d));
                config.setSuppressionLimit(1d);
    
                // Anonymize
                ARXDistributedAnonymizer anonymizer = new ARXDistributedAnonymizer(threads,
                                                                                   PartitioningStrategy.SORTED,
                                                                                   DistributionStrategy.LOCAL,
                                                                                   TransformationStrategy.LOCAL);
                ARXDistributedResult result = anonymizer.anonymize(data, config);
                
                System.out.println("--------------------------");
                ARXPartition.print(result.getOutput());
                System.out.println("Records: " + result.getOutput().getNumRows());
                System.out.println("Number of threads: " + threads);
                for (Entry<String, List<Double>> entry : result.getQuality().entrySet()) {
                    System.out.println(entry.getKey() + ": " + getAverage(entry.getValue()));
                }
    
                // Timing
                System.out.println("Preparation time: " + result.getTimePrepare());
                System.out.println("Anonymization time: " + result.getTimeAnonymize());
                System.out.println("Postprocessing time: " + result.getTimePostprocess());
            }
        }
    }
}
