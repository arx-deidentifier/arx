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
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

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
            return row < dataQI.getArray().length;
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
    private ARXResult     result;

    /** The current node. */
    private ARXNode       node;

    /** The data. */
    protected Data        dataIS;

    /** The data. */
    protected Data        dataQI;

    /** The data. */
    protected Data        dataSE;

    /** An inverse map to data arrays. */
    private int[][][]     inverseData;

    /** An inverse map to dictionaries. */
    private Dictionary[]  inverseDictionaries;

    /** An inverse map for column indices. */
    private int[]         inverseMap;

    /** The generalization hierarchies. */
    private int[][][]     map;

    /** The names of the quasiIdentifer. */
    private String[]      quasiIdentifiers;

    /** Suppression handling. */
    private final int     suppressedAttributeTypes;

    /** Suppression handling. */
    private final String  suppressionString;

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

        registry.updateOutput(node, this);
        this.setRegistry(registry);

        // Init
        this.suppressionString = config.getSuppressionString();
        this.suppressedAttributeTypes = config.getSuppressedAttributeTypes();
        this.result = result;
        this.definition = definition;
        this.statistics = new StatisticsBuilder(new DataHandleStatistics(this), statistics);
        this.node = node;

        // Extract data
        this.dataQI = buffer;
        this.dataSE = manager.getDataSE();
        this.dataIS = manager.getDataIS();
        this.header = manager.getHeader();

        // Init quasi identifiers and hierarchies
        GeneralizationHierarchy[] hierarchies = manager.getHierarchies();
        this.quasiIdentifiers = new String[hierarchies.length];
        this.map = new int[hierarchies.length][][];
        for (int i = 0; i < hierarchies.length; i++) {
            this.quasiIdentifiers[i] = hierarchies[i].getName();
            this.map[i] = hierarchies[i].getArray();
        }

        // Build map inverse
        this.inverseMap = new int[header.length];
        for (int i = 0; i < this.inverseMap.length; i++) {
            this.inverseMap[i] = (AttributeType.ATTR_TYPE_ID << AttributeType.SHIFT);
        }
        for (int i = 0; i < this.dataQI.getMap().length; i++) {
            this.inverseMap[dataQI.getMap()[i]] = i | (AttributeType.ATTR_TYPE_QI << AttributeType.SHIFT);
        }
        for (int i = 0; i < this.dataSE.getMap().length; i++) {
            this.inverseMap[dataSE.getMap()[i]] = i | (AttributeType.ATTR_TYPE_SE << AttributeType.SHIFT);
        }
        for (int i = 0; i < dataIS.getMap().length; i++) {
            this.inverseMap[dataIS.getMap()[i]] = i | (AttributeType.ATTR_TYPE_IS << AttributeType.SHIFT);
        }

        // Build inverse data array
        this.inverseData = new int[3][][];
        this.inverseData[AttributeType.ATTR_TYPE_IS] = this.dataIS.getArray();
        this.inverseData[AttributeType.ATTR_TYPE_SE] = this.dataSE.getArray();
        this.inverseData[AttributeType.ATTR_TYPE_QI] = this.dataQI.getArray();

        // Build inverse dictionary array
        this.inverseDictionaries = new Dictionary[3];
        this.inverseDictionaries[AttributeType.ATTR_TYPE_IS] = this.dataIS.getDictionary();
        this.inverseDictionaries[AttributeType.ATTR_TYPE_SE] = this.dataSE.getDictionary();
        this.inverseDictionaries[AttributeType.ATTR_TYPE_QI] = this.dataQI.getDictionary();
        
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
        final int type = inverseMap[col] >>> AttributeType.SHIFT;
        switch (type) {
        case AttributeType.ATTR_TYPE_ID:
            return DataType.STRING;
        default:
            final int index = inverseMap[col] & AttributeType.MASK;
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
        return dataQI.getDataLength();
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
        checkRow(row, dataQI.getDataLength());

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
     * Releases all resources.
     */
    protected void doRelease() {
        result.releaseBuffer(this);
        node = null;
        dataIS = null;
        dataQI = null;
        dataSE = null;
        inverseData = null;
        inverseDictionaries = null;
        inverseMap = null;
        map = null;
        quasiIdentifiers = null;
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

        DataType<?>[][] dataTypes = new DataType[3][];
        dataTypes[AttributeType.ATTR_TYPE_IS] = new DataType[dataIS.getHeader().length];
        dataTypes[AttributeType.ATTR_TYPE_SE] = new DataType[dataSE.getHeader().length];
        dataTypes[AttributeType.ATTR_TYPE_QI] = new DataType[dataQI.getHeader().length];

        for (int i = 0; i < dataTypes.length; i++) {
            final DataType<?>[] type = dataTypes[i];

            String[] header = null;

            switch (i) {
            case AttributeType.ATTR_TYPE_IS:
                header = dataIS.getHeader();
                break;
            case AttributeType.ATTR_TYPE_QI:
                header = dataQI.getHeader();
                break;
            case AttributeType.ATTR_TYPE_SE:
                header = dataSE.getHeader();
                break;
            }

            for (int j = 0; j < type.length; j++) {
                dataTypes[i][j] = definition.getDataType(header[j]);
                if ((i == AttributeType.ATTR_TYPE_QI) &&
                    (node.getTransformation()[j] > 0)) {
                    dataTypes[i][j] = DataType.STRING;
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
    protected String getSuppressionString(){
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

            final int attributeType = inverseMap[index] >>> AttributeType.SHIFT;
            final int indexMap = inverseMap[index] & AttributeType.MASK;
            if (attributeType == AttributeType.ATTR_TYPE_ID) return 0;
            
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
        final int type = inverseMap[col] >>> AttributeType.SHIFT;
        switch (type) {
        case AttributeType.ATTR_TYPE_ID:
            return suppressionString;
        default:
            final int index = inverseMap[col] & AttributeType.MASK;
            final int[][] data = inverseData[type];

            if ((suppressedAttributeTypes & (1 << type)) != 0 &&
                ((dataQI.getArray()[row][0] & Data.OUTLIER_MASK) != 0)) { return suppressionString; }

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
        return ((dataQI.getArray()[row][0] & Data.OUTLIER_MASK) != 0);
    }

    @Override
    protected boolean internalReplace(int column,
                                      String original,
                                      String replacement) {


        // Init and check
        if (column >= inverseMap.length) return false;
        int type = inverseMap[column] >>> AttributeType.SHIFT;
        if (type >= inverseDictionaries.length) return false;
        String[][] dictionary = inverseDictionaries[type].getMapping();
        int index = inverseMap[column] & AttributeType.MASK;
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
        int[] temp = dataQI.getArray()[row1];
        dataQI.getArray()[row1] = dataQI.getArray()[row2];
        dataQI.getArray()[row2] = temp;
    }
}
