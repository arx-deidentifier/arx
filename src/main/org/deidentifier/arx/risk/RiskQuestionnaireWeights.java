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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private Map<String, Double> weights;

    /**
     * Create a new configuration
     */
    public RiskQuestionnaireWeights() {
        weights = new HashMap<String, Double>();
    }

    /**
     * Create a new configuration from a file
     * 
     * @param filename
     */
    public RiskQuestionnaireWeights(String filename) {
        this();
        loadProperties(filename);
    }

    /**
     * Save the configuration to a new filename
     * 
     * @param filename
     */
    public void save(String filename) {
        if (filename == null) {
            // System.out.println("No file specified to save to");
            return;
        }

        Properties props = new Properties();
        for (Entry<String, Double> entry : weights.entrySet()) {
            props.setProperty(entry.getKey(), Double.toString(entry.getValue()));
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(filename);
            props.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            // System.err.println("Couldn't open file: "+filename);
            return;
        } catch (IOException e) {
            // System.err.println("Couldn't store file: "+filename);
            return;
        }
    }

    /**
     * Sets the weight for an item's identifier
     * 
     * @param identifier
     * @param weight
     */
    public void setWeightForIdentifier(String identifier, double weight) {
        weights.put(identifier, weight);
    }

    /**
     * Get the weight for an item's identifier
     * 
     * @param identifier
     * @return the weight
     */
    public double weightForIdentifier(String identifier) {
        if (weights.containsKey(identifier) == false) { return 1.0; }
        return weights.get(identifier);
    }

    /**
     * Try to load the weights from the specified filename
     * 
     * @param filename
     */
    private void loadProperties(String filename) {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream(filename);
            props.load(in);

            for (Entry<Object, Object> entry : props.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                double weight = Double.parseDouble(value);
                weights.put(key, weight);
            }
            in.close();
        } catch (FileNotFoundException e) {
            // System.err.println("Couldn't open file: "+filename);
            return;
        } catch (IOException e) {
            // System.err.println("Couldn't parse file: "+filename);
            return;
        }

    }
}
