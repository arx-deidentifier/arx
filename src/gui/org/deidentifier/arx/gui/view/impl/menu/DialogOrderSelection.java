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
import java.util.Locale;

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
 * An dialog that allows ordering data items.
 *
 * @author Fabian Prasser
 */
public class DialogOrderSelection extends TitleAreaDialog implements IDialog {

    /** A list control. */
    private List        list;
    
    /** Logo. */
    private Image       image;
    
    /** Controller. */
    private Controller  controller;
    
    /** Elements. */
    private String[]    elements;
    
    /** Type. */
    private DataType<?> type;
    
    /** Combo control. */
    private Combo       combo;
    
    /** Locale. */
    private Locale      locale;

    /**
     * Creates a new instance.
     *
     * @param parentShell
     * @param elements
     * @param type
     * @param locale
     * @param controller
     */
    public DialogOrderSelection(final Shell parentShell,
                                final String[] elements,
                                final DataType<?> type,
                                final Locale locale,
                                final Controller controller) {
        super(parentShell);
        this.controller = controller;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
        this.elements = elements.clone();
        this.type = type;
        this.locale = locale;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        if (image != null)
            image.dispose();
        return super.close();
    }

    /**
     * Returns the result.
     *
     * @return
     */
    public String[] getResult() {
        return elements;
    }

    /**
     * Move an item down.
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
            if (!desc.getLabel().equals("OrderedString")) {
                list.add(desc.getLabel());
            }
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
     * Checks whether the data type is valid.
     *
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
     * Loads the array from a file.
     *
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
            controller.actionShowInfoDialog(getShell(), "Error", "Error while loading values: "+e.getMessage());
            return null;
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                controller.actionShowInfoDialog(getShell(), "Error", "Error while loading values: "+e.getMessage());
                return null;
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Saves the array to file.
     *
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
            controller.actionShowInfoDialog(getShell(), "Error", "Error while saving values: "+e.getMessage());
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException e) {
                controller.actionShowInfoDialog(getShell(), "Error", "Error while saving values: "+e.getMessage());
            }
        }
    }

    /**
     * Perform sorting.
     *
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
     * Move an item up.
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());
        
        final Button loadButton = createButton(parent,
                                             Integer.MAX_VALUE-1,
                                             "Load", false); //$NON-NLS-1$
        loadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                String file = controller.actionShowOpenFileDialog(getShell(), "*.csv");
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
                String file = controller.actionShowSaveFileDialog(getShell(), "*.csv");
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle("Specify an order"); //$NON-NLS-1$
        setMessage("Please order the list of data items"); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
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
                            final String format = controller.actionShowFormatInputDialog(getShell(), text1, text2, locale, description, elements);
                            if (format == null) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING)+1);
                            } else {
                                type = description.newInstance(format, locale);
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return false;
    }
}
