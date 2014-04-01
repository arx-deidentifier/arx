package org.deidentifier.arx.io.importdata;

import java.util.ArrayList;
import java.util.List;

public abstract class DataSourceConfiguration {

    /**
     * List of columns to be imported
     *
     * Each element of this list represents a column to import from. Refer to
     * {@link Column} for details.
     *
     * @note Only columns that are part of this list will be imported from,
     * any other column will simply be ignored and not be returned.
     */
    protected List<Column> columns = new ArrayList<Column>();

    /**
     * Adds a column actually import from
     *
     * @param column A column
     */
    public void addColumn(Column column) throws Exception {
        this.columns.add(column);
    }

    /**
     * Returns all columns
     * @return
     */
    public List<Column> getColumns() {
        return columns;
    }
}
