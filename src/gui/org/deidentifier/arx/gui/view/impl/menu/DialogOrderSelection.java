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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * An dialog that allows ordering data items
 * @author Fabian Prasser
 */
public class DialogOrderSelection extends TitleAreaDialog implements IDialog {

    /** A list control*/
    private List        list;
    /** Logo*/
    private Image       image;
    /** Controller*/
    private Controller  controller;
    /** Elements*/
    private String[]    elements;
    /** Type*/
    private DataType<?> type;
    /** Combo control*/
    private Combo       combo;

    /**
     * Creates a new instance
     * @param parentShell
     * @param elements
     * @param type
     * @param controller
     */
    public DialogOrderSelection(final Shell parentShell,
                                final String[] elements,
                                final DataType<?> type,
                                final Controller controller) {
        super(parentShell);
        this.controller = controller;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
        this.elements = elements.clone();
        this.type = type;
    }

    @Override
    public boolean close() {
        if (image != null)
            image.dispose();
        return super.close();
    }

    /**
     * Returns the result
     * @return
     */
    public String[] getResult() {
        return elements;
    }

    /**
     * Move an item down
     */
    private void down() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index < (list.getItemCount() - 1))) {
            final String temp = elements[index + 1];
            elements[index + 1] = elements[index];
            elements[index] = temp;
            list.setItems(elements);
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
            if (!desc.getLabel().equals("OrderedString")) {
                list.add(desc.getLabel());
            }
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
    private boolean isValidDataType(DataType<?> type, String[] values) {
        for (String value : values) {
            if (!type.isValid(value)) { return false; }
        }
        return true;
    }
    
    /**
     * Loads the array from a file
     * @param file
     * @return
     */
    private String[] loadArray(String file) {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(file)));
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            controller.actionShowErrorDialog("Error", "Input/output error while saving values", e);
            return null;
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                controller.actionShowErrorDialog("Error", "Input/output error while saving values", e);
                return null;
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Saves the array to file
     * @param file
     * @param elements
     */
    private void saveArray(String file, String[] elements) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(file)));
            for (int i=0; i<elements.length; i++) {
                writer.write(elements[i]);
                if (i<elements.length-1) writer.write("\n");
            }
        } catch (IOException e) {
            controller.actionShowErrorDialog("Error", "Input/output error while saving values", e);
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException e) {
                controller.actionShowErrorDialog("Error", "Input/output error while saving values", e);
            }
        }
    }

    /**
     * Perform sorting
     * @param type
     */
    private void sort(final DataType<?> type) {
        list.removeAll();
        Arrays.sort(elements, new Comparator<String>() {
            @Override public int compare(final String arg0, final String arg1) {
                try {
                    return type.compare(arg0, arg1);
                } catch (final ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        for (final String s : elements) {
            list.add(s);
        }
    }

    /**
     * Move an item up
     */
    private void up() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index > 0)) {
            final String temp = elements[index - 1];
            elements[index - 1] = elements[index];
            elements[index] = temp;
            list.setItems(elements);
            list.setSelection(index - 1);
        }
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());
        
        final Button loadButton = createButton(parent,
                                             Integer.MAX_VALUE-1,
                                             "Load", false); //$NON-NLS-1$
        loadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                String file = controller.actionShowOpenFileDialog("*.csv");
                if (file != null){
                    String[] array = loadArray(file);
                    if (array != null) {
                        
                        // Select string
                        for (int i=0; i<combo.getItems().length; i++){
                            if (combo.getItem(i).equals("String")) {
                                combo.select(i);
                            }
                        }
                        
                        // Set items
                        elements = array;
                        list.setItems(array);
                    }
                }
            }
        });

        final Button saveButton = createButton(parent,
                                             Integer.MAX_VALUE-2,
                                             "Save", false); //$NON-NLS-1$
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                String file = controller.actionShowSaveFileDialog("*.csv");
                if (file != null) {
                    saveArray(file, elements);
                }
            }
        });
        
        // Create OK Button
        final Button okButton = createButton(parent,
                                             Window.OK,
                                             "OK", true); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });

        final Button cancelButton = createButton(parent,
                                                 Window.CANCEL,
                                                 "Cancel", false); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }

    @Override
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle("Specify an order"); //$NON-NLS-1$
        setMessage("Please order the list of data items"); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {

        final GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = 1;
        parent.setLayout(compositeLayout);
        list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        // Limit to 10 entries
        final int itemHeight = list.getItemHeight();
        final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 10 * itemHeight;
        list.setLayoutData(data);

        final Composite bottom2 = new Composite(parent, SWT.NONE);
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

        final Composite bottom1 = new Composite(parent, SWT.NONE);
        bottom1.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        final GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 2;
        bottom1.setLayout(bottomLayout);

        final Label text = new Label(bottom1, SWT.NONE);
        text.setText(Resources.getMessage("HierarchyWizardPageOrder.7")); //$NON-NLS-1$

        combo = new Combo(bottom1, SWT.NONE);
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
                    DataType<?> type = DialogOrderSelection.this.type;
                    if (combo.getSelectionIndex() > 0) {
                        String label = combo.getItem(combo.getSelectionIndex());
                        DataTypeDescription<?> description = getDataType(label);
    
                        // Open format dialog
                        if (description.hasFormat()) {
                            final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                            final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                            final String format = controller.actionShowFormatInputDialog(text1, text2, description, elements);
                            if (format == null) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING)+1);
                            } else {
                                type = description.newInstance(format);
                            }
                        } else {
                            type = description.newInstance();
                            if (!isValidDataType(type, elements)) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING)+1);                        
                            }
                        }
                    }
                    try {
                        sort(type);
                    } catch (Exception e){
                        sort(DataType.STRING);
                    }
                }
            }
        });
        sort(this.type);
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return false;
    }
}
