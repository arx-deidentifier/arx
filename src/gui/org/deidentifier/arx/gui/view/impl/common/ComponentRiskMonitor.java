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
package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A risk monitor
 * 
 * @author Fabian Prasser
 */
public class ComponentRiskMonitor {

    /** The root canvas*/
    private final Composite root;
    /** The gauge*/
    private final ComponentGauge gauge;
    /** The text*/
    private final Text text;
    /**
     * Creates a new instance
     * @param parent
     */
    public ComponentRiskMonitor(Composite parent, String text) {
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(2));
        
        Label label = new Label(root, SWT.CENTER);
        label.setText(text);
        GridData data = SWTUtil.createFillGridData(2);
        data.horizontalAlignment = SWT.CENTER;
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        
        this.gauge = new ComponentGauge(root);
        data = SWTUtil.createFillGridData(2);
        data.horizontalAlignment = SWT.CENTER;
        data.heightHint = 100;
        data.widthHint = 100;
        data.minimumHeight = 100;
        data.minimumWidth = 100;
        this.gauge.setLayoutData(data);

        this.text = new Text(root, SWT.BORDER | SWT.CENTER);
        this.text.setText("0 %"); 
        this.text.setLayoutData(SWTUtil.createFillHorizontallyGridData(true, 1));
        this.text.setEditable(false);
    }
    
    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data) {
        this.root.setLayoutData(data);
    }
    
    /**
     * Value between 0 and 1
     * @param value
     * @return
     */
    public void setValue(double value) {
        if (value < 0d) {
            value = 0d;
        }
        if (value > 1d) {
            value = 1d;
        }
        gauge.setValue(value);
        text.setText(SWTUtil.getPrettyString(value * 100d) + "[%]");
    }
}
