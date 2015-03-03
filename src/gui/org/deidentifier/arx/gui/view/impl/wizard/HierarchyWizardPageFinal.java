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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ViewHierarchy;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;

/**
 * The final page that shows an overview of the resulting hierarchy.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPageFinal<T> extends WizardPage{

    /** Var. */
    private final HierarchyWizard<T> wizard;
    
    /** Var. */
    private Composite                composite;
    
    /** Var. */
    private List                     list;
    
    /** Var. */
    private ViewHierarchy            view;
    
    /** Var. */
    private int[]                    groups;
    
    /** Var. */
    private Hierarchy                hierarchy;
    
    /**
     * Creates a new instance.
     *
     * @param wizard
     */
    public HierarchyWizardPageFinal(final HierarchyWizard<T> wizard) {
        super("");
        this.wizard = wizard;
        setTitle("Review the hierarchy");
        setDescription("Overview of groups and values");
        setPageComplete(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void  createControl(final Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, false));

        final Group center = new Group(composite, SWT.SHADOW_ETCHED_IN);
        center.setText("#Groups");
        center.setLayoutData(SWTUtil.createFillVerticallyGridData());
        center.setLayout(SWTUtil.createGridLayout(1, false));
        
        Composite base = new Composite(center, SWT.NONE);
        base.setLayoutData(SWTUtil.createFillGridData());
        base.setLayout(SWTUtil.createGridLayout(1, false));
        
        list = new List(base, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
        list.setLayoutData(SWTUtil.createFillGridData());
        
        final Group right = new Group(composite, SWT.SHADOW_ETCHED_IN);
        right.setText("Table");
        right.setLayoutData(SWTUtil.createFillGridData());
        right.setLayout(SWTUtil.createGridLayout(1, false));
        view = new ViewHierarchy(right);

        setControl(composite);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return false;
    }
    
    /**
     * Sets the groups.
     *
     * @param groups
     */
    public void setGroups(int[] groups){
        this.groups = groups;
    }
    
    /**
     * Sets the hierarchy.
     *
     * @param hierarchy
     */
    public void setHierarchy(Hierarchy hierarchy){
        this.hierarchy = hierarchy;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean value){
        
        if (value) {
            this.composite.setRedraw(false);
            
            // Reset
            list.removeAll();
            this.view.setHierarchy(Hierarchy.create());
            
            if (groups != null) {
                for (int count : groups){
                    list.add(String.valueOf(count));
                }
            }
            if (hierarchy != null) {
                view.setHierarchy(hierarchy);
            }
            
            this.composite.layout(true);
            this.composite.setRedraw(true);

            // Deactivate buttons
            Button load = this.wizard.getLoadButton();
            if (load != null) load.setEnabled(false);
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(false);
        }
        super.setVisible(value);
    }
}
