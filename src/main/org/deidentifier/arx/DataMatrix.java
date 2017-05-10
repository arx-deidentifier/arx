/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager.AttributeTypeInternal;
import org.deidentifier.arx.framework.data.Dictionary;

/**
 * A simple data matrix that can be constructed from input data, output data and projected data
 * 
 * @author Fabian Prasser
 */
public class DataMatrix {

    /** The actual matrix */
    private final int[][] matrix;
    /** Num rows */
    private final int     rows;
    /** Num columns */
    private final int     columns;

    /**
     * Creates a data matrix for output data
     * @param inverseMap
     * @param inverseData
     * @param inverseDictionaries
     * @param outputGeneralized
     * @param suppressedAttributeTypes
     * @param columns
     * @param rows Can be null
     */
    DataMatrix(int[] inverseMap,
               int[][][] inverseData,
               Dictionary[] inverseDictionaries,
               Data outputGeneralized,
               int suppressedAttributeTypes,
               int[] columns,
               int[] rows) {

        // Prepare
        this.columns = columns.length;

        // Extract
        List<int[]> list = new ArrayList<>();
        if (rows != null) {
            for (int row : rows) {
                int[] record = getRow(inverseMap,
                                      inverseData,
                                      inverseDictionaries,
                                      outputGeneralized,
                                      suppressedAttributeTypes,
                                      row,
                                      columns);
                if (record != null) {
                    list.add(record);
                }
            }
        } else {
            for (int row = 0; row < outputGeneralized.getDataLength(); row++) {
                int[] record = getRow(inverseMap,
                                      inverseData,
                                      inverseDictionaries,
                                      outputGeneralized,
                                      suppressedAttributeTypes,
                                      row,
                                      columns);
                if (record != null) {
                    list.add(record);
                }
            }
        }
        
        // Store
        this.matrix = new int[list.size()][];
        for (int i=0; i<matrix.length; i++) {
            matrix[i] = list.get(i);
        }
        this.rows = this.matrix.length;
    }

    /**
     * Creates a data matrix for input data
     * @param data
     * @param columns
     * @param rows Can be null
     */
    DataMatrix(int[][] data, int[] columns, int[] rows) {
        
        // Prepare
        this.columns = columns.length;
        this.rows = rows != null ? rows.length : data.length;
        this.matrix = new int[this.rows][];
        
        // Extract
        if (rows != null) {
            int index = 0;
            for (int row : rows) {
                this.matrix[index++] = getRow(data[row], columns);
            }
        } else {
            for (int row = 0; row < data.length; row++) {
                this.matrix[row] = getRow(data[row], columns);
            }
        }
    }

    /**
     * @return the columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return the matrix
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Extracts a subset of the columns
     * @param data
     * @param columns
     * @return
     */
    private int[] getRow(int[] data, int[] columns) {
        int[] result = new int[columns.length];
        int index = 0;
        for (int column : columns) {
            result[index++] = data[column];
        }
        return result;
    }

    /**
     * Returns a row for the specified columns
     * @param inverseMap
     * @param inverseData
     * @param inverseDictionaries
     * @param outputGeneralized
     * @param suppressedAttributeTypes
     * @param row
     * @param columns
     * @return
     */
    private int[] getRow(int[] inverseMap,
                         int[][][] inverseData,
                         Dictionary[] inverseDictionaries,
                         Data outputGeneralized,
                         int suppressedAttributeTypes,
                         int row,
                         int[] columns) {
        
        // We ignore suppressed records
        if ((outputGeneralized.getArray()[row][0] & Data.OUTLIER_MASK) != 0) {
            return null;
        }
        
        // Extract data
        int[] result = new int[columns.length];
        int columnindex = 0;
        for (int col : columns) {
            
            // See: DataHandleOutput.internalGetValue()
            int key = col * 2;
            int type = inverseMap[key];
            final int index = inverseMap[key + 1];
            final int[][] data = inverseData[type];
            
            // Now find the value
            result[columnindex++] = (type == AttributeTypeInternal.IDENTIFYING) ?
                                    // We set the value to #distinct+1 for suppressed values
                                    inverseDictionaries[type].getMapping()[index].length :
                                    // To the actual value, otherwise    
                                    data[row][index] & Data.REMOVE_OUTLIER_MASK;
        }
        return result;
    }
}
