package org.deidentifier.arx.gui.view.impl.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Button;

public abstract class HierarchyWizardPageBuilder<T> extends WizardPage implements HierarchyWizardView {

    private final HierarchyWizardPageFinal<T> finalPage;
    private final HierarchyWizardModelAbstract<T> model;
    private final HierarchyWizard<T> wizard;
    

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
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(true);
        }
        super.setVisible(value);
    }
    
    @Override
    public void update() {
        if (model.getError() != null) {
            this.setErrorMessage(model.getError());
            finalPage.setGroups(null);
            finalPage.setHierarchy(null);
            this.setPageComplete(false);
        } else {
            this.setErrorMessage(null);
            finalPage.setGroups(model.getGroups());
            finalPage.setHierarchy(model.getHierarchy());
            this.setPageComplete(true);
        }
    }

    public abstract void updatePage();
}
