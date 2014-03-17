package org.deidentifier.arx.gui.view.impl.wizard.importdata;


public class WizardImportDataColumn {

    private boolean enabled;
    private String name;
    private String datatype;


    public WizardImportDataColumn(boolean enabled, String name, String datatype)
    {

        setEnabled(enabled);
        setName(name);
        setDatatype(datatype);

    }

    public boolean isEnabled()
    {

        return enabled;

    }

    public void setEnabled(boolean enabled)
    {

        this.enabled = enabled;

    }

    public String getName()
    {

        return name;

    }

    public void setName(String name)
    {

        this.name = name;

    }

    public String getDatatype()
    {

        return datatype;

    }

    public void setDatatype(String datatype)
    {

        this.datatype = datatype;

    }

}
