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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelHistogram;

import cern.colt.Swapper;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * This class provides access to dictionary encoded data. Furthermore, the data
 * is linked to the associated input or output data. This means that, e.g., if
 * the input data is sorted, the output data will be sorted accordingly. This
 * ensures that original tuples and their generalized counterpart will always
 * have the same row index, which is important for many use cases, e.g., for
 * graphical tools that allow to compare the original dataset to generalized
 * versions.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class DataHandle {

    /** The data types. */
    protected DataType<?>[]                columnToDataType = null;

    /** The data definition. */
    protected DataDefinition               definition       = null;

    /** The header. */
    protected String[]                     header           = null;

    /** The header. */
    protected ObjectIntOpenHashMap<String> headerMap        = null;

    /** The node. */
    protected ARXNode                      node             = null;

    /** The current registry. */
    protected DataRegistry                 registry         = null;

    /** The current research subset. */
    protected DataHandle                   subset           = null;
    
    /**
     * Returns the name of the specified column.
     *
     * @param col The column index
     * @return the attribute name
     */
    public abstract String getAttributeName(int col);

    /**
     * Returns the index of the given attribute, -1 if it is not in the header.
     *
     * @param attribute the attribute
     * @return the column index of
     */
    public int getColumnIndexOf(final String attribute) {
        checkRegistry();
        return headerMap.getOrDefault(attribute, -1);
    }

    /**
     * Returns the according data type.
     *
     * @param attribute the attribute
     * @return the data type
     */
    public DataType<?> getDataType(final String attribute) {
        checkRegistry();
        return definition.getDataType(attribute);
    }

    /**
     * Returns a date/time value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the date
     * @throws ParseException the parse exception
     */
    public Date getDate(int row, int col) throws ParseException {
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDate) {
            return ((ARXDate) type).parse(value);
        } else {
            throw new ParseException("Invalid datatype: " + type.getClass().getSimpleName(), col);
        }
    }
    
    /**
     * Returns the data definition.
     *
     * @return the definition
     */
    public DataDefinition getDefinition() {
        checkRegistry();
        return definition;
    }

    /**
     * Returns an array containing the distinct values in the given column.
     *
     * @param column The column to process
     * @return the distinct values
     */
    public final String[] getDistinctValues(int column) {
        return getDistinctValues(column, false, new InterruptHandler() {
            @Override
            public void checkInterrupt() {
                // Nothing to do
            }
        });
    }

    /**
     * Returns a double value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the double
     * @throws ParseException the parse exception
     */
    public Double getDouble(int row, int col) throws ParseException {
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDecimal) {
            return ((ARXDecimal) type).parse(value);
        } else if (type instanceof ARXInteger) {
            Long _long = ((ARXInteger) type).parse(value);
            return _long == null ? null : _long.doubleValue();
        } else {
            throw new ParseException("Invalid datatype: " + type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns a float value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the float
     * @throws ParseException the parse exception
     */
    public Float getFloat(int row, int col) throws ParseException {
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXDecimal) {
            Double _double = ((ARXDecimal) type).parse(value);
            return _double == null ? null : _double.floatValue();
        } else if (type instanceof ARXInteger) {
            Long _long = ((ARXInteger) type).parse(value);
            return _long == null ? null : _long.floatValue();
        } else {
            throw new ParseException("Invalid datatype: " + type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns the generalization level for the attribute.
     *
     * @param attribute the attribute
     * @return the generalization
     */
    public abstract int getGeneralization(String attribute);

    /**
     * Returns an int value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the int
     * @throws ParseException the parse exception
     */
    public Integer getInt(int row, int col) throws ParseException {
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXInteger) {
            Long _long = ((ARXInteger) type).parse(value);
            return _long == null ? null : _long.intValue();
        } else {
            throw new ParseException("Invalid datatype: " + type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns a long value from the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the long
     * @throws ParseException the parse exception
     */
    public Long getLong(int row, int col) throws ParseException {
        String value = getValue(row, col);
        DataType<?> type = getDataType(getAttributeName(col));
        if (type instanceof ARXInteger) {
            return ((ARXInteger) type).parse(value);
        } else {
            throw new ParseException("Invalid datatype: " + type.getClass().getSimpleName(), col);
        }
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type.
     * This method uses the default locale.
     * This method only returns types that match at least 80% of all values in the column .
     *
     * @param column the column
     * @return the matching data types
     */
    public List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column) {
        return getMatchingDataTypes(column, Locale.getDefault(), 0.8d);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type for a given wrapped class.
     * This method uses the default locale.
     * This method only returns types that match at least 80% of all values in the column .
     *
     * @param <U> the generic type
     * @param column the column
     * @param clazz The wrapped class
     * @return the matching data types
     */
    public <U> List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Class<U> clazz) {
        return getMatchingDataTypes(column, clazz, Locale.getDefault(), 0.8d);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type for a given wrapped class.
     * This method uses the default locale.
     *
     * @param <U> the generic type
     * @param column the column
     * @param clazz The wrapped class
     * @param threshold Relative minimal number of values that must match to include a data type in the results
     * @return the matching data types
     */
    public <U> List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Class<U> clazz, double threshold) {
        return getMatchingDataTypes(column, clazz, Locale.getDefault(), threshold);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type for a given wrapped class.
     * This method only returns types that match at least 80% of all values in the column .
     *
     * @param <U> the generic type
     * @param column the column
     * @param clazz The wrapped class
     * @param locale The locale to use
     * @return the matching data types
     */
    public <U> List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Class<U> clazz, Locale locale) {
        return getMatchingDataTypes(column, clazz, locale, 0.8d);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type for a given wrapped class.
     *
     * @param <U> the generic type
     * @param column the column
     * @param clazz The wrapped class
     * @param locale The locale to use
     * @param threshold Relative minimal number of values that must match to include a data type in the results
     * @return the matching data types
     */
    public <U> List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Class<U> clazz, Locale locale, double threshold) {

        checkRegistry();
        checkColumn(column);
        double distinct = this.getDistinctValues(column).length;
        List<Pair<DataType<?>, Double>> result = new ArrayList<Pair<DataType<?>, Double>>();
        DataTypeDescription<U> description = DataType.list(clazz);
        if (description == null) {
            return result;
        }
        if (description.hasFormat()) {
            for (String format : description.getExampleFormats()) {
                DataType<U> type = description.newInstance(format, locale);
                double matching = getNumConformingValues(column, type) / distinct;
                if (matching >= threshold) {
                    result.add(new Pair<DataType<?>, Double>(type, matching));
                }
            }
        } else {
            DataType<U> type = description.newInstance();
            double matching = getNumConformingValues(column, type) / distinct;
            if (matching >= threshold) {
                result.add(new Pair<DataType<?>, Double>(type, matching));
            }
        }
        return result;
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type.
     * This method uses the default locale.
     *
     * @param column the column
     * @param threshold Relative minimal number of values that must match to include a data type in the results
     * @return the matching data types
     */
    public List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, double threshold) {
        return getMatchingDataTypes(column, Locale.getDefault(), threshold);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type
     * This method only returns types that match at least 80% of all values in the column .
     *
     * @param column the column
     * @param locale The locale to use
     * @return the matching data types
     */
    public List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Locale locale) {
        return getMatchingDataTypes(column, locale, 0.8d);
    }

    /**
     * Returns a mapping from data types to the relative number of values that conform to the according type.
     *
     * @param column the column
     * @param locale The locale to use
     * @param threshold Relative minimal number of values that must match to include a data type in the results
     * @return the matching data types
     */
    public List<Pair<DataType<?>, Double>> getMatchingDataTypes(int column, Locale locale, double threshold) {

        checkRegistry();
        checkColumn(column);
        List<Pair<DataType<?>, Double>> result = new ArrayList<Pair<DataType<?>, Double>>();
        result.addAll(getMatchingDataTypes(column, Long.class, locale, threshold));
        result.addAll(getMatchingDataTypes(column, Date.class, locale, threshold));
        result.addAll(getMatchingDataTypes(column, Double.class, locale, threshold));
        result.add(new Pair<DataType<?>, Double>(DataType.STRING, 1.0d));
        
        // Sort order
        final Map<Class<?>, Integer> order = new HashMap<Class<?>, Integer>();
        order.put(Long.class, 0);
        order.put(Date.class, 1);
        order.put(Double.class, 2);
        order.put(String.class, 3);
        
        // Sort
        Collections.sort(result, new Comparator<Pair<DataType<?>, Double>>() {
            public int compare(Pair<DataType<?>, Double> o1, Pair<DataType<?>, Double> o2) {
                
                // Sort by matching quality
                int cmp = o1.getSecond().compareTo(o2.getSecond());
                if (cmp != 0) return -cmp;
                
                // Sort by order
                int order1 = order.get(o1.getFirst().getDescription().getWrappedClass());
                int order2 = order.get(o2.getFirst().getDescription().getWrappedClass());
                return Integer.compare(order1, order2);
            }
        });
        return result;
    }

    /**
     * Returns a set of values that do not conform to the given data type.
     *
     * @param column The column to test
     * @param type The type to test
     * @param max The maximal number of values returned by this method
     * @return the non conforming values
     */
    public String[] getNonConformingValues(int column, DataType<?> type, int max) {
        checkRegistry();
        checkColumn(column);
        Set<String> result = new HashSet<String>();
        for (String value : this.getDistinctValues(column)) {
            if (!type.isValid(value)) {
                result.add(value);
            }
            if (result.size() == max) {
                break;
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the number of columns in the dataset.
     *
     * @return the num columns
     */
    public abstract int getNumColumns();

    /**
     * Returns the number of (distinct) values that conform to the given data type.
     *
     * @param column The column to test
     * @param type The type to test
     * @return the num conforming values
     */
    public int getNumConformingValues(int column, DataType<?> type) {
        checkRegistry();
        checkColumn(column);
        int count = 0;
        for (String value : this.getDistinctValues(column)) {
            count += type.isValid(value) ? 1 : 0;
        }
        return count;
    }

    /**
     * Returns the number of rows in the dataset.
     *
     * @return the num rows
     */
    public abstract int getNumRows();

    /**
     * Returns a risk estimator, using the US population if required
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator() {
        return getRiskEstimator(ARXPopulationModel.create(Region.USA), getDefinition().getQuasiIdentifyingAttributes());
    }

    /**
     * Returns a risk estimator
     * @param model
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model) {
        return getRiskEstimator(model, getDefinition().getQuasiIdentifyingAttributes());
    }

    /**
     * Returns a risk estimator
     * @param model
     * @param config
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model, ARXSolverConfiguration config) {
        return getRiskEstimator(model, getDefinition().getQuasiIdentifyingAttributes(), config);
    }
    
    /**
     * Returns a risk estimator for the given set of equivalence classes. Saves resources by re-using existing classes
     * @param model
     * @param classes
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model, RiskModelHistogram classes) {
        return new RiskEstimateBuilder(model, new DataHandleInternal(this), classes, getConfiguration());
    }

    /**
     * Returns a risk estimator for the given set of equivalence classes. Saves resources by re-using existing classes
     * @param model
     * @param classes
     * @param config
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model, RiskModelHistogram classes, ARXSolverConfiguration config) {
        return new RiskEstimateBuilder(model, new DataHandleInternal(this), classes, config, getConfiguration());
    }

    /**
     * Returns a risk estimator for the given set of quasi-identifiers
     * @param model
     * @param qis
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model, Set<String> qis) {
        return new RiskEstimateBuilder(model, new DataHandleInternal(this), qis, getConfiguration());
    }

    /**
     * Returns a risk estimator for the given set of quasi-identifiers
     * @param model
     * @param qis
     * @param config
     * @return
     */
    public RiskEstimateBuilder getRiskEstimator(ARXPopulationModel model, Set<String> qis, ARXSolverConfiguration config) {
        return new RiskEstimateBuilder(model, new DataHandleInternal(this), qis, config, getConfiguration());
    }

    /**
     * Returns an object providing access to basic descriptive statistics about the data represented
     * by this handle.
     *
     * @return the statistics
     */
    public abstract StatisticsBuilder getStatistics();

    /**
     * Returns the transformation .
     *
     * @return the transformation
     */
    public ARXNode getTransformation() {
        return node;
    }

    /**
     * Returns the value in the specified cell.
     *
     * @param row The cell's row index
     * @param col The cell's column index
     * @return the value
     */
    public abstract String getValue(int row, int col);

    /**
     * Returns a new data handle that represents a context specific view on the dataset.
     *
     * @return the view
     */
    public DataHandle getView() {
        checkRegistry();
        if (subset == null) {
            return this;
        } else {
            return subset;
        }
    }

    /**
     * Has this handle been optimized with local recoding?
     * @return
     */
    public boolean isOptimized() {
        checkRegistry();
        return false;
    }

    /**
     * Determines whether this handle is orphaned, i.e., should not be used anymore
     *
     * @return true, if is orphaned
     */
    public boolean isOrphaned() {
        return registry == null;
    }

    /**
     * Determines whether a given row is an outlier in the currently associated
     * data transformation.
     *
     * @param row the row
     * @return true, if is outlier
     */
    public boolean isOutlier(int row) {
        checkRegistry();
        return registry.isOutlier(this, row);
    }

    /**
     * Returns an iterator over the data.
     *
     * @return the iterator
     */
    public abstract Iterator<String[]> iterator();

    /**
     * Releases this handle and all associated resources. If a input handle is released all associated results are released
     * as well.
     */
    public void release() {
        if (registry != null) {
            registry.release(this);
        }
    }

    /**
     * Renders this object
     * @return
     */
    public ElementData render() {
        ElementData data = new ElementData("Data");
        data.addProperty("Records", this.getNumRows());
        data.addProperty("Attributes", this.getNumColumns());
        return data;
    }

    /**
     * Replaces the original value with the replacement in the given column. Only supported by
     * handles for input data.
     *
     * @param column the column
     * @param original the original
     * @param replacement the replacement
     * @return Whether the original value was found
     */
    public boolean replace(int column, String original, String replacement) {
        checkRegistry();
        checkColumn(column);
        if (!getDataType(getAttributeName(column)).isValid(replacement)) {
            throw new IllegalArgumentException("Value does'nt match the attribute's data type");
        }
        for (String s : getDistinctValues(column)) {
            if (s.equals(replacement)) {
                throw new IllegalArgumentException("Value is already contained in the data set");
            }
        }
        return registry.replace(column, original, replacement);
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final File file) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(file);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param file A file
     * @param separator The utilized separator character
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final File file, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(file, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final File file, final CSVSyntax config) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(file, config);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final OutputStream out) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(out);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param out Output stream
     * @param separator The utilized separator character
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final OutputStream out, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(out, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param out the out
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final OutputStream out, final CSVSyntax config) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(out, config);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param path the path
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final String path) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(path);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param path A path
     * @param separator The utilized separator character
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final String path, final char separator) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(path, separator);
        output.write(iterator());
    }

    /**
     * Writes the data to a CSV file.
     *
     * @param path the path
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save(final String path, final CSVSyntax config) throws IOException {
        checkRegistry();
        final CSVDataOutput output = new CSVDataOutput(path, config);
        output.write(iterator());
    }

    /**
     * Sorts the dataset according to the given columns. Will sort input and
     * output analogously.
     *
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns and the given range.
     * Will sort input and output analogously.
     *
     * @param from The lower bound
     * @param to The upper bound
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(int from, int to, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, from, to, ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns. Will sort input and
     * output analogously.
     *
     * @param swapper A swapper
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(Swapper swapper, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, swapper, ascending, columns);
    }

    /**
     * Sorts the dataset according to the given columns and the given range.
     * Will sort input and output analogously.
     *
     * @param swapper A swapper
     * @param from The lower bound
     * @param to The upper bound
     * @param ascending Sort ascending or descending
     * @param columns An integer array containing column indicides
     */
    public void sort(Swapper swapper, int from, int to, boolean ascending, int... columns) {
        checkRegistry();
        registry.sort(this, swapper, from, to, ascending, columns);
    }

    /**
     * Swaps both rows.
     *
     * @param row1 the row1
     * @param row2 the row2
     */
    public void swap(int row1, int row2) {
        checkRegistry();
        registry.swap(this, row1, row2);
    }

    /**
     * Checks a column index.
     *
     * @param column1 the column1
     */
    protected void checkColumn(final int column1) {
        if ((column1 < 0) || (column1 > (header.length - 1))) {
            throw new IndexOutOfBoundsException("Column index out of range: " + column1 + ". Valid: 0 - " + (header.length - 1));
        }
    }

    /**
     * Checks the column indexes.
     *
     * @param columns the columns
     */
    protected void checkColumns(final int[] columns) {

        // Check
        if ((columns.length == 0) || (columns.length > header.length)) {
            throw new IllegalArgumentException("Invalid number of column indices");
        }

        // Create a sorted copy of the input columns
        final int[] cols = new int[columns.length];
        System.arraycopy(columns, 0, cols, 0, cols.length);
        Arrays.sort(cols);

        // Check
        for (int i = 0; i < cols.length; i++) {
            checkColumn(cols[i]);
            if ((i > 0) && (cols[i] == cols[i - 1])) {
                throw new IllegalArgumentException("Duplicate column index");
            }
        }
    }

    /**
     * Checks whether a registry is referenced.
     */
    protected void checkRegistry() {
        if (registry == null) {
            throw new RuntimeException("This data handle (" + this.getClass().getSimpleName() + "@" +
                                       hashCode() + ") is orphaned");
        }
    }

    /**
     * Checks a row index.
     *
     * @param row1 the row1
     * @param length the length
     */
    protected void checkRow(final int row1, final int length) {
        if ((row1 < 0) || (row1 > length)) {
            throw new IndexOutOfBoundsException("Row index (" + row1 + ") out of range (0 <= row <= " + length + ")");
        }
    }

    /**
     * Releases all resources.
     */
    protected abstract void doRelease();

    /**
     * Returns the base data type without generalization.
     *
     * @param attribute the attribute
     * @return the base data type
     */
    protected DataType<?> getBaseDataType(final String attribute) {
        checkRegistry();
        return getRegistry().getBaseDataType(attribute);
    }

    /**
     * Returns the ARXConfiguration that is currently being used, null if this is an input handle
     * @return
     */
    protected abstract ARXConfiguration getConfiguration();

    /**
     * Returns a raw data array needed for some functionalities. Suppressed records will not be included.
     * @param columns Columns to include
     * @return
     */
    protected DataArray getDataArray(int[] columns) {
        return this.getDataArray(columns, null);
    }

    /**
     * Returns a raw data array needed for some functionalities. Suppressed records will not be included.
     * @param columns Columns to include
     * @param rows Rows to include. Can be null.
     * @return
     */
    protected abstract DataArray getDataArray(int[] columns, int[] rows);

    /**
     * Generates an array of data types.
     *
     * @return the data type array
     */
    protected abstract DataType<?>[] getColumnToDataType();

    /**
     * Returns the distinct values.
     *
     * @param column the column
     * @param ignoreSuppression
     * @param handler the handler
     * @return the distinct values
     */
    protected abstract String[] getDistinctValues(int column, boolean ignoreSuppression, InterruptHandler handler);

    /**
     * Returns the registry associated with this handle.
     *
     * @return the registry
     */
    protected DataRegistry getRegistry() {
        return registry;
    }

    /**
     * Returns the internal value identifier
     * @param column
     * @param value
     * @return
     */
    protected abstract int getValueIdentifier(int column, String value);

    /**
     * A negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. It uses the specified
     * data types for comparison. If no datatype is specified for a specific
     * column it uses string comparison.
     *
     * @param row1 the row1
     * @param row2 the row2
     * @param columns the columns
     * @param ascending the ascending
     * @return the int
     */
    protected int internalCompare(final int row1,
                                  final int row2,
                                  final int[] columns,
                                  final boolean ascending) {

        checkRegistry();
        try {
            for (int i = 0; i < columns.length; i++) {

                int index = columns[i];
                int cmp = columnToDataType[index].compare(internalGetValue(row1, index, false),
                                                   internalGetValue(row2, index, false));
                if (cmp != 0) {
                    return ascending ? cmp : -cmp;
                }
            }
            return 0;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Internal representation of get encoded value. Returns -1 for suppressed values.
     *
     * @param row the row
     * @param col the col
     * @return the value
     */
    protected abstract int internalGetEncodedValue(int row, int col, boolean ignoreSuppression);

    /**
     * Internal representation of get value.
     *
     * @param row the row
     * @param col the col
     * @return the string
     */
    protected abstract String internalGetValue(int row, int col, boolean ignoreSuppression);

    /**
     * Internal replacement method.
     *
     * @param column the column
     * @param original the original
     * @param replacement the replacement
     * @return true, if successful
     */
    protected abstract boolean internalReplace(int column, String original, String replacement);

    /**
     * Returns whether the data represented by this handle is anonymous
     * @return
     */
    protected boolean isAnonymous() {
        return false;
    }

    /**
     * Sets the current header
     * @param header
     */
    protected void setHeader(String[] header) {
        this.header = header;
        this.headerMap = new ObjectIntOpenHashMap<String>();
        for (int i = 0; i < header.length; i++) {
            headerMap.put(header[i], i);
        }
    }

    /**
     * Updates the registry.
     *
     * @param registry the new registry
     */
    protected void setRegistry(DataRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Sets the subset.
     *
     * @param handle the new view
     */
    protected void setView(DataHandle handle) {
        subset = handle;
    }
}
