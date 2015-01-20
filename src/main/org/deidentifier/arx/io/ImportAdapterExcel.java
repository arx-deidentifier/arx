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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.deidentifier.arx.io.ImportConfigurationExcel.ExcelFileTypes;

/**
 * Import adapter for Excel files
 * 
 * This adapter can import data from Excel files. It handles both XLS and XLSX
 * files. The file type itself is defined by {@link ImportConfigurationExcel}.
 * The files are accessed using Apache POI.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 * @see <a href="https://poi.apache.org/">Aapache POI</a>
 */
public class ImportAdapterExcel extends ImportAdapter {

    /** The configuration describing the Excel file. */
    private ImportConfigurationExcel config;

    /** Actual iterator used to go through data. */
    private Iterator<Row>            iterator;

    /**
     * Contains the last row as returned by the iterator.
     *
     * @note This row cannot be simply returned, but needs to be further
     *       processed, e.g. to return only selected columns.
     */
    private Row                      row;

    /**
     * Indicates whether the first row has already been returned
     * 
     * The first row contains the name of the columns and always needs to be
     * returned first in order to guarantee that the framework will pick up the
     * names correctly.
     */
    private boolean                  headerReturned = false;

    /** Number of rows within the specified sheet. */
    private int                      totalRows;

    /** Current row {@link lastRow} is referencing. */
    private int                      currentRow     = 0;

    /**
     * Holds the number of columns
     * 
     * This is set in the first iteration and is checked against in every other
     * iteration. Once a row contains more columns that this, an exception is
     * thrown.
     */
    private int                      numberOfColumns;

    /**  TODO */
    private FileInputStream          input;

    /**
     * Creates a new instance of this object with given configuration
     * 
     * Depending upon the file type it either uses HSSF or XSSF to access the
     * file. In both cases {@link #iterator} will be assigned a reference to
     * an iterator, which can then be used to access the actual data on a row by
     * row basis.
     * 
     * @param config
     *            {@link #config}
     * 
     * @throws IOException
     *             In case file doesn't contain actual data
     */
    protected ImportAdapterExcel(ImportConfigurationExcel config) throws IOException {

        super(config);
        this.config = config;

        /* Get row iterator */
        input = new FileInputStream(config.getFileLocation());
        Workbook workbook = null;

        if (config.getExcelFileType() == ExcelFileTypes.XLS) {
            workbook = new HSSFWorkbook(input);
        } else if (config.getExcelFileType() == ExcelFileTypes.XLSX) {
            workbook = new XSSFWorkbook(input);
        } else {
            input.close();
            throw new IllegalArgumentException("File type not supported");
        }

        workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
        Sheet sheet = workbook.getSheetAt(config.getSheetIndex());
        iterator = sheet.iterator();

        /* Get total number of rows */
        totalRows = sheet.getPhysicalNumberOfRows();

        /* Check whether there is actual data within the file */
        if (iterator.hasNext()) {

            row = iterator.next();
            if (config.getContainsHeader()) {
                if (!iterator.hasNext()) {
                    throw new IOException("File contains nothing but header");
                }
            }
        } else {
            throw new IOException("File contains no data");
        }

        // Create header
        header = createHeader();
    }

    /**
     * Returns the percentage of data that has already been returned
     * 
     * The basis for this calculation is the row currently being accessed.
     *
     * @return
     * @see {@link #currentRow}
     * @see {@link #totalRows}
     */
    @Override
    public int getProgress() {
        return (int) ((double) currentRow / (double) totalRows * 100d);
    }

    /**
     * Indicates whether there is another element to return
     * 
     * This returns true when the file contains another line, which could be
     * accessed by {@link #iterator}.
     *
     * @return
     * @note {@link #row} effectively works as buffer and will always be set
     *       up by the previous iteration, so once there is no data, it will be
     *       assigned <code>null</code>, which is checked for here.
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


        /* Check whether number of columns is too big */
        if (row.getPhysicalNumberOfCells() > numberOfColumns) {
            throw new IllegalArgumentException("Number of columns in row " + currentRow + " is too big");
        }

        /* Create regular row */
        String[] result = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {

            row.getCell(indexes[i]).setCellType(Cell.CELL_TYPE_STRING);
            result[i] = row.getCell(indexes[i]).getStringCellValue();

            if (!dataTypes[i].isValid(result[i])) {
                throw new IllegalArgumentException("Data value does not match data type");
            }
        }

        /* Fetches the next row, which will be used in next iteration */
        if (iterator.hasNext()) {
            row = iterator.next();
            currentRow++;
        } else {
            row = null;
            try {
                input.close();
            } catch (Exception e){
                /* Die silently*/
            }
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
     * configuration {@link ImportConfigurationExcel#getContainsHeader()} and
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

            row.getCell(((ImportColumnExcel) column).getIndex())
                   .setCellType(Cell.CELL_TYPE_STRING);
            String name = row.getCell(((ImportColumnExcel) column).getIndex())
                                 .getStringCellValue();

            if (config.getContainsHeader() && !name.equals("")) {
                /* Assign name of file itself */
                header[i] = name;
            } else {
                /* Nothing defined in header (or empty), build name manually */
                header[i] = "Column #" +
                            ((ImportColumnExcel) column).getIndex();
            }

            if (column.getAliasName() != null) {
                /* Name has been assigned explicitly */
                header[i] = column.getAliasName();
            }

            column.setAliasName(header[i]);
        }

        /* Fetch next row in preparation for next iteration */
        if (config.getContainsHeader()) {

            if (iterator.hasNext()) {
                row = iterator.next();
                currentRow++;
            } else {
                row = null;
            }
        }

        /* Store number of columns */
        numberOfColumns = header.length;

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
            indexes.add(((ImportColumnExcel) column).getIndex());
        }

        int[] result = new int[indexes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indexes.get(i);
        }
        return result;
    }
}
