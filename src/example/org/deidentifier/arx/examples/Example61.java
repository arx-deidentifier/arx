/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to compare data mining performance
 * using a training and a test set
 * @author Fabian Prasser
 * @author Ibhraheem Al-Dhamari
 */
public class Example61 extends Example {
    
    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {
        
        // Load data
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
     * Gets a set of random record indices for this dataset
     * @param data
     * @param sampleFraction
     * @return
     */
    public static Set<Integer> getRandomSample(Data data, double sampleFraction) {

        // Create list
        int rows = data.getHandle().getNumRows();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < rows; ++i) {
            list.add(i);
        }

        // Shuffle
        Collections.shuffle(list, new Random(0xDEADBEEF));

        // Select sample and create set
        return new HashSet<Integer>(list.subList(0, (int) Math.round((double) rows * sampleFraction)));
     }    

    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws ParseException
     * @throws IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        
        Data data = createData("adult");
        data.getDefinition().setAttributeType("marital-status", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setResponseVariable("marital-status", true);
        
        // Size of training set
        double trainingSetSize = 0.8d; 
        
        // Create sample
        Set<Integer> trainingSetIndices = getRandomSample(data,  trainingSetSize);
        DataSubset trainingSet = DataSubset.create(data, trainingSetIndices);
   
        // Configure anonymization
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(5));
        config.addPrivacyModel(new Inclusion(trainingSet));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createClassificationMetric());
        
        // Start anonymization process
        ARXResult result = anonymizer.anonymize(data, config);
        DataHandle output = result.getOutput();
        
        // Run evaluation using k-fold cross validation 
        System.out.println("Evaluation using k-fold cross validation");
        evaluate(output, false);
        
        // Run evaluation using test/training set
        System.out.println("Evaluation using test and training set");
        evaluate(output, true);
    }

    /**
     * Run evaluations
     * @param data
     * @param useTestTrainingSet
     * @throws ParseException 
     */
    private static void evaluate(DataHandle data, boolean useTestTrainingSet) throws ParseException {

        String[] features = new String[] {
                                           "sex",
                                           "age",
                                           "race",
                                           "education",
                                           "native-country",
                                           "workclass",
                                           "occupation",
                                           "salary-class"
        };
        
        String clazz = "marital-status";
        
        System.out.println(" - Logistic regression");
        ClassificationConfigurationLogisticRegression logisticClassifier = ARXClassificationConfiguration.createLogisticRegression();
        logisticClassifier.setUseTrainingTestSet(useTestTrainingSet);        
        System.out.println(data.getStatistics().getClassificationPerformance(features, clazz, logisticClassifier));

        System.out.println(" - Naive bayes");
        ClassificationConfigurationNaiveBayes naiveBayesClassifier = ARXClassificationConfiguration.createNaiveBayes();
        naiveBayesClassifier.setUseTrainingTestSet(useTestTrainingSet);
        System.out.println(data.getStatistics().getClassificationPerformance(features, clazz, naiveBayesClassifier));
        
        System.out.println(" - Random forest");
        ClassificationConfigurationRandomForest randomForestClassifier = ARXClassificationConfiguration.createRandomForest();
        randomForestClassifier.setUseTrainingTestSet(useTestTrainingSet);
        System.out.println(data.getStatistics().getClassificationPerformance(features, clazz, randomForestClassifier));
    }
}