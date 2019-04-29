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

package org.deidentifier.arx.gui.view.impl.define;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This view lists all attributes and their metadata
 * 
 * @author Fabian Prasser
 * @author Martin Waltl
 * @author Johanna Eicher
 */
public class ViewAttributeList implements IView {

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class AttributesPageLoader implements IPageLoader<PageResult<String>> {

        @Override
        public PageResult<String> loadPage(PageableController controller) {
            if (attributes == null || attributes.isEmpty()) {
                return PageListHelper.createPage(new ArrayList<String>(), controller);
            } else {
                return PageListHelper.createPage(attributes, controller);
            }
        }
    }

    /** Resource */
    private final Image      IMAGE_ENABLED;

    /** Resource */
    private final Image      IMAGE_DISABLED;

    /** Controller */
    private final Controller controller;
    /** Model */
    private Model            model;
    /** Model */
    private String[]         dataTypes;

    /** Model */
    private List<String>     attributes = new ArrayList<>();

    /** View */
    private PageableTable    table;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewAttributeList(final Composite parent, final Controller controller) {
        
        // Load images
        IMAGE_ENABLED           = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        IMAGE_DISABLED          = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$

        // Controller
        this.controller = controller;
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
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
        this.attributes = null;
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        SWTUtil.disable(this.table);
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
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE) {
            if (!attributes.isEmpty()) {
                table.refreshPage();
            }
        } else if (event.part == ModelPart.DATA_TYPE) {
            if (!attributes.isEmpty()) {
                table.refreshPage();
            }
        }
    }

    /**
     * Data type changed
     */
    private void actionDataTypeChanged(String label) {

        if (label != null && model != null && model.getInputConfig().getInput() != null) {

            // Prepare
            String attribute = model.getSelectedAttribute();
            DataTypeDescription<?> description = getDataTypeDescription(label);
            DataType<?> type = model.getInputDefinition().getDataType(attribute);
            boolean changed = false;

            try {
                // Open format dialog
                if (description.getLabel().equals("Ordinal")) { //$NON-NLS-1$
                    final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                    final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                    String[] array = controller.actionShowOrderValuesDialog(controller.getResources().getShell(),
                                                                            text1,
                                                                            text2,
                                                                            type,
                                                                            model.getLocale(),
                                                                            getValuesAsArray(attribute));

                    // Only update the data type of the attribute if an order has been determined
                    if (array != null) {

                        // Only update the data type of the attribute if the selected type is valid
                        DataType<?> tempType = DataType.createOrderedString(array);
                        if (isValidDataType(tempType, getValuesAsList(attribute))) {
                            type = tempType;
                            changed = true;
                        }

                    }
                } else if (description.hasFormat()) {
                    final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                    final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                    final String[] format = controller.actionShowFormatInputDialog(controller.getResources().getShell(),
                                                                                 text1,
                                                                                 text2,
                                                                                 model.getLocale(),
                                                                                 description,
                                                                                 getValuesAsList(attribute));
                    
                    // Only update the data type of the attribute if a format has been selected
                    if (format != null && format[0] != null) {
                        // The format input already performs a validity check,
                        // hence the returned format is valid
                        type = description.newInstance(format[0], format[1] != null ? getLocale(format[1]) : model.getLocale());
                        changed = true;
                    }
                } else {
                    
                    // Only update the data type of the attribute if the selected type is valid
                    DataType<?> typeTemp = description.newInstance();
                    if (isValidDataType(typeTemp, getValuesAsList(attribute))) {
                        type = typeTemp;
                        changed = true;
                    }
                }
                
            // Handle unexpected errors
            } catch (Exception e) {
                
                controller.actionShowInfoDialog(controller.getResources().getShell(),
                                                Resources.getMessage("ViewAttributeDefinition.1"), Resources.getMessage("ViewAttributeDefinition.2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                type = DataType.STRING;
                changed = true;
            }

            // Set and update
            if (changed) {
                this.model.getInputDefinition().setDataType(attribute, type);
                table.refreshPage();
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
        this.table = SWTUtil.createPageableTableViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION, true, true);
        this.table.setLayoutData(SWTUtil.createFillGridData());
        this.table.getViewer().setContentProvider(new ArrayContentProvider());
        this.table.setPageLoader(new AttributesPageLoader());

        // Table
        Table tTable = this.table.getViewer().getTable();
        tTable.setHeaderVisible(true);
        tTable.setLinesVisible(true);
        GridData gd = SWTUtil.createFillGridData();
        gd.heightHint = 100;
        tTable.setLayoutData(gd);
        SWTUtil.createGenericTooltip(tTable);
        
        // Column: name and icon
        this.createColumn(table, Resources.getMessage("ViewAttributeList.0"), //$NON-NLS-1$
                          30, new ColumnLabelProvider() {
                            @Override
                            public Image getImage(Object element) {
                                if (model == null) {
                                    return null;
                                }
                                String attribute = (String)element;
                                AttributeType type = model.getInputDefinition().getAttributeType(attribute);
                                boolean isResponseVariable = model.getInputDefinition().isResponseVariable(attribute);
                                return controller.getResources().getImage(type, isResponseVariable);
                            }

                            @Override
                            public String getText(Object element) {
                                return (String)element;
                            }
        });

        // Column: data type
        this.createColumn(table, Resources.getMessage("ViewAttributeList.1"), //$NON-NLS-1$
                          30, new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getDataType((String)element);
            }
        });

        // Column: format
        this.createColumn(table, Resources.getMessage("ViewAttributeList.2"), //$NON-NLS-1$
                          30, new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getDataTypeFormat((String)element);
            }
        });

        // Column: target
        this.createColumn(table, Resources.getMessage("ViewAttributeList.3"), //$NON-NLS-1$
                          30, new ColumnLabelProvider() {
                            @Override
                            public Image getImage(Object element) {
                                if (model == null) {
                                    return null;
                                }
                                String attribute = (String)element;
                                boolean isResponseVariable = model.getInputDefinition().isResponseVariable(attribute);
                                return isResponseVariable ? IMAGE_ENABLED : IMAGE_DISABLED;
                            }
                            @Override
                            public String getText(Object element) {
                                return "";
                            }
        });
        
        // Attribute selection update
        this.table.getViewer().getTable().addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
                    return;
                }
                int index = table.getViewer().getTable().getSelectionIndex();
                if (index >= 0) {
                    String attribute = (String)table.getViewer().getTable().getItem(index).getData();
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
        this.table.getViewer().getTable().addMouseListener(new MouseAdapter(){
            @Override public void mouseDown(MouseEvent e) {
                Point pt = new Point(e.x, e.y);
                int index = table.getViewer().getTable().getTopIndex();
                while (index < table.getViewer().getTable().getItemCount()) {
                    TableItem item = table.getViewer().getTable().getItem(index);
                    for (int i = 0; i < 5; i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            
                            // Data type or Format and right click
                            if ((i == 1 || i == 2) && e.button == 3) {
                                menu.setLocation(table.toDisplay(e.x, e.y));
                                menu.setVisible(true);
                                return;
                            }
                            
                            // Response variable and left click
                            else if (i == 3 && e.button == 1) {
                                String attribute = (String)item.getData();
                                boolean isResponseVariable = !model.getInputDefinition().isResponseVariable(attribute);
                                model.getInputDefinition().setResponseVariable(attribute, isResponseVariable);
                                table.refreshPage();
                                controller.update(new ModelEvent(this, ModelPart.RESPONSE_VARIABLES, attribute));
                                return;
                            }
                        }
                    }
                    index++;
                }
            }
        });
        this.table.setCurrentPage(0);
        this.table.refreshPage();
    }

    /**
     * Creates a new column
     * @param table
     * @param name
     * @param width
     * @param provider
     */
    private TableViewerColumn createColumn(PageableTable table,
                                           String name, 
                                           int width,
                                           ColumnLabelProvider provider) {
        
        TableViewerColumn column = new TableViewerColumn(table.getViewer(), SWT.NONE);
        column.setLabelProvider(provider);
        TableColumn tColumn = column.getColumn();
        tColumn.setToolTipText(name);
        tColumn.setText(name);
        tColumn.setWidth(width);
        tColumn.setResizable(true);
        return column;
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
            String locale = dtwf.getLocale() != null ? dtwf.getLocale().getLanguage() : null;
            String format = dtwf.getFormat();
            String result = "";
            if (format == null) {
                result = Resources.getMessage("ViewAttributeDefinition.7"); //$NON-NLS-1$
            } else {
                result = format;
            }
            if (locale == null) {
                return result;
            } else {
                return result +  " (" + locale.toUpperCase() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
     * Returns the local for the given isoLanguage
     * @param isoLanguage
     * @return
     */
    private Locale getLocale(String isoLanguage) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getLanguage().toUpperCase().equals(isoLanguage.toUpperCase())) {
                return locale;
            }
        }
        throw new IllegalStateException("Unknown locale");
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
     * Updates the view.
     * 
     * @param node
     */
    private void updateEntries() {

        // Check
        if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) { 
            return; 
        }

        // Update data
        this.attributes = new ArrayList<String>();
        DataHandle data = model.getInputConfig().getInput().getHandle();
        for (int i = 0; i < data.getNumColumns(); i++) {
            String attribute = data.getAttributeName(i);
            this.attributes.add(attribute);
        }
        
        // Refresh
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        SWTUtil.enable(this.table);
    }

    /**
     * Update
     * @param attribute
     */
    private void updateSelectedAttribute(String attribute) {
        if (model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null) {
            Table table = this.table.getViewer().getTable();
            for (int i=0; i < table.getItemCount(); i++) {
                TableItem item = table.getItem(i);
                if (item.getData().equals(attribute)) {
                    table.select(i);
                    break;
                }
            }
        }
    }
}
