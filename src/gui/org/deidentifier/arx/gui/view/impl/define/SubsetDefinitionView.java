package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class SubsetDefinitionView implements IView{

    private Controller controller;
    private Composite root;
    private Model model;

    public SubsetDefinitionView(final Composite parent,
                                   final Controller controller) {

        this.controller = controller;
        this.controller.addListener(EventTarget.MODEL, this);
        this.controller.addListener(EventTarget.INPUT, this);
        this.root = build(parent);
    }

    private Composite build(Composite parent) {

        /*
         * Add general view
         */
        CTabFolder folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(true);
        folder.setTabHeight(25);
        GridData layoutdata = SWTUtil.createFillGridData();
        layoutdata.grabExcessVerticalSpace = false;
        folder.setLayoutData(layoutdata);
        
        // Create help button
        SWTUtil.createHelpButton(controller, folder, "id-10"); //$NON-NLS-1$

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
        
        // Create general tab
        final CTabItem tab = new CTabItem(folder, SWT.NULL);
        tab.setText(Resources.getMessage("SubsetDefinitionView.0"));  //$NON-NLS-1$
        tab.setShowClose(false);

        Composite group = new Composite(folder, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        group.setLayout(layout);
        tab.setControl(group);

        folder.setSelection(tab);
        return group;
    }


    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void reset() {
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.MODEL) {
            
            model = (Model) event.data;
            root.setRedraw(false);
            // TODO: Load subset
            root.setRedraw(true);
        } else if (event.target == EventTarget.INPUT) {
            SWTUtil.enable(root);
        } 
    }
}
