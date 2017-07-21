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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view allows to select a set of attributes for classification analysis
 * 
 * @author Fabian Prasser
 */
public class ViewStatisticsClassificationAttributes implements IView, ViewStatisticsBasic {
    
    /**
     * Internal state management
     * 
     * @author Fabian Prasser
     */
    private class State {

        /** Data */
        private final List<String>        attributes = new ArrayList<String>();
        /** Data */
        private final List<AttributeType> types      = new ArrayList<AttributeType>();
        /** Data */
        private final Set<String>         features   = new HashSet<String>();
        /** Data */
        private final Set<String>         classes    = new HashSet<String>();

        /**
         * Creates a new instance
         * 
         * @param model
         * @param handle
         * @param definition
         */
        private State(Model model, DataHandle handle, DataDefinition definition) {

            for (int col = 0; col < handle.getNumColumns(); col++) {
                String attribute = handle.getAttributeName(col);
                attributes.add(attribute);
                types.add(definition.getAttributeType(attribute));
            }
            features.addAll(model.getSelectedFeatures());
            classes.addAll(model.getSelectedClasses());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            State other = (State) obj;
            if (attributes == null) {
                if (other.attributes != null) return false;
            } else if (!attributes.equals(other.attributes)) return false;
            if (classes == null) {
                if (other.classes != null) return false;
            } else if (!classes.equals(other.classes)) return false;
            if (features == null) {
                if (other.features != null) return false;
            } else if (!features.equals(other.features)) return false;
            if (types == null) {
                if (other.types != null) return false;
            } else if (!types.equals(other.types)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
            result = prime * result + ((classes == null) ? 0 : classes.hashCode());
            result = prime * result + ((features == null) ? 0 : features.hashCode());
            result = prime * result + ((types == null) ? 0 : types.hashCode());
            return result;
        }
    }

    /** Controller */
    private final Controller   controller;

    /** View */
    private final Composite    root;
    /** View */
    private final DynamicTable features;
    /** View */
    private final Table        classes;

    /** Model */
    private Model              model;
    /** State */
    private State              state;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationAttributes(final Composite parent,
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
        features.addSelectionListener(new DelayedChangeListener(1000) {
            @Override
            public void delayedEvent() {
                fireEvent();
            }
        });
        DynamicTableColumn column0 = new DynamicTableColumn(features, SWT.NONE);
        column0.setWidth("10%", "40px");
        DynamicTableColumn column1 = new DynamicTableColumn(features, SWT.NONE);
        column1.setWidth("90%", "40px");
        
        
        // Create button
        classes = SWTUtil.createTable(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        classes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        classes.addSelectionListener(new DelayedChangeListener(1000) {
            public void delayedEvent() {
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
        state = null;
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
    private void fireEvent() {
        Set<String> selectedFeatures = new HashSet<String>();
        for (TableItem item : features.getItems()) {
            if (item.getChecked()) {
                selectedFeatures.add(item.getText(1));
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
                controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.SELECTED_FEATURES_OR_CLASSES, null));
            }
        }
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Check
        if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
            return;
        }
        
        // Create state
        State state = new State(model, 
                                model.getInputConfig().getInput().getHandle(), 
                                model.getOutputDefinition() == null ? model.getInputDefinition() : model.getOutputDefinition());
        
        // Check again
        if (this.state == null || !this.state.equals(state)) {
            this.state = state;
        } else {
            return;
        }

        // Clear
        root.setRedraw(false);        
        for (TableItem item : features.getItems()) {
            item.dispose();
        }
        for (TableItem item : classes.getItems()) {
            item.dispose();
        }
        
        // Add
        for (int i = 0; i < state.attributes.size(); i++) {
            
            // Features
            String attribute = state.attributes.get(i);
            AttributeType type = state.types.get(i);
            Image image = controller.getResources().getImage(type);
            TableItem itemF = new TableItem(features, SWT.NONE);
            itemF.setText(new String[] { "", attribute } );
            itemF.setImage(0, image);
            itemF.setChecked(model.getSelectedFeatures().contains(attribute));

            // Classes
            TableItem itemC = new TableItem(classes, SWT.NONE);
            itemC.setText(attribute);
            itemC.setChecked(model.getSelectedClasses().contains(attribute));
        }
        
        root.setRedraw(true);
        SWTUtil.enable(root);
    }    
}
