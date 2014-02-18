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
import org.deidentifier.arx.gui.view.impl.menu.DialogHelpConfig.Entry;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class DialogHelp extends TitleAreaDialog implements IDialog {

    private String           id;
    private Browser          browser;
    private List             list;
    private Image            image;
    private DialogHelpConfig config = new DialogHelpConfig();

    public DialogHelp(final Shell parentShell, final Controller controller, final String id) {
        super(parentShell);
        this.id = id;
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
        
        Composite compTools = new Composite(parent, SWT.NONE);
        compTools.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        compTools.setLayout(new GridLayout(1, false));
        ToolBar navBar = new ToolBar(compTools, SWT.NONE);
        navBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        final ToolItem back = new ToolItem(navBar, SWT.PUSH);
        back.setText("Back");
        back.setEnabled(false);
        final ToolItem forward = new ToolItem(navBar, SWT.PUSH);
        forward.setText("Forward");
        forward.setEnabled(false);
        
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(SWTUtil.createFillGridData());
        comp.setLayout(new FillLayout());
        
        final SashForm form = new SashForm(comp, SWT.HORIZONTAL);
        form.setLayout(new FillLayout());

        list = new List(form, SWT.SINGLE);
        try {
            browser = new Browser(form, SWT.BORDER);
        } catch (SWTError e) {
            throw new RuntimeException(e);
        }
        
        for (Entry entry : config.getEntries()) {
            list.add(entry.title);
        }
        
        form.setWeights(new int[]{25,75});
        
        back.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                browser.back();
            }
        });
        forward.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                browser.forward();
            }
        });
        browser.addLocationListener(new LocationAdapter() {
            public void changed(LocationEvent event) {
                Browser browser = (Browser) event.widget;
                back.setEnabled(browser.isBackEnabled());
                forward.setEnabled(browser.isForwardEnabled());
                list.select(getIndexOf(event.location));
            }
        });
        list.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                browser.setUrl(getUrlOf(list.getSelectionIndex()));
            }
        });
        
        int index = id == null ? 0 : config.getIndexForId(id);
        list.select(index);
        browser.setUrl(getUrlOf(index));
        return parent;
    }

    protected String getUrlOf(int index) {
        return config.getUrlForIndex(index);
    }

    private int getIndexOf(String location) {
        return config.getIndexForUrl(location);
    }
    
    

    @Override
    protected Point getInitialSize() {
        return new Point(900,600);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    public boolean close() {
        if (image != null)
            image.dispose();
        return super.close();
    }
}
