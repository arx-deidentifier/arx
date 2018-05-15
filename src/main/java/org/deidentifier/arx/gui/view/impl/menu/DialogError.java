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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
 * A dialog for displaying error messages.
 *
 * @author Fabian Prasser
 */
public class DialogError extends TitleAreaDialog implements IDialog {

    /**  TODO */
    private Image image;
    
    /**  TODO */
    private final String message;
    
    /**  TODO */
    private final String error;

    /**
     * Constructor for displaying two messages.
     *
     * @param parentShell
     * @param controller
     * @param message
     * @param error
     */
    public DialogError(final Shell parentShell, final Controller controller, final String message, String error) {
        super(parentShell);
        this.message = message;
        this.error = error;
        this.image = controller.getResources().getManagedImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    public boolean close() {
        return super.close();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
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
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("DialogError.0")); //$NON-NLS-1$
        setMessage(message.replaceAll(" \\(\\)\\!", "!"), IMessageProvider.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }
    
    @Override
    protected Control createDialogArea(final Composite parent) {

    	parent.setLayout(new GridLayout());
        final Text text = new Text(parent, SWT.NONE | SWT.MULTI | SWT.V_SCROLL |
                                           SWT.H_SCROLL | SWT.BORDER);
        text.setText(error);
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 100;
        text.setLayoutData(d);
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return false;
    }
}
