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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultColumnResizeBindings;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultRowResizeBindings;
import org.eclipse.nebula.widgets.nattable.search.config.DefaultSearchBindings;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultMoveSelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tickupdate.config.DefaultTickUpdateConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectColumnAction;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * A virtual table implemented with NatTable
 * 
 * @author Fabian Prasser
 */
public class ComponentTable implements IComponent {
    
/**
 * The table style configuration
 * @author Fabian Prasser
 *
 */
private class TableStyleConfiguration extends AbstractRegistryConfiguration {

    public Color bgColor = GUIHelper.COLOR_WHITE;
    public Color fgColor = GUIHelper.COLOR_BLACK;
    public Color gradientBgColor = GUIHelper.COLOR_WHITE;
    public Color gradientFgColor = GUIHelper.getColor(136, 212, 215);
    public Font font = root.getFont();
    public HorizontalAlignmentEnum hAlign = 
            config.alignment.horizontal == SWT.LEFT ? HorizontalAlignmentEnum.LEFT : 
            config.alignment.horizontal == SWT.RIGHT ? HorizontalAlignmentEnum.RIGHT : 
            HorizontalAlignmentEnum.CENTER ;
    public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
    public BorderStyle borderStyle = null;

    public ICellPainter cellPainter = new LineBorderDecorator(new TextPainter());
    
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter);

        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, gradientBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, gradientFgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
    
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
    }
}
    
    /**
     * The body layer
     * @author Fabian Prasser
     */
    private class BodyLayerStack extends AbstractLayerTransform {

        /** Selection layer */
        private SelectionLayer selectionLayer;
        /** Data layer */
        private DataLayer      dataLayer;

        /**
         * Creates a new instance
         * @param dataProvider
         */
        public BodyLayerStack(IDataProvider dataProvider) {
            dataLayer = new DataLayer(dataProvider);
            selectionLayer = new SelectionLayerStack(dataLayer);
            selectionLayer.addConfiguration(new SelectionStyleConfiguration());
            ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
            setUnderlyingLayer(viewportLayer);
        }

        /**
         * Returns the data layer
         * @return
         */
        public DataLayer getDataLayer(){
            return dataLayer;
        }
        
        /**
         * Returns the selection layer
         * @return
         */
        public SelectionLayer getSelectionLayer() {
            return selectionLayer;
        }
    }

    /**
     * The column layer
     * @author Fabian Prasser
     */
    private class ColumnHeaderLayerStack extends AbstractLayerTransform {
        
        /**
         * Creates a new instance
         * @param parent
         * @param dataProvider
         * @param bodyLayer
         */
        public ColumnHeaderLayerStack(Composite parent,
                                      IDataProvider dataProvider,
                                      BodyLayerStack bodyLayer) {
            
            DataLayer dataLayer = new DataLayer(dataProvider);
            ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer,
                                                                     bodyLayer,
                                                                     bodyLayer.getSelectionLayer(),
                                                                     false);
            colHeaderLayer.addConfiguration(new HeaderStyleConfiguration(parent, GridRegion.COLUMN_HEADER));
            colHeaderLayer.addConfiguration(new DefaultColumnResizeBindings());
            setUnderlyingLayer(colHeaderLayer);
        }
    }
    
    /**
     * Header style
     * @author Fabian Prasser
     */
    private class HeaderStyleConfiguration extends AbstractRegistryConfiguration {

        private final Font                    font;
        private final Color                   bgColor         = GUIHelper.COLOR_WIDGET_BACKGROUND;
        private final Color                   fgColor         = GUIHelper.COLOR_WIDGET_FOREGROUND;
        private final Color                   gradientBgColor = GUIHelper.COLOR_WHITE;
        private final Color                   gradientFgColor = GUIHelper.getColor(136,
                                                                                   212,
                                                                                   215);
        private final HorizontalAlignmentEnum hAlign          = HorizontalAlignmentEnum.CENTER;
        private final VerticalAlignmentEnum   vAlign          = VerticalAlignmentEnum.MIDDLE;
        private final BorderStyle             borderStyle     = null;
        private final ICellPainter            cellPainter     = new BeveledBorderDecorator(new TextPainter());
        private final Boolean                 renderGridLines = Boolean.FALSE;
        private final String                  region;
        
        /**
         * Creates a new instance
         * @param parent
         * @param region
         */
        public HeaderStyleConfiguration(Composite parent, String region){
            this.font = parent.getFont();
            this.region = region;
        }
        
        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            //configure the painter
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    cellPainter, 
                    DisplayMode.NORMAL, 
                    region);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    cellPainter, 
                    DisplayMode.NORMAL, 
                    GridRegion.CORNER);

            //configure whether to render grid lines or not
            //e.g. for the BeveledBorderDecorator the rendering of the grid lines should be disabled
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, 
                    renderGridLines, 
                    DisplayMode.NORMAL, 
                    region);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, 
                    renderGridLines, 
                    DisplayMode.NORMAL, 
                    GridRegion.CORNER);
            
            //configure the normal style
            Style cellStyle = new Style();
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, gradientBgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, gradientFgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
            cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
            cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
            cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.NORMAL, 
                    region);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.NORMAL, 
                    GridRegion.CORNER);
        }
    }

    /**
     * The row layer
     * @author Fabian Prasser
     */
    private class RowHeaderLayerStack extends AbstractLayerTransform {
        
        /**
         * Creates a new instance
         * @param parent
         * @param dataProvider
         * @param bodyLayer
         */
        public RowHeaderLayerStack(Composite parent,
                                   IDataProvider dataProvider,
                                   BodyLayerStack bodyLayer) {
            DataLayer dataLayer = new DataLayer(dataProvider, 50, 20);
            RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(dataLayer, bodyLayer, bodyLayer.getSelectionLayer(), false);
            rowHeaderLayer.addConfiguration(new HeaderStyleConfiguration(parent, GridRegion.ROW_HEADER));
            rowHeaderLayer.addConfiguration(new DefaultRowResizeBindings());
            setUnderlyingLayer(rowHeaderLayer);
        }
    }

    /**
     * A selection layer for table views
     * @author Fabian Prasser
     *
     */
    private class SelectionLayerStack extends SelectionLayer {

        /**
         * Creates a new instance
         * @param underlyingLayer
         */
        public SelectionLayerStack(IUniqueIndexLayer underlyingLayer) {
            super(underlyingLayer, false);
            addConfiguration(new DefaultSelectionStyleConfiguration());
            addConfiguration(new DefaultSelectionBindings(){
                /** Override some default behavior */
                protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                    IMouseAction action = new SelectCellAction();
                    uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
                }
                /** Override some default behavior */
                protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
                    // Ignore
                }
                /** Override some default behavior */
                protected void configureColumnHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                    uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.NONE), new ViewportSelectColumnAction(false, false));
                }
                /** Override some default behavior */
                protected void configureRowHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                    uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.NONE), new ViewportSelectRowAction(false, false));
                }
            });
            addConfiguration(new DefaultSearchBindings());
            addConfiguration(new DefaultTickUpdateConfiguration());
            addConfiguration(new DefaultMoveSelectionConfiguration());
        }

        @Override
        public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
            return config.selection.cell && super.isCellPositionSelected(columnPosition, rowPosition);
        }

        @Override
        public boolean isColumnPositionFullySelected(int columnPosition) {
            return config.selection.column && super.isColumnPositionFullySelected(columnPosition);
        }

        @Override
        public boolean isColumnPositionSelected(int columnPosition) {
            return false;
        }

        @Override
        public boolean isRowPositionFullySelected(int rowPosition) {
            return config.selection.row && super.isRowPositionFullySelected(rowPosition);
        }

        @Override
        public boolean isRowPositionSelected(int rowPosition) {
            return false;
        }
    }
    /**
     * Sets up rendering style used for selected areas and the selection anchor.
     */
    private class SelectionStyleConfiguration extends AbstractRegistryConfiguration {

        // General style
        public Font        font                       = root.getFont();
        public Color       selectionBgColor           = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
        public Color       selectionFgColor           = GUIHelper.COLOR_BLACK;
        public Color       selectedHeaderBgColor      = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
        public Color       selectedHeaderFgColor      = GUIHelper.COLOR_BLACK;
        public BorderStyle selectedHeaderBorderStyle  = new BorderStyle(-1,
                                                                        selectedHeaderFgColor,
                                                                        LineStyleEnum.SOLID);
        public Color       fullySelectedHeaderBgColor = GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configureSelectionStyle(configRegistry);
            configureSelectionAnchorStyle(configRegistry);
            configureHeaderHasSelectionStyle(configRegistry);
            configureHeaderFullySelectedStyle(configRegistry);
        }

        protected void configureHeaderFullySelectedStyle(IConfigRegistry configRegistry) {
            // Header fully selected
            Style cellStyle = new Style();  
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fullySelectedHeaderBgColor);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
        }

        protected void configureHeaderHasSelectionStyle(IConfigRegistry configRegistry) {
            Style cellStyle = new Style();

            cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectedHeaderFgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectedHeaderBgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
            
            switch (config.alignment.horizontal) {
                case SWT.LEFT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                    break;
                case SWT.RIGHT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                    break;
                case SWT.CENTER:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                    break;
            }
            cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectedHeaderBorderStyle);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    GridRegion.COLUMN_HEADER);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    GridRegion.CORNER);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    GridRegion.ROW_HEADER);
        }

        
        protected void configureSelectionAnchorStyle(IConfigRegistry configRegistry) {
            // Selection anchor style for normal display mode
            Style cellStyle = new Style();
            cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

            switch (config.alignment.horizontal) {
                case SWT.LEFT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                    break;
                case SWT.RIGHT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                    break;
                case SWT.CENTER:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                    break;
            }
            
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.NORMAL, 
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

            // Selection anchor style for select display mode
            cellStyle = new Style();
            cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

            switch (config.alignment.horizontal) {
                case SWT.LEFT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                    break;
                case SWT.RIGHT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                    break;
                case SWT.CENTER:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                    break;
            }
            
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT, 
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        }

        protected void configureSelectionStyle(IConfigRegistry configRegistry) {
            Style cellStyle = new Style();
            cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
            cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
            cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

            switch (config.alignment.horizontal) {
                case SWT.LEFT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                    break;
                case SWT.RIGHT:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                    break;
                case SWT.CENTER:
                    cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                    break;
            }
            
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, 
                    cellStyle, 
                    DisplayMode.SELECT);
        }
    }

    /**
     * Checkstyle
     * @param style
     * @return
     */
    private static int checkStyle(int style) {
        return style & (SWT.BORDER | SWT.NONE);
    }

    /** The parent */
    private final Composite             root;
    /** The underlying nattable instance */
    private NatTable                    table              = null;
    /** The underlying data provider */
    private IDataProvider               provider           = null;
    /** The underlying data layer */
    private DataLayer                   dataLayer          = null;
    /** The underlying corner layer */
    private CornerLayer                 cornerLayer        = null;
    /** The layout data */
    private Object                      layoutData         = null;
    /** The config */
    private ComponentTableConfiguration config             = null;
    /** State */
    private Integer                     selectedRow        = null;
    /** State */
    private Integer                     selectedColumn     = null;

    /** Listeners */
    private List<SelectionListener>     selectionListeners = new ArrayList<SelectionListener>();
    /** Listeners */
    private List<MouseListener>         mouseListeners     = new ArrayList<MouseListener>();

    /**
     * Creates a new instance
     * @param parent
     * @param style
     */
    public ComponentTable(Composite parent, int style) {
        this(parent, style, null);
    }
    
    /**
     * Creates a new instance
     * @param parent
     * @param style
     * @param config
     */
    public ComponentTable(Composite parent, int style, ComponentTableConfiguration config) {
        this.root = new Composite(parent, checkStyle(style));
        this.root.setLayout(new FillLayout());
        if (config != null) {
            this.config = config;
        } else {
            this.config = new ComponentTableConfiguration();
        }
    }

    /**
     * Adds a listener
     * @param arg0
     */
    public void addMouseListener(MouseListener arg0) {
        mouseListeners.add(arg0);
    }

    /**
     * Adds a selection listener
     * @param e
     * @return
     */
    public boolean addSelectionListener(SelectionListener e) {
        return selectionListeners.add(e);
    }

    /**
     * Returns the backing widget
     * @return
     */
    public Control getControl() {
        return this.root;
    }

    /** 
     * Returns the selected column, or null
     * @return
     */
    public Integer getSelectedColumn() {
        return selectedColumn;
    }

    /**
     * Returns the selected row, or null
     * @return
     */
    public Integer getSelectedRow() {
        return selectedRow;
    }

    /**
     * Removes a listener
     * @param arg0
     */
    public void removeMouseListener(MouseListener arg0) {
        mouseListeners.remove(arg0);
    }
    
    /**
     * Removes a selection listener
     * @param index
     * @return
     */
    public SelectionListener removeSelectionListener(int index) {
        return selectionListeners.remove(index);
    }
    
    /**
     * Layouts the table, after a column has been removed
     * @param column
     */
    public void doLayoutOnColumnRemoval(int column){
        if (column<0 || column>=provider.getColumnCount()+1) {
            return;
        }
        
        if (dataLayer == null) {
            return;
        }
        
        table.setRedraw(false);

        int total = cornerLayer != null ? cornerLayer.getColumnWidthByPosition(0) : 0;
        int columns = provider.getColumnCount();
        for (int i = 0; i < columns; i++) {
            total += dataLayer.getColumnWidthByPosition(i);
        }

        // Adjust
        int width = table.getSize().x;
        if (total < width) {
            dataLayer.setColumnWidthByPosition(columns - 1, width - total, true);
        }

        table.setRedraw(true);
        table.redraw();
    }
    
    /**
     * Updates the underlying table
     * @param data
     */
    public void setData(IDataProvider dataProvider) {
        this.setData(dataProvider, null, null);
    }
    
    /**
     * Updates the underlying data
     * @param dataProvider
     * @param rows May be null
     * @param columns May be null
     */
    public void setData(IDataProvider dataProvider, String[] rows, String[] columns) {

        // Disable redrawing
        this.root.setRedraw(false);
        
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
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(root, columnHeaderDataProvider, bodyLayer);
        RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(root, rowHeaderDataProvider, bodyLayer);
        final CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);
        GridLayer gridLayer = new GridLayer(bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
        
        // Create table
        this.table = new NatTable(root, gridLayer, false);
        this.table.addConfiguration(new TableStyleConfiguration());
        this.table.configure();
        this.provider = dataProvider;
        this.dataLayer = bodyLayer.getDataLayer();
        this.cornerLayer = cornerLayer;
        this.addSelectionListener(bodyLayer.getSelectionLayer());
        this.addMouseListener(table);

        // Add column width handler
        table.addControlListener(new ControlAdapter(){
            public void controlResized(ControlEvent arg0) {
                config.header.update(table.getSize().x);
            } 
        });
        table.addMouseListener(new MouseAdapter(){
            public void mouseUp(MouseEvent arg0) {
                config.header.update(table.getSize().x);
            }
        });
        
        // Set layout
        if (this.layoutData != null) {
            table.setLayoutData(layoutData);
        }
        
        // Redraw
        this.root.setRedraw(true);
        this.root.layout(true);
        
        // Layout
        this.config.header.init(table, dataLayer, cornerLayer, provider, table.getSize().x);
        
        // Reset state
        this.selectedRow = null;
        this.selectedColumn = null;
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
     * @param data
     * @param rows May be null
     * @param columns May be null
     */
    public void setData(String[][] data, String[] rows, String[] columns) {
        setData(getDataProvider(data), rows, columns);
    }

    /**
     * Empties the table
     */
    public void setEmpty() {
        if (this.table == null || this.table.isDisposed()) return;
        this.root.setRedraw(false);
        this.provider = null;
        this.dataLayer = null;
        this.cornerLayer = null;
        this.table.dispose();
        this.root.setRedraw(true);
        this.root.layout(true);
        this.selectedRow = null;
        this.selectedColumn = null;
    }

    /**
     * Sets the layout data
     * @param data
     */
    public void setLayoutData(Object data){
        this.layoutData = data;
        if (table != null) table.setLayoutData(data);
    }
    
    /**
     * Updates the underlying table. Hides the row header.
     * @param dataProvider
     * @param columns
     */
    public void setTable(IDataProvider dataProvider, String[] columns) {
        setTable(dataProvider, getHeaderDataProvider(dataProvider, columns, false));
    }

    /**
     * Updates the underlying table. Hides the row header.
     * @param dataProvider
     * @param columns
     */
    public void setTable(IDataProvider dataProvider, IDataProvider columns) {

        // Disable redrawing
        this.root.setRedraw(false);
        
        // Dispose
        if (table != null && !table.isDisposed()) {
            table.dispose();
        }

        // Create layers
        BodyLayerStack bodyLayer = new BodyLayerStack(dataProvider);
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(root, columns, bodyLayer);
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayer, 0, 1);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        addUIBindings(compositeLayer);

        // Create table
        this.table = new NatTable(root, compositeLayer, false);
        this.table.addConfiguration(new TableStyleConfiguration());
        this.table.configure();
        this.provider = dataProvider;
        this.dataLayer = bodyLayer.getDataLayer();
        this.cornerLayer = null;
        this.addSelectionListener(bodyLayer.getSelectionLayer());
        this.addMouseListener(table);
        
        // Add column width handler
        table.addControlListener(new ControlAdapter(){
            public void controlResized(ControlEvent arg0) {
                config.header.update(table.getSize().x);
            } 
        });
        table.addMouseListener(new MouseAdapter(){
            public void mouseUp(MouseEvent arg0) {
                config.header.update(table.getSize().x);
            }
        });
        
        // Set layout
        if (this.layoutData != null) {
            table.setLayoutData(layoutData);
        }
        
        // Redraw
        this.root.setRedraw(true);
        this.root.layout(true);

        // Layout
        this.config.header.init(table, dataLayer, cornerLayer, provider, table.getSize().x);
        
        // Reset state
        this.selectedRow = null;
        this.selectedColumn = null;
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
     * To display coordinates
     * @param x
     * @param y
     * @return
     */
    public Point toDisplay(int x, int y) {
        return table.toDisplay(x, y);
    }

    /**
     * Action
     * @param arg0
     */
    private void actionCellSelected(CellSelectionEvent arg0) {

        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int column = arg0.getColumnPosition();
        int row = arg0.getRowPosition();
        if (column>=0 && row>=0){
            this.selectedColumn = column;
            this.selectedRow = row;
            fireSelectionEvent();
        }
    }

    /**
     * Action
     * @param arg0
     */
    private void actionColumnSelected(ColumnSelectionEvent arg0) {
        
        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int column = arg0.getColumnPositionRanges().iterator().next().start;
        if (column>=0) {
            this.selectedColumn = column;
            fireSelectionEvent();
        }
    }

    /**
     * Action
     * @param arg0
     */
    private void actionRowSelected(RowSelectionEvent arg0) {
        
        // Reset
        this.selectedColumn = null;
        this.selectedRow = null;
        
        // Set
        int row = arg0.getRowPositionRanges().iterator().next().start;
        if (row>=0) {
            this.selectedRow = row;
            fireSelectionEvent();
        }
    }

    /**
     * Add listener
     * @param table
     */
    private void addMouseListener(NatTable table) {
        table.addMouseListener(new MouseListener(){
            public void mouseDoubleClick(MouseEvent arg0) {
                for (MouseListener listener : mouseListeners) {
                    listener.mouseDoubleClick(arg0);
                }
            }
            public void mouseDown(MouseEvent arg0) {
                for (MouseListener listener : mouseListeners) {
                    listener.mouseDown(arg0);
                }
            }
            public void mouseUp(MouseEvent arg0) {
                for (MouseListener listener : mouseListeners) {
                    listener.mouseUp(arg0);
                }
            }
        });
    }

    /**
     * Adds a selection listener
     * @param layer
     */
    private void addSelectionListener(SelectionLayer layer) {
        layer.addLayerListener(new ILayerListener(){
            @Override
            public void handleLayerEvent(ILayerEvent arg0) {
                if (arg0 instanceof CellSelectionEvent) {
                    actionCellSelected((CellSelectionEvent)arg0);
                } else if (arg0 instanceof ColumnSelectionEvent) {
                    actionColumnSelected((ColumnSelectionEvent)arg0);
                } else if (arg0 instanceof RowSelectionEvent) {
                    actionRowSelected((RowSelectionEvent)arg0);
                }
            }
        });

    }

    /**
     * Adds some UI bindings
     * @param layer
     */
    private void addUIBindings(CompositeLayer layer) {

        // Make corner resizable
        layer.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON), new SelectCellAction(){
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        if (config == null || config.selection.cell) super.run(natTable, event);
                    }
                });
                uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON), new ViewportSelectColumnAction(true, true){
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        if (config == null || config.selection.column) super.run(natTable, event);
                    }
                });
                uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,GridRegion.ROW_HEADER, MouseEventMatcher.RIGHT_BUTTON), new ViewportSelectRowAction(true, true){
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        if (config == null || config.selection.row) super.run(natTable, event);
                    }
                });
            }
        });
    }

    /**
     * Fires a new event
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

    /**
     * Redraws the table and resets the layout
     */
    public void reset() {
        if (table==null || table.isDisposed()) return;
        this.table.refresh();
        this.selectedRow = null;
        this.selectedColumn = null;
        this.config.header.init(table, dataLayer, cornerLayer, provider, table.getSize().x);
    }
    /**
     * Redraws the table
     */
    public void refresh() {
        if (table==null || table.isDisposed()) return;
        this.table.refresh();
        if (this.selectedColumn == null ||
            this.selectedColumn >= provider.getColumnCount() ||
            this.provider.getColumnCount() == 0) {
            this.selectedColumn = null;
        }
        if (this.selectedRow == null ||
            this.selectedRow >= provider.getRowCount() ||
            this.provider.getRowCount() == 0) {
            this.selectedRow = null;
        }
        config.header.update(table.getSize().x);
    }
}