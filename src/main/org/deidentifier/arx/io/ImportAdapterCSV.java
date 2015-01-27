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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.input.CountingInputStream;

/**
 * Import adapter for CSV files
 * 
 * This adapter can import data from a CSV file. The CSV file itself is
 * described by an appropriate {@link ImportConfigurationCSV} object. Internally
 * this class makes use of {@link CSVDataInput} to read the CSV file on a line
 * by line basis. A counting input stream (@link CountingInputStream} is used in
 * order for {@link #getProgress() to be able to return the percentage of data
 * that has already been processed.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportAdapterCSV extends ImportAdapter {

    /** The configuration describing the CSV file being used. */
    private ImportConfigurationCSV config;

    /** The size of the CSV file. */
    private long                   bytesTotal;

    /**
     * Counting input stream
     * 
     * This is used within {@link #getProgress()} to be able to know how many
     * bytes have already been processed.
     */
    private CountingInputStream    cin;

    /**
     * @see {@link CSVDataInput}
     */
    private CSVDataInput           in;

    /**
     * Actual iterator used to go through data within CSV file.
     *
     * @see {@link CSVDataInput#iterator()}
     */
    private Iterator<String[]>     it;

    /**
     * Contains the last row as returned by {@link CSVDataInput#iterator()}.
     *
     * @note This row cannot be simply returned, but needs to be further
     *       processed, e.g. to return only selected columns.
     */
    private String[]               row;

    /**
     * Indicates whether the first row has already been returned
     * 
     * The first row contains the name of the columns. Depending upon
     * {@link #containsHeader} and whether the name of the column has been
     * assigned explicitly, this is either the value of the file itself, the
     * value defined by the user, or a default value.
     */
    private boolean                headerReturned = false;

    /**
     * Creates a new instance of this object with given configuration.
     *
     * @param config {@link #config}
     * @throws IOException In case file doesn't contain actual data
     */
    protected ImportAdapterCSV(ImportConfigurationCSV config) throws IOException {

        super(config);
        this.config = config;
        this.bytesTotal = new File(config.getFileLocation()).length();

        /* Used to keep track of progress */
        cin = new CountingInputStream(new FileInputStream(new File(config.getFileLocation())));

        /* Get CSV iterator */
        in = new CSVDataInput(cin, config.getSeparator());
        it = in.iterator();

        /* Check whether there is actual data within the CSV file */
        if (it.hasNext()) {
            row = it.next();
            if (config.getContainsHeader()) {
                if (!it.hasNext()) {
                    throw new IOException("CSV contains nothing but header");
                }
            }
        } else {
            throw new IOException("CSV file contains no data");
        }
        
        // Create header
        header = createHeader();
    }

    /**
     * Returns the percentage of data that has already been returned
     * 
     * This divides the amount of bytes that have already been read by the
     * amount of total bytes and casts the result into a percentage.
     *
     * @return
     */
    @Override
    public int getProgress() {

        /* Check whether stream has been opened already at all */
        if (cin == null) {
            return 0;
        }

        long bytesRead = cin.getByteCount();
        return (int) ((double) bytesRead / (double) bytesTotal * 100d);
    }

    /**
     * Indicates whether there is another element to return
     * 
     * This returns true when the CSV file has another line, which would be
     * assigned to {@link #row} during the last iteration of {@link #next()}.
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return row != null;
    }

    /**
     * Returns the next row
     * 
     * The returned element is sorted as defined by {@link ImportColumn#index} and contains as many elements as there are columns selected to import
     * from {@link #indexes}. The first row will always contain the names of the
     * columns. {@link #headerReturned} is used to keep track of that.
     *
     * @return
     */
    @Override
    public String[] next() {

        /* Check whether header was already returned */
        if (!headerReturned) {
            headerReturned = true;
            return header;
        }

        /* Create regular row */
        String[] result = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = row[indexes[i]];
            if (!dataTypes[i].isValid(result[i])) {
                throw new IllegalArgumentException("Data value does not match data type");
            }
        }

        /* Fetches the next row, which will be used in next iteration */
        if (it.hasNext()) {
            row = it.next();
        } else {
            row = null;
        }

        /* Return resulting row */
        return result;
    }

    /**
     * Dummy.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the header row
     * 
     * This returns a string array with the names of the columns that will be
     * returned later on by iterating over this object. Depending upon the
     * configuration {@link ImportConfigurationCSV#getContainsHeader()} and
     * whether or not names have been assigned explicitly either the appropriate
     * values will be returned, or names will be made up on the fly following
     * the pattern "Column #x", where x is incremented for each column.
     *
     * @return
     */
    private String[] createHeader() {
        
        /* Preparation work */
        if (config.getContainsHeader()) this.config.prepare(row);
        this.indexes = getIndexesToImport();
        this.dataTypes = getColumnDatatypes();

        /* Initialization */
        String[] header = new String[config.getColumns().size()];
        List<ImportColumn> columns = config.getColumns();

        /* Create header */
        for (int i = 0, len = columns.size(); i < len; i++) {

            ImportColumn column = columns.get(i);

            /* Check whether there is a header, which is not empty */
            if (config.getContainsHeader() &&
                !row[((ImportColumnCSV) column).getIndex()].equals("")) {
                
                /* Assign name of CSV file itself */
                header[i] = row[((ImportColumnCSV) column).getIndex()];
            } else {
                /* Nothing defined in header (or empty), build name manually */
                header[i] = "Column #" + ((ImportColumnCSV) column).getIndex();
            }

            if (column.getAliasName() != null) {
                /* Name has been assigned explicitly */
                header[i] = column.getAliasName();
            }
            column.setAliasName(header[i]);
        }

        /* Fetch next row in preparation for next iteration */
        if (config.getContainsHeader()) {
            if (it.hasNext()) {
                row = it.next();
            } else {
                row = null;
            }
        }

        /* Return header */
        return header;
    }

    /**
     * Returns an array with indexes of columns that should be imported
     * 
     * Only columns listed within {@link #column} will be imported. This
     * iterates over the list of columns and returns an array with indexes of
     * columns that should be imported.
     * 
     * @return Array containing indexes of columns that should be imported
     */
    protected int[] getIndexesToImport() {

        /* Get indexes to import from */
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (ImportColumn column : config.getColumns()) {
            indexes.add(((ImportColumnCSV) column).getIndex());
        }

        int[] result = new int[indexes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indexes.get(i);
        }

        return result;
    }
}
