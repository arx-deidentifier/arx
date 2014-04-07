package org.deidentifier.arx.io;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.io.importdata.Column;

/**
 * Abstract base configuration
 *
 * Such a configuration contains characteristics that are needed to access
 * the data. This abstract superclass defines properties that all of them have
 * in common, i.e. a notion of columns, which can be added and retrieved.
 */
public abstract class DataSourceConfiguration {

    /**
     * List of columns to be imported
     *
     * Each element of this list represents a single column to import from.
     * Refer to {@link Column} for details. Columns can be added by invoking
     * {@link #addColumn(Column)} and retrieved by {@link #getColumns()}.
     *
     * @note Only columns that are part of this list will be imported from,
     * any other column will simply be ignored.
     */
    protected List<Column> columns = new ArrayList<Column>();


    /**
     * Adds a single column to import from
     *
     * @param column A single column to import from
     */
    public void addColumn(Column column) {

        this.columns.add(column);

    }

    /**
     * Returns all added columns
     *
     * @return {@link #columns}
     */
    public List<Column> getColumns() {

        return columns;

    }

}
