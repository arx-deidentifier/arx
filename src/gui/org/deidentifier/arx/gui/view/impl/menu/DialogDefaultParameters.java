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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * A dialog to select default configurations for privacy models
 *
 * @author Fabian Prasser
 */
public class DialogDefaultParameters extends TitleAreaDialog implements IDialog {

    /** List */
    private final List<ModelCriterion> list;

    /** Selection */
    private ModelCriterion             selection;

    /** Button */
    private Button                     ok;
    
    /**
     * Constructor.
     *
     * @param parentShell
     * @param controller
     */
    public DialogDefaultParameters(final Shell parentShell, 
                                   final Controller controller,
                                   final List<ModelCriterion> list) {
        super(parentShell);
        this.list = list;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    /**
     * Returns the selected element
     * @return
     */
    public ModelCriterion getSelection() {
        return selection;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }
    
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

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
        ok.setEnabled(false);

        // Create Cancel Button
        Button cancel = createButton(parent,
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
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("DialogDefaultParameters.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("DialogDefaultParameters.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(SWTUtil.createGridLayout(1));

        // Create table
        final DynamicTable table = SWTUtil.createTableDynamic(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        GridData data = SWTUtil.createFillGridData();
        data.heightHint = 300;
        table.setLayoutData(data);
        
        // Create column
        DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.NONE);
        column1.setText(Resources.getMessage("DialogDefaultParameters.2")); //$NON-NLS-1$
        column1.setWidth("100%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        column1.pack();
        
        // Add
        for (ModelCriterion c : list) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] {c.toString()});
        }

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (table.getSelectionIndex() != -1) {
                    selection = list.get(table.getSelectionIndex());
                    ok.setEnabled(true);
                } else {
                    ok.setEnabled(false);
                }
            }
        });
        
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
}
