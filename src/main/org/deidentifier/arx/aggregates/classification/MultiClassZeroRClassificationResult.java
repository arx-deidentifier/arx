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
import java.util.Map.Entry;

/**
 * A classification result
 * 
 * @author Fabian Prasser
 */
public class MultiClassZeroRClassificationResult implements ClassificationResult {

    /** Field */
    private final Map<String, Integer> map;
    /** Counts */
    private final double[]             confidences;
    /** Field */
    private final Integer              result;

    /**
     * Creates a new instance
     * @param result
     * @param map
     */
    MultiClassZeroRClassificationResult(Map<Integer, Integer> counts, Map<String, Integer> map) {
        this.map = map;
        this.result = getIndexWithMostCounts(counts);
        this.confidences = getConfidences(counts);
    }

    @Override
    public double[] confidences() {
        return confidences;
    }

    @Override
    public boolean correct(String clazz) {
        if (result == null) {
            return false;
        }
        return result.intValue() == map.get(clazz).intValue();
    }

    @Override
    public double error(String clazz) {
        if (result == null) {
            return 1d;
        }
        if (correct(clazz)) {
            return 0d;
        } else {
            return 1d;
        }
    }

    /**
     * Calculate confidences
     * @param counts
     * @return
     */
    private double[] getConfidences(Map<Integer, Integer> counts) {
        double[] confidences = new double[counts.size()];
        int index=0;
        double sum = 0d;
        for(Integer count : counts.values()) {
            confidences[index++] = count;
            sum += count;
        }
        for (int i=0; i<confidences.length; i++) {
            confidences[i] /= sum;
        }
        return confidences;
    }

    /**
     * Returns the index of the most frequent element
     * @param counts 
     * @return
     */
    private Integer getIndexWithMostCounts(Map<Integer, Integer> counts) {
        int max = Integer.MIN_VALUE;
        Integer result = null;
        for (Entry<Integer, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            int index = entry.getKey();
            if (count > max) {
                max = count;
                result = index;
            }
        }
        return result;
    }

    @Override
    public int index() {
        return result;
    }
}
