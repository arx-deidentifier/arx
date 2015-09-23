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

package org.deidentifier.arx.risk.hipaa;

import org.deidentifier.arx.risk.hipaa.Match.HIPAAIdentifier;

/**
 * Encapsulates validation logic for column headers and pattern matching
 * @author David Gaﬂmann
 */
public class MatcherConfig {
    private ValueMatcher    pattern;
    private HIPAAIdentifier category;
    private HeaderMatcher[] labels;
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    public MatcherConfig(HIPAAIdentifier category, HeaderMatcher label) {
        this(category, label, null);
    }
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    public MatcherConfig(HIPAAIdentifier category, HeaderMatcher label, ValueMatcher pattern) {
        this(category, new HeaderMatcher[] { label }, pattern);
    }
    
    /**
     * Constructor.
     * @param category
     * @param label
     */
    public MatcherConfig(HIPAAIdentifier category, HeaderMatcher[] labels) {
        this(category, labels, null);
    }
    
    /**
     * Creates a new matcher configuration
     * @param category The identifier this attribute belongs to
     * @param labels An array of labels associated which an attribute
     * @param pattern A pattern which is used to check the row contents
     */
    public MatcherConfig(HIPAAIdentifier category, HeaderMatcher[] labels, ValueMatcher pattern) {
        this.category = category;
        this.labels = labels;
        this.pattern = pattern;
    }
    
    /**
     * @return Category which is associated to this label
     */
    public HIPAAIdentifier getCategory() {
        return category;
    }
    
    /**
     * @return True if attribute has a pattern
     */
    public boolean hasPattern() {
        return pattern != null;
    }
    
    /**
     * Returns if the value matches the header matcher
     * 
     * @param value The column name
     * @return True if input matches label
     */
    public boolean matchesLabel(String value) {
        for (HeaderMatcher label : labels) {
            if (label.matches(value)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns if the value matches the pattern matcher.
     * 
     * @param value
     * @return
     */
    public boolean matchesPattern(String value) {
        if (!hasPattern()) {
            return false;
        } else {
            return pattern.matches(value);
        }
    }
}
