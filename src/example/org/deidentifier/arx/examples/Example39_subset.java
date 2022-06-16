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
import java.io.IOError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.HashSet;
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
 * It shows how to use subset for training and different subset for testing
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example39_subset extends Example {
    
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

    public static  Set<Integer> getRandomDataSubsetIndices(double dataSize, Data inputData, int numRecords) {
        
        if (dataSize < 0d || dataSize > 1d) {
                     System.out.println(" data size ratio is out of range");
                     throw new IOError(new Exception());                  
         }
    
         // Create a data subset via sampling based on beta
         Set<Integer> subsetIndices = new HashSet<Integer>();
         Random random = new SecureRandom();
         for (int i = 0; i < numRecords; ++i) {
             if (random.nextDouble() < dataSize) {
                 subsetIndices.add(i);
             }
         }
         return subsetIndices;
     }    

    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws ParseException
     * @throws IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        
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
        
        Data data = createData("adult");
        data.getDefinition().setAttributeType("marital-status", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setResponseVariable("marital-status", true);
        
        // Create a training subset data with a specific percentage of the original data e.g 80%

        double dataSize = 0.80; 
        
        // Createing a view from the original dataset 
        Set<Integer>  subsetIndicesTrain = getRandomDataSubsetIndices(dataSize,  data,  data.getHandle().getNumRows()) ;        

        System.out.println("Creating a training data subset ....");
        DataSubset datasubTrain = DataSubset.create(data.getHandle().getNumRows(), subsetIndicesTrain);
   
        // To create a testing subset data from the remaining data we can use this commented  code
        
        // Set<Integer> subsetIndicesTest = new HashSet<Integer>();       
        // for (int i = 0; i < data.getHandle().getNumRows(); ++i) {
        //     subsetIndicesTest.add(i);
        // }
        // subsetIndicesTest.removeAll(subsetIndicesTrain);

        // System.out.println("Creating a testing data subset ....");
        // DataSubset datasubTest = DataSubset.create(data.getHandle().getNumRows(), subsetIndicesTest);

        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(5));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createClassificationMetric());
        
   
        
        // Adding the data subset to the current configuration,
        // this subset will be used for the anonymization, 
        // other records will be transformed but only suppressed,   
        // In the training, only the subset will be used 
        config.addPrivacyModel(new Inclusion (datasubTrain) );
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createClassificationMetric());
        
        // Start anonymization process
        ARXResult result = anonymizer.anonymize(data, config);
        boolean evaluateWithKfold = true; 
        
        System.out.println("5-anonymous dataset (logistic regression)");
        ClassificationConfigurationLogisticRegression logisticClassifier = ARXClassificationConfiguration.createLogisticRegression();
        logisticClassifier.setEvaluateWithKfold(evaluateWithKfold);        
        System.out.println(result.getOutput().getStatistics().getClassificationPerformance(features, clazz, logisticClassifier));

        System.out.println("5-anonymous dataset (naive bayes)");
        ClassificationConfigurationNaiveBayes naiveBayesClassifier = ARXClassificationConfiguration.createNaiveBayes();
        naiveBayesClassifier.setEvaluateWithKfold(evaluateWithKfold);
        System.out.println(result.getOutput().getStatistics().getClassificationPerformance(features, clazz, naiveBayesClassifier));
        
        System.out.println("5-anonymous dataset (random forest)");
        ClassificationConfigurationRandomForest randomForestClassifier = ARXClassificationConfiguration.createRandomForest();
        randomForestClassifier.setEvaluateWithKfold(evaluateWithKfold);
        System.out.println(result.getOutput().getStatistics().getClassificationPerformance(features, clazz, randomForestClassifier));
    }
}


/**
 * ===========================================================
 * Example output with evaluateWithKfold = true;
 * ===========================================================
 Creating a training data subset ....
5-anonymous dataset (logistic regression)
StatisticsClassification{
 - Accuracy:
   * Original: 0.6953119819640607
   * ZeroR: 0.4663152310854718
   * Output: 0.6940189642596645
 - Average error:
   * Original: 0.4301467184165105
   * ZeroR: 0.5336847689145282
   * Output: 0.43041888382409704
 - Brier score:
   * Original: 0.4333317703310125
   * ZeroR: 0.6572252917603948
   * Output: 0.4372894125209676
 - Number of classes: 7
 - Number of measurements: 30162
}
5-anonymous dataset (naive bayes)
StatisticsClassification{
 - Accuracy:
   * Original: 0.6447516742921557
   * ZeroR: 0.4663152310854718
   * Output: 0.6722697433857171
 - Average error:
   * Original: 0.38050937350272185
   * ZeroR: 0.5336847689145282
   * Output: 0.35648745375532154
 - Brier score:
   * Original: 0.5499427575714274
   * ZeroR: 0.6572252917603948
   * Output: 0.512156556610383
 - Number of classes: 7
 - Number of measurements: 30162
}
5-anonymous dataset (random forest)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
StatisticsClassification{
 - Accuracy:
   * Original: 0.603772959352828
   * ZeroR: 0.4663152310854718
   * Output: 0.6156421987931835
 - Average error:
   * Original: 0.5699613869024831
   * ZeroR: 0.5336847689145282
   * Output: 0.5552447567631911
 - Brier score:
   * Original: 0.5317809865104269
   * ZeroR: 0.6572252917603948
   * Output: 0.5042967130971948
 - Number of classes: 7
 - Number of measurements: 30162
}

 
 * ===========================================================
 * Example output with evaluateWithKfold = false;
 * ===========================================================

Creating a training data subset ....
5-anonymous dataset (logistic regression)
StatisticsClassification{
 - Accuracy:
    Original: 0.69
    ZeroR: 0.46
    Output: 0.6845
 - Average error:
    Original: 0.4357585527644324
    ZeroR: 0.54
    Output: 0.4377426743661062
 - Brier score:
    Original: 0.888404431332961
    ZeroR: 0.932658447872528
    Output: 0.8896271153039844
 - Number of classes: 7
 - Number of measurements: 6000
}
5-anonymous dataset (naive bayes)
StatisticsClassification{
 - Accuracy:
    Original: 0.6406666666666667
    ZeroR: 0.46
    Output: 0.6766666666666666
 - Average error:
    Original: 0.3822702437028365
    ZeroR: 0.54
    Output: 0.3566992097114707
 - Brier score:
    Original: 0.9110100351549149
    ZeroR: 0.932658447872528
    Output: 0.9023854777043593
 - Number of classes: 7
 - Number of measurements: 6000
}
5-anonymous dataset (random forest)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
StatisticsClassification{
 - Accuracy:
    Original: 0.5766666666666667
    ZeroR: 0.46
    Output: 0.5466666666666666
 - Average error:
    Original: 0.5760646545342571
    ZeroR: 0.54
    Output: 0.5770816662108619
 - Brier score:
    Original: 0.908826414086422
    ZeroR: 0.932658447872528
    Output: 0.9066104920932022
 - Number of classes: 7
 - Number of measurements: 6000
}

 */
