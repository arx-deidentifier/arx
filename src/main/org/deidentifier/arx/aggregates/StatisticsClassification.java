/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.classification.ClassificationDataSpecification;
import org.deidentifier.arx.aggregates.classification.ClassificationMethod;
import org.deidentifier.arx.aggregates.classification.ClassificationResult;
import org.deidentifier.arx.aggregates.classification.MultiClassLogisticRegression;
import org.deidentifier.arx.aggregates.classification.MultiClassNaiveBayes;
import org.deidentifier.arx.aggregates.classification.MultiClassRandomForest;
import org.deidentifier.arx.aggregates.classification.MultiClassSVM;
import org.deidentifier.arx.aggregates.classification.MultiClassZeroR;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;
import org.deidentifier.arx.exceptions.UnexpectedErrorException;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * Statistics representing the performance of various classifiers
 * 
 * @author Fabian Prasser
 * @author Johanna Eicher
 */
public class StatisticsClassification {

    /**
     * A matrix mapping confidence thresholds to precision, recall and f-score
     * 
     * @author Fabian Prasser
     *
     */
    public static class PrecisionRecallMatrix {
        
        /** Measurements */
        private double                measurements          = 0d;
        /** Precision */
        private final double[]        precision             = new double[CONFIDENCE_THRESHOLDS.length];
        /** Recall */
        private final double[]        recall                = new double[CONFIDENCE_THRESHOLDS.length];
        /** F-Score */
        private final double[]        fscore                = new double[CONFIDENCE_THRESHOLDS.length];

        /**
         * Cut-off points
         * @return the confidence thresholds
         */
        public double[] getConfidenceThresholds() {
            return CONFIDENCE_THRESHOLDS;
        }
        
        /**
         * F-scores
         * @return the f-score
         */
        public double[] getFscore(){
            return fscore;
        }
        
        /**
         * Precision
         * @return the precision
         */
        public double[] getPrecision() {
            return precision;
        }

        /**
         * Recall
         * @return the recall
         */
        public double[] getRecall() {
            return recall;
        }

        /**
         * Adds a new value
         * @param confidence
         * @param correct
         */
        void add(double confidence, boolean correct) {
            
            int index = (int)(confidence * (CONFIDENCE_THRESHOLDS.length - 1d));
            recall[index]++;
            precision[index] += correct ? 1d : 0d;
            measurements++;
        }

        /**
         * Packs the results
         */
        void pack() {
            
            // Pack
            packConfidence(precision);
            packConfidence(recall);
            
            // Calculate
            for (int i = 0; i < CONFIDENCE_THRESHOLDS.length; i++) {
                if (recall[i] == 0d) {
                    precision[i] = 1d;
                } else {
                    precision[i] /= recall[i];
                    recall[i] /= measurements;
                }
                fscore[i] = 2 * (precision[i] * recall[i]) / (precision[i] + recall[i]);
            }
        }
    }

    /**
     * A ROC curve
     * 
     * @author Fabian Prasser
     *
     */
    public static class ROCCurve {

        /** Precision */
        private final double[] truePositive;
        /** Recall */
        private final double[] falsePositive;
        /** AUC */
        private final double   AUC;
        
