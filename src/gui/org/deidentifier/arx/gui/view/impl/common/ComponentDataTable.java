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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.def.IComponent;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableArrayDataProvider;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableBodyLayerStack;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableGridLayer;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableContext;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableDecorator;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableHandleDataProvider;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableColumnHeaderConfiguration;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableRowHeaderConfiguration;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableGridLayerStack;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class ComponentDataTable implements IComponent {

    private NatTable                table;
    private DataTableContext        context;
    private DataTableBodyLayerStack bodyLayer;
    private DataTableGridLayer      gridLayer;

    public ComponentDataTable(final Controller controller, final Composite parent) {
        
        this.context = new DataTableContext(controller);
        this.table = createControl(parent); 
        this.table.setVisible(false);
    }

    public void addScrollBarListener(final Listener listener) {
        this.table.getVerticalBar().addListener(SWT.Selection, listener);
        this.table.getHorizontalBar().addListener(SWT.Selection, listener);
    }

    public void addSelectionLayerListener(ILayerListener listener){
        this.context.getListeners().add(listener);
    }

    public void dispose() {
        // Nothing to dispose
    }

    public List<Image> getHeaderImages() {
        return this.context.getImages();
    }

    public ViewportLayer getViewportLayer() {
        return this.gridLayer.getBodyLayer().getViewportLayer();
    }

    public void redraw() {
        this.table.redraw();
    }

    public void reset() {
        this.table.setRedraw(false);
        this.context.getImages().clear();
        this.gridLayer = new DataTableGridLayerStack(new DataTableHandleDataProvider(null, context), table, context);
        this.context.reset();
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

    public void setAttribute(String attribute) {
        int index = -1;
        if (context.getHandle()!=null) {
            index = context.getHandle().getColumnIndexOf(attribute);
        }
        this.context.setSelectedIndex(index);
    }
    
    public void setData(final String[][] data) {
        this.table.setRedraw(false);
        this.context.setHandle(null);
        this.context.setArray(data);
        this.gridLayer = new DataTableGridLayerStack(new DataTableArrayDataProvider(data, context), table, context);
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
    }

    public void setData(final DataHandle handle) {
        this.table.setRedraw(false);
        this.context.setHandle(handle);
        this.context.setArray(null);
        this.gridLayer = new DataTableGridLayerStack(new DataTableHandleDataProvider(handle, context), table, context);
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
    }
  
    public void setEnabled(final boolean val) {
        if (table != null) {
            table.setEnabled(val);
        }
    }

    public void setLayoutData(final Object data) {
        table.setLayoutData(data);
    }

    public void setResearchSubset(RowSet researchSubset) {
        this.context.setRows(researchSubset);
    }
    
    public void setGroups(int[] groups) {
        this.context.setGroups(groups);
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
    
    private NatTable createTable(final Composite parent) {
        final IDataProvider provider = new DataTableHandleDataProvider(null, context);
        gridLayer = new DataTableGridLayerStack(provider, table, context);
        final NatTable natTable = new NatTable(parent, gridLayer, false);
        final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

        // Add an AggregrateConfigLabelAccumulator 
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
        Color light = GUIHelper.getColor(240, 240, 240);
        Color dark = GUIHelper.getColor(180, 180, 180);
        Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, light);
        natTable.getConfigRegistry().registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                                             style,
                                             DisplayMode.NORMAL,
                                             "background0");
        
        style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, dark);
        natTable.getConfigRegistry().registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                                             style,
                                             DisplayMode.NORMAL,
                                             "background1");
        
        // Register checkbox painter for subset
        natTable.getConfigRegistry().registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, 
                                                              new CheckBoxPainter(), 
                                                              DisplayMode.NORMAL, 
                                                              "checkbox");
        
        return natTable;
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
        natTable.addConfiguration(new DataTableRowHeaderConfiguration(context));
        natTable.addConfiguration(new DataTableColumnHeaderConfiguration(context));
    }

    public DataHandle getData() {
        return this.context.getHandle();
    }
}
