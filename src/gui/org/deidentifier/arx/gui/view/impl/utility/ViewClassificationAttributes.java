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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
public class ViewClassificationAttributes implements IView, ViewStatisticsBasic {

    /** Label */
    private static final String LABEL_CATEGORICAL = Resources.getMessage("ViewClassificationAttributes.2"); //$NON-NLS-1$
    /** Delay */
    private static final int    DELAY             = 1000;

    /** Controller */
    private final Controller    controller;

    /** View */
    private final Composite     root;
    /** View */
    private final DynamicTable  features;
    /** View */
    private final Table         classes;

    /** Model */
    private Model               model;

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
        features = SWTUtil.createTableDynamic(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        features.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        DynamicTableColumn column1 = new DynamicTableColumn(features, SWT.NONE);
        column1.setWidth("50%");
        DynamicTableColumn column2 = new DynamicTableColumn(features, SWT.NONE);
        column2.setWidth("50%");
        
        features.addSelectionListener(new DelayedChangeListener(DELAY) {
            @Override
            public void delayedEvent() {
                fireEvent();
            }
        });
        
        // Create button
        classes = SWTUtil.createTable(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        classes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        classes.addSelectionListener(new DelayedChangeListener(DELAY) {
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

        // Check
        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) {
            return;
        }
        
        // Prepare
        DataHandle handle = model.getInputConfig().getInput().getHandle();
        root.setRedraw(false);
        
        // Add features
        Set<String> selectedFeatures = model.getSelectedFeatures();
        
        for (TableItem item : features.getItems()) {
            item.dispose();
        }
        
        for (int i = 0; i < handle.getNumColumns(); i++) {
            TableItem item = new TableItem(features, SWT.NONE);
            final String value = handle.getAttributeName(i);
            item.setText(value);
            item.setChecked(selectedFeatures.contains(value));
            
            TableEditor editor = new TableEditor(features);
            final CCombo combo = new CCombo(features, SWT.NONE);
            final Color defaultColor = combo.getForeground();
            combo.add("x");
            combo.add("x^2");
            combo.add("sqrt(x)");
            combo.add("log(x)");
            combo.add("2^x");
            combo.add("1/x");
            combo.add(LABEL_CATEGORICAL);
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    updateCombo(value, combo, defaultColor);
                }
            });
            combo.addSelectionListener(new DelayedChangeListener(DELAY) {
                public void delayedEvent() {
                    updateFunction(value, combo);
                }   
            });
            combo.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent arg0) {
                    updateCombo(value, combo, defaultColor);
                }
            });
            combo.addKeyListener(new DelayedChangeListener(DELAY) {
                public void delayedEvent() {
                    updateFunction(value, combo);
                }   
            });
            editor.grabHorizontal = true;
            editor.setEditor(combo, item, 1);
            String function = model.getClassificationModel().getFeatureScaling().getScalingFunction(value);
            if (function == null || function.equals("")) {
                function = LABEL_CATEGORICAL;
            }
            combo.setText(function);
        }
        
        // Add classes
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
        
        // Finish
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
    
    /**
     * Updates the combo
     * @param attribute
     * @param combo
     * @param defaultColor 
     */
    private void updateCombo(String attribute, CCombo combo, Color defaultColor) {
        String function = combo.getText();
        if (function.equals(LABEL_CATEGORICAL)) {
            combo.setForeground(defaultColor);
        } else if (!model.getClassificationModel().getFeatureScaling().isValidScalingFunction(function) || 
                   !(model.getInputDefinition().getDataType(attribute) instanceof DataTypeWithRatioScale)) {
            combo.setForeground(GUIHelper.COLOR_RED);
        } else {
            combo.setForeground(defaultColor);
        }
    }

    /**
     * Updates the function
     * @param attribute
     * @param combo
     */
    private void updateFunction(String attribute, CCombo combo) {
        String function = combo.getText();
        if (function.equals(LABEL_CATEGORICAL)) {
            model.getClassificationModel().setScalingFunction(attribute, null);
        } else if (!model.getClassificationModel().getFeatureScaling().isValidScalingFunction(function) || 
                   !(model.getInputDefinition().getDataType(attribute) instanceof DataTypeWithRatioScale)) {
            model.getClassificationModel().setScalingFunction(attribute, null);
        } else {
            model.getClassificationModel().setScalingFunction(attribute, function);
        }
    }
}
