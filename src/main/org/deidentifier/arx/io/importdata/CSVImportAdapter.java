package org.deidentifier.arx.io.importdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.input.CountingInputStream;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.CSVDataInput;

/**
 * Adapter for CSV files
 *
 * This adapter can be used to import from a CSV file. It is basically a
 * wrapper around {@link CSVDataInput}, however it is more flexible, as
 * columns can be renamed and selected on an individual basis.
 */
public class CSVImportAdapter extends DataSourceImportAdapter {
    
    private CSVConfiguration config; 
    private long bytesTotal;
    private CountingInputStream cin;
    private DataType<?>[] types;
    
    /**
     * @see {@link CSVDataInput}
     */
    private CSVDataInput in;

    /**
     * @see {@link CSVDataInput#iterator()}
     */
    private Iterator<String[]> it;

    /**
     * Indexes of columns that should be imported
     *
     * This keeps track of columns that should be imported, as columns can be
     * selected on an individual basis.
     */
    private int[] indexes;

    /**
     * Contains the last row as returned by {@link CSVDataInput#iterator()}
     *
     * This row needs to be further processed, e.g. to return only selected
     * columns.
     */
    private String[] lastRow;

    /**
     * Indicates whether the first row has already been returned
     *
     * The first row contains the name of the columns. Depending upon
     * {@link #containsHeader} and whether the name of the column has been
     * assigned explicitly, this is either the value of the file itself,
     * the value defined by the user, or a default value.
     */
    private boolean firstRowReturned = false;

    /**
     * Creates a new instance
     * @param config
     */
    protected CSVImportAdapter(CSVConfiguration config) throws IOException{

        super(config);
        this.config = config;
        this.bytesTotal = new File(config.getFile()).length();

        // Prepare
        this.indexes = getIndexesToImport();
        this.types = getColumnDatatypes();

        // Track progress
        cin = new CountingInputStream(new FileInputStream(new File(config.getFile())));
        
        /* Get CSV iterator */
        in = new CSVDataInput(cin, config.getSeparator());
        it = in.iterator();

        /* Check whether first row exists */
        if (it.hasNext()) {
            lastRow = it.next();
            if (config.isContainsHeader()) {
                if (!it.hasNext()) { 
                    throw new IOException("CSV contains nothing but header");
                }
            }
        } else {
            throw new IOException("CSV file contains no data");
        }
    }

    /**
     * Indicates whether there is another element to return
     *
     * This returns true when the CSV file has another line and there are
     * actually columns to import from {@link #columns}.
     *
     * @return Boolean value, see above
     */
    @Override
    public boolean hasNext() {
        return it.hasNext() && (config.getColumns().size() != 0);
    }

    /**
     * Returns the next row
     *
     * The returned element is sorted as defined by {@link Column#index} and
     * contains as many elements as there are columns selected to import from
     * {@link #indexes}. The first row {@link #firstRowReturned}
     * contains the names of the columns.
     */
    @Override
    public String[] next() {

        if (!firstRowReturned) {

            firstRowReturned = true;
            String[] header = lastRow;
            int i = 0;

            for (Column column : config.getColumns()) {
                if (!config.isContainsHeader()) {
                    header[i] = "Column #" + column.getIndex();
                } else {
                    header[i] = lastRow[column.getIndex()];
                }

                if (column.getName() != null) {
                    header[i] = column.getName();
                }
                
                column.setName(header[i]);
                i++;
            }
            return header;
        }

        lastRow = it.next();
        String[] result = new String[indexes.length];
        for (int i=0; i<indexes.length; i++){
            result[i] = lastRow[indexes[i]];
            if (!types[i].isValid(result[i])) {
                throw new IllegalArgumentException("Data value does not match data type");
            }
        }
        return result;
    }

    /**
     * Dummy
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getProgress() {
        if (cin==null) return 0;
        long bytesRead = cin.getByteCount();
        return (int)((double)bytesRead / (double)bytesTotal * 100d);
    }
}
