/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class DialogHelp extends TitleAreaDialog implements IDialog {

    private Controller    controller;
    private Browser		  browser;
    private Image         image;
    private String[]      urls = {};

    public DialogHelp(final Shell parentShell, final Controller controller) {
        super(parentShell);
        this.controller = controller;
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
    	Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("DialogHelp.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("DialogHelp.2"), IMessageProvider.INFORMATION); //$NON-NLS-1$
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
        parent.setLayout(SWTUtil.createGridLayout(2));
        
        final List list = new List(parent, SWT.SIMPLE | SWT.BORDER);
        list.setLayoutData(SWTUtil.createFillVerticallyGridData());
        list.addListener(SWT.Selection, new Listener() {
           public void handleEvent(Event e) {
              int index = list.getSelectionIndex();
              browser.setUrl(urls[index]);
           }
        });
        
		try {
			browser = new Browser(parent, SWT.NONE | SWT.BORDER);
			browser.setLayoutData(SWTUtil.createFillGridData());
		} catch (SWTError e) {
			controller.actionShowErrorDialog("Error", "Browser cannot be initialized.");
			this.close();
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
