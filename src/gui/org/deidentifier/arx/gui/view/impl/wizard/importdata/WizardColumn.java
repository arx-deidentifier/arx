package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.io.importdata.Column;

/**
 * Wrapper for {@link Column} used in the wizard context
 *
 * This is a wrapper for {@link Column}. It essentially adds an property
 * indicating whether a column is enabled {@link #enabled}.
 */
public class WizardColumn {

    /**
     * Indicates whether the given column is enabled
     *
     * Columns can be disabled by the user. Once disabled they won't be
     * imported.
     */
    private boolean enabled = true;

    /**
     * The actual column this wraps around
     */
    private Column column;


    /**
     * Creates a new instance for the given column
     *
     * @note This implicitly assumes that the column should be enabled.
     *
     * @param column Column that should be wrapped around
     */
    public WizardColumn(Column column)
    {

        this(column, true);

    }

    /**
     * Creates a new instance for the given column
     *
     * @param column Column that should be wrapped around
     */
    public WizardColumn(Column column, boolean enabled)
    {

        setEnabled(enabled);
        setColumn(column);

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
     * @return {@link #column}
     */
    public Column getColumn()
    {

        return column;

    }

    /**
     * @param enabled {@link #column}
     */
    public void setColumn(Column column)
    {

        this.column = column;

    }

}
