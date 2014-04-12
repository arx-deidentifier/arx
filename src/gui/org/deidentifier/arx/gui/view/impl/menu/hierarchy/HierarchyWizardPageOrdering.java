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

package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class HierarchyWizardPageOrdering<T> extends WizardPage{

    private final HierarchyWizardModel<T> model;

    private Button interval;
    private Button order;
    private Button redaction;
    
    public HierarchyWizardPageOrdering(final HierarchyWizardModel<T> model) {
        super(""); //$NON-NLS-1$
        this.model = model;
        setTitle(Resources.getMessage("HierarchyWizardPageFanout.1")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageFanout.2")); //$NON-NLS-1$
        setPageComplete(true);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillGridData());
        composite.setLayout(SWTUtil.createGridLayout(1));
        
        // Create group
        Group group1 = new Group(composite, SWT.NONE);
        group1.setLayout(new RowLayout(SWT.VERTICAL));
        this.interval = new Button(group1, SWT.RADIO);
        this.interval.setText("Use intervals (for variables with ratio scale)");
        if (!(model.getDataType() instanceof DataTypeWithRatioScale)) {
            this.interval.setEnabled(false);
        }
        
        this.order = new Button(group1, SWT.RADIO);
        this.order.setText("Use ordering (e.g., for variables with ordinal scale");
        this.redaction = new Button(group1, SWT.RADIO);
        this.redaction.setText("Use redaction (e.g., for alphanumeric strings) ");
        this.redaction.setEnabled(true);
        setControl(composite);
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }
}
