/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.io.ImportColumn;

/**
 * Wrapper for {@link ImportColumn} used in the wizard context
 * 
 * This is a wrapper for {@link ImportColumn}. It essentially adds an property
 * indicating whether a column is enabled {@link #enabled}.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardModelColumn {

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
    private ImportColumn  column;

    /**
     * Creates a new instance for the given column
     * 
     * @note This implicitly assumes that the column should be enabled.
     * 
     * @param column
     *            Column that should be wrapped around
     */
    public ImportWizardModelColumn(ImportColumn column) {

        this(column, true);
    }

    /**
     * Creates a new instance for the given column
     * 
     * @param column
     *            Column that should be wrapped around
     */
    public ImportWizardModelColumn(ImportColumn column, boolean enabled) {

        setEnabled(enabled);
        setColumn(column);
    }

    /**
     * @return {@link #column}
     */
    public ImportColumn getColumn() {

        return column;
    }

    /**
     * @return {@link #enabled}
     */
    public boolean isEnabled() {

        return enabled;
    }

    /**
     * @param enabled
     *            {@link #column}
     */
    public void setColumn(ImportColumn column) {

        this.column = column;
    }

    /**
     * @param enabled
     *            {@link #enabled}
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }
}
