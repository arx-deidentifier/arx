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
public class MultiClassZeroRClassificationResult implements ClassificationResult {

    /** Field*/
    private final Map<String, Integer> map;
    /** Field*/
    private final Integer              result;

    /**
     * Creates a new instance
     * @param result
     * @param map
     */
    MultiClassZeroRClassificationResult(Integer result, Map<String, Integer> map) {
        this.map = map;
        this.result = result;
    }

    @Override
    public double confidence() {
        return 1d;
    }

    @Override
    public double[] confidences() {
        throw new UnsupportedOperationException();
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

    @Override
    public int index() {
        return result;
    }
}
