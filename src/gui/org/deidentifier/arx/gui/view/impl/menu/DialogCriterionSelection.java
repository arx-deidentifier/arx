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
import org.deidentifier.arx.gui.model.ModelBLikenessCriterion;
import org.deidentifier.arx.gui.model.ModelDDisclosurePrivacyCriterion;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A dialog for selecting privacy models.
 *
 * @author Fabian Prasser
 */
public class DialogCriterionSelection extends TitleAreaDialog implements IDialog {

    /** View */
    private Button                       ok         = null;    
    /**  View */
    private Button                       cancel     = null;
    /**  Model */
    private List<ModelExplicitCriterion> elements   = null;
    /**  Model */
    private ModelExplicitCriterion       selection  = null;
    /**  Controller */
    private Controller                   controller = null;

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param parent
     * @param elements
     */
    public DialogCriterionSelection(final Controller controller,
                                    final Shell parent,
                                    List<ModelExplicitCriterion> elements) {
        super(parent);
        this.elements = elements;
        this.controller = controller;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    /**
     * Returns the selected model.
     *
     * @return
     */
    public ModelExplicitCriterion getCriterion() {
        return this.selection;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());

        // Create OK Button
        ok = createButton(parent, Window.OK, Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
        ok.setEnabled(false);

        // Create Cancel Button
        cancel = createButton(parent, Window.CANCEL, Resources.getMessage("ProjectDialog.4"), false); //$NON-NLS-1$
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
        setTitle(Resources.getMessage("CriterionSelectionDialog.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("CriterionSelectionDialog.0"), IMessageProvider.NONE); //$NON-NLS-1$
        return contents;
    }
    
    @Override
    protected Control createDialogArea(final Composite parent) {

        parent.setLayout(new GridLayout());

        final Table table = SWTUtil.createTable(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 100;
        table.setLayoutData(d);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(""); //$NON-NLS-1$
        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$
        
        Image symbolL = controller.getResources().getManagedImage("symbol_l.png"); //$NON-NLS-1$
        Image symbolT = controller.getResources().getManagedImage("symbol_t.png"); //$NON-NLS-1$
        Image symbolD = controller.getResources().getManagedImage("symbol_d.png"); //$NON-NLS-1$
        Image symbolB = controller.getResources().getManagedImage("symbol_b.png"); //$NON-NLS-1$
        
        for (ModelExplicitCriterion c : elements) {

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", c.toString(), c.getAttribute() }); //$NON-NLS-1$
            if (c instanceof ModelLDiversityCriterion) {
                item.setImage(0, symbolL);
            } else if (c instanceof ModelTClosenessCriterion) {
                item.setImage(0, symbolT);
            } else if (c instanceof ModelDDisclosurePrivacyCriterion) {
                item.setImage(0, symbolD);
            } else if (c instanceof ModelBLikenessCriterion) {
                item.setImage(0, symbolB);
            }
            
        }

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (table.getSelectionIndex() != -1) {
                    selection = elements.get(table.getSelectionIndex());
                    ok.setEnabled(true);
                } else {
                    selection = null;
                    ok.setEnabled(false);
                }
            }
        });

        column1.pack();
        column2.pack();
        column3.pack();

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

    @Override
    protected boolean isResizable() {
        return false;
    }
}
