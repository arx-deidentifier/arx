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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataManager.AttributeTypeInternal;
import org.deidentifier.arx.framework.data.Dictionary;

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
    public class ResultIterator implements Iterator<String[]> {
        
        /** The current row. */
        private int row = -1;
        
        @Override
        public boolean hasNext() {
            return row < outputGeneralized.getArray().length;
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

    /** The data. */
    private Data         inputAnalyzed;

    /** The data. */
    private Data         inputStatic;

    /** An inverse map to data arrays. */
    private int[][][]    inverseData;

    /** An inverse map to dictionaries. */
    private Dictionary[] inverseDictionaries;

    /** An inverse map for column indices. map[i*2]=attribute type, map[i*2+1]=index position. */
    private int[]        inverseMap;

    /** The start index of the MA attributes in the dataDI */
    private final int    microaggregationStartIndex;

    /** The data. */
    private Data         outputGeneralized;

    /** The data. */
    private Data         outputMicroaggregated;

    /** The current result. */
    private ARXResult    result;

    /** Suppression handling. */
    private final int    suppressedAttributeTypes;

    /** Flag determining whether this buffer has been optimized */
    private boolean      optimized = false;

    /** Flag determining whether this buffer is anonymous */
    private boolean      anonymous = false;
    
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
        
        registry.updateOutput(node, this);
        this.setRegistry(registry);
        
        // Init
        this.suppressedAttributeTypes = convert(config.getSuppressedAttributeTypes());
        this.result = result;
        this.definition = definition;
        this.anonymous = node.getAnonymity() == Anonymity.ANONYMOUS;
        this.node = node;
        
        // Extract data
        this.outputGeneralized = outputGeneralized;
        this.outputMicroaggregated = outputMicroaggregated;
        this.inputAnalyzed = manager.getDataAnalyzed();
        this.inputStatic = manager.getDataStatic();
        this.header = manager.getHeader();
        this.microaggregationStartIndex = manager.getMicroaggregationStartIndex();
        
        // Build map inverse
        this.inverseMap = new int[header.length * 2];
        // Init with attribute type ID
        for (int i = 0; i < this.inverseMap.length; i += 2) {
            this.inverseMap[i] = AttributeTypeInternal.IDENTIFYING;
            this.inverseMap[i + 1] = -1;
        }
        for (int i = 0; i < this.outputGeneralized.getMap().length; i++) {
            final int pos = outputGeneralized.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED;
            this.inverseMap[pos + 1] = i;
        }
        for (int i = 0; i < this.microaggregationStartIndex; i++) {
            final int pos = inputAnalyzed.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.SENSITIVE;
            this.inverseMap[pos + 1] = i;
        }
        
        for (int i = 0; i < outputMicroaggregated.getMap().length; i++) {
            final int pos = outputMicroaggregated.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED;
            this.inverseMap[pos + 1] = i;
        }
        
        for (int i = 0; i < inputStatic.getMap().length; i++) {
            final int pos = inputStatic.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.INSENSITIVE;
            this.inverseMap[pos + 1] = i;
        }
        
        // Build inverse data array
        this.inverseData = new int[5][][];
        this.inverseData[AttributeTypeInternal.INSENSITIVE] = this.inputStatic.getArray();
        this.inverseData[AttributeTypeInternal.SENSITIVE] = this.inputAnalyzed.getArray();
        this.inverseData[AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED] = this.outputGeneralized.getArray();
        this.inverseData[AttributeTypeInternal.IDENTIFYING] = null;
        this.inverseData[AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED] = this.outputMicroaggregated.getArray();
        
        // Build inverse dictionary array
        this.inverseDictionaries = new Dictionary[5];
        this.inverseDictionaries[AttributeTypeInternal.INSENSITIVE] = this.inputStatic.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.SENSITIVE] = this.inputAnalyzed.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED] = this.outputGeneralized.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.IDENTIFYING] = null;
        this.inverseDictionaries[AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED] = this.outputMicroaggregated.getDictionary();
        
        // Create view
        this.getRegistry().createOutputSubset(node, config);
        
        // Obtain data types
        this.dataTypes = getDataTypeArray();
    }
    
    /**
     * Gets the attribute name.
     * 
     * @param col
     *            the col
     * @return the attribute name
     */
    @Override
    public String getAttributeName(final int col) {
        checkRegistry();
        checkColumn(col);
        return header[col];
    }

    @Override
    public DataType<?> getDataType(String attribute) {
        
        checkRegistry();
        int col = this.getColumnIndexOf(attribute);
        
        // Return the according values
        final int key = col * 2;
        final int type = inverseMap[key];
        switch (type) {
        case AttributeTypeInternal.IDENTIFYING:
            return DataType.STRING;
        default:
            final int index = inverseMap[key + 1];
            return dataTypes[type][index];
        }
    }

    @Override
    public int getGeneralization(final String attribute) {
        checkRegistry();
        return node.getGeneralization(attribute);
    }
    
    /**
     * Gets the num columns.
     * 
     * @return the num columns
     */
    @Override
    public int getNumColumns() {
        checkRegistry();
        return header.length;
    }

    /**
     * Gets the num rows.
     * 
     * @return the num rows
     */
    @Override
    public int getNumRows() {
        checkRegistry();
        return outputGeneralized.getDataLength();
    }

    @Override
    public StatisticsBuilder getStatistics() {
        return new StatisticsBuilder(new DataHandleInternal(this));
    }

    /**
     * Gets the value.
     * 
     * @param row
     *            the row
     * @param col
     *            the col
     * @return the value
     */
    @Override
    public String getValue(final int row, final int col) {
        
        // Check
        checkRegistry();
        checkColumn(col);
        checkRow(row, outputGeneralized.getDataLength());
        
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
     * Used to update data when loading projects after local recoding. This is part of the internal API
     * and should never be called by users
     * @param data
     * @param types
     */
    public void updateData(DataHandle data, 
                           Map<String, DataType<?>> types,
                           int[] outliers) {

        updateData(data, outputGeneralized, types, outliers);
        updateData(data, outputMicroaggregated, types, outliers);
        
        // Update outliers
        int previous = 0;
        for (int index : outliers) {

            // Mark as not outlier from previous to index
            for (int i = previous; i < index; i++) {
                outputGeneralized.getArray()[i][0] &= Data.REMOVE_OUTLIER_MASK;
            }

            // Mark index as outlier
            outputGeneralized.getArray()[index][0] |= Data.OUTLIER_MASK;

            // Update
            previous = index + 1;
        }
        
        // Mark as not outlier from previous to num rows
        for (int i = previous; i < this.getNumRows(); i++) {
            outputGeneralized.getArray()[i][0] &= Data.REMOVE_OUTLIER_MASK;
        }
                
        // Update data types
        for (int i = 0; i < dataTypes.length; i++) {
            DataType<?>[] type = dataTypes[i];
            if (type != null) {
                for (int j = 0; j < type.length; j++) {
                    if (i == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED) {
                        String attribute = this.outputGeneralized.getHeader()[j];
                        if (types.get(attribute) == DataType.STRING) {
                            dataTypes[i][j] = DataType.STRING;
                        }
                    }
                }
            }
        }
        
        // Mark as optimized
        this.optimized = true;
    }
    
    /**
     * Converts the suppressed attribute type bitset to the internal datatypes.
     * 
     * @param suppressedAttributeTypes
     * @return
     */
    private int convert(int suppressedAttributeTypes) {
        int converted = 0;
        for (int j = 0; j < 32; j++) {
            if ((suppressedAttributeTypes & (1 << j)) != 0) {
                switch (j) {
                case AttributeType.ATTR_TYPE_ID:
                    converted |= (1 << AttributeTypeInternal.IDENTIFYING);
                    break;
                case AttributeType.ATTR_TYPE_IS:
                    converted |= (1 << AttributeTypeInternal.INSENSITIVE);
                    break;
                case AttributeType.ATTR_TYPE_QI:
                    converted |= (1 << AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED) | (1 << AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED);
                    break;
                case AttributeType.ATTR_TYPE_SE:
                    converted |= (1 << AttributeTypeInternal.SENSITIVE);
                    break;
                }
            }
            
        }
        return converted;
    }
    
    /**
     * Used to update data when loading projects after local recoding. This is part of the internal API
     * and should never be called by users
     * @param input
     * @param output
     * @param types
     * @param outliers 
     */
    private void updateData(DataHandle input,
                            Data output,
                            Map<String, DataType<?>> types, 
                            int[] outliers) {
        
        // Init
        String[] header = output.getHeader();
        int[][] data = output.getData();
        Dictionary dictionary = output.getDictionary();
        
        // De-finalize
        dictionary.definalizeAll();
        
        // Update
        for (int column = 0; column < header.length; column++) {
            String attribute = header[column];
            int columnindex = input.getColumnIndexOf(attribute);

            // Update only tuples that are not outliers
            int previous = 0;
            for (int index : outliers) {

                // Update
                for (int row = previous; row < index; row++) {
                    
                    String value = input.internalGetValue(row, columnindex, false);
                    int identifier = dictionary.register(column, value);
                    data[row][column] = identifier;                    
                }

                // Update
                previous = index + 1;
            }

            // Update remaining tuples
            for (int row = previous; row < input.getNumRows(); row++) {
                
                String value = input.internalGetValue(row, columnindex, false);
                int identifier = dictionary.register(column, value);
                data[row][column] = identifier;                    
            }
        }
        
        // Finalize
        dictionary.finalizeAll();
    }

    /**
     * Releases all resources.
     */
    protected void doRelease() {
        result.releaseBuffer(this);
        node = null;
        inputStatic = null;
        outputGeneralized = null;
        inputAnalyzed = null;
        outputMicroaggregated = null;
        inverseData = null;
        inverseDictionaries = null;
        inverseMap = null;
        registry = null;
        subset = null;
        dataTypes = null;
        definition = null;
        header = null;
        node = null;
    }
    
    @Override
    protected ARXConfiguration getConfiguration() {
        return result.getConfiguration();
    }
    
    /**
     * Creates the data type array.
     *
     * @return
     */
    @Override
    protected DataType<?>[][] getDataTypeArray() {
        
        DataType<?>[][] dataTypes = new DataType[5][];
        dataTypes[AttributeTypeInternal.INSENSITIVE] = new DataType[inputStatic.getHeader().length];
        dataTypes[AttributeTypeInternal.SENSITIVE] = new DataType[inputAnalyzed.getHeader().length];
        dataTypes[AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED] = new DataType[outputGeneralized.getHeader().length];
        dataTypes[AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED] = new DataType[outputMicroaggregated.getHeader().length];
        dataTypes[AttributeTypeInternal.IDENTIFYING] = null;
        
        for (int i = 0; i < dataTypes.length; i++) {
            
            final DataType<?>[] type = dataTypes[i];
            String[] header = null;
            
            switch (i) {
            case AttributeTypeInternal.INSENSITIVE:
                header = inputStatic.getHeader();
                break;
            case AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED:
                header = outputGeneralized.getHeader();
                break;
            case AttributeTypeInternal.SENSITIVE:
                header = inputAnalyzed.getHeader();
                break;
            case AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED:
                header = outputMicroaggregated.getHeader();
                break;
            }
            if (type != null) {
                for (int j = 0; j < type.length; j++) {
                    dataTypes[i][j] = definition.getDataType(header[j]);
                    if ((i == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED && node.getTransformation()[j] > 0) || 
                        (i == AttributeTypeInternal.QUASI_IDENTIFYING_MICROAGGREGATED && !definition.getMicroAggregationFunction(header[j]).isTypePreserving())) {
                        dataTypes[i][j] = DataType.STRING;
                    }
                }
            }
        }
        return dataTypes;
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
    protected int[][] getInputBuffer() {
        checkRegistry();
        return registry.getInputHandle().getInputBuffer();
    }
    
    /**
     * Returns the output buffer
     * @return
     */
    protected Data getOutputBufferGeneralized() {
        return outputGeneralized;
    }
    
    /**
     * Returns the output buffer
     * @return
     */
    protected Data getOutputBufferMicroaggregated() {
        return outputMicroaggregated;
    }
    
    /**
     * A negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. It uses the specified
     * data types for comparison if no generalization was applied, otherwise it
     * uses string comparison.
     * 
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     * @param columns
     *            the columns
     * @param ascending
     *            the ascending
     * @return the int
     */
    @Override
    protected int internalCompare(final int row1,
                                  final int row2,
                                  final int[] columns,
                                  final boolean ascending) {
        
        for (final int index : columns) {
            final int key = index * 2;
            final int attributeType = inverseMap[key];
            final int indexMap = inverseMap[key + 1];
            
            // Identifying attributes are removed from output data
            if (attributeType == AttributeTypeInternal.IDENTIFYING) {
                continue;
            }
            
            int cmp = 0;
            
            try {
                String s1 = internalGetValue(row1, index, false);
                String s2 = internalGetValue(row2, index, false);
                cmp = (s1 == DataType.ANY_VALUE && s2 == DataType.ANY_VALUE) ? 0
                        : (s1 == DataType.ANY_VALUE ? +1
                                : (s2 == DataType.ANY_VALUE ? -1
                                        : dataTypes[attributeType][indexMap].compare(s1, s2)));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            if (cmp != 0) {
                return ascending ? cmp : -cmp;
            }
        }
        return 0;
    }
    
    /**
     * Gets the value internal.
     * 
     * @param row
     *            the row
     * @param col
     *            the col
     * @return the value internal
     */
    @Override
    protected String internalGetValue(final int row, 
                                      final int col,
                                      final boolean ignoreSuppression) {
        
        // Return the according values
        final int key = col * 2;
        final int type = inverseMap[key];
        switch (type) {
        case AttributeTypeInternal.IDENTIFYING:
            return DataType.ANY_VALUE;
        default:
            final int index = inverseMap[key + 1];
            final int[][] data = inverseData[type];
            
            if (!ignoreSuppression && (suppressedAttributeTypes & (1 << type)) != 0 &&
                ((outputGeneralized.getArray()[row][0] & Data.OUTLIER_MASK) != 0)) {
                return DataType.ANY_VALUE;
            }
            
            final int value = data[row][index] & Data.REMOVE_OUTLIER_MASK;
            final String[][] dictionary = inverseDictionaries[type].getMapping();
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
        return ((outputGeneralized.getArray()[row][0] & Data.OUTLIER_MASK) != 0);
    }
    
    @Override
    protected boolean internalReplace(int column,
                                      String original,
                                      String replacement) {
        
        // Init and check
        if (column >= inverseMap.length) return false;
        final int key = column * 2;
        int type = inverseMap[key];
        if (type >= inverseDictionaries.length) return false;
        String[][] dictionary = inverseDictionaries[type].getMapping();
        int index = inverseMap[key + 1];
        if (index >= dictionary.length) return false;
        String[] values = dictionary[index];
        
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
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     */
    protected void internalSwap(final int row1, final int row2) {
        // Swap GH
        int[] temp = outputGeneralized.getArray()[row1];
        outputGeneralized.getArray()[row1] = outputGeneralized.getArray()[row2];
        outputGeneralized.getArray()[row2] = temp;
        
        // Swap OT
        if (outputMicroaggregated.getArray().length != 0) {
            temp = outputMicroaggregated.getArray()[row1];
            outputMicroaggregated.getArray()[row1] = outputMicroaggregated.getArray()[row2];
            outputMicroaggregated.getArray()[row2] = temp;
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

        for (int i = 0; i < dataTypes.length; i++) {
            DataType<?>[] type = dataTypes[i];
            if (type != null) {
                for (int j = 0; j < type.length; j++) {
                    if ((i == AttributeTypeInternal.QUASI_IDENTIFYING_GENERALIZED && transformation[j] > 0)) {
                        dataTypes[i][j] = DataType.STRING;
                    }
                }
            }
        }
    }
}
