/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public abstract class EditorString implements IEditor<String> {

    private final String  category;
    private final String  label;
    private final boolean multi;
    private final Button  ok;
    private Text text;
    
    public EditorString(Composite composite) {
        this.category = null;
        this.label = null;
        this.multi = false;
        this.ok = null;
        this.createControl(composite);
    }
    
    public EditorString(final String category,
                        final String label,
                        final Button ok,
                        final boolean multi) {
        this.category = category;
        this.label = label;
        this.multi = multi;
        this.ok = ok;
    }

    @Override
    public void createControl(final Composite parent) {
        
        final GridData ldata = SWTUtil.createFillHorizontallyGridData();
        if (multi) {
            text = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
            ldata.heightHint = 100;
        } else {
            text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        }

        text.setText(getValue());

        text.setLayoutData(ldata);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                if (accepts(text.getText())) {
                    setValue(text.getText());
                    if (ok != null) ok.setEnabled(true);
                } else {
                    if (ok != null) ok.setEnabled(false);
                }
            }
        });
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getLabel() {
        return label;
    }
    
    /**
     * Update
     */
    public void update(){
        if (text!=null) text.setText(getValue());
    }
}
