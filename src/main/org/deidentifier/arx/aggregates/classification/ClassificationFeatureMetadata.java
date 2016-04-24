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

import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;

/**
 * Metadata about a single feature
 * @author Fabian Prasser
 *
 */
public class ClassificationFeatureMetadata {

    /** Attribute */
    private final String      attribute;
    /** Maximum */
    private double            maximum = -Double.MAX_VALUE;
    /** Minimum */
    private double            minimum = Double.MAX_VALUE;
    /** Data type */
    private final DataType<?> type;

    /**
     * Creates a new instance
     * @param attribute
     * @param type
     */
    public ClassificationFeatureMetadata(String attribute, DataType<?> type) {
        this.attribute = attribute;
        this.type = type;
    }

    /**
     * Returns the name of the feature
     * @return
     */
    public String getName() {
        return this.attribute;
    }
    
    /**
     * Returns whether this is a numeric attribute
     * @return
     */
    public boolean isNumeric() {
        return minimum != Double.MAX_VALUE || maximum != - Double.MAX_VALUE;
    }
    
    /**
     * Updates minimum and maximum for feature scaling
     * @param value
     */
    protected void updateMinMax(String value) {

        // Convert
        double numericValue = Double.NaN;
        try {
            if (type instanceof ARXDecimal) {
                numericValue = 0d;
                numericValue = (Double) type.parse(value);
            } else if (type instanceof ARXInteger) {
                numericValue = 0d;
                numericValue = (Long) type.parse(value);
            } else if (type instanceof ARXDate) {
                numericValue = 0d;
                numericValue = ((Date) type.parse(value)).getTime();
            }
        } catch (Exception e) {
            // Ignore: this is for the handling of suppressed values
        }

        // Trace
        if (!Double.isNaN(numericValue)) {
            minimum = Math.min(minimum, numericValue);
            maximum = Math.max(maximum, numericValue);
        }
    }
}
