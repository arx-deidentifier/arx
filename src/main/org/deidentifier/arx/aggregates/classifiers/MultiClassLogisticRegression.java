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
package org.deidentifier.arx.aggregates.classifiers;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;

/**
 * Implements a classifier
 * @author Fabian Prasser
 */
public class MultiClassLogisticRegression implements Classifier{
    
    /** Instance*/
    private final OnlineLogisticRegression lr;

    /**
     * Creates a new instance
     * @param features
     * @param classes
     */
    public MultiClassLogisticRegression(int features, int classes) {

        // Check
        if (features == 0) {
            features = 1;
        }

        // Prepare classifier
        this.lr = new OnlineLogisticRegression(classes, features, new L1());
        
        // Configure
        this.lr.learningRate(1);
        this.lr.alpha(1);
        this.lr.lambda(0.000001);
        this.lr.stepOffset(10000);
        this.lr.decayExponent(0.2);
    }

    @Override
    public int classify(Vector features) {
        return lr.classifyFull(features).maxValueIndex();
    }

    @Override
    public void close() {
        lr.close();
    }

    @Override
    public void train(Vector features, int clazz) {
        lr.train(clazz, features);
    }
}
