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

import java.text.ParseException;
import java.util.Date;

import org.deidentifier.arx.aggregates.StatisticsBuilder;

/**
 * Wrapper class that provides information to StatisticsBuilder.
 *
 * @author Fabian Prasser
 */
public class DataHandleInternal {

    /** Interface */
    public static interface InterruptHandler {
        
        /** Method*/
        public void checkInterrupt();
    }

    /** Handle */
    private DataHandle handle;

    /**
     * Constructor
     * @param handle
     * @param anonymous
     */
    protected DataHandleInternal(DataHandle handle){
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
     * Delegate
     * @param attribute
     * @return
     */
    public int getColumnIndexOf(String attribute) {
        return handle.getColumnIndexOf(attribute);
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
     * Delegate
     * @param row
     * @param column
     * @return
     * @throws ParseException
     */
    public Date getDate(int row, int column) throws ParseException {
        return handle.getDate(row, column);
    }

    /**
     * Method
     * @return
     */
    public DataDefinition getDefinition() {
        return handle.getDefinition();
    }

    /**
     * Delegate
     * @param column
     * @return
     */
    public String[] getDistinctValues(int column) {
        return handle.getDistinctValues(column);
    }

    /**
     * Method
     * @param column
     * @param ignoreSuppression
     * @param stop
     * @return
     */
    public String[] getDistinctValues(int column, boolean ignoreSuppression, InterruptHandler stop) {
        return handle.getDistinctValues(column, ignoreSuppression, stop);
    }

    /**
     * Method
     * @param column
     * @param stop
     * @return
     */
    public String[] getDistinctValues(int column, InterruptHandler stop) {
        return handle.getDistinctValues(column, false, stop);
    }
    
    /**
     * Delegate
     * @param row
     * @param column
     * @return
     * @throws ParseException
     */
    public Double getDouble(int row, int column) throws ParseException {
        return handle.getDouble(row, column);
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
     * Delegate
     * @param row
     * @param column
     * @return
     * @throws ParseException
     */
    public Long getLong(int row, int column) throws ParseException {
        return handle.getLong(row, column);
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

    public StatisticsBuilder getStatisticsBuilder() {
        return this.handle.getStatistics();
    }

    /**
     * Returns the superset, if this handle is a subset
     * @return
     */
    public DataHandleInternal getSuperset() {
        if (!(handle instanceof DataHandleSubset)) {
            return null;
        } else {
            return new DataHandleInternal(((DataHandleSubset)handle).getSource());
        }
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
     * Gets the value
     */
    public String getValue(final int row, final int col, final boolean ignoreSuppression) {
        return handle.internalGetValue(row, col, ignoreSuppression);
    }

    /**
     * Returns the view
     * @return
     */
    public DataHandleInternal getView() {
        return new DataHandleInternal(handle.getView());
    }
    
    /**
     * Returns whether the handle is anonymous
     * @return
     */
    public boolean isAnonymous() {
        return handle.isAnonymous();
    }
    
    /**
     * Returns whether the handle is optimized
     * @return
     */
    public boolean isOptimized() {
        return handle.isOptimized();
    }

    /**
     * Returns whether the given row is suppressed
     * @param row
     * @return
     */
    public boolean isOutlier(int row) {
        return handle.isOutlier(row);
    }

    /**
     * Returns whether this is an output handle
     * @return
     */
    public boolean isOutput() {
        if (this.getSuperset() != null) {
            return this.getSuperset().isOutput();
        } else {
            return this.handle instanceof DataHandleOutput;
        }
    }

    /**
     * Returns the associated input handle, itself if there is none.
     * @return
     */
    public DataHandleInternal getAssociatedInput() {
        
        // Check if input already
        if (!this.isOutput()) {
            return this;
        }
        
        // Obtain
        DataHandle input = this.handle.registry.getInputHandle();
        
        // Map to subset
        if (this.getSuperset() != null) {
            input = input.getView();
        }
        
        // Return
        return new DataHandleInternal(input);
    }
}
