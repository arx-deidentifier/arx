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
package org.deidentifier.arx.framework.data;

import java.io.Serializable;

/**
 * A modifiable column of data
 * @author Fabian Prasser
 */
public class DataColumn implements Serializable {

    /** SVUID*/
    private static final long serialVersionUID = -1232153972088248644L;

    /** The data */
    private final String[]    data;
    
    /** The attribute*/
    private final String attribute;

    /**
     * Creates a new instance
     * @param data
     * @param header
     * @param column
     * @param dictionary
     */
    public DataColumn(DataMatrix data, String[] header, int column, Dictionary dictionary) {

        // Create array
        this.data = new String[data.getNumRows()];
        this.attribute = header[column];

        // Copy data
        String[] _dictionary = dictionary.getMapping()[column];
        for (int row = 0; row < data.getNumRows(); row++) {
            this.data[row] = _dictionary[data.get(row, column)];
        }
    }
    
    /**
     * Copy constructor
     * @param column
     */
    DataColumn(DataColumn column) {
        this.data = column.data;
        this.attribute = column.attribute;
    }

    /**
     * Returns the value at the given row
     * @param row
     * @return
     */
    public String get(int row) {
        return data[row];
    }
    
    /**
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Returns the number of rows
     * @return
     */
    public int getNumRows() {
        return this.data.length;
    }
    
    /**
     * Sets the value at the given row
     * @param row
     * @param value
     */
    public void set(int row, String value) {
        this.data[row] = value;
    }
}
