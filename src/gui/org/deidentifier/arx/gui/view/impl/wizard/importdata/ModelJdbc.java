/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ModelJdbc implements Serializable {

    private static final long serialVersionUID = 1922111530152010271L;

    private Connection con = null;

    public boolean isConnected()
    {

        return con == null;

    }

    public boolean connect(String type, String server, String port,
                    String username, String password, String database)
    {

        switch (type) {

            case "mysql":

                try {

                    Class.forName("com.mysql.jdbc.Driver");

                } catch (ClassNotFoundException e) {

                }

                break;

        }

        try {

            con = DriverManager.getConnection("jdbc:" + type + "://" + server
                            + ":" + port + "/" + database, username, password);

        } catch (SQLException e) {

            return false;

        }

        return true;

    }

    public ArrayList<String> getTables() throws SQLException
    {

        ArrayList<String> tables = new ArrayList<String>();

        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);

        while (rs.next()) {

            tables.add(rs.getString("TABLE_NAME"));

        }

        return tables;

    }

    public ArrayList<String> getColumns(String table) throws SQLException
    {

        ArrayList<String> columns = new ArrayList<String>();

        ResultSet rs = con.getMetaData().getColumns(null, null, table, null);

        while (rs.next()) {

            columns.add(rs.getString("COLUMN_NAME"));

        }

        return columns;

    }

}
