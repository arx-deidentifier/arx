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

package org.deidentifier.arx.io;

import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * Configuration describing an Excel file
 * 
 * This is used to describe Excel files. Both file types (XLS and XLSX) are
 * supported. The file type can either be detected automatically by the file
 * extension, or alternatively can be set manually. Furthermore there is a sheet
 * index {@link #sheetIndex}, which describes which sheet within the file should
 * be used.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportConfigurationExcel extends ImportConfigurationFile implements
        IImportConfigurationWithHeader {

    /**
     * Valid file types for Excel files
     * 
     * XLS is the "old" Excel file type, XLSX is the "new" Excel file type.
     */
    public enum ExcelFileTypes {
        XLS,
        XLSX
    };

    /**
     * Used file type
     * 
     * This is the actual filetype that will be used
     * 
     * @see {@link #setExcelFileType(ExcelFileTypes excelFileType)}
     */
    private ExcelFileTypes excelFileType;

    /**
     * Sheet index
     */
    private int            sheetIndex;

    /**
     * Indicates whether first row contains header (names of columns)
     * 
     * @see {@link IImportConfigurationWithHeader}
     */
    private boolean        containsHeader;

    /**
     * Creates a new instance of this object
     * 
     * @param fileLocation
     *            {@link #setFileLocation(String)}
     * @param excelFileType
     *            {@link #setExcelFileType(ExcelFileTypes)}
     * @param sheetIndex
     *            {@link #setSheetIndex(int)}
     * @param containsHeader
     *            {@link #setContainsHeader(boolean)}
     */
    public ImportConfigurationExcel(String fileLocation,
                                    ExcelFileTypes excelFileType,
                                    int sheetIndex,
                                    boolean containsHeader) {

        setFileLocation(fileLocation);
        setExcelFileType(excelFileType);
        setSheetIndex(sheetIndex);
        setContainsHeader(containsHeader);
    }

    /**
     * Creates a new instance of this object without specifying the file type
     * 
     * The file type will be detected automatically using the file extension. By
     * default "xlsx" is assumed. In case the file extension is "xls" the file
     * type will be set to {@link ExcelFileTypes#XLS}.
     * 
     * @param fileLocation
     *            {@link #setFileLocation(String)}
     * @param sheetIndex
     *            {@link #sheetIndex}
     * @param containsHeader
     *            {@link #containsHeader}
     */
    public ImportConfigurationExcel(String fileLocation,
                                    int sheetIndex,
                                    boolean containsHeader) {

        ExcelFileTypes excelFileType;
        String ext = FilenameUtils.getExtension(fileLocation);

        switch (ext) {
            case "xls":
                excelFileType = ExcelFileTypes.XLS;
                break;
    
            default:
                excelFileType = ExcelFileTypes.XLSX;
                break;
        }

        setFileLocation(fileLocation);
        setSheetIndex(sheetIndex);
        setContainsHeader(containsHeader);
        setExcelFileType(excelFileType);
    }

    /**
     * Adds a single column to import from
     * 
     * This makes sure that only {@link ImportColumnExcel} can be added,
     * otherwise an {@link IllegalArgumentException} will be thrown.
     * 
     * @param column
     *            A single column to import from, {@link ImportColumnExcel}
     */
    @Override
    public void addColumn(ImportColumn column) {

        if (!(column instanceof ImportColumnExcel)) {
            throw new IllegalArgumentException("Column needs to be of type ExcelColumn");
        }
        
        if (!((ImportColumnExcel) column).isIndexSpecified() && 
            !this.getContainsHeader()){
            final String ERROR = "Adressing columns by name is only possible if the source contains a header";
            throw new IllegalArgumentException(ERROR);
        }

        for (ImportColumn c : columns) {
            if (((ImportColumnExcel) column).isIndexSpecified() &&
                ((ImportColumnExcel) column).getIndex() == ((ImportColumnExcel) c).getIndex()) { 
                throw new IllegalArgumentException("Column for this index already assigned"); 
            }

            if (!((ImportColumnExcel) column).isIndexSpecified() &&
                ((ImportColumnExcel) column).getName().equals(((ImportColumnExcel) c).getName())) { 
                throw new IllegalArgumentException("Column for this name already assigned"); 
            }

            if (column.getAliasName() != null && c.getAliasName() != null &&
                c.getAliasName().equals(column.getAliasName())) { 
                throw new IllegalArgumentException("Column names need to be unique"); 
            }
        }
        this.columns.add(column);
    }

    /**
     * @return {@link #containsHeader}
     */
    @Override
    public boolean getContainsHeader() {
        return containsHeader;
    }

    /**
     * @return {@link #ExcelFileTypes}
     */
    public ExcelFileTypes getExcelFileType() {
        return excelFileType;
    }

    /**
     * @return {@link #sheetIndex}
     */
    public int getSheetIndex() {
        return sheetIndex;
    }

    /**
     * @param containsHeader
     *            {@link #containsHeader}
     */
    @Override
    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }

    /**
     * @param excelFileType
     *            {@link #ExcelFileTypes}
     */
    public void setExcelFileType(ExcelFileTypes excelFileType) {
        this.excelFileType = excelFileType;
    }

    /**
     * @param sheetIndex
     *            {@link #sheetIndex}
     */
    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }


    /**
     * Sets the indexes based on the header
     * @param row
     */
    public void prepare(Row row) {

        for (ImportColumn c : super.getColumns()) {
            ImportColumnExcel column = (ImportColumnExcel) c;
            if (!column.isIndexSpecified()) {
                boolean found = false;
                for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
                    row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
                    if (row.getCell(i).getStringCellValue().equals(column.getName())) {
                        found = true;
                        column.setIndex(i);
                    }
                }
                if (!found) {
                    throw new NoSuchElementException("Index for column '" + column.getName() + "' couldn't be found");
                }
            }
        }
    }
}
