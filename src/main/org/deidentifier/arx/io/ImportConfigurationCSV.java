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


/**
 * Configuration describing a CSV file.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportConfigurationCSV extends ImportConfigurationFile implements IImportConfigurationWithHeader {

    /** Default values */
    private static final char DEFAULT_DELIMITER = ';';
    /** Default values */
    private static final char DEFAULT_QUOTE     = '\"';
    /** Default values */
    private static final char DEFAULT_ESCAPE    = '\"';

    /** Character that separates the columns from each other. */
    private final char        delimiter;

    /** Character that delimits strings. */
    private final char        quote;

    /** Character that escapes. */
    private final char        escape;

    /**
     * Indicates whether first row contains header (names of columns).
     * 
     * @see {@link IImportConfigurationWithHeader}
     */
    private boolean     containsHeader;

    /**
     * Creates a new instance of this object.
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param containsHeader {@link #containsHeader}
     */
    public ImportConfigurationCSV(String fileLocation,
                                  boolean containsHeader) {
        this(fileLocation, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE, containsHeader);
    }
    /**
     * Creates a new instance of this object.
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param delimiter {@link #separator}
     * @param containsHeader {@link #containsHeader}
     */
    public ImportConfigurationCSV(String fileLocation,
                                  char delimiter,
                                  boolean containsHeader) {
        this(fileLocation, delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE, containsHeader);
    }
    
    /**
     * Creates a new instance of this object.
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param delimiter {@link #delimiter}
     * @param quote {@link #quote}
     * @param containsHeader {@link #containsHeader}
     */
    public ImportConfigurationCSV(String fileLocation,
                                  char delimiter,
                                  char quote,
                                  boolean containsHeader) {
        this(fileLocation, delimiter, quote, DEFAULT_ESCAPE, containsHeader);
    }

    /**
     * Creates a new instance of this object.
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param delimiter {@link #delimiter}
     * @param quote {@link #quote}
     * @param escape {@link #escape}
     * @param containsHeader {@link #containsHeader}
     */
    public ImportConfigurationCSV(String fileLocation,
                                  char delimiter,
                                  char quote,
                                  char escape,
                                  boolean containsHeader) {

        setFileLocation(fileLocation);
        this.quote = quote;
        this.delimiter = delimiter;
        this.escape = escape;
        this.containsHeader = containsHeader;
    }
    
    /**
     * Adds a single column to import from
     * 
     * This makes sure that only {@link ImportColumnCSV} can be added, otherwise
     * an {@link IllegalArgumentException} will be thrown.
     * 
     * @param column
     *            A single column to import from, {@link ImportColumnCSV}
     */
    @Override
    public void addColumn(ImportColumn column) {

        if (!(column instanceof ImportColumnCSV)) {
            throw new IllegalArgumentException("Column needs to be of type CSVColumn");
        }
        
        if (!((ImportColumnCSV) column).isIndexSpecified() && 
            !this.getContainsHeader()){
            final String ERROR = "Adressing columns by name is only possible if the source contains a header";
            throw new IllegalArgumentException(ERROR);
        }

        for (ImportColumn c : columns) {
            if (((ImportColumnCSV) column).isIndexSpecified() &&
                ((ImportColumnCSV) column).getIndex() == ((ImportColumnCSV) c).getIndex()) {
                throw new IllegalArgumentException("Column for this index already assigned");
            }
            
            if (!((ImportColumnCSV) column).isIndexSpecified() &&
                    ((ImportColumnCSV) column).getName().equals(((ImportColumnCSV) c).getName())) {
                    throw new IllegalArgumentException("Column for this name already assigned");
            }

            if (column.getAliasName() != null && c.getAliasName() != null &&
                c.getAliasName().equals(column.getAliasName())) {
                throw new IllegalArgumentException("Column names need to be unique");
            }
        }

        this.columns.add(column);
    }

    /**
     * @return {@link #containsHeader}
     */
    @Override
    public boolean getContainsHeader() {
        return containsHeader;
    }

    /**
     * @return {@link #delimiter}
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * @return {@link #quote}
     */
    public char getQuote() {
        return quote;
    }

    /**
     * @return {@link #quote}
     */
    public char getEscape() {
        return escape;
    }
    
    /**
     * Sets the indexes based on the header.
     *
     * @param row
     */
    public void prepare(String[] row) {

        for (ImportColumn c : super.getColumns()) {
            ImportColumnCSV column = (ImportColumnCSV) c;
            if (!column.isIndexSpecified()) {
                boolean found = false;
                for (int i = 0; i < row.length; i++) {
                    if (row[i].equals(column.getName())) {
                        found = true;
                        column.setIndex(i);
                    }
                }
                if (!found) {
                    throw new IllegalArgumentException("Index for column '" + column.getName() + "' couldn't be found");
                }
            }
        }
    }

    /**
     * @param containsHeader
     *            {@link #containsHeader}
     */
    @Override
    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }
}
