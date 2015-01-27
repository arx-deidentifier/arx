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

package org.deidentifier.arx.gui.view.impl.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.deidentifier.arx.gui.view.impl.common.table.CTContext;
import org.deidentifier.arx.gui.view.impl.common.table.CTDataProvider;
import org.deidentifier.arx.gui.view.impl.common.table.FillLayerResetCommand;
import org.deidentifier.arx.gui.view.impl.common.table.DataProviderWrapped;
import org.deidentifier.arx.gui.view.impl.common.table.LayerBody;
import org.deidentifier.arx.gui.view.impl.common.table.LayerColumnHeader;
import org.deidentifier.arx.gui.view.impl.common.table.LayerRowHeader;
import org.deidentifier.arx.gui.view.impl.common.table.StyleConfigurationTable;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectColumnAction;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * A virtual table implemented with NatTable.
 *
 * @author Fabian Prasser
 */
public class ComponentTable implements IComponent {

    /**
     * Checkstyle.
     *
     * @param style
     * @return
     */
    private static int checkStyle(int style) {
        return style & (SWT.BORDER | SWT.NONE);
    }

    /** The parent. */
    private final Composite         root;
    
    /** The underlying nattable instance. */
    private final NatTable                table;
    
    /** The context. */
    private final CTContext context;
    
    /** Data provider. */
    private final CTDataProvider dataProviderRowHeader;
    
    /** Data provider. */
    private final CTDataProvider dataProviderColumnHeader;
    
    /** Data provider. */
    private final CTDataProvider dataProviderBody;
    
    /** State. */
    private Integer                 selectedRow        = null;
    
    /** State. */
    private Integer                 selectedColumn     = null;

    /** Body layer. */
    private final LayerBody         bodyLayer;

    /** Listeners. */
    private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param style
     */
    public ComponentTable(Composite parent, int style) {
        this(parent, style, new CTConfiguration(parent));
    }
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param style
     * @param config
     */
    public ComponentTable(final Composite parent, 
                          final int style, 
                          final CTConfiguration config) {
        
        // Check and store
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent must not be null");
        }
        this.root = new Composite(parent, checkStyle(style));
        this.root.setLayout(new FillLayout());
        
        // Create context
        this.context = new CTContext(){
            public NatTable getTable() {
                return table;
            }
        };
        
        // Create data providers
        this.dataProviderRowHeader = new DataProviderWrapped();
        this.dataProviderColumnHeader = new DataProviderWrapped();
        this.dataProviderBody = new DataProviderWrapped();
        
