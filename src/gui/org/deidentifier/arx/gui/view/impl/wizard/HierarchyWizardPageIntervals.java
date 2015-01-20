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

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A page for configuring the interval-based builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPageIntervals<T> extends HierarchyWizardPageBuilder<T> {

    /** Var. */
    private final HierarchyWizardModelIntervals<T> model;
    
    /** Var. */
    private HierarchyWizardEditor<T> editor;
    
    /**
     * Creates a new instance.
     *
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPageIntervals(final HierarchyWizard<T> wizard,
                                        final HierarchyWizardModel<T> model, 
                                        final HierarchyWizardPageFinal<T> finalPage) {
        super(wizard, model.getIntervalModel(), finalPage);
        this.model = model.getIntervalModel();
        setTitle("Create a hierarchy by defining intervals");
        setDescription("Specify the parameters");
        setPageComplete(true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        editor =  new HierarchyWizardEditor<T>(composite,  (HierarchyWizardModelGrouping<T>) model);
        editor.setLayoutData(SWTUtil.createFillGridData());

        setControl(composite);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardPageBuilder#updatePage()
     */
    @Override
    public void updatePage() {
        model.update();
        if (editor != null) editor.setFunction(model.getDefaultFunction());
    }
}
