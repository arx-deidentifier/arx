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

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class HierarchyWizard<T> extends Wizard implements IWizard {

    private final HierarchyWizardModel<T> model;
    private WizardDialog                  dialog;
    private final Controller              controller;

    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           final DataType<T> datatype,
                           final String[] items) {
        super();
        // TODO: Also offer a variant in which the builder is stored in the
        // model, and this model is initialized with it
        model = new HierarchyWizardModel<T>(datatype,
                                         items);
        this.controller = controller;
        setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("wizard.png"))); //$NON-NLS-1$
    }

    @Override
    public void addPages() {  
        HierarchyWizardPageIntervals<T> intervals = new HierarchyWizardPageIntervals<T>(model);
        HierarchyWizardPageOrdering<T> ordering = new HierarchyWizardPageOrdering<T>(model);
        HierarchyWizardPageRedaction<T> redaction = new HierarchyWizardPageRedaction<T>(model);
        addPage(new HierarchyWizardPageType<T>(model, intervals, ordering, redaction));
        addPage(intervals);
        addPage(ordering);
        addPage(redaction);
    }

    @Override
    public boolean canFinish() {
        return (model.getHierarchy() != null) && (dialog != null) &&
               ((dialog.getCurrentPage() instanceof HierarchyWizardPageIntervals)
               || (dialog.getCurrentPage() instanceof HierarchyWizardPageOrdering)
               || (dialog.getCurrentPage() instanceof HierarchyWizardPageRedaction));
    }

    public boolean open(final Shell shell) {
        final WizardDialog dialog = new WizardDialog(shell, this);
        this.dialog = dialog;
        return dialog.open() == 0;
    }

    @Override
    public boolean performFinish() {
        return true;
    }
}
