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

import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;
import org.deidentifier.arx.gui.Controller;
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
        setTitle("Create a hierarchy by redacting characters");
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
        
        Group group1 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group1.setText("Alignment");
        group1.setLayout(SWTUtil.createGridLayout(1, false));
        group1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        buttonLeftAlign = new Button(group1, SWT.RADIO);
        buttonLeftAlign.setText("Align items to the left");
        buttonRightAlign = new Button(group1, SWT.RADIO);
        buttonRightAlign.setText("Align items to the right");
    
        Group group2 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group2.setText("Redaction");
        group2.setLayout(SWTUtil.createGridLayout(1, false));
        group2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        buttonLeftRedact = new Button(group2, SWT.RADIO);
        buttonLeftRedact.setText("Redact characters left to right");
        buttonRightRedact = new Button(group2, SWT.RADIO);
        buttonRightRedact.setText("Redact characters right to left");
    
        Group group3 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group3.setText("Characters");
        group3.setLayout(SWTUtil.createGridLayout(2, false));
        group3.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label1 = new Label(group3, SWT.NONE);
        label1.setText("Padding character");
        comboPaddingChar = new Combo(group3, SWT.READ_ONLY);
        comboPaddingChar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label2 = new Label(group3, SWT.NONE);
        label2.setText("Redaction character");
        comboRedactionChar = new Combo(group3, SWT.READ_ONLY);
        comboRedactionChar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
    
        createItems(comboPaddingChar, true);
        createItems(comboRedactionChar, false);

        Group group4 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group4.setText("Domain properties");
        GridLayout layout = SWTUtil.createGridLayout(6, false);
        layout.horizontalSpacing = 10;
        group4.setLayout(layout);
        group4.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Label label3 = new Label(group4, SWT.NONE);
        
        label3.setText("Domain size");
        textDomainSize = new Text(group4, SWT.BORDER);
        textDomainSize.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textDomainSize);
        
        Label label4 = new Label(group4, SWT.NONE);
        label4.setText("Alphabet size");
        textAlphabetSize = new Text(group4, SWT.BORDER);
        textAlphabetSize.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        decorate(textAlphabetSize);
        
        Label label5 = new Label(group4, SWT.NONE);
        label5.setText("Max. characters");
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
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
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardPageBuilder#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        model.setVisible(value);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardPageBuilder#updatePage()
     */
    @Override
    public void updatePage() {
        textMaximalLength.setText(model.getMaxValueLength() == null ? "" : String.valueOf(model.getMaxValueLength()));
        textAlphabetSize.setText(model.getAlphabetSize() == null ? "" : String.valueOf(model.getAlphabetSize()));
        textDomainSize.setText(model.getDomainSize() == null ? "" : String.valueOf(model.getDomainSize()));
        buttonLeftAlign.setSelection(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
        buttonRightAlign.setSelection(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
        buttonLeftRedact.setSelection(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
        buttonRightRedact.setSelection(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
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
        if (padding) combo.add("( )");
        combo.add("(*)");
        combo.add("(x)");
        combo.add("(#)");
        combo.add("(-)");
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
                    decoration.setDescriptionText("Not a valid positive number");
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
        combo.add("("+String.valueOf(value)+")");
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
