/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This layout manages views for criteria and the population model
 *
 * @author Fabian Prasser
 */
public class LayoutPrivacySettings implements ILayout {

    /** View */
    private final ViewPrivacyModels      criteriaView;
    /** View */
    private final ToolItem              buttonAdd;
    /** View */
    private final ToolItem              buttonCross;
    /** View */
    private final ToolItem              buttonConfigure;
    /** View */
    private final ToolItem              buttonUp;
    /** View */
    private final ToolItem              buttonDown;
    /** View */
    private final ComponentTitledFolder folder; 
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutPrivacySettings(final Composite parent,
                                 final Controller controller) {


        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar("id-80"); //$NON-NLS-1$
        bar.add(Resources.getMessage("CriterionDefinitionView.80"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("add.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        criteriaView.actionAdd();
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.59"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("remove.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        criteriaView.actionRemove();
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.82"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("edit.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        criteriaView.actionConfigure();
                    }
                });

        bar.add(Resources.getMessage("CriterionDefinitionView.57"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("bullet_arrow_up.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        criteriaView.actionPush();
                    }
                });
        
        bar.add(Resources.getMessage("CriterionDefinitionView.58"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("bullet_arrow_down.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        criteriaView.actionPull();
                    }
                });

        // Add view
        folder = new ComponentTitledFolder(parent, controller, bar, null);

        buttonCross = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.59")); //$NON-NLS-1$
        buttonUp = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.57")); //$NON-NLS-1$
        buttonDown = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.58")); //$NON-NLS-1$
        buttonAdd = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.80")); //$NON-NLS-1$
        buttonConfigure = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.82")); //$NON-NLS-1$

        Composite composite1 = folder.createItem(Resources.getMessage("CriterionSelectionDialog.4"), null); //$NON-NLS-1$
        composite1.setLayout(SWTUtil.createGridLayout(1));
        this.criteriaView = new ViewPrivacyModels(composite1, controller, this);

        Composite composite2 = folder.createItem(Resources.getMessage("CriterionSelectionDialog.5"), null); //$NON-NLS-1$
        composite2.setLayout(new FillLayout());
        new ViewPopulationModel(composite2, controller);
        
        Composite composite3 = folder.createItem(Resources.getMessage("CriterionSelectionDialog.10"), null); //$NON-NLS-1$
        composite3.setLayout(new FillLayout());
        new ViewCostBenefitModel(composite3, controller);
        
        // Update buttons
        folder.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                updateButtons();
            }
        });
        
        // Select
        folder.setSelection(0);
    }
    
    /**
     * May be triggered by several views and events
     */
    public void updateButtons() {
        
        if (criteriaView == null || folder.getSelectionIndex() == 1) {
            buttonCross.setEnabled(false);
            buttonDown.setEnabled(false);
            buttonUp.setEnabled(false);
            buttonAdd.setEnabled(false);
            buttonConfigure.setEnabled(false);
            return;
        }
        
        ModelCriterion criterion = criteriaView.getSelectedCriterion();
        boolean enabled = criterion != null;
        buttonCross.setEnabled(enabled);
        buttonConfigure.setEnabled(enabled);
        if (enabled) {
            buttonUp.setEnabled(criterion instanceof ModelExplicitCriterion);
            buttonDown.setEnabled(criterion instanceof ModelExplicitCriterion);
        } else {
            buttonUp.setEnabled(enabled);
            buttonDown.setEnabled(enabled);
        }
        buttonAdd.setEnabled(criteriaView.isAddEnabled());
    }
}