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

import java.util.NoSuchElementException;

/**
 * Configuration describing a CSV file
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportConfigurationCSV extends ImportConfigurationFile implements
        IImportConfigurationWithHeader {

    /**
     * Character that separates the columns from each other
     */
    private char    separator;

    /**
     * Indicates whether first row contains header (names of columns)
     * 
     * @see {@link IImportConfigurationWithHeader}
     */
    private boolean containsHeader;

    /**
     * Creates a new instance of this object
     * 
     * @param fileLocation
     *            {@link #setFileLocation(String)}
     * @param separator
     *            {@link #separator}
     * @param containsHeader
     *            {@link #containsHeader}
     */
    public ImportConfigurationCSV(String fileLocation,
                                  char separator,
                                  boolean containsHeader) {

        setFileLocation(fileLocation);
        this.separator = separator;
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
     * @return {@link #separator}
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @param containsHeader
     *            {@link #containsHeader}
     */
    @Override
    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }

    /**
     * Sets the indexes based on the header
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
                    throw new NoSuchElementException("Index for column '" + column.getName() + "' couldn't be found");
                }
            }
        }
    }
}
