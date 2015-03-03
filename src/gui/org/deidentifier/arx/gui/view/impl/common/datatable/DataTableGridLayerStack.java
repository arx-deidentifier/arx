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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.swt.widgets.Control;

/**
 * A grid layer stack for the data view.
 *
 * @author Fabian Prasser
 */
public class DataTableGridLayerStack extends DataTableGridLayer {

    /**
     * Creates a new instance.
     *
     * @param bodyDataProvider
     * @param table
     * @param context
     * @param parent
     */
    public DataTableGridLayerStack(final IDataProvider bodyDataProvider, NatTable table, DataTableContext context, Control parent) {
        super(true, table, context);
        List<String> lcolumns = new ArrayList<String>();
        RowSet rows = context.getRows();
        DataHandle handle = context.getHandle();
        if (bodyDataProvider.getColumnCount() != 0) {
            if (rows != null) {
                lcolumns.add("");
            }
            if (handle != null) {
                for (int i = 0; i < handle.getNumColumns(); i++) {
                    lcolumns.add(handle.getAttributeName(i));
                }
            } 
        }
        String[] columns = lcolumns.toArray(new String[] {});
        final IDataProvider columnHeaderDataProvider = new DataTableDataProvider(new DefaultColumnHeaderDataProvider(columns));
        final IDataProvider rowHeaderDataProvider = new DataTableDataProvider(new DefaultRowHeaderDataProvider(bodyDataProvider));
        final IDataProvider cornerDataProvider = new DataTableDataProvider(new DefaultCornerDataProvider(columnHeaderDataProvider,
                                                                               rowHeaderDataProvider));
        init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider, parent);
    }
}
