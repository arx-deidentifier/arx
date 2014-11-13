/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
