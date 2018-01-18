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

package org.deidentifier.arx.gui.view.impl.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;

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
import org.eclipse.swt.widgets.Shell;

/**
 * An dialog that allows ordering data items.
 *
 * @author Fabian Prasser
 */
public class DialogOrderSelection extends TitleAreaDialog implements IDialog {

    /** A list control. */
    private org.eclipse.swt.widgets.List        list;
    
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
        this.image = controller.getResources().getManagedImage("logo_small.png"); //$NON-NLS-1$
        this.elements = elements.clone();
        this.type = type;
        this.locale = locale;
    }

    @Override
    public boolean close() {
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
        throw new RuntimeException(Resources.getMessage("DialogOrderSelection.0")+label); //$NON-NLS-1$
    }
    
    /**
     * Returns the labels of all available data types.
     *
     * @return
     */
    private String[] getDataTypes(){
        ArrayList<String> list = new ArrayList<String>();
        for (DataTypeDescription<?> desc : DataType.list()){
            if (!desc.getLabel().equals("Ordinal")) { //$NON-NLS-1$
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
        throw new RuntimeException(Resources.getMessage("DialogOrderSelection.2")+type.getDescription().getLabel()); //$NON-NLS-1$
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
     * Loads the array from a file. If the file contains more or additional
     * values (lines) than present in the attribute's domain, the loading is
     * aborted and an <code>IllegalStateException</code> is thrown.
     *
     * @param file
     * @param charset
     *            TODO
     * @return
     * @throws IllegalStateException
     *             The file contains more or additional values (lines) than
     *             present in the attribute's domain
     */
    private List<String> loadFile(String file, Charset charset) {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            String line = reader.readLine();
            Set<String> _elements = new HashSet<String>();
            _elements.addAll(Arrays.asList(elements));

            while (line != null) {
                list.add(line);
                if (list.size() > _elements.size() || !_elements.contains(line)) {
                    // The file contains more or additional values (lines) than present in the attribute's domain
                	controller.actionShowInfoDialog(getShell(),
                            Resources.getMessage("DialogOrderSelection.16"),
                            Resources.getMessage("DialogOrderSelection.17"));
                	return null;
                }
                line = reader.readLine();
            }
            
            if (list.size() != _elements.size()) {
            	// The file contains less values (lines) than present in the attribute's domain
                controller.actionShowInfoDialog(getShell(),
                                                Resources.getMessage("DialogOrderSelection.16"),
                                                Resources.getMessage("DialogOrderSelection.17"));
                return null;
            }
        } catch (IOException e) {
            controller.actionShowInfoDialog(getShell(),
                                            Resources.getMessage("DialogOrderSelection.3"), //$NON-NLS-1$
                                            Resources.getMessage("DialogOrderSelection.4") + e.getMessage()); //$NON-NLS-1$
            return null;
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                controller.actionShowInfoDialog(getShell(),
                                                Resources.getMessage("DialogOrderSelection.5"), //$NON-NLS-1$
                                                Resources.getMessage("DialogOrderSelection.6") + e.getMessage()); //$NON-NLS-1$
                return null;
            }
        }
        return list;
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
                if (i<elements.length-1) writer.write("\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            controller.actionShowInfoDialog(getShell(), Resources.getMessage("DialogOrderSelection.8"), Resources.getMessage("DialogOrderSelection.9")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException e) {
                controller.actionShowInfoDialog(getShell(), Resources.getMessage("DialogOrderSelection.10"), Resources.getMessage("DialogOrderSelection.11")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());
        
        // Create IMPORT Button
        final Button importButton = createButton(parent,
                                             Integer.MAX_VALUE-1,
                                             Resources.getMessage("DialogOrderSelection.12"), false); //$NON-NLS-1$
        importButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                String file = controller.actionShowOpenFileDialog(getShell(), "*.csv"); //$NON-NLS-1$

				if (file != null) {
					List<String> fileData = loadFile(file, Charset.defaultCharset());
					if (fileData != null) {
						// Select "Custom"
						for (int i = 0; i < combo.getItems().length; i++) {
							if (combo.getItem(i).equals(Resources.getMessage("HierarchyWizardPageOrder.8"))) { //$NON-NLS-1$
								combo.select(i);
							}
						}

						// Set items
						elements = fileData.toArray(new String[fileData.size()]);
						list.setItems(elements);
					}
				}
            }
        });

        // Create EXPORT Button
        final Button exportButton = createButton(parent,
                                             Integer.MAX_VALUE-2,
                                             Resources.getMessage("DialogOrderSelection.13"), false); //$NON-NLS-1$
        exportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                String file = controller.actionShowSaveFileDialog(getShell(), "*.csv"); //$NON-NLS-1$
                if (file != null) {
                    saveArray(file, elements);
                }
            }
        });
        
        // Create OK Button
        final Button okButton = createButton(parent,
                                             Window.OK,
                                             Resources.getMessage("DialogOrderSelection.14"), true); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });

        // Create CANCEL Button
        final Button cancelButton = createButton(parent,
                                                 Window.CANCEL,
                                                 Resources.getMessage("DialogOrderSelection.15"), false); //$NON-NLS-1$
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
        list = new org.eclipse.swt.widgets.List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

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
        up.setImage(controller.getResources().getManagedImage("arrow_up.png")); //$NON-NLS-1$
        up.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                up();
            }
        });

        final Button down = new Button(bottom2, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyWizardPageOrder.5")); //$NON-NLS-1$
        down.setImage(controller.getResources().getManagedImage("arrow_down.png")); //$NON-NLS-1$
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
                            final String format[] = controller.actionShowFormatInputDialog(getShell(), text1, text2, locale, description, elements);
                            if (format == null || format[0] == null) {
                                type = DataType.STRING;
                                combo.select(getIndexOfDataType(DataType.STRING) + 1);
                            } else {
                                type = description.newInstance(format[0], format[1] != null ? getLocale(format[1]) : locale);
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
