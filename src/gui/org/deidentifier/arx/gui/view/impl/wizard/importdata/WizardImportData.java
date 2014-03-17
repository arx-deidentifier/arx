package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.List;


public class WizardImportData {

    public enum source {CSV, JDBC, XLS};

    private source source;

    private List<WizardImportDataColumn> columns;

    private String fileLocation;
    private char csvSeparator;
    private boolean firstRowContainsHeader = true;
    private int xlsSheetIndex;


    public int getXlsSheetIndex() {

        return xlsSheetIndex;

    }

    public void setXlsSheetIndex(int xlsSheetIndex) {

        this.xlsSheetIndex = xlsSheetIndex;

    }

    public String getFileLocation()
    {

        return fileLocation;

    }

    public void setFileLocation(String fileLocation)
    {

        this.fileLocation = fileLocation;

    }

    public char getCsvSeparator()
    {

        return csvSeparator;

    }

    public void setCsvSeparator(char csvSeparator)
    {

        this.csvSeparator = csvSeparator;

    }

    public boolean getfirstRowContainsHeader()
    {

        return firstRowContainsHeader;

    }

    public void setfirstRowContainsHeader(boolean firstRowContainsHeader)
    {

        this.firstRowContainsHeader = firstRowContainsHeader;

    }

    public source getSource()
    {

        return source;

    }

    public void setSource(source source)
    {

        this.source = source;

    }

    public List<WizardImportDataColumn> getColumns()
    {

        return columns;

    }

    public void setColumns(List<WizardImportDataColumn> columns)
    {

        this.columns = columns;

    }

}
