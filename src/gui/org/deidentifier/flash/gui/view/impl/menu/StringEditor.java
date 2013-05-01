/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl.menu;

import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.view.def.IPropertyEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public abstract class StringEditor implements IPropertyEditor<String> {

    private final String  category;
    private final String  label;
    private final boolean multi;
    private final Button  ok;

    public StringEditor(final String category,
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
        final Text result;
        final GridData ldata = SWTUtil.createFillHorizontallyGridData();
        if (multi) {
            result = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
            ldata.heightHint = 100;
        } else {
            result = new Text(parent, SWT.SINGLE | SWT.BORDER);
        }

        result.setText(getValue());

        result.setLayoutData(ldata);
        result.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                if (accepts(result.getText())) {
                    setValue(result.getText());
                    ok.setEnabled(true);
                } else {
                    ok.setEnabled(false);
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
}
