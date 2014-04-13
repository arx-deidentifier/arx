/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * Copyright (C) 2014 Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.io.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base configuration
 *
 * Such a configuration contains characteristics that are needed to access
 * the data. This abstract superclass defines properties that all of them have
 * in common, i.e. a notion of columns, which can be added and retrieved.
 */
public abstract class Configuration {

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

        for (Column c : columns) {

            if (column.getIndex() == c.getIndex()) {

                throw new IllegalArgumentException("Column for this index already assigned");

            }

            if (column.getAliasName() != null && c.getAliasName() != null &&
                c.getAliasName().equals(column.getAliasName())) {

                throw new IllegalArgumentException("Column names need to be unique");

            }

        }

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
