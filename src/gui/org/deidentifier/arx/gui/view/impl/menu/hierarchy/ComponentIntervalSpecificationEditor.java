package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ComponentIntervalSpecificationEditor<T> {

    private Composite composite;
    
    public ComponentIntervalSpecificationEditor(Composite parent, ModelInterval<T> model) {

        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillGridData());
        composite.setLayout(SWTUtil.createGridLayout(1));
        
        ComponentIntervalAdjustmentEditor<T> editor1 = new ComponentIntervalAdjustmentEditor<T>(composite, model, true);
        ComponentIntervalEditor<T> editor2 = new ComponentIntervalEditor<T>(composite, model);
        ComponentIntervalAdjustmentEditor<T> editor3 = new ComponentIntervalAdjustmentEditor<T>(composite, model, false);
        editor2.setLayoutData(SWTUtil.createFillGridData());
    }
    
    public void setLayoutData(Object data){
        composite.setLayoutData(data);
    }
}
