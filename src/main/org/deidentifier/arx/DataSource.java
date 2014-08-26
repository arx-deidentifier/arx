/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx;

import java.io.File;
import java.sql.SQLException;

import org.deidentifier.arx.io.ImportColumnCSV;
import org.deidentifier.arx.io.ImportColumnExcel;
import org.deidentifier.arx.io.ImportColumnJDBC;
import org.deidentifier.arx.io.ImportConfiguration;
import org.deidentifier.arx.io.ImportConfigurationCSV;
import org.deidentifier.arx.io.ImportConfigurationExcel;
import org.deidentifier.arx.io.ImportConfigurationJDBC;

/**
 * This class provides configuration options for importing data from CSV-files, from Excel-files
 * or via a JDBC connection
 * @author Fabian Prasser
 */
public class DataSource {
    
    /**
     * Creates a CSV data source
     * @param file
     * @param separator
     * @param containsHeader
     * @return
     */
    public static DataSource createCSVSource(File file, char separator, boolean containsHeader) {
        return new DataSource(file, separator, containsHeader);
    }
    
    /**
     * Creates a CSV data source
     * @param file
     * @param separator
     * @param containsHeader
     * @return
     */
    public static DataSource createCSVSource(String file, char separator, boolean containsHeader) {
        return createCSVSource(new File(file), separator, containsHeader);
    }

    /**
     * Creates an Excel data source
     * @param file
     * @param sheetIndex
     * @param containsHeader
     * @return
     */
    public static DataSource createExcelSource(File file, int sheetIndex, boolean containsHeader) {
        return new DataSource(file, sheetIndex, containsHeader);
    }

    /**
     * Creates an Excel data source
     * @param file
     * @param sheetIndex
     * @param containsHeader
     * @return
     */
    public static DataSource createExcelSource(String file, int sheetIndex, boolean containsHeader) {
        return createExcelSource(new File(file), sheetIndex, containsHeader);
    }


    /**
     * Creates a JDBC data source
     * @param url
     * @param table
     * @return
     * @throws SQLException
     */
    public static DataSource createJDBCSource(String url, String table) throws SQLException {
        return new DataSource(url, table);
    }

    /**
     * Creates a JDBC data source
     * @param url
     * @param user
     * @param password
     * @param table
     * @return
     * @throws SQLException
     */
    public static DataSource createJDBCSource(String url, String user, String password, String table) throws SQLException {
        return new DataSource(url, user, password, table);
    }
    
    /** The config*/
    private final ImportConfiguration config;

    /**
     * Creates a CSV source
     * @param file
     * @param separator
     * @param containsHeader
     */
    private DataSource(File file, char separator, boolean containsHeader) {
        config = new ImportConfigurationCSV(file.getAbsolutePath(), separator, containsHeader);
    }
    
    /**
     * Creates an Excel source
     * @param file
     * @param sheetIndex
     * @param containsHeader
     */
    private DataSource(File file, int sheetIndex, boolean containsHeader) {
        config = new ImportConfigurationExcel(file.getAbsolutePath(), sheetIndex, containsHeader);
    }
    
    /**
     * Creates a JDBC data source
     * @param url
     * @param table
     * @throws SQLException 
     */
    private DataSource(String url, String table) throws SQLException {
        config = new ImportConfigurationJDBC(url, table);
    }
    
    /**
     * Creates a JDBC data source
     * @param url
     * @param user
     * @param password
     * @param table
     * @throws SQLException 
     */
    private DataSource(String url, String user, String password, String table) throws SQLException {
        config = new ImportConfigurationJDBC(url, user, password, table);
    }
    
    /**
     * Adds a new column
     * @param index
     */
    public void addColumn(int index) {
        addColumn(index, DataType.STRING);
    }
    
    /**
     * Adds a new column
     * @param index
     * @param datatype
     */
    public void addColumn(int index, DataType<?> datatype) {
        if (config instanceof ImportConfigurationCSV){
            config.addColumn(new ImportColumnCSV(index, datatype));
        } else if (config instanceof ImportConfigurationExcel){
            config.addColumn(new ImportColumnExcel(index, datatype));
        } else if (config instanceof ImportConfigurationJDBC){
            config.addColumn(new ImportColumnJDBC(index, datatype));
        }
    }
    
    /**
     * Adds a new column
     * @param index
     * @param alias
     */
    public void addColumn(int index, String alias) {
        addColumn(index, alias, DataType.STRING);
    }
    
    /**
     * Adds a new column
     * @param index
     * @param alias
     * @param datatype
     */
    public void addColumn(int index, String alias, DataType<?> datatype) {
        if (config instanceof ImportConfigurationCSV){
            config.addColumn(new ImportColumnCSV(index, alias, datatype));
        } else if (config instanceof ImportConfigurationExcel){
            config.addColumn(new ImportColumnExcel(index, alias, datatype));
        } else if (config instanceof ImportConfigurationJDBC){
            config.addColumn(new ImportColumnJDBC(index, alias, datatype));
        }
    }
    
    /**
     * Adds a new column
     * @param name
     */
    public void addColumn(String name) {
        addColumn(name, DataType.STRING);
    }
    
    /**
     * Adds a new column
     * @param name
     * @param datatype
     */
    public void addColumn(String name, DataType<?> datatype) {
        if (config instanceof ImportConfigurationCSV){
            config.addColumn(new ImportColumnCSV(name, datatype));
        } else if (config instanceof ImportConfigurationExcel){
            config.addColumn(new ImportColumnExcel(name, datatype));
        } else if (config instanceof ImportConfigurationJDBC){
            config.addColumn(new ImportColumnJDBC(name, datatype));
        }
    }
    
    /**
     * Adds a new column
     * @param name
     * @param alias
     */
    public void addColumn(String name, String alias) {
        addColumn(name, alias, DataType.STRING);
    }
    
    /**
     * Adds a new column
     * @param name
     * @param alias
     * @param datatype
     */
    public void addColumn(String name, String alias, DataType<?> datatype) {
        if (config instanceof ImportConfigurationCSV){
            config.addColumn(new ImportColumnCSV(name, alias, datatype));
        } else if (config instanceof ImportConfigurationExcel){
            config.addColumn(new ImportColumnExcel(name, alias, datatype));
        } else if (config instanceof ImportConfigurationJDBC){
            config.addColumn(new ImportColumnJDBC(name, alias, datatype));
        }
    }
    
    /**
     * Returns the configuration
     * @return
     */
    protected ImportConfiguration getConfiguration(){
        return config;
    }
}
