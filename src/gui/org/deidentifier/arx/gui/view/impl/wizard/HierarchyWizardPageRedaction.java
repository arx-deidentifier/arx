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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A page for configuring the redaction-based builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPageRedaction<T> extends HierarchyWizardPageBuilder<T> {

    /** Var. */
    private final HierarchyWizardModelRedaction<T> model;
    
    /** Var. */
    private Button                                 buttonLeftAlign;
    
    /** Var. */
    private Button                                 buttonRightAlign;
    
    /** Var. */
    private Button                                 buttonLeftRedact;
    
    /** Var. */
    private Button                                 buttonRightRedact;
    
    /** Var. */
    private Combo                                  comboPaddingChar;
    
    /** Var. */
    private Combo                                  comboRedactionChar;
    
    /** Var. */
    private Text                                   textDomainSize;
    
    /** Var. */
    private Text                                   textAlphabetSize;
    
    /** Var. */
    private Text                                   textMaximalLength;

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPageRedaction(Controller controller,
                                        final HierarchyWizard<T> wizard,
                                        final HierarchyWizardModel<T> model, 
                                        final HierarchyWizardPageFinal<T> finalPage) {
        super(wizard, model.getRedactionModel(), finalPage);
        this.model = model.getRedactionModel();
        setTitle(Resources.getMessage("HierarchyWizardPageRedaction.0")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageRedaction.1")); //$NON-NLS-1$
        setPageComplete(true);
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        Group group1 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group1.setText(Resources.getMessage("HierarchyWizardPageRedaction.2")); //$NON-NLS-1$
        group1.setLayout(SWTUtil.createGridLayout(1, false));
        group1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        buttonLeftAlign = new Button(group1, SWT.RADIO);
        buttonLeftAlign.setText(Resources.getMessage("HierarchyWizardPageRedaction.3")); //$NON-NLS-1$
        buttonRightAlign = new Button(group1, SWT.RADIO);
        buttonRightAlign.setText(Resources.getMessage("HierarchyWizardPageRedaction.4")); //$NON-NLS-1$
    
        Group group2 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group2.setText(Resources.getMessage("HierarchyWizardPageRedaction.5")); //$NON-NLS-1$
        group2.setLayout(SWTUtil.createGridLayout(1, false));
        group2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        buttonLeftRedact = new Button(group2, SWT.RADIO);
        buttonLeftRedact.setText(Resources.getMessage("HierarchyWizardPageRedaction.6")); //$NON-NLS-1$
        buttonRightRedact = new Button(group2, SWT.RADIO);
        buttonRightRedact.setText(Resources.getMessage("HierarchyWizardPageRedaction.7")); //$NON-NLS-1$
    
        Group group3 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group3.setText(Resources.getMessage("HierarchyWizardPageRedaction.8")); //$NON-NLS-1$
        group3.setLayout(SWTUtil.createGridLayout(2, false));
        group3.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label1 = new Label(group3, SWT.NONE);
        label1.setText(Resources.getMessage("HierarchyWizardPageRedaction.9")); //$NON-NLS-1$
        comboPaddingChar = new Combo(group3, SWT.READ_ONLY);
        comboPaddingChar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label2 = new Label(group3, SWT.NONE);
        label2.setText(Resources.getMessage("HierarchyWizardPageRedaction.10")); //$NON-NLS-1$
        comboRedactionChar = new Combo(group3, SWT.READ_ONLY);
        comboRedactionChar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
    
        createItems(comboPaddingChar, true);
        createItems(comboRedactionChar, false);

        Group group4 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group4.setText(Resources.getMessage("HierarchyWizardPageRedaction.11")); //$NON-NLS-1$
        GridLayout layout = SWTUtil.createGridLayout(6, false);
        layout.horizontalSpacing = 10;
        group4.setLayout(layout);
        group4.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label3 = new Label(group4, SWT.NONE);
        
        label3.setText(Resources.getMessage("HierarchyWizardPageRedaction.12")); //$NON-NLS-1$
        textDomainSize = new Text(group4, SWT.BORDER);
        textDomainSize.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textDomainSize);
        
        Label label4 = new Label(group4, SWT.NONE);
        label4.setText(Resources.getMessage("HierarchyWizardPageRedaction.13")); //$NON-NLS-1$
        textAlphabetSize = new Text(group4, SWT.BORDER);
        textAlphabetSize.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textAlphabetSize);
        
        Label label5 = new Label(group4, SWT.NONE);
        label5.setText(Resources.getMessage("HierarchyWizardPageRedaction.14")); //$NON-NLS-1$
        textMaximalLength = new Text(group4, SWT.BORDER);
        textMaximalLength.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textMaximalLength);
        
        buttonLeftAlign.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonLeftAlign.getSelection()) {
                    model.setAlignmentOrder(Order.LEFT_TO_RIGHT);
                }
            }
        });
        buttonRightAlign.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonRightAlign.getSelection()) {
                    model.setAlignmentOrder(Order.RIGHT_TO_LEFT);
                }
            }
        });
    
        buttonLeftRedact.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonLeftRedact.getSelection()) {
                    model.setRedactionOrder(Order.LEFT_TO_RIGHT);
                }
            }
        });
        buttonRightRedact.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (buttonRightRedact.getSelection()) {
                    model.setRedactionOrder(Order.RIGHT_TO_LEFT);
                }
            }
        });
        
        comboPaddingChar.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                int index = comboPaddingChar.getSelectionIndex();
                if (index>=0){
                    model.setPaddingCharacter(comboPaddingChar.getItem(index).toCharArray()[1]);
                }
            }
        });
        
        comboRedactionChar.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                int index = comboRedactionChar.getSelectionIndex();
                if (index>=0){
                    model.setRedactionCharacter(comboRedactionChar.getItem(index).toCharArray()[1]);
                }
            }
        });
        
        updatePage();
        setControl(composite);
    }
    
    @Override
    public boolean isPageComplete() {
        
        String alphabet = textAlphabetSize.getText();
        String domain = textDomainSize.getText();
        String length = textMaximalLength.getText();
        
        if (alphabet.length() == 0 && domain.length() == 0 && length.length() == 0) {
            return true;
        }
        
        if (alphabet.length() != 0 && domain.length() != 0 && length.length() != 0) {
            return isValidNumber(alphabet) && isValidNumber(domain) && isValidNumber(length);
        }
        
        if (alphabet.length() != 0 && length.length() != 0) {
            return isValidNumber(alphabet) && isValidNumber(length);
        }
        
        if (domain.length() != 0 && length.length() != 0) {
            return isValidNumber(domain) && isValidNumber(length);
        }
        
        return false;
    }
    
    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        model.setVisible(value);
    }
    
    @Override
    public void updatePage() {
        textMaximalLength.setText(model.getMaxValueLength() == null ? "" : String.valueOf(model.getMaxValueLength())); //$NON-NLS-1$
        textAlphabetSize.setText(model.getAlphabetSize() == null ? "" : String.valueOf(model.getAlphabetSize())); //$NON-NLS-1$
        textDomainSize.setText(model.getDomainSize() == null ? "" : String.valueOf(model.getDomainSize())); //$NON-NLS-1$
        buttonLeftAlign.setSelection(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
        buttonRightAlign.setSelection(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
        buttonLeftRedact.setSelection(model.getRedactionOrder() == Order.LEFT_TO_RIGHT);
        buttonRightRedact.setSelection(model.getRedactionOrder() == Order.RIGHT_TO_LEFT);
        comboPaddingChar.select(indexOf(comboPaddingChar, model.getPaddingCharacter()));
        comboRedactionChar.select(indexOf(comboRedactionChar, model.getRedactionCharacter()));
    }
    
    /**
     * Creates combo items.
     *
     * @param combo
     * @param padding
     */
    private void createItems(Combo combo, boolean padding){
        if (padding) {
            combo.add("( )"); //$NON-NLS-1$
            combo.add("(0)"); //$NON-NLS-1$
        }
        combo.add("(*)"); //$NON-NLS-1$
        combo.add("(x)"); //$NON-NLS-1$
        combo.add("(#)"); //$NON-NLS-1$
        combo.add("(-)"); //$NON-NLS-1$
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
                    if (text == textAlphabetSize) {
                        model.setAlphabetSize(text.getText().length() == 0 ? null : Integer.valueOf(text.getText()));
                    } else if (text == textDomainSize) {
                        model.setDomainSize(text.getText().length() == 0 ? null : Integer.valueOf(text.getText()));
                    } else if (text == textMaximalLength) {
                        model.setMaxValueLength(text.getText().length() == 0 ? null : Integer.valueOf(text.getText()));
                    }
                }
                setPageComplete(isPageComplete());
            }
        });
    }
    

    /**
     * Returns the index of the item, or adds it to the combo.
     *
     * @param combo
     * @param value
     * @return
     */
    private int indexOf(Combo combo, char value){
        for (int i=0; i < combo.getItems().length; i++) {
            if (combo.getItem(i).toCharArray()[1]==value) {
                return i;
            }
        }
        combo.add("("+String.valueOf(value)+")"); //$NON-NLS-1$ //$NON-NLS-2$
        return combo.getItemCount()-1;
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
