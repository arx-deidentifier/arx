package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.gui.view.impl.common.TitledBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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

        TitledBorder border = new TitledBorder(parent, controller, Resources.getMessage("SubsetDefinitionView.0"), "id-40");  //$NON-NLS-1$
        Composite group = new Composite(border.getControl(), SWT.NONE);
        border.setChild(group);
        border.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        group.setLayout(layout);
        group.setLayoutData(SWTUtil.createFillGridData());
        
        Label l = new Label(group, SWT.NONE);
        l.setText("Size: 0 / 0 [0%]");
        l.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        Button b1 = new Button(group, SWT.PUSH);
        b1.setText("Load from file");
        b1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        Button b2 = new Button(group, SWT.PUSH);
        b2.setText("Define predicate");
        b2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
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