        /**
         * Creates a new ROC curve
         * @param value
         * @param confidences
         * @param confidenceIndex
         * @param handle
         * @param handleIndex
         */
        private ROCCurve(String value,
                         Map<Integer, double[]> confidences,
                         int confidenceIndex,
                         DataHandleInternal handle,
                         int handleIndex) {
            
            int positives = 0;
            int valueID = handle.getValueIdentifier(handleIndex, value);
            final boolean[] correct = new boolean[confidences.size()];
            final double[] confidence = new double[confidences.size()];
            int j = 0;
            for (int index : confidences.keySet()) {
                correct[j] = (handle.getEncodedValue(index, handleIndex, true) == valueID);
                positives += correct[j] ? 1 : 0;
                confidence[j] = confidences.get(index)[confidenceIndex];
                j++;
            }
            
            // Sort by confidence
            GenericSorting.mergeSort(0, correct.length, new IntComparator() {
                @Override public int compare(int arg0, int arg1) {
                    return Double.compare(confidence[arg0], confidence[arg1]);
                }
            }, new Swapper() {
                @Override public void swap(int arg0, int arg1) {
                    double temp = confidence[arg0];
                    confidence[arg0] = confidence[arg1];
                    confidence[arg1] = temp;
                    boolean temp2 = correct[arg0];
                    correct[arg0] = correct[arg1];
                    correct[arg1] = temp2;
                }
            });
            
            // Initialize curve
            truePositive = new double[confidences.size()];
            falsePositive = new double[confidences.size()];
            
            // Draw curve
            int negatives = confidences.size() - positives;
            int x = 0;
            int y = 0;
            int offset=0;
            for (int i = correct.length - 1; i >= 0; i--) {
                x += correct[i] ? 0 : 1;
                y += correct[i] ? 1 : 0;
                falsePositive[offset] = (double)x/(double)(negatives);
                truePositive[offset] = (double)y/(double)positives;
                offset++;
            }

            // Calculate AUC: trapezoidal rule
            double AUC = 0d;
            for (int i=0; i<truePositive.length-1; i++) {
                double minX = Math.min(falsePositive[i], falsePositive[i + 1]);
                double maxX = Math.max(falsePositive[i], falsePositive[i + 1]);
                double minY = Math.min(truePositive[i], truePositive[i + 1]);
                double maxY = Math.max(truePositive[i], truePositive[i + 1]);
                AUC += (maxX - minX) * (minY + maxY) / 2d;
            }
            this.AUC = AUC;
        }

        /**
         * Returns the AUC
         * @return
         */
        public double getAUC() {
            return AUC;
        }

        /**
         * Returns false-positive rates for all cut-off points
         * @return the falsePositive
         */
        public double[] getFalsePositiveRate() {
            return falsePositive;
        }
        
        /**
         * Returns true-positive rates for all cut-off points
         * @return the truePositive
         */
        public double[] getTruePositiveRate() {
            return truePositive;
        }

    }
    
    /** Confidence thresholds: only change together with thresholds, steps and num*/
    private static final double[] CONFIDENCE_THRESHOLDS   = new double[]{
        0d, 0.1d, 0.2d, 0.3d, 0.4d, 0.5d, 0.6d, 0.7d, 0.8d, 0.9d, 1d
    };

    /**
     * Returns the classification method for the given config
     * @param specification
     * @param config
     * @return
     */
    private static ClassificationMethod getClassifier(ClassificationDataSpecification specification,
                                                      ARXClassificationConfiguration<?> config) {
        if (config instanceof ClassificationConfigurationLogisticRegression) {
            return new MultiClassLogisticRegression(specification, (ClassificationConfigurationLogisticRegression)config);
        } else if (config instanceof ClassificationConfigurationNaiveBayes) {
            System.setProperty("smile.threads", "1");
            return new MultiClassNaiveBayes(specification, (ClassificationConfigurationNaiveBayes)config);
        } else if (config instanceof ClassificationConfigurationSVM) {
            System.setProperty("smile.threads", "1");
            return new MultiClassSVM(specification, (ClassificationConfigurationSVM)config);
        } else if (config instanceof ClassificationConfigurationRandomForest) {
            System.setProperty("smile.threads", "1");
            return new MultiClassRandomForest(specification, (ClassificationConfigurationRandomForest)config);
        } else {
            throw new IllegalArgumentException("Unknown type of configuration");
        }
    }
    
    /**
     * Packs an array that is based on confidence intervals
     * @param array
     */
    private static void packConfidence(double[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            array[i - 1] += array[i];
        }
    }
    
