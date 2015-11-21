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
package org.deidentifier.arx.aggregates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deidentifier.arx.DataHandleStatistics;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Statistics representing the prediction accuracy of a data mining
 * classification operator
 * 
 * @author Fabian Prasser
 */
public class StatisticsClassification {

    /** Random */
    private final Random         random;
    /** Features and class */
    private final int[]          attributeIndexes;
    /** Index of class */
    private final int            classIndexInIndexesArray;
    /** Interrupt flag */
    private final WrappedBoolean interrupt;

    /** Result */
    private Double               resultPctCorrect = null;
    /** Result */
    private Double               resultAvgCost = null;
    /** Result */
    private Double               resultCorrect = null;
    /** Result */
    private Double               resultCorrelationCoefficient = null;
    /** Result */
    private Double               resultErrorRate = null;
    /** Result */
    private Double               resultIncorrect = null;
    /** Result */
    private Double               resultKappa = null;
    /** Result */
    private Double               resultKBInformation = null;
    /** Result */
    private Double               resultKBMeanInformation = null;
    /** Result */
    private Double               resultKBRelativeInformation = null;
    /** Result */
    private Double               resultMeanAbsoluteError = null;
    /** Result */
    private Double               resultMeanPriorAbsoluteError = null;
    /** Result */
    private Double               resultPctIncorrect = null;
    /** Result */
    private Double               resultPctUnclassified = null;
    /** Result */
    private Double               resultPriorEntropy = null;
    /** Result */
    private Double               resultRelativeAbsoluteError = null;
    /** Result */
    private Double               resultRootMeanPriorSquaredError = null;
    /** Result */
    private Double               resultRootMeanSquaredError = null;
    /** Result */
    private Double               resultRootRelativeSquaredError = null;
    /** Result */
    private Double               resultSFEntropyGain = null;
    /** Result */
    private Double               resultSFMeanEntropyGain = null;
    /** Result */
    private Double               resultSFMeanPriorEntropy = null;
    /** Result */
    private Double               resultSFMeanSchemeEntropy = null;
    /** Result */
    private Double               resultSFPriorEntropy = null;
    /** Result */
    private Double               resultSFSchemeEntropy = null;
    /** Result */
    private Double               resultTotalCost = null;
    /** Result */
    private Double               resultUnclassified = null;
    /** Result */
    private Double               resultWeightedAreaUnderROC = null;
    /** Result */
    private Double               resultWeightedFalseNegativeRate = null;
    /** Result */
    private Double               resultWeightedFalsePositiveRate = null;
    /** Result */
    private Double               resultWeightedFMeasure = null;
    /** Result */
    private Double               resultWeightedPrecision = null;
    /** Result */
    private Double               resultWeightedRecall = null;
    /** Result */
    private Double               resultWeightedTrueNegativeRate = null;
    /** Result */
    private Double               resultWeightedTruePositiveRate = null;
    /** Result: exception */
    private Exception            resultException = null;

    /**
     * Creates a new set of statistics for the given classification task
     * @param handle - The handle
     * @param features - The feature attributes
     * @param clazz - The class attributes
     * @param seed - The random seed, null, if the process should be randomized
     * @param samplingFraction - The sampling fraction
     * @throws ParseException 
     */
    @SuppressWarnings("deprecation")
    StatisticsClassification(DataHandleStatistics handle,
                             String[] features,
                             String clazz,
                             Integer seed,
                             double samplingFraction,
                             WrappedBoolean interrupt) throws ParseException {

        // Init
        this.interrupt = interrupt;
        
        // Check and clean up
        if (samplingFraction <= 0d || samplingFraction > 1d) {
            throw new IllegalArgumentException("Samling fraction must be in ]0,1]");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("No class attribute defined");
        }
        if (handle.getColumnIndexOf(clazz) == -1) {
            throw new IllegalArgumentException("Unknown class '"+clazz+"'");
        }
        if (features == null) {
            throw new IllegalArgumentException("No features defined");
        }
        List<String> featuresList = new ArrayList<>();
        for (String feature : features) {
            if (feature == null) {
                throw new IllegalArgumentException("Feature must not be null");    
            }
            if (handle.getColumnIndexOf(feature) == -1) {
                throw new IllegalArgumentException("Unknown feature '"+feature+"'");
            }
            if (!feature.equals(clazz)) {
                featuresList.add(feature);
            }
        }
        features = featuresList.toArray(new String[featuresList.size()]);
        
        // Initialize random
        if (seed == null) {
            this.random = new Random();
        } else {
            this.random = new Random(seed);
        }
        
        // Create indexes
        this.attributeIndexes = getAttributeIndexes(handle, features, clazz);
        this.classIndexInIndexesArray = getClassIndex(handle, attributeIndexes, clazz);
        
        // Create a schema for the training set
        final Instances instances = getSchema(handle, attributeIndexes, classIndexInIndexesArray);
        
        // Add data to the training set
        for (int row=0; row < handle.getNumRows(); row++) {
            
            // Sample
            if (random.nextDouble() <= samplingFraction) {
             
                // Convert and add
                instances.add(getInstance(instances, handle, row, attributeIndexes));
            }
            
            // Check
            checkInterrupt();
        }
        
        // Execute WEKA in an interruptible background thread
        // WEKA does not support Thread.interrupt(), so we have to work around this
        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Run J48 classifier
                    J48 tree = new J48();
                    tree.setConfidenceFactor(0.25f);
                    tree.setMinNumObj(2);

                    // Perform 10-fold cross-validation
                    Evaluation eval = new Evaluation(instances);
                    eval.crossValidateModel(tree, instances, 10, random);
                        
                    // Store results
                    try { resultPctCorrect = eval.pctCorrect(); } catch (Exception e) { /* That's fine*/}
                    try { resultAvgCost = eval.avgCost(); } catch (Exception e) { /* That's fine*/}
                    try { resultCorrect = eval.correct(); } catch (Exception e) { /* That's fine*/}
                    try { resultCorrelationCoefficient = eval.correlationCoefficient(); } catch (Exception e) { /* That's fine*/}
                    try { resultErrorRate = eval.errorRate(); } catch (Exception e) { /* That's fine*/}
                    try { resultIncorrect = eval.incorrect(); } catch (Exception e) { /* That's fine*/}
                    try { resultKappa = eval.kappa(); } catch (Exception e) { /* That's fine*/}
                    try { resultKBInformation = eval.KBInformation(); } catch (Exception e) { /* That's fine*/} 
                    try { resultKBMeanInformation = eval.KBMeanInformation(); } catch (Exception e) { /* That's fine*/}
                    try { resultKBRelativeInformation = eval.KBRelativeInformation(); } catch (Exception e) { /* That's fine*/}
                    try { resultMeanAbsoluteError = eval.meanAbsoluteError(); } catch (Exception e) { /* That's fine*/}
                    try { resultMeanPriorAbsoluteError = eval.meanPriorAbsoluteError(); } catch (Exception e) { /* That's fine*/}
                    try { resultPctIncorrect = eval.pctIncorrect(); } catch (Exception e) { /* That's fine*/}
                    try { resultPctUnclassified = eval.pctUnclassified(); } catch (Exception e) { /* That's fine*/}
                    try { resultPriorEntropy = eval.priorEntropy(); } catch (Exception e) { /* That's fine*/}
                    try { resultRelativeAbsoluteError = eval.relativeAbsoluteError(); } catch (Exception e) { /* That's fine*/}
                    try { resultRootMeanPriorSquaredError = eval.rootMeanPriorSquaredError(); } catch (Exception e) { /* That's fine*/}
                    try { resultRootMeanSquaredError = eval.rootMeanSquaredError(); } catch (Exception e) { /* That's fine*/}
                    try { resultRootRelativeSquaredError = eval.rootRelativeSquaredError(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFEntropyGain = eval.SFEntropyGain(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFMeanEntropyGain = eval.SFMeanEntropyGain(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFMeanPriorEntropy = eval.SFMeanPriorEntropy(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFMeanSchemeEntropy = eval.SFMeanSchemeEntropy(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFPriorEntropy = eval.SFPriorEntropy(); } catch (Exception e) { /* That's fine*/}
                    try { resultSFSchemeEntropy = eval.SFSchemeEntropy(); } catch (Exception e) { /* That's fine*/}
                    try { resultTotalCost = eval.totalCost(); } catch (Exception e) { /* That's fine*/}
                    try { resultUnclassified = eval.unclassified(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedAreaUnderROC = eval.weightedAreaUnderROC(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedFalseNegativeRate = eval.weightedFalseNegativeRate(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedFalsePositiveRate = eval.weightedFalsePositiveRate(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedFMeasure = eval.weightedFMeasure(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedPrecision = eval.weightedPrecision(); } catch (Exception e) { /* That's fine*/} 
                    try { resultWeightedRecall = eval.weightedRecall(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedTrueNegativeRate = eval.weightedTrueNegativeRate(); } catch (Exception e) { /* That's fine*/}
                    try { resultWeightedTruePositiveRate = eval.weightedTruePositiveRate(); } catch (Exception e) { /* That's fine*/}
                } catch (Exception e) {
                    resultException = e;
                }
            } 
        });
        background.setDaemon(true);
        background.start();
        
        // Wait for thread to finish
        while (background.isAlive() && !interrupt.value) {

            // Sleep
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        // Check interrupt
        if (interrupt.value) {
            if (background.isAlive()) {
                background.stop();
            }
            throw new ComputationInterruptedException("Training and evaluation interrupted");
            
        // Throw exception
        } else {
            if (resultException != null) {
                throw (new RuntimeException(resultException));
            }
        }
    }
    
    /**
     * Returns data about the classifier
     */
    public double getAvgCost() {
        return resultAvgCost;
    }

    /**
     * Returns data about the classifier
     */
    public double getCorrect() {
        return resultCorrect;
    }

    /**
     * Returns data about the classifier
     */
    public double getCorrelationCoefficient() {
        return resultCorrelationCoefficient;
    }

    /**
     * Returns data about the classifier
     */
    public double getErrorRate() {
        return resultErrorRate;
    }
    
    /**
     * Returns data about the classifier
     */
    public double getIncorrect() {
        return resultIncorrect;
    }

    /**
     * Returns data about the classifier
     */
    public double getKappa() {
        return resultKappa;
    }

    /**
     * Returns data about the classifier
     */
    public double getKBInformation() {
        return resultKBInformation;
    }

    /**
     * Returns data about the classifier
     */
    public double getKBMeanInformation() {
        return resultKBMeanInformation;
    }

    /**
     * Returns data about the classifier
     */
    public double getKBRelativeInformation() {
        return resultKBRelativeInformation;
    }

    /**
     * Returns data about the classifier
     */
    public double getMeanAbsoluteError() {
        return resultMeanAbsoluteError;
    }

    /**
     * Returns data about the classifier
     */
    public double getMeanPriorAbsoluteError() {
        return resultMeanPriorAbsoluteError;
    }

    /**
     * Returns data about the classifier
     */
    public double getPctCorrect() {
        return resultPctCorrect;
    }

    /**
     * Returns data about the classifier
     */
    public double getPctIncorrect() {
        return resultPctIncorrect;
    }

    /**
     * Returns data about the classifier
     */
    public double getPctUnclassified() {
        return resultPctUnclassified;
    }

    /**
     * Returns data about the classifier
     */
    public double getPriorEntropy() {
        return resultPriorEntropy;
    }

    /**
     * Returns data about the classifier
     */
    public double getRelativeAbsoluteError() {
        return resultRelativeAbsoluteError;
    }

    /**
     * Returns data about the classifier
     */
    public double getRootMeanPriorSquaredError() {
        return resultRootMeanPriorSquaredError;
    }

    /**
     * Returns data about the classifier
     */
    public double getRootMeanSquaredError() {
        return resultRootMeanSquaredError;
    }

    /**
     * Returns data about the classifier
     */
    public double getRootRelativeSquaredError() {
        return resultRootRelativeSquaredError;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFEntropyGain() {
        return resultSFEntropyGain;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFMeanEntropyGain() {
        return resultSFMeanEntropyGain;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFMeanPriorEntropy() {
        return resultSFMeanPriorEntropy;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFMeanSchemeEntropy() {
        return resultSFMeanSchemeEntropy;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFPriorEntropy() {
        return resultSFPriorEntropy;
    }

    /**
     * Returns data about the classifier
     */
    public double getSFSchemeEntropy() {
        return resultSFSchemeEntropy;
    }

    /**
     * Returns data about the classifier
     */
    public double getTotalCost() {
        return resultTotalCost;
    }

    /**
     * Returns data about the classifier
     */
    public double getUnclassified() {
        return resultUnclassified;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedAreaUnderROC() {
        return resultWeightedAreaUnderROC;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedFalseNegativeRate() {
        return resultWeightedFalseNegativeRate;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedFalsePositiveRate() {
        return resultWeightedFalsePositiveRate;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedFMeasure() {
        return resultWeightedFMeasure;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedPrecision() {
        return resultWeightedPrecision;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedRecall() {
        return resultWeightedRecall;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedTrueNegativeRate() {
        return resultWeightedTrueNegativeRate;
    }

    /**
     * Returns data about the classifier
     */
    public double getWeightedTruePositiveRate() {
        return resultWeightedTruePositiveRate;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isAvgCostAvailable() {
        return resultAvgCost != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isCorrectAvailable() {
        return resultCorrect != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isCorrelationCoefficientAvailable() {
        return resultCorrelationCoefficient != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isErrorRateAvailable() {
        return resultErrorRate != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isIncorrectAvailable() {
        return resultIncorrect != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isKappaAvailable() {
        return resultKappa != null;
    }
    
    /**
     * Returns whether data about the classifier is available
     */
    public boolean isKBInformationAvailable() {
        return resultKBInformation != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isKBMeanInformationAvailable() {
        return resultKBMeanInformation != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isKBRelativeInformationAvailable() {
        return resultKBRelativeInformation != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isMeanAbsoluteErrorAvailable() {
        return resultMeanAbsoluteError != null;
    }
    
    /**
     * Returns whether data about the classifier is available
     */
    public boolean isMeanPriorAbsoluteErrorAvailable() {
        return resultMeanPriorAbsoluteError != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isPctCorrectAvailable() {
        return resultPctCorrect != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isPctIncorrectAvailable() {
        return resultPctIncorrect != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isPctUnclassifiedAvailable() {
        return resultPctUnclassified != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isPriorEntropyAvailable() {
        return resultPriorEntropy != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isRelativeAbsoluteErrorAvailable() {
        return resultRelativeAbsoluteError != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isRootMeanPriorSquaredErrorAvailable() {
        return resultRootMeanPriorSquaredError != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isRootMeanSquaredErrorAvailable() {
        return resultRootMeanSquaredError != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isRootRelativeSquaredErrorAvailable() {
        return resultRootRelativeSquaredError != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFEntropyGainAvailable() {
        return resultSFEntropyGain != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFMeanEntropyGainAvailable() {
        return resultSFMeanEntropyGain != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFMeanPriorEntropyAvailable() {
        return resultSFMeanPriorEntropy != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFMeanSchemeEntropyAvailable() {
        return resultSFMeanSchemeEntropy != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFPriorEntropyAvailable() {
        return resultSFPriorEntropy != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isSFSchemeEntropyAvailable() {
        return resultSFSchemeEntropy != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isTotalCostAvailable() {
        return resultTotalCost != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isUnclassifiedAvailable() {
        return resultUnclassified != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedAreaUnderROCAvailable() {
        return resultWeightedAreaUnderROC != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedFalseNegativeRateAvailable() {
        return resultWeightedFalseNegativeRate != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedFalsePositiveRateAvailable() {
        return resultWeightedFalsePositiveRate != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedFMeasureAvailable() {
        return resultWeightedFMeasure != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedPrecisionAvailable() {
        return resultWeightedPrecision != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedRecallAvailable() {
        return resultWeightedRecall != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedTrueNegativeRateAvailable() {
        return resultWeightedTrueNegativeRate != null;
    }

    /**
     * Returns whether data about the classifier is available
     */
    public boolean isWeightedTruePositiveRateAvailable() {
        return resultWeightedTruePositiveRate != null;
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Returns the indexes of all relevant attributes
     * @param handle
     * @param features
     * @param clazz
     * @return
     */
    private int[] getAttributeIndexes(DataHandleStatistics handle, String[] features, String clazz) {
        // Collect
        List<Integer> list = new ArrayList<>();
        for (int column = 0; column < handle.getNumColumns(); column++) {
            String attribute = handle.getAttributeName(column);
            if (attribute.equals(clazz) || isContained(features, attribute)) {
                list.add(column);
            }
        }
        
        // Convert
        int[] result = new int[list.size()];
        for (int i=0; i<list.size(); i++) {
            result[i] = list.get(i);
        }
        
        // Return
        return result;
    }

    /**
     * Returns the index of the class attribute in the array of indexes
     * @param handle
     * @param attribueIndexes
     * @param clazz
     * @return
     */
    private int getClassIndex(DataHandleStatistics handle, int[] attribueIndexes, String clazz) {
        
        int index = handle.getColumnIndexOf(clazz);
        for (int i=0; i<attributeIndexes.length; i++) {
            if (attributeIndexes[i] == index) {
                return i;
            }
        }
        throw new IllegalStateException("Did not find class in array");
    }

    /**
     * Creates a dense instance for the i-th row
     * @param schema
     * @param handle
     * @param row
     * @param attributeIndexes
     * @return
     * @throws ParseException 
     */
    private Instance getInstance(Instances schema, DataHandleStatistics handle, int row, int[] attributeIndexes) throws ParseException {

        // Prepare
        Instance instance = new Instance(attributeIndexes.length);
        
        // For each attribute
        for (int i = 0; i < attributeIndexes.length; i++) {

            // Obtain meta data
            int column = attributeIndexes[i];
            String name = handle.getAttributeName(column);
            DataType<?> type = handle.getDefinition().getDataType(name);
            
            // Numeric
            if (type instanceof ARXDecimal) {
                instance.setValue(schema.attribute(i), handle.getDouble(row, column));
            } else if (type instanceof ARXInteger) {
                instance.setValue(schema.attribute(i), handle.getLong(row, column).doubleValue());
            } else if (type instanceof ARXDate) {
                instance.setValue(schema.attribute(i), (double)handle.getDate(row, column).getTime());
                
            // Nominal
            } else {
                instance.setValue(schema.attribute(i), handle.getValue(row, column));
            }
        }
                
        // Return
        return instance;
    }

    /**
     * Creates a schema for the relation
     * @param handle
     * @param attributeIndexes
     * @param classIndex
     * @return
     */
    private Instances getSchema(DataHandleStatistics handle, int[] attributeIndexes, int classIndex) {
        
        // Init
        FastVector schema = new FastVector(attributeIndexes.length);
        
        // Add per column
        for (int column : attributeIndexes) {
            
            // Name and type
            String name = handle.getAttributeName(column);
            DataType<?> type = handle.getDefinition().getDataType(name);
            
            // Numeric
            if (type instanceof ARXDecimal || type instanceof ARXInteger || type instanceof ARXDate) {
                schema.addElement(new Attribute(name));
                
            // Nominal
            } else {
        
                String[] array = handle.getDistinctValues(column);
                FastVector values = new FastVector(array.length);
                for (String value : array) {
                    values.addElement(value);
                }
                schema.addElement(new Attribute(name, values));
            }
        }
        
        // Create training set
        Instances instances = new Instances("ARXWekaTrainingSet", schema, 10);
        
        // Set class index
        instances.setClassIndex(classIndex);
        
        // Return
        return instances;
    }

    /**
     * Returns whether the given array contains the given value
     * @param array
     * @param value
     * @return
     */
    private boolean isContained(String[] array, String value) {
        for (String element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
    }
}