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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * A selection layer for data views
 * @author Fabian Prasser
 *
 */
public class DataTableSelectionLayer extends SelectionLayer {

    private DataTableContext context;
    
    /**
     * Creates a new instance
     * @param underlyingLayer
     * @param context
     */
    public DataTableSelectionLayer(IUniqueIndexLayer underlyingLayer, DataTableContext context) {
        super(underlyingLayer);
        this.context = context;
    }

    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        return false;
    }

    @Override
    public boolean isColumnPositionFullySelected(int columnPosition) {
        return false;
    }

    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        return columnPosition-1==context.getSelectedIndex();
    }

    @Override
    public boolean isRowPositionFullySelected(int rowPosition) {
        return false;
    }

    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        return false;
    }
}
