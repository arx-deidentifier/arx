/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * A DataProvider for the DataView
 * @author Fabian Prasser
 */
public class DataTableArrayDataProvider implements IDataProvider {

    private final String[][]       data;
    private final DataTableContext context;

    /**
     * Creates a new instance
     * @param data
     * @param context
     */
    public DataTableArrayDataProvider(final String[][] data, DataTableContext context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getColumnCount() {
        if (data == null) { return 0; }
        return data[0].length + (context.getRows() != null ? 1 : 0);
    }

    @Override
    public Object getDataValue(final int arg0, final int arg1) {
        if (data == null) { return null; }
        RowSet rows = context.getRows();
        if (rows == null) {
            return data[arg1][arg0];
        } else if (arg0 == 0) {
            return rows.contains(arg1);
        } else {
            return data[arg1][arg0 - 1];
        }
    }

    @Override
    public int getRowCount() {
        if (data == null) { return 0; }
        return data.length;
    }

    @Override
    public void setDataValue(final int arg0, final int arg1, final Object arg2) {
        return;
    }
}
