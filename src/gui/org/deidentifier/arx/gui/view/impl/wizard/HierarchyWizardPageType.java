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

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A hierarchy page for choosing the type of builder.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardPageType<T> extends WizardPage {

    /** Var. */
    private final HierarchyWizardModel<T>         model;
    
    /** Var. */
    private Button                                interval;
    
    /** Var. */
    private Button                                order;
    
    /** Var. */
    private Button                                redaction;
    
    /** Var. */
    private IWizardPage                           next;
    
    /** Var. */
    private final HierarchyWizardPageIntervals<T> intervalPage;
    
    /** Var. */
    private final HierarchyWizardPageOrder<T>     orderPage;
    
    /** Var. */
    private final HierarchyWizardPageRedaction<T> redactionPage;
    
    /** Var. */
    private final HierarchyWizard<T>              wizard;
    
    /**
     * Creates a new instance.
     *
     * @param wizard
     * @param model
     * @param intervalPage
     * @param orderPage
     * @param redactionPage
     */
    public HierarchyWizardPageType(final HierarchyWizard<T> wizard,
                                   final HierarchyWizardModel<T> model,
                                   final HierarchyWizardPageIntervals<T> intervalPage,
                                   final HierarchyWizardPageOrder<T> orderPage,
                                   final HierarchyWizardPageRedaction<T> redactionPage) {
        
        super(""); //$NON-NLS-1$
        this.wizard = wizard;
        this.redactionPage = redactionPage;
        this.orderPage = orderPage;
        this.intervalPage = intervalPage;
        this.model = model;
        this.next = intervalPage;
        setTitle("Create a generalization hierarchy");
        setDescription("Specify the type of hierarchy");
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
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillGridData());
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        this.interval = new Button(composite, SWT.RADIO);
        this.interval.setText("Use intervals (for variables with ratio scale)");
        if (!(model.getDataType() instanceof DataTypeWithRatioScale)) {
            this.interval.setEnabled(false);
        }
        
        this.order = new Button(composite, SWT.RADIO);
        this.order.setText("Use ordering (e.g., for variables with ordinal scale)");
        this.order.setEnabled(true);
        
        this.redaction = new Button(composite, SWT.RADIO);
        this.redaction.setText("Use redaction (e.g., for alphanumeric strings) ");
        this.redaction.setEnabled(true);
        
        this.interval.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (interval.getSelection()) {
                    next = intervalPage;
                    model.setType(Type.INTERVAL_BASED);
                }
            }
        });

        this.order.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (order.getSelection()) {
                    next = orderPage;
                    model.setType(Type.ORDER_BASED);
                }
            }
        });

        this.redaction.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (redaction.getSelection()) {
                    next = redactionPage;
                    model.setType(Type.REDACTION_BASED);
                }
            }
        });
        
        interval.setSelection(model.getType() == Type.INTERVAL_BASED);
        order.setSelection(model.getType() == Type.ORDER_BASED);
        redaction.setSelection(model.getType() == Type.REDACTION_BASED);
        
        switch (model.getType()){
        case INTERVAL_BASED:  next = intervalPage;  break;
        case ORDER_BASED:     next = orderPage;     break;
        case REDACTION_BASED: next = redactionPage; break;
        default:
            throw new IllegalStateException("Unknown type of builder");
        }
        
        setControl(composite);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        return next;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean value){
        
        if (value) {
            Button load = this.wizard.getLoadButton();
            if (load != null) load.setEnabled(true);
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(false);
        }
        super.setVisible(value);
    }

    /**
     * Updates the page.
     */
    public void updatePage() {
        interval.setSelection(model.getType() == Type.INTERVAL_BASED);
        order.setSelection(model.getType() == Type.ORDER_BASED);
        redaction.setSelection(model.getType() == Type.REDACTION_BASED);
        switch (model.getType()){
            case INTERVAL_BASED:  next = intervalPage;  break;
            case ORDER_BASED:     next = orderPage;     break;
            case REDACTION_BASED: next = redactionPage; break;
        }
    }
}
