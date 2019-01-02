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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.ImportColumn;
import org.deidentifier.arx.io.ImportColumnIndexed;
import org.deidentifier.arx.io.ImportColumnJDBC;

/**
 * Stores all of the data gathered by the wizard and offers means to access it
 * 
 * This object is accessible to all pages of the wizard and can be used to store
 * and/or retrieve data. It is mainly used to exchange data between multiple
 * wizard pages.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardModel {

    /**
     * Possible sources for importing data from.
     *
     * @see {@link sourceType}
     */
    public enum SourceType {
        
        /**  Constant */
        CSV,
        
        /**  Constant */
        JDBC,
        
        /**  Constant */
        EXCEL
    }

    /** Maximum number of lines to be loaded for preview purposes. */
    public static final int               PREVIEW_MAX_LINES      = 25;

    /** Maximum number of chars to be loaded for detecting separators. */
    public static final int               DETECT_MAX_CHARS       = 100000;

    /** Actual source data should be imported from. */
    private SourceType                    sourceType;

    /**
     * List of columns used throughout the wizard
     * 
     * This contains a list of all the columns that were detected and is used
     * through the whole wizard. Note however, that not all of the columns here
     * will necessarily be imported from, as columns can be disabled on an
     * individual basis {@link ImportWizardModelColumn#setEnabled(boolean)}.
     * 
     * Each column itself is represented by {@link ImportWizardModelColumn}.
     */
    private List<ImportWizardModelColumn> wizardColumns;

    /** Location of file to import from. */
    private String                        fileLocation;

    /** Line break characters (in case of CSV import). */
    private char[]                        csvLinebreak;

    /** Escape character (in case of CSV import). */
    private char                          csvEscape;

    /** Separator for columns (in case of CSV import). */
    private char                          csvDelimiter;
                                          
    /** Character to enclose strings (in case of CSV import). */
    private char                          csvQuote;
                                          
    /** The charset of the file */
    private Charset                       charset = Charset.defaultCharset();
                                          
    /**
     * Indicates whether first row contains header
     * 
     * In case of CSV and XLS files the first row might contain a header
     * describing the column. This makes sure that the appropriate row is not
     * considered to be data.
     */
    private boolean                       firstRowContainsHeader = true;

    /**
     * Index of sheet to import from (in case of Excel import).
     *
     * @see {@link SourceType#EXCEL}
     */
    private int                           excelSheetIndex;

    /**
     * Preview data
     * 
     * For reasons of simplicity and performance data is imported rather early
     * on in the wizard and stored here. This makes sure that
     * {@link ImportWizardPagePreview} doesn't need to know anything about the
     * source type the data is coming from.
     * 
     * It will contain up to {@link #PREVIEW_MAX_LINES} lines of data.
     */
    private List<String[]>                previewData;

    /** List of potential JDBC tables. */
    private List<String>                  jdbcTables;

    /** Name of table selected by user. */
    private String                        selectedJdbcTable;

    /** Jdbc connection potentially used throughout the wizard. */
    private Connection                    jdbcConnection;

    /** The locale */
    private Locale                        locale;

    /** Should we perform cleansing */
    private boolean                       performCleansing       = true;

    /**
     * Creates a new instance
     * @param model
     */
    public ImportWizardModel(Model model) {
        this.locale = model.getLocale();
    }

    /**
     * Returns the charset
     * @return
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * @return the csvDelimiter
     */
    public char getCsvDelimiter() {
        return csvDelimiter;
    }

    /**
     * @return {@link #csvEscape}
     */
    public char getCsvEscape() {
        return csvEscape;
    }

    /**
     * Getter
     * @return
     */
    public char[] getCsvLinebreak() {
        return csvLinebreak;
    }

    /**
     * Getter
     * @return
     */
    public char getCsvQuote() {
        return csvQuote;
    }

    /**
     * Returns list of enabled columns
     * 
     * This iterates over {@link #wizardColumns} and returns only the columns
     * that are enabled {@link ImportWizardModelColumn#isEnabled()}. Columns
     * that have been disabled by the user will not be returned.
     * 
     * @return {@link ImportColumn} List of enabled columns
     */
    public List<ImportColumn> getEnabledColumns() {

        List<ImportColumn> result = new ArrayList<ImportColumn>();
        for (ImportWizardModelColumn column : wizardColumns) {
            if (column.isEnabled()) {
                result.add(column.getColumn());
            }
        }
        return result;
    }

    /**
     * @return {@link #excelSheetIndex}
     */
    public int getExcelSheetIndex() {

        return excelSheetIndex;

    }

    /**
     * @return {@link #fileLocation}
     */
    public String getFileLocation() {

        return fileLocation;
    }

    /**
     * @return {@link #firstRowContainsHeader}
     */
    public boolean getFirstRowContainsHeader() {

        return firstRowContainsHeader;
    }

    /**
     * @return {@link #jdbcConnection}
     */
    public Connection getJdbcConnection() {

        return jdbcConnection;
    }

    /**
     * @return {@link #jdbcTables}
     */
    public List<String> getJdbcTables() {

        return jdbcTables;
    }
    
    /**
     * Returns a list of matching data types
     * @param column
     */
    public List<Pair<DataType<?>, Double>> getMatchingDataTypes(ImportWizardModelColumn column) {
        
        if (wizardColumns.indexOf(column) == -1) { 
            throw new IllegalArgumentException(Resources.getMessage("ImportWizardModel.0"));  //$NON-NLS-1$
        }

        Data data = Data.create(getPreviewData());
        int columnIndex = -1;
        ImportColumn c = column.getColumn();
        if (c instanceof ImportColumnIndexed) {
            columnIndex =  ((ImportColumnIndexed) column.getColumn()).getIndex();
        } else if (column.getColumn() instanceof ImportColumnJDBC){
            columnIndex = ((ImportColumnJDBC) column.getColumn()).getIndex();
        }
        
        return data.getHandle().getMatchingDataTypes(columnIndex, locale, 0d);
    }

    /**
     * @return {@link #previewData}
     */
    public List<String[]> getPreviewData() {

        return previewData;
    }

    /**
     * @return {@link #selectedJdbcTable}
     */
    public String getSelectedJdbcTable() {

        return selectedJdbcTable;
    }

    /**
     * @return {@link #sourceType}
     */
    public SourceType getSourceType() {

        return sourceType;
    }

    /**
     * @return {@link #wizardColumns}
     */
    public List<ImportWizardModelColumn> getWizardColumns() {

        return wizardColumns;
    }

    /**
     * @return the performCleansing
     */
    public boolean isPerformCleansing() {
        return performCleansing;
    }
    
    /**
     * Sets the charset
     * @param charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * @param csvDelimiter the csvDelimiter to set
     */
    public void setCsvDelimiter(char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    /**
     * 
     * @param csvEscape
     */
    public void setCsvEscape(char csvEscape) {
        this.csvEscape = csvEscape;
    }

    /**
     * Setter
     * @param csvLinebreak
     */
    public void setCsvLinebreak(char[] csvLinebreak) {
        this.csvLinebreak = csvLinebreak;
    }

    /**
     * Setter
     * @param csvQuote
     */
    public void setCsvQuote(char csvQuote) {
        this.csvQuote = csvQuote;
    }

    /**
     * @param excelSheetIndex
     *            {@link #excelSheetIndex}
     */
    public void setExcelSheetIndex(int excelSheetIndex) {

        this.excelSheetIndex = excelSheetIndex;
    }

    /**
     * @param fileLocation
     *            {@link #fileLocation}
     */
    public void setFileLocation(String fileLocation) {

        this.fileLocation = fileLocation;
    }

    /**
     * @param firstRowContainsHeader
     *            {@link #firstRowContainsHeader}
     */
    public void setFirstRowContainsHeader(boolean firstRowContainsHeader) {

        this.firstRowContainsHeader = firstRowContainsHeader;
    }

    /**
     * 
     *
     * @param jdbcConnection
     */
    public void setJdbcConnection(Connection jdbcConnection) {

        this.jdbcConnection = jdbcConnection;
    }

    /**
     * @param jdbcTables
     *            {@link #jdbcTables}
     */
    public void setJdbcTables(List<String> jdbcTables) {

        this.jdbcTables = jdbcTables;
    }

    /**
     * @param performCleansing the performCleansing to set
     */
    public void setPerformCleansing(boolean performCleansing) {
        this.performCleansing = performCleansing;
    }

    /**
     * 
     *
     * @param previewData
     */
    public void setPreviewData(List<String[]> previewData) {

        this.previewData = previewData;
    }

    /**
     * @param selectedJdbcTable
     *            {@link #selectedJdbcTable}
     */
    public void setSelectedJdbcTable(String selectedJdbcTable) {

        this.selectedJdbcTable = selectedJdbcTable;
    }

    /**
     * Setter
     * @param sourceType
     */
    public void setSourceType(SourceType sourceType) {

        this.sourceType = sourceType;
    }
    
    /**
     * @param columns
     *            {@link #wizardColumns}
     */
    public void setWizardColumns(List<ImportWizardModelColumn> columns) {

        this.wizardColumns = columns;
    }
}
