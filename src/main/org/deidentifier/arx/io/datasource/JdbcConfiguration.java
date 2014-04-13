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

import java.sql.Connection;


/**
 * Configuration describing a JDBC source
 */
public class JdbcConfiguration extends DataSourceConfiguration {


    /**
     * Reference to connecting
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
    private String table;


    /**
     * Creates a new instance of this object
     *
     * @param connection {@link #setConnection(Connection)}
     * @param table {@link #setTab
     */
    public JdbcConfiguration(Connection connection, String table)
    {

        setConnection(connection);
        setTable(table);

    }

    /**
     * Sets the connection
     *
     * @param connection {@link #setConnection(Connection)}
     */
    public void setConnection(Connection connection)
    {

        this.connection = connection;

    }

    /**
     * Retrieves the used connection
     *
     * @return {@link #connection}
     */
    public Connection getConnection()
    {

        return connection;

    }

    /**
     * Sets the table
     *
     * @param table {@link #setTable(String)}
     */
    public void setTable(String table)
    {

        this.table = table;

    }

    /**
     * Retrieves the used table
     *
     * @return {@link #table}
     */
    public String getTable()
    {

        return table;

    }

}
