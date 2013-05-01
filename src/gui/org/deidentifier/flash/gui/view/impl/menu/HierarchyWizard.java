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

import org.deidentifier.flash.DataType;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class HierarchyWizard extends Wizard implements IWizard {

    private final HierarchyWizardModel model;
    private WizardDialog               dialog;
    private final Controller           controller;

    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           final DataType datatype,
                           final String suppressionString,
                           final String[] items) {
        super();
        model = new HierarchyWizardModel(attribute,
                                         datatype,
                                         suppressionString,
                                         items);
        this.controller = controller;
        setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("wizard.png"))); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        final HierarchyWizardPageLabels check = new HierarchyWizardPageLabels(model);
        addPage(new HierarchyWizardPageOrder(controller, model));
        addPage(new HierarchyWizardPageFanout(model));
        addPage(check);
    }

    @Override
    public boolean canFinish() {
        return (model.getHierarchy() != null) && (dialog != null) &&
               (dialog.getCurrentPage() == getPages()[2]);
    }

    public HierarchyWizardModel getModel() {
        return model;
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
