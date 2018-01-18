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

import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IEditor;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * String editor
 * 
 * @author prasser Fabian Prasser
 */
public abstract class EditorString implements IEditor<String> {

    /**  Category */
    private final String  category;
    
    /**  Label */
    private final String  label;
    
    /**  Combo */
    private final boolean multi;
    
    /**  OK */
    private final Button  ok;
    
    /**  Text */
    private Text text;
    
    /**
     * Creates a new instance
     *
     * @param composite
     */
    public EditorString(Composite composite) {
        this.category = null;
        this.label = null;
        this.multi = false;
        this.ok = null;
        this.createControl(composite);
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        final GridData ldata = SWTUtil.createFillHorizontallyGridData();
        if (multi) {
            text = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
            ldata.heightHint = 60;
        } else {
            text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        }
        ldata.minimumWidth = 60;
        text.setText(getValue());
        text.setLayoutData(ldata);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                if (accepts(text.getText())) {
                    setValue(text.getText());
                    text.setForeground(GUIHelper.COLOR_BLACK);
                    if (ok != null) ok.setEnabled(true);
                } else {
                    text.setForeground(GUIHelper.COLOR_RED);
                    if (ok != null) ok.setEnabled(false);
                }
            }
        });
    }

    @Override
    public String getCategory() {
        return category;
    }

    /**
     * Returns the control
     *
     * @return
     */
    public Control getControl() {
        return text;
    }
    
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Update.
     */
    public void update(){
        if (text != null) {
            String value = getValue();
            if (isDifferent(text.getText(), value)) {
                text.setText(value);
            }
        }
    }
}
