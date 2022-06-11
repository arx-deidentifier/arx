/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.NatTable;

/**
 * Table context wrapper
 * @author Fabian Prasser
 *
 */
public abstract class CTContext {
    
    /**  Column */
    private boolean columnExpanded = false;
    
    /**  Row */
    private boolean rowExpanded = false;
    
    /**
     * Returns table
     *
     * @return
     */
    public abstract NatTable getTable();

    /**
     * Returns whether column is expanded
     *
     * @return
     */
    public boolean isColumnExpanded() {
        return columnExpanded;
    }

    /**
     * Returns whether row is expanded 
     *
     * @return
     */
    public boolean isRowExpanded() {
        return rowExpanded;
    }

    /**
     * Sets column expanded
     *
     * @param columnExpanded
     */
    public void setColumnExpanded(boolean columnExpanded) {
        this.columnExpanded = columnExpanded;
    }

    /**
     * Sets row expanded
     *
     * @param rowExpanded
     */
    public void setRowExpanded(boolean rowExpanded) {
        this.rowExpanded = rowExpanded;
    }
}
