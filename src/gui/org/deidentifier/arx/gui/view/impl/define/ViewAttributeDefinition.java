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
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
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
import org.eclipse.swt.layout.GridLayout;
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

    /**  TODO */
    private static final AttributeType[] COMBO1_TYPES  = new AttributeType[] { 
                                        AttributeType.INSENSITIVE_ATTRIBUTE,
                                        AttributeType.SENSITIVE_ATTRIBUTE,
                                        null,
                                        AttributeType.IDENTIFYING_ATTRIBUTE };
    
    /**  TODO */
    private static final String[]       COMBO1_VALUES = new String[] { 
                                        Resources.getMessage("AttributeDefinitionView.0"), //$NON-NLS-1$
                                        Resources.getMessage("AttributeDefinitionView.1"), //$NON-NLS-1$
                                        Resources.getMessage("AttributeDefinitionView.2"), //$NON-NLS-1$
                                        Resources.getMessage("AttributeDefinitionView.3") }; //$NON-NLS-1$

    /**  TODO */
    private String                       attribute     = null;
    
    /**  TODO */
    private Model                        model;

    /**  TODO */
    private final Controller             controller;
    
    /**  TODO */
    private final Combo                  dataTypeCombo;
    
    /**  TODO */
    private final Text                   dataTypeText;
    
    /**  TODO */
    private final ViewHierarchy          editor;
    
    /**  TODO */
    private final Image                  IMAGE_IDENTIFYING;
    
    /**  TODO */
    private final Image                  IMAGE_INSENSITIVE;
    
    /**  TODO */
    private final Image                  IMAGE_QUASI_IDENTIFYING;
    
    /**  TODO */
    private final Image                  IMAGE_SENSITIVE;
    
    /**  TODO */
    private final CTabItem               tab;
    
    /**  TODO */
    private final Combo                  typeCombo;

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

        // Create input group
        tab = new CTabItem(parent, SWT.NULL);
        tab.setText(attribute);
        tab.setShowClose(false);
        tab.setImage(IMAGE_INSENSITIVE);

        Composite group = new Composite(parent, SWT.NULL);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 1;
        group.setLayout(groupInputGridLayout);

        final Composite type = new Composite(group, SWT.NULL);
        type.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout typeInputGridLayout = new GridLayout();
        typeInputGridLayout.numColumns = 6;
        type.setLayout(typeInputGridLayout);

        final IView outer = this;
        final Label kLabel = new Label(type, SWT.PUSH);
        kLabel.setText(Resources.getMessage("AttributeDefinitionView.7")); //$NON-NLS-1$
        typeCombo = new Combo(type, SWT.READ_ONLY);
        typeCombo.setLayoutData(SWTUtil.createFillGridData());
        typeCombo.setItems(COMBO1_VALUES);
        typeCombo.select(0);
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                if ((typeCombo.getSelectionIndex() != -1) &&
                    (attribute != null)) {
                    if ((model != null) &&
                        (model.getInputConfig().getInput() != null)) {
                        final AttributeType type = COMBO1_TYPES[typeCombo.getSelectionIndex()];
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
                        if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
                        	model.getLDiversityModel().get(attribute).setActive(true);
                        	model.getTClosenessModel().get(attribute).setActive(true);
                        } else {
                            
                            if (model.getLDiversityModel().get(attribute).isEnabled() ||
                                model.getTClosenessModel().get(attribute).isEnabled()){
                                criteriaDisabled = true;
                            }
                            
                        	model.getLDiversityModel().get(attribute).setActive(false);
                        	model.getTClosenessModel().get(attribute).setActive(false);
                        	
                        	model.getTClosenessModel().get(attribute).setEnabled(false);
                        	model.getLDiversityModel().get(attribute).setEnabled(false);
                        }
                        
                        // Enable/disable criteria for quasi-identifiers
                        if (definition.getQuasiIdentifyingAttributes().isEmpty()){

                            if ( model.getKAnonymityModel().isEnabled() ||
                                 model.getDPresenceModel().isEnabled()){
                                criteriaDisabled = true;
                            }
                            
                            model.getKAnonymityModel().setActive(false);
                            model.getDPresenceModel().setActive(false);
                            model.getKAnonymityModel().setEnabled(false);
                            model.getDPresenceModel().setEnabled(false);

                        } else {
                            model.getKAnonymityModel().setActive(true);
                            model.getDPresenceModel().setActive(true);
                        }

                        // Update icon
                        updateIcon();
                        
                        // Update criteria
                        if (criteriaDisabled){
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

        final Label kLabel2 = new Label(type, SWT.PUSH);
        kLabel2.setText(Resources.getMessage("AttributeDefinitionView.8")); //$NON-NLS-1$
        dataTypeCombo = new Combo(type, SWT.READ_ONLY);
        dataTypeCombo.setLayoutData(SWTUtil.createFillGridData());
        dataTypeCombo.setItems(getDataTypes());
        dataTypeCombo.select(getIndexOfDataType(DataType.STRING));
        dataTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if ((dataTypeCombo.getSelectionIndex() != -1) &&
                    (attribute != null)) {
                    if ((model != null) &&
                        (model.getInputConfig().getInput() != null)) {

                        // Obtain type
                        String label = dataTypeCombo.getItem(dataTypeCombo.getSelectionIndex());
                        DataTypeDescription<?> description = getDataType(label);
                        DataType<?> type;

                        // Open format dialog
                        if (description.getLabel().equals("OrderedString")) {
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
                                } catch (Exception e){
                                    controller.actionShowInfoDialog(controller.getResources().getShell(),
                                                                    "Error", "Cannot create data type: "+e.getMessage());
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
                        updateDataType();
                        controller.update(new ModelEvent(outer, ModelPart.DATA_TYPE, attribute));
                    }
                }
            }
        });

        final Label kLabel3 = new Label(type, SWT.PUSH);
        kLabel3.setText(Resources.getMessage("AttributeDefinitionView.10")); //$NON-NLS-1$
        dataTypeText = new Text(type, SWT.READ_ONLY | SWT.BORDER);
        dataTypeText.setLayoutData(SWTUtil.createFillGridData());
        dataTypeText.setEditable(false);
        dataTypeText.setText("");

        // Editor hierarchy
        editor = new ViewHierarchy(group, attribute, controller);

        // Attach to tab
        tab.setControl(group);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        
        // Dispose views
        controller.removeListener(this);
        editor.dispose();

        // Dispose images
        IMAGE_INSENSITIVE.dispose();
        IMAGE_SENSITIVE.dispose();
        IMAGE_QUASI_IDENTIFYING.dispose();
        IMAGE_IDENTIFYING.dispose();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        dataTypeText.setText("");
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            editor.update(event);
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
            editor.update(event);
        }
    }
    
    /**
     * Returns a description for the given label.
     *
     * @param label
     * @return
     */
    private DataTypeDescription<?> getDataType(String label){
        for (DataTypeDescription<?> desc : DataType.list()){
            if (label.equals(desc.getLabel())){
                return desc;
            }
        }
        throw new RuntimeException("Unknown data type: "+label);
    }
    
    /**
     * Returns the labels of all available data types.
     *
     * @return
     */
    private String[] getDataTypes(){
        List<String> list = new ArrayList<String>();
        for (DataTypeDescription<?> desc : DataType.list()){
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
    private int getIndexOfDataType(DataType<?> type){
        int idx = 0;
        for (DataTypeDescription<?> desc : DataType.list()){
            if (desc.getLabel().equals(type.getDescription().getLabel())) {
                return idx;
            }
            idx++;
        }
        throw new RuntimeException("Unknown data type: "+type.getDescription().getLabel());
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
    private boolean isValidDataType(DataType<?> type, Collection<String> values){
        for (String value : values){
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
        AttributeType type = model.getInputDefinition()
                                  .getAttributeType(attribute);
        if (type instanceof Hierarchy) {
            type = null;
        }
        for (int i = 0; i < COMBO1_TYPES.length; i++) {
            if (type == COMBO1_TYPES[i]) {
                typeCombo.select(i);
                break;
            }
        }
    }

    /**
     * Update the data type.
     */
    private void updateDataType() {

        final DataType<?> dtype = model.getInputDefinition()
                                       .getDataType(attribute);
        
        dataTypeCombo.select(getIndexOfDataType(dtype));
        
        if (!(dtype instanceof ARXOrderedString) && 
            dtype.getDescription().hasFormat()) {
            
            DataTypeWithFormat dtwf = (DataTypeWithFormat)dtype;
            String format = dtwf.getFormat();
            if (format==null) {
                dataTypeText.setText("Default");
            } else {
                dataTypeText.setText(format);
            }
        } else {
            dataTypeText.setText("Default");
        }
    }

    /**
     * Update the column icon.
     */
    private void updateIcon() {
        AttributeType type = model.getInputDefinition()
                                  .getAttributeType(attribute);
        if (type instanceof Hierarchy) {
            tab.setImage(IMAGE_QUASI_IDENTIFYING);
        } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            tab.setImage(IMAGE_INSENSITIVE);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            tab.setImage(IMAGE_SENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            tab.setImage(IMAGE_IDENTIFYING);
        }
    }
}
