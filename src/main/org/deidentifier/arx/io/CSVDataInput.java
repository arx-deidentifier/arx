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

    /** The number of columns in the data set. */
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
        super(separator, '\"');
        reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final File file, final char separator, final char stringdelimiter) throws IOException {
        super(separator, stringdelimiter);
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
        super(separator, '\"');
        reader = new BufferedReader(new InputStreamReader(stream), BUFFER_SIZE);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char separator, final char stringdelimiter) throws IOException {
        super(separator, stringdelimiter);
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
        super(separator, '\"');
        reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVDataInput(final String file, final char separator, final char stringdelimiter) throws IOException {
        super(separator, stringdelimiter);
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

    /**
     * Reads a line from the input
     * @return
     * @throws IOException
     */
    private String readLine() throws IOException {
        
        // Read a line, ignoring empty lines
        String line = "";
        while (line != null && line.length()==0) {
            line = reader.readLine();
        }

        // Extract number of columns
        if (columns == -1) {
            columns = countColumns(line);
        }

        // Return
        return line;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.io.CSVAbstractInput#readRow()
     */
    @Override
    protected String[] readRow() throws IOException {

        // Read a line
        String line = readLine();
        
        // Check for EOF
        if (line == null) {
            reader.close();
            return null;
        }

        // Extract tuple
        final String[] tuple = new String[columns];
        int column = 0;
        int offset = 0;
        int index = 0;
        while (column < (columns - 1)) {
            
            // Read the next line in case of line breaks
            index = line.indexOf(separator, offset);
            while (index < 0) {
                offset = 0;
                line = readLine();
                if (line == null) {
                    throw new IOException("Schema mismatch: too few columns");
                }
                index = line.indexOf(separator, offset);
            }
            
            // Extract 
            String value = line.substring(offset, index);
            offset = index + 1;
            
            // If starts with an escape character, read on
            if (value.length() > 0 && value.charAt(0) == delimiter && value.charAt(value.length() - 1) != delimiter) {
                StringBuilder builder = new StringBuilder();
                builder.append(value);
                while (builder.charAt(builder.length() - 1) != delimiter) {
                    index = line.indexOf(separator, offset);
                    while (index < 0) {
                        offset = 0;
                        line = readLine();
                        if (line == null) { 
                            throw new IOException("Schema mismatch: too few columns"); 
                        }
                        index = line.indexOf(separator, offset);
                    }
                    builder.append(line.substring(offset, index));
                    offset = index + 1;
                }
                value = builder.toString();
            }
            
            // Store
            tuple[column++] = value;
        }
        
        // Store remainder
        String value = line.substring(offset);
        tuple[column] = value;
        
        // Check if end of line equals end of tuple
        if (value.indexOf(separator) >= 0 && !(value.charAt(0) == delimiter && value.charAt(value.length()-1) == delimiter)) {
            
            throw new IOException("Schema mismatch: too many columns");
        }

        // Return
        return tuple;
    }
}