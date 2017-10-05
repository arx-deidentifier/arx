/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A view on a <code>Data</code> object.
 *
 * @author Fabian Prasser
 * @author Johanna Eicher
 */
public abstract class ViewData implements IView {

    /** Image */
    private final Image                  IMAGE_ASCENDING;

    /** Image */
    private final Image                  IMAGE_DESCENDING;

    /** Widget */
    private final ToolItem               groupsButton;

    /** Widget */
    private final ToolItem               subsetButton;

    /** Widget */
    private final ToolItem               ascendingButton;

    /** Widget */
    private final ToolItem               descendingButton;

    /** Widget */
    private final ComponentTitledFolder  folder;

    /** Widget */
    protected final ComponentDataTable   table;

    /** Controller */
    protected final Controller           controller;

    /** Model */
    protected Model                      model;

    /** View */
    private final Map<Composite, String> helpids = new HashMap<Composite, String>();

    /**
     * 
     * Creates a new data view.
     *
     * @param parent
     * @param controller
     * @param title
     * @param helpid
     */
    public ViewData(final Composite parent,
                    final Controller controller,
                    final String helpid,
                    final String title) {

        // Register
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.INPUT, this);
        
        // Store
        this.controller = controller;
        
        // Load images
        IMAGE_ASCENDING         = controller.getResources().getManagedImage("sort_ascending.png"); //$NON-NLS-1$
        IMAGE_DESCENDING        = controller.getResources().getManagedImage("sort_descending.png");//$NON-NLS-1$

        // Create title bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(helpid, helpids); //$NON-NLS-1$
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
                controller.getResources().getManagedImage("sort_groups.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionDataShowGroups();
                    }
                });
        bar.add(Resources.getMessage("DataView.3"), //$NON-NLS-1$ 
                controller.getResources().getManagedImage("sort_subset.png"), //$NON-NLS-1$
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionDataToggleSubset();
                    }
                });
        
        // Build border
        folder = new ComponentTitledFolder(parent, controller, bar, null);
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
     * Add a scrollbar listener to this view.
     *
     * @param listener
     */
    public void addScrollBarListener(final Listener listener) {
        table.addScrollBarListener(listener);
    }
    
    /**
     * Adds a listener to the folder
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        this.folder.addSelectionListener(listener);
    }
    
    /**
     * Adds an additional item to the folder
     * @param title
     * @param helpid 
     * @return
     */
    public Composite createAdditionalItem(String title, String helpid) {
        Composite result = folder.createItem(title, null);
        helpids.put(result, helpid);
        return result;
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
        table.dispose();
    }
    
    /**
     * Returns the selection index of the folder
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    /**
     * Returns the NatTable viewport layer.
     *
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
     * Sets the selection
     * @param index
     */
    public void setSelectedItem(int index) {
        folder.setSelection(index);
    }

    /**
     * Sets the selection index of the folder
     * @param index
     */
    public void setSelectionIndex(int index) {
        folder.setSelection(index);
    }
    
    @Override
    public void update(final ModelEvent event) {

        // Enable/Disable sort button
        if (event.part == ModelPart.OUTPUT ||
            event.part == ModelPart.INPUT ||
            event.part == ModelPart.SELECTED_VIEW_CONFIG) {
            
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
        
        if (event.part == ModelPart.SELECTED_VIEW_CONFIG) {          
            subsetButton.setSelection(model.getViewConfig().isSubset());
        }
        
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
        	table.setSelectedAttribute((String)event.data);
        }
    }
    
    /**
     * Selects the given column.
     *
     * @param index
     */
    private void actionColumnSelected(int index){
    	DataHandle handle = getHandle();
        if (handle != null && index < handle.getNumColumns()){
            final String attr = handle.getAttributeName(index);
            model.setSelectedAttribute(attr);
            table.setSelectedAttribute(attr);
            controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, attr));
        }
    }

    /**
     * Cell selection event.
     *
     * @param arg1
     */
    protected void actionCellSelected(CellSelectionEvent arg1){
        if (model != null) {
            int column = arg1.getColumnPosition() - 1;
            if (column>=0) actionColumnSelected(column);
        }
    }
    
    /**
     * Column selection event.
     *
     * @param arg1
     */
    protected void actionColumnSelected(ColumnSelectionEvent arg1) {
        if (model != null) {
            int column = arg1.getColumnPositionRanges().iterator().next().start - 1;
            if (column>=0) actionColumnSelected(column);
        }
    }
    

    /**
     * Called when the sort button is pressed.
     */
    protected abstract void actionSort();

    /**
     * Enable sorting.
     */
    protected void enableSorting(){
        ascendingButton.setEnabled(true);
        descendingButton.setEnabled(true);
    }

    /**
     * Returns the data definition.
     *
     * @return
     */
    protected abstract DataDefinition getDefinition();

    /**
     * Returns the data definition.
     *
     * @return
     */
    protected abstract DataHandle getHandle();

    /**
     * Updates the header image in the table.
     *
     * @param index
     * @param type
     */
    protected void updateHeaderImage(final int index, final String attribute, DataDefinition def) {
        AttributeType type = def.getAttributeType(attribute);
        while (table.getHeaderImages().size() <= index) {
            table.getHeaderImages().add(null);
        }
        table.getHeaderImages().set(index, controller.getResources().getImage(type, def.isResponseVariable(attribute)));
    }
}
