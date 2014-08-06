/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.common;

import java.util.Arrays;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * A virtual table implemented with NatTable
 * 
 * @author Fabian Prasser
 */
public class ComponentTable implements IComponent {

    /**
     * The body layer
     * @author Fabian Prasser
     */
    private static class BodyLayerStack extends AbstractLayerTransform {
        
        /** Selection layer*/
        private SelectionLayer selectionLayer;
        /** Data layer*/
        private DataLayer dataLayer;

        /**
         * Creates a new instance
         * @param dataProvider
         */
        public BodyLayerStack(IDataProvider dataProvider) {
            dataLayer = new DataLayer(dataProvider);
            selectionLayer = new SelectionLayer(dataLayer);
            ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
            setUnderlyingLayer(viewportLayer);
        }

        /**
         * Returns the selection layer
         * @return
         */
        public SelectionLayer getSelectionLayer() {
            return selectionLayer;
        }
        
        /**
         * Returns the data layer
         * @return
         */
        public DataLayer getDataLayer(){
            return dataLayer;
        }
    }

    /**
     * The column layer
     * @author Fabian Prasser
     */
    public class ColumnHeaderLayerStack extends AbstractLayerTransform {
        
        /**
         * Creates a new instance
         * @param dataProvider
         * @param bodyLayer
         */
        public ColumnHeaderLayerStack(IDataProvider dataProvider,
                                      BodyLayerStack bodyLayer) {
            
            DataLayer dataLayer = new DataLayer(dataProvider);
            ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer,
                                                                     bodyLayer,
                                                                     bodyLayer.getSelectionLayer());
            setUnderlyingLayer(colHeaderLayer);
        }
    }

    /**
     * The row layer
     * @author Fabian Prasser
     */
    private static class RowHeaderLayerStack extends AbstractLayerTransform {
        
        /**
         * Creates a new instance
         * @param dataProvider
         * @param bodyLayer
         */
        public RowHeaderLayerStack(IDataProvider dataProvider,
                                   BodyLayerStack bodyLayer) {
            DataLayer dataLayer = new DataLayer(dataProvider, 50, 20);
            RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(dataLayer, bodyLayer, bodyLayer.getSelectionLayer());
            setUnderlyingLayer(rowHeaderLayer);
        }
    }
    
    /** The parent*/
    private final Composite parent;
    /** The underlying nattable instance*/
    private NatTable table = null;
    /** The layout data*/
    private Object layoutData = null;
    /** The layout*/
    private ComponentTableLayout layout = new ComponentTableLayout(true, 100);

    /**
     * Creates a new instance
     * @param parent
     */
    public ComponentTable(Composite parent) {
        this.parent = parent;  
    }

    /**
     * Updates the underlying table. Hides the row header.
     * @param dataProvider
     * @param columns
     */
    public void setTable(IDataProvider dataProvider, String[] columns) {

        // Disable redrawing
        this.parent.setRedraw(false);
        
        // Dispose
        if (table != null && !table.isDisposed()) {
            table.dispose();
        }

        // Create data providers
        IDataProvider columnHeaderDataProvider = getHeaderDataProvider(dataProvider, columns, false);

        // Create layers
        BodyLayerStack bodyLayer = new BodyLayerStack(dataProvider);
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(columnHeaderDataProvider, bodyLayer);
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayer, 0, 1);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);

        // Create table
        table = new NatTable(parent, compositeLayer);
        addColumnWidthHandler(table, dataProvider, bodyLayer.getDataLayer(), null);
        
        // Set layout
        if (this.layoutData != null) {
            table.setLayoutData(layoutData);
        }
        
        // Redraw
        this.parent.setRedraw(true);
        this.parent.layout(true);
    }
    
    /**
     * Sets the layout
     * @param layout
     */
    public void setLayout(ComponentTableLayout layout){
        this.layout = layout;
        if (this.table != null && !this.table.isDisposed()) {
            parent.layout(true);
        }
    }
    
    /**
     * Updates the underlying table. Hides the row header.
     * @param data
     * @param columns
     */
    public void setTable(String[][] data, String[] columns) {
        setTable(getDataProvider(data), columns);
    }
    /**
     * Updates the underlying table
     * @param data
     */
    public void setData(IDataProvider dataProvider) {
        this.setData(dataProvider, null, null);
    }
    
    /**
     * Updates the underlying table
     * @param data
     */
    public void setData(String[][] data) {
        this.setData(data, null, null);
    }

    /**
     * Updates the underlying data
     * @param dataProvider
     * @param rows May be null
     * @param columns May be null
     */
    public void setData(IDataProvider dataProvider, String[] rows, String[] columns) {

        // Disable redrawing
        this.parent.setRedraw(false);
        
        // Dispose
        if (table != null && !table.isDisposed()) {
            table.dispose();
        }

        // Create data providers
        IDataProvider rowHeaderDataProvider = getHeaderDataProvider(dataProvider, rows, true);
        IDataProvider columnHeaderDataProvider = getHeaderDataProvider(dataProvider, columns, false);
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);

        // Create layers
        BodyLayerStack bodyLayer = new BodyLayerStack(dataProvider);
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(columnHeaderDataProvider, bodyLayer);
        RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(rowHeaderDataProvider, bodyLayer);
        CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);
        GridLayer gridLayer = new GridLayer(bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer); 
        
        // Create table
        table = new NatTable(parent, gridLayer);
        addColumnWidthHandler(table, dataProvider, bodyLayer.getDataLayer(), cornerLayer);
        
        // Set layout
        if (this.layoutData != null) {
            table.setLayoutData(layoutData);
        }
        
        // Redraw
        this.parent.setRedraw(true);
        this.parent.layout(true);
    }

    /**
     * Adds a handler for automatically resizing columns
     * @param table
     * @param dataProvider
     * @param dataLayer
     * @param cornerLayer
     */
    private void addColumnWidthHandler(final NatTable table, 
                                       final IDataProvider dataProvider, 
                                       final DataLayer dataLayer,
                                       final CornerLayer cornerLayer) {
        table.addControlListener(new ControlAdapter(){
            public void controlResized(ControlEvent arg0) {
                
                parent.setRedraw(false);
                
                // Prepare
                int width = table.getSize().x;
                
                // Check if larger than parent
                int total = layout.columnWidth * dataProvider.getColumnCount();
                total += cornerLayer != null ? cornerLayer.getColumnWidthByPosition(0) : 0;
                if (total >= width) {
                    for (int i=0; i<dataProvider.getColumnCount(); i++){
                        dataLayer.setColumnWidthByPosition(i, layout.columnWidth);
                    }
                } else {
                    // If not, extend to cover the whole area
                    int columnWidth = width;
                    columnWidth -= (cornerLayer != null) ? cornerLayer.getColumnWidthByPosition(0) : 0;
                    columnWidth = (int)Math.round((double)columnWidth / (double)dataProvider.getColumnCount());
                    total = (cornerLayer != null) ? cornerLayer.getColumnWidthByPosition(0) : 0;
                    for (int i=0; i<dataProvider.getColumnCount(); i++){
                        if (total + columnWidth > width) {
                            columnWidth = width - total;
                        }
                        dataLayer.setColumnWidthByPosition(i, columnWidth);
                        total += columnWidth;
                    }   
                }
                
                parent.setRedraw(true);
                table.redraw();
            } 
        });
    }

    /**
     * Updates the underlying data
     * @param data
     * @param rows May be null
     * @param columns May be null
     */
    public void setData(String[][] data, String[] rows, String[] columns) {
        setData(getDataProvider(data), rows, columns);
    }
    
    /**
     * Sets the layout data
     * @param data
     */
    public void setLayoutData(Object data){
        this.layoutData = data;
        if (table != null) table.setLayoutData(data);
    }
        
    private IDataProvider getHeaderDataProvider(final IDataProvider data, 
                                                final String[] header, 
                                                final boolean row) {
        if (header==null) {
            return new IDataProvider(){

                @Override
                public int getColumnCount() {
                    return row ? 1 : data.getColumnCount();
                }

                @Override
                public Object getDataValue(int arg0, int arg1) {
                    return row ? arg0 : arg1;
                }

                @Override
                public int getRowCount() {
                    return row ? data.getRowCount() : 1;
                }

                @Override
                public void setDataValue(int arg0, int arg1, Object arg2) {
                    // Ignore
                }
            };
        } else {
            return new IDataProvider(){
                @Override
                public int getColumnCount() {
                    return row ? 1 : data.getColumnCount();
                }

                @Override
                public Object getDataValue(int arg0, int arg1) {
                    return row ? header[arg1] : header[arg0];
                }

                @Override
                public int getRowCount() {
                    return row ? data.getRowCount() : 1;
                }

                @Override
                public void setDataValue(int arg0, int arg1, Object arg2) {
                    // Ignore
                }
            };
        }
    }

    private IDataProvider getDataProvider(final String[][] data) {

        return new ListDataProvider<String[]>(Arrays.asList(data), new IColumnAccessor<String[]>(){

            @Override
            public int getColumnCount() {
                return data==null || data.length==0 || data[0]==null ? 0 : data[0].length;
            }

            @Override
            public Object getDataValue(String[] arg0, int arg1) {
                return arg0[arg1];
            }

            @Override
            public void setDataValue(String[] arg0, int arg1, Object arg2) {
                arg0[arg1] = arg2.toString();
            }
        });
    }

    /**
     * Empties the table
     */
    public void setEmpty() {
        if (this.table == null || this.table.isDisposed()) return;
        this.parent.setRedraw(false);
        this.table.dispose();
        this.parent.setRedraw(true);
        this.parent.layout(true);
    }
}