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

package org.deidentifier.arx.gui.view.impl.wizards;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class HierarchyWizardPageIntervals<T> extends HierarchyWizardPageBuilder<T> {

    private final HierarchyWizardModelIntervals<T> model;
    private final Controller controller;
    
    public HierarchyWizardPageIntervals(final Controller controller,
                                        final HierarchyWizard<T> wizard,
                                       final HierarchyWizardModel<T> model, 
                                       final HierarchyWizardPageFinal<T> finalPage) {
        super(wizard, model.getIntervalModel(), finalPage);
        this.model = model.getIntervalModel();
        this.controller = controller;
        setTitle("Create a hierarchy by defining intervals");
        setDescription("Specify the parameters");
        setPageComplete(true);
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        HierarchyWizardEditor<Long> component = 
                new HierarchyWizardEditor<Long>(composite, 
                        (HierarchyWizardModelGrouping<Long>) model);
        component.setLayoutData(SWTUtil.createFillGridData());

        setControl(composite);
    }

    @Override
    public void updatePage() {
        model.update();
    }
}
