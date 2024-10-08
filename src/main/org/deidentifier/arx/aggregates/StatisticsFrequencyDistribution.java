/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2024 Fabian Prasser and contributors
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A frequency distribution.
 *
 * @author Fabian Prasser
 */
public class StatisticsFrequencyDistribution {

    /** The data values, sorted. */
    public final String[] values;
    
    /** The corresponding frequencies. */
    public final double[] frequency;
    
    /** The total number of data values. */
    public final int      count;

    /**
     * Internal constructor.
     *
     * @param items
     * @param frequency
     * @param count
     */
    StatisticsFrequencyDistribution(String[] items, double[] frequency, int count) {
        this.values = items;
        this.count = count;
        this.frequency = frequency;
    }
    
    /**
     * Returns the distribution as a map
     * @return
     */
    public Map<String, Double> asMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < frequency.length; i++) {
            map.put(values[i], frequency[i]);
        }
        return map;
    }
}