package org.deidentifier.arx.io.importdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.io.CSVDataInput;

/**
 * Adapter for CSV files
 *
 * This adapter can be used to import from a CSV file. It is basically a
 * wrapper around {@link CSVDataInput}, however it is more flexible, as
 * columns can be renamed and selected on an individual basis.
 */
public class CSVImportAdapter extends ImportAdapter {

    /**
     * List of columns to be imported
     *
     * Each element of this list represents a column to import from. Refer to
     * {@link Column} for details.
     *
     * @note Only columns that are part of this list will be imported from,
     * any other column will simply be ignored and not be returned.
     */
    protected List<Column> columns;

    /**
     * Name of file to import from
     */
    private String file;

    /**
     * Character that separates the column from each other
     */
    private char separator;

    /**
     * Indicates whether first row contains header with names of columns
     */
    private boolean containsHeader;

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
    private Integer[] indexesToImport;

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
     * Creates a new instance of this object with the given parameters
     *
     * The first line is read in order to get the number of columns within the
     * CSV file. This is stored within {@link #lastRow}. Exceptions are thrown
     * in case the file doesn't contain any useful information.
     *
     * @param file {@link #file}
     * @param separator {@link #separator}
     * @param containsHeader {@link #containsHeader}
     * @throws Exception In case file is invalid
     */
    public CSVImportAdapter(String file, char separator, boolean containsHeader) throws Exception {

        this.file = file;
        this.separator = separator;
        this.containsHeader = containsHeader;

        /*
         * Get CSV iterator
         */
        in = new CSVDataInput(file, separator);
        it = in.iterator();

        /*
         * Check whether first row exists
         */
        if (it.hasNext()) {

            lastRow = it.next();

            if (containsHeader) {

                if (!it.hasNext()) {

                    throw new Exception("CSV contains nothing but header");

                }

            }

        } else {

            throw new Exception("CSV file contains no data");

        }

    }

    /**
     * @return {@link #file}
     */
    public String getFile() {

        return file;

    }

    /**
     * @return {@link #separator}
     */
    public char getSeparator() {

        return separator;

    }

    /**
     * @return {@link #containsHeader}
     */
    public boolean getContainsHeader() {

        return containsHeader;

    }

    /**
     * Sets the list of columns to actually import from
     *
     * @param columns List of columns to import from
     *
     * @exception In case list is empty or something has already been returned
     *
     * @see {@link #column}
     */
    @Override
    public void setColumns(List<Column> columns) throws Exception {

        if (columns.isEmpty()) {

            throw new Exception("Empty column list");

        }

        if (firstRowReturned) {

            throw new Exception("Can't be changed anymore");

        }

        /*
         * Get indexes to import from {@see #indexesToImport}
         */

        ArrayList<Integer> listIndexesToImport = new ArrayList<Integer>();

        for(Column column : columns) {

            listIndexesToImport.add(column.getIndex());

        }

        Integer[] integerArray = new Integer[listIndexesToImport.size()];

        this.columns = columns;
        this.indexesToImport = listIndexesToImport.toArray(integerArray);

    }

    /**
     * @return {@link #columns}
     */
    @Override
    public List<Column> getColumns() {

        return columns;

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

        return it.hasNext() && (columns.size() != 0);

    }

    /**
     * Returns the next row
     *
     * The returned element is sorted as defined by {@link Column#index} and
     * contains as many elements as there are columns selected to import from
     * {@link #indexesToImport}. The first row {@link #firstRowReturned}
     * contains the names of the columns.
     */
    @Override
    public String[] next() {

        if (!firstRowReturned) {

            firstRowReturned = true;
            String[] header = lastRow;
            int i = 0;

            for (Column column : columns) {

                if (!containsHeader) {

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
        int i = 0;
        String[] result = new String[indexesToImport.length];

        for (Integer index : indexesToImport) {

            result[i++] = lastRow[index];

        }

        return result;

    }

    /**
     * Dummy
     */
    @Override
    public void remove() {

        return;

    }

}
