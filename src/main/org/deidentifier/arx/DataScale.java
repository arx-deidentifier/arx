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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * This class represents different scales of measure. Note that the order of entries in this enum is important.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public enum DataScale implements Serializable {
    
    NOMINAL("Nominal scale"),
    
    ORDINAL("Ordinal scale"),
    
    INTERVAL("Interval scale"),
    
    RATIO("Ratio scale");
    
    /** Label */
    private final String label;
    
    /**
     * Constructor
     * @param label
     */
    private DataScale(String label) {
        this.label = label;
    }
    
    /**
     * Returns whether this scale provides at least the properties of the given scale.
     * @param other
     * @return
     */
    public boolean provides(DataScale other) {
        return this.compareTo(other) >= 0;
    }
    
    @Override
    public String toString() {
        return label;
    }
}