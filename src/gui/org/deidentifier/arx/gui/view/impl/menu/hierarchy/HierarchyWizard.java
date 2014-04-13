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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.menu.ARXWizardDialog;
import org.deidentifier.arx.gui.view.impl.menu.ARXWizardDialog.ARXWizardButton;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
        this.model = new HierarchyWizardModel<T>(datatype, items);
        this.controller = controller;
        this.setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        this.setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("wizard.png"))); //$NON-NLS-1$
    }
    
    @Override
    public void addPages() {
        HierarchyWizardPageFinal<T> finalPage = new HierarchyWizardPageFinal<T>(model);
        HierarchyWizardPageIntervals<T> intervalsPage = null;
        if (model.getDataType() instanceof DataTypeWithRatioScale){
            intervalsPage = new HierarchyWizardPageIntervals<T>(controller, model, finalPage);
        }
        HierarchyWizardPageOrder<T> orderingPage = new HierarchyWizardPageOrder<T>(controller, model, finalPage);
        HierarchyWizardPageRedaction<T> redactionPage = new HierarchyWizardPageRedaction<T>(controller, model, finalPage);
        addPage(new HierarchyWizardPageType<T>(model, intervalsPage, orderingPage, redactionPage));
        if (intervalsPage != null) addPage(intervalsPage);
        addPage(orderingPage);
        addPage(redactionPage);
        addPage(finalPage);
    }

    @Override
    public boolean canFinish() {
        return dialog.getCurrentPage() instanceof HierarchyWizardPageFinal;
    }

    private void load(){
        
    }
    
    private void save(){
        
    }
    
    public boolean open(final Shell shell) {
        
        List<ARXWizardButton> buttons = new ArrayList<ARXWizardButton>();
        buttons.add(new ARXWizardButton("Load...", new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                load();
            }
        }));
        buttons.add(new ARXWizardButton("Save...", new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                save();
            }
        }));
        
        final WizardDialog dialog = new ARXWizardDialog(shell, this, buttons);
        this.dialog = dialog;
        this.dialog.setPageSize(800, 400);
        return dialog.open() == 0;
    }

    @Override
    public boolean performFinish() {
        return true;
    }
}
