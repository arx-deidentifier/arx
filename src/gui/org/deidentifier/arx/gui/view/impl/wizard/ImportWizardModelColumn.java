/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    /** The actual column this wraps around. */
    private ImportColumn  column;

    /**
     * Creates a new instance for the given column.
     *
     * @param column Column that should be wrapped around
     * @note This implicitly assumes that the column should be enabled.
     */
    public ImportWizardModelColumn(ImportColumn column) {

        this(column, true);
    }

    /**
     * Creates a new instance for the given column.
     *
     * @param column Column that should be wrapped around
     * @param enabled
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
     * 
     *
     * @param column
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
