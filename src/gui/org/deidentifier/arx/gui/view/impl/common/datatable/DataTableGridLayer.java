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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * A grid layer for the data view.
 *
 * @author Fabian Prasser
 */
public class DataTableGridLayer extends GridLayer {

    /**  TODO */
    protected IUniqueIndexLayer bodyDataLayer;
    
    /**  TODO */
    protected IUniqueIndexLayer columnHeaderDataLayer;
    
    /**  TODO */
    protected IUniqueIndexLayer rowHeaderDataLayer;
    
    /**  TODO */
    protected IUniqueIndexLayer cornerDataLayer;
    
    /**  TODO */
    private DataTableContext    context;
    
    /**  TODO */
    private NatTable            table;

    /**
     * Creates a new instance.
     *
     * @param useDefaultConfiguration
     * @param table
     * @param context
     */
    protected DataTableGridLayer(boolean useDefaultConfiguration, NatTable table, DataTableContext context) {
        super(useDefaultConfiguration);
        this.context = context;
        this.table = table;
    }

    /**
     * Returns the body data layer.
     *
     * @return
     */
    public IUniqueIndexLayer getBodyDataLayer() {
        return bodyDataLayer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer#getBodyLayer()
     */
    @Override
    public DataTableBodyLayerStack getBodyLayer() {
        return (DataTableBodyLayerStack) super.getBodyLayer();
    }

    /**
     * Returns the column header layer.
     *
     * @return
     */
    public IUniqueIndexLayer getColumnHeaderDataLayer() {
        return columnHeaderDataLayer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer#getColumnHeaderLayer()
     */
    @Override
    public ColumnHeaderLayer getColumnHeaderLayer() {
        return (ColumnHeaderLayer) super.getColumnHeaderLayer();
    }

    /**
     * Returns the corner data layer.
     *
     * @return
     */
    public IUniqueIndexLayer getCornerDataLayer() {
        return cornerDataLayer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer#getCornerLayer()
     */
    @Override
    public CornerLayer getCornerLayer() {
        return (CornerLayer) super.getCornerLayer();
    }

    /**
     * Returns the row header layer.
     *
     * @return
     */
    public IUniqueIndexLayer getRowHeaderDataLayer() {
        return rowHeaderDataLayer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer#getRowHeaderLayer()
     */
    @Override
    public RowHeaderLayer getRowHeaderLayer() {
        return (RowHeaderLayer) super.getRowHeaderLayer();
    }

    /**
     * Initialize.
     *
     * @param bodyDataProvider
     * @param columnHeaderDataProvider
     * @param rowHeaderDataProvider
     * @param cornerDataProvider
     * @param parent
     */
    protected void init(IDataProvider bodyDataProvider,
                        IDataProvider columnHeaderDataProvider,
                        IDataProvider rowHeaderDataProvider,
                        IDataProvider cornerDataProvider,
                        Control parent) {
        init(new DataLayer(bodyDataProvider),
             new DefaultColumnHeaderDataLayer(columnHeaderDataProvider),
             new DefaultRowHeaderDataLayer(rowHeaderDataProvider),
             new DataLayer(cornerDataProvider),
             parent);
    }

    /**
     * Initialize.
     *
     * @param bodyDataLayer
     * @param columnHeaderDataLayer
     * @param rowHeaderDataLayer
     * @param cornerDataLayer
     * @param parent
     */
    protected void init(final IUniqueIndexLayer bodyDataLayer,
                        IUniqueIndexLayer columnHeaderDataLayer,
                        IUniqueIndexLayer rowHeaderDataLayer,
                        IUniqueIndexLayer cornerDataLayer,
                        Control parent) {
        // Body
        this.bodyDataLayer = bodyDataLayer;
        DataTableBodyLayerStack bodyLayer = new DataTableBodyLayerStack(bodyDataLayer, table, context, parent);

        SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

        // Column header
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, selectionLayer);
        
        // Configure the column resize action
        ((ColumnHeaderLayer) columnHeaderLayer).addConfiguration(new AbstractUiBindingConfiguration() {
            @Override
            public void configureUiBindings(UiBindingRegistry paramUiBindingRegistry) {
                paramUiBindingRegistry.registerFirstDoubleClickBinding(new ColumnResizeEventMatcher(SWT.NONE,
                                                                                                    GridRegion.COLUMN_HEADER,
                                                                                                    MouseEventMatcher.LEFT_BUTTON),
                                                                       new DataTableResizeColumnAction(bodyDataLayer));
            }
        });

        // Row header
        this.rowHeaderDataLayer = rowHeaderDataLayer;
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, selectionLayer);
        bodyLayer.setRowHeaderLayer(rowHeaderLayer);
        
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
}
