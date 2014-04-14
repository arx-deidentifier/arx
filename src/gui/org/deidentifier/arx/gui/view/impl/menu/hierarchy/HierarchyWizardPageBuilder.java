package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

public abstract class HierarchyWizardPageBuilder<T> extends WizardPage implements HierarchyWizardView {

    private final HierarchyWizardPageFinal<T> finalPage;
    private final HierarchyWizardModelAbstract<T> model;

    public HierarchyWizardPageBuilder(final HierarchyWizardModelAbstract<T> model, 
                                      final HierarchyWizardPageFinal<T> finalPage){
        super("");
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
    

    @Override
    public void setVisible(boolean value){
        
        if (value) {
            this.model.update();
        }
        super.setVisible(value);
    }
}
