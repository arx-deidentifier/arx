/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DialogError extends TitleAreaDialog implements IDialog {

    private Image image;
    private final String title;
    private final String message;
    private final String error;

    public DialogError(final Shell parentShell, final Controller controller,
    				   final String title, final String message) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.error = null;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
    }

    public DialogError(final Shell parentShell, final Controller controller,
    				   final String title, final String message, String error) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.error = error;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle(title); //$NON-NLS-1$
        setMessage(title, IMessageProvider.ERROR); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
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
    	
    	if (message != null){
    		final Label label = new Label(parent, SWT.NONE);
    		label.setText(message);
    		final GridData d = SWTUtil.createFillHorizontallyGridData();
    		d.heightHint = 20;
    		label.setLayoutData(d); 
    	}
    	if (error != null){
	        final Text text = new Text(parent, SWT.NONE | SWT.MULTI |
	                                              SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
	        text.setText(error);
	        final GridData d = SWTUtil.createFillGridData();
	        d.heightHint = 100;
	        text.setLayoutData(d);
    	}
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
