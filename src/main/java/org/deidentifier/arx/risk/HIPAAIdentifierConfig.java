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

import java.util.List;

import org.deidentifier.arx.risk.HIPAAIdentifierMatch.HIPAAIdentifier;

/**
 * Encapsulates validation logic for column headers and pattern matching
 * @author David Gassmann
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class HIPAAIdentifierConfig {

    /** TODO */
    private HIPAAMatcherAttributeValue      matcherValue;
    /** TODO */
    private HIPAAIdentifier                 identifier;
    /** TODO */
    private List<HIPAAMatcherAttributeName> matchersName;
    /** TODO */
    private String                          instance;
    
    /**
     * Creates a new matcher configuration
     * @param identifier The identifier this attribute belongs to
     * @param instance The specific instance of the identifier
     * @param labels An array of labels associated which an attribute
     */
    HIPAAIdentifierConfig(HIPAAIdentifier identifier, String instance, HIPAAMatcherAttributeValue pattern) {
        this.identifier = identifier;
        this.instance = instance;
        this.matchersName = null;
        this.matcherValue = pattern;
    }
    
    /**
     * Constructor.
     * @param identifier
     * @param instance
     * @param label
     */
    HIPAAIdentifierConfig(HIPAAIdentifier identifier, String instance, List<HIPAAMatcherAttributeName> labels) {
        this.identifier = identifier;
        this.instance = instance;
        this.matchersName = labels;
        this.matcherValue = null;
    }
    
    /**
     * Returns the instance
     * @return
     */
    public String getInstance() {
        return this.instance;
    }
    
    /**
     * @return Category which is associated to this label
     */
    HIPAAIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns if the attribute name matches
     * 
     * @param name The column name
     * @return The value if any, null otherwise
     */
    String getMatchingAttributeName(String name) {
        if (matchersName == null) {
            return null;
        }
        for (HIPAAMatcherAttributeName label : matchersName) {
            if (label.matches(name)) {
                return label.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Returns the value itself if it matches
     * 
     * @param value
     * @return
     */
    String getMatchingAttributeValue(String value) {
        if (matcherValue != null && matcherValue.matches(value)) {
            return value;
        } else {
            return null;
        }
    }
}
