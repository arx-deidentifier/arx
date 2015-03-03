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

import java.util.List;

import org.deidentifier.arx.gui.Controller;
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
 * A dialog for selecting privacy criteria.
 *
 * @author Fabian Prasser
 */
public class DialogCriterionSelection extends TitleAreaDialog implements IDialog {

    /**  TODO */
    private Button                       ok         = null;
    
    /**  TODO */
    private Button                       cancel     = null;
    
    /**  TODO */
    private List<ModelExplicitCriterion> elements   = null;
    
    /**  TODO */
    private ModelExplicitCriterion       selection  = null;
    
    /**  TODO */
    private Image                        symbolL    = null;
    
    /**  TODO */
    private Image                        symbolT    = null;
    
    /**  TODO */
    private Controller                   controller = null;

    /**
     * Constructor.
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        if (symbolL != null) symbolL.dispose();
        if (symbolT != null) symbolT.dispose();
        return super.close();
    }

    /**
     * Returns the selected criterion.
     *
     * @return
     */
    public ModelExplicitCriterion getCriterion() {
        return this.selection;
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("CriterionSelectionDialog.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("CriterionSelectionDialog.0"), IMessageProvider.NONE); //$NON-NLS-1$
        return contents;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {

        parent.setLayout(new GridLayout());

        final Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 100;
        table.setLayoutData(d);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("");
        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$
        
        this.symbolL = controller.getResources().getImage("symbol_l.png"); //$NON-NLS-1$
        this.symbolT = controller.getResources().getImage("symbol_t.png"); //$NON-NLS-1$
        
        for (ModelExplicitCriterion c : elements) {

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", c.toString(), c.getAttribute() });
            if (c instanceof ModelLDiversityCriterion) {
                item.setImage(0, symbolL);
            } else if (c instanceof ModelTClosenessCriterion) {
                item.setImage(0, symbolT);
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getShellListener()
     */
    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                setReturnCode(Window.CANCEL);
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return false;
    }
}