        // Create grid
        if (config.getStyle() == CTConfiguration.STYLE_GRID) {
            
            // Create layers
            bodyLayer = new LayerBody(this.dataProviderBody, config, context);
            LayerColumnHeader layerColumnHeader = new LayerColumnHeader(root,
                                                                        dataProviderColumnHeader,
                                                                        bodyLayer,
                                                                        config,
                                                                        context);
            LayerRowHeader layerRowHeader = new LayerRowHeader(root, dataProviderRowHeader, bodyLayer, config, context);
            CornerLayer layerCorner = new CornerLayer(new DataLayer(new DefaultCornerDataProvider(dataProviderColumnHeader,
                                                                                                  dataProviderRowHeader)),
                                                                    layerRowHeader,
                                                                    layerColumnHeader);
            GridLayer gridLayer = new GridLayer(bodyLayer, layerColumnHeader, layerRowHeader, layerCorner);

            // Create table
            this.table = new NatTable(root, gridLayer, false);
            this.table.addConfiguration(new StyleConfigurationTable(config));
            this.table.configure();
            this.addSelectionListener(bodyLayer.getSelectionLayer());
            
        // Create a table
        } else {

            // Create layers
            bodyLayer = new LayerBody(dataProviderBody, config, context);
            LayerColumnHeader layerColumnHeader = new LayerColumnHeader(root,
                                                                        dataProviderColumnHeader,
                                                                        bodyLayer,
                                                                        config,
                                                                        context);
            CompositeLayer layerComposite = new CompositeLayer(1, 2);
            layerComposite.setChildLayer(GridRegion.BODY, bodyLayer, 0, 1);
            layerComposite.setChildLayer(GridRegion.COLUMN_HEADER, layerColumnHeader, 0, 0);

            // Make corner resizable
            layerComposite.addConfiguration(new AbstractUiBindingConfiguration() {

                @Override
                public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                    uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON), new SelectCellAction(){
                        @Override
                        public void run(NatTable natTable, MouseEvent event) {
                            if (config == null || config.isCellSelectionEnabled()) super.run(natTable, event);
                        }
                    });
                    uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON), new ViewportSelectColumnAction(true, true){
                        @Override
                        public void run(NatTable natTable, MouseEvent event) {
                            if (config == null || config.isColumnSelectionEnabled()) super.run(natTable, event);
                        }
                    });
                    uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.ROW_HEADER, MouseEventMatcher.RIGHT_BUTTON), new ViewportSelectRowAction(true, true){
                        @Override
                        public void run(NatTable natTable, MouseEvent event) {
                            if (config == null || config.isRowSelectionEnabled()) super.run(natTable, event);
                        }
                    });
                }
            });
            
            // Create table
            this.table = new NatTable(root, layerComposite, false);
            this.table.addConfiguration(new StyleConfigurationTable(config));
            this.table.configure();
            this.addSelectionListener(bodyLayer.getSelectionLayer());
        }
    }

    /**
     * Adds a listener.
     *
     * @param arg0
     */
    public void addMouseListener(MouseListener arg0) {
        table.addMouseListener(arg0);
    }

    /**
     * Adds a selection listener.
     *
     * @param e
     * @return
     */
    public boolean addSelectionListener(SelectionListener e) {
        return selectionListeners.add(e);
    }

    /**
     * Clears the table.
     */
    public void clear() {

        this.table.doCommand(new FillLayerResetCommand());
        this.dataProviderBody.clear();
        this.dataProviderColumnHeader.clear();
        this.dataProviderRowHeader.clear();
        this.table.refresh();
        this.selectedRow = null;
        this.selectedColumn = null;
    }

    /**
     * Returns the backing widget.
     *
     * @return
     */
    public Control getControl() {
        return this.root;
    }

    /**
     * 
     * Returns the selected column, or null.
     *
     * @return
     */
    public Integer getSelectedColumn() {
        return selectedColumn;
    }

    /**
     * Returns the selected row, or null.
     *
     * @return
     */
    public Integer getSelectedRow() {
        return selectedRow;
    }
    
    /**
     * Redraws the table.
     */
    public void refresh() {
        this.table.refresh();
        if (this.selectedColumn == null ||
            this.selectedColumn >= dataProviderBody.getColumnCount() ||
            this.dataProviderBody.getColumnCount() == 0) {
            this.selectedColumn = null;
        }
        if (this.selectedRow == null ||
            this.selectedRow >= dataProviderBody.getRowCount() ||
            this.dataProviderBody.getRowCount() == 0) {
            this.selectedRow = null;
        }
    }
    
    /**
     * Removes a listener.
     *
     * @param arg0
     */
    public void removeMouseListener(MouseListener arg0) {
        table.removeMouseListener(arg0);
    }

    /**
     * Removes a selection listener.
     *
     * @param index
     * @return
     */
    public SelectionListener removeSelectionListener(int index) {
        return selectionListeners.remove(index);
    }
    
    /**
     * Updates the underlying table.
     *
     * @param data
     */
    public void setData(IDataProvider data) {
        this.setData(data, 
                     createRowHeaderDataProvider(data.getRowCount()), 
                     createColumnHeaderDataProvider(data.getColumnCount()));
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     * @param columns
     */
    public void setData(IDataProvider data, IDataProvider columns) {
        this.setData(data, 
                     createRowHeaderDataProvider(data.getRowCount()), 
                     columns);
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     * @param rows
     * @param columns
     */
    public void setData(IDataProvider data, 
                        IDataProvider rows,
                        IDataProvider columns) {
        // Disable redrawing
        this.root.setRedraw(false);
        
        this.table.doCommand(new FillLayerResetCommand());
        this.dataProviderBody.setData(data);
        this.dataProviderColumnHeader.setData(columns);
        this.dataProviderRowHeader.setData(rows);
        this.table.refresh();
        
        // Redraw
        this.root.setRedraw(true);
        this.root.layout(true);
        
        // Reset state
        this.selectedRow = null;
        this.selectedColumn = null;
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     * @param columns
     */
    public void setData(IDataProvider data, String[] columns) {
        this.setData(data,  createRowHeaderDataProvider(data.getRowCount()), createColumnHeaderDataProvider(columns));
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     * @param rows
     * @param columns
     */
    public void setData(IDataProvider data, String[] rows, String[] columns) {
        this.setData(data,  createRowHeaderDataProvider(rows), createColumnHeaderDataProvider(columns));
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     */
    public void setData(String[][] data) {
        this.setData(createBodyDataProvider(data));
    }

    
    /**
     * Updates the underlying table.
     *
     * @param data
     * @param columns
     */
    public void setData(String[][] data, String[] columns) {
        IDataProvider body = createBodyDataProvider(data);
        setData(body, createRowHeaderDataProvider(body.getRowCount()), createColumnHeaderDataProvider(columns));
    }

    /**
     * Updates the underlying table.
     *
     * @param data
     * @param rows
     * @param columns
     */
    public void setData(String[][] data, String[] rows, String[] columns) {
        setData(createBodyDataProvider(data), rows, columns);
    }

    /**
     * To display coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    public Point toDisplay(int x, int y) {
        return table.toDisplay(x, y);
    }

    /**
     * Action.
     *
     * @param arg0
     * @return
     */
    private boolean actionCellSelected(CellSelectionEvent arg0) {

        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int column = arg0.getColumnPosition();
        int row = arg0.getRowPosition();
        if (column>=0 && row>=0 && row<dataProviderBody.getRowCount() && column<dataProviderBody.getColumnCount()){
            this.selectedColumn = column;
            this.selectedRow = row;
            fireSelectionEvent();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Action.
     *
     * @param arg0
     * @return
     */
    private boolean actionColumnSelected(ColumnSelectionEvent arg0) {
        
        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int column = arg0.getColumnPositionRanges().iterator().next().start;
        if (column>=0 && column<dataProviderBody.getColumnCount()){
            this.selectedColumn = column;
            fireSelectionEvent();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Action.
     *
     * @param arg0
     * @return
     */
    private boolean actionRowSelected(RowSelectionEvent arg0) {
        
        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int row = arg0.getRowPositionRanges().iterator().next().start;
        if (row>=0 && row<dataProviderBody.getRowCount()){
            this.selectedRow = row;
            fireSelectionEvent();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a selection listener.
     *
     * @param layer
     */
    private void addSelectionListener(final SelectionLayer layer) {
        layer.addLayerListener(new ILayerListener(){
            @Override
            public void handleLayerEvent(ILayerEvent arg0) {
                if (arg0 instanceof CellSelectionEvent) {
                    if (!actionCellSelected((CellSelectionEvent)arg0)){
                        layer.clear(true);
                    }
                } else if (arg0 instanceof ColumnSelectionEvent) {
                    if (!actionColumnSelected((ColumnSelectionEvent)arg0)){
                        layer.clear(true);
                    }
                } else if (arg0 instanceof RowSelectionEvent) {
                    if (!actionRowSelected((RowSelectionEvent)arg0)) {
                        layer.clear(true);
                    }
                }
            }
        });

    }

    /**
     * 
     *
     * @param data
     * @return
     */
    private IDataProvider createBodyDataProvider(final String[][] data) {

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
     * 
     *
     * @param length
     * @return
     */
    private IDataProvider createColumnHeaderDataProvider(final int length) {

        return new IDataProvider(){

            @Override
            public int getColumnCount() {
                return length;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return columnIndex;
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                // Ignore
            }
        };
    }

    /**
     * 
     *
     * @param data
     * @return
     */
    private IDataProvider createColumnHeaderDataProvider(final String[] data) {

        return new IDataProvider(){

            @Override
            public int getColumnCount() {
                return data.length;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return data[columnIndex];
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                // Ignore
            }
        };
    }

    /**
     * 
     *
     * @param length
     * @return
     */
    private IDataProvider createRowHeaderDataProvider(final int length) {

        return new IDataProvider(){

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return rowIndex;
            }

            @Override
            public int getRowCount() {
                return length;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                // Ignore
            }
        };
    }

    /**
     * 
     *
     * @param data
     * @return
     */
    private IDataProvider createRowHeaderDataProvider(final String[] data) {

        return new IDataProvider(){

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return data[rowIndex];
            }

            @Override
            public int getRowCount() {
                return data.length;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                // Ignore
            }
        };
    }
    
    /**
     * Fires a new event.
     */
    private void fireSelectionEvent(){
        Event event = new Event();
        event.display = table.getDisplay();
        event.item = table;
        event.widget = table;
        SelectionEvent sEvent = new SelectionEvent(event);
        for (SelectionListener listener : selectionListeners) {
            listener.widgetSelected(sEvent);
        }
    }

    /**
     * Updates the selection.
     *
     * @param row
     * @param column
     */
    public void setSelection(int row, int column) {
        this.table.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(), 
                                                   column, 
                                                   row, 
                                                   false,
                                                   false));
    }
}