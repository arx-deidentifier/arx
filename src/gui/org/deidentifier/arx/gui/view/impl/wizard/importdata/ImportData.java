package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.io.importdata.Column;


/**
 * Stores all of the data gathered by the wizard and offers means to access it
 *
 * This object is accessible to all pages of the wizard and can be used to
 * store and/or retrieve data. It is mainly used to exchange data between
 * multiple wizard pages.
 *
 * TODO Change to more elegant implementation (i.e. general key value storage)
 */
public class ImportData {

    /**
     * Possible sources for importing data from
     *
     * @see {@link sourceType}
     */
    public enum SourceType {CSV, JDBC, XLS};

    /**
     * Actual source data should be imported from
     */
    private SourceType sourceType;

    /**
     * List of columns used throughout the wizard
     *
     * This contains a list of all the columns that were detected and is
     * used through the whole wizard. Note however, that not all of the columns
     * here will necessarily be imported from, as columns can be disabled on an
     * individual basis {@link WizardColumn#setEnabled(boolean)}.
     *
     * Each column itself is represented by {@link WizardColumn}.
     */
    private List<WizardColumn> wizardColumns;

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
     *
     * In case of CSV and XLS files the first row might contain a header
     * describing the column. This makes sure that the appropriate row is not
     * considered to be data.
     */
    private boolean firstRowContainsHeader = true;

    /**
     * Index of sheet to import from (in case of XLS import)
     *
     * @see {@link SourceType#XLS}
     */
    private int xlsSheetIndex;

    /**
     * Preview data
     *
     * For reasons of simplicity and performance data is imported rather early
     * on in the wizard and stored here. This makes sure that
     * {@link PreviewPage} doesn't need to know anything about the source type
     * the data is coming from.
     *
     * It will contain up to {@link #previewDataMaxLines} lines of data.
     */
    private List<String[]> previewData;

    /**
     * Maximum number of lines to be loaded for preview purposes
     */
    public static final int previewDataMaxLines = 25;


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
     * @return {@link #sourceType}
     */
    public SourceType getSourceType()
    {

        return sourceType;

    }

    /**
     * @param source {@link #sourceType}
     */
    public void setSourceType(SourceType sourceType)
    {

        this.sourceType = sourceType;

    }

    /**
     * @return {@link #wizardColumns}
     */
    public List<WizardColumn> getWizardColumns()
    {

        return wizardColumns;

    }

    /**
     * @param columns {@link #wizardColumns}
     */
    public void setWizardColumns(List<WizardColumn> columns)
    {

        this.wizardColumns = columns;

    }

    /**
     * @param wizardColumns {@link #previewData}
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
     * This will only return the {@link #previewData} for the given column
     * rather than all of the preview data.
     *
     * @param column Column the preview data should be returned for
     *
     * @return Data for the given column
     *
     * @see {@link #getPreviewData()}
     */
    public List<String> getPreviewData(WizardColumn column) {

        List<String> result = new ArrayList<String>();
        int index = wizardColumns.indexOf(column);

        if (index != -1) {

            for (String[] s : getPreviewData()) {

                result.add(s[column.getColumn().getIndex()]);

            }

        } else {

            throw new IllegalArgumentException("Column not part of preview data");

        }

        return result;

    }

    /**
     * Returns list of enabled columns
     *
     * This iterates over {@link #wizardColumns} and returns only the columns
     * that are enabled {@link WizardColumn#isEnabled()}. Columns that have
     * been disabled by the user will not be returned.
     *
     * @return {@link Column} List of enabled columns
     */
    public List<Column> getEnabledColumns()
    {

        List<Column> result = new ArrayList<Column>();

        for (WizardColumn column : wizardColumns) {

            if (column.isEnabled()) {

                result.add(column.getColumn());

            }

        }

        return result;

    }

}
