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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;

/**
 * A grid layer stack for the data view
 * @author Fabian Prasser
 *
 */
public class DataTableGridLayerStack extends DataTableGridLayer {

    /**
     * Creates a new instance
     * @param bodyDataProvider
     * @param table
     * @param context
     */
    public DataTableGridLayerStack(final IDataProvider bodyDataProvider, NatTable table, DataTableContext context) {
        super(true, table, context);
        List<String> lcolumns = new ArrayList<String>();
        RowSet rows = context.getRows();
        DataHandle handle = context.getHandle();
        String[][] data = context.getArray();
        if (bodyDataProvider.getColumnCount() != 0) {
            if (rows != null) {
                lcolumns.add("");
            }
            if (handle != null) {
                for (int i = 0; i < handle.getNumColumns(); i++) {
                    lcolumns.add(handle.getAttributeName(i));
                }
            } else if (data != null) {
                for (int i = 0; i < data[0].length; i++) {
                    lcolumns.add(data[0][i]);
                }
            }
        }
        String[] columns = lcolumns.toArray(new String[] {});
        final IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(columns);
        final IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        final IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider,
                                                                               rowHeaderDataProvider);
        init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
    }
}
