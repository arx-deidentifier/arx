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

import org.deidentifier.arx.DataType;

/**
 * Basic configuration for quality models
 * 
 * @author Fabian Prasser
 */
public class QualityConfiguration {
    
    /** Value*/
    private String suppressedValue = DataType.ANY_VALUE;

    /**
     * @return the suppressedValue
     */
    public String getSuppressedValue() {
        return suppressedValue;
    }

    /**
     * @param suppressedValue the suppressedValue to set
     */
    public void setSuppressedValue(String suppressedValue) {
        this.suppressedValue = suppressedValue;
    }
}
