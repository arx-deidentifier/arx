package org.deidentifier.arx;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.arx.DataStatistics.EquivalenceClassStatistics;


/**
 * This implementation of a data handle projects a given data handle onto a given research subset.
 * @author Prasser, Kohlmayer
 */
public class DataHandleSubset extends DataHandle {
    
    /** The original data handle*/
    private final DataHandle source;
    
    /** The research subset*/
    private final DataSubset subset;
    
    /**
     * Creates a new handle that represents the research subset
     * @param source
     * @param subset
     */
    protected DataHandleSubset(DataHandle source, DataSubset subset){
        this(source, subset, null);
    }

    /**
     * Creates a new handle that represents the research subset
     * @param source
     * @param subset
     * @param eqStatistics
     */
    public DataHandleSubset(DataHandle source, DataSubset subset, EquivalenceClassStatistics eqStatistics) {
        this.source = source;
        this.dataTypes = source.dataTypes;
        this.definition = source.definition;
        this.header = source.header;
        this.subset = subset;
        // TODO: How about such statistics for the subset?
        this.statistics = new DataStatistics(this, eqStatistics);
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
    public String[] getDistinctValues(int column) {

        // Check
        checkRegistry();
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
     * Returns the research subset
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
    protected DataType<?>[][] getDataTypeArray() {
        return source.dataTypes;
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
     * Rebuild array representation of subset
     */
    protected void internalRebuild() {
        int index = 0;
        for (int i = 0; i < subset.getSet().length(); i++) {
            if (this.subset.getSet().contains(i)) {
                this.subset.getArray()[index++] = i;
            }
        }
    }

    /**
     * Swaps the bits in the set representation
     * @param row1
     * @param row2
     */
    protected void internalSwap(int row1, int row2) {
        this.subset.getSet().swap(row1, row2);
    }
    
    /**
     * Translates the row number
     * @param row
     * @return
     */
    protected int internalTranslate(int row) {
        return this.subset.getArray()[row];
    }
    
    /**
     * Returns the underlying source data handle
     * @return
     */
    protected DataHandle getSource(){
        return source;
    }

    @Override
    protected void doRelease() {
        // Nothing to do
    }
}
