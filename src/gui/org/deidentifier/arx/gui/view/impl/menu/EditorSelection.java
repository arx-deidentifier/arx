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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class EditorSelection implements IEditor<String> {

    private final String   category;
    private final String   label;
    private final String[] elems;
    private Combo combo;

    public EditorSelection(Composite composite, final String[] elems) {
        this.category = null;
        this.label = null;
        this.elems = elems;
        this.createControl(composite);
    }

    public EditorSelection(final String category,
                           final String label,
                           final String[] elems) {
        this.category = category;
        this.label = label;
        this.elems = elems;
    }

    @Override
    public boolean accepts(final String s) {
        return true;
    }

    @Override
    public void createControl(final Composite parent) {
        combo = new Combo(parent, SWT.NONE);
        combo.setItems(elems);
        combo.select(indexOf(getValue()));
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() != -1) {
                    setValue(elems[combo.getSelectionIndex()]);
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

    private int indexOf(final String value) {
        for (int i = 0; i < elems.length; i++) {
            if (elems[i].equals(value)) { return i; }
        }
        return -1;
    }

    /**
     * Update
     */
    public void update(){
        if (combo!=null){
            int index = indexOf(getValue());
            if (index != combo.getSelectionIndex()){
                if (index == -1){
                    combo.deselect(combo.getSelectionIndex());
                } else {
                    combo.select(index);
                }
            }
        }
    }
    
    public Control getControl(){
        return combo;
    }
}
