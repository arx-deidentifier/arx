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

import java.util.List;

import org.deidentifier.arx.gui.model.ModelAuditTrailEntry;
import org.deidentifier.arx.gui.model.ModelAuditTrailEntry.AuditTrailEntryFindReplace;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * A dialog for displaying the audit trail
 * 
 * @author Fabian Prasser
 */
public class DialogAuditTrail extends TitleAreaDialog {

    /** Model */
    private String                     title;
    /** Model */
    private String                     message;
    /** Model */
    private List<ModelAuditTrailEntry> auditTrail;

    /**
     * Creates a new instance
     * 
     * @param shell
     * @param auditTrail
     * 
     */
    public DialogAuditTrail(Shell shell, List<ModelAuditTrailEntry> auditTrail) {
        super(shell);
        this.title = Resources.getMessage("DialogAuditTrail.0"); //$NON-NLS-1$
        this.message = Resources.getMessage("DialogAuditTrail.1"); //$NON-NLS-1$
        this.auditTrail = auditTrail;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }


    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent,
                     IDialogConstants.OK_ID,
                     IDialogConstants.OK_LABEL,
                     true).setEnabled(true);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(title); 
        setMessage(message);
        return contents;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {

        Composite composite = (Composite) super.createDialogArea(parent);        

        final DynamicTable table = SWTUtil.createTableDynamic(composite, SWT.SINGLE | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        
        final DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.LEFT);
        column1.setText(Resources.getMessage("DialogAuditTrail.2")); //$NON-NLS-1$
        column1.setWidth("25%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$

        final DynamicTableColumn column2 = new DynamicTableColumn(table, SWT.LEFT);
        column2.setText(Resources.getMessage("DialogAuditTrail.3")); //$NON-NLS-1$
        column2.setWidth("25%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        final DynamicTableColumn column3 = new DynamicTableColumn(table, SWT.LEFT);
        column3.setText(Resources.getMessage("DialogAuditTrail.4")); //$NON-NLS-1$
        column3.setWidth("25%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$

        final DynamicTableColumn column4 = new DynamicTableColumn(table, SWT.LEFT);
        column4.setText(Resources.getMessage("DialogAuditTrail.5")); //$NON-NLS-1$
        column4.setWidth("25%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        table.setItemCount(auditTrail.size());
        
        column1.pack();
        column2.pack();
        column3.pack();
        column4.pack();
        
        table.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final TableItem item = (TableItem) event.item;
                final int index = table.indexOf(item);
                item.setText(0, Resources.getMessage("DialogAuditTrail.7")); //$NON-NLS-1$
                item.setText(1, ((AuditTrailEntryFindReplace)auditTrail.get(index)).getAttribute());
                item.setText(2, ((AuditTrailEntryFindReplace)auditTrail.get(index)).getSearchString());
                item.setText(3, ((AuditTrailEntryFindReplace)auditTrail.get(index)).getReplacementString());
            }
        });
        
        applyDialogFont(composite);
        return composite;
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
