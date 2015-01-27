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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Button;

/**
 * An abstract base class for pages that allow configuring a builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class HierarchyWizardPageBuilder<T> extends WizardPage implements HierarchyWizardView {

    /** Var. */
    private final HierarchyWizardPageFinal<T>     finalPage;
    
    /** Var. */
    private final HierarchyWizardModelAbstract<T> model;
    
    /** Var. */
    private final HierarchyWizard<T>              wizard;

    /**
     * Creates a new base class.
     *
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        return finalPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
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
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView#update()
     */
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
     * Update the page when the model has changed.
     */
    public abstract void updatePage();
}
