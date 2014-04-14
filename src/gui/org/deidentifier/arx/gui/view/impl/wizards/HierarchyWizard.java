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

package org.deidentifier.arx.gui.view.impl.wizards;

import java.util.Arrays;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.wizards.ARXWizardDialog.ARXWizardButton;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

public class HierarchyWizard<T> extends Wizard implements IWizard {

    private final HierarchyWizardModel<T>   model;
    private final Controller                controller;
    private final ARXWizardButton           buttonLoad;
    private final ARXWizardButton           buttonSave;
    private ARXWizardDialog                 dialog;
    private HierarchyWizardPageIntervals<T> pageIntervals;
    private HierarchyWizardPageOrder<T>     pageOrder;
    private HierarchyWizardPageRedaction<T> pageRedaction;
    private HierarchyWizardPageFinal<T>     pageFinal;
    private HierarchyWizardPageType<T>      pageType;


    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           final DataType<T> datatype,
                           final String[] items) {
        this(controller, attribute, null, datatype, items);
    }
    
    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           HierarchyBuilder<?> builder,
                           final DataType<T> datatype,
                           final String[] items) {
        this.model = new HierarchyWizardModel<T>(datatype, items, builder);
        this.controller = controller;
        this.setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        this.setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("wizard.png"))); //$NON-NLS-1$
        this.buttonLoad = new ARXWizardButton("Load...", new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                load();
            }
        });

        this.buttonSave = new ARXWizardButton("Save...", new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                save();
            }
        });
        
        // Initialize pages
        pageFinal = new HierarchyWizardPageFinal<T>(this, model);
        if (model.getIntervalModel() != null){
            pageIntervals = new HierarchyWizardPageIntervals<T>(controller, this, model, pageFinal);
        } else {
            pageIntervals = null;
        }
        pageOrder = new HierarchyWizardPageOrder<T>(controller, this, model, pageFinal);
        pageRedaction = new HierarchyWizardPageRedaction<T>(controller, this, model, pageFinal);
        pageType = new HierarchyWizardPageType<T>(this, model, pageIntervals, pageOrder, pageRedaction);
    }
    
    @Override
    public void addPages() {
        
        addPage(pageType);
        addPage(pageOrder);
        addPage(pageRedaction);
        addPage(pageFinal);
        if (pageIntervals != null) {
            addPage(pageIntervals);
        }
    }

    @Override
    public boolean canFinish() {
        return dialog.getCurrentPage() instanceof HierarchyWizardPageFinal;
    }

    /**
     * Returns the created builder
     * @return
     */
    public HierarchyBuilder<T> getBuilder() {
        return model.getBuilder();
    }
    
    /**
     * Returns the created hierarchy
     * @return
     */
    public Hierarchy getHierarchy() {
        return model.getHierarchy();
    }

    /**
     * Returns the load button
     * @return
     */
    public Button getLoadButton(){
        if (dialog != null) {
            return dialog.getButton(buttonLoad);
        } else {
            return null;
        }
    }
    
    /**
     * Returns the load button
     * @return
     */
    public Button getSaveButton(){
        if (dialog != null) {
            return dialog.getButton(buttonSave);
        } else {
            return null;
        }
    }
    
    public boolean open(final Shell shell) {
        
        this.dialog = new ARXWizardDialog(shell, this, Arrays.asList(new ARXWizardButton[]{buttonLoad, buttonSave}));
        this.dialog.setPageSize(800, 400);
        return dialog.open() == 0;
    }
    
    @Override
    public boolean performFinish() {
        return true;
    }

    /**
     * Loads a specification
     */
    private void load(){

        final String ERROR_HEADER = "Error loading hierarchy specification";
        final String ERROR_TEXT = "Unknown error: ";
        final String ERROR_RATIO_TEXT = "Intervals can only be used for data types with ratio scales";
        final String ERROR_TYPE_TEXT = "Incompatible data types";
        final String ERROR_APPLY_TEXT = "Cannot apply specification: ";
        
        // Dialog
        String file = controller.actionShowOpenFileDialog(getShell(), "*.ahs");
        if (file == null) return;

        // Load
        HierarchyBuilder<T> builder = null;
        try {
            builder = HierarchyBuilder.create(file);
        } catch (Exception e){
            controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_TEXT+e.getMessage());
            return;
        }
        
        // Checks
        if (builder == null) return;
        else if (builder.getType() == Type.INTERVAL_BASED) {
            if (!(model.getDataType() instanceof DataTypeWithRatioScale)) {
                controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_RATIO_TEXT);
                return;
            } else if (!((HierarchyBuilderIntervalBased<?>)builder).getDataType().equals(model.getDataType())){
                controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_TYPE_TEXT);
                return;
            }
        } else if (builder.getType() == Type.ORDER_BASED) {
            if (!((HierarchyBuilderOrderBased<?>)builder).getDataType().equals(model.getDataType())){
                controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_TYPE_TEXT);
                return;
            }
        }
        
        // Select
        try {
            model.setSpecification(builder);
        } catch (Exception e){
            controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_APPLY_TEXT+e.getMessage());
            return;
        }
        
        // Update views
        switch (builder.getType()) {
        case INTERVAL_BASED:
            this.pageIntervals.updatePage();
            this.model.setType(Type.INTERVAL_BASED);
            this.pageType.updatePage();
            this.getContainer().showPage(pageIntervals);
            break;
        case ORDER_BASED:
            this.pageOrder.updatePage();
            this.model.setType(Type.ORDER_BASED);
            this.pageType.updatePage();
            this.getContainer().showPage(pageOrder);
            break;
        case REDACTION_BASED:
            this.pageRedaction.updatePage();
            this.model.setType(Type.REDACTION_BASED);
            this.pageType.updatePage();
            this.getContainer().showPage(pageRedaction);
            break;
        }
    }

    /**
     * Saves the current specification
     */
    private void save(){
        
        final String ERROR_HEADER = "Error saving hierarchy specification";
        final String ERROR_TEXT = "Unknown error: ";
        
        // Dialog
        String file = controller.actionShowSaveFileDialog(getShell(), "*.ahs");
        if (file == null) return;
        
        // Select
        HierarchyBuilder<T> builder = null;
        if (dialog.getCurrentPage()  instanceof HierarchyWizardPageOrder){
            builder = model.getOrderModel().getBuilder();
        } else if (dialog.getCurrentPage()  instanceof HierarchyWizardPageIntervals){
            builder = model.getIntervalModel().getBuilder();
        } else if (dialog.getCurrentPage()  instanceof HierarchyWizardPageRedaction){
            builder = model.getRedactionModel().getBuilder();
        }
        
        // Save
        try {
            builder.save(file);
        } catch (Exception e){
            controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_TEXT+e.getMessage());
            return;
        }
    }
}
