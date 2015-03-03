/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * Configuration describing a JDBC source.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportConfigurationJDBC extends ImportConfiguration {

    /**
     * Connection to be used.
     *
     * @see {@link #setConnection(Connection)}
     * @see {@link #getConnection()}
     */
    private Connection connection;

    /**
     * Name of table to be used.
     *
     * @see {@link #setTable(String)}
     * @see {@link #getTable()}
     */
    private String     table;

    /**
     * Creates a new instance of this object.
     *
     * @param connection {@link #setConnection(Connection)}
     * @param table {@link #setTable(String)}
     */
    public ImportConfigurationJDBC(Connection connection, String table) {
        setConnection(connection);
        setTable(table);
    }

    /**
     * Creates a new instance of this object.
     *
     * @param url
     * @param table {@link #setTable(String)}
     * @throws SQLException
     */
    public ImportConfigurationJDBC(String url, String table) throws SQLException {
        setConnection(DriverManager.getConnection(url));
        setTable(table);
    }

    /**
     * Creates a new instance of this object.
     *
     * @param url
     * @param user
     * @param password
     * @param table {@link #setTable(String)}
     * @throws SQLException
     */
    public ImportConfigurationJDBC(String url, String user, String password, String table) throws SQLException {
        setConnection(DriverManager.getConnection(url, user, password));
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

    /**
     * 
     *
     * @param aliasName
     * @return
     * @throws NoSuchElementException
     */
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
