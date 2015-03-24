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

import org.deidentifier.arx.DataHandleStatistics.InterruptHandler;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;


/**
 * This implementation of a data handle projects a given data handle onto a given research subset.
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataHandleSubset extends DataHandle {
    
    /** The original data handle. */
    private final DataHandle source;
    
    /** The research subset. */
    private final DataSubset subset;
    
    /**
     * Creates a new handle that represents the research subset.
     *
     * @param source
     * @param subset
     * @param eqStatistics
     */
    public DataHandleSubset(DataHandle source, DataSubset subset, StatisticsEquivalenceClasses eqStatistics) {
        this.source = source;
        this.dataTypes = source.dataTypes;
        this.definition = source.definition;
        this.header = source.header;
        this.subset = subset;
        this.statistics = new StatisticsBuilder(new DataHandleStatistics(this), eqStatistics);
    }

    /**
     * Creates a new handle that represents the research subset.
     *
     * @param source
     * @param subset
     */
    protected DataHandleSubset(DataHandle source, DataSubset subset){
        this(source, subset, null);
    }

    @Override
    public String getAttributeName(int col) {
        checkRegistry();
        return source.getAttributeName(col);
    }
    
    @Override
    public DataType<?> getDataType(String attribute) {
        return source.getDataType(attribute);
    }

    @Override
    public int getGeneralization(String attribute) {
        checkRegistry();
        return source.getGeneralization(attribute);
    }

    @Override
    public int getNumColumns() {
        checkRegistry();
        return source.getNumColumns();
    }

    @Override
    public int getNumRows() {
        checkRegistry();
        return this.subset.getArray().length;
    }

    /**
     * Returns the research subset.
     *
     * @return
     */
    public int[] getSubset() {
        checkRegistry();
        return this.subset.getArray();
    }

    @Override
    public String getValue(int row, int col) {
        checkRegistry();
        return source.getValue(this.subset.getArray()[row], col);
    }

    @Override
    public DataHandle getView(){
        checkRegistry();
        return this;
    }

    @Override
    public Iterator<String[]> iterator() {
        checkRegistry();
        return new Iterator<String[]>() {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index < subset.getArray().length);
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
                throw new UnsupportedOperationException("Remove is unsupported!");
            }
        };
    }

    @Override
    public boolean replace(int column, String original, String replacement) {
        throw new UnsupportedOperationException("This operation is not supported by handles for data subsets");
    }

    @Override
    protected void doRelease() {
        // Nothing to do
    }

    @Override
    protected DataType<?>[][] getDataTypeArray() {
        return source.dataTypes;
    }

    @Override
    protected String[] getDistinctValues(int column, InterruptHandler handler) {

        // Check
        checkRegistry();
        checkColumn(column);

        final Set<String> vals = new HashSet<String>();
        for (int i = 0; i < getNumRows(); i++) {
            handler.checkInterrupt();
            vals.add(getValue(i, column));
        }
        handler.checkInterrupt();
        return vals.toArray(new String[vals.size()]);
    }

    /**
     * Returns the underlying source data handle.
     *
     * @return
     */
    protected DataHandle getSource(){
        return source;
    }

    @Override
    protected String getSuppressionString(){
        return source.getSuppressionString();
    }

    @Override
    protected int internalCompare(int row1, int row2, int[] columns, boolean ascending) {
        return source.internalCompare(this.subset.getArray()[row1], this.subset.getArray()[row2], columns, ascending);
    }
    
    @Override
    protected String internalGetValue(int row, int col) {
        return source.internalGetValue(this.subset.getArray()[row], col);
    }

    /**
     * Rebuild array representation of subset.
     */
    protected void internalRebuild() {
        int index = 0;
        for (int i = 0; i < subset.getSet().length(); i++) {
            if (this.subset.getSet().contains(i)) {
                this.subset.getArray()[index++] = i;
            }
        }
    }

    @Override
    protected boolean internalReplace(int column,
                                      String original,
                                      String replacement) {
        return source.internalReplace(column, original, replacement);
    }

    /**
     * Swaps the bits in the set representation.
     *
     * @param row1
     * @param row2
     */
    protected void internalSwap(int row1, int row2) {
        this.subset.getSet().swap(row1, row2);
    }

    /**
     * Translates the row number.
     *
     * @param row
     * @return
     */
    protected int internalTranslate(int row) {
        return this.subset.getArray()[row];
    }
}
