package org.deidentifier.arx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This implementation of a data handle projects a given data handle onto a given research subset.
 * @author Prasser, Kohlmayer
 */
public class DataHandleSubset extends DataHandle {
    
    /** The original data handle*/
    private final DataHandle source;
    
    /** The research subset*/
    private final int[] subset;
    
    /**
     * Creates a new handle that represents the research subset
     * @param source
     * @param subset
     */
    public DataHandleSubset(DataHandle source, int[] subset){
        this.source = source;
        this.dataTypes = source.dataTypes;
        this.definition = source.definition;
        this.header = source.header;
        this.other = source.other;
        this.subset = subset;
        createDataTypeArray();
    }

    @Override
    protected void createDataTypeArray() {
        this.dataTypes = source.dataTypes;
    }

    @Override
    public String getAttributeName(int col) {
        return source.getAttributeName(col);
    }

    @Override
    public String[] getDistinctValues(int column) {

        // Check
        checkColumn(column);

        // TODO: Inefficient
        final Set<String> vals = new HashSet<String>();
        for (int i = 0; i < getNumRows(); i++) {
            vals.add(getValue(i, column));
        }
        return vals.toArray(new String[vals.size()]);
    }

    @Override
    public int getGeneralization(String attribute) {
        return source.getGeneralization(attribute);
    }

    @Override
    public int getNumColumns() {
        return source.getNumColumns();
    }

    @Override
    public int getNumRows() {
        return this.subset.length;
    }

    @Override
    public String getValue(int row, int col) {
        return source.getValue(this.subset[row], col);
    }

    @Override
    protected String getValueInternal(int row, int col) {
        return source.getValueInternal(this.subset[row], col);
    }

    @Override
    protected boolean isOutlierInternal(int row) {
        return source.isOutlierInternal(this.subset[row]);
    }

    @Override
    protected int compare(int row1, int row2, int[] columns, boolean ascending) {
        return source.compare(this.subset[row1], this.subset[row2], columns, ascending);
    }

    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index < subset.length);
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
    public void swap(int row1, int row2) {
        this.source.swap(subset[row1], subset[row2]);
    }

    @Override
    protected void swapInternal(int row1, int row2) {
        this.source.swapInternal(subset[row1], subset[row2]);
    }

    @Override
    public DataHandle getView(ARXConfiguration config){
        return this;
    }
}
