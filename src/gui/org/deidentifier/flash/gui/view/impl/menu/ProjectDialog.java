/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl.menu;

import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProjectDialog extends TitleAreaDialog {

    private Text   name        = null;
    private Text   description = null;
    private Button ok          = null;
    private Model  model       = null;

    public ProjectDialog(final Shell parent) {
        super(parent);
    }

    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("ProjectDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("ProjectDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$

        name.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                model = new Model(name.getText(), description.getText());
                if (name.getText().equals("")) { //$NON-NLS-1$
                    ok.setEnabled(false);
                } else {
                    ok.setEnabled(true);
                }
            }
        });

        description.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                model = new Model(name.getText(), description.getText());
            }
        });
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
        ok.setEnabled(false);

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
        final GridLayout l = new GridLayout();
        l.numColumns = 2;
        l.makeColumnsEqualWidth = false;
        parent.setLayout(l);

        // Build components
        final Label label = new Label(parent, SWT.NONE);
        label.setText(Resources.getMessage("ProjectDialog.5")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createNoFillGridData());
        name = new Text(parent, SWT.BORDER);
        name.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        name.setText(""); //$NON-NLS-1$

        final Label label2 = new Label(parent, SWT.LEFT | SWT.WRAP);
        label2.setText(Resources.getMessage("ProjectDialog.7")); //$NON-NLS-1$
        label2.setLayoutData(SWTUtil.createNoFillGridData());
        description = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP |
                                       SWT.V_SCROLL);
        description.setLayoutData(SWTUtil.createFillGridData());
        description.setText(""); //$NON-NLS-1$
        return parent;
    }

    public Model getProject() {
        return model;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                event.doit = false;
            }
        };
    }
}
