package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.io.importdata.Column;

/**
 * Wrapper for {@link #Column}
 *
 * This wraps around {@link #Column} and adds a enabled property
 * {@link #enabled}, which is needed for the wizard, but is not available
 * on an API level.
 */
public class WizardColumn {

    private boolean enabled = true;
    private Column column;


    public WizardColumn(Column column)
    {

        this(true, column);

    }

    public WizardColumn(boolean enabled, Column column)
    {

        setEnabled(enabled);
        setColumn(column);

    }

    public boolean isEnabled()
    {

        return enabled;

    }

    public void setEnabled(boolean enabled)
    {

        this.enabled = enabled;

    }

    public Column getColumn()
    {

        return column;

    }

    public void setColumn(Column column)
    {

        this.column = column;

    }

}
