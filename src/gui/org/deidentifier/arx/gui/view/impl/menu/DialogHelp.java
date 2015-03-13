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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.gui.view.impl.menu.DialogHelpConfig.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
public class DialogHelp implements IDialog {

    /** View */
    private final Shell shell;

    /** Model */
    private DialogHelpConfig config = new DialogHelpConfig();

    /**
     * Constructor.
     *
     * @param parentShell
     * @param controller
     * @param id
     */
    public DialogHelp(final Shell parent, final Controller controller, final String id) {
        
        // Shell
        shell = new Shell(parent, SWT.TITLE | SWT.CLOSE | SWT.BORDER | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
        shell.setImages(Resources.getIconSet(shell.getDisplay()));
        shell.setText(Resources.getMessage("DialogHelp.1")); //$NON-NLS-1$
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginBottom = 5;
        layout.marginLeft = 5;
        layout.marginTop = 5;
        layout.marginRight = 5;
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);
        
        // Toolbar
        ToolBar toolbar = new ToolBar(shell, SWT.NONE);
        toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        final ToolItem back = new ToolItem(toolbar, SWT.PUSH);
        back.setText("Back");
        back.setEnabled(false);
        final ToolItem forward = new ToolItem(toolbar, SWT.PUSH);
        forward.setText("Forward");
        forward.setEnabled(false);
        
        // Base
        Composite root = new Composite(shell, SWT.NONE);
        root.setLayoutData(SWTUtil.createFillGridData());
        root.setLayout(SWTUtil.createGridLayout(2));

        // List
        final List list = new List(root, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        for (Entry entry : config.getEntries()) {
            list.add(entry.title);
        }
        list.setLayoutData(SWTUtil.createFillVerticallyGridData());
        
        // Browser
        final HTMLBrowser browser = new HTMLBrowser(root, SWT.BORDER);
        browser.setLayoutData(SWTUtil.createFillGridData());

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
        
        // OK button
        Button button = new Button(root, SWT.PUSH);
        button.setText(Resources.getMessage("AboutDialog.15")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (shell != null && !shell.isDisposed()) {
                    shell.dispose();
                }
            }
        });
        
        GridData data = SWTUtil.createGridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        data.verticalAlignment = SWT.CENTER;
        Point size = button.computeSize(100, SWT.DEFAULT);
        data.widthHint = size.x;
        data.heightHint = size.y;
        button.setLayoutData(data);
        
        // Select
        int index = id == null ? 0 : config.getIndexForId(id);
        list.select(index);
        try{browser.setUrl(getUrlOf(index));} catch (Exception e){}
    }
    /**
     * Centers the shell within its parent shell
     * @param shell
     */
    private void center(Shell shell) {
        
        // Init
        Shell parent = (Shell)shell.getParent();
        Rectangle bounds = parent.getBounds();
        Point size = shell.getSize();
        
        // Compute
        int x = bounds.x + bounds.width / 2 - size.x / 2;
        int y = bounds.y + bounds.height / 2 - size.y / 2;
        
        // Set
        shell.setLocation(x, y);
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
    
    protected Point getInitialSize() {
        return new Point(900,600);
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
    public void open() {
        shell.setSize(getInitialSize());
        center(shell);
        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}