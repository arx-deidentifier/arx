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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.framework.data.Dictionary;

/**
 * An implementation of the DataHandle interface for input data.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataHandleInput extends DataHandle {

    /** The data. */
    protected int[][]    data       = null;

    /** The dictionary. */
    protected Dictionary dictionary = null;

    /** The data. */
    private int[][]      dataGH     = null;

    /** The data. */
    private int[][]      dataDI     = null;

    /** The data. */
    private int[][]      dataIS     = null;
    
    /** Is this handle locked?. */
    private boolean      locked     = false;

    /**
     * Creates a new data handle.
     *
     * @param data
     */
    protected DataHandleInput(final Data data) {
        
        // Obtain and check iterator
        final Iterator<String[]> iterator = data.iterator();
        if (!iterator.hasNext()) { 
            throw new IllegalArgumentException("Data object is empty!"); 
        }

        // Register
        this.setRegistry(new DataRegistry());
        this.getRegistry().updateInput(this);
        this.definition = data.getDefinition().clone();

        // Obtain header
        final String[] columns = iterator.next();
        super.header = Arrays.copyOf(columns, columns.length);

        // Init dictionary
        this.dictionary = new Dictionary(header.length);

        // Encode data
        List<int[]> vals = new ArrayList<int[]>();
        while (iterator.hasNext()) {

            // Process a tuple
            final String[] strings = iterator.next();
            final int[] tuple = new int[header.length];
            for (int i = 0; i < strings.length; i++) {
                tuple[i] = dictionary.register(i, strings[i]);
            }
            vals.add(tuple);
        }

        // Build array
        this.data = vals.toArray(new int[vals.size()][]);

        // finalize dictionary
        this.dictionary.finalizeAll();

        // Create datatype array
        this.dataTypes = getDataTypeArray();
    }

    @Override
    public String getAttributeName(final int column) {
        checkRegistry();
        checkColumn(column);
        return header[column];
    }

    @Override
    public int getGeneralization(final String attribute) {
        checkRegistry();
        return 0;
    }

    @Override
    public int getNumColumns() {
        checkRegistry();
        return header.length;
    }

    @Override
    public int getNumRows() {
        checkRegistry();
        return data.length;
    }

    @Override
    public StatisticsBuilder getStatistics() {
        return new StatisticsBuilder(new DataHandleInternal(this));
    }

    @Override
    public String getValue(final int row, final int column) {
        checkRegistry();
        checkColumn(column);
        checkRow(row, data.length);
        return internalGetValue(row, column, false);
    }

    @Override
    public boolean isOutlier(int row){
        return false;
    }

    @Override
    public Iterator<String[]> iterator() {
        checkRegistry();
        return new Iterator<String[]>() {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index < data.length);
            }

            @Override
            public String[] next() {
                if (index == -1) {
                    index++;
                    return header;
                } else {
                    final String[] result = new String[header.length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = getValue(index, i);
                    }
                    index++;
                    return result;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported by this iterator");
            }
        };
    }
    
    /**
     * Swaps two rows.
     *
     * @param row1
     * @param row2
     * @param data
     */
    private void swap(int row1, int row2, int[][] data){
        final int[] temp = data[row1];
        data[row1] = data[row2];
        data[row2] = temp;
    }

    /**
     * Releases all resources.
     */
    protected void doRelease() {
        this.setLocked(false);
        dataGH = null;
        dataDI = null;
        dataIS = null;
    }
    
    @Override
    protected DataType<?> getBaseDataType(final String attribute) {
        return this.getDataType(attribute);
    }

    @Override
    protected ARXConfiguration getConfiguration() {
        return null;
    }

    @Override
    protected DataType<?>[][] getDataTypeArray() {
        checkRegistry();
        DataType<?>[][] dataTypes = new DataType[1][header.length];
        for (int i = 0; i < header.length; i++) {
            final DataType<?> type = definition.getDataType(header[i]);
            if (type != null) {
                dataTypes[0][i] = type;
            } else {
                dataTypes[0][i] = DataType.STRING;
            }
        }
        return dataTypes;
    }
    
    @Override
    protected String[] getDistinctValues(final int column, final boolean ignoreSuppression, InterruptHandler handler) {
        checkRegistry();
        handler.checkInterrupt();
        checkColumn(column);
        handler.checkInterrupt();
        final String[] dict = dictionary.getMapping()[column];
        handler.checkInterrupt();
        final String[] vals = new String[dict.length];
        handler.checkInterrupt();
        System.arraycopy(dict, 0, vals, 0, vals.length);
        return vals;
    }

    /**
     * Returns the input buffer
     * @return
     */
    protected int[][] getInputBuffer() {
        checkRegistry();
        return this.dataGH;
    }
    
    @Override
    protected String internalGetValue(final int row, final int column, final boolean ignoreSuppression) {
        return dictionary.getMapping()[column][data[row][column]];
    }
    
    @Override
    protected boolean internalReplace(int column,
                                      String original,
                                      String replacement) {

        String[] values = dictionary.getMapping()[column];
        boolean found = false;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(original)) {
                values[i] = replacement;
                found = true;
            }
        }
        return found;
    }

    /**
     * Swaps the rows.
     *
     * @param row1
     * @param row2
     */
    protected void internalSwap(final int row1, final int row2) {

        // Check
        checkRow(row1, data.length);
        checkRow(row2, data.length);

        // Swap
        swap(row1, row2, data);
        if (dataGH != null) swap(row1, row2, dataGH);
        if (dataDI != null) swap(row1, row2, dataDI);
        if (dataIS != null) swap(row1, row2, dataIS);
    }

    /**
     * Is this handle locked?.
     *
     * @return
     */
    protected boolean isLocked(){
        return this.locked;
    }

    /**
     * Overrides the handles data definition.
     *
     * @param definition
     */
    protected void setDefinition(DataDefinition definition) {
        this.definition = definition;
    }

    /**
     * Lock/unlock this handle.
     *
     * @param locked
     */
    protected void setLocked(boolean locked){
        this.locked = locked;
    }

    /**
     * Update the definition.
     *
     * @param data
     */
    protected void update(Data data){

        if (!this.isLocked()) {
            this.definition = data.getDefinition().clone();
            this.dataTypes = getDataTypeArray();
            this.definition.setLocked(true);
        }
    }

    /**
     * Updates the definition with further data to swap.
     *
     * @param dataGH
     * @param dataDI
     * @param dataIS
     */
    protected void update(int[][] dataGH, int[][] dataDI, int[][] dataIS) {
        this.dataGH = dataGH;
        this.dataDI = dataDI;
        this.dataIS = dataIS;
    }
}
