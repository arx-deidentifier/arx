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

package org.deidentifier.arx.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class implements a reader for CSV encoded information.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class CSVDataInput extends CSVAbstractInput {

    /** The number of columns in the dataset. */
    private int                  columns     = -1;

    /** A reader. */
    private final BufferedReader reader;

    /** Size of the buffer. */
    private static final int     BUFFER_SIZE = 1024 * 1024;

    /**
     * Instantiate.
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
     * Instantiate.
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
     * Instantiate.
     *
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final String file, final char separator) throws IOException {
        super(separator);
        reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.io.CSVAbstractInput#close()
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Counts the number of columns.
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.io.CSVAbstractInput#readRow()
     */
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
            if (index < 0) {
                throw new IOException("Each line must have at least ("+tuple.length+") columns");
            }
            tuple[column++] = line.substring(offset, index);
            offset = index + 1;
        }
        tuple[column] = line.substring(offset);

        // Return
        return tuple;
    }
}
