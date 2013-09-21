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

        folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
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
    
    private void createBar(final Controller controller, final CTabFolder folder, final TitleBar bar) {
        ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight( toolbar, SWT.RIGHT );
        
        for (String title : bar.getTitles()){
            
            final String key = title;
            ToolItem item = new ToolItem( toolbar, SWT.PUSH );
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

    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }

    public void addSelectionListener(SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    public void setSelection(int index) {
        folder.setSelection(index);
    }

    public void setEnabled(boolean b) {
        folder.setEnabled(b);
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
}
