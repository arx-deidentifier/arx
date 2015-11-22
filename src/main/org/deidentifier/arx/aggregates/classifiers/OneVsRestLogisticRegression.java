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
public class OneVsRestLogisticRegression implements Classifier{
    
    /** Instances*/
    private final OnlineLogisticRegression[] classifiers;
    
    /**
     * Creates a new instance
     * @param features
     * @param classes
     */
    public OneVsRestLogisticRegression(int features, int classes) {
        
        this.classifiers = new OnlineLogisticRegression[classes];
        for (int i = 0; i < classes; i++) {

            // Prepare classifier
            this.classifiers[i] = new OnlineLogisticRegression(2, features, new L1());
            
            // Configure
            this.classifiers[i].learningRate(1);
            this.classifiers[i].alpha(1);
            this.classifiers[i].lambda(0.000001);
            this.classifiers[i].stepOffset(10000);
            this.classifiers[i].decayExponent(0.2);
        }
    }

    @Override
    public int classify(Vector features) {
        
        double maxProbability = -1d;
        int mostProbableClass = -1;
        
        for (int i = 0; i < this.classifiers.length; i++) {
            double probability = this.classifiers[i].classifyScalar(features);
            if (probability > maxProbability) {
                mostProbableClass = i;
                maxProbability = probability;
            }
        }
        
        if (mostProbableClass == -1) {
            throw new IllegalStateException("Could not be classified");
        }
        return mostProbableClass;
    }

    @Override
    public void close() {
        for (int i = 0; i < this.classifiers.length; i++) {
            this.classifiers[i].close();
        }
    }

    @Override
    public void train(Vector features, int clazz) {
        for (int i = 0; i < this.classifiers.length; i++) {
            int actual = i == clazz ? 1 : 0;
            this.classifiers[i].train(actual, features);
        }
    }
}