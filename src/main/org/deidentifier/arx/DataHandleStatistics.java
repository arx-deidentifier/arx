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

package org.deidentifier.arx;

/**
 * Wrapper class that provides information to StatisticsBuilder.
 *
 * @author Fabian Prasser
 */
public class DataHandleStatistics {

    /** Interface */
    public static interface InterruptHandler {
        
        /** Method*/
        public void checkInterrupt();
    }
    
    /**  Handle */
    private DataHandle handle;
    
    /**
     * Constructor
     * @param handle
     */
    protected DataHandleStatistics(DataHandle handle){
        this.handle = handle;
    }

    /**
     * Method
     * @param column
     * @return
     */
    public String getAttributeName(int column) {
        return handle.getAttributeName(column);
    }

    /**
     * 
     *
     * @param attribute
     * @return
     */
    public DataType<?> getBaseDataType(String attribute) {
        return handle.getBaseDataType(attribute);
    }

    /**
     * Method
     * @param attribute
     * @return
     */
    public DataType<?> getDataType(String attribute) {
        return handle.getDataType(attribute);
    }

    /**
     * Method
     * @return
     */
    public DataDefinition getDefinition() {
        return handle.getDefinition();
    }

    /**
     * Method
     * @param column
     * @param stop
     * @return
     */
    public String[] getDistinctValues(int column, InterruptHandler stop) {
        return handle.getDistinctValues(column, stop);
    }

    /**
     * Method
     * @param attribute
     * @return
     */
    public int getGeneralization(String attribute) {
        return handle.getGeneralization(attribute);
    }

    /**
     * Method
     * @return
     */
    public int getNumColumns() {
        return handle.getNumColumns();
    }

    /**
     * Method
     * @return
     */
    public int getNumRows() {   
        return handle.getNumRows();
    }

    /**
     * Method
     * @return
     */
    public String getSuppressionString() {
        return handle.getSuppressionString();
    }

    /**
     * Method
     * @param row
     * @param column
     * @return
     */
    public String getValue(int row, int column) {
        return handle.getValue(row, column);
    }
    
    /**
     * Returns whether the given row is suppressed
     * @param row
     * @return
     */
    public boolean isSuppressed(int row) {
        return handle.isOutlier(row);
    }
}
