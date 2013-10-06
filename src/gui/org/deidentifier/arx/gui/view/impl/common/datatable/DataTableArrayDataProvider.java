package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DataTableArrayDataProvider implements IDataProvider {

    private final String[][]       data;
    private final DataTableContext context;

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
