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

package org.deidentifier.arx.gui.view.impl.risk;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Base class for layouts in this perspective
 * @author Fabian Prasser
 */
public class LayoutRisksAbstract implements ILayout, IView {

    /** Model */
    private final Map<Integer, ViewRisks<?>> views   = new HashMap<Integer, ViewRisks<?>>();
    /** Model */
    private final Map<Composite, String>     helpids = new HashMap<Composite, String>();
    /** Model */
    private final boolean                    isInput;
    /** Model */
    private final boolean                    isTop;
    /** Model */
    protected Model                          model;
    
    /** Controller */
    private final Controller                 controller;

    /** View */
    private final Image                      imageEnabled;
    /** View */
    private final Image                      imageDisabled;
    /** View */
    private final ToolItem                   buttonSubset;
    /** View */
    private final ToolItem                   buttonEnable;

    /** View */
    private final ComponentTitledFolder      folder;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param input
     * @param top
     */
    public LayoutRisksAbstract(Composite parent, final Controller controller, boolean input, boolean top) {
        
        this.isInput = input;
        this.isTop = top;
        this.controller = controller;
        this.imageEnabled = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        this.imageDisabled = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$
        
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.MODEL, this);
        
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(isTop ? "help.risk.overview" : "help.risk.overview", helpids); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (isTop) {
            bar.add(Resources.getMessage("DataView.3"), //$NON-NLS-1$ 
                    controller.getResources().getManagedImage("sort_subset.png"), //$NON-NLS-1$
                    true,
                    new Runnable() {
                        @Override
                        public void run() {
                            controller.actionDataToggleSubset();
                        }
                    });
        }
        
        bar.add(Resources.getMessage("StatisticsView.3"), //$NON-NLS-1$ 
                imageEnabled,
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        updateEnableImage();
                        pushEnableState();
                    }
                });
        
        this.folder = new ComponentTitledFolder(parent, controller, bar, null);
        this.folder.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                pullEnableState();
            }
        });

        this.buttonEnable = folder.getButtonItem(Resources.getMessage("StatisticsView.3")); //$NON-NLS-1$
        this.buttonSubset = folder.getButtonItem(Resources.getMessage("DataView.3")); //$NON-NLS-1$
        if (this.buttonSubset != null) {
            this.buttonSubset.setEnabled(false);
        }
    }
    

    /**
     * Adds a selection listener.
     *
     * @param listener
     */
    public void addSelectionListener(final SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the selected type of view
     */
    public ViewRiskType getSelectedViewType() {
        return this.getViewForSelectionIndex(getSelectionIndex()).getViewType();
    }

    /**
     * Returns the selection index.
     *
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    /**
     * Returns the according view type
     * @param index
     * @return
     */
    public ViewRisks<?> getViewForSelectionIndex(final int index) {
        return this.views.get(index);
    }

    /**
     * Returns the according view type
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <U> U getViewForType(final Class<U> clazz) {
        for (ViewRisks<?> view : this.views.values()) {
            if (view.getClass().equals(clazz)) {
                return (U)view;
            }
        }
        return null;
    }
    
    @Override
    public void reset() {
        if (buttonSubset != null) {
            buttonSubset.setEnabled(false);
        }
        buttonEnable.setEnabled(false);
    }

    /**
     * Sets the selection index.
     *
     * @param index
     */
    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
        pullEnableState();
    }
    
    @Override
    public void update(ModelEvent event) {
        

        // Update model
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
            pullEnableState();
        }
        
        // Update subset
        if (this.isTop) {
            updateButtonSubset(event);
        }
    }

    /**
     * Pulls the state from the model
     * @param event
     */
    private void pullEnableState() {
        
        if (model == null) {
            return;
        }
        
        ViewRisks<?> view = getViewForSelectionIndex(this.getSelectionIndex());
        if (view == null) {
            buttonEnable.setEnabled(false);
            buttonEnable.setSelection(true);
            updateEnableImage();
        } else {
            boolean enabled;
            if (this.isInput) {
                enabled = model.getRiskModel().isViewEnabledForInput(view.getViewType());
                buttonEnable.setSelection(enabled);
                if (view.isEnabled() != enabled) {
                    view.setEnabled(enabled);
                } 
            } else {
                enabled = model.getRiskModel().isViewEnabledForOutput(view.getViewType());
                buttonEnable.setSelection(enabled);
                if (view.isEnabled() != enabled) {
                    view.setEnabled(enabled);
                }
            }
            buttonEnable.setEnabled(true);
            updateEnableImage();
        }
    }

    /**
     * Pushes the state into the model
     */
    private void pushEnableState() {

        if (model == null) {
            return;
        }
        
        ViewRisks<?> view = getViewForSelectionIndex(this.getSelectionIndex());
        if (view != null) {
            boolean enabled = buttonEnable.getSelection();
            if (this.isInput) {
                if (enabled != model.getRiskModel().isViewEnabledForInput(view.getViewType())) {
                    model.getRiskModel().setViewEnabledForInput(view.getViewType(), enabled);
                    view.setEnabled(enabled);
                } 
            } else {
                if (enabled != model.getRiskModel().isViewEnabledForOutput(view.getViewType())) {
                    model.getRiskModel().setViewEnabledForOutput(view.getViewType(), enabled);
                    view.setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Updates the subset button
     * @param event
     */
    private void updateButtonSubset(ModelEvent event) {

        // Enable/Disable sort button
        if (event.part == ModelPart.OUTPUT ||
            event.part == ModelPart.INPUT ||
            event.part == ModelPart.SELECTED_VIEW_CONFIG) {
            
            if (model != null && model.getOutput() != null){
                buttonSubset.setEnabled(true);
            } else {
                buttonSubset.setEnabled(false);
            }
        }
        
        if (event.part == ModelPart.SELECTED_VIEW_CONFIG) {          
            buttonSubset.setSelection(model.getViewConfig().isSubset());
        }
    }

    /**
     * Action for the according button
     */
    private void updateEnableImage(){
        if (buttonEnable.getSelection()) {
            buttonEnable.setImage(imageEnabled);
        } else {
            buttonEnable.setImage(imageDisabled);
        }
    }
    
    /**
     * Creates a new tab
     * @param title
     * @return
     */
    protected Composite createTab(String title, String helpid) {
        final Composite item = folder.createItem(title, null); 
        item.setLayout(new FillLayout());
        helpids.put(item, helpid);
        return item;
    }


    /**
     * Registers a new view
     * @param index
     * @param view
     */
    protected void registerView(int index, ViewRisks<?> view) {
        this.views.put(index, view);
    }
}
