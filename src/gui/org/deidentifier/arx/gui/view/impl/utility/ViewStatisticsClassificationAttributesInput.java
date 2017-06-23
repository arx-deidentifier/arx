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

package org.deidentifier.arx.gui.view.impl.utility;


import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view allows to select a set of attributes for classification analysis
 * 
 * @author Fabian Prasser
 */
public class ViewStatisticsClassificationAttributesInput implements IView, ViewStatisticsBasic {

    /** Controller */
    private final Controller   controller;

    /** View */
    private final Composite    root;
    /** View */
    private final DynamicTable features;
    /** View */
    private final DynamicTable classes;

    /** Model */
    private Model              model;

    /** Constant */
    private final String       ALL = Resources.getMessage("ViewClassificationAttributes.3");

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationAttributesInput(final Composite parent,
                                    final Controller controller) {
        
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_FEATURES_OR_CLASSES, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.OUTPUT, this);
        
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        Label label = new Label(parent, SWT.LEFT);
        label.setText(Resources.getMessage("ViewClassificationAttributes.0")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        label = new Label(parent, SWT.LEFT);
        label.setText(Resources.getMessage("ViewClassificationAttributes.1")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        // Create table
        features = SWTUtil.createTableDynamic(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        features.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        features.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Set<String> newSelection = new HashSet<String>();
                boolean update = fireEvent(arg0, features, model.getSelectedFeatures(), newSelection);
                if (update) {
                    model.setSelectedFeatures(newSelection);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributesInput.this, ModelPart.SELECTED_FEATURES_OR_CLASSES, null));
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub

            }
        });
        DynamicTableColumn column0 = new DynamicTableColumn(features, SWT.NONE);
        column0.setWidth("10%", "40px");
        DynamicTableColumn column1 = new DynamicTableColumn(features, SWT.NONE);
        column1.setWidth("90%", "40px");
        
        
        // Create button
        classes = SWTUtil.createTableDynamic(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        classes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        classes.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Set<String> newSelection = new HashSet<String>();
                boolean update = fireEvent(arg0, classes, model.getSelectedClasses(), newSelection);
                if (update) {
                   model.setSelectedClasses(newSelection);
                   controller.update(new ModelEvent(ViewStatisticsClassificationAttributesInput.this, ModelPart.SELECTED_FEATURES_OR_CLASSES, null));
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        
        DynamicTableColumn column2 = new DynamicTableColumn(classes, SWT.NONE);
        column2.setWidth("10%", "40px");
        DynamicTableColumn column3 = new DynamicTableColumn(classes, SWT.NONE);
        column3.setWidth("90%", "40px");

        // Reset view
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public Composite getParent() {
        return this.root;
    }

    /**
     * Returns the type
     * @return
     */
    public ViewUtilityType getType() {
        return ViewUtilityType.CLASSIFICATION;
    }

    @Override
    public void reset() {
        for (TableItem item : features.getItems()) {
            item.dispose();
        }
        for (TableItem item : classes.getItems()) {
            item.dispose();
        }
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        } else if (event.part == ModelPart.INPUT ||
                   event.part == ModelPart.SELECTED_FEATURES_OR_CLASSES ||
                   event.part == ModelPart.ATTRIBUTE_TYPE || event.part == ModelPart.OUTPUT) {
           update();
        }
    }
    
    /**
     * Checks the selected items and fires an event on changes
     */
    private boolean fireEvent(SelectionEvent event, DynamicTable table, Set<String> currentSelection, Set<String> newSelection){
        TableItem item = (TableItem) event.item;

        Boolean checkAll = null;
        if (item.getText(1).equals(ALL)) {
            if (item.getChecked()) {
                checkAll = true;
            } else {
                checkAll = false;
            }
        }

        // ignore first item
        for (int i = 1; i < table.getItemCount(); i++) {
            item = table.getItem(i);
            // all checkbox checked or one item checked
            if ((checkAll != null && checkAll) || (item.getChecked() && checkAll == null)) {
                newSelection.add(item.getText(1));
                item.setChecked(true);
            } 
            // all checkbox unchecked
            else if (checkAll != null && !checkAll) {
                item.setChecked(false);
            }
        }

        // Update
        if (model != null && !newSelection.equals(currentSelection)) {
            return true;
        }
        return false;
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) {
            return;
        }
        
        DataHandle handle = model.getInputConfig().getInput().getHandle();

        root.setRedraw(false);
        
        for (TableItem item : features.getItems()) {
            item.dispose();
        }
        for (TableItem item : classes.getItems()) {
            item.dispose();
        }
        
        TableItem itemAllFeatures = new TableItem(features, SWT.NONE);
        itemAllFeatures.setText(new String[] { "", ALL });

        TableItem itemAllclasses = new TableItem(classes, SWT.NONE);
        itemAllclasses.setText(new String[] { "", ALL });
        
        
        for (int col = 0; col < handle.getNumColumns(); col++) {
            String attribute = handle.getAttributeName(col);
            DataDefinition def = model.getOutputDefinition() == null ? model.getInputDefinition() : model.getOutputDefinition();
            Image image = controller.getResources().getImage(def.getAttributeType(attribute));
            
            // Features
            TableItem itemF = new TableItem(features, SWT.NONE);
            itemF.setText(new String[] { "", attribute } );
            itemF.setImage(0, image);
            itemF.setChecked(model.getSelectedFeatures().contains(attribute));

            // Classes
            TableItem itemC = new TableItem(classes, SWT.NONE);
            itemC.setText(new String[] { "", attribute });
            itemC.setChecked(model.getSelectedClasses().contains(attribute));
        }
        
        root.setRedraw(true);
        SWTUtil.enable(root);
    }    

}
