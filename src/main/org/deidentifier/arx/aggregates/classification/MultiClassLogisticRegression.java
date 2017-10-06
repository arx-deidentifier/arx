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
package org.deidentifier.arx.aggregates.classification;

import org.apache.mahout.classifier.sgd.ElasticBandPrior;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.L2;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.PriorFunction;
import org.apache.mahout.classifier.sgd.UniformPrior;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;

/**
 * Implements a classifier
 * @author Fabian Prasser
 */
public class MultiClassLogisticRegression implements ClassificationMethod {

    /** Config */
    private final ClassificationConfigurationLogisticRegression config;
    /** Encoder */
    private final ConstantValueEncoder               interceptEncoder;
    /** Instance */
    private final OnlineLogisticRegression           lr;
    /** Specification */
    private final ClassificationDataSpecification    specification;
    /** Encoder */
    private final StaticWordValueEncoder             wordEncoder;

    /**
     * Creates a new instance
     * @param specification
     * @param config
     */
    public MultiClassLogisticRegression(ClassificationDataSpecification specification,
                                        ClassificationConfigurationLogisticRegression config) {

        // Store
        this.config = config;
        this.specification = specification;
        
        // Prepare classifier
        PriorFunction prior = null;
        switch (config.getPriorFunction()) {
        case ELASTIC_BAND:
            prior = new ElasticBandPrior();
            break;
        case L1:
            prior = new L1();
            break;
        case L2:
            prior = new L2();
            break;
        case UNIFORM:
            prior = new UniformPrior();
            break;
        default:
            throw new IllegalArgumentException("Unknown prior function");
        }
        this.lr = new OnlineLogisticRegression(this.specification.classMap.size(), config.getVectorLength(), prior);
        
        // Configure
        this.lr.learningRate(config.getLearningRate());
        this.lr.alpha(config.getAlpha());
        this.lr.lambda(config.getLambda());
        this.lr.stepOffset(config.getStepOffset());
        this.lr.decayExponent(config.getDecayExponent());    
        
        // Prepare encoders
        this.interceptEncoder = new ConstantValueEncoder("intercept");
        this.wordEncoder = new StaticWordValueEncoder("feature");
    }

    @Override
    public ClassificationResult classify(DataHandleInternal features, int row) {
        return new MultiClassLogisticRegressionClassificationResult(lr.classifyFull(encodeFeatures(features, row)), specification.classMap);
    }

    @Override
    public void close() {
        lr.close();
    }

    @Override
    public void train(DataHandleInternal features, DataHandleInternal clazz, int row) {
        lr.train(encodeClass(clazz, row), encodeFeatures(features, row));
    }

    /**
     * Encodes a class
     * @param handle
     * @param row
     * @return
     */
    private int encodeClass(DataHandleInternal handle, int row) {
        return specification.classMap.get(handle.getValue(row, specification.classIndex, true));
    }

    /**
     * Encodes a feature
     * @param handle
     * @param row
     * @return
     */
    private Vector encodeFeatures(DataHandleInternal handle, int row) {

        // Prepare
        DenseVector vector = new DenseVector(config.getVectorLength());
        interceptEncoder.addToVector("1", vector);
        
        // Special case where there are no features
        if (specification.featureIndices.length == 0) {
            wordEncoder.addToVector("Feature:1", 1, vector);
            return vector;
        }
        
        // For each attribute
        int count = 0;
        for (int index : specification.featureIndices) {
            
            // Obtain data
            ClassificationFeatureMetadata metadata = specification.featureMetadata[count];
            String value = handle.getValue(row, index, true);
            Double numeric = metadata.getNumericValue(value);
            if (Double.isNaN(numeric)) {    
                wordEncoder.addToVector("Attribute-" + index + ":" + value, 1, vector);
            } else {
                wordEncoder.addToVector("Attribute-" + index, numeric, vector);
            }
            count++;
        }
        
        // Return
        return vector;
    }
}
