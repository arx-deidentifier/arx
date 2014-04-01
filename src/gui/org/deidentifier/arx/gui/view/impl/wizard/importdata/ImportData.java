package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.io.importdata.Column;


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
     * Each column is represented by {@link Column}.
     */
    private List<WizardColumn> columns;

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
    public boolean getFirstRowContainsHeader()
    {

        return firstRowContainsHeader;

    }

    /**
     * @param firstRowContainsHeader {@link #firstRowContainsHeader}
     */
    public void setFirstRowContainsHeader(boolean firstRowContainsHeader)
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
    public List<WizardColumn> getWizardColumns()
    {

        return columns;

    }

    /**
     * @param columns {@link #columns}
     */
    public void setWizardColumns(List<WizardColumn> columns)
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
     * @param column Column the data should be returned for
     *
     * @return Data for the given column
     */
    public List<String> getPreviewData(WizardColumn column) throws Exception {

        List<String> result = new ArrayList<String>();
        int index = columns.indexOf(column);

        if (index != -1) {

            for (String[] s : getPreviewData()) {

                result.add(s[index]);

            }

        } else {

            throw new Exception("Column not part of preview data");

        }

        return result;

    }

    /**
     * Returns a list of {@link Column} with elements that are enabled
     *
     * {@link WizardColumn} is a wrapper for the wizard. These elements can
     * be disabled by the user. This method will only return those elements
     * that are enabled.
     *
     * @return {@link Column} List of enabled elements
     */
    public List<Column> getEnabledColumns()
    {

        List<Column> result = new ArrayList<Column>();

        for (WizardColumn column : columns) {

            if (column.isEnabled()) {

                result.add(column.getColumn());

            }

        }

        return result;

    }

}
