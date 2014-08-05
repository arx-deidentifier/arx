/*
 * ARX: Powerful Data Anonymization
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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An about dialog
 * @author Fabian Prasser
 */
public class DialogDebug extends TitleAreaDialog implements IDialog {

    private Image      image;
    private Controller controller;
    private Text       data;

    /**
     * Constructor
     * @param parentShell
     * @param controller
     */
    public DialogDebug(final Shell parentShell, final Controller controller) {
        super(parentShell);
        this.controller = controller;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle("Debugging Console"); //$NON-NLS-1$
        setMessage("Displays internal data structures"); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        // Create OK Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        
        final Button ceButton = createButton(parent,
                                             Integer.MAX_VALUE,
                                             "Clear events", false); //$NON-NLS-1$
        ceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionClearEventLog();
                data.setText(controller.getDebugData());
            }
        });
        
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
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(new GridLayout());

        // License
        data = new Text(parent, SWT.NONE | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        data.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        data.setText(controller.getDebugData());
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 200;
        d.grabExcessVerticalSpace = true;
        data.setLayoutData(d);

        return parent;
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
    
    @Override
    public boolean close() {
        if (image != null)
            image.dispose();
        return super.close();
    }
}
