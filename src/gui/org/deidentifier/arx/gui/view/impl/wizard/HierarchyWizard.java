/*
 * ARX: Powerful Data Anonymization
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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.wizard.ARXWizardDialog.ARXWizardButton;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardResult;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;

/**
 * This class implements a wizard for generalization hierarchies
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizard<T> extends ARXWizard<HierarchyWizardResult<T>> {
    
    /**
     * Result of the wizard
     * @author Fabian Prasser
     *
     * @param <T>
     */
    public static class HierarchyWizardResult<T> {

        public final Hierarchy hierarchy;
        public final HierarchyBuilder<T> builder;
        public HierarchyWizardResult(Hierarchy hierarchy,
                                     HierarchyBuilder<T> builder) {
            this.hierarchy = hierarchy;
            this.builder = builder;
        }
    }
    
    /**
     * Updateable part of the wizard
     * @author Fabian Prasser
     */
    public static interface HierarchyWizardView {
        /** Update*/
        public void update();
    }
    
    /** Var */
    private HierarchyWizardModel<T>   model;
    /** Var */
    private final Controller                controller;
    /** Var */
    private final ARXWizardButton           buttonLoad;
    /** Var */
    private final ARXWizardButton           buttonSave;
    /** Var */
    private HierarchyWizardPageIntervals<T> pageIntervals;
    /** Var */
    private HierarchyWizardPageOrder<T>     pageOrder;
    /** Var */
    private HierarchyWizardPageRedaction<T> pageRedaction;
    /** Var */
    private HierarchyWizardPageFinal<T>     pageFinal;
    /** Var */
    private HierarchyWizardPageType<T>      pageType;

    /**
     * Creates a new instance
     * @param controller
     * @param attribute
     * @param datatype
     * @param items
     */
    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           final DataType<T> datatype,
                           final String[] items) {
        this(controller, attribute, null, datatype, items);
    }
    
    /**
     * Creates a new instance
     * @param controller
     * @param attribute
     * @param builder
     * @param datatype
     * @param items
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizard(final Controller controller,
                           final String attribute,
                           final HierarchyBuilder<?> builder,
                           final DataType<T> datatype,
                           final String[] items) {
        super(new Point(800, 400));
        
        // Store
        this.model = new HierarchyWizardModel<T>(datatype, items);
        this.controller = controller;
        
        // Parse given builder, if needed
        try {
            if (builder != null){
                this.model.parse((HierarchyBuilder<T>)builder);
            }
        } catch (Exception e){ 
            /* Die silently, and recover*/
            this.model = new HierarchyWizardModel<T>(datatype, items);
        }
        
        // Initialize window
        this.setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        this.setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("hierarchy.png"))); //$NON-NLS-1$
        
        // Initialize buttons
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
        
        ARXWizardButton help = new ARXWizardButton("Help...", new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                help();
            }
        });
        
        this.setButtons(help, this.buttonLoad, this.buttonSave);
        
        // Initialize pages
        pageFinal = new HierarchyWizardPageFinal<T>(this);
        if (model.getIntervalModel() != null){
            pageIntervals = new HierarchyWizardPageIntervals<T>(this, model, pageFinal);
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
        return getDialog().getCurrentPage() instanceof HierarchyWizardPageFinal;
    }

    /**
     * Returns the created builder
     * @return
     */
    public HierarchyWizardResult<T> getResult(){
        try {
            return new HierarchyWizardResult<T>(model.getHierarchy(), model.getBuilder(true));
        } catch (Exception e){
            return null;
        }
    }
    
    /**
     * Shows the help dialog
     */
    private void help() {
        controller.actionShowHelpDialog("id-51");
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
            model.parse(builder);
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

        // Save
        try {
            // Select
            HierarchyBuilder<T> builder = null;
            if (getDialog().getCurrentPage()  instanceof HierarchyWizardPageOrder){
                builder = model.getOrderModel().getBuilder(true);
            } else if (getDialog().getCurrentPage()  instanceof HierarchyWizardPageIntervals){
                builder = model.getIntervalModel().getBuilder(true);
            } else if (getDialog().getCurrentPage()  instanceof HierarchyWizardPageRedaction){
                builder = model.getRedactionModel().getBuilder(true);
            }

            // Save
            builder.save(file);
        } catch (Exception e){
            e.printStackTrace();
            controller.actionShowInfoDialog(getShell(), ERROR_HEADER, ERROR_TEXT+e.getMessage());
            return;
        }
    }
    /**
     * Returns the load button
     * @return
     */
    protected Button getLoadButton(){
        return super.getButton(buttonLoad);
    }

    /**
     * Returns the load button
     * @return
     */
    protected Button getSaveButton(){
        return super.getButton(buttonSave);
    }
}
