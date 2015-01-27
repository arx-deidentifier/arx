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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * A page for configuring the order-based builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPageOrder<T> extends HierarchyWizardPageBuilder<T> {

    /** Var. */
    private final HierarchyWizardModelOrder<T> model;
    
    /** Var. */
    private final Controller                   controller;
    
    /** Var. */
    private ComponentTable                     table;
    
    /** Var. */
    private Combo                              combo;
    
    /** Var. */
    private HierarchyWizardEditor<T>           editor; 
    
    /**
     * Creates a new instance.
     *
     * @param controller
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPageOrder(final Controller controller,
                                    final HierarchyWizard<T> wizard,
                                    final HierarchyWizardModel<T> model,
                                    final HierarchyWizardPageFinal<T> finalPage) {
        super(wizard, model.getOrderModel(), finalPage);
        this.model = model.getOrderModel();
        this.controller = controller;
        setTitle("Create a hierarchy by ordering and grouping items");
        setDescription("Specify the parameters");
        setPageComplete(true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, false));
        createOrder(composite);
        createGroups(composite);
        setControl(composite);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardPageBuilder#updatePage()
     */
    @Override
    public void updatePage() {
        table.setData(getDataProvider(model.getData()), new String[]{"Values"});
        combo.select(getIndexOfDataType(model.getDataType()));
        if (editor != null) editor.setFunction(model.getDefaultFunction());
        model.update();
    }
    
    /**
     * Moves the selected item down.
     */
    private void actionDown() {
        Integer index = table.getSelectedRow();
        if (index == null) return;
        if (model.moveDown(index)) {
            table.refresh();
            table.setSelection(index+1, 0);
            update();
        }
    }
    
    /**
     * Sorts according to the index of a data type.
     *
     * @param index
     * @return
     */
    private int actionSort(int index) {
        
        // Initial data type
        DataType<?> type = model.getDataType();
        int returnIndex = index;
 
        // If not default
        if (index>0) {
            
            // Extract chosen type
            String label = combo.getItem(combo.getSelectionIndex());
            DataTypeDescription<?> description = getDataType(label);
    
            // Open format dialog
            if (description.getLabel().equals("OrderedString")) {
                final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                String[] array = controller.actionShowOrderValuesDialog(getShell(), 
                                                                        text1, text2,
                                                                        DataType.STRING,
                                                                        model.getLocale(),
                                                                        model.getData());
                if (array == null) {
                    type = DataType.STRING;
                } else {
                    try {
                        type = DataType.createOrderedString(array);
                        if (!isValid(type, model.getData())) {
                            type = DataType.STRING;
                        }
                    } catch (Exception e) {
                        controller.actionShowInfoDialog(getShell(), 
                                                        "Error", "Cannot create data type: " + e.getMessage());
                        type = DataType.STRING;
                    }
                }
            } else if (description.hasFormat()) {
                final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                final String format = controller.actionShowFormatInputDialog(getShell(),
                                                                             text1,
                                                                             text2,
                                                                             model.getLocale(),
                                                                             description,
                                                                             model.getData());
                if (format == null) {
                    type = DataType.STRING;
                } else {
                    type = description.newInstance(format, model.getLocale());
                }
            } else {
                type = description.newInstance();
                if (!isValid(type, model.getData())) {
                    type = DataType.STRING;
                }
            }
            returnIndex = getIndexOfDataType(type) + 1;
        }
        if (!model.sort(type)) {
            model.sort(DataType.STRING);
        }
        table.refresh();
        return returnIndex;
    }
    
    /**
     * Moves the selected item up.
     */
    private void actionUp() {
        Integer index = table.getSelectedRow();
        if (index == null) return;
        if (model.moveUp(index)) {
            table.refresh();
            table.setSelection(index-1, 0);
            update();
        }
    }

    /**
     * Create the grouping-part of the page.
     *
     * @param parent
     */
    private void createGroups(Composite parent){
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Groups");
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        composite.setLayoutData(SWTUtil.createFillGridData());
        editor =  new HierarchyWizardEditor<T>(composite, (HierarchyWizardModelGrouping<T>) model);
        editor.setLayoutData(SWTUtil.createFillGridData());
    }

    /**
     * Create the ordering-part of the page.
     *
     * @param parent
     */
    private void createOrder(Composite parent){
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Order");
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        composite.setLayoutData(SWTUtil.createFillVerticallyGridData());
        
        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_TABLE);
        config.setHorizontalAlignment(SWT.CENTER);
        config.setCellSelectionEnabled(false);
        config.setColumnSelectionEnabled(false);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_FILL_EQUAL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_FILL);

        // Create table
        this.table = new ComponentTable(composite, SWT.BORDER, config);
        this.table.getControl().setLayoutData(SWTUtil.createFillGridData());
        
        final Button up = new Button(composite, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyWizardPageOrder.3")); //$NON-NLS-1$
        up.setImage(controller.getResources().getImage("arrow_up.png")); //$NON-NLS-1$
        up.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionUp();
            }
        });

        final Button down = new Button(composite, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyWizardPageOrder.5")); //$NON-NLS-1$
        down.setImage(controller.getResources().getImage("arrow_down.png")); //$NON-NLS-1$
        down.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionDown();
            }
        });

        final Composite bottom1 = new Composite(composite, SWT.NONE);
        bottom1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        bottom1.setLayout(SWTUtil.createGridLayout(2, false));

        final Label text = new Label(bottom1, SWT.NONE);
        text.setText(Resources.getMessage("HierarchyWizardPageOrder.7")); //$NON-NLS-1$

        combo = new Combo(bottom1, SWT.NONE);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.8")); //$NON-NLS-1$
        for (String type : getDataTypes()){
            combo.add(type);
        }
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = combo.getSelectionIndex();
                if (index >=0 ){
                    combo.select(actionSort(index));
                }
            }
        });
        updatePage();
    }

    /**
     * Returns a data provider for the given array.
     *
     * @param array
     * @return
     */
    private IDataProvider getDataProvider(final String[] array){
        return new IDataProvider(){
            @Override
            public int getColumnCount() {
                return 1;
            }
            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return array[rowIndex];
            }
            @Override
            public int getRowCount() {
                return array.length;
            }
            @Override
            public void setDataValue(int columnIndex,
                                     int rowIndex,
                                     Object newValue) {
                /* Ignore*/
            }
        };
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
        ArrayList<String> list = new ArrayList<String>();
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
        return -1;
    }
    
    /**
     * Checks whether the data type is valid.
     *
     * @param type
     * @param values
     * @return
     */
    private boolean isValid(DataType<?> type, String[] values){
        for (String value : values){
            if (!type.isValid(value)) {
                return false;
            }
        }
        return true;
    }
}
