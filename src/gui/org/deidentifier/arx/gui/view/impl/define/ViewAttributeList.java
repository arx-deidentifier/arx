/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view lists all attributes and their metadata
 * 
 * @author Fabian Prasser
 */
public class ViewAttributeList implements IView {

    /** Controller */
    private final Controller controller;

    /** Model */
    private Model            model;

    /** Model */
    private String[]         dataTypes;

    /** View */
    private DynamicTable     table;

    /** Resource */
    private final Image      IMAGE_IDENTIFYING;
    /** Resource */
    private final Image      IMAGE_INSENSITIVE;
    /** Resource */
    private final Image      IMAGE_QUASI_IDENTIFYING;
    /** Resource */
    private final Image      IMAGE_SENSITIVE;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewAttributeList(final Composite parent,
                             final Controller controller) {


        // Load images
        IMAGE_INSENSITIVE = controller.getResources().getManagedImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE = controller.getResources().getManagedImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources().getManagedImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING = controller.getResources().getManagedImage("bullet_red.png"); //$NON-NLS-1$
        
        
        // Controller
        this.controller = controller;
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.DATA_TYPE, this);
        this.dataTypes = getDataTypes();
        
        // Create group
        this.create(parent);
        this.reset();
    }
    
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        table.clearAll();
        SWTUtil.disable(table);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           updateEntries();
        } else if (event.part == ModelPart.INPUT) {
           updateEntries();
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            String attribute = (String) event.data;
            updateSelectedAttribute(attribute);
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            updateAttributeTypes();
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateDataTypes();
        }
    }

    /**
     * Data type changed
     */
    private void actionDataTypeChanged(String label) {
        if (label != null) {
            if ((model != null) && (model.getInputConfig().getInput() != null)) {
                
                // Obtain type
                String attribute = model.getSelectedAttribute();
                DataTypeDescription<?> description = getDataTypeDescription(label);
                DataType<?> type;
                
                // Open format dialog
                if (description.getLabel().equals("Ordinal")) { //$NON-NLS-1$
                    final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                    final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                    String[] array = controller.actionShowOrderValuesDialog(controller.getResources().getShell(),
                                                                            text1, text2, DataType.STRING,
                                                                            model.getLocale(), getValuesAsArray(attribute));
                    if (array == null) {
                        type = DataType.STRING;
                    } else {
                        try {
                            type = DataType.createOrderedString(array);
                            if (!isValidDataType(type, getValuesAsList(attribute))) {
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
                                                                                 text1, text2, model.getLocale(), description, getValuesAsList(attribute));
                    if (format == null) {
                        type = DataType.STRING;
                    } else {
                        type = description.newInstance(format, model.getLocale());
                    }
                } else {
                    type = description.newInstance();
                    if (!isValidDataType(type, getValuesAsList(attribute))) {
                        type = DataType.STRING;
                    }
                }
                
                // Set and update
                this.model.getInputDefinition().setDataType(attribute, type);
                this.updateDataTypes();
                this.controller.update(new ModelEvent(this, ModelPart.DATA_TYPE, attribute));
            }
        }
    }

    /**
     * Creates the required controls.
     * 
     * @param parent
     */
    private void create(final Composite parent) {
        this.table = SWTUtil.createTableDynamic(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setLayoutData(SWTUtil.createFillGridData());
        SWTUtil.createGenericTooltip(table);
        DynamicTableColumn column0 = new DynamicTableColumn(table, SWT.NONE);
        column0.setText(""); //$NON-NLS-1$
        column0.setWidth("4%", "25px"); //$NON-NLS-1$ //$NON-NLS-2$
        DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.NONE);
        column1.setText(Resources.getMessage("ViewAttributeList.0")); //$NON-NLS-1$
        column1.setWidth("32%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        DynamicTableColumn column2 = new DynamicTableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("ViewAttributeList.1")); //$NON-NLS-1$
        column2.setWidth("32%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        DynamicTableColumn column3 = new DynamicTableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("ViewAttributeList.2")); //$NON-NLS-1$
        column3.setWidth("32%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        column1.pack();
        column2.pack();
        column3.pack();
        
        this.table.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
                    return;
                }
                int index = table.getSelectionIndex();
                if (index >= 0 && index <= model.getInputConfig().getInput().getHandle().getNumColumns()) {
                    String attribute = model.getInputConfig().getInput().getHandle().getAttributeName(index);
                    model.setSelectedAttribute(attribute);
                    controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, attribute));
                }
            }
        });
        
        // Create menu
        final Menu menu = new Menu(this.table);
        for (final String type : getDataTypes()) {
            MenuItem item = new MenuItem(menu, SWT.NONE); 
            item.setText(type);
            item.addSelectionListener(new SelectionAdapter(){
                @Override public void widgetSelected(SelectionEvent arg0) {
                    actionDataTypeChanged(type);
                }
            });
        }
        
        // Trigger menu
        this.table.addMouseListener(new MouseAdapter(){
            @Override public void mouseDown(MouseEvent e) {
                if (e.button == 3) {
                    menu.setLocation(table.toDisplay(e.x, e.y));
                    menu.setVisible(true);
                }
            }
        });
    }

    /**
     * Returns the data type of the attribute
     * @param attribute
     * @return
     */
    private String getDataType(String attribute) {
        return dataTypes[getIndexOfDataType(model.getInputDefinition().getDataType(attribute))];
    }

    /**
     * Returns a description for the given label.
     *
     * @param label
     * @return
     */
    private DataTypeDescription<?> getDataTypeDescription(String label) {
        for (DataTypeDescription<?> desc : DataType.list()) {
            if (label.equals(desc.getLabel())) {
                return desc;
            }
        }
        throw new RuntimeException(Resources.getMessage("ViewAttributeDefinition.5") + label); //$NON-NLS-1$
    }

    /**
     * Returns the format of the attribute
     * @param attribute
     * @return
     */
    private String getDataTypeFormat(String attribute) {

        DataType<?> dtype = model.getInputDefinition().getDataType(attribute);
        if (!(dtype instanceof ARXOrderedString) && dtype.getDescription().hasFormat()) {
            DataTypeWithFormat dtwf = (DataTypeWithFormat) dtype;
            String format = dtwf.getFormat();
            if (format == null) {
                return Resources.getMessage("ViewAttributeDefinition.7"); //$NON-NLS-1$
            } else {
                return format;
            }
        } else {
            return Resources.getMessage("ViewAttributeDefinition.8"); //$NON-NLS-1$
        }
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
    private String[] getValuesAsArray(String attribute) {
        final DataHandle h = model.getInputConfig().getInput().getHandle();
        return h.getStatistics().getDistinctValues(h.getColumnIndexOf(attribute));
    }

    /**
     * Create a collection of the values in the column for this attribute.
     *
     * @return
     */
    private Collection<String> getValuesAsList(String attribute) {
        return Arrays.asList(getValuesAsArray(attribute));
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
     * Update
     */
    private void updateAttributeTypes() {
        if (model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null) {
            table.setRedraw(false);
            DataHandle data = model.getInputConfig().getInput().getHandle();
            for (int i = 0; i < data.getNumColumns(); i++) {
                String attribute = data.getAttributeName(i);
                AttributeType type = model.getInputDefinition().getAttributeType(attribute);
                if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
                    table.getItem(i).setImage(0, IMAGE_IDENTIFYING);  
                } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
                    table.getItem(i).setImage(0, IMAGE_SENSITIVE);
                } else if (type == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
                    table.getItem(i).setImage(0, IMAGE_QUASI_IDENTIFYING);
                } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
                    table.getItem(i).setImage(0, IMAGE_INSENSITIVE);
                }
            }
            table.setRedraw(true);
            SWTUtil.enable(table);
        }
    }

    /**
     * Update
     */
    private void updateDataTypes() {
        if (model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null) {
            table.setRedraw(false);
            DataHandle data = model.getInputConfig().getInput().getHandle();
            for (int i = 0; i < data.getNumColumns(); i++) {
                String attribute = data.getAttributeName(i);
                table.getItem(i).setText(2, getDataType(attribute));
                table.getItem(i).setText(3, getDataTypeFormat(attribute));
            }
            table.setRedraw(true);
            SWTUtil.enable(table);
        }
    }
    
    /**
     * Updates the view.
     * 
     * @param node
     */
    private void updateEntries() {

        // Check
        if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) { 
            return; 
        }

        table.setRedraw(false);
        table.removeAll();
        DataHandle data = model.getInputConfig().getInput().getHandle();
        for (int i = 0; i < data.getNumColumns(); i++) {
            String attribute = data.getAttributeName(i);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", attribute, getDataType(attribute), getDataTypeFormat(attribute) }); //$NON-NLS-1$
            AttributeType type = model.getInputDefinition().getAttributeType(attribute);
            if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
                item.setImage(0, IMAGE_IDENTIFYING);  
            } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
                item.setImage(0, IMAGE_SENSITIVE);
            } else if (type == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
                item.setImage(0, IMAGE_QUASI_IDENTIFYING);
            } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
                item.setImage(0, IMAGE_INSENSITIVE);
            }
            if (model.getSelectedAttribute() != null && model.getSelectedAttribute().equals(attribute)) {
                table.select(i);
            }
        }
        
        table.setRedraw(true);
        SWTUtil.enable(table);
    }

    /**
     * Update
     * @param attribute
     */
    private void updateSelectedAttribute(String attribute) {
        if (model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null) {
            DataHandle data = model.getInputConfig().getInput().getHandle();
            for (int i = 0; i < data.getNumColumns(); i++) {
                if (data.getAttributeName(i).equals(attribute)) {
                    table.select(i);
                    break;
                }
            }   
        }
    }
    

}
