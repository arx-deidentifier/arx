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

import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * A page for configuring the redaction-based builder
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardPageRedaction<T> extends HierarchyWizardPageBuilder<T> {

    /** Var */
    private final HierarchyWizardModelRedaction<T> model;
    /** Var */
    private Button                                 buttonLeftAlign;
    /** Var */
    private Button                                 buttonRightAlign;
    /** Var */
    private Button                                 buttonLeftRedact;
    /** Var */
    private Button                                 buttonRightRedact;
    /** Var */
    private Combo                                  comboPaddingChar;
    /** Var */
    private Combo                                  comboRedactionChar;

    /**
     * Creates a new instance
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
    public void updatePage() {
        buttonLeftAlign.setSelection(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
        buttonRightAlign.setSelection(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
        buttonLeftRedact.setSelection(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
        buttonRightRedact.setSelection(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
        comboPaddingChar.select(indexOf(comboPaddingChar, model.getPaddingCharacter()));
        comboRedactionChar.select(indexOf(comboRedactionChar, model.getRedactionCharacter()));
    }
    
    /**
     * Creates combo items
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
     * Returns the index of the item, or adds it to the combo
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
}
