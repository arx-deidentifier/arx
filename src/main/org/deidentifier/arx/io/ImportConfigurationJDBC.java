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

package org.deidentifier.arx.io;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * Configuration describing a JDBC source
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportConfigurationJDBC extends ImportConfiguration {

    /**
     * Connection to be used
     * 
     * @see {@link #setConnection(Connection)}
     * @see {@link #getConnection()}
     */
    private Connection connection;

    /**
     * Name of table to be used
     * 
     * @see {@link #setTable(String)}
     * @see {@link #getTable()}
     */
    private String     table;

    /**
     * Creates a new instance of this object
     * 
     * @param connection
     *            {@link #setConnection(Connection)}
     * @param table
     *            {@link #setTable(String)}
     */
    public ImportConfigurationJDBC(Connection connection, String table) {
        setConnection(connection);
        setTable(table);
    }

    /**
     * Adds a single column to import from
     * 
     * This makes sure that only {@link ImportColumnJDBC} can be added,
     * otherwise an {@link IllegalArgumentException} will be thrown.
     * 
     * @param column
     *            A single column to import from, {@link ImportColumnJDBC}
     */
    @Override
    public void addColumn(ImportColumn column) {

        if (!(column instanceof ImportColumnJDBC)) {
            throw new IllegalArgumentException("");
        }

        if (((ImportColumnJDBC) column).getIndex() == -1) {
            int index = getIndexForColumn(((ImportColumnJDBC) column).getName());
            ((ImportColumnJDBC) column).setIndex(index);
        }

        for (ImportColumn c : columns) {

            if (((ImportColumnJDBC) column).getIndex() == ((ImportColumnJDBC) c).getIndex()) {
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
     * @return {@link #connection}
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return {@link #table}
     */
    public String getTable() {
        return table;
    }

    /**
     * @param connection
     *            {@link #setConnection(Connection)}
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param table
     *            {@link #setTable(String)}
     */
    public void setTable(String table) {
        this.table = table;
    }

    private int getIndexForColumn(String aliasName) throws NoSuchElementException {
        try {

            ResultSet rs = connection.getMetaData().getColumns(null,
                                                               null,
                                                               table,
                                                               null);

            int i = 0;
            while (rs.next()) {
                if (rs.getString("COLUMN_NAME").equals(aliasName)) {
                    return i;
                }
                i++;
            }
        } catch (SQLException e) {
            /* Catch silently*/
        }
        throw new NoSuchElementException("Index for column '" + aliasName +
                                         "' couldn't be found");

    }
}
