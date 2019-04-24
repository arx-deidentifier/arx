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
package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.nebula.widgets.pagination.AbstractPageControllerComposite;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Empty navigator to support hiding
 * @author Fabian Prasser
 */
public class PageableTableNavigatorEmpty extends AbstractPageControllerComposite {

    /**
     * Creates a new instance
     * @param parent
     * @param style
     * @param controller 
     */
    public PageableTableNavigatorEmpty(Composite parent, int style, PageableController controller) {
        super(parent, style, controller);
        // Empty by design
    }

    @Override
    public void pageIndexChanged(int arg0, int arg1, PageableController arg2) {
        // Empty by design
    }

    @Override
    public void pageSizeChanged(int arg0, int arg1, PageableController arg2) {
        // Empty by design        
    }

    @Override
    public void sortChanged(String arg0, String arg1, int arg2, int arg3, PageableController arg4) {
        // Empty by design
    }

    @Override
    public void totalElementsChanged(long arg0, long arg1, PageableController arg2) {
        // Empty by design
    }

    @Override
    protected void createUI(Composite parent) {
        
        // Parent
        GridLayout layout = SWTUtil.createGridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        this.setLayout(layout);
        GridData data = SWTUtil.createNoFillGridData();
        data.heightHint=0;
        data.widthHint=0;
        this.setLayoutData(data);
        
        // Empty child
        Composite empty = new Composite(parent, SWT.NONE);
        data = SWTUtil.createNoFillGridData();
        data.heightHint=0;
        data.widthHint=0;
        empty.setLayoutData(data);
    }
}
