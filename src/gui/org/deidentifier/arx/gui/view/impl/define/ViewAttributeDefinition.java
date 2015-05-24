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
 * TODO: Display data type formats
 * 
 * @author Fabian Prasser
 */
public class ViewAttributeDefinition implements IView {
    
    /** Resource */
    private static final AttributeType[] COMBO1_TYPES  = new AttributeType[] {
                                                               AttributeType.INSENSITIVE_ATTRIBUTE,
                                                               AttributeType.SENSITIVE_ATTRIBUTE,
                                                               null,
                                                               AttributeType.IDENTIFYING_ATTRIBUTE };
    
    /** Resource */
    private static final String[]        COMBO1_VALUES = new String[] {
                                                        Resources.getMessage("AttributeDefinitionView.0"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.1"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.2"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.3") }; //$NON-NLS-1$
    
    /** Resource */
    private static final List<MicroAggregationFunctionDescription> FUNCTIONS = AttributeType.listMicroAggregationFunctions(); 
                                                                    

    /** Resource */
    private final Image                  IMAGE_IDENTIFYING;
    /** Resource */
    private final Image                  IMAGE_INSENSITIVE;
    /** Resource */
    private final Image                  IMAGE_QUASI_IDENTIFYING;
    /** Resource */
    private final Image                  IMAGE_SENSITIVE;
    
    
    /** Model */
    private String                       attribute     = null;
    /** Model */
    private Model                        model;
    
    /** Controller */
    private final Controller             controller;
    
    /** Widget */
    private final Combo                  cmbDataType;
    /** Widget */
    private final Text                   txtDataType;
    /** Widget */
    private final CTabItem               tabItem;
    /** Widget */
    private final Combo                  cmbType;
    /** Widget */
    private final Combo                  cmbMode;
    /** Widget */
    private final Combo                  cmbFunction;
    /** Widget */
    private final Button                 btnMissing;
    
    /** View */
    private final ViewHierarchy          viewGeneralization;
    
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
        IMAGE_INSENSITIVE = controller.getResources().getImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE = controller.getResources().getImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources().getImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING = controller.getResources().getImage("bullet_red.png"); //$NON-NLS-1$
        
        // Register
        this.controller = controller;
        this.attribute = attribute;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.DATA_TYPE, this);
        
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
        final IView outer = this;
        final Label kLabel = new Label(innerGroup, SWT.PUSH);
        kLabel.setText(Resources.getMessage("AttributeDefinitionView.7")); //$NON-NLS-1$
        cmbType = new Combo(innerGroup, SWT.READ_ONLY);
        cmbType.setLayoutData(SWTUtil.createFillGridData());
        cmbType.setItems(COMBO1_VALUES);
        cmbType.select(0);
        cmbType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                
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
                        
                        // Update criteria
                        if (criteriaDisabled) {
                            controller.update(new ModelEvent(outer,
                                                             ModelPart.CRITERION_DEFINITION,
                                                             null));
                        }
                        
                        // Update the views
                        controller.update(new ModelEvent(outer,
                                                         ModelPart.ATTRIBUTE_TYPE,
                                                         attribute));
                    }
                }
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
                        controller.update(new ModelEvent(outer, ModelPart.DATA_TYPE, attribute));
                    }
                }
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

        // Add combo for function
        final Label fLabel = new Label(innerGroup, SWT.PUSH);
        fLabel.setText(Resources.getMessage("ViewMicoaggregation.0")); //$NON-NLS-1$
        cmbFunction = new Combo(innerGroup, SWT.READ_ONLY);
        cmbFunction.setLayoutData(SWTUtil.createFillGridData());
        cmbFunction.setEnabled(false);
        cmbFunction.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (cmbFunction.getSelectionIndex() != -1 && model != null && model.getInputConfig() != null) {
                    String function = cmbFunction.getItem(cmbFunction.getSelectionIndex());
                    model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(function));
                }
            }
        });
        
        // Add button for missing data
        btnMissing = new Button(innerGroup, SWT.CHECK);
        GridData btnMissingData = SWTUtil.createFillGridData();
        btnMissingData.horizontalSpan = 2;
        btnMissing.setLayoutData(btnMissingData);
        btnMissing.setText(Resources.getMessage("ViewMicoaggregation.2")); //$NON-NLS-1$
        btnMissing.setSelection(true);
        btnMissing.setEnabled(false);
        btnMissing.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (model != null && model.getInputConfig() != null) {
                    model.getInputConfig().setMicroAggregationIgnoreMissingData(attribute, btnMissing.getSelection());
                }
            }
        });
          
        // Editor hierarchy
        viewGeneralization = new ViewHierarchy(group, attribute, controller);

        // Combo for transformation mode
        cmbMode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (model == null || model.getInputConfig() == null) {
                    return;
                }
                if (cmbMode.getSelectionIndex() == 0) {
                    model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.GENERALIZATION);
                    cmbFunction.setEnabled(false);
                    btnMissing.setEnabled(false);
                } else {
                    model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.MICRO_AGGREGATION);
                    cmbFunction.setEnabled(true);
                    btnMissing.setEnabled(true);
                }
            }
        });

        // Button for missing data
        btnMissing.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (model == null || model.getInputConfig() == null) {
                    return;
                }
                model.getInputConfig().setMicroAggregationIgnoreMissingData(attribute, btnMissing.getSelection());
            }
        });
        
        // Attach to tab
        tabItem.setControl(group);
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
    
    @Override
    public void dispose() {
        
        // Dispose views
        controller.removeListener(this);
        viewGeneralization.dispose();
        
        // Dispose images
        IMAGE_INSENSITIVE.dispose();
        IMAGE_SENSITIVE.dispose();
        IMAGE_QUASI_IDENTIFYING.dispose();
        IMAGE_IDENTIFYING.dispose();
    }
    
    @Override
    public void reset() {
        txtDataType.setText(""); //$NON-NLS-1$
    }
    
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            updateAttributeType();
            updateDataType();
            updateFunction();
            updateMode();
            viewGeneralization.update(event);
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateAttributeType();
                updateDataType();
                updateIcon();
            }
        } else if (event.part == ModelPart.INPUT) {
            updateAttributeType();
            updateDataType();
            updateMode();
            viewGeneralization.update(event);
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateFunction();
            updateMode();
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
     * Update mode
     */
    private void updateMode() {
        if (model != null && model.getInputConfig() != null) {
            if (model.getInputConfig().getTransformationMode(attribute) == ModelTransformationMode.GENERALIZATION) {
                cmbMode.select(0);
                cmbFunction.setEnabled(false);
                btnMissing.setEnabled(false);
            } else {
                cmbMode.select(1);
                cmbFunction.setEnabled(true);
                btnMissing.setEnabled(true);
            }
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
        if (type instanceof Hierarchy) {
            type = null;
        }
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
     * Update the column icon.
     */
    private void updateIcon() {
        AttributeType type = model.getInputDefinition().getAttributeType(attribute);
        if (type instanceof Hierarchy) {
            tabItem.setImage(IMAGE_QUASI_IDENTIFYING);
        } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            tabItem.setImage(IMAGE_INSENSITIVE);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            tabItem.setImage(IMAGE_SENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            tabItem.setImage(IMAGE_IDENTIFYING);
        }
    }
}
