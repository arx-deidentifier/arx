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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.DataHandleStatistics.InterruptHandler;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
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
            return row < dataGH.getArray().length;
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
                    result[i] = internalGetValue(row, i);
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
    
    /** The current result. */
    private ARXResult    result;
    
    /** The current node. */
    private ARXNode      node;
    
    /** The data. */
    protected Data       dataIS;
    
    /** The data. */
    protected Data       dataGH;
    
    /** The data. */
    protected Data       dataOT;
    
    /** The data. */
    protected Data       dataDI;
    
    /** An inverse map to data arrays. */
    private int[][][]    inverseData;
    
    /** The start index of the MA attributes in the dataDI */
    private final int    startIndexMA;
    
    /** An inverse map to dictionaries. */
    private Dictionary[] inverseDictionaries;
    
    /** An inverse map for column indices. map[i*2]=attribute type, map[i*2+1]=index position. */
    private int[]        inverseMap;
    
    /** Suppression handling. */
    private final int    suppressedAttributeTypes;
    
    /** Suppression handling. */
    private final String suppressionString;
    
    /**
     * Instantiates a new handle.
     *
     * @param result
     * @param registry The registry
     * @param manager The data manager
     * @param buffer
     * @param node The underlying transformation
     * @param statistics Statistics for the dataset
     * @param definition The data definition
     * @param config The underlying config
     */
    protected DataHandleOutput(final ARXResult result,
                               final DataRegistry registry,
                               final DataManager manager,
                               final Data buffer,
                               final ARXNode node,
                               final StatisticsEquivalenceClasses statistics,
                               final DataDefinition definition,
                               final ARXConfiguration config) {
        this(result, registry, manager, buffer, null, node, statistics, definition, config);
    }
    
    /**
     * Instantiates a new handle.
     * 
     * @param result
     * @param registry
     * @param manager
     * @param bufferGH
     * @param bufferOT
     * @param node
     * @param statistics
     * @param definition
     * @param config
     */
    protected DataHandleOutput(final ARXResult result,
                               final DataRegistry registry,
                               final DataManager manager,
                               final Data bufferGH,
                               final Data bufferOT,
                               final ARXNode node,
                               final StatisticsEquivalenceClasses statistics,
                               final DataDefinition definition,
                               final ARXConfiguration config) {
        
        registry.updateOutput(node, this);
        this.setRegistry(registry);
        
        // Init
        this.suppressionString = config.getSuppressionString();
        this.suppressedAttributeTypes = convert(config.getSuppressedAttributeTypes());
        this.result = result;
        this.definition = definition;
        this.statistics = new StatisticsBuilder(new DataHandleStatistics(this), statistics);
        this.node = node;
        
        // Extract data
        this.dataGH = bufferGH;
        this.dataOT = bufferOT;
        this.dataDI = manager.getDataDI();
        this.dataIS = manager.getDataIS();
        this.header = manager.getHeader();
        this.startIndexMA = manager.getStartIndexMA();
        
        // Build map inverse
        this.inverseMap = new int[header.length * 2];
        // Init with attribute type ID
        for (int i = 0; i < this.inverseMap.length; i += 2) {
            this.inverseMap[i] = AttributeTypeInternal.IDENTIFIER;
            this.inverseMap[i + 1] = -1;
        }
        for (int i = 0; i < this.dataGH.getMap().length; i++) {
            final int pos = dataGH.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.GENERALIZATION;
            this.inverseMap[pos + 1] = i;
        }
        for (int i = 0; i < this.startIndexMA; i++) {
            final int pos = dataDI.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.SENSITIVE;
            this.inverseMap[pos + 1] = i;
        }
        
        for (int i = 0; i < dataOT.getMap().length; i++) {
            final int pos = dataOT.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.MICROAGGREGATION;
            this.inverseMap[pos + 1] = i;
        }
        
        for (int i = 0; i < dataIS.getMap().length; i++) {
            final int pos = dataIS.getMap()[i] * 2;
            this.inverseMap[pos] = AttributeTypeInternal.INSENSITIVE;
            this.inverseMap[pos + 1] = i;
        }
        
        // Build inverse data array
        this.inverseData = new int[5][][];
        this.inverseData[AttributeTypeInternal.INSENSITIVE] = this.dataIS.getArray();
        this.inverseData[AttributeTypeInternal.SENSITIVE] = this.dataDI.getArray();
        this.inverseData[AttributeTypeInternal.GENERALIZATION] = this.dataGH.getArray();
        this.inverseData[AttributeTypeInternal.IDENTIFIER] = null;
        this.inverseData[AttributeTypeInternal.MICROAGGREGATION] = this.dataOT.getArray();
        
        // Build inverse dictionary array
        this.inverseDictionaries = new Dictionary[5];
        this.inverseDictionaries[AttributeTypeInternal.INSENSITIVE] = this.dataIS.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.SENSITIVE] = this.dataDI.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.GENERALIZATION] = this.dataGH.getDictionary();
        this.inverseDictionaries[AttributeTypeInternal.IDENTIFIER] = null;
        this.inverseDictionaries[AttributeTypeInternal.MICROAGGREGATION] = this.dataOT.getDictionary();
        
        // Create view
        this.getRegistry().createOutputSubset(node, config, statistics);
        
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
        case AttributeTypeInternal.IDENTIFIER:
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
        return dataGH.getDataLength();
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
        checkRow(row, dataGH.getDataLength());
        
        // Perform
        return internalGetValue(row, col);
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
                    converted |= (1 << AttributeTypeInternal.IDENTIFIER);
                    break;
                case AttributeType.ATTR_TYPE_IS:
                    converted |= (1 << AttributeTypeInternal.INSENSITIVE);
                    break;
                case AttributeType.ATTR_TYPE_QI:
                    converted |= (1 << AttributeTypeInternal.GENERALIZATION) | (1 << AttributeTypeInternal.MICROAGGREGATION);
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
     * Releases all resources.
     */
    protected void doRelease() {
        result.releaseBuffer(this);
        node = null;
        dataIS = null;
        dataGH = null;
        dataDI = null;
        dataOT = null;
        inverseData = null;
        inverseDictionaries = null;
        inverseMap = null;
        registry = null;
        subset = null;
        dataTypes = null;
        definition = null;
        header = null;
        statistics = null;
        node = null;
    }
    
    /**
     * Creates the data type array.
     *
     * @return
     */
    @Override
    protected DataType<?>[][] getDataTypeArray() {
        
        DataType<?>[][] dataTypes = new DataType[5][];
        dataTypes[AttributeTypeInternal.INSENSITIVE] = new DataType[dataIS.getHeader().length];
        dataTypes[AttributeTypeInternal.SENSITIVE] = new DataType[dataDI.getHeader().length];
        dataTypes[AttributeTypeInternal.GENERALIZATION] = new DataType[dataGH.getHeader().length];
        dataTypes[AttributeTypeInternal.MICROAGGREGATION] = new DataType[dataOT.getHeader().length];
        dataTypes[AttributeTypeInternal.IDENTIFIER] = null;
        
        for (int i = 0; i < dataTypes.length; i++) {
            final DataType<?>[] type = dataTypes[i];
            
            String[] header = null;
            
            switch (i) {
            case AttributeTypeInternal.INSENSITIVE:
                header = dataIS.getHeader();
                break;
            case AttributeTypeInternal.GENERALIZATION:
                header = dataGH.getHeader();
                break;
            case AttributeTypeInternal.SENSITIVE:
                header = dataDI.getHeader();
                break;
            case AttributeTypeInternal.MICROAGGREGATION:
                header = dataOT.getHeader();
                break;
            }
            if (type != null) {
                for (int j = 0; j < type.length; j++) {
                    if (dataTypes[i][j] != null) {
                        dataTypes[i][j] = definition.getDataType(header[j]);
                        if ((i == AttributeTypeInternal.GENERALIZATION) &&
                            (node.getTransformation()[j] > 0)) {
                            dataTypes[i][j] = DataType.STRING;
                        }
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
     * @param handler
     * @return the distinct values
     */
    @Override
    protected String[] getDistinctValues(final int col, InterruptHandler handler) {
        
        // Check
        checkRegistry();
        checkColumn(col);
        
        final Set<String> vals = new HashSet<String>();
        for (int i = 0; i < getNumRows(); i++) {
            handler.checkInterrupt();
            vals.add(getValue(i, col));
        }
        handler.checkInterrupt();
        return vals.toArray(new String[vals.size()]);
    }
    
    /**
     * Returns the suppression string.
     *
     * @return
     */
    protected String getSuppressionString() {
        return this.suppressionString;
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
            if (attributeType == AttributeTypeInternal.IDENTIFIER) return 0;
            
            int cmp = 0;
            try {
                String s1 = internalGetValue(row1, index);
                String s2 = internalGetValue(row2, index);
                cmp = (s1 == suppressionString && s2 == suppressionString) ? 0
                        : (s1 == suppressionString ? +1
                                : (s2 == suppressionString ? -1
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
    protected String internalGetValue(final int row, final int col) {
        
        // Return the according values
        final int key = col * 2;
        final int type = inverseMap[key];
        switch (type) {
        case AttributeTypeInternal.IDENTIFIER:
            return suppressionString;
        default:
            final int index = inverseMap[key + 1];
            final int[][] data = inverseData[type];
            
            if ((suppressedAttributeTypes & (1 << type)) != 0 &&
                ((dataGH.getArray()[row][0] & Data.OUTLIER_MASK) != 0)) {
                return suppressionString;
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
        return ((dataGH.getArray()[row][0] & Data.OUTLIER_MASK) != 0);
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
        int[] temp = dataGH.getArray()[row1];
        dataGH.getArray()[row1] = dataGH.getArray()[row2];
        dataGH.getArray()[row2] = temp;
        
        // Swap OT
        temp = dataOT.getArray()[row1];
        dataOT.getArray()[row1] = dataOT.getArray()[row2];
        dataOT.getArray()[row2] = temp;
    }
}
