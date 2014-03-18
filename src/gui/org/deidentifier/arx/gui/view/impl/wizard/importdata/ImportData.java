package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.List;


/**
 * Contains data gathered by the wizard and offers means to access it
 *
 * This object is accessible to all pages of the wizard and can be used to set
 * or retrieve data.
 *
 * TODO Change to more elegant implementation (i.e. general key value storage)
 */
public class ImportData {

    /**
     * Possible sources for importing data
     */
    public enum source {CSV, JDBC, XLS};

    /**
     * Actual source data should be imported from
     */
    private source source;

    /**
     * List of detected columns to be imported
     *
     * Each column is represented by {@link ImportDataColumn}.
     */
    private List<ImportDataColumn> columns;

    /**
     * Location of file to import from
     */
    private String fileLocation;

    /**
     * Separator for columns (in case of CSV import)
     */
    private char csvSeparator;

    /**
     * Indicates whether first row contains header
     */
    private boolean firstRowContainsHeader = true;

    /**
     * Index of sheet to import from (in case of Excel import)
     */
    private int xlsSheetIndex;


    /**
     * @return {@link #xlsSheetIndex}
     */
    public int getXlsSheetIndex() {

        return xlsSheetIndex;

    }

    /**
     * @param xlsSheetIndex {@link #xlsSheetIndex}
     */
    public void setXlsSheetIndex(int xlsSheetIndex) {

        this.xlsSheetIndex = xlsSheetIndex;

    }

    /**
     * @return {@link #fileLocation}
     */
    public String getFileLocation()
    {

        return fileLocation;

    }

    /**
     * @param fileLocation {@link #fileLocation}
     */
    public void setFileLocation(String fileLocation)
    {

        this.fileLocation = fileLocation;

    }

    /**
     * @return {@link #csvSeparator}
     */
    public char getCsvSeparator()
    {

        return csvSeparator;

    }

    /**
     * @param csvSeparator {@link #csvSeparator}
     */
    public void setCsvSeparator(char csvSeparator)
    {

        this.csvSeparator = csvSeparator;

    }

    /**
     * @return {@link #firstRowContainsHeader}
     */
    public boolean getfirstRowContainsHeader()
    {

        return firstRowContainsHeader;

    }

    /**
     * @param firstRowContainsHeader {@link #firstRowContainsHeader}
     */
    public void setfirstRowContainsHeader(boolean firstRowContainsHeader)
    {

        this.firstRowContainsHeader = firstRowContainsHeader;

    }

    /**
     * @return {@link #source}
     */
    public source getSource()
    {

        return source;

    }

    /**
     * @param source {@link #source}
     */
    public void setSource(source source)
    {

        this.source = source;

    }

    /**
     * @return {@link #columns}
     */
    public List<ImportDataColumn> getColumns()
    {

        return columns;

    }

    /**
     * @param columns {@link #columns}
     */
    public void setColumns(List<ImportDataColumn> columns)
    {

        this.columns = columns;

    }

}
