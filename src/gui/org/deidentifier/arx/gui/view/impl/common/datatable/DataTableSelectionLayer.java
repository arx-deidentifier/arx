package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class DataTableSelectionLayer extends SelectionLayer {

    private DataTableContext context;
    
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
