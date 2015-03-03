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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleSubset;
import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * A data provider based on a data handle.
 *
 * @author Fabian Prasser
 */
public class DataTableHandleDataProvider implements IDataProvider {

    /**  TODO */
    private final DataTableContext context;

    /**
     * Creates a new instance.
     *
     * @param context
     */
    public DataTableHandleDataProvider(final DataTableContext context) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        DataHandle data = context.getHandle();
        if (data == null || data.isOrphaned()) { return 0; }
        return data.getNumColumns() + (context.getRows() != null ? 1 : 0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getDataValue(int, int)
     */
    @Override
    public Object getDataValue(int arg0, int arg1) {
        DataHandle data = context.getHandle();
        if (data == null) { return null; }
        RowSet rows = context.getRows();
        if (rows == null) {
            return data.getValue(arg1, arg0);
        } else if (arg0 == 0) {
            // Remap row index for subset if in subset view
            if (data instanceof DataHandleSubset){
                int[] subset = ((DataHandleSubset)data).getSubset();
                arg1 = subset[arg1];
            }
            return rows.contains(arg1);
        } else {
            return data.getValue(arg1, arg0 - 1);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getRowCount()
     */
    @Override
    public int getRowCount() {
        DataHandle data = context.getHandle();
        if (data == null || data.isOrphaned()) { return 0; }
        return data.getNumRows();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#setDataValue(int, int, java.lang.Object)
     */
    @Override
    public void setDataValue(final int arg0, final int arg1, final Object arg2) {
        return;
    }
}
