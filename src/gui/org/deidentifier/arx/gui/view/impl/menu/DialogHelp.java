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
import org.deidentifier.arx.gui.view.impl.menu.DialogHelpConfig.Entry;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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

import de.linearbits.swt.simplebrowser.HTMLBrowser;

/**
 * A help dialog.
 *
 * @author Fabian Prasser
 */
public class DialogHelp extends TitleAreaDialog implements IDialog {

    /** View */
    private String           id;

    /** View */
    private HTMLBrowser      browser;

    /** View */
    private List             list;

    /** View */
    private Image            image;

    /** Model */
    private DialogHelpConfig config = new DialogHelpConfig();

    /**
     * Constructor.
     *
     * @param parentShell
     * @param controller
     * @param id
     */
    public DialogHelp(final Shell parentShell, final Controller controller, final String id) {
        super(parentShell);
        this.id = id;
        this.image = controller.getResources().getManagedImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    public boolean close() {
        return super.close();
    }

    /**
     * Returns the index of a url.
     *
     * @param location
     * @return
     */
    private int getIndexOf(String location) {
        return config.getIndexForUrl(location);
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
        setTitle(Resources.getMessage("DialogHelp.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("DialogHelp.2"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        
        // Root
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        root.setLayout(new GridLayout(1, false));
        
        // Toolbar
        ToolBar toolbar = new ToolBar(root, SWT.NONE);
        toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        final ToolItem back = new ToolItem(toolbar, SWT.PUSH);
        back.setText(Resources.getMessage("DialogHelp.0")); //$NON-NLS-1$
        back.setEnabled(false);
        final ToolItem forward = new ToolItem(toolbar, SWT.PUSH);
        forward.setText(Resources.getMessage("DialogHelp.3")); //$NON-NLS-1$
        forward.setEnabled(false);
        
        // Base
        Composite base = new Composite(parent, SWT.NONE);
        base.setLayoutData(SWTUtil.createFillGridData());
        base.setLayout(SWTUtil.createGridLayout(2));

        // List
        list = new List(base, SWT.SINGLE | SWT.V_SCROLL);
        for (Entry entry : config.getEntries()) {
            list.add(entry.title);
        }
        list.setLayoutData(SWTUtil.createFillVerticallyGridData());
        
        // Browser
        browser = new HTMLBrowser(base, SWT.BORDER | SWT.DOUBLE_BUFFERED);
        browser.getStyle().setClickableLinks(false);
        browser.setLayoutData(SWTUtil.createFillGridData());

        // Listeners
        back.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {browser.back();} catch (Exception e){}
            }
        });
        forward.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {browser.forward();} catch (Exception e){}
            }
        });
        browser.addLocationListener(new LocationAdapter() {
            public void changed(LocationEvent event) {
                back.setEnabled(browser.isBackEnabled());
                forward.setEnabled(browser.isForwardEnabled());
                try{list.select(getIndexOf(event.location));} catch (Exception e){}
            }
        });
        list.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                try{browser.setUrl(getUrlOf(list.getSelectionIndex()));} catch (Exception e){}
            }
        });
        
        // Init
        int index = id == null ? 0 : config.getIndexForId(id);
        list.select(index);
        list.showSelection();
        try{browser.setUrl(getUrlOf(index));} catch (Exception e){}
        return parent;
    }
    
    @Override
    protected Point getInitialSize() {
        return new Point(1000,600);
    }

    /**
     * Returns the url for an index.
     *
     * @param index
     * @return
     */
    protected String getUrlOf(int index) {
        return config.getUrlForIndex(index);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
}