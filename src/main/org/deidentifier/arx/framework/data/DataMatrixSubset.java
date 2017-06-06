/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.framework.data;


/**
 * A subset of a data matrix
 * 
 * @author Fabian Prasser
 */
public class DataMatrixSubset extends DataMatrix {

    /** Subset */
    private final int[]      subset;

    /** Matrix */
    private final DataMatrix matrix;
    
    /**
     * Creates a new instance
     * @param matrix
     * @param subset
     */
    public DataMatrixSubset(DataMatrix matrix, int[] subset) {
        super(0, 0);
        this.subset = subset;
        this.matrix = matrix;
    }

    @Override
    public void and(int row, long value) {
        matrix.and(subset[row], value);
    }

    @Override
    public DataMatrix clone() {
        return matrix.clone(this.subset);
    }

    @Override
    public boolean equals(int row1, int row2) {
        return matrix.equals(subset[row1], subset[row2]);
    }

    @Override
    public boolean equals(int row, int[] data) {
        return matrix.equals(subset[row], data);
    }
    
    @Override
    public boolean equalsIgnoringOutliers(int row1, int row2) {
        return matrix.equalsIgnoringOutliers(subset[row1], subset[row2]);
    }

    @Override
    public void free() {
        // Nothing to do
    }

    @Override
    public int get(int row, int col) {
        return matrix.get(subset[row], col);
    }

    @Override
    public int getNumColumns() {
        return matrix.getNumColumns();
    }

    @Override
    public int getNumRows() {
        return subset.length;
    }

    @Override
    public int getValueAtColumn(int column) {
        return matrix.getValueAtColumn(column);
    }

    @Override
    public int hashCode(int row) {
        return matrix.hashCode(subset[row]);
    }

    @Override
    public void iterator1(int row) {
        matrix.iterator1(subset[row]);
    }

    @Override
    public boolean iterator1_hasNext() {
        return matrix.iterator1_hasNext();
    }

    @Override
    public int iterator1_next() {
        return matrix.iterator1_next();
    }

    @Override
    public void iterator1_write(int value) {
        matrix.iterator1_write(value);
    }

    @Override
    public void iterator2(int row) {
        matrix.iterator2(subset[row]);
    }

    @Override
    public boolean iterator2_hasNext() {
        return matrix.iterator2_hasNext();
    }

    @Override
    public int iterator2_next() {
        return matrix.iterator2_next();
    }

    @Override
    public void iterator2_write(int value) {
        matrix.iterator2_write(value);
    }

    @Override
    public void or(int row, long value) {
        matrix.or(subset[row], value);
    }

    @Override
    public void set(int row, int column, int value) {
        matrix.set(subset[row], column, value);
    }

    @Override
    public void setRow(int row) {
       matrix.setRow(this.subset[row]);
    }
    
    @Override
    public void setRow(int row, int[] data) {
        matrix.setRow(subset[row], data);
    }

    @Override
    public void setValueAtColumn(int column, int value) {
        matrix.setValueAtColumn(column, value);
    }
    @Override
    public void swap(int row1, int row2) {
        matrix.swap(subset[row1], subset[row2]);
    }

    @Override
    protected void finalize() throws Throwable {
        // Nothing to do
    }
}
