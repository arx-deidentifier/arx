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

package org.deidentifier.arx.gui.worker.io;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataHandleOutput;
import org.deidentifier.arx.DataType;

import com.carrotsearch.hppc.IntArrayList;

/**
 * A model for the result of a local recoding
 *
 * @author Fabian Prasser
 */
public class LocalRecodingData implements Serializable {

    /** SVUID*/
    private static final long serialVersionUID = 1064292779963693831L;

    /** The data types that resulted from the local recoding step */
    private final Map<String, DataType<?>> dataTypes        = new HashMap<String, DataType<?>>();

    /** Outliers */
    private final int[]                    outliers;

    /**
     * Creates a new instance for the given handle
     * @param output
     */
    public LocalRecodingData(DataHandleOutput output) {
        
        // Create the map
        for (int i=0; i<output.getNumColumns(); i++) {
            String attribute = output.getAttributeName(i);
            this.dataTypes.put(attribute, output.getDataType(attribute));
        }
        
        // List
        IntArrayList list = new IntArrayList();
        for (int i=0; i<output.getNumRows(); i++) {
            if (output.isOutlier(i)) {
                list.add(i);
            }
        }
        this.outliers = list.toArray();
        
        System.out.println(dataTypes);
    }
    
    /**
     * Returns a map containing data types
     * @return
     */
    public final Map<String, DataType<?>> getDataTypes() {
        return this.dataTypes;
    }
    
    /**
     * Outliers
     * @return
     */
    public final int[] getOutliers() {
        return this.outliers;
    }
}
