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
 * 
 */
public abstract class EditorString implements IEditor<String> {

    /**  TODO */
    private final String  category;
    
    /**  TODO */
    private final String  label;
    
    /**  TODO */
    private final boolean multi;
    
    /**  TODO */
    private final Button  ok;
    
    /**  TODO */
    private Text text;
    
    /**
     * 
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
    
    /**
     * 
     *
     * @param category
     * @param label
     * @param ok
     * @param multi
     */
    public EditorString(final String category,
                        final String label,
                        final Button ok,
                        final boolean multi) {
        this.category = category;
        this.label = label;
        this.multi = multi;
        this.ok = ok;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#createControl(org.eclipse.swt.widgets.Composite)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#getCategory()
     */
    @Override
    public String getCategory() {
        return category;
    }

    /**
     * 
     *
     * @return
     */
    public Control getControl() {
        return text;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#getLabel()
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Update.
     */
    public void update(){
        if (text!=null){
            String value = getValue();
            if (!text.getText().equals(value)) {
                text.setText(value);
            }
        }
    }
}
