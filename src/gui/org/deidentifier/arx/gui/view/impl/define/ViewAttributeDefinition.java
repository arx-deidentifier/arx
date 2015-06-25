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

package org.deidentifier.arx.gui.view.impl.define;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunctionDescription;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataScale;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.model.ModelTransformationMode;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentMultiStack;
import org.deidentifier.arx.gui.view.impl.common.ViewHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This view displays basic attribute information.
 * 
 * @author Fabian Prasser
 */
public class ViewAttributeDefinition implements IView {
    
    /** Resource */
    private static final AttributeType[] COMBO1_TYPES  = new AttributeType[] {
                                                               AttributeType.INSENSITIVE_ATTRIBUTE,
                                                               AttributeType.SENSITIVE_ATTRIBUTE,
                                                               AttributeType.QUASI_IDENTIFYING_ATTRIBUTE,
                                                               AttributeType.IDENTIFYING_ATTRIBUTE };
    
    /** Resource */
    private static final String[]        COMBO1_VALUES = new String[] {
                                                        Resources.getMessage("AttributeDefinitionView.0"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.1"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.2"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.3") }; //$NON-NLS-1$
    
    /** Resource */
    private static final List<MicroAggregationFunctionDescription> FUNCTIONS = AttributeType.listMicroAggregationFunctions(); 

    /** Resource. */
    private static final String                                    ITEM_ALL      = Resources.getMessage("HierarchyView.0");      //$NON-NLS-1$
    
    /** Resource */
    private final Image                                            IMAGE_IDENTIFYING;
    /** Resource */
    private final Image                                            IMAGE_INSENSITIVE;
    /** Resource */
    private final Image                                            IMAGE_QUASI_IDENTIFYING;
    /** Resource */
    private final Image                                            IMAGE_SENSITIVE;

    /** Model */
    private String                                                 attribute     = null;
    /** Model */
    private Model                                                  model;

    /** Controller */
    private final Controller                                       controller;

    /** Widget */
    private final Combo                                            cmbDataType;
    /** Widget */
    private final Text                                             txtDataType;
    /** Widget */
    private final CTabItem                                         tabItem;
    /** Widget */
    private final Combo                                            cmbType;
    /** Widget */
    private final Combo                                            cmbMode;
    /** Widget */
    private final Combo                                            cmbFunction;
    /** Widget */
    private final Button                                           btnMissing;
    /** Widget. */
    private final Combo                                            cmbMin;
    /** Widget. */
    private final Combo                                            cmbMax;
    /** Widget. */
    private final ComponentMultiStack                              transformationStack;
    
    /** View */
    private final ViewHierarchy                                    viewGeneralization;

    /**
     * Constructor.
     *
     * @param parent
     * @param attribute
     * @param controller
     */
    public ViewAttributeDefinition(final CTabFolder parent,
                                   final String attribute,
                                   final Controller controller) {
        
        // Load images
        IMAGE_INSENSITIVE = controller.getResources().getManagedImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE = controller.getResources().getManagedImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources().getManagedImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING = controller.getResources().getManagedImage("bullet_red.png"); //$NON-NLS-1$
        
        // Register
        this.controller = controller;
        this.attribute = attribute;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.DATA_TYPE, this);
        this.controller.addListener(ModelPart.HIERARCHY, this);
        
        // Create input group
        tabItem = new CTabItem(parent, SWT.NULL);
        tabItem.setText(attribute);
        tabItem.setShowClose(false);
        tabItem.setImage(IMAGE_INSENSITIVE);
        
        // Group
        Composite group = new Composite(parent, SWT.NULL);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 1;
        group.setLayout(groupInputGridLayout);
        
        // Group
        final Composite innerGroup = new Composite(group, SWT.NULL);
        innerGroup.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout typeInputGridLayout = new GridLayout();
        typeInputGridLayout.numColumns = 6;
        innerGroup.setLayout(typeInputGridLayout);
        
        // Combo for attribute type
        final Label kLabel = new Label(innerGroup, SWT.PUSH);
        kLabel.setText(Resources.getMessage("AttributeDefinitionView.7")); //$NON-NLS-1$
        cmbType = new Combo(innerGroup, SWT.READ_ONLY);
        cmbType.setLayoutData(SWTUtil.createFillGridData());
        cmbType.setItems(COMBO1_VALUES);
        cmbType.select(0);
        cmbType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionAttributeTypeChanged();
            }
        });
        
        // Combo for data type
        final Label kLabel2 = new Label(innerGroup, SWT.PUSH);
        kLabel2.setText(Resources.getMessage("AttributeDefinitionView.8")); //$NON-NLS-1$
        cmbDataType = new Combo(innerGroup, SWT.READ_ONLY);
        cmbDataType.setLayoutData(SWTUtil.createFillGridData());
        cmbDataType.setItems(getDataTypes());
        cmbDataType.select(getIndexOfDataType(DataType.STRING));
        cmbDataType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionDataTypeChanged();
            }
        });
        
        final Label kLabel3 = new Label(innerGroup, SWT.PUSH);
        kLabel3.setText(Resources.getMessage("AttributeDefinitionView.10")); //$NON-NLS-1$
        txtDataType = new Text(innerGroup, SWT.READ_ONLY | SWT.BORDER);
        txtDataType.setLayoutData(SWTUtil.createFillGridData());
        txtDataType.setEditable(false);
        txtDataType.setText(""); //$NON-NLS-1$

        // Add combo for mode
        final Label fLabel2 = new Label(innerGroup, SWT.PUSH);
        fLabel2.setText(Resources.getMessage("ViewMicoaggregation.4")); //$NON-NLS-1$
        cmbMode = new Combo(innerGroup, SWT.READ_ONLY);
        cmbMode.setLayoutData(SWTUtil.createFillGridData());
        cmbMode.setItems(new String[]{Resources.getMessage("ViewMicoaggregation.5"),
                                      Resources.getMessage("ViewMicoaggregation.6")});
        cmbMode.select(0);
        cmbMode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionTransformationModeChanged();
            }
        });
        
        // Create multistack
        transformationStack = new ComponentMultiStack(innerGroup);
        
        // First column
        Composite first = transformationStack.create(SWTUtil.createGridData());
        Composite compositeLabelMin = new Composite(first, SWT.NONE);
        GridLayout compositeLabelMinLayout = new GridLayout();
        compositeLabelMinLayout.numColumns = 1;
        compositeLabelMinLayout.marginLeft = 0;
        compositeLabelMinLayout.marginRight = 0;
        compositeLabelMinLayout.marginWidth = 0;
        compositeLabelMin.setLayout(compositeLabelMinLayout);
        Label labelMin = new Label(compositeLabelMin, SWT.PUSH);
        labelMin.setText(Resources.getMessage("HierarchyView.4")); //$NON-NLS-1$
        Composite compositelabelFunction = new Composite(first, SWT.NONE);
        GridLayout compositelabelFunctionLayout = new GridLayout();
        compositelabelFunctionLayout.numColumns = 1;
        compositelabelFunctionLayout.marginLeft = 0;
        compositelabelFunctionLayout.marginRight = 0;
        compositelabelFunctionLayout.marginWidth = 0;
        compositelabelFunction.setLayout(compositelabelFunctionLayout);
        final Label labelFunction = new Label(compositelabelFunction, SWT.PUSH);
        labelFunction.setText(Resources.getMessage("ViewMicoaggregation.0")); //$NON-NLS-1$

        // Second column
        Composite second = transformationStack.create(SWTUtil.createFillHorizontallyGridData());
        this.cmbMin = new Combo(second, SWT.READ_ONLY);
        this.cmbMin.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.cmbMin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMinChanged();
            }
        });
        this.cmbFunction = new Combo(second, SWT.READ_ONLY);
        this.cmbFunction.setLayoutData(SWTUtil.createFillGridData());
        this.cmbFunction.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionFunctionChanged();
            }
        });

        // Third column
        Composite third = transformationStack.create(SWTUtil.createGridData());
        Composite compositelabelMax = new Composite(third, SWT.NONE);
        GridLayout compositelabelMaxLayout = new GridLayout();
        compositelabelMaxLayout.numColumns = 1;
        compositelabelMaxLayout.marginLeft = 0;
        compositelabelMaxLayout.marginRight = 0;
        compositelabelMaxLayout.marginWidth = 0;
        compositelabelMax.setLayout(compositelabelMaxLayout);
        Label labelMax = new Label(compositelabelMax, SWT.PUSH);
        labelMax.setText(Resources.getMessage("HierarchyView.6")); //$NON-NLS-1$
        Composite compositelabelMissing = new Composite(third, SWT.NONE);
        GridLayout compositelabelMissingLayout = new GridLayout();
        compositelabelMissingLayout.numColumns = 1;
        compositelabelMissingLayout.marginLeft = 0;
        compositelabelMissingLayout.marginRight = 0;
        compositelabelMissingLayout.marginWidth = 0;
        compositelabelMissing.setLayout(compositelabelMissingLayout);
        Label labelMissing = new Label(compositelabelMissing, SWT.PUSH);
        labelMissing.setText(Resources.getMessage("ViewMicoaggregation.7")); //$NON-NLS-1$

        // Fourth column
        Composite fourth = transformationStack.create(SWTUtil.createFillHorizontallyGridData());
        this.cmbMax = new Combo(fourth, SWT.READ_ONLY);
        this.cmbMax.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.cmbMax.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMaxChanged();
            }
        });
        btnMissing = new Button(fourth, SWT.CHECK);
        GridData btnMissingData = SWTUtil.createFillGridData();
        btnMissingData.horizontalSpan = 2;
        btnMissing.setLayoutData(btnMissingData);
        btnMissing.setText(Resources.getMessage("ViewMicoaggregation.2")); //$NON-NLS-1$
        btnMissing.setSelection(true);
        btnMissing.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMissingChanged();
            }
        });
        
        // Collect info about children in stack
        transformationStack.pack();
          
        // Editor hierarchy
        viewGeneralization = new ViewHierarchy(group, attribute, controller);

        // Attach to tab
        tabItem.setControl(group);
    }
    
    @Override
    public void dispose() {
        
        // Dispose views
        controller.removeListener(this);
        viewGeneralization.dispose();
    }
    
    @Override
    public void reset() {
        txtDataType.setText(""); //$NON-NLS-1$
        updateMinMax();
    }
    
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            updateAttributeType();
            updateDataType();
            updateFunction();
            updateMode();
            updateMinMax();
            viewGeneralization.update(event);
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateAttributeType();
                updateDataType();
                updateIcon();
            }
        } else if (event.part == ModelPart.HIERARCHY) {
            // TODO: Attribute should be associated with the event
            if (attribute.equals(model.getSelectedAttribute())) {
                updateMinMax();
            }
        } else if (event.part == ModelPart.INPUT) {
            updateAttributeType();
            updateDataType();
            updateMode();
            updateMinMax();
            viewGeneralization.update(event);
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateFunction();
            updateMode();
        }
    }
    
    /**
     * Attribute type changed
     */
    private void actionAttributeTypeChanged() {
        if ((cmbType.getSelectionIndex() != -1) &&
            (attribute != null)) {
            if ((model != null) &&
                (model.getInputConfig().getInput() != null)) {
                final AttributeType type = COMBO1_TYPES[cmbType.getSelectionIndex()];
                final DataDefinition definition = model.getInputDefinition();
                
                // Handle QIs
                if (type == null) {
                    definition.setAttributeType(attribute,
                                                Hierarchy.create());
                } else {
                    definition.setAttributeType(attribute, type);
                }
                
                // Do we need to disable criteria?
                boolean criteriaDisabled = false;
                
                // Enable/disable criteria for sensitive attributes
                if (type != AttributeType.SENSITIVE_ATTRIBUTE) {
                    
                    if (model.getLDiversityModel().get(attribute).isEnabled() ||
                        model.getTClosenessModel().get(attribute).isEnabled()) {
                        criteriaDisabled = true;
                    }
                    
                    model.getTClosenessModel().get(attribute).setEnabled(false);
                    model.getLDiversityModel().get(attribute).setEnabled(false);
                }
                
                // Enable/disable criteria for quasi-identifiers
                if (definition.getQuasiIdentifyingAttributes().isEmpty()) {
                    
                    if (model.getKAnonymityModel().isEnabled() ||
                        model.getDPresenceModel().isEnabled()) {
                        criteriaDisabled = true;
                    }
                    
                    model.getKAnonymityModel().setEnabled(false);
                    model.getDPresenceModel().setEnabled(false);
                    for (ModelRiskBasedCriterion c : model.getRiskBasedModel()) {
                        if (c.isEnabled()) {
                            criteriaDisabled = true;
                        }
                        c.setEnabled(false);
                    }
                    
                }
                
                // Update icon
                updateIcon();
                // Update mode
                updateMode();
                
                // Update criteria
                if (criteriaDisabled) {
                    controller.update(new ModelEvent(this,
                                                     ModelPart.CRITERION_DEFINITION,
                                                     null));
                }
                
                // Update the views
                controller.update(new ModelEvent(this,
                                                 ModelPart.ATTRIBUTE_TYPE,
                                                 attribute));
            }
        }
    }
    
    /**
     * Data type changed
     */
    private void actionDataTypeChanged() {
        if ((cmbDataType.getSelectionIndex() != -1) &&
            (attribute != null)) {
            if ((model != null) &&
                (model.getInputConfig().getInput() != null)) {
                
                // Obtain type
                String label = cmbDataType.getItem(cmbDataType.getSelectionIndex());
                DataTypeDescription<?> description = getDataType(label);
                DataType<?> type;
                
                // Open format dialog
                if (description.getLabel().equals("Ordinal")) { //$NON-NLS-1$
                    final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                    final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                    String[] array = controller.actionShowOrderValuesDialog(controller.getResources().getShell(),
                                                                            text1, text2, DataType.STRING,
                                                                            model.getLocale(), getValuesAsArray());
                    if (array == null) {
                        type = DataType.STRING;
                    } else {
                        try {
                            type = DataType.createOrderedString(array);
                            if (!isValidDataType(type, getValuesAsList())) {
                                type = DataType.STRING;
                            }
                        } catch (Exception e) {
                            controller.actionShowInfoDialog(controller.getResources().getShell(),
                                                            Resources.getMessage("ViewAttributeDefinition.1"), Resources.getMessage("ViewAttributeDefinition.2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                            type = DataType.STRING;
                        }
                    }
                } else if (description.hasFormat()) {
                    final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                    final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                    final String format = controller.actionShowFormatInputDialog(controller.getResources().getShell(),
                                                                                 text1, text2, model.getLocale(), description, getValuesAsList());
                    if (format == null) {
                        type = DataType.STRING;
                    } else {
                        type = description.newInstance(format, model.getLocale());
                    }
                } else {
                    type = description.newInstance();
                    if (!isValidDataType(type, getValuesAsList())) {
                        type = DataType.STRING;
                    }
                }
                
                // Set and update
                model.getInputDefinition().setDataType(attribute, type);
                updateFunction();
                updateDataType();
                controller.update(new ModelEvent(this, ModelPart.DATA_TYPE, attribute));
            }
        }
    }
    
    /**
     * Function changed
     */
    private void actionFunctionChanged() {
        if (cmbFunction.getSelectionIndex() != -1 && model != null && model.getInputConfig() != null) {
            String function = cmbFunction.getItem(cmbFunction.getSelectionIndex());
            model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(function));
        }
    }
    
    /**
     * Updates the max generalization level.
     *
     * @return
     */
    private boolean actionMaxChanged() {
        
        if (cmbMax.getSelectionIndex() >= 0) {
            if (cmbMax.getSelectionIndex() < (cmbMin.getSelectionIndex() - 1)) {
                cmbMax.select(cmbMin.getSelectionIndex() - 1);
            }
            if (model != null) {
                String val = cmbMax.getItem(cmbMax.getSelectionIndex());
                model.getInputConfig().setMaximumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the min generalization level.
     *
     * @return
     */
    private boolean actionMinChanged() {
        
        if (cmbMin.getSelectionIndex() >= 0) {
            if (cmbMin.getSelectionIndex() > (cmbMax.getSelectionIndex() + 1)) {
                cmbMin.select(cmbMax.getSelectionIndex() + 1);
            }
            if (model != null) {
                String val = cmbMin.getItem(cmbMin.getSelectionIndex());
                model.getInputConfig().setMinimumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            }
        }
        return false;
    }

    /**
     * Missing changed
     */
    private void actionMissingChanged() {
        if (model != null && model.getInputConfig() != null) {
            model.getInputConfig().setMicroAggregationIgnoreMissingData(attribute, btnMissing.getSelection());
        }
    }
    
    /**
     * Transformation mode changed
     */
    private void actionTransformationModeChanged() {
        if (model == null || model.getInputConfig() == null) {
            return;
        }
        if (cmbMode.getSelectionIndex() == 0) {
            model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.GENERALIZATION);
            transformationStack.setLayer(0);
        } else {
            model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.MICRO_AGGREGATION);
            transformationStack.setLayer(1);
        }
    }
    
    /**
     * Returns a description for the given label.
     *
     * @param label
     * @return
     */
    private DataTypeDescription<?> getDataType(String label) {
        for (DataTypeDescription<?> desc : DataType.list()) {
            if (label.equals(desc.getLabel())) {
                return desc;
            }
        }
        throw new RuntimeException(Resources.getMessage("ViewAttributeDefinition.5") + label); //$NON-NLS-1$
    }
    
    /**
     * Returns the labels of all available data types.
     *
     * @return
     */
    private String[] getDataTypes() {
        List<String> list = new ArrayList<String>();
        for (DataTypeDescription<?> desc : DataType.list()) {
            list.add(desc.getLabel());
        }
        return list.toArray(new String[list.size()]);
    }
    
    /**
     * Returns the index of a given data type.
     *
     * @param type
     * @return
     */
    private int getIndexOfDataType(DataType<?> type) {
        int idx = 0;
        for (DataTypeDescription<?> desc : DataType.list()) {
            if (desc.getLabel().equals(type.getDescription().getLabel())) {
                return idx;
            }
            idx++;
        }
        throw new RuntimeException(Resources.getMessage("ViewAttributeDefinition.6") + type.getDescription().getLabel()); //$NON-NLS-1$
    }
    
    /**
     * Returns the microaggregation function for the given label
     * @param function
     * @return
     */
    private MicroAggregationFunctionDescription getMicroAggregationFunction(String label) {
        for (MicroAggregationFunctionDescription function : FUNCTIONS) {
            if (function.getLabel().equals(label)) {
                return function;
            }
        }
        return null;
    }
    
    /**
     * Create an array of the values in the column for this attribute.
     *
     * @return
     */
    private String[] getValuesAsArray() {
        final DataHandle h = model.getInputConfig().getInput().getHandle();
        return h.getStatistics().getDistinctValues(h.getColumnIndexOf(attribute));
    }
    
    /**
     * Create a collection of the values in the column for this attribute.
     *
     * @return
     */
    private Collection<String> getValuesAsList() {
        return Arrays.asList(getValuesAsArray());
    }
    
    /**
     * Checks whether the data type is valid.
     *
     * @param type
     * @param values
     * @return
     */
    private boolean isValidDataType(DataType<?> type, Collection<String> values) {
        for (String value : values) {
            if (!type.isValid(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * Update the attribute type.
     */
    private void updateAttributeType() {
        AttributeType type = model.getInputDefinition().getAttributeType(attribute);
        for (int i = 0; i < COMBO1_TYPES.length; i++) {
            if (type == COMBO1_TYPES[i]) {
                cmbType.select(i);
                break;
            }
        }
    }
    
    /**
     * Update the data type.
     */
    private void updateDataType() {
        
        DataType<?> dtype = model.getInputDefinition().getDataType(attribute);
        cmbDataType.select(getIndexOfDataType(dtype));
        
        if (!(dtype instanceof ARXOrderedString) && dtype.getDescription().hasFormat()) {
            DataTypeWithFormat dtwf = (DataTypeWithFormat) dtype;
            String format = dtwf.getFormat();
            if (format == null) {
                txtDataType.setText(Resources.getMessage("ViewAttributeDefinition.7")); //$NON-NLS-1$
            } else {
                txtDataType.setText(format);
            }
        } else {
            txtDataType.setText(Resources.getMessage("ViewAttributeDefinition.8")); //$NON-NLS-1$
        }
    }
    
    /**
     * Update function
     */
    private void updateFunction() {
        if (model != null && model.getInputConfig() != null) {
            DataScale scale = model.getInputDefinition().getDataType(attribute).getDescription().getScale();
            List<String> functions = new ArrayList<String>();
            for (MicroAggregationFunctionDescription function : FUNCTIONS) {
                if (scale.provides(function.getRequiredScale())) {
                    functions.add(function.getLabel());
                } 
            }
            this.cmbFunction.setItems(functions.toArray(new String[functions.size()]));
            int index = functions.indexOf(model.getInputConfig().getMicroAggregationFunction(attribute));
            if (index == -1) {
                this.cmbFunction.select(0);
                this.model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(functions.get(0)));
            } else {
                this.cmbFunction.select(index);
                this.model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(functions.get(index)));
            }
            this.btnMissing.setSelection(this.model.getInputConfig().getMicroAggregationIgnoreMissingData(attribute));
        }
    }

    /**
     * Update the column icon.
     */
    private void updateIcon() {
        AttributeType type = model.getInputDefinition().getAttributeType(attribute);
        if (type == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
            tabItem.setImage(IMAGE_QUASI_IDENTIFYING);
        } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            tabItem.setImage(IMAGE_INSENSITIVE);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            tabItem.setImage(IMAGE_SENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            tabItem.setImage(IMAGE_IDENTIFYING);
        }
    }

    /**
     * Updates the combos.
     */
    private void updateMinMax() {
        
        // Check whether min & max are still ok
        if (model == null || model.getInputConfig() == null || cmbMin == null || cmbMin.isDisposed()) {
            return;
        }
        
        // Prepare lists
        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        int length = 0;
        Hierarchy hierarchy = model.getInputConfig().getHierarchy(attribute);
        if (!(hierarchy == null || hierarchy.getHierarchy() == null || hierarchy.getHierarchy()[0] == null || hierarchy.getHierarchy()[0].length == 0)) {
            length = hierarchy.getHierarchy()[0].length;
        }
        for (int i = 0; i < length; i++) {
            minItems.add(String.valueOf(i));
            maxItems.add(String.valueOf(i));
        }
        maxItems.add(ITEM_ALL);
        
        // Determine min index
        Integer minModel = model.getInputConfig().getMinimumGeneralization(attribute);
        int minIndex = minModel != null ? minModel + 1 : 0;
        
        // Determine max index
        Integer maxModel = model.getInputConfig().getMaximumGeneralization(attribute);
        int maxIndex = maxModel != null ? maxModel : maxItems.size() - 1;
        
        // Fix indices
        maxIndex = maxIndex > maxItems.size() - 1 ? maxItems.size() - 1 : maxIndex;
        maxIndex = maxIndex < 0 ? maxItems.size() - 1 : maxIndex;
        minIndex = minIndex > minItems.size() - 1 ? minItems.size() - 1 : minIndex;
        minIndex = minIndex < 0 ? 0 : minIndex;
        minIndex = minIndex > (maxIndex + 1) ? maxIndex + 1 : minIndex;
        
        // Set items
        cmbMin.setItems(minItems.toArray(new String[minItems.size()]));
        cmbMax.setItems(maxItems.toArray(new String[maxItems.size()]));
        
        // Select
        cmbMin.select(minIndex);
        cmbMax.select(maxIndex);
        actionMinChanged();
        actionMaxChanged();
    }

    /**
     * Update mode
     */
    private void updateMode() {
        
        if (model.getInputDefinition().getQuasiIdentifyingAttributes().contains(attribute) &&
            model != null && model.getInputConfig() != null) {
            
            if (model.getInputConfig().getTransformationMode(attribute) == ModelTransformationMode.GENERALIZATION) {
                transformationStack.setLayer(0);
                cmbMode.select(0);
            } else {
                transformationStack.setLayer(1);
                cmbMode.select(1);
            }
        } else {
            transformationStack.setLayer(0);
        }
    }
}
