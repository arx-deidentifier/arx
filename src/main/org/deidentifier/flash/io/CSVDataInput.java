/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class implements a reader for CSV encoded information
 * 
 * @author Prasser, Kohlmayer
 */
public class CSVDataInput extends CSVAbstractInput {

    /** The number of columns in the dataset */
    private int                  columns     = -1;

    /** A reader */
    private final BufferedReader reader;

    /** Size of the buffer */
    private static final int     BUFFER_SIZE = 1024 * 1024;

    /**
     * Instantiate
     * 
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final File file, final char separator) throws IOException {
        super(separator);
        reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
    }

    /**
     * Instantiate
     * 
     * @param stream
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char separator) throws IOException {
        super(separator);
        reader = new BufferedReader(new InputStreamReader(stream), BUFFER_SIZE);
    }

    /**
     * Instantiate
     * 
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final String file, final char separator) throws IOException {
        super(separator);
        reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Counts the number of columns
     * 
     * @param line
     * @return
     */
    private int countColumns(final String line) {
        int result = 0;
        int offset = 0;
        int index = 0;
        while (index != -1) {
            index = line.indexOf(separator, offset);
            offset = index + 1;
            result++;
        }
        return result;
    }

    @Override
    protected String[] readRow() throws IOException {

        // Read
        final String line = reader.readLine();
        if (line == null) {
            reader.close();
            return null;
        }

        // Extract num columns
        if (columns == -1) {
            columns = countColumns(line);
        }

        // Extract tuple
        final String[] tuple = new String[columns];
        int column = 0;
        int offset = 0;
        int index = 0;
        while (column < (columns - 1)) {
            index = line.indexOf(separator, offset);
            tuple[column++] = line.substring(offset, index);
            offset = index + 1;
        }
        tuple[column] = line.substring(offset);

        // Return
        return tuple;
    }
}
