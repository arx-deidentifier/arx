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

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataMatrix;

/**
 * A simple data array that can be constructed from input data, output data and projected data
 * TODO: Should probably be replaced by DataMatrix
 * 
 * @author Fabian Prasser
 */
public class DataArray {

    /** The actual array */
    private final int[][] array;
    /** Num rows */
    private final int     rows;
    /** Num columns */
    private final int     columns;

    /**
     * To build an array from a groupify
     * @param entries
     */
    public DataArray(List<HashGroupifyEntry> entries) {
        
        int[][] array = new int[entries.size()][];
        for (int i=0; i<entries.size(); i++) {
            HashGroupifyEntry entry = entries.get(i);
            int[] row = new int[entry.columns()];
            entry.read();
            for (int j=0; j<row.length; j++) {
                row[j] = entry.next();
            }
            this.array[i] = row;
        }
        if (array == null || array.length == 0) {
            this.rows = 0;
            this.columns = 0;
            this.array = new int[0][0];
        } else {
            this.rows = array.length;
            this.columns = array[0].length;
            this.array = array;
        }
    }

    /**
     * Creates a data array for output data
     * @param columnToData
     * @param columnToIndex
     * @param columnToSuppressionStatus
     * @param dataGeneralized
     * @param columns
     * @param rows
     */
    DataArray(Data[] columnToData,
              int[] columnToIndex,
              boolean[] columnToSuppressionStatus,
              Data dataGeneralized,
              int[] columns,
              int[] rows) {
        
       
        // Prepare
        this.columns = columns.length;

        // Extract
        List<int[]> list = new ArrayList<>();
        if (rows != null) {
            for (int row : rows) {
                int[] record = getRow(columnToData,
                                      columnToIndex,
                                      columnToSuppressionStatus,
                                      dataGeneralized,
                                      row,
                                      columns);
                if (record != null) {
                    list.add(record);
                }
            }
        } else {
            for (int row = 0; row < dataGeneralized.getDataLength(); row++) {
                int[] record = getRow(columnToData,
                                      columnToIndex,
                                      columnToSuppressionStatus,
                                      dataGeneralized,
                                      row,
                                      columns);
                if (record != null) {
                    list.add(record);
                }
            }
        }
        
        // Store
        this.array = new int[list.size()][];
        for (int i=0; i<array.length; i++) {
            array[i] = list.get(i);
        }
        this.rows = this.array.length;
    }

    /**
     * Creates a data array for input data
     * @param data
     * @param columns
     * @param rows Can be null
     */
    DataArray(DataMatrix data, int[] columns, int[] rows) {
        
        // Prepare
        this.columns = columns.length;
        this.rows = rows != null ? rows.length : data.getNumRows();
        this.array = new int[this.rows][];
        
        // Extract
        if (rows != null) {
            int index = 0;
            for (int row : rows) {
                this.array[index++] = getRow(data, row, columns);
            }
        } else {
            for (int row = 0; row < data.getNumRows(); row++) {
                this.array[row] = getRow(data, row, columns);
            }
        }
    }

    /**
     * @return the array
     */
    public int[][] getArray() {
        return array;
    }

    /**
     * @return the columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns a row for the specified columns
     * @param columnToData
     * @param columnToIndex
     * @param columnToSuppressionStatus
     * @param dataGeneralized
     * @param row
     * @param columns
     * @return
     */
    private int[] getRow(Data[] columnToData,
                         int[] columnToIndex,
                         boolean[] columnToSuppressionStatus,
                         Data dataGeneralized,
                         int row,
                         int[] columns) {

        // We ignore suppressed records
        if ((dataGeneralized.getArray().get(row, 0) & Data.OUTLIER_MASK) !=0) {
            return null;
        }
        
        // Extract data
        int[] result = new int[columns.length];
        int columnindex = 0;
        for (int col : columns) {
            
            // See: DataHandleOutput.internalGetValue()
            Data data = columnToData[col];
            int index = columnToIndex[col];
            
            // Handle identifying values
            if (data == null) {
                
                // TODO: Correct? Was: 
                // // We set the value to #distinct+1 for suppressed values
                // inverseDictionaries[type].getMapping()[index].length :
                result[columnindex++] = 0; 
                
            } else {
                
                // Decode
                result[columnindex++] = data.getArray().get(row, index) & Data.REMOVE_OUTLIER_MASK;
            }
        }
        return result;
    }

    /**
     * Extracts a subset of the columns
     * @param data
     * @param row
     * @param columns
     * @return
     */
    private int[] getRow(DataMatrix data, int row, int[] columns) {
        int[] result = new int[columns.length];
        int index = 0;
        data.setRow(row);
        for (int column : columns) {
            result[index++] = data.getValueAtColumn(column);
        }
        return result;
    }
}
