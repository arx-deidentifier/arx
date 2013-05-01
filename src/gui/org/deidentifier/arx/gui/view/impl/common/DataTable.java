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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataHandle;
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
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
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
    private class ArrayDataProvider implements IDataProvider {

        private final String[][] data;

        public ArrayDataProvider(final String[][] data) {
            this.data = data;
        }

        @Override
        public int getColumnCount() {
            if (data == null) { return 0; }
            return data[0].length;
        }

        @Override
        public Object getDataValue(final int arg0, final int arg1) {
            if (data == null) { return null; }
            return data[arg1][arg0];
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

    class DataBodyLayerStack extends AbstractLayerTransform {

        private final SelectionLayer selectionLayer;
        private final ViewportLayer  viewportLayer;

        public DataBodyLayerStack(IUniqueIndexLayer underlyingLayer) {
            selectionLayer = new SelectionLayer(underlyingLayer);
            viewportLayer = new ViewportLayer(selectionLayer);
            setUnderlyingLayer(viewportLayer);
           this.setConfigLabelAccumulator(new RowColorConfigLabelAccumulator());
            registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
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

        protected DataGridLayer(boolean useDefaultConfiguration) {
            super(useDefaultConfiguration);
        }

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

    private class HandleDataProvider implements IDataProvider {

        private final DataHandle data;

        public HandleDataProvider(final DataHandle data) {
            this.data = data;
        }

        @Override
        public int getColumnCount() {
            if (data == null) { return 0; }
            return data.getNumColumns();
        }

        @Override
        public Object getDataValue(final int arg0, final int arg1) {
            if (data == null) { return null; }
            return data.getValue(arg1, arg0);
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
            //
            // Rectangle imageBounds = image.getBounds();
            // IStyle cellStyle = CellStyleUtil.getCellStyle(cell,
            // configRegistry);
            if ((headerImages != null) && (headerImages.size() > 0)) {
                final Image image = headerImages.get(cell.getColumnIndex());
                if (image != null) {
                    gc.drawImage(image, bounds.x + 3, bounds.y - 8);
                }
            }
        }

    }

    public class StyledColumnHeaderConfiguration extends
            DefaultColumnHeaderStyleConfiguration {

        public StyledColumnHeaderConfiguration() {
            font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
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

        @Override
        public void configureRegistry(final IConfigRegistry configRegistry) {
            super.configureRegistry(configRegistry);
            addNormalModeStyling(configRegistry);
            addSelectedModeStyling(configRegistry);
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

        @Override
        public void configureRegistry(final IConfigRegistry configRegistry) {
            super.configureRegistry(configRegistry);
            addSelectedModeStyling(configRegistry);
        }
    }

    private class TableGridLayerStack extends DataGridLayer {

        public TableGridLayerStack(final IDataProvider bodyDataProvider) {
            super(true);
            String[] columns = {};
            if (bodyDataProvider.getColumnCount() != 0) {
                if (handle != null) {
                    columns = new String[handle.getNumColumns()];
                    for (int i = 0; i < columns.length; i++) {
                        columns[i] = handle.getAttributeName(i);
                    }
                } else if (data != null) {
                    columns = data[0];
                }
            }
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

    private final Image        IMAGE_COL_BACK;
    private final Image        IMAGE_ROW_BACK;

    private final Image        IMAGE_COL_SELECT;

    private final Image        IMAGE_ROW_SELECT;
    private final Color[]      GRADIENT;
    private final NatTable     table;
    private int[]            rowColors = null;
    private int[]            rowGroups = null;

    private DataHandle         handle;

    private String[][]         data;

    private DataBodyLayerStack bodyLayer;

    private DataGridLayer      gridLayer;

    private List<Image>        headerImages = new ArrayList<Image>();

    public DataTable(final Controller controller, final Composite parent) {
        IMAGE_COL_BACK = controller.getResources()
                                   .getImage("column_header_bg.png"); //$NON-NLS-1$
        IMAGE_ROW_BACK = controller.getResources()
                                   .getImage("row_header_bg.png"); //$NON-NLS-1$
        IMAGE_COL_SELECT = controller.getResources()
                                     .getImage("selected_column_header_bg.png"); //$NON-NLS-1$
        IMAGE_ROW_SELECT = controller.getResources()
                                     .getImage("selected_row_header_bg.png"); //$NON-NLS-1$
        GRADIENT = createGradient(controller, controller.getResources().getGradientLength());
        table = createControl(parent);
        table.setVisible(false);
    }

    /**
     * Register an attribute to be applied to all cells with the highlight
     * label. A similar approach can be used to bind styling to an arbitrary
     * group of cells
     */
    private void addCellStyling(final IConfigRegistry configRegistry) {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
                                GUIHelper.COLOR_BLUE);
        style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                                HorizontalAlignmentEnum.RIGHT);
        style.setAttributeValue(CellStyleAttributes.FONT,
                                GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL))); //$NON-NLS-1$

        // configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
        // // attribute to apply
        // style, // value of the attribute
        // DisplayMode.NORMAL, // apply during normal rendering i.e not during
        // selection or edit
        // COLUMN_LABEL_1); // apply the above for all cells with this label
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.gui.view.impl.common.IDataTable#addSelectionListener
     * ( org.eclipse.swt.widgets.Listener)
     */
    @Override
    public void addSelectionListener(final Listener listener) {
        table.getVerticalBar().addListener(SWT.Selection, listener);
        table.getHorizontalBar().addListener(SWT.Selection, listener);
    }

    private void addTableStyling(final NatTable natTable) {
        // Setup NatTable default styling

        // NOTE: Getting the colors and fonts from the GUIHelper ensures that
        // they are disposed properly (required by SWT)
        final DefaultNatTableStyleConfiguration natTableConfiguration = new DefaultNatTableStyleConfiguration();
        natTableConfiguration.bgColor = GUIHelper.getColor(249, 172, 7);
        natTableConfiguration.fgColor = GUIHelper.getColor(0, 0, 0);
        natTableConfiguration.hAlign = HorizontalAlignmentEnum.LEFT;
        natTableConfiguration.vAlign = VerticalAlignmentEnum.TOP;
        natTableConfiguration.font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

        // A custom painter can be plugged in to paint the cells differently
        natTableConfiguration.cellPainter = new PaddingDecorator(new TextPainter(),
                                                                 1);

        // Setup even odd row colors - row colors override the NatTable default
        // colors
        final DefaultRowStyleConfiguration rowStyleConfiguration = new DefaultRowStyleConfiguration();
        rowStyleConfiguration.oddRowBgColor = GUIHelper.getColor(254, 251, 243);
        rowStyleConfiguration.evenRowBgColor = GUIHelper.COLOR_WHITE;

        // Setup selection styling
        final DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
        selectionStyle.selectionBgColor = GUIHelper.getColor(220, 220, 220);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1,
                                                           GUIHelper.COLOR_DARK_GRAY,
                                                           LineStyleEnum.SOLID);
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

        // Dont show a popup menu on the header
        // natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
    }

    public NatTable createControl(final Composite parent) {
        final NatTable natTable = setup(parent);

        addTableStyling(natTable);
        addCellStyling(natTable.getConfigRegistry());

        natTable.configure();

        final GridData tableLayoutData = new GridData();
        tableLayoutData.horizontalAlignment = SWT.FILL;
        tableLayoutData.verticalAlignment = SWT.FILL;
        tableLayoutData.grabExcessHorizontalSpace = true;
        tableLayoutData.grabExcessVerticalSpace = true;
        natTable.setLayoutData(tableLayoutData);
        return natTable;
    }

    public List<Image> getHeaderImages() {
        return headerImages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.gui.view.impl.common.IDataTable#getUiBindingRegistry
     * ()
     */
    @Override
    public UiBindingRegistry getUiBindingRegistry() {
        return table.getUiBindingRegistry();
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
        table.setRedraw(false);
        headerImages.clear();
        gridLayer = new TableGridLayerStack(new HandleDataProvider(null));
        handle = null;
        data = null;
        table.setLayer(gridLayer);
        table.refresh();
        gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        table.getVerticalBar().setVisible(false);
        table.getHorizontalBar().setVisible(false);
        table.setRedraw(true);
        table.redraw();
        table.setVisible(false);
        table.getVerticalBar().setVisible(true);
        table.getHorizontalBar().setVisible(true);
        table.setVisible(false);
    }

    /*
     * (non-Javadoc)
     * @see org.deidentifier.ARX.gui.view.def.IDataTable#setData(org.deidentifier.ARX.DataHandle, int[])
     */
    @Override
    public void setData(final DataHandle handle, int[] colors, int[] groups) {
        // TODO: Refactor to colors[groups[row]]
        table.setRedraw(false);
        this.handle = handle;
        data = null;
        headerImages.clear();
        gridLayer = new TableGridLayerStack(new HandleDataProvider(handle));
        headerImages = new ArrayList<Image>();
        table.setLayer(gridLayer);
        table.refresh();
        gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        table.getVerticalBar().setVisible(false);
        table.getHorizontalBar().setVisible(false);
        table.setRedraw(true);
        table.redraw();
        table.setVisible(true);
        table.getVerticalBar().setVisible(true);
        table.getHorizontalBar().setVisible(true);
        table.setVisible(true);
        this.rowColors = colors;
        this.rowGroups = groups;
    }

    /*
     * (non-Javadoc)
     * @see org.deidentifier.ARX.gui.view.def.IDataTable#setData(java.lang.String[][], int[])
     */
    @Override
    public void setData(final String[][] data, int[] colors, int[] groups) {
        // TODO: Refactor to colors[groups[row]]
        table.setRedraw(false);
        handle = null;
        this.data = data;
        headerImages.clear();
        gridLayer = new TableGridLayerStack(new ArrayDataProvider(data));
        table.setLayer(gridLayer);
        table.refresh();
        gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        table.getVerticalBar().setVisible(false);
        table.getHorizontalBar().setVisible(false);
        table.setRedraw(true);
        table.redraw();
        table.setVisible(true);
        table.getVerticalBar().setVisible(true);
        table.getHorizontalBar().setVisible(true);
        table.setVisible(true);
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
    public void setData(final DataHandle handle) {
        table.setRedraw(false);
        this.handle = handle;
        data = null;
        headerImages.clear();
        gridLayer = new TableGridLayerStack(new HandleDataProvider(handle));
        headerImages = new ArrayList<Image>();
        table.setLayer(gridLayer);
        table.refresh();
        gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        table.getVerticalBar().setVisible(false);
        table.getHorizontalBar().setVisible(false);
        table.setRedraw(true);
        table.redraw();
        table.setVisible(true);
        table.getVerticalBar().setVisible(true);
        table.getHorizontalBar().setVisible(true);
        table.setVisible(true);
        this.rowColors = null;
        this.rowGroups = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.gui.view.impl.common.IDataTable#setData(org.
     * deidentifier.ARX .DataHandle)
     */
    @Override
    public void setData(final String[][] data) {
        table.setRedraw(false);
        handle = null;
        this.data = data;
        headerImages.clear();
        gridLayer = new TableGridLayerStack(new ArrayDataProvider(data));
        table.setLayer(gridLayer);
        table.refresh();
        gridLayer.getBodyLayer().getViewportLayer().recalculateScrollBars();
        table.getVerticalBar().setVisible(false);
        table.getHorizontalBar().setVisible(false);
        table.setRedraw(true);
        table.redraw();
        table.setVisible(true);
        table.getVerticalBar().setVisible(true);
        table.getHorizontalBar().setVisible(true);
        table.setVisible(true);
        this.rowColors = null;
        this.rowGroups = null;
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

    private NatTable setup(final Composite parent) {
        final IDataProvider provider = new HandleDataProvider(null);
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
        // This will persist the style applied to the columns when
        // NatTable#saveState is invoked
        bodyLayer.registerPersistable(styleChooserCommandHandler);
        bodyLayer.registerPersistable(columnLabelAccumulator);

        natTable.getConfigRegistry().registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
                                                             new CustomLineBorderDecorator( new TextPainter(false, true, 0, true),
                                                             new BorderStyle(2, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID)),
                                                                DisplayMode.NORMAL,
                                                                GridRegion.BODY);
        
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
    
    private class RowColorConfigLabelAccumulator implements IConfigLabelAccumulator {
        
        @Override
        public void accumulateConfigLabels(LabelStack configLabels,
                                           int columnPosition,
                                           int rowPosition) {
            
            if (table!=null && rowColors!=null){
                int row = table.getRowIndexByPosition(rowPosition+1);
                configLabels.addLabel("background"+rowColors[row]); //$NON-NLS-1$
                if (row<rowGroups.length-1 && rowGroups[row]!=rowGroups[row+1]){
                    configLabels.addLabel(CustomLineBorderDecorator.BOTTOM_LINE_BORDER_LABEL);
                }
            } 
        }
    }
}
