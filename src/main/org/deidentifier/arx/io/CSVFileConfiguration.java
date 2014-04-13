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

import org.deidentifier.arx.io.datasource.CSVColumn;
import org.deidentifier.arx.io.datasource.Column;
import org.deidentifier.arx.io.datasource.FileConfiguration;
import org.deidentifier.arx.io.datasource.ICanContainHeader;


/**
 * Configuration describing a CSV file
 */
public class CSVFileConfiguration extends FileConfiguration implements ICanContainHeader {

    /**
     * Character that separates the columns from each other
     */
    private char separator;

    /**
     * Indicates whether first row contains header (names of columns)
     *
     * @see {@link ICanContainHeader}
     */
    private boolean containsHeader;


    /**
     * Creates a new instance of this object
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param separator {@link #separator}
     * @param containsHeader {@link #containsHeader}
     */
    public CSVFileConfiguration(String fileLocation, char separator, boolean containsHeader) {

        setFileLocation(fileLocation);
        this.separator = separator;
        this.containsHeader = containsHeader;

    }

    /**
     * @return {@link #separator}
     */
    public char getSeparator() {

        return separator;

    }

    @Override
    public boolean getContainsHeader() {

        return containsHeader;

    }

    @Override
    public void setContainsHeader(boolean containsHeader)
    {

        this.containsHeader = containsHeader;

    }

    /**
     * Adds a single column to import from
     *
     * @param column A single column to import from
     */
    @Override
    public void addColumn(Column column) {

        if (!(column instanceof CSVColumn)) {

            throw new IllegalArgumentException("");

        }

        for (Column c : columns) {

            if (((CSVColumn) column).getIndex() == ((CSVColumn) c).getIndex()) {

                throw new IllegalArgumentException("Column for this index already assigned");

            }

            if (column.getAliasName() != null && c.getAliasName() != null &&
                c.getAliasName().equals(column.getAliasName())) {

                throw new IllegalArgumentException("Column names need to be unique");

            }

        }

        this.columns.add(column);

    }

}
