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
package org.deidentifier.arx.metric.v2;

import java.io.Serializable;

/**
 * A class encapsulating information about data quality
 * 
 * @author Fabian Prasser
 */
public class QualityMetadata<T> implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 8750896039746232218L;

    /** Parameter */
    private final String      parameter;
    
    /** Value */
    private final T           value;

    /**
     * Creates a new instance
     * @param parameter
     * @param value
     */
    QualityMetadata(String parameter, T value) {
        this.parameter = parameter;
        this.value = value;
    }
    
    /**
     * @return the parameter
     */
    public String getParameter() {
        return parameter;
    }
    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }
}
