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

package org.deidentifier.arx.gui.view.impl.utility;


import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This view allows to select a set of attributes for classification analysis
 * 
 * @author Fabian Prasser
 */
public class ViewClassificationAttributes implements IView {

    /** Controller */
    private final Controller controller;

    /** View */
    private final Composite  root;
    /** View */
    private final Table      features;
    /** View */
    private final Table      classes;

    /** Model */
    private Model            model;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewClassificationAttributes(final Composite parent,
                                    final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_FEATURES_OR_CLASSES, this);
        
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
        features = SWTUtil.createTable(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        features.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        features.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                fireEvent();
            }   
        });
        
        // Create button
        classes = SWTUtil.createTable(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        classes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        classes.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                fireEvent();
            }   
        });
        
        // Reset view
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
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
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.SELECTED_FEATURES_OR_CLASSES) {
           update();
        }
    }

    /**
     * Checks the selected items and fires an event on changes
     */
    private void fireEvent() {
        Set<String> selectedFeatures = new HashSet<String>();
        for (TableItem item : features.getItems()) {
            if (item.getChecked()) {
                selectedFeatures.add(item.getText());
            }
        }
        Set<String> selectedClasses = new HashSet<String>();
        for (TableItem item : classes.getItems()) {
            if (item.getChecked()) {
                selectedClasses.add(item.getText());
            }
        }
        if (model != null) {
            
            boolean modified = false;
            if (!selectedFeatures.equals(model.getSelectedFeatures())) {
                model.setSelectedFeatures(selectedFeatures);
                modified = true;
            }
            if (!selectedClasses.equals(model.getSelectedClasses())) {
                model.setSelectedClasses(selectedClasses);
                modified = true;
            }
            if (modified) {
                controller.update(new ModelEvent(ViewClassificationAttributes.this, ModelPart.SELECTED_FEATURES_OR_CLASSES, null));
            }
        }
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
        
        Set<String> selectedFeatures = model.getSelectedFeatures();
        
        for (TableItem item : features.getItems()) {
            item.dispose();
        }
        
        for (int i = 0; i < handle.getNumColumns(); i++) {
            TableItem item = new TableItem(features, SWT.NONE);
            String value = handle.getAttributeName(i);
            item.setText(value);
            item.setChecked(selectedFeatures.contains(value));
        }
        
        Set<String> selectedClasses = model.getSelectedClasses();
        
        for (TableItem item : classes.getItems()) {
            item.dispose();
        }
        
        for (int i = 0; i < handle.getNumColumns(); i++) {
            TableItem item = new TableItem(classes, SWT.NONE);
            String value = handle.getAttributeName(i);
            item.setText(value);
            item.setChecked(selectedClasses.contains(value));
        }
        
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
