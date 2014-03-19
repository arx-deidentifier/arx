package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
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
    public enum sources {CSV, JDBC, XLS};

    /**
     * Actual source data should be imported from
     */
    private sources source;

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
     * Preview data
     */
    private List<String[]> previewData;

    /**
     * Maximum number of lines to be loaded for preview
     */
    public static final int previewDataMaxLines = 25;

    /**
     * Indicates whether wizard was finished completely
     */
    private boolean wizardFinished;


    /**
     * @return {@link #wizardFinished}
     */
    public boolean isWizardFinished() {

        return wizardFinished;

    }

    /**
     * @param {@link #wizardFinished}
     */
    public void setWizardFinished(boolean wizardFinished) {

        this.wizardFinished = wizardFinished;

    }

    /**
     * @return {@link #xlsSheetIndex}
     */
    public int getXlsSheetIndex() {

        return xlsSheetIndex;

    }

    /**
     * @param {@link #xlsSheetIndex}
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
    public sources getSource()
    {

        return source;

    }

    /**
     * @param source {@link #source}
     */
    public void setSource(sources source)
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

    /**
     * @param columns {@link #previewData}
     */
    public void setPreviewData(List<String[]> previewData) {

        this.previewData = previewData;

    }

    /**
     * @return {@link #previewData}
     */
    public List<String[]> getPreviewData() {

        return previewData;

    }

    /**
     * Returns a list of strings containing the data for the given column
     *
     * @param importDataColumn Column the data should be returned for
     *
     * @return Data for the given column
     */
    public List<String> getPreviewDataForColumn(ImportDataColumn importDataColumn) {

        List<String> result = new ArrayList<String>();
        int index = importDataColumn.getIndex();

        for (String[] previewData : getPreviewData()) {

            result.add(previewData[index]);

        }

        return result;

    }

}
