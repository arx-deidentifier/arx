/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.menu;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * This class implements a page in the wizard that allows ordering data items
 * @author Fabian Prasser
 */
public class WizardHierarchyPageOrder extends WizardPage {

    private final Controller           controller;
    private List                       list;
    private final WizardHierarchyModel model;

    /**
     * Constructor
     * @param controller
     * @param model
     */
    public WizardHierarchyPageOrder(final Controller controller,
                                    final WizardHierarchyModel model) {
        super(""); //$NON-NLS-1$
        this.model = model;
        this.controller = controller;
        setTitle(Resources.getMessage("HierarchyWizardPageOrder.1")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageOrder.2")); //$NON-NLS-1$
        setPageComplete(true);
    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }

    @Override
    public void createControl(final Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = 1;
        composite.setLayout(compositeLayout);
        list = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        // Limit to 10 entries
        final int itemHeight = list.getItemHeight();
        final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 10 * itemHeight;
        list.setLayoutData(data);

        final Composite bottom2 = new Composite(composite, SWT.NONE);
        bottom2.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        final GridLayout bottomLayout2 = new GridLayout();
        bottomLayout2.numColumns = 2;
        bottom2.setLayout(bottomLayout2);

        final Button up = new Button(bottom2, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyWizardPageOrder.3")); //$NON-NLS-1$
        up.setImage(controller.getResources().getImage("arrow_up.png")); //$NON-NLS-1$
        up.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                up();
            }
        });

        final Button down = new Button(bottom2, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyWizardPageOrder.5")); //$NON-NLS-1$
        down.setImage(controller.getResources().getImage("arrow_down.png")); //$NON-NLS-1$
        down.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                down();
            }
        });

        final Composite bottom1 = new Composite(composite, SWT.NONE);
        bottom1.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        final GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 2;
        bottom1.setLayout(bottomLayout);

        final Label text = new Label(bottom1, SWT.NONE);
        text.setText(Resources.getMessage("HierarchyWizardPageOrder.7")); //$NON-NLS-1$

        final Combo combo = new Combo(bottom1, SWT.NONE);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.8")); //$NON-NLS-1$
        for (String type : getDataTypes()){
            combo.add(type);
        }
        combo.select(0);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() >=0 ){
                    DataType<?> type = model.getDataType();
                    if (combo.getSelectionIndex() > 0) {
                        String label = combo.getItem(combo.getSelectionIndex());
                        DataTypeDescription<?> description = getDataType(label);
    
                        // Open format dialog
                        if (description.hasFormat()) {
                            final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                            final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                            final String format = controller.actionShowFormatInputDialog(text1, text2, description, model.getItems());
                            if (format == null) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING)+1);
                            } else {
                                type = description.newInstance(format);
                            }
                        } else {
                            type = description.newInstance();
                            if (!isValidDataType(type, model.getItems())) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING)+1);                        
                            }
                        }
                    }
                    try {
                        sort(type);
                    } catch (Exception e){
                        // TODO: This is an ugly fix for cases in which the data type is not correct,
                        // TODO: e.g., when specifying "numeric" for "strings" which will result in
                        // TODO: a NumberFormatException
                        sort(DataType.STRING);
                    }
                }
            }
        });
        sort(model.getDataType());
        setControl(composite);
    }
    
    @Override
    public boolean isPageComplete() {
        return true;
    }
    
    /**
     * Move an item down
     */
    private void down() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index < (list.getItemCount() - 1))) {

            // TODO: Ugly!
            final String t = model.getItems().get(index + 1);
            model.getItems().set(index + 1, model.getItems().get(index));
            model.getItems().set(index, t);
            list.setItems(model.getItems().toArray(new String[] {}));
            list.setSelection(index + 1);
        }
    }

    /**
     * Returns a description for the given label
     * @param label
     * @return
     */
    private DataTypeDescription<?> getDataType(String label){
        for (DataTypeDescription<?> desc : DataType.LIST){
            if (label.equals(desc.getLabel())){
                return desc;
            }
        }
        throw new RuntimeException("Unknown data type: "+label);
    }
    
    /**
     * Returns the labels of all available data types
     * @return
     */
    private String[] getDataTypes(){
        ArrayList<String> list = new ArrayList<String>();
        for (DataTypeDescription<?> desc : DataType.LIST){
            list.add(desc.getLabel());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns the index of a given data type
     * @param type
     * @return
     */
    private int getIndexOfDataType(DataType<?> type){
        int idx = 0;
        for (DataTypeDescription<?> desc : DataType.LIST){
            if (desc.getLabel().equals(type.getDescription().getLabel())) {
                return idx;
            }
            idx++;
        }
        throw new RuntimeException("Unknown data type: "+type.getDescription().getLabel());
    }

    /**
     * Checks whether the data type is valid
     * @param type
     * @param values
     * @return
     */
    private boolean isValidDataType(DataType<?> type, Collection<String> values){
        // TODO: Ugly
        try {
            for (String value : values){
                type.parse(value);
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * Perform sorting
     * @param type
     */
    private void sort(final DataType<?> type) {
        list.removeAll();
        Collections.sort(model.getItems(), new Comparator<String>() {
            @Override public int compare(final String arg0, final String arg1) {
                try {
                    return type.compare(arg0, arg1);
                } catch (final ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        for (final String s : model.getItems()) {
            list.add(s);
        }
    }

    /**
     * Move an item up
     */
    private void up() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index > 0)) {

            // TODO: Ugly!
            final String t = model.getItems().get(index - 1);
            model.getItems().set(index - 1, model.getItems().get(index));
            model.getItems().set(index, t);
            list.setItems(model.getItems().toArray(new String[] {}));
            list.setSelection(index - 1);
        }
    }
}
