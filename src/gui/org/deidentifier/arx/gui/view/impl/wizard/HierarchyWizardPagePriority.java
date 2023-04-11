/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2023 Fabian Prasser and contributors
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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelPriority.Priority;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A page for configuring the priority-based builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPagePriority<T> extends HierarchyWizardPageBuilder<T> {

    /** Var. */
    private final HierarchyWizardModelPriority<T>  model;
    
    /** Var. */
    private Button                                 buttonOrderLowestToHighest;
    
    /** Var. */
    private Button                                 buttonOrderHighestToLowest;

    /** Var. */
    private Button                                 buttonFrequencyLowestToHighest;
    
    /** Var. */
    private Button                                 buttonFrequencyHighestToLowest;
    
    /** Var. */
    private Text                                   textMaxLevel;
    
    /**
     * Creates a new instance.
     *
     * @param controller
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPagePriority(Controller controller,
                                        final HierarchyWizard<T> wizard,
                                        final HierarchyWizardModel<T> model, 
                                        final HierarchyWizardPageFinal<T> finalPage) {
        super(wizard, model.getPriorityModel(), finalPage);
        this.model = model.getPriorityModel();
        setTitle(Resources.getMessage("HierarchyWizardPagePriority.0")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPagePriority.1")); //$NON-NLS-1$
        setPageComplete(true);
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        // Base
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        // Frame
        Group group1 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group1.setText(Resources.getMessage("HierarchyWizardPagePriority.2")); //$NON-NLS-1$
        group1.setLayout(SWTUtil.createGridLayout(1, false));
        group1.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        // Max levels
        Label label5 = new Label(group1, SWT.NONE);
        label5.setText(Resources.getMessage("HierarchyWizardPagePriority.7")); //$NON-NLS-1$
        textMaxLevel = new Text(group1, SWT.BORDER);
        textMaxLevel.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textMaxLevel);
        
        // Options
        buttonOrderLowestToHighest = new Button(group1, SWT.RADIO);
        buttonOrderLowestToHighest.setText(Resources.getMessage("HierarchyWizardPagePriority.3")); //$NON-NLS-1$
        buttonOrderHighestToLowest = new Button(group1, SWT.RADIO);
        buttonOrderHighestToLowest.setText(Resources.getMessage("HierarchyWizardPagePriority.4")); //$NON-NLS-1$
        buttonFrequencyLowestToHighest = new Button(group1, SWT.RADIO);
        buttonFrequencyLowestToHighest.setText(Resources.getMessage("HierarchyWizardPagePriority.5")); //$NON-NLS-1$
        buttonFrequencyHighestToLowest = new Button(group1, SWT.RADIO);
        buttonFrequencyHighestToLowest.setText(Resources.getMessage("HierarchyWizardPagePriority.6")); //$NON-NLS-1$
        
        // Events
        buttonOrderLowestToHighest.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonOrderLowestToHighest.getSelection()) {
                    model.setPriority(Priority.ORDER_LOWEST_TO_HIGHEST);
                }
            }
        });
        buttonOrderHighestToLowest.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonOrderHighestToLowest.getSelection()) {
                    model.setPriority(Priority.ORDER_HIGHEST_TO_LOWEST);
                }
            }
        });
        buttonFrequencyLowestToHighest.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonFrequencyLowestToHighest.getSelection()) {
                    model.setPriority(Priority.FREQUENCY_LOWEST_TO_HIGHEST);
                }
            }
        });
        buttonFrequencyHighestToLowest.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonFrequencyHighestToLowest.getSelection()) {
                    model.setPriority(Priority.FREQUENCY_HIGHEST_TO_LOWEST);
                }
            }
        });
        
        updatePage();
        setControl(composite);
    }
    
    @Override
    public boolean isPageComplete() {
        if (textMaxLevel.getText().length() == 0 || !isValidNumber(textMaxLevel.getText())) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        model.setVisible(value);
    }

    @Override
    public void updatePage() {
        buttonOrderLowestToHighest.setSelection(model.getPriority() == Priority.ORDER_LOWEST_TO_HIGHEST);
        buttonOrderHighestToLowest.setSelection(model.getPriority() == Priority.ORDER_HIGHEST_TO_LOWEST);
        buttonFrequencyLowestToHighest.setSelection(model.getPriority() == Priority.FREQUENCY_LOWEST_TO_HIGHEST);
        buttonFrequencyHighestToLowest.setSelection(model.getPriority() == Priority.FREQUENCY_HIGHEST_TO_LOWEST);
        textMaxLevel.setText(String.valueOf(model.getMaxLevels()));
    }

    /**
     * Decorates a text field for domain properties.
     *
     * @param text
     */
    private void decorate(final Text text) {
        final ControlDecoration decoration = new ControlDecoration(text, SWT.RIGHT);
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                if (!isValidNumber(text.getText())) {
                    decoration.setDescriptionText(Resources.getMessage("HierarchyWizardPageRedaction.23")); //$NON-NLS-1$
                    Image image = FieldDecorationRegistry.getDefault()
                          .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                          .getImage();
                    decoration.setImage(image);
                    decoration.show();
                } else {
                    decoration.hide();
                    model.setMaxLevels(text.getText().length() == 0 ? 10 : Integer.valueOf(text.getText()));
                }
                setPageComplete(isPageComplete());
            }
        });
    }
    
    /**
     * Returns whether a valid number has been entered.
     *
     * @param text
     * @return
     */
    private boolean isValidNumber(String text) {
        if (text.length() == 0) {
            return true;
        } else {
            try {
                int value = Integer.parseInt(text);
                return value > 0d;
            } catch (Exception e){
                return false;
            }
        }
    }
}
