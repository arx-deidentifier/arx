/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.attributes;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This view allows to select a subset of the quasi-identifiers
 * 
 * @author Fabian Prasser
 */
public class ViewAttributesQuasiIdentifiers implements IView {

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class AttributesPageLoader implements IPageLoader<PageResult<String>> {

        @Override
        public PageResult<String> loadPage(PageableController controller) {
            List<String> attributes = getAttributes();
            if (attributes == null) {
                return PageListHelper.createPage(new ArrayList<String>(), controller);
            } else {
                return PageListHelper.createPage(attributes, controller);
            }
        }
    }

    /** Controller */
    private final Controller    controller;
    
    /** View */
    private final Composite     root;
    /** View */
    private final PageableTable table;
    /** View */
    private final Label         label;
    /** View */
    private final Image         IMAGE_ENABLED;
    /** View */
    private final Image         IMAGE_DISABLED;
    /** Model */
    private Model               model;

    /** Model */
    private Set<String>         selection;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewAttributesQuasiIdentifiers(final Composite parent, final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_QUASI_IDENTIFIERS, this);

        // Load images
        this.IMAGE_ENABLED = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        this.IMAGE_DISABLED = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$
        this.controller = controller;

        // Create group
        this.root = parent;
        this.root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        // Create table
        this.table = SWTUtil.createPageableTableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, true, true);
        this.table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
        this.table.getViewer().setContentProvider(new ArrayContentProvider());
        this.table.setPageLoader(new AttributesPageLoader());

        // Table
        final Table tTable = table.getViewer().getTable();
        SWTUtil.createGenericTooltip(tTable);
        GridData gd = SWTUtil.createFillGridData();
        gd.heightHint = 100;
        tTable.setLayoutData(gd);
        tTable.setHeaderVisible(false);
        
        // Column
        TableViewerColumn column = new TableViewerColumn(table.getViewer(), SWT.NONE);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return (selection != null && selection.contains(element)) ? IMAGE_ENABLED : IMAGE_DISABLED;
            }
            @Override
            public String getText(Object element) {
                return (String)element;
            }
        });
        TableColumn tColumn = column.getColumn();
        tColumn.setToolTipText("Attribute");
        tColumn.setText("Attribute");
        tColumn.setWidth(30);
        tColumn.setResizable(false);
        
        // Selection event
        tTable.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                int index = tTable.getSelectionIndex();
                if (index < 0) {
                    return;
                } 
                String attribute = (String)tTable.getItem(index).getData();
                if (selection == null) {
                    selection = new HashSet<>();
                }
                if (selection.contains(attribute)) {
                    selection.remove(attribute);
                } else {
                    selection.add(attribute);
                }
                table.refreshPage();
                fireEvent();
            }   
        });
        
        // Create button
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(SWTUtil.createGridData());
        button.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.0")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                selection = new HashSet<>();
                table.setCurrentPage(0);
                table.refreshPage();
                fireEvent();
            }
        });
        
        // Create label
        this.label = new Label(parent, SWT.RIGHT);
        this.label.setText(""); //$NON-NLS-1$
        this.label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        // Init
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        
        // Reset view
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        this.label.setText(""); //$NON-NLS-1$
        this.model = null;
        this.selection = null;
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.SELECTED_QUASI_IDENTIFIERS) {
           update();
        }
    }

    /**
     * Checks the selected items and fires an event on changes
     */
    private void fireEvent() {
        if (model != null && selection != null) {
            if (selection.size() <= model.getRiskModel().getMaxQiSize()) {
                label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.3") + (int)(Math.pow(2, selection.size()) - 1)); //$NON-NLS-1$
                label.setForeground(GUIHelper.COLOR_BLACK);
                if (!selection.equals(model.getSelectedQuasiIdentifiers())) {
                    model.setSelectedQuasiIdentifiers(new HashSet<>(selection));
                    controller.update(new ModelEvent(ViewAttributesQuasiIdentifiers.this, ModelPart.SELECTED_QUASI_IDENTIFIERS, selection));
                }
            } else {
                label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.4") + (int)(Math.pow(2, selection.size()) - 1)); //$NON-NLS-1$
                label.setForeground(GUIHelper.COLOR_RED);
            }
        }
    }

    /**
     * Returns the attributes to display, <code>null</code> if nothing to show.
     * @return
     */
    private List<String> getAttributes() {

        // Check
        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null || model.getSelectedQuasiIdentifiers() == null) {
            return null;
        }
        
        // Obtain handle
        DataHandle handle = model.getInputConfig().getInput().getHandle();
        if (handle == null) {
            return null;
        }
        
        // Return
        List<String> result = new ArrayList<>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            result.add(handle.getAttributeName(i));
        }
        return result;
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {
        if (this.getAttributes() == null) {
            return;
        }
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        this.selection = new HashSet<>(model.getSelectedQuasiIdentifiers());
        this.label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.5") + (int)(Math.pow(2, selection.size())-1)); //$NON-NLS-1$
        SWTUtil.enable(root);
    }
}
