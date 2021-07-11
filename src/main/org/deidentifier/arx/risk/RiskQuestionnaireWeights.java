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

package org.deidentifier.arx.risk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * The weight configuration used when evaluating the checklist
 * 
 */
public class RiskQuestionnaireWeights implements Serializable {

    /** SVUID */
    private static final long   serialVersionUID = -7601091333545929290L;

    /** Map, mapping the item identifiers to a weight */
    private Map<String, Double> weights = new HashMap<String, Double>();

    /**
     * Returns properties
     * 
     */
    public Properties asProperties() {
        Properties props = new Properties();
        for (Entry<String, Double> entry : weights.entrySet()) {
            props.setProperty(entry.getKey(), Double.toString(entry.getValue()));
        }
        return props;
    }

    /**
     * Get the weight for an item's identifier
     * 
     * @param identifier
     * @return the weight
     */
    public double getWeight(String identifier) {
        if (weights.containsKey(identifier) == false) { return 1.0; }
        return weights.get(identifier);
    }

    /**
     * Load weights
     * @param properties
     */
    public void loadFromProperties(Properties properties) {
        try {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                double weight = Double.parseDouble(value);
                weights.put(key, weight);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid properties");
        }
    }

    /**
     * Sets the weight for an item's identifier
     * 
     * @param identifier
     * @param weight
     */
    public void setWeight(String identifier, double weight) {
        weights.put(identifier, weight);
    }
}
