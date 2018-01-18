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

/**
 * A classification result
 * 
 * @author Fabian Prasser
 */
public class MultiClassRandomForestClassificationResult implements ClassificationResult {

    /** Field */
    private final Map<String, Integer> map;
    /** Field */
    private final int                  result;
    /** Field */
    private final double[]             probabilities;
    
    /**
     * Creates a new instance
     * @param result
     * @param probabilities
     * @param map
     */
    MultiClassRandomForestClassificationResult(int result, double[] probabilities, Map<String, Integer> map) {
        this.map = map;
        this.probabilities = probabilities;
        this.result = result;
    }

    @Override
    public double[] confidences() {
        return this.probabilities;
    }

    @Override
    public boolean correct(String clazz) {
        return result == map.get(clazz).intValue();
    }

    @Override
    public double error(String clazz) {
        return 1d - probabilities[map.get(clazz)];
    }

    @Override
    public int index() {
        return result;
    }
}
