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

import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.gui.resources.Resources;
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
    private Button                                date;

    /** Var. */
    private IWizardPage                           next;

    /** Var. */
    private final HierarchyWizardPageIntervals<T> intervalPage;

    /** Var. */
    private final HierarchyWizardPageOrder<T>     orderPage;

    /** Var. */
    private final HierarchyWizardPageRedaction<T> redactionPage;

    /** Var. */
    private final HierarchyWizardPageDate         datePage;

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
     * @param datePage
     */
    public HierarchyWizardPageType(final HierarchyWizard<T> wizard,
                                   final HierarchyWizardModel<T> model,
                                   final HierarchyWizardPageIntervals<T> intervalPage,
                                   final HierarchyWizardPageOrder<T> orderPage,
                                   final HierarchyWizardPageRedaction<T> redactionPage,
                                   final HierarchyWizardPageDate datePage) {
        
        super(""); //$NON-NLS-1$
        this.wizard = wizard;
        this.redactionPage = redactionPage;
        this.orderPage = orderPage;
        this.intervalPage = intervalPage;
        this.datePage = datePage;
        this.model = model;
        this.next = intervalPage;
        setTitle(Resources.getMessage("HierarchyWizardPageType.0")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageType.1")); //$NON-NLS-1$
        setPageComplete(true);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillGridData());
        composite.setLayout(SWTUtil.createGridLayout(1, false));

        this.date = new Button(composite, SWT.RADIO);
        this.date.setText(Resources.getMessage("HierarchyWizardPageType.5")); //$NON-NLS-1$
        this.date.setEnabled(model.getDateModel() != null);
        
        this.interval = new Button(composite, SWT.RADIO);
        this.interval.setText(Resources.getMessage("HierarchyWizardPageType.2")); //$NON-NLS-1$
        this.interval.setEnabled(model.getIntervalModel() != null);
        
        this.order = new Button(composite, SWT.RADIO);
        this.order.setText(Resources.getMessage("HierarchyWizardPageType.3")); //$NON-NLS-1$
        this.order.setEnabled(true);
        
        this.redaction = new Button(composite, SWT.RADIO);
        this.redaction.setText(Resources.getMessage("HierarchyWizardPageType.4")); //$NON-NLS-1$
        this.redaction.setEnabled(true);

        this.date.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                if (date.getSelection()) {
                    next = datePage;
                    model.setType(Type.DATE_BASED);
                }
            }
        });
        
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
        
        date.setSelection(model.getType() == Type.DATE_BASED);
        interval.setSelection(model.getType() == Type.INTERVAL_BASED);
        order.setSelection(model.getType() == Type.ORDER_BASED);
        redaction.setSelection(model.getType() == Type.REDACTION_BASED);
        
        switch (model.getType()){
        case DATE_BASED:      next = datePage;      break;
        case INTERVAL_BASED:  next = intervalPage;  break;
        case ORDER_BASED:     next = orderPage;     break;
        case REDACTION_BASED: next = redactionPage; break;
        default:
            throw new IllegalStateException("Unknown type of builder"); //$NON-NLS-1$
        }
        
        setControl(composite);
    }

    @Override
    public IWizardPage getNextPage() {
        return next;
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }
    
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
        date.setSelection(model.getType() == Type.DATE_BASED);
        switch (model.getType()){
            case INTERVAL_BASED:  next = intervalPage;  break;
            case ORDER_BASED:     next = orderPage;     break;
            case REDACTION_BASED: next = redactionPage; break;
            case DATE_BASED:      next = datePage;      break;
        }
    }
}
