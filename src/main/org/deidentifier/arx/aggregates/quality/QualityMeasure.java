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

package org.deidentifier.arx.aggregates.quality;

/**
 * Base class for quality measures. Results are reported in range [0, 1].
 * Higher is better.
 * 
 * @author Fabian rasser
 */
public abstract class QualityMeasure {
    
    /** Row-oriented model*/
    private final boolean rowOriented;
    
    /**
     * Creates a new instance
     * @param rowOriented
     */
    public QualityMeasure(boolean rowOriented) {
        this.rowOriented = rowOriented;
    }

    /**
     * Returns whether this measure is available
     * @return
     */
    public abstract boolean isAvailable();
    
    /**
     * Returns whether this measure is available for the given column
     * @param column
     * @return
     */
    public abstract boolean isAvailable(String column);
    
    /**
     * Returns whether this is a column-oriented measure
     * @return
     */
    public boolean isColumnOriented() {
        return !this.rowOriented;
    }

    /**
     * Returns whether this is a row-oriented measure
     * @return
     */
    public boolean isRowOriented() {
        return this.rowOriented;
    }
}