    /** Accuracy */
    private double                accuracy;
    /** Average error */
    private double                averageError;
    /** Interrupt flag */
    private final WrappedBoolean  interrupt;
    /** Interrupt flag */
    private final WrappedInteger  progress;
    /** Precision/recall matrix */
    private PrecisionRecallMatrix matrix                = new PrecisionRecallMatrix();
    /** ROC curve */
    private Map<String, ROCCurve> ROC                   = new HashMap<>();
    /** Num classes */
    private int                   numClasses;
    /** Original accuracy */
    private double                originalAccuracy;
    /** Original accuracy */
    private double                originalAverageError;
    /** Precision/recall matrix */
    private PrecisionRecallMatrix originalMatrix        = new PrecisionRecallMatrix();
    /** ROC curve */
    private Map<String, ROCCurve> originalROC           = new HashMap<>();
    /** Random */
    private final Random          random;

    /** ZeroR accuracy */
    private double                zeroRAccuracy;

    /** ZeroR accuracy */
    private double                zeroRAverageError;
    
    /** Measurements */
    private int                   numMeasurements;

    /**
     * Creates a new set of statistics for the given classification task
     * @param inputHandle - The input features handle
     * @param outputHandle - The output features handle
     * @param features - The feature attributes
     * @param clazz - The class attributes
     * @param config - The configuration
     * @param scaling 
     * @param interrupt - The interrupt flag
     * @param progress 
     * @throws ParseException 
     */
    StatisticsClassification(DataHandleInternal inputHandle,
                             DataHandleInternal outputHandle,
                             String[] features,
                             String clazz,
                             ARXClassificationConfiguration<?> config,
                             ARXFeatureScaling scaling, 
                             WrappedBoolean interrupt,
                             WrappedInteger progress) throws ParseException {

        // Init
        this.interrupt = interrupt;
        this.progress = progress;
        
        // Number of rows
        int numRecords = inputHandle.getNumRows();
        // Sampling size
        int numSamples = getNumSamples(numRecords, config);
       
        // Initialize random
        if (!config.isDeterministic()) {
            this.random = new Random();
        } else {
            this.random = new Random(config.getSeed());
        }
        
        // Create specification
        ClassificationDataSpecification specification = new ClassificationDataSpecification(inputHandle, 
                                                                                            outputHandle, 
                                                                                            scaling,
                                                                                            features,
                                                                                            clazz,
                                                                                            interrupt);
        
        // Train and evaluate
        int k = numSamples > config.getNumFolds() ? config.getNumFolds() : numSamples;
        List<List<Integer>> folds = getFolds(numRecords, numSamples, k);

        // Track
        int classifications = 0;
        double total = 100d / ((double)numSamples * (double)folds.size());
        double done = 0d;
        
        // ROC
        Map<Integer, double[]> originalConfidences = new HashMap<Integer, double[]>();
        Map<Integer, double[]> confidences = new HashMap<Integer, double[]>();
                
        // For each fold as a validation set
        for (int evaluationFold = 0; evaluationFold < folds.size(); evaluationFold++) {
            
            // Create classifiers
            ClassificationMethod inputLR = getClassifier(specification, config);
            ClassificationMethod inputZR = new MultiClassZeroR(specification);
            ClassificationMethod outputLR = null;
            if (inputHandle != outputHandle) {
                outputLR = getClassifier(specification, config);
            }
            
            // Try
            try {
                
                // Train with all training sets
                boolean trained = false;
                for (int trainingFold = 0; trainingFold < folds.size(); trainingFold++) {
                    if (trainingFold != evaluationFold) {                        
                        for (int index : folds.get(trainingFold)) {
                            checkInterrupt();
                            inputLR.train(inputHandle, outputHandle, index);
                            inputZR.train(inputHandle, outputHandle, index);
                            if (outputLR != null && !outputHandle.isOutlier(index)) {
                                outputLR.train(outputHandle, outputHandle, index);
                            }
                            trained = true;
                            this.progress.value = (int)((++done) * total);
                        }
                    }
                }
                
                // Close
                inputLR.close();
                inputZR.close();
                if (outputLR != null) {
                    outputLR.close();
                }
                
                // Now validate
                for (int index : folds.get(evaluationFold)) {
                    
                    // Check
                    checkInterrupt();
                    
                    // If trained
                    if (trained) {
                        
                        // Classify
                        ClassificationResult resultInputLR = inputLR.classify(inputHandle, index);
                        ClassificationResult resultInputZR = inputZR.classify(inputHandle, index);
                        ClassificationResult resultOutputLR = outputLR == null ? null : outputLR.classify(outputHandle, index);
                        classifications++;
                        
                        // Correct result
                        String actualValue = outputHandle.getValue(index, specification.classIndex, true);
                        
                        // Maintain data about inputZR
                        this.zeroRAverageError += resultInputZR.error(actualValue);
                        this.zeroRAccuracy += resultInputZR.correct(actualValue) ? 1d : 0d;

                        // Maintain data about inputLR
                        boolean correct = resultInputLR.correct(actualValue);
                        this.originalAverageError += resultInputLR.error(actualValue);
                        this.originalAccuracy += correct ? 1d : 0d;
                        this.originalMatrix.add(resultInputLR.confidence(), correct);
                        originalConfidences.put(index, resultInputLR.confidences());
                        
                        // Maintain data about outputLR                        
                        if (resultOutputLR != null) {
                            correct = resultOutputLR.correct(actualValue);
                            this.averageError += resultOutputLR.error(actualValue);
                            this.accuracy += correct ? 1d : 0d;
                            this.matrix.add(resultOutputLR.confidence(), correct);
                            confidences.put(index, resultOutputLR.confidences());
                        }
                    }
                    this.progress.value = (int)((++done) * total);
                }
            } catch (Exception e) {
                if (e instanceof ComputationInterruptedException) {
                    throw e;
                } else {
                    throw new UnexpectedErrorException(e);
                }
            }
        }
        
        // Maintain data about inputZR
        this.zeroRAverageError /= (double)classifications;
        this.zeroRAccuracy/= (double)classifications;

        // Maintain data about inputLR
        this.originalAverageError /= (double)classifications;
        this.originalAccuracy /= (double)classifications;
        this.originalMatrix.pack();
        
        // Initialize ROC curves on original data
        for (String attr : specification.classMap.keySet()) {
            originalROC.put(attr, new ROCCurve(attr, originalConfidences, specification.classMap.get(attr), outputHandle, specification.classIndex));
        }
        // Initialize ROC curves on anonymized data
        if (!confidences.isEmpty()) {
            for (String attr : specification.classMap.keySet()) {
                ROC.put(attr, new ROCCurve(attr, confidences, specification.classMap.get(attr), outputHandle, specification.classIndex));
            }    
        }

        // Maintain data about outputLR                        
        if (inputHandle != outputHandle) {
            this.averageError /= (double)classifications;
            this.accuracy /= (double)classifications;
            this.matrix.pack();
        } else {
            this.averageError = this.originalAverageError;
            this.accuracy = this.originalAccuracy;
            this.matrix = this.originalMatrix;
        }
        
        this.numClasses = specification.classMap.size();
        this.numMeasurements = classifications;
    }

