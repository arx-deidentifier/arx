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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This class implements a dialog for selecting multiple elements
 *
 * @author Fabian Prasser
 */
public class DialogMultiSelection extends TitleAreaDialog implements IDialog {

    /** Widget. */
    private Button ok          = null;
    
    /** Elements*/
    private List<String> elements = new ArrayList<String>();
    
    /** Selected elements*/
    private List<String> selected = new ArrayList<String>();
    
    /** Constant*/
    private final String title;
    
    /** Constant*/
    private final String message;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     */
    public DialogMultiSelection(final Shell parent,
                             final String title,
                             final String message,
                             final List<String> elements,
                             final List<String> selected) {
        super(parent);
        this.title = title;
        this.message = message;
        this.elements = new ArrayList<String>(elements);
        this.selected = new ArrayList<String>(selected);
    }

    @Override
    public void create() {
        super.create();
        setTitle(title); //$NON-NLS-1$
        setMessage(message, IMessageProvider.NONE); //$NON-NLS-1$
    }

    /**
     * Returns the resulting project.
     *
     * @return
     */
    public List<String> getSelectedItems() {
        return this.selected;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
    }
    
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        final GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.verticalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.horizontalAlignment = SWT.LEFT;
        parent.setLayoutData(gridData);
        parent.getParent().setLayoutData(gridData);

        // Create OK Button
        ok = createButton(parent,
                          Window.OK,
                          Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });

        // Create Cancel Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        final Button cancel = createButton(parent,
                                           Window.CANCEL,
                                           Resources.getMessage("ProjectDialog.4"), false); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }
    
    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(SWTUtil.createGridLayout(1));
        final Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        
        for (String element : this.elements) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(element);
            item.setChecked(this.selected.contains(element));
        }
        
        table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem item = event.item instanceof TableItem ? (TableItem) event.item : null;
                    if (item != null) {
                        if (item.getChecked() && !selected.contains(item.getText())) {
                            selected.add(item.getText());
                        } else {
                            selected.remove(item.getText());
                        }
                    }
                }
            }
        });
        
        table.setLayoutData(SWTUtil.createFillGridData());
        
        return parent;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                setReturnCode(Window.CANCEL);
            }
        };
    }
}
