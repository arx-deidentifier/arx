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

import org.apache.mahout.classifier.mlp.MultilayerPerceptron;
import org.apache.mahout.math.Vector;

/**
 * Implements a classifier
 * @author Fabian Prasser
 */
public class MultiClassMultiLayerPerceptron implements Classifier{

    /** Instance*/
    private final MultilayerPerceptron lr;

    /**
     * Implements a classifier
     * @author Fabian Prasser
     */
    public MultiClassMultiLayerPerceptron(int features, int classes) {

        // Prepare classifier
        this.lr = new MultilayerPerceptron();

        // Configure
        this.lr.setLearningRate(0.5);
        this.lr.setMomentumWeight(0.1);
        this.lr.setRegularizationWeight(0.01);
        this.lr.setCostFunction("SquaredError");

        // Add some layers
        this.lr.addLayer(features, false, "Identity");
        this.lr.addLayer((features + classes) / 2, false, "Sigmoid");
        this.lr.addLayer(classes, true, "Sigmoid");
    }

    @Override
    public int classify(Vector features) {
        Vector result = lr.getOutput(features);
        System.out.println(result.size());
        return result.maxValueIndex();
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
