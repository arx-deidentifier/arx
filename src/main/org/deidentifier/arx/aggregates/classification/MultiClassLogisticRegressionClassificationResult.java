/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Florian Kohlmayer, Fabian Prasser
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

import java.util.Map;

import org.apache.mahout.math.Vector;

/**
 * A classification result
 * 
 * @author Fabian Prasser
 */
public class MultiClassLogisticRegressionClassificationResult implements ClassificationResult {

    /** Field*/
    private final Map<String, Integer> map;
    /** Field*/
    private final Vector               vector;

    /**
     * Creates a new instance
     * @param vector
     * @param map
     */
    MultiClassLogisticRegressionClassificationResult(Vector vector, Map<String, Integer> map) {
        this.map = map;
        this.vector = vector;
    }

    @Override
    public double confidence() {
        return vector.getQuick(vector.maxValueIndex());
    }

    @Override
    public double[] confidences() {
        double[] result = new double[vector.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = vector.getQuick(i);
        }
        return result;
    }

    @Override
    public boolean correct(String clazz) {
        return vector.maxValueIndex() == map.get(clazz).intValue();
    }

    @Override
    public double error(String clazz) {
        return 1d - vector.getQuick(map.get(clazz));
    }

    @Override
    public int index() {
        return vector.maxValueIndex();
    }
}