    /**
     * Returns the resulting accuracy. Obtained by training a
     * Logistic Regression classifier on the output (or input) dataset.
     * 
     * @return
     */
    public double getAccuracy() {
        return this.accuracy;
    }
    
    /**
     * Returns the average error, defined as avg(1d-probability-of-correct-result) for
     * each classification event.
     * 
     * @return
     */
    public double getAverageError() {
        return this.averageError;
    }

    /**
     * Returns the set of class attributes
     * @return
     */
    public Set<String> getClassValues() {
        return this.originalROC.keySet();
    }
    
    /**
     * Returns the number of classes
     * @return
     */
    public int getNumClasses() {
        return this.numClasses;
    }
    
    /**
     * Returns the number of measurements
     * @return
     */
    public int getNumMeasurements() {
        return this.numMeasurements;
    }

    /**
     * Returns the maximal accuracy. Obtained by training a
     * Logistic Regression classifier on the input dataset.
     * 
     * @return
     */
    public double getOriginalAccuracy() {
        return this.originalAccuracy;
    }

    /**
     * Returns the average error, defined as avg(1d-probability-of-correct-result) for
     * each classification event.
     * 
     * @return
     */
    public double getOriginalAverageError() {
        return this.originalAverageError;
    }
    
    /**
     * Returns a precision/recall matrix for LogisticRegression on input
     * @return
     */
    public PrecisionRecallMatrix getOriginalPrecisionRecall() {
        return this.originalMatrix;
    }
    
