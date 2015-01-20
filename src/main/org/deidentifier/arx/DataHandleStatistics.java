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
 * Stupid wrapper class that provides information to StatisticsBuilder.
 *
 * @author Fabian Prasser
 */
public class DataHandleStatistics {

    /**
     * 
     */
    public static interface InterruptHandler {
        
        /**
         * 
         */
        public void checkInterrupt();
    }
    
    /**  TODO */
    private DataHandle handle;
    
    /**
     * 
     *
     * @param handle
     */
    protected DataHandleStatistics(DataHandle handle){
        this.handle = handle;
    }

    /**
     * 
     *
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
     * 
     *
     * @param attribute
     * @return
     */
    public DataType<?> getDataType(String attribute) {
        return handle.getDataType(attribute);
    }

    /**
     * 
     *
     * @return
     */
    public DataDefinition getDefinition() {
        return handle.getDefinition();
    }

    /**
     * 
     *
     * @param column
     * @param stop
     * @return
     */
    public String[] getDistinctValues(int column, InterruptHandler stop) {
        return handle.getDistinctValues(column, stop);
    }

    /**
     * 
     *
     * @param attribute
     * @return
     */
    public int getGeneralization(String attribute) {
        return handle.getGeneralization(attribute);
    }

    /**
     * 
     *
     * @return
     */
    public int getNumRows() {   
        return handle.getNumRows();
    }

    /**
     * 
     *
     * @return
     */
    public String getSuppressionString() {
        return handle.getSuppressionString();
    }

    /**
     * 
     *
     * @param row
     * @param column
     * @return
     */
    public String getValue(int row, int column) {
        return handle.getValue(row, column);
    }
}
