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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Button;

/**
 * An abstract base class for pages that allow configuring a builder
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyWizardPageBuilder<T> extends WizardPage implements HierarchyWizardView {

    /** Var */
    private final HierarchyWizardPageFinal<T>     finalPage;
    /** Var */
    private final HierarchyWizardModelAbstract<T> model;
    /** Var */
    private final HierarchyWizard<T>              wizard;

    /**
     * Creates a new base class
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPageBuilder(final HierarchyWizard<T> wizard,
                                      final HierarchyWizardModelAbstract<T> model, 
                                      final HierarchyWizardPageFinal<T> finalPage){
        super("");
        this.wizard = wizard;
        this.model = model;
        this.finalPage = finalPage;
        this.model.setView(this);
        this.model.update();
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }


    @Override
    public IWizardPage getNextPage() {
        return finalPage;
    }

    @Override
    public void setVisible(boolean value){
        
        if (value) {
            this.model.update();
            Button load = this.wizard.getLoadButton();
            if (load != null) load.setEnabled(false);
        }
        super.setVisible(value);
        model.setVisible(value);
    }
    
    @Override
    public void update() {
        if (model.getError() != null) {
            this.setErrorMessage(model.getError());
            finalPage.setGroups(null);
            finalPage.setHierarchy(null);
            this.setPageComplete(false);
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(false);
        } else {
            this.setErrorMessage(null);
            finalPage.setGroups(model.getGroups());
            finalPage.setHierarchy(model.getHierarchy());
            this.setPageComplete(true);
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(true);
        }
    }

    /**
     * Update the page when the model has changed
     */
    public abstract void updatePage();
}
