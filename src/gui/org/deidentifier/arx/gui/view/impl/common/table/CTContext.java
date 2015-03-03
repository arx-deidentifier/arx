/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
 * 
 * @author Fabian Prasser
 *
 */
public abstract class CTContext {
    
    /**  TODO */
    private boolean columnExpanded = false;
    
    /**  TODO */
    private boolean rowExpanded = false;
    
    /**
     * 
     *
     * @return
     */
    public abstract NatTable getTable();

    /**
     * 
     *
     * @return
     */
    public boolean isColumnExpanded() {
        return columnExpanded;
    }

    /**
     * 
     *
     * @return
     */
    public boolean isRowExpanded() {
        return rowExpanded;
    }

    /**
     * 
     *
     * @param columnExpanded
     */
    public void setColumnExpanded(boolean columnExpanded) {
        this.columnExpanded = columnExpanded;
    }

    /**
     * 
     *
     * @param rowExpanded
     */
    public void setRowExpanded(boolean rowExpanded) {
        this.rowExpanded = rowExpanded;
    }
}
