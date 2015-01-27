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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public abstract class EditorBoolean implements IEditor<Boolean> {

    /**  TODO */
    private final String category;
    
    /**  TODO */
    private final String label;

    /**
     * 
     *
     * @param category
     * @param label
     */
    public EditorBoolean(final String category, final String label) {
        this.category = category;
        this.label = label;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#accepts(java.lang.Object)
     */
    @Override
    public boolean accepts(final Boolean t) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#createControl(org.eclipse.swt.widgets.Composite)
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#getCategory()
     */
    @Override
    public String getCategory() {
        return category;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IEditor#getLabel()
     */
    @Override
    public String getLabel() {
        return label;
    }
}
