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

package org.deidentifier.arx;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;

/**
 * An implementation of the class DataHandle for output data.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataHandleOutput extends DataHandle {
    
    /**
     * The class ResultIterator.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    class ResultIterator implements Iterator<String[]> {
        
        /** The current row. */
        private int row = -1;
        
        @Override
        public boolean hasNext() {
            return row < dataGeneralized.getArray().getNumRows();
        }
        
        @Override
        public String[] next() {
            
            String[] result = null;
            
            /* write header */
            if (row == -1) {
                result = header;
                
                /* write a normal row */
            } else {
                
                // Create row
                result = new String[header.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = internalGetValue(row, i, false);
                }
            }
            
            row++;
            return result;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** A specific slice of data */
    private Data          dataInput;

    /** A specific slice of data */
    private Data          dataGeneralized;

    /** A specific slice of data */
    private Data          dataAggregated;

    /** Column to data */
    private Data[]        columnToData;

    /** Column to index */
    private int[]         columnToIndex;

    /** Column to suppression status */
    private boolean[]     columnToSuppressionStatus;

    /** The current result. */
    private ARXResult     result;

    /** Flag determining whether this buffer has been optimized */
    private boolean       optimized = false;

    /** Flag determining whether this buffer is anonymous */
    private boolean       anonymous = false;

    /**
     * Instantiates a new handle.
     * 
     * @param result
     * @param registry
     * @param manager
     * @param outputGeneralized
     * @param outputMicroaggregated
     * @param node
     * @param definition
     * @param config
     */
    protected DataHandleOutput(final ARXResult result,
                               final DataRegistry registry,
                               final DataManager manager,
                               final Data outputGeneralized,
                               final Data outputMicroaggregated,
                               final ARXNode node,
                               final DataDefinition definition,
                               final ARXConfiguration config) {
        
        // Initialize
        this.initialize(result, registry, manager, outputGeneralized, outputMicroaggregated, node, definition, config);

        // Obtain data types
        this.columnToDataType = getColumnToDataType();
    }
        
    /**
     * Instantiates a new handle.
     * 
     * @param result
     * @param registry
     * @param manager
     * @param stream
     * @param node
     * @param definition
     * @param config
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    protected DataHandleOutput(final ARXResult result,
                               final DataRegistry registry,
                               final DataManager manager,
                               final InputStream stream,
                               final ARXNode node,
                               final DataDefinition definition,
                               final ARXConfiguration config) throws ClassNotFoundException, IOException {
        
        // Read data from stream
        ObjectInputStream ois = new ObjectInputStream(stream);
        Data outputGeneralized = (Data) ois.readObject();
        Data outputMicroaggregated = (Data) ois.readObject();
        DataType<?>[] dataTypes = (DataType<?>[]) ois.readObject();

        // Initialize
        this.initialize(result, registry, manager, outputGeneralized, outputMicroaggregated, node, definition, config);

        // Obtain data types
        this.columnToDataType = dataTypes;
        
        // Mark as optimized
        this.optimized = true;
    }

    @Override
    public String getAttributeName(final int col) {
        checkRegistry();
        checkColumn(col);
        return header[col];
    }
    
    @Override
    public DataType<?> getDataType(String attribute) {
        checkRegistry();
        return this.columnToDataType[this.getColumnIndexOf(attribute)];
    }

    @Override
    public int getGeneralization(final String attribute) {
        checkRegistry();
        return node.getGeneralization(attribute);
    }

    @Override
    public int getNumColumns() {
        checkRegistry();
        return header.length;
    }
    
    @Override
    public int getNumRows() {
        checkRegistry();
        return dataGeneralized.getDataLength();
    }

    @Override
    public StatisticsBuilder getStatistics() {
        return new StatisticsBuilder(new DataHandleInternal(this));
    }

    @Override
    public String getValue(final int row, final int col) {
        
        // Check
        checkRegistry();
        checkColumn(col);
        checkRow(row, dataGeneralized.getDataLength());
        
        // Perform
        return internalGetValue(row, col, false);
    }

    @Override
    public boolean isOptimized() {
        return this.optimized;
    }
    
    /**
     * Iterator.
     * 
     * @return the iterator
     */
    @Override
    public Iterator<String[]> iterator() {
        checkRegistry();
        return new ResultIterator();
    }
    
    @Override
    public boolean replace(int column, String original, String replacement) {
        throw new UnsupportedOperationException("This operation is only supported by handles for data input");
    }
    
    /**
     * Internal method: writes some data into the output stream
     * @param out
     * @throws IOException 
     */
    public void write(OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(this.dataGeneralized);
        oos.writeObject(this.dataAggregated);
        oos.writeObject(this.columnToDataType);
    }

    /**
     * Initialization method
     * @param result
     * @param registry
     * @param manager
     * @param outputGeneralized
     * @param outputMicroaggregated
     * @param node
     * @param definition
     * @param config
     */
    private void initialize(final ARXResult result,
                            final DataRegistry registry,
                            final DataManager manager,
                            final Data outputGeneralized,
                            final Data outputMicroaggregated,
                            final ARXNode node,
                            final DataDefinition definition,
                            final ARXConfiguration config) {
        
        registry.updateOutput(node, this);
        this.setRegistry(registry);

        // Extract data
        this.dataGeneralized = outputGeneralized;
        this.dataAggregated = outputMicroaggregated;
        this.dataInput = manager.getDataInput();
        this.setHeader(manager.getHeader());

        // Prepare column mappings
        this.columnToData = new Data[header.length];
        this.columnToIndex = new int[header.length];
        
        // For each different block of data: it is important that generalized data is
        // processed before aggregated data, so that pointers for attributes that
        // have been clustered and aggregated are overwritten accordingly.
        for (Data data : new Data[]{dataGeneralized, dataAggregated}) {
            
            // For each attribute in this block
            for (int i = 0; i < data.getHeader().length; i++) {
                
                // Extract
                int column = data.getColumns()[i];
                
                // Store
                this.columnToIndex[column] = i;
                this.columnToData[column] = data;
            }
        }
        
        // Handle sensitive and insensitive data
        for (int column = 0; column < header.length; column++) {
            
            String attribute = header[column];
                        
            // Sensitive attributes
            if (definition.getSensitiveAttributes().contains(attribute) || 
                definition.getInsensitiveAttributes().contains(attribute)) {

                // Store
                this.columnToIndex[column] = column;
                this.columnToData[column] = dataInput;
            }
        }
        
        // Init
        this.columnToSuppressionStatus = getColumnToSuppressionStatus(config, definition);
        this.result = result;
        this.definition = definition;
        this.anonymous = node.getAnonymity() == Anonymity.ANONYMOUS;
        this.node = node;
        
        // Create view
        this.getRegistry().createOutputSubset(node, config);
    }
    
    /**
     * Releases all resources.
     */
    protected void doRelease() {
        result.releaseBuffer(this);
        node = null;
        dataInput = null;
        dataGeneralized = null;
        dataAggregated = null;
        registry = null;
        subset = null;
        columnToDataType = null;
        columnToIndex = null;
        columnToData = null;
        definition = null;
        header = null;
        headerMap = null;
        node = null;
    }
    
    @Override
    protected DataArray getDataArray(int[] columns, int[] rows) {
        return new DataArray(columnToData,
                             columnToIndex,
                             columnToSuppressionStatus,
                             dataGeneralized,
                             columns,
                             rows);
    }
    
    /**
     * Creates the data type array.
     *
     * @return
     */
    @Override
    protected DataType<?>[] getColumnToDataType() {
        
        // Prepare
        DataType<?>[] result = new DataType[header.length];
        
        // For each column
        for (int i = 0; i < header.length; i++) {
            
            // Initialize
            String attribute = header[i];
            Data data = columnToData[i];
            int index = columnToIndex[i];
            
            // We first check for aggregation, as it "dominates" generalization
            if (data == dataAggregated && !definition.getMicroAggregationFunction(attribute).isTypePreserving()) {
                result[i] = DataType.STRING;
                
            // Now we check for generalization
            } else if (data == dataGeneralized && node.getTransformation()[index] > 0) {
                result[i] = DataType.STRING;
                
            // Now we check whether this is a completely suppressed identifying variable
            } else if (data == null) {
                result[i] = DataType.STRING;
                
            // Now, we can safely assume that the data type has been preserved
            } else {
                result[i] = definition.getDataType(attribute);
            }
        }
        
        // Return
        return result;
    }
    
    /**
     * Returns the suppression status for each attribute
     * 
     * @param config
     * @param definition
     * @return
     */
    protected boolean[] getColumnToSuppressionStatus(ARXConfiguration config, DataDefinition definition) {

        // Convert
        boolean suppressedQuasiIdentifying = (config.getSuppressedAttributeTypes() & (1 << AttributeType.ATTR_TYPE_QI)) != 0;
        boolean suppressedSensitive = (config.getSuppressedAttributeTypes() & (1 << AttributeType.ATTR_TYPE_SE)) != 0;
        boolean suppressedInsensitive = (config.getSuppressedAttributeTypes() & (1 << AttributeType.ATTR_TYPE_IS)) != 0;
        
        // Prepare
        boolean[] result = new boolean[header.length];
        
        // For each column
        for (int i = 0; i < header.length; i++) {
            
            // Initialize
            String attribute = header[i];
            Data data = columnToData[i];
            
            // Quasi-identifiers
            if (data == dataAggregated || data == dataGeneralized) {
                result[i] = suppressedQuasiIdentifying;
                
            // Sensitive attributes
            } else if (definition.getSensitiveAttributes().contains(attribute)) {
                result[i] = suppressedSensitive;

            // Insensitive attributes
            } else if (definition.getInsensitiveAttributes().contains(attribute)) {
                result[i] = suppressedInsensitive;

            // We suppress by default, e.g. for identifying variables
            } else {
                result[i] = true;
            }
        }
        
        // Return
        return result;
    }
    
    @Override
    protected ARXConfiguration getConfiguration() {
        return result.getConfiguration();
    }
        
    /**
     * Gets the distinct values.
     *
     * @param col the column
     * @param ignoreSuppression
     * @param handler
     * @return the distinct values
     */
    @Override
    protected String[] getDistinctValues(final int col, final boolean ignoreSuppression, InterruptHandler handler) {
        
        // Check
        checkRegistry();
        checkColumn(col);
        
        final Set<String> vals = new HashSet<String>();
        for (int i = 0; i < getNumRows(); i++) {
            handler.checkInterrupt();
            vals.add(internalGetValue(i, col, ignoreSuppression));
        }
        handler.checkInterrupt();
        return vals.toArray(new String[vals.size()]);
    }
    
    /**
     * Returns the input buffer
     * @return
     */
    protected DataMatrix getInputBuffer() {
        checkRegistry();
        return registry.getInputHandle().getInputBuffer();
    }
    
    /**
     * Returns the output buffer
     * @return
     */
    protected Data getOutputBufferGeneralized() {
        return dataGeneralized;
    }
    
    /**
     * Returns the output buffer
     * @return
     */
    protected Data getOutputBufferMicroaggregated() {
        return dataAggregated;
    }
    
    @Override
    protected int getValueIdentifier(int column, String value) {
        
        // Extract info
        Data data = columnToData[column];
        int index = columnToIndex[column];
        
        // Handle identifying values
        if (data == null) {
            return -1;
            
        // Else return
        } else {
            String[] values = data.getDictionary().getMapping()[index];
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    /**
     * A negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. It uses the specified
     * data types for comparison if no generalization was applied, otherwise it
     * uses string comparison.
     * 
     * @param row1
     * @param row2
     * @param columns
     * @param ascending
     * @return the int
     */
    @Override
    protected int internalCompare(final int row1,
                                  final int row2,
                                  final int[] columns,
                                  final boolean ascending) {
        
        for (final int col : columns) {
            
            // Identifying attributes are removed from output data
            if (columnToData[col] == null) {
                continue;
            }
            
            int cmp = 0;
            
            try {
                String s1 = internalGetValue(row1, col, false);
                String s2 = internalGetValue(row2, col, false);
                cmp = (s1 == DataType.ANY_VALUE && s2 == DataType.ANY_VALUE) ? 0
                        : (s1 == DataType.ANY_VALUE ? +1
                                : (s2 == DataType.ANY_VALUE ? -1
                                        : columnToDataType[col].compare(s1, s2)));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            if (cmp != 0) {
                return ascending ? cmp : -cmp;
            }
        }
        return 0;
    }

    @Override
    protected int internalGetEncodedValue(final int row,
                                          final int col,
                                          final boolean ignoreSuppression) {

        // Extract info
        Data data = columnToData[col];
        
        // Handle identifying values
        if (data == null) {
            return -1;
            
        // Handle suppressed values
        } else if (!ignoreSuppression && (dataGeneralized.getArray().get(row, 0) & Data.OUTLIER_MASK) !=0 && columnToSuppressionStatus[col]) {
            return -1;
            
        // Handle all other values
        } else {
            
            // Decode and return
            return data.getArray().get(row, columnToIndex[col]) & Data.REMOVE_OUTLIER_MASK;
        }
    }
    
    /**
     * Gets the value internal.
     * 
     * @param row the row
     * @param col the col
     * @return the value internal
     */
    @Override
    protected String internalGetValue(final int row, 
                                      final int col,
                                      final boolean ignoreSuppression) {

        // Extract info
        Data data = columnToData[col];
        int index = columnToIndex[col];
        
        // Handle identifying values
        if (data == null) {
            return DataType.ANY_VALUE;
            
        // Handle suppressed values
        } else if (!ignoreSuppression && (dataGeneralized.getArray().get(row, 0) & Data.OUTLIER_MASK) !=0 && columnToSuppressionStatus[col]) {
            return DataType.ANY_VALUE;
            
        // Handle all other values
        } else {
            
            // Decode
            int value = data.getArray().get(row, index) & Data.REMOVE_OUTLIER_MASK;
            String[][] dictionary = data.getDictionary().getMapping();
            return dictionary[index][value];
        }
    }
    
    /**
     * Returns whether the given row is an outlier.
     *
     * @param row
     * @return
     */
    protected boolean internalIsOutlier(final int row) {
        return ((dataGeneralized.getArray().get(row, 0) & Data.OUTLIER_MASK) != 0);
    }
    
    @Override
    protected boolean internalIsOutlier(int row, int[] columns) {

        for (int column : columns) {

            // Extract info
            Data data = columnToData[column];
            
            // Identifying values are suppressed
            if (data == null) {
                continue;
            }
                
            // Suppressed values are suppressed
            if ((dataGeneralized.getArray().get(row, 0) & Data.OUTLIER_MASK) !=0 && columnToSuppressionStatus[column]) {
                continue;
            }
            
            // Extract info
            int index = columnToIndex[column];
            
            // Completely generalized values are suppressed
            int suppressed = data.getDictionary().getSuppressedCodes()[index];
            if ((data.getArray().get(row, index) & Data.REMOVE_OUTLIER_MASK) == suppressed) {
                continue;
            }
            
            // Not suppressed
            return false;
        }
        return true;
    }
    

    @Override
    protected boolean internalReplace(int column,
                                      String original,
                                      String replacement) {
        
        // Check
        if (column >= header.length || column < 0) {
            return false;
        }

        // Extract info
        Data data = columnToData[column];
        int index = columnToIndex[column];
        
        // Check
        if (data == null) {
            return false;
        }
        
        // Extract dictionary values
        String[] values = data.getDictionary().getMapping()[index];
        
        // Replace
        boolean found = false;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(original)) {
                values[i] = replacement;
                found = true;
            }
        }
        
        // Return
        return found;
    }

    /**
     * Swap internal.
     * 
     * @param row1 the row1
     * @param row2 the row2
     */
    protected void internalSwap(final int row1, final int row2) {
        
        // Swap generalized data
        dataGeneralized.getArray().swap(row1, row2);
        
        // Swap aggregated data
        if (dataAggregated.getArray().getNumRows() != 0) {
            dataAggregated.getArray().swap(row1, row2);
        }
    }

    @Override
    protected boolean isAnonymous() {
        return this.anonymous;
    }

    /**
     * Marks this handle as optimized
     * @param optimized
     */
    protected void setOptimized(boolean optimized) {
        this.optimized = true;
    }

    /**
     * Used to update data types after local recoding
     * @param transformation
     */
    protected void updateDataTypes(int[] transformation) {

        // For each column
        for (int i = 0; i < header.length; i++) {
            
            // Initialize
            Data data = columnToData[i];
            int index = columnToIndex[i];
            
            // Here, we only check for generalization
            if (data == dataGeneralized && transformation[index] > 0) {
                columnToDataType[i] = DataType.STRING;
            }
        }
    }
}
