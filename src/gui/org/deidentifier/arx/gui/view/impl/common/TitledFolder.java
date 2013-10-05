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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class TitledFolder {
    
    private final CTabFolder folder;

    public TitledFolder(Composite parent, Controller controller, TitleBar bar, String id){
        this(parent, controller, bar, id, false);
    }

    public TitledFolder(Composite parent, Controller controller, TitleBar bar, String id, boolean bottom){

        int flags = SWT.BORDER | SWT.FLAT;
        if (bottom) flags |= SWT.BOTTOM;
        else flags |= SWT.TOP;
        
        folder = new CTabFolder(parent, flags);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        
        // Create help button
        if (bar == null) SWTUtil.createHelpButton(controller, folder, id);
        else createBar(controller, folder, bar);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
    }
    
    public void addSelectionListener(SelectionListener listener) {
        folder.addSelectionListener(listener);
    }
    
    public Composite createItem(String title, Image image){

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        CTabItem item = new CTabItem(folder, SWT.NULL);
        item.setText(title);
        if (image!=null) item.setImage(image);
        item.setShowClose(false);
        item.setControl(composite);
        
        return composite;
    }

    public ToolItem getBarItem(String text) {
        Control c = folder.getTopRight();
        if (c == null) return null;
        if (!(c instanceof ToolBar)) return null;
        ToolBar t = (ToolBar)c;
        for (ToolItem i : t.getItems()){
            if (i.getToolTipText().equals(text)) return i;
        }
        return null;
    }

    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    public void setEnabled(boolean b) {
        folder.setEnabled(b);
    }

    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }

    public void setSelection(int index) {
        folder.setSelection(index);
    }

    private void createBar(final Controller controller, final CTabFolder folder, final TitleBar bar) {
        ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight( toolbar, SWT.RIGHT );
        
        for (String title : bar.getTitles()){
            
            final String key = title;
            ToolItem item = new ToolItem( toolbar, SWT.PUSH);
            item.setImage(bar.getImage(key));
            item.setToolTipText(title);
            item.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    bar.getRunnable(key).run();
                }
            });
        }
        
        ToolItem item = new ToolItem( toolbar, SWT.PUSH );
        item.setImage(controller.getResources().getImage("help.png"));  //$NON-NLS-1$
        item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                controller.actionShowHelp(bar.getId());
            }
        });
        
        int height = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        folder.setTabHeight(Math.max(height, folder.getTabHeight()));
    }
}
