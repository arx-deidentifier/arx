/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A view on a <code>Data</code> object
 * @author Fabian Prasser
 */
public abstract class ViewData implements IView {
    
    private final Image                IMAGE_ASCENDING;
    private final Image                IMAGE_DESCENDING;
    private final Image                IMAGE_INSENSITIVE;
    private final Image                IMAGE_SENSITIVE;
    private final Image                IMAGE_QUASI_IDENTIFYING;
    private final Image                IMAGE_IDENTIFYING;

    private final ToolItem             groupsButton;
    private final ToolItem             subsetButton;
    private final ToolItem             ascendingButton;
    private final ToolItem             descendingButton;

    protected final ComponentDataTable table;
    protected final Controller         controller;

    protected Model                    model;

    /** 
     * Creates a new data view
     * 
     * @param parent
     * @param controller
     * @param title
     */
    public ViewData(final Composite parent,
                    final Controller controller,
                    final String title) {

        // Register
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.INPUT, this);
        
        // Store
        this.controller = controller;
        
        // Load images
        IMAGE_INSENSITIVE       = controller.getResources().getImage("bullet_green.png");   //$NON-NLS-1$
        IMAGE_SENSITIVE         = controller.getResources().getImage("bullet_purple.png");  //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources().getImage("bullet_yellow.png");  //$NON-NLS-1$
        IMAGE_IDENTIFYING       = controller.getResources().getImage("bullet_red.png");     //$NON-NLS-1$
        IMAGE_ASCENDING         = controller.getResources().getImage("sort_ascending.png"); //$NON-NLS-1$
        IMAGE_DESCENDING        = controller.getResources().getImage("sort_descending.png");//$NON-NLS-1$

        // Create title bar
        ComponentTitleBar bar = new ComponentTitleBar("id-140");
        bar.add(Resources.getMessage("DataView.1"), //$NON-NLS-1$ 
                IMAGE_ASCENDING,
                new Runnable() {
                    @Override
                    public void run() {
                        model.getViewConfig().setSortOrder(true);
                        actionSort();
                    }
                });
        bar.add(Resources.getMessage("DataView.4"), //$NON-NLS-1$ 
                IMAGE_DESCENDING,
                new Runnable() {
                    @Override
                    public void run() {
                        model.getViewConfig().setSortOrder(false);
                        actionSort();
                    }
                });
        bar.add(Resources.getMessage("DataView.2"), //$NON-NLS-1$ 
                controller.getResources().getImage("sort_groups.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionDataShowGroups();
                    }
                });
        bar.add(Resources.getMessage("DataView.3"), //$NON-NLS-1$ 
                controller.getResources().getImage("sort_subset.png"),
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionDataToggleSubset();
                    }
                });
        
        // Build border
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillGridData());
        Composite c = folder.createItem(title, null);
        folder.setSelection(0);
        GridLayout l = new GridLayout();
        l.numColumns = 1;
        c.setLayout(l);
        
        // Build table
        table = new ComponentDataTable(controller, c);
        table.addSelectionLayerListener(new ILayerListener(){
            @Override
            public void handleLayerEvent(ILayerEvent arg0) {
                if (arg0 instanceof CellSelectionEvent) {
                    actionCellSelected((CellSelectionEvent)arg0);
                } else if (arg0 instanceof ColumnSelectionEvent) {
                    actionColumnSelected((ColumnSelectionEvent)arg0);
                }
            }
        });
        
        // Build buttons
        this.groupsButton = folder.getButtonItem(Resources.getMessage("DataView.2")); //$NON-NLS-1$
        this.groupsButton.setEnabled(false);
        this.subsetButton = folder.getButtonItem(Resources.getMessage("DataView.3")); //$NON-NLS-1$
        this.subsetButton.setEnabled(false);
        this.ascendingButton = folder.getButtonItem(Resources.getMessage("DataView.1")); //$NON-NLS-1$
        this.ascendingButton.setEnabled(false);
        this.descendingButton = folder.getButtonItem(Resources.getMessage("DataView.4")); //$NON-NLS-1$
        this.descendingButton.setEnabled(false);
    }
    
    /**
     * Cell selection event
     * @param arg1
     */
    protected void actionCellSelected(CellSelectionEvent arg1){
        if (model != null) {
            int column = arg1.getColumnPosition() - 1;
            if (column>=0) actionColumnSelected(column);
        }
    }
    
    /**
     * Column selection event
     * @param arg1
     */
    protected void actionColumnSelected(ColumnSelectionEvent arg1) {
        if (model != null) {
            int column = arg1.getColumnPositionRanges().iterator().next().start - 1;
            if (column>=0) actionColumnSelected(column);
        }
    }
    
    /**
     * Selects the given column
     * @param index
     */
    private void actionColumnSelected(int index){
    	DataHandle handle = getHandle();
        if (handle != null){
            final String attr = handle.getAttributeName(index);
            model.setSelectedAttribute(attr);
            table.setSelectedAttribute(attr);
            controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, attr));
        }
    }

    /**
     * Called when the sort button is pressed
     */
    protected abstract void actionSort();

    /**
     * Add a scrollbar listener to this view
     * @param listener
     */
    public void addScrollBarListener(final Listener listener) {
        table.addScrollBarListener(listener);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        IMAGE_INSENSITIVE.dispose();
        IMAGE_SENSITIVE.dispose();
        IMAGE_QUASI_IDENTIFYING.dispose();
        IMAGE_IDENTIFYING.dispose();
        table.dispose();
    }

    /**
     * Returns the data definition
     * @return
     */
    protected abstract DataDefinition getDefinition();
    
    /**
     * Returns the data definition
     * @return
     */
    protected abstract DataHandle getHandle();
    
    /**
     * Returns the NatTable viewport layer
     * @return
     */
    public ViewportLayer getViewportLayer() {
        return table.getViewportLayer();
    }

    @Override
    public void reset() {
        table.reset();
        groupsButton.setEnabled(false);
        subsetButton.setEnabled(false);
        ascendingButton.setEnabled(false);
        descendingButton.setEnabled(false);
    }
    
    /**
     * Enable sorting
     */
    protected void enableSorting(){
        ascendingButton.setEnabled(true);
        descendingButton.setEnabled(true);
    }
    

    @Override
    public void update(final ModelEvent event) {

        // Enable/Disable sort button
        if (event.part == ModelPart.OUTPUT ||
            event.part == ModelPart.INPUT ||
            event.part == ModelPart.VIEW_CONFIG) {
            
            if (model != null && model.getOutput() != null){
                groupsButton.setEnabled(true);
                subsetButton.setEnabled(true);
            } else {
                groupsButton.setEnabled(false);
                subsetButton.setEnabled(false);
            }
        }
        
        // Update model
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
        }
        
        if (event.part == ModelPart.VIEW_CONFIG) {          
            subsetButton.setSelection(model.getViewConfig().isSubset());
        }
        
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
        	table.setSelectedAttribute((String)event.data);
        }
    }

    /**
     * Updates the header image in the table
     * @param index
     * @param type
     */
    protected void updateHeaderImage(final int index, final AttributeType type) {
        while (table.getHeaderImages().size() <= index) {
            table.getHeaderImages().add(null);
        }
        if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            table.getHeaderImages().set(index, IMAGE_INSENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            table.getHeaderImages().set(index, IMAGE_IDENTIFYING);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            table.getHeaderImages().set(index, IMAGE_SENSITIVE);
        } else {
            table.getHeaderImages().set(index, IMAGE_QUASI_IDENTIFYING);
        }
    }
}
