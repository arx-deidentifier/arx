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

import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.def.IComponent;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableBodyLayerStack;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableColumnHeaderConfiguration;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableContext;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableDecorator;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableGridLayer;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableGridLayerStack;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableHandleDataProvider;
import org.deidentifier.arx.gui.view.impl.common.datatable.DataTableRowHeaderConfiguration;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
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
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

/**
 * This component displays a data table. It provides ARX-specific methods for displaying
 * equivalence classes, research subsets and attribute types.
 * @author Fabian Prasser
 */
public class ComponentDataTable implements IComponent {

    /**  TODO */
    private NatTable                table;
    
    /**  TODO */
    private DataTableContext        context;
    
    /**  TODO */
    private DataTableBodyLayerStack bodyLayer;
    
    /**  TODO */
    private DataTableGridLayer      gridLayer;
    
    /**  TODO */
    private Font                    font;
    
    /**  TODO */
    private Control                 parent;

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param parent
     */
    public ComponentDataTable(final Controller controller, final Composite parent) {
        
        this.context = new DataTableContext(controller);
        this.context.setFont(parent.getFont());
        this.table = createControl(parent); 
        this.table.setVisible(false);
        this.context.setTable(this.table);
        this.font = parent.getFont();
        this.parent = parent;
    }

    /**
     * Adds a scroll bar listener.
     *
     * @param listener
     */
    public void addScrollBarListener(final Listener listener) {
        this.table.getVerticalBar().addListener(SWT.Selection, listener);
        this.table.getHorizontalBar().addListener(SWT.Selection, listener);
    }

    /**
     * Adds a select layer listener.
     *
     * @param listener
     */
    public void addSelectionLayerListener(ILayerListener listener){
        this.context.getListeners().add(listener);
    }

    /**
     * Creates the control contents.
     *
     * @param parent
     * @return
     */
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
     * Creates the nattable.
     *
     * @param parent
     * @return
     */
    private NatTable createTable(final Composite parent) {
        final IDataProvider provider = new DataTableHandleDataProvider(context);
        gridLayer = new DataTableGridLayerStack(provider, table, context, parent);
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

    /**
     * Creates the table styling.
     *
     * @param natTable
     */
    private void createTableStyling(final NatTable natTable) {

        final DefaultNatTableStyleConfiguration natTableConfiguration = new DefaultNatTableStyleConfiguration();
        natTableConfiguration.bgColor = GUIHelper.getColor(249, 172, 7);
        natTableConfiguration.fgColor = GUIHelper.getColor(0, 0, 0);
        natTableConfiguration.hAlign = HorizontalAlignmentEnum.LEFT;
        natTableConfiguration.vAlign = VerticalAlignmentEnum.TOP;
        natTableConfiguration.font = this.font;

        // A custom painter can be plugged in to paint the cells differently
        natTableConfiguration.cellPainter = new PaddingDecorator(new TextPainter(), 1);

        // Setup even odd row colors - row colors override the NatTable default colors
        final DefaultRowStyleConfiguration rowStyleConfiguration = new DefaultRowStyleConfiguration();
        rowStyleConfiguration.oddRowBgColor = GUIHelper.getColor(254, 251, 243);
        rowStyleConfiguration.evenRowBgColor = GUIHelper.COLOR_WHITE;

        // Setup selection styling
        final DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = this.font;
        selectionStyle.selectionBgColor = GUIHelper.getColor(220, 220, 220);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
        selectionStyle.anchorBgColor = GUIHelper.getColor(220, 220, 220);
        selectionStyle.anchorFgColor = GUIHelper.getColor(0, 0, 0);
        selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(156, 209, 103);
        selectionStyle.selectedHeaderFont = this.font;

        // Add all style configurations to NatTable
        natTable.addConfiguration(natTableConfiguration);
        natTable.addConfiguration(rowStyleConfiguration);
        natTable.addConfiguration(selectionStyle);

        // Column/Row header style and custom painters
        natTable.addConfiguration(new DataTableRowHeaderConfiguration(context));
        natTable.addConfiguration(new DataTableColumnHeaderConfiguration(context));

        // Make corner resizable
        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                // Mouse move - Show resize cursor
                uiBindingRegistry.registerFirstMouseMoveBinding(new ColumnResizeEventMatcher(SWT.NONE,
                                                                                             GridRegion.CORNER,
                                                                                             0),
                                                                new ColumnResizeCursorAction());
                uiBindingRegistry.registerMouseMoveBinding(new MouseEventMatcher(), new ClearCursorAction());

                // Column resize
                uiBindingRegistry.registerFirstMouseDragMode(new ColumnResizeEventMatcher(SWT.NONE,
                                                                                          GridRegion.CORNER,
                                                                                          1),
                                                             new ColumnResizeDragMode());

                uiBindingRegistry.registerDoubleClickBinding(new ColumnResizeEventMatcher(SWT.NONE,
                                                                                          GridRegion.CORNER,
                                                                                          1),
                                                             new AutoResizeColumnAction());
                uiBindingRegistry.registerSingleClickBinding(new ColumnResizeEventMatcher(SWT.NONE,
                                                                                          GridRegion.CORNER,
                                                                                          1), new NoOpMouseAction());
            }
        });
        
    }

    /**
     * Disposes the control.
     */
    public void dispose() {
        if (!table.isDisposed()) table.dispose();
    }

    /**
     * Returns the displayed data.
     *
     * @return
     */
    public DataHandle getData() {
        return this.context.getHandle();
    }

    /**
     * Returns the list of header images.
     *
     * @return
     */
    public List<Image> getHeaderImages() {
        return this.context.getImages();
    }
    
    /**
     * Returns the viewport layer.
     *
     * @return
     */
    public ViewportLayer getViewportLayer() {
        return this.gridLayer.getBodyLayer().getViewportLayer();
    }

    /**
     * Redraws the component.
     */
    public void redraw() {
        this.table.redraw();
    }
  
    /**
     * Resets the component.
     */
    public void reset() {
        this.table.setRedraw(false);
        this.context.getImages().clear();
        this.context.reset();
        this.gridLayer = new DataTableGridLayerStack(new DataTableHandleDataProvider(context), table, context, parent);
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

    /**
     * Sets the selected attribute.
     *
     * @param attribute
     */
    public void setSelectedAttribute(String attribute) {
        int index = -1;
        if (context.getHandle()!=null) {
            index = context.getHandle().getColumnIndexOf(attribute);
        }
        this.context.setSelectedIndex(index);
        this.redraw();
    }

    /**
     * Sets the displayed data.
     *
     * @param handle
     */
    public void setData(final DataHandle handle) {
        this.table.setRedraw(false);
        this.context.setHandle(handle);
        this.gridLayer = new DataTableGridLayerStack(new DataTableHandleDataProvider(context), table, context, parent);
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
    
    /**
     * Enables/disables the component.
     *
     * @param val
     */
    public void setEnabled(final boolean val) {
        if (table != null) {
            table.setEnabled(val);
        }
    }
    
    /**
     * Sets information about equivalence classes.
     *
     * @param groups
     */
    public void setGroups(int[] groups) {
        this.context.setGroups(groups);
    }

    /**
     * Sets layout data.
     *
     * @param data
     */
    public void setLayoutData(final Object data) {
        table.setLayoutData(data);
    }

    /**
     * Sets information about the research subset.
     *
     * @param researchSubset
     */
    public void setResearchSubset(RowSet researchSubset) {
        this.context.setRows(researchSubset);
    }
}
