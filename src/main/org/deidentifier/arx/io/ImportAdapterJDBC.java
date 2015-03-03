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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Import adapter for JDBC
 * 
 * This adapter can import data from JDBC sources. The source itself is
 * described by an appropriate {@link ImportConfigurationJDBC} object.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportAdapterJDBC extends ImportAdapter {

    /** The configuration describing the CSV file being used. */
    private ImportConfigurationJDBC config;

    /**
     * ResultSet containing rows to return.
     *
     * @see {@link #next()}
     */
    private ResultSet               resultSet;

    /** Indicates whether there is another row to return. */
    private boolean                 hasNext;

    /**
     * Indicates whether the first row has already been returned
     * 
     * The first row contains the name of the columns. Depending upon whether
     * the name of the column has been assigned explicitly, this is either the
     * value of the table itself, the value defined by the user.
     */
    private boolean                 headerReturned;

    /**
     * Number of rows that need to be processed in total.
     *
     * @see {@link #getProgress()}
     */
    private int                     totalRows;

    /**
     * Creates a new instance of this object with given configuration.
     *
     * @param config {@link #config}
     * @throws IOException In case of communication errors with JDBC
     * @todo Fix IOException
     */
    protected ImportAdapterJDBC(ImportConfigurationJDBC config) throws IOException {

        super(config);
        this.config = config;

        /* Preparation work */
        this.indexes = getIndexesToImport();
        this.dataTypes = getColumnDatatypes();

        try {

            Statement statement;

            /* Used to keep track of progress */
            statement = config.getConnection().createStatement();
            statement.execute("SELECT COUNT(*) FROM " + config.getTable());
            resultSet = statement.getResultSet();

            if (resultSet.next()) {

                totalRows = resultSet.getInt(1);
                if (totalRows == 0) {
                    throw new IOException("Table doesn't contain any rows");
                }

            } else {
                throw new IOException("Couldn't determine number of rows");
            }

            /* Query for actual data */
            statement = config.getConnection().createStatement();
            statement.execute("SELECT * FROM " + config.getTable());
            resultSet = statement.getResultSet();
            hasNext = resultSet.next();

        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }

        // Create header
        header = createHeader();
    }

    /**
     * Returns the percentage of data that has already been returned
     * 
     * This divides the number of rows that have already been returned by the
     * number of total rows and casts the result into a percentage. In case of
     * an {@link SQLException} 0 will be returned.
     *
     * @return
     */
    @Override
    public int getProgress() {

        try {
            return (int) ((double) resultSet.getRow() / (double) totalRows * 100d);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Indicates whether there is another element to return
     * 
     * This returns true when there is another element in the result set {@link #resultSet}.
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public String[] next() {

        /* Return header in first iteration */
        if (!headerReturned) {
            headerReturned = true;
            return header;
        }

        try {

            /* Create regular row */
            String[] result = new String[indexes.length];
            for (int i = 0; i < indexes.length; i++) {
                
                result[i] = resultSet.getString(indexes[i]);
                if (!dataTypes[i].isValid(result[i])) {
                    throw new IllegalArgumentException("Data value does not match data type");
                }
            }

            /* Move cursor forward and assign result to {@link #hasNext} */
            hasNext = resultSet.next();
            
            if (!hasNext) {
                try {
                    if (!config.getConnection().isClosed()) {
                        config.getConnection().close();
                    }
                } catch (Exception e){
                    /* Die silently*/
                }
            }
            
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Couldn't retrieve data from database");
        }
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
     * returned later on by iterating over this object. Depending upon whether
     * or not names have been assigned explicitly either the appropriate values
     * will be returned, or names from the JDBC metadata will be used.
     *
     * @return
     */
    private String[] createHeader() {

        /* Initialization */
        String[] header = new String[config.getColumns().size()];
        List<ImportColumn> columns = config.getColumns();

        /* Create header */
        for (int i = 0, len = columns.size(); i < len; i++) {

            ImportColumn column = columns.get(i);

            /* Check whether name has been assigned explicitly or is nonempty */
            if (column.getAliasName() != null &&
                !column.getAliasName().equals("")) {

                header[i] = column.getAliasName();

            } else {

                /* Assign name from JDBC metadata */
                try {
                    /* +1 offset, because counting in JDBC starts at 1 */
                    header[i] = resultSet.getMetaData().getColumnName(((ImportColumnJDBC) column).getIndex() + 1);
                } catch (SQLException e) {
                    throw new IllegalArgumentException("Index for column '" + ((ImportColumnJDBC) column).getIndex() + "' couldn't be found");
                }
            }
            column.setAliasName(header[i]);
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
            indexes.add(((ImportColumnJDBC) column).getIndex());
        }

        int[] result = new int[indexes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indexes.get(i) + 1;
        }

        return result;
    }
}
