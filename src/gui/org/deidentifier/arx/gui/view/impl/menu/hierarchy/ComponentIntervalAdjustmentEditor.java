package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ComponentIntervalAdjustmentEditor<T> {

    private Composite composite;
    private Text label;
    private Text snap;
    private Text repeat;
    
    public ComponentIntervalAdjustmentEditor(Composite parent, ModelInterval<T> model, boolean lower){

        composite = new Composite(parent, SWT.NONE);
   
        composite.setLayout(SWTUtil.createGridLayout(3));

            Label label3 = new Label(composite, SWT.NONE);
            label3.setText("Repeat");
            label3.setAlignment(SWT.CENTER);
            GridData data = SWTUtil.createFillGridData();
            data.verticalAlignment = SWT.CENTER; 
            label3.setLayoutData(data);
            
            Label label2 = new Label(composite, SWT.NONE);
            label2.setText("Snap");
            label2.setAlignment(SWT.CENTER);
            data = SWTUtil.createFillGridData();
            data.verticalAlignment = SWT.CENTER; 
            label2.setLayoutData(data);
            
            Label label1 = new Label(composite, SWT.NONE);
            label1.setText("Label");
            label1.setAlignment(SWT.CENTER);
            data = SWTUtil.createFillGridData();
            data.verticalAlignment = SWT.CENTER; 
            label1.setLayoutData(data);
            
            repeat = new Text(composite, SWT.BORDER);
            snap = new Text(composite, SWT.BORDER);
            label = new Text(composite, SWT.BORDER);
    }
}
