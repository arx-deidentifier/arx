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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class BooleanEditor implements IPropertyEditor<Boolean> {

    private final String category;
    private final String label;

    public BooleanEditor(final String category, final String label) {
        this.category = category;
        this.label = label;
    }

    @Override
    public boolean accepts(final Boolean t) {
        return true;
    }

    @Override
    public void createControl(final Composite parent) {
        final Button result = new Button(parent, SWT.CHECK);
        result.setSelection(getValue());
        result.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        result.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                setValue(result.getSelection());
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
