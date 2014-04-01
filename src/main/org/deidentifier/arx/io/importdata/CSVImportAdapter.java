package org.deidentifier.arx.io.importdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
    
    /** TODO */
    private CSVConfiguration config;
    
    /** TODO */
    private long bytesTotal;
    
    /** TODO */
    private CountingInputStream cin;
    
    /** TODO */
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
    private String[] row;

    /**
     * Indicates whether the first row has already been returned
     *
     * The first row contains the name of the columns. Depending upon
     * {@link #containsHeader} and whether the name of the column has been
     * assigned explicitly, this is either the value of the file itself,
     * the value defined by the user, or a default value.
     */
    private boolean headerReturned = false;

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
            row = it.next();
            if (config.fileContainsHeader()) {
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
        return row != null;
    }

    /**
     * Returns the next row
     *
     * The returned element is sorted as defined by {@link Column#index} and
     * contains as many elements as there are columns selected to import from
     * {@link #indexes}. The first row {@link #headerReturned}
     * contains the names of the columns.
     */
    @Override
    public String[] next() {

        // Create header
        if (!headerReturned) {
            headerReturned = true;
            return createHeader();
        } 
        
        // Create row
        String[] result = new String[indexes.length];
        for (int i=0; i<indexes.length; i++){
            result[i] = row[indexes[i]];
            if (!types[i].isValid(result[i])) {
                throw new IllegalArgumentException("Data value does not match data type");
            }
        }
        

        // Fetch next row
        if (it.hasNext()) {
            row = it.next();
        } else {
            row = null;
        }
        
        // Return
        return result;
    }

    /**
     * Creates the header
     * @return
     */
    private String[] createHeader() {

        // Init
        String[] header = new String[config.getColumns().size()];
        List<Column> columns = config.getColumns();
        
        // Create header
        for (int i=0, len=columns.size(); i<len; i++) {
            
            Column column = columns.get(i);
            if (!config.fileContainsHeader()) {
                header[i] = "Column #" + column.getIndex();
            } else {
                header[i] = row[column.getIndex()];
            }

            if (column.getName() != null) {
                header[i] = column.getName();
            }

            column.setName(header[i]);
        }

        // Fetch next row
        if (config.fileContainsHeader()) {
            if (it.hasNext()) {
                row = it.next();
            } else {
                row = null;
            }
        }

        // Return header
        return header;
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
