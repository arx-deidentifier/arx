/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.risk.resources.us.HIPAAConstantsUS;

/**
 * Utility class providing access to important constants for finding HIPAA identifiers.
 * 
 * @author Fabian Prasser
 */
public abstract class HIPAAConstants {
    
    /** US data*/
    private static final HIPAAConstants dataUS = new HIPAAConstantsUS();
    
    /**
     * Returns constants for the US
     * @return
     */
    public static HIPAAConstants getUSData() {
        return dataUS;
    }

    /** Cities */
    private Set<String>                       cities     = null;
    /** First names */
    private Set<String>                       firstnames = null;
    /** Last names */
    private Set<String>                       lastnames  = null;
    /** States */
    private Set<String>                       states     = null;
    /** Zip codes */
    private Set<String>                       zipcodes   = null;
    /** Labels */
    private Map<String, Map<String, Integer>> labels     = null;
                                                         
    /** Default charset */
    private static final Charset              CHARSET    = StandardCharsets.UTF_8;

    /**
     * Returns all matchers for the given category
     * @param category
     * @return
     */
    public List<HIPAAMatcherAttributeName> getNameMatchers(String category) {

        // Check
        if (!getNameConfigurations().containsKey(category)) {
            return new ArrayList<HIPAAMatcherAttributeName>(); 
        }
        
        // Collect each matcher
        List<HIPAAMatcherAttributeName> result = new ArrayList<HIPAAMatcherAttributeName>();
        for (Entry<String, Integer> entry2 : getNameConfigurations().get(category).entrySet()) {
            result.add(new HIPAAMatcherAttributeName(entry2.getKey(), entry2.getValue()));
        }
        
        // Return
        return result;
    }
    
    /** 
     * Cities
     * @param value
     * @return
     */
    public boolean isCity(String value) {
        return getCities().contains(value);
    }
    
    /** 
     * First names
     * @param value
     * @return
     */
    public boolean isFirstname(String value) {
        return getFirstnames().contains(value);
    }
    
    /** 
     * Last names
     * 
     * @param value
     * @return
     */
    public boolean isLastname(String value) {
        return getLastnames().contains(value);
    }
    
    /** 
     * States
     * 
     * @param value
     * @return
     */
    public boolean isState(String value) {
        return getStates().contains(value);
    }

    /** 
     * Zip codes
     * 
     * @param value
     * @return
     */
    public boolean isZipcode(String value) {
        return getZipcodes().contains(value);
    }
    
    /** Cities */
    private Set<String> getCities() {
        if (cities == null) {
            cities = load("cities.csv");
        }
        return cities;
    }
    
    /** First names */
    private Set<String> getFirstnames() {
        if (firstnames == null) {
            firstnames = load("firstnames.csv");
        }
        return firstnames;
    }
    
    /** Last names */
    private Set<String> getLastnames() {
        if (lastnames == null) {
            lastnames = load("lastnames.csv");
        }
        return lastnames;
    }
    
    /**
     * Returns all name configurations
     * @return
     */
    private Map<String, Map<String, Integer>> getNameConfigurations() {
        
        if (this.labels == null) {
                
            InputStream stream = getInputStream("labels.properties");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, CHARSET));
            this.labels = new HashMap<String, Map<String, Integer>>();
    
            try {
                String line = br.readLine();
                while (line != null) {
    
                    String[] parts = line.split("=");
                    String label = parts[0];
                    parts = parts[1].split(",");
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    for (int i = 0; i < parts.length; i += 2) {
                        map.put(parts[i], Integer.valueOf(parts[i+1]));
                    }
                    labels.put(label, map);
                    line = br.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return this.labels;
    }

    /** States */
    private Set<String> getStates() {
        if (states == null) {
            states = load("states.csv");
        }
        return states;
    }

    /** Zip codes */
    private Set<String> getZipcodes() {
        if (zipcodes == null) {
            zipcodes = load("zipcodes.csv");
        }
        return zipcodes;
    }

    /**
     * Loads the given set of resources
     * @param file
     * @return
     */
    private Set<String> load(String file) {
        InputStream stream = getInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, CHARSET));
        Set<String> set = new HashSet<String>();
        try {
            String line = br.readLine();
            while (line != null) {
                set.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return set;
    }
    
    /**
     * Implement this to load the according file
     * @param file
     * @return
     */
    protected abstract InputStream getInputStream(String file);
}