    /**
     * Returns the ROC curve for this class value calculated using the one-vs-all approach.
     * @param clazz
     * @return
     */
    public ROCCurve getOriginalROCCurve(String clazz) {
        return this.originalROC.get(clazz);
    }

    /**
     * Returns a precision/recall matrix
     * @return
     */
    public PrecisionRecallMatrix getPrecisionRecall() {
        return this.matrix;
    }
    
    /**
     * Returns the ROC curve for this class value calculated using the one-vs-all approach.
     * @param clazz
     * @return
     */
    public ROCCurve getROCCurve(String clazz) {
        return this.ROC.get(clazz);
    }
    
    /**
     * Returns the minimal accuracy. Obtained by training a
     * ZeroR classifier on the input dataset.
     * 
     * @return
     */
    public double getZeroRAccuracy() {
        return this.zeroRAccuracy;
    }

    /**
     * Returns the average error, defined as avg(1d-probability-of-correct-result) for
     * each classification event.
     * 
     * @return
     */
    public double getZeroRAverageError() {
        return this.zeroRAverageError;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StatisticsClassification{\n");
        builder.append(" - Accuracy:\n");
        builder.append("   * Original: ").append(originalAccuracy).append("\n");
        builder.append("   * ZeroR: ").append(zeroRAccuracy).append("\n");
        builder.append("   * Output: ").append(accuracy).append("\n");
        builder.append(" - Average error:\n");
        builder.append("   * Original: ").append(originalAverageError).append("\n");
        builder.append("   * ZeroR: ").append(zeroRAverageError).append("\n");
        builder.append("   * Output: ").append(averageError).append("\n");
        builder.append(" - Number of classes: ").append(numClasses).append("\n");
        builder.append(" - Number of measurements: ").append(numMeasurements).append("\n");
        builder.append("}");
        return builder.toString();
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
     * Creates the folds
     * @param numRecords
     * @param numSamples
     * @param k
     * @return
     */
    private List<List<Integer>> getFolds(int numRecords, int numSamples, int k) {
        
        // Prepare indexes of all records
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < numRecords; row++) {
            rows.add(row);
        }
        
        // Shuffle
        Collections.shuffle(rows, random);
        
        // Select subset of size numSamples
        rows = rows.subList(0, numSamples);

        // Create folds
        List<List<Integer>> folds = new ArrayList<>();
        int size = rows.size() / k;
        size = size > 1 ? size : 1;
        for (int i = 0; i < k; i++) {
            // For each fold
            int min = i * size;
            int max = (i + 1) * size;
            if (i == k - 1) {
                max = rows.size();
            }

            // Collect rows
            List<Integer> fold = new ArrayList<>();
            for (int j = min; j < max; j++) {
                fold.add(rows.get(j));
            }

            // Store
            folds.add(fold);
        }

        // Free
        rows.clear();
        rows = null;
        return folds;
    }
    
    /**
     * Returns the number of samples as the minimum of actual number of rows in
     * the dataset and maximal number of rows as specified in config.
     * 
     * @param numRows
     * @param config
     * @return
     */
    private int getNumSamples(int numRows, ARXClassificationConfiguration<?> config) {
        int numSamples = numRows;
        if (config.getMaxRecords() > 0) {
            numSamples = Math.min(config.getMaxRecords(), numSamples);
        }
        return numSamples;
    }
}
