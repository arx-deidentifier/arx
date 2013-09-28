/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.def.IDataTable;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class DataTable implements IDataTable {
    /**
     * Paints an image. If no image is provided, it will attempt to look up an
     * image from the cell style.
     */
    public class ImagePainter extends BackgroundPainter {

        @Override
        public void paintCell(final ILayerCell cell,
                              final GC gc,
                              final Rectangle bounds,
                              final IConfigRegistry configRegistry) {
            if ((headerImages != null) && (headerImages.size() > 0)) {
                final int index = cell.getColumnIndex() - (rows != null ? 1 : 0);
                if (index >= 0){
                    final Image image = headerImages.get(index);
                    if (image != null) {
                        gc.drawImage(image, bounds.x + 3, bounds.y - 8);
                    }
                }
            }
        }

    }

    public class StyledColumnHeaderConfiguration extends
            DefaultColumnHeaderStyleConfiguration {

        public StyledColumnHeaderConfiguration() {
            font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
        }

        @Override
        public void configureRegistry(final IConfigRegistry configRegistry) {
            super.configureRegistry(configRegistry);
            addNormalModeStyling(configRegistry);
            addSelectedModeStyling(configRegistry);
        }

        private void addNormalModeStyling(final IConfigRegistry configRegistry) {

            final TextPainter txtPainter = new TextPainter(false, false);
            final ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter,
                                                                           IMAGE_COL_BACK,
                                                                           GUIHelper.getColor(192,
                                                                                              192,
                                                                                              192));
            final SortableHeaderTextPainter headerBasePainter = new SortableHeaderTextPainter(bgImagePainter,
                                                                                              false,
                                                                                              true);

            final CellPainterDecorator headerPainter = new CellPainterDecorator(headerBasePainter,
                                                                                CellEdgeEnum.LEFT,
                                                                                new ImagePainter());

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                                   headerPainter,
                                                   DisplayMode.NORMAL,
                                                   GridRegion.COLUMN_HEADER);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                                   headerBasePainter,
                                                   DisplayMode.NORMAL,
                                                   GridRegion.CORNER);
        }

        private void
                addSelectedModeStyling(final IConfigRegistry configRegistry) {

            final TextPainter txtPainter = new TextPainter(false, false);
            final ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter,
                                                                                IMAGE_COL_SELECT,
                                                                                GUIHelper.getColor(192,
                                                                                                   192,
                                                                                                   192));

            final CellPainterDecorator selectedHeaderPainter = new CellPainterDecorator(selectedCellPainter,
                                                                                        CellEdgeEnum.LEFT,
                                                                                        new ImagePainter());

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                                   selectedHeaderPainter,
                                                   DisplayMode.SELECT,
                                                   GridRegion.COLUMN_HEADER);
        }
    }

    public class StyledRowHeaderConfiguration extends
            DefaultRowHeaderStyleConfiguration {

        public StyledRowHeaderConfiguration() {
            font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

            final TextPainter txtPainter = new TextPainter(false, false);
            final ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter,
                                                                           IMAGE_ROW_BACK,
                                                                           null);
            cellPainter = bgImagePainter;
        }

        @Override
        public void configureRegistry(final IConfigRegistry configRegistry) {
            super.configureRegistry(configRegistry);
            addSelectedModeStyling(configRegistry);
        }

        private void
                addSelectedModeStyling(final IConfigRegistry configRegistry) {

            final TextPainter txtPainter = new TextPainter(false, false);
            final ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter,
                                                                                IMAGE_ROW_SELECT,
                                                                                GUIHelper.getColor(192,
                                                                                                   192,
                                                                                                   192));

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                                   selectedCellPainter,
                                                   DisplayMode.SELECT,
                                                   GridRegion.ROW_HEADER);
        }
    }

    private class ArrayDataProvider implements IDataProvider {

        private final String[][] data;
        private final RowSet     rows;

        public ArrayDataProvider(final String[][] data, final RowSet rows) {
            this.data = data;
            this.rows = rows;
        }

        @Override
        public int getColumnCount() {
            if (data == null) { return 0; }
            return data[0].length + (rows != null ? 1 : 0);
        }

        @Override
        public Object getDataValue(final int arg0, final int arg1) {
            if (data == null) { return null; }
            if (rows == null) {
                return data[arg1][arg0];
            } else if (arg0 == 0){
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
        public void setDataValue(final int arg0,
                                 final int arg1,
                                 final Object arg2) {
            return;
        }
    }

    private class HandleDataProvider implements IDataProvider {

        private final DataHandle data;
        private final RowSet     rows;

        public HandleDataProvider(final DataHandle data, final RowSet rows) {
            this.data = data;
            this.rows = rows;
        }

        @Override
        public int getColumnCount() {
            if (data == null) { return 0; }
            return data.getNumColumns() + (rows != null ? 1 : 0);
        }

        @Override
        public Object getDataValue(final int arg0, final int arg1) {
            if (data == null) { return null; }
            if (rows == null) {
                return  data.getValue(arg1, arg0);
            } else if (arg0 == 0){
                return rows.contains(arg1);
            } else {
                return  data.getValue(arg1, arg0 - 1);
            }
        }

        @Override
        public int getRowCount() {
            if (data == null) { return 0; }
            return data.getNumRows();
        }

        @Override
        public void setDataValue(final int arg0,
                                 final int arg1,
                                 final Object arg2) {
            return;
        }
    }

    private class DataConfigLabelAccumulator implements IConfigLabelAccumulator {
        
        @Override
        public void accumulateConfigLabels(LabelStack configLabels,
                                           int columnPosition,
                                           int rowPosition) {
            
            if (table!=null && rowColors!=null){
                int row = table.getRowIndexByPosition(rowPosition+1);
                configLabels.addLabel("background"+rowColors[row]); //$NON-NLS-1$
                if (row<rowGroups.length-1 && rowGroups[row]!=rowGroups[row+1]){
                    configLabels.addLabel(DataTableDecorator.BOTTOM_LINE_BORDER_LABEL);
                }
            } 
            
            if (table!=null && rows != null){
                int column = table.getColumnIndexByPosition(columnPosition+1);
                if (column == 0) {
                    configLabels.addLabel("checkbox"); //$NON-NLS-1$
                }
            }
        }
    }

    private class TableGridLayerStack extends DataGridLayer {

        public TableGridLayerStack(final IDataProvider bodyDataProvider) {
            super(true);
            List<String> lcolumns = new ArrayList<String>();
            if (bodyDataProvider.getColumnCount() != 0) {
                if (rows != null){
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
            String[] columns = lcolumns.toArray(new String[]{});
            final IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(columns);
            final IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
            final IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider,
                                                                                   rowHeaderDataProvider);
            init(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider,
                 cornerDataProvider);
        }
    }

    class DataBodyLayerStack extends AbstractLayerTransform {

        private final SelectionLayer selectionLayer;
        private final ViewportLayer  viewportLayer;

        public DataBodyLayerStack(IUniqueIndexLayer underlyingLayer) {
            this.selectionLayer = new SelectionLayer(underlyingLayer);
            this.viewportLayer = new ViewportLayer(selectionLayer);
            this.setUnderlyingLayer(viewportLayer);
            this.setConfigLabelAccumulator(new DataConfigLabelAccumulator());
            this.registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
        }

        public SelectionLayer getSelectionLayer() {
            return selectionLayer;
        }

        public ViewportLayer getViewportLayer() {
            return viewportLayer;
        }

        @Override
        public void
                setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
            super.setClientAreaProvider(clientAreaProvider);
        }
    }

    class DataGridLayer extends GridLayer {

        protected IUniqueIndexLayer bodyDataLayer;
        protected IUniqueIndexLayer columnHeaderDataLayer;
        protected IUniqueIndexLayer rowHeaderDataLayer;
        protected IUniqueIndexLayer cornerDataLayer;

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider) {
            this(bodyDataProvider, columnHeaderDataProvider, true);
        }

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider,
                             boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
            init(bodyDataProvider, columnHeaderDataProvider);
        }

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider,
                             IDataProvider rowHeaderDataProvider) {
            this(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider,
                 true);
        }

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider,
                             IDataProvider rowHeaderDataProvider,
                             boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
            init(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider);
        }

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider,
                             IDataProvider rowHeaderDataProvider,
                             IDataProvider cornerDataProvider) {
            this(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider,
                 cornerDataProvider,
                 true);
        }

        public DataGridLayer(IDataProvider bodyDataProvider,
                             IDataProvider columnHeaderDataProvider,
                             IDataProvider rowHeaderDataProvider,
                             IDataProvider cornerDataProvider,
                             boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
            init(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider,
                 cornerDataProvider);
        }

        public DataGridLayer(IUniqueIndexLayer bodyDataLayer,
                             IUniqueIndexLayer columnHeaderDataLayer,
                             IUniqueIndexLayer rowHeaderDataLayer,
                             IUniqueIndexLayer cornerDataLayer) {
            this(bodyDataLayer,
                 columnHeaderDataLayer,
                 rowHeaderDataLayer,
                 cornerDataLayer,
                 true);
        }

        public DataGridLayer(IUniqueIndexLayer bodyDataLayer,
                             IUniqueIndexLayer columnHeaderDataLayer,
                             IUniqueIndexLayer rowHeaderDataLayer,
                             IUniqueIndexLayer cornerDataLayer,
                             boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
            init(bodyDataLayer,
                 columnHeaderDataLayer,
                 rowHeaderDataLayer,
                 cornerDataLayer);
        }

        public <T> DataGridLayer(List<T> rowData,
                                 String[] propertyNames,
                                 Map<String, String> propertyToLabelMap) {
            this(rowData, propertyNames, propertyToLabelMap, true);
        }

        public <T> DataGridLayer(List<T> rowData,
                                 String[] propertyNames,
                                 Map<String, String> propertyToLabelMap,
                                 boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
            init(rowData, propertyNames, propertyToLabelMap);
        }

        protected DataGridLayer(boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
        }

        public IUniqueIndexLayer getBodyDataLayer() {
            return bodyDataLayer;
        }

        @Override
        public DataBodyLayerStack getBodyLayer() {
            return (DataBodyLayerStack) super.getBodyLayer();
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

        protected void init(IDataProvider bodyDataProvider,
                            IDataProvider columnHeaderDataProvider) {
            init(bodyDataProvider,
                 columnHeaderDataProvider,
                 new DefaultRowHeaderDataProvider(bodyDataProvider));
        }

        protected void init(IDataProvider bodyDataProvider,
                            IDataProvider columnHeaderDataProvider,
                            IDataProvider rowHeaderDataProvider) {
            init(bodyDataProvider,
                 columnHeaderDataProvider,
                 rowHeaderDataProvider,
                 new DefaultCornerDataProvider(columnHeaderDataProvider,
                                               rowHeaderDataProvider));
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
            DataBodyLayerStack bodyLayer = new DataBodyLayerStack(bodyDataLayer);

            SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

            // Column header
            this.columnHeaderDataLayer = columnHeaderDataLayer;
            ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer,
                                                             bodyLayer,
                                                             selectionLayer);

            // Row header
            this.rowHeaderDataLayer = rowHeaderDataLayer;
            ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                                                       bodyLayer,
                                                       selectionLayer);

            // Corner
            this.cornerDataLayer = cornerDataLayer;
            ILayer cornerLayer = new CornerLayer(cornerDataLayer,
                                                 rowHeaderLayer,
                                                 columnHeaderLayer);
            
            // Attach the listeners
            for (ILayerListener listener : selectionLayerListeners) {
                selectionLayer.addLayerListener(listener);
            }

            setBodyLayer(bodyLayer);
            setColumnHeaderLayer(columnHeaderLayer);
            setRowHeaderLayer(rowHeaderLayer);
            setCornerLayer(cornerLayer);
        }

        protected <T> void init(List<T> rowData,
                                String[] propertyNames,
                                Map<String, String> propertyToLabelMap) {
            init(new DefaultBodyDataProvider<T>(rowData, propertyNames),
                 new DefaultColumnHeaderDataProvider(propertyNames,
                                                     propertyToLabelMap));
        }

    }
    private final Image        IMAGE_COL_BACK;
    private final Image        IMAGE_ROW_BACK;
    private final Image        IMAGE_COL_SELECT;
    private final Image        IMAGE_ROW_SELECT;
    private List<Image>        headerImages = new ArrayList<Image>();
    
    private final Color[]      GRADIENT;
    private final NatTable     table;
    
    private int[]              rowColors = null;
    private int[]              rowGroups = null;

    private DataHandle         handle;
    private String[][]         data;
    private RowSet             rows;

    private DataBodyLayerStack bodyLayer;
    private DataGridLayer      gridLayer;
    private List<ILayerListener>  selectionLayerListeners = new ArrayList<ILayerListener>();
    
    public DataTable(final Controller controller, final Composite parent) {
        IMAGE_COL_BACK = controller.getResources().getImage("column_header_bg.png"); //$NON-NLS-1$
        IMAGE_ROW_BACK = controller.getResources().getImage("row_header_bg.png"); //$NON-NLS-1$
        IMAGE_COL_SELECT = controller.getResources().getImage("selected_column_header_bg.png"); //$NON-NLS-1$
        IMAGE_ROW_SELECT = controller.getResources().getImage("selected_row_header_bg.png"); //$NON-NLS-1$
        GRADIENT = createGradient(controller, controller.getResources().getGradientLength());
        table = createControl(parent);
        table.setVisible(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.gui.view.impl.common.IDataTable#addScrollBarListener
     * ( org.eclipse.swt.widgets.Listener)
     */
    @Override
    public void addScrollBarListener(final Listener listener) {
        table.getVerticalBar().addListener(SWT.Selection, listener);
        table.getHorizontalBar().addListener(SWT.Selection, listener);
    }

    public void dispose() {
        IMAGE_COL_BACK.dispose();
        IMAGE_ROW_BACK.dispose();
        IMAGE_COL_SELECT.dispose();
        IMAGE_ROW_SELECT.dispose();
    }

    public List<Image> getHeaderImages() {
        return headerImages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.gui.view.impl.common.IDataTable#getViewportLayer()
     */
    @Override
    public ViewportLayer getViewportLayer() {
        return gridLayer.getBodyLayer().getViewportLayer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.gui.view.impl.common.IDataTable#redraw()
     */
    @Override
    public void redraw() {
        table.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.gui.view.impl.common.IDataTable#reset()
     */
    @Override
    public void reset() {
        this.table.setRedraw(false);
        this.headerImages.clear();
        this.gridLayer = new TableGridLayerStack(new HandleDataProvider(null, null));
        this.handle = null;
        this.data = null;
        this.rows = null;
        this.table.setLayer(gridLayer);
        this.table.refresh();
        this.gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        this.table.getVerticalBar().setVisible(false);
        this.table.getHorizontalBar().setVisible(false);
        this.table.setRedraw(true);
        this.table.redraw();
        this.table.setVisible(false);
        this.table.getVerticalBar().setVisible(true);
        this.table.getHorizontalBar().setVisible(true);
        this.table.setVisible(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.gui.view.impl.common.IDataTable#setData(org.
     * deidentifier.ARX .DataHandle)
     */
    @Override
    public void setData(final DataHandle handle, final RowSet rows) {
        this.table.setRedraw(false);
        this.handle = handle;
        this.rows = rows;
        this.data = null;
        this.headerImages.clear();
        this.gridLayer = new TableGridLayerStack(new HandleDataProvider(handle, rows));
        this.headerImages = new ArrayList<Image>();
        this.table.setLayer(gridLayer);
        this.table.refresh();
        this.gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        this.table.getVerticalBar().setVisible(false);
        this.table.getHorizontalBar().setVisible(false);
        this.table.setRedraw(true);
        this.table.redraw();
        this.table.setVisible(true);
        this.table.getVerticalBar().setVisible(true);
        this.table.getHorizontalBar().setVisible(true);
        this.table.setVisible(true);
        this.rowColors = null;
        this.rowGroups = null;
    }

    /*
     * (non-Javadoc)
     * @see org.deidentifier.ARX.gui.view.def.IDataTable#setData(org.deidentifier.ARX.DataHandle, int[])
     */
    @Override
    public void setData(final DataHandle handle, final RowSet rows, int[] colors, int[] groups) {
        // TODO: Refactor to colors[groups[row]]
        this.table.setRedraw(false);
        this.handle = handle;
        this.rows = rows;
        this.data = null;
        this.headerImages.clear();
        this.gridLayer = new TableGridLayerStack(new HandleDataProvider(handle, rows));
        this.headerImages = new ArrayList<Image>();
        this.table.setLayer(gridLayer);
        this.table.refresh();
        this.gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        ((DataLayer)this.gridLayer.getBodyDataLayer()).setColumnWidthByPosition(0, 18);
        ((DataLayer)this.gridLayer.getBodyDataLayer()).setColumnPositionResizable(0, false);
          
        this.table.getVerticalBar().setVisible(false);
        this.table.getHorizontalBar().setVisible(false);
        this.table.setRedraw(true);
        this.table.redraw();
        this.table.setVisible(true);
        this.table.getVerticalBar().setVisible(true);
        this.table.getHorizontalBar().setVisible(true);
        this.table.setVisible(true);
        this.rowColors = colors;
        this.rowGroups = groups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.gui.view.impl.common.IDataTable#setData(org.
     * deidentifier.ARX .DataHandle)
     */
    @Override
    public void setData(final String[][] data, final RowSet rows) {
        this.table.setRedraw(false);
        this.handle = null;
        this.data = data;
        this.rows = rows;
        this.headerImages.clear();
        this.gridLayer = new TableGridLayerStack(new ArrayDataProvider(data, rows));
        this.table.setLayer(gridLayer);
        this.table.refresh();
        this.gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        this.table.getVerticalBar().setVisible(false);
        this.table.getHorizontalBar().setVisible(false);
        this.table.setRedraw(true);
        this.table.redraw();
        this.table.setVisible(true);
        this.table.getVerticalBar().setVisible(true);
        this.table.getHorizontalBar().setVisible(true);
        this.table.setVisible(true);
        this.rowColors = null;
        this.rowGroups = null;
    }

    /*
     * (non-Javadoc)
     * @see org.deidentifier.ARX.gui.view.def.IDataTable#setData(java.lang.String[][], int[])
     */
    @Override
    public void setData(final String[][] data, final RowSet rows, int[] colors, int[] groups) {
        // TODO: Refactor to colors[groups[row]]
        this.table.setRedraw(false);
        this.handle = null;
        this.data = data;
        this.rows = rows;
        this.headerImages.clear();
        this.gridLayer = new TableGridLayerStack(new ArrayDataProvider(data, rows));
        this.table.setLayer(gridLayer);
        this.table.refresh();
        this.gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        this.table.getVerticalBar().setVisible(false);
        this.table.getHorizontalBar().setVisible(false);
        this.table.setRedraw(true);
        this.table.redraw();
        this.table.setVisible(true);
        this.table.getVerticalBar().setVisible(true);
        this.table.getHorizontalBar().setVisible(true);
        this.table.setVisible(true);
        this.rowColors = colors;
        this.rowGroups = groups;
    }

    public void setEnabled(final boolean val) {
        if (table != null) {
            table.setEnabled(val);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.gui.view.impl.common.IDataTable#setLayoutData(
     * java.lang .Object)
     */
    @Override
    public void setLayoutData(final Object data) {
        table.setLayoutData(data);
    }

    private void createTableStyling(final NatTable natTable) {

        // NOTE: Getting the colors and fonts from the GUIHelper ensures that
        // they are disposed properly (required by SWT)
        final DefaultNatTableStyleConfiguration natTableConfiguration = new DefaultNatTableStyleConfiguration();
        natTableConfiguration.bgColor = GUIHelper.getColor(249, 172, 7);
        natTableConfiguration.fgColor = GUIHelper.getColor(0, 0, 0);
        natTableConfiguration.hAlign = HorizontalAlignmentEnum.LEFT;
        natTableConfiguration.vAlign = VerticalAlignmentEnum.TOP;
        natTableConfiguration.font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

        // A custom painter can be plugged in to paint the cells differently
        natTableConfiguration.cellPainter = new PaddingDecorator(new TextPainter(), 1);

        // Setup even odd row colors - row colors override the NatTable default colors
        final DefaultRowStyleConfiguration rowStyleConfiguration = new DefaultRowStyleConfiguration();
        rowStyleConfiguration.oddRowBgColor = GUIHelper.getColor(254, 251, 243);
        rowStyleConfiguration.evenRowBgColor = GUIHelper.COLOR_WHITE;

        // Setup selection styling
        final DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
        selectionStyle.selectionBgColor = GUIHelper.getColor(220, 220, 220);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
        selectionStyle.anchorBgColor = GUIHelper.getColor(220, 220, 220);
        selectionStyle.anchorFgColor = GUIHelper.getColor(0, 0, 0);
        selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(156, 209, 103);
        selectionStyle.selectedHeaderFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

        // Add all style configurations to NatTable
        natTable.addConfiguration(natTableConfiguration);
        natTable.addConfiguration(rowStyleConfiguration);
        natTable.addConfiguration(selectionStyle);

        // Column/Row header style and custom painters
        natTable.addConfiguration(new StyledRowHeaderConfiguration());
        natTable.addConfiguration(new StyledColumnHeaderConfiguration());
    }

    private NatTable createControl(final Composite parent) {
        final NatTable natTable = createTable(parent);
        createTableStyling(natTable);
        natTable.configure();
        final GridData tableLayoutData = new GridData();
        tableLayoutData.horizontalAlignment = SWT.FILL;
        tableLayoutData.verticalAlignment = SWT.FILL;
        tableLayoutData.grabExcessHorizontalSpace = true;
        tableLayoutData.grabExcessVerticalSpace = true;
        natTable.setLayoutData(tableLayoutData);
        return natTable;
    }
    
    /**
     * Creates a red to green gradient
     * @param controller
     * @param length
     * @return
     */
    private Color[] createGradient(final Controller controller, int length){
        Color[] colors = new Color[length];
        for (int i=0; i<length; i++){
            double hue = (double)i / (double)length * 0.4d;
            java.awt.Color c = java.awt.Color.getHSBColor((float)hue, 0.9f, 0.9f); 
            colors[i] = new Color(controller.getResources().getDisplay(), c.getRed(), c.getGreen(), c.getBlue());
        }
        return colors;
    }
    
    private NatTable createTable(final Composite parent) {
        final IDataProvider provider = new HandleDataProvider(null, null);
        gridLayer = new TableGridLayerStack(provider);
        final NatTable natTable = new NatTable(parent, gridLayer, false);
        final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

        // Add an AggregrateConfigLabelAccumulator - we can add other
        // accumulators to this as required
        final AggregrateConfigLabelAccumulator aggregrateConfigLabelAccumulator = new AggregrateConfigLabelAccumulator();
        bodyDataLayer.setConfigLabelAccumulator(aggregrateConfigLabelAccumulator);

        final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        final ColumnOverrideLabelAccumulator bodyLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);

        aggregrateConfigLabelAccumulator.add(bodyLabelAccumulator);
        aggregrateConfigLabelAccumulator.add(columnLabelAccumulator);

        // Register a command handler for the StyleEditorDialog
        final DisplayColumnStyleEditorCommandHandler styleChooserCommandHandler = new DisplayColumnStyleEditorCommandHandler(gridLayer.getBodyLayer()
                                                                                                                                      .getSelectionLayer(),
                                                                                                                             columnLabelAccumulator,
                                                                                                                             natTable.getConfigRegistry());

        bodyLayer = gridLayer.getBodyLayer();
        bodyLayer.registerCommandHandler(styleChooserCommandHandler);

        // Register the style editor as persistable
        bodyLayer.registerPersistable(styleChooserCommandHandler);
        bodyLayer.registerPersistable(columnLabelAccumulator);

        // Register default cell painter
        natTable.getConfigRegistry().registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
                                                             new DataTableDecorator( new TextPainter(false, true, 0, true),
                                                             new BorderStyle(2, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID)),
                                                                DisplayMode.NORMAL,
                                                                GridRegion.BODY);
        
        // Register gradient painters for groups
        for (int i=0; i<GRADIENT.length; i++){
            Style style = new Style();
            style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
                                                       GRADIENT[i]);
            natTable.getConfigRegistry()
                    .registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                                             style,
                                             DisplayMode.NORMAL,
                                             "background"+i);
          
        }
        
        // Register checkbox painter for subset
        natTable.getConfigRegistry().registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, 
                                                              new CheckBoxPainter(), 
                                                              DisplayMode.NORMAL, 
                                                              "checkbox");
        
        return natTable;
    }
    
    @Override
    public void addSelectionLayerListener(ILayerListener listener){
        selectionLayerListeners.add(listener);
    }

    /**
     * Sets the research subset
     * @param researchSubset
     */
    public void setResearchSubset(RowSet researchSubset) {
        this.rows = researchSubset;
    }
}
