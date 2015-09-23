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

package org.deidentifier.arx.risk;

import org.deidentifier.arx.risk.HIPAAIdentifierMatch.HIPAAIdentifier;

/**
 * Encapsulates validation logic for column headers and pattern matching
 * @author David Gaﬂmann
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
class HIPAAIdentifierConfig {
    
    /** TODO*/
    private HIPAAMatcherAttributeValue  matcherValue;
    /** TODO*/
    private HIPAAIdentifier             identifier;
    /** TODO*/
    private HIPAAMatcherAttributeName[] matchersName;
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    HIPAAIdentifierConfig(HIPAAIdentifier category, HIPAAMatcherAttributeName label) {
        this(category, label, null);
    }
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    HIPAAIdentifierConfig(HIPAAIdentifier category, HIPAAMatcherAttributeName label, HIPAAMatcherAttributeValue pattern) {
        this(category, new HIPAAMatcherAttributeName[] { label }, pattern);
    }
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    HIPAAIdentifierConfig(HIPAAIdentifier category, HIPAAMatcherAttributeName[] labels) {
        this(category, labels, null);
    }
    
    /**
     * Creates a new matcher configuration
     * @param category The identifier this attribute belongs to
     * @param labels An array of labels associated which an attribute
     * @param pattern A pattern which is used to check the row contents
     */
    HIPAAIdentifierConfig(HIPAAIdentifier category, HIPAAMatcherAttributeName[] labels, HIPAAMatcherAttributeValue pattern) {
        this.identifier = category;
        this.matchersName = labels;
        this.matcherValue = pattern;
    }
    
    /**
     * @return Category which is associated to this label
     */
    HIPAAIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * @return True if attribute has a pattern
     */
    boolean hasValueMatcher() {
        return matcherValue != null;
    }
    
    /**
     * Returns if the attribute name matches
     * 
     * @param name The column name
     * @return True if input matches label
     */
    boolean matchesAttributeName(String name) {
        for (HIPAAMatcherAttributeName label : matchersName) {
            if (label.matches(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns if the value matches
     * 
     * @param value
     * @return
     */
    boolean matchesAttributeValue(String value) {
        if (!hasValueMatcher()) {
            return false;
        } else {
            return matcherValue.matches(value);
        }
    }
}
