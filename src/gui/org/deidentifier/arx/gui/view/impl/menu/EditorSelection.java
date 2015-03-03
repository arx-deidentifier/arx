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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public abstract class EditorSelection implements IEditor<String> {

    /**  TODO */
    private final String   category;
    
    /**  TODO */
    private final String   label;
    
    /**  TODO */
    private final String[] elems;
    
    /**  TODO */
    private Combo combo;

    /**
     * 
     *
     * @param composite
     * @param elems
     */
    public EditorSelection(Composite composite, final String[] elems) {
        this.category = null;
        this.label = null;
        this.elems = elems;
        this.createControl(composite);
    }

    /**
     * 
     *
     * @param category
     * @param label
     * @param elems
     */
    public EditorSelection(final String category,
                           final String label,
                           final String[] elems) {
        this.category = category;
        this.label = label;
        this.elems = elems;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#accepts(java.lang.Object)
     */
    @Override
    public boolean accepts(final String s) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite parent) {
        combo = new Combo(parent, SWT.READ_ONLY);
        combo.setItems(elems);
        combo.select(indexOf(getValue()));
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() >= 0) {
                    setValue(elems[combo.getSelectionIndex()]);
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
    public Control getControl(){
        return combo;
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
    
    /**
     * 
     *
     * @param value
     * @return
     */
    private int indexOf(final String value) {
        for (int i = 0; i < elems.length; i++) {
            if (elems[i].equals(value)) { return i; }
        }
        return -1;
    }
}
