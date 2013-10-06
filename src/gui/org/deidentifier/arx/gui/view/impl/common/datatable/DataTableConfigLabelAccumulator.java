package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

public class DataTableConfigLabelAccumulator implements IConfigLabelAccumulator {

    private final DataTableContext context;

    public DataTableConfigLabelAccumulator(DataTableContext context) {
        this.context = context;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {

        NatTable table = context.getTable();
        int[] groups = context.getGroups();
        RowSet rows = context.getRows();

        if (table != null && groups != null) {
            int row = table.getRowIndexByPosition(rowPosition + 1);
            configLabels.addLabel("background" + (groups[row] % 2)); //$NON-NLS-1$
            if (row < groups.length - 1 && groups[row] != groups[row + 1]) {
                configLabels.addLabel(DataTableDecorator.BOTTOM_LINE_BORDER_LABEL);
            }
        }

        if (table != null && rows != null) {
            int column = table.getColumnIndexByPosition(columnPosition + 1);
            if (column == 0) {
                configLabels.addLabel("checkbox"); //$NON-NLS-1$
            }
        }
    }
}
