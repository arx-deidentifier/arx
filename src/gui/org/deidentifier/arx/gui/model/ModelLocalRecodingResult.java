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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataHandleOutput;
import org.deidentifier.arx.DataType;

/**
 * A model for the result of a local recoding
 *
 * @author Fabian Prasser
 */
public class ModelLocalRecodingResult implements Serializable {

    /** SVUID */
    private static final long              serialVersionUID = -2447897054772331576L;

    /** The data types that resulted from the local recoding step */
    private final Map<String, DataType<?>> dataTypes        = new HashMap<String, DataType<?>>();

    /**
     * Creates a new instance for the given handle
     * @param output
     */
    public ModelLocalRecodingResult(DataHandleOutput output) {
        
        // Create the map
        for (int i=0; i<output.getNumColumns(); i++) {
            String attribute = output.getAttributeName(i);
            this.dataTypes.put(attribute, output.getDataType(attribute));
        }
    }
    
    /**
     * Returns the data type as created from local recoding
     * @param attribute
     * @return
     */
    public final DataType<?> getDataType(String attribute) {
        return this.dataTypes.get(attribute);
    }
}
