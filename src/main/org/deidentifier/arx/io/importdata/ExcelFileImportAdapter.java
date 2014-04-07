/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.io.importdata;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.deidentifier.arx.io.ExcelFileConfiguration;
import org.deidentifier.arx.io.ExcelFileConfiguration.ExcelFileTypes;

/**
 * Import adapter for Excel files
 *
 * This adapter can import data from Excel files. It handles both XLS and XLSX
 * files. The file type itself is defined by {@link ExcelFileConfiguration}.
 * The files are accessed using Apache POI.
 *
 * @see <a href="https://poi.apache.org/">Aapache POI</a>
 */
public class ExcelFileImportAdapter extends DataSourceImportAdapter {

    /**
     * The configuration describing the Excel file
     */
    private ExcelFileConfiguration config;

    /**
     * Actual iterator used to go through data
     */
    private Iterator<Row> rowIterator;

    /**
     * Contains the last row as returned by the iterator
     *
     * @note This row cannot be simply returned, but needs to be further
     * processed, e.g. to return only selected columns.
     */
    private Row lastRow;

    /**
     * Indicates whether the first row has already been returned
     *
     * The first row contains the name of the columns and always needs to be
     * returned first in order to guarantee that the framework will pick up
     * the names correctly.
     */
    private boolean headerReturned = false;

    /**
     * Number of rows within the specified sheet
     */
    private int totalRows;

    /**
     * Current row {@link lastRow} is referencing
     */
    private int currentRow = 0;


    /**
     * Creates a new instance of this object with given configuration
     *
     * Depending upon the file type it either uses HSSF or XSSF to access the
     * file. In both cases {@link #rowIterator} will be assigned a reference
     * to an iterator, which can then be used to access the actual data on a
     * row by row basis.
     *
     * @param config {@link #config}
     *
     * @throws IOException In case file doesn't contain actual data
     */
    protected ExcelFileImportAdapter(ExcelFileConfiguration config) throws IOException{

        super(config);
        this.config = config;

        /* Preparation work */
        this.indexes = getIndexesToImport();
        this.dataTypes = getColumnDatatypes();

        /* Get row iterator */
        FileInputStream input = new FileInputStream(config.getFileLocation());
        Workbook workbook = null;

        if (config.getExcelFileType() == ExcelFileTypes.XLS) {

            workbook = new HSSFWorkbook(input);

        } else if (config.getExcelFileType() == ExcelFileTypes.XLSX) {

            workbook = new XSSFWorkbook(input);

        } else {

            input.close();
            throw new IllegalArgumentException("File type not supported");

        }

        Sheet sheet = workbook.getSheetAt(config.getSheetIndex());
        rowIterator = sheet.iterator();

        /* Get total number of rows */
        totalRows = sheet.getPhysicalNumberOfRows();

        /* Check whether there is actual data within the file */
        if (rowIterator.hasNext()) {

            lastRow = rowIterator.next();

            if (config.getContainsHeader()) {

                if (!rowIterator.hasNext()) {

                    throw new IOException("File contains nothing but header");

                }

            }

        } else {

            throw new IOException("File contains no data");

        }

    }

    /**
     * Indicates whether there is another element to return
     *
     * This returns true when the file contains another line, which could be
     * accessed by {@link #rowIterator}.
     *
     * @note {@link #lastRow} effectively works as buffer and will always be
     * set up by the previous iteration, so once there is no data, it will
     * be assigned <code>null</code>, which is checked for here.
     */
    @Override
    public boolean hasNext() {

        return lastRow != null;

    }

    /**
     * Returns the next row
     *
     * The returned element is sorted as defined by {@link Column#index} and
     * contains as many elements as there are columns selected to import from
     * {@link #indexes}. The first row will always contain the names of the
     * columns. {@link #headerReturned} is used to keep track of that.
     *
     * @throws IllegalArgumentException In case defined datatypes don't match
     */
    @Override
    public String[] next() {

        /* Check whether header was already returned */
        if (!headerReturned) {

            headerReturned = true;
            return createHeader();

        }

        /* Create regular row */
        String[] result = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {

            lastRow.getCell(indexes[i]).setCellType(Cell.CELL_TYPE_STRING);
            result[i] = lastRow.getCell(indexes[i]).getStringCellValue();

            if (!dataTypes[i].isValid(result[i])) {

                throw new IllegalArgumentException("Data value does not match data type");

            }

        }

        /* Fetches the next row, which will be used in next iteration */
        if (rowIterator.hasNext()) {

            lastRow = rowIterator.next();
            currentRow++;

        } else {

            lastRow = null;

        }

        /* Return resulting row */
        return result;

    }

    /**
     * Creates the header row
     *
     * This returns a string array with the names of the columns that will be
     * returned later on by iterating over this object. Depending upon the
     * configuration {@link ExcelFileConfiguration#getContainsHeader()} and
     * whether or not names have been assigned explicitly either the
     * appropriate values will be returned, or names will be made up on the
     * fly following the pattern "Column #x", where x is incremented for each
     * column.
     */
    private String[] createHeader() {

        /* Initialization */
        String[] header = new String[config.getColumns().size()];
        List<Column> columns = config.getColumns();

        /* Create header */
        for (int i = 0, len = columns.size(); i < len; i++) {

            Column column = columns.get(i);

            if (config.getContainsHeader()) {

                /* Assign name of file itself */
                lastRow.getCell(column.getIndex()).setCellType(Cell.CELL_TYPE_STRING);
                header[i] = lastRow.getCell(column.getIndex()).getStringCellValue();

            } else {

                /* Nothing defined in CSV file, build name manually */
                header[i] = "Column #" + column.getIndex();

            }

            if (column.getName() != null) {

                /* Name has been assigned explicitly */
                header[i] = column.getName();

            }

            column.setName(header[i]);

        }

        /* Fetch next row in preparation for next iteration */
        if (config.getContainsHeader()) {

            if (rowIterator.hasNext()) {

                lastRow = rowIterator.next();
                currentRow++;

            } else {

                lastRow = null;

            }

        }

        /* Return header */
        return header;

    }

    /**
     * Dummy
     */
    @Override
    public void remove() {

        throw new UnsupportedOperationException();

    }

    /**
     * Returns the percentage of data that has already been returned
     *
     * The basis for this calculation is the row currently being accessed.
     *
     * @see {@link #currentRow}
     * @see {@link #totalRows}
     */
    @Override
    public int getProgress() {

        return (int)((double)currentRow / (double)totalRows * 100d);

    }

}
