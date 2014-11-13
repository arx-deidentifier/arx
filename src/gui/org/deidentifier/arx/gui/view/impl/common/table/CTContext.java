/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
