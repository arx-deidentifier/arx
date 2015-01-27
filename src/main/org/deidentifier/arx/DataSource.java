/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
 * or via a JDBC connection.
 *
 * @author Fabian Prasser
 */
public class DataSource {
    
    /**
     * Creates a CSV data source.
     *
     * @param file
     * @param separator
     * @param containsHeader
     * @return
     */
    public static DataSource createCSVSource(File file, char separator, boolean containsHeader) {
        return new DataSource(file, separator, containsHeader);
    }
    
    /**
     * Creates a CSV data source.
     *
     * @param file
     * @param separator
     * @param containsHeader
     * @return
     */
    public static DataSource createCSVSource(String file, char separator, boolean containsHeader) {
        return createCSVSource(new File(file), separator, containsHeader);
    }

    /**
     * Creates an Excel data source.
     *
     * @param file
     * @param sheetIndex
     * @param containsHeader
     * @return
     */
    public static DataSource createExcelSource(File file, int sheetIndex, boolean containsHeader) {
        return new DataSource(file, sheetIndex, containsHeader);
    }

    /**
     * Creates an Excel data source.
     *
     * @param file
     * @param sheetIndex
     * @param containsHeader
     * @return
     */
    public static DataSource createExcelSource(String file, int sheetIndex, boolean containsHeader) {
        return createExcelSource(new File(file), sheetIndex, containsHeader);
    }


    /**
     * Creates a JDBC data source.
     *
     * @param url
     * @param table
     * @return
     * @throws SQLException
     */
    public static DataSource createJDBCSource(String url, String table) throws SQLException {
        return new DataSource(url, table);
    }

    /**
     * Creates a JDBC data source.
     *
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
    
    /** The config. */
    private final ImportConfiguration config;

    /**
     * Creates a CSV source.
     *
     * @param file
     * @param separator
     * @param containsHeader
     */
    private DataSource(File file, char separator, boolean containsHeader) {
        config = new ImportConfigurationCSV(file.getAbsolutePath(), separator, containsHeader);
    }
    
    /**
     * Creates an Excel source.
     *
     * @param file
     * @param sheetIndex
     * @param containsHeader
     */
    private DataSource(File file, int sheetIndex, boolean containsHeader) {
        config = new ImportConfigurationExcel(file.getAbsolutePath(), sheetIndex, containsHeader);
    }
    
    /**
     * Creates a JDBC data source.
     *
     * @param url
     * @param table
     * @throws SQLException
     */
    private DataSource(String url, String table) throws SQLException {
        config = new ImportConfigurationJDBC(url, table);
    }
    
    /**
     * Creates a JDBC data source.
     *
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
     * Adds a new column.
     *
     * @param index
     */
    public void addColumn(int index) {
        addColumn(index, DataType.STRING);
    }
    
    /**
     * Adds a new column.
     *
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
     * Adds a new column.
     *
     * @param index
     * @param alias
     */
    public void addColumn(int index, String alias) {
        addColumn(index, alias, DataType.STRING);
    }
    
    /**
     * Adds a new column.
     *
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
     * Adds a new column.
     *
     * @param name
     */
    public void addColumn(String name) {
        addColumn(name, DataType.STRING);
    }
    
    /**
     * Adds a new column.
     *
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
     * Adds a new column.
     *
     * @param name
     * @param alias
     */
    public void addColumn(String name, String alias) {
        addColumn(name, alias, DataType.STRING);
    }
    
    /**
     * Adds a new column.
     *
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
     * Returns the configuration.
     *
     * @return
     */
    protected ImportConfiguration getConfiguration(){
        return config;
    }
}
