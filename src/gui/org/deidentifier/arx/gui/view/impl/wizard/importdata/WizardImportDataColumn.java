package org.deidentifier.arx.gui.view.impl.wizard.importdata;


/**
 * Represents a single data column
 *
 * This object represents a single column detected within the source. As the
 * user can choose whether or not to import from this column, it does not
 * necessarily end up in the set of imported data. Furthermore it is possible
 * for the user to rename a column and change its datatype.
 */
public class WizardImportDataColumn {

    /**
     * Indicates whether this particular column is enabled
     */
    private boolean enabled;

    /**
     * Name of the column
     */
    private String name;

    /**
     * Datatype of the column
     *
     * TODO Change to DataType from arx framework
     */
    private String datatype;


    /**
     * Creates a new object representing a column with the given parameters
     *
     * @param enabled {@link #enabled}
     * @param name {@link #name}
     * @param datatype {@link #datatype}
     */
    public WizardImportDataColumn(boolean enabled, String name, String datatype)
    {

        setEnabled(enabled);
        setName(name);
        setDatatype(datatype);

    }

    /**
     * @return {@link #enabled}
     */
    public boolean isEnabled()
    {

        return enabled;

    }

    /**
     * @param enabled {@link #enabled}
     */
    public void setEnabled(boolean enabled)
    {

        this.enabled = enabled;

    }

    /**
     * @return {@link #name}
     */
    public String getName()
    {

        return name;

    }

    /**
     * @param name {@link #name}
     */
    public void setName(String name)
    {

        this.name = name;

    }

    /**
     * @return {@link #datatype}
     */
    public String getDatatype()
    {

        return datatype;

    }

    /**
     * @param datatype {@link #datatype}
     */
    public void setDatatype(String datatype)
    {

        this.datatype = datatype;

    }

}
