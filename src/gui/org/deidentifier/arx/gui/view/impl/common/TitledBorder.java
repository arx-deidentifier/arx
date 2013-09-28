package org.deidentifier.arx.gui.view.impl.common;


import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TitledBorder {
    
    private final CTabFolder folder;
    private final CTabItem tab; 
    
    public TitledBorder(Composite parent, Controller controller, String title, String id){

        folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        
        // Create help button
        SWTUtil.createHelpButton(controller, folder, id);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
        
        // Create general tab
        tab = new CTabItem(folder, SWT.NULL);
        tab.setText(title);
        tab.setShowClose(false);

        folder.setSelection(tab);
    }
    
    public Composite getControl(){
        return folder;
    }
    
    public void setChild(Control child){
        this.tab.setControl(child);
    }
    
    public void setLayoutData(Object data){
        folder.setLayoutData(data);
    }
}
