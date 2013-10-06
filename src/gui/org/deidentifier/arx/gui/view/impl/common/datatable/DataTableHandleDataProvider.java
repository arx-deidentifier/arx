package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DataTableHandleDataProvider implements IDataProvider {

    private final DataHandle       data;
    private final DataTableContext context;

    public DataTableHandleDataProvider(final DataHandle data, final DataTableContext context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getColumnCount() {
        if (data == null) { return 0; }
        return data.getNumColumns() + (context.getRows() != null ? 1 : 0);
    }

    @Override
    public Object getDataValue(final int arg0, final int arg1) {
        if (data == null) { return null; }
        RowSet rows = context.getRows();
        if (rows == null) {
            return data.getValue(arg1, arg0);
        } else if (arg0 == 0) {
            return rows.contains(arg1);
        } else {
            return data.getValue(arg1, arg0 - 1);
        }
    }

    @Override
    public int getRowCount() {
        if (data == null) { return 0; }
        return data.getNumRows();
    }

    @Override
    public void setDataValue(final int arg0, final int arg1, final Object arg2) {
        return;
    }
}
