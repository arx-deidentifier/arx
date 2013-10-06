package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class DataTableBodyLayerStack extends AbstractLayerTransform {

    private final SelectionLayer selectionLayer;
    private final ViewportLayer  viewportLayer;

    public DataTableBodyLayerStack(IUniqueIndexLayer underlyingLayer, NatTable table, DataTableContext context) {
        this.selectionLayer = new SelectionLayer(underlyingLayer);
        this.viewportLayer = new ViewportLayer(selectionLayer);
        this.setUnderlyingLayer(viewportLayer);
        this.setConfigLabelAccumulator(new DataTableConfigLabelAccumulator(table, context));
        this.registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
    }

    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    public ViewportLayer getViewportLayer() {
        return viewportLayer;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }
}
