package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class DataTableGridLayer extends GridLayer {

    protected IUniqueIndexLayer bodyDataLayer;
    protected IUniqueIndexLayer columnHeaderDataLayer;
    protected IUniqueIndexLayer rowHeaderDataLayer;
    protected IUniqueIndexLayer cornerDataLayer;
    private DataTableContext    context;
    private NatTable            table;

    protected DataTableGridLayer(boolean useDefaultConfiguration, NatTable table, DataTableContext context) {
        super(useDefaultConfiguration);
        this.context = context;
        this.table = table;
    }

    public IUniqueIndexLayer getBodyDataLayer() {
        return bodyDataLayer;
    }

    @Override
    public DataTableBodyLayerStack getBodyLayer() {
        return (DataTableBodyLayerStack) super.getBodyLayer();
    }

    public IUniqueIndexLayer getColumnHeaderDataLayer() {
        return columnHeaderDataLayer;
    }

    @Override
    public ColumnHeaderLayer getColumnHeaderLayer() {
        return (ColumnHeaderLayer) super.getColumnHeaderLayer();
    }

    public IUniqueIndexLayer getCornerDataLayer() {
        return cornerDataLayer;
    }

    @Override
    public CornerLayer getCornerLayer() {
        return (CornerLayer) super.getCornerLayer();
    }

    public IUniqueIndexLayer getRowHeaderDataLayer() {
        return rowHeaderDataLayer;
    }

    @Override
    public RowHeaderLayer getRowHeaderLayer() {
        return (RowHeaderLayer) super.getRowHeaderLayer();
    }

    protected void init(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider) {
        init(bodyDataProvider, columnHeaderDataProvider, new DefaultRowHeaderDataProvider(bodyDataProvider));
    }

    protected void init(IDataProvider bodyDataProvider,
                        IDataProvider columnHeaderDataProvider,
                        IDataProvider rowHeaderDataProvider) {
        init(bodyDataProvider,
             columnHeaderDataProvider,
             rowHeaderDataProvider,
             new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider));
    }

    protected void init(IDataProvider bodyDataProvider,
                        IDataProvider columnHeaderDataProvider,
                        IDataProvider rowHeaderDataProvider,
                        IDataProvider cornerDataProvider) {
        init(new DataLayer(bodyDataProvider),
             new DefaultColumnHeaderDataLayer(columnHeaderDataProvider),
             new DefaultRowHeaderDataLayer(rowHeaderDataProvider),
             new DataLayer(cornerDataProvider));
    }

    protected void init(IUniqueIndexLayer bodyDataLayer,
                        IUniqueIndexLayer columnHeaderDataLayer,
                        IUniqueIndexLayer rowHeaderDataLayer,
                        IUniqueIndexLayer cornerDataLayer) {
        // Body
        this.bodyDataLayer = bodyDataLayer;
        DataTableBodyLayerStack bodyLayer = new DataTableBodyLayerStack(bodyDataLayer, table, context);

        SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

        // Column header
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, selectionLayer);

        // Row header
        this.rowHeaderDataLayer = rowHeaderDataLayer;
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, selectionLayer);

        // Corner
        this.cornerDataLayer = cornerDataLayer;
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // Attach the listeners
        for (ILayerListener listener : context.getListeners()) {
            selectionLayer.addLayerListener(listener);
        }

        setBodyLayer(bodyLayer);
        setColumnHeaderLayer(columnHeaderLayer);
        setRowHeaderLayer(rowHeaderLayer);
        setCornerLayer(cornerLayer);
    }

    protected <T> void init(List<T> rowData, String[] propertyNames, Map<String, String> propertyToLabelMap) {
        init(new DefaultBodyDataProvider<T>(rowData, propertyNames),
             new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));
    }

}
