package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;

public class DataTableGridLayerStack extends DataTableGridLayer {

    public DataTableGridLayerStack(final IDataProvider bodyDataProvider, DataTableContext context) {
        super(true, context);
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
