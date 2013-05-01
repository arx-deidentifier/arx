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

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AboutDialog extends TitleAreaDialog {

    private static final String LICENSE = Resources.getMessage("AboutDialog.0") + Resources.getMessage("AboutDialog.1") //$NON-NLS-1$ //$NON-NLS-2$
                                          +
                                          Resources.getMessage("AboutDialog.2") + Resources.getMessage("AboutDialog.3") + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                          +
                                          Resources.getMessage("AboutDialog.5") + Resources.getMessage("AboutDialog.6") //$NON-NLS-1$ //$NON-NLS-2$
                                          +
                                          Resources.getMessage("AboutDialog.7") + Resources.getMessage("AboutDialog.8") + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                          +
                                          Resources.getMessage("AboutDialog.10") + Resources.getMessage("AboutDialog.11"); //$NON-NLS-1$ //$NON-NLS-2$
    private final Controller    controller;

    public AboutDialog(final Shell parentShell, final Controller controller) {
        super(parentShell);
        this.controller = controller;
    }

    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("AboutDialog.12")); //$NON-NLS-1$
        setMessage(Resources.getMessage("AboutDialog.13"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        setTitleImage(controller.getResources().getImage("logo.png")); //$NON-NLS-1$
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        // Create OK Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        final Button okButton = createButton(parent,
                                             Window.OK,
                                             Resources.getMessage("AboutDialog.15"), true); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(new GridLayout());

        // Text
        final Label label = new Label(parent, SWT.CENTER | SWT.NONE);
        final StringBuffer text = new StringBuffer();
        text.append(Resources.getMessage("AboutDialog.16")); //$NON-NLS-1$
        text.append("\n"); //$NON-NLS-1$
        text.append(Resources.getMessage("AboutDialog.18")); //$NON-NLS-1$
        text.append("\n"); //$NON-NLS-1$
        text.append("\n"); //$NON-NLS-1$
        text.append(Resources.getMessage("AboutDialog.21") + controller.getResources().getVersion()); //$NON-NLS-1$
        text.append("\n"); //$NON-NLS-1$
        text.append(Resources.getMessage("AboutDialog.23")); //$NON-NLS-1$
        label.setText(text.toString());
        label.setLayoutData(SWTUtil.createFillGridData());

        // Separator
        new Label(parent, SWT.NONE);

        // License
        final Text license = new Text(parent, SWT.NONE | SWT.MULTI |
                                              SWT.V_SCROLL | SWT.BORDER);
        license.setText(LICENSE);
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 100;
        license.setLayoutData(d);

        return parent;
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}
