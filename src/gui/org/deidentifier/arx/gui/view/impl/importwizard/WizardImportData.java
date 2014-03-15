package org.deidentifier.arx.gui.view.impl.importwizard;

import java.util.List;


public class WizardImportData {

    public enum source {CSV, JDBC, XLS};

    private WizardImport wizard;

    private source source;

    private List<WizardImportDataColumn> columns;

    private String csvFileLocation;
    private char csvSeparator;
    private boolean csvContainsHeader;


    public WizardImportData(WizardImport wizard)
    {

        this.wizard = wizard;

    }

    public WizardImport getWizard()
    {

        return wizard;

    }

    public String getCsvFileLocation()
    {

        return csvFileLocation;

    }

    public void setCsvFileLocation(String fileLocation)
    {

        this.csvFileLocation = fileLocation;

    }

    public char getCsvSeparator()
    {

        return csvSeparator;

    }

    public void setCsvSeparator(char csvSeparator)
    {

        this.csvSeparator = csvSeparator;

    }

    public boolean getCsvContainsHeader()
    {

        return csvContainsHeader;

    }

    public void setCsvContainsHeader(boolean csvContainsHeader)
    {

        this.csvContainsHeader = csvContainsHeader;

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
