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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Layouts the visualization and allows enabling/disabling them.
 *
 * @author Fabian Prasser
 */
public class LayoutUtilityStatistics implements ILayout, IView {

    /** Constant */
    private static final String         TAB_SUMMARY            = Resources.getMessage("StatisticsView.6"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_DISTRIBUTION       = Resources.getMessage("StatisticsView.0"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_DISTRIBUTION_TABLE = Resources.getMessage("StatisticsView.4"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_CONTINGENCY        = Resources.getMessage("StatisticsView.1"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_CONTINGENCY_TABLE  = Resources.getMessage("StatisticsView.5"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_CLASSES_TABLE      = Resources.getMessage("StatisticsView.7"); //$NON-NLS-1$

    /** Constant */
    private static final String         TAB_PROPERTIES         = Resources.getMessage("StatisticsView.2"); //$NON-NLS-1$

    /**  View */
    private final ComponentTitledFolder folder;
    
    /**  View */
    private final ToolItem              enable;
    
    /**  View */
    private final Image                 enabled;
    
    /**  View */
    private final Image                 disabled;
    
    /** Controller */
    private final Controller            controller;

    /** Model */
    private Model                       model                  = null;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public LayoutUtilityStatistics(final Composite parent,
                            final Controller controller,
                            final ModelPart target,
                            final ModelPart reset) {

        this.enabled = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        this.disabled = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$
        this.controller = controller;
        
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_UTILITY_VISUALIZATION, this);

        // Create enable/disable button
        final String label = Resources.getMessage("StatisticsView.3"); //$NON-NLS-1$
        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-50"); //$NON-NLS-1$
        bar.add(label, disabled, true, new Runnable() { @Override public void run() {
            toggleEnabled();
            toggleImage(); 
        }});
        
        // Create the tab folder
        folder = new ComponentTitledFolder(parent, controller, bar, null);
        final Composite item0 = folder.createItem(TAB_SUMMARY, null);
        item0.setLayout(new FillLayout());
        final Composite item1 = folder.createItem(TAB_DISTRIBUTION, null);
        item1.setLayout(new FillLayout());
        final Composite item1b = folder.createItem(TAB_DISTRIBUTION_TABLE, null);
        item1b.setLayout(new FillLayout());
        final Composite item2 = folder.createItem(TAB_CONTINGENCY, null);
        item2.setLayout(new FillLayout());
        final Composite item2b = folder.createItem(TAB_CONTINGENCY_TABLE, null);
        item2b.setLayout(new FillLayout());
        final Composite item3a = folder.createItem(TAB_CLASSES_TABLE, null);
        item3a.setLayout(new FillLayout());
        final Composite item3 = folder.createItem(TAB_PROPERTIES, null);
        item3.setLayout(new FillLayout());
        folder.setSelection(0);
        this.enable = folder.getButtonItem(label);
        this.enable.setEnabled(false);
        
        // Create the views
        new ViewStatisticsSummaryTable(item0, controller, target, reset);
        new ViewStatisticsDistributionHistogram(item1, controller, target, reset);
        new ViewStatisticsDistributionTable(item1b, controller, target, reset);
        new ViewStatisticsContingencyHeatmap(item2, controller, target, reset);
        new ViewStatisticsContingencyTable(item2b, controller, target, reset);
        new ViewStatisticsEquivalenceClassTable(item3a, controller, target, reset);
        if (target == ModelPart.INPUT) {
            new ViewPropertiesInput(item3, controller);
        } else {
            new ViewPropertiesOutput(item3, controller);
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
        // Empty by design
    }

    /**
     * Returns the selection index.
     *
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }
    
    @Override
    public void reset() {
        model = null;
        enable.setSelection(true);
        enable.setImage(enabled);
        enable.setEnabled(false);
    }
    
    /**
     * Sets the selection index.
     *
     * @param index
     */
    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
    }

    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.enable.setEnabled(true);
            this.enable.setSelection(model.isVisualizationEnabled());
            this.toggleImage();
        } else if (event.part == ModelPart.SELECTED_UTILITY_VISUALIZATION) {
            this.enable.setSelection(model.isVisualizationEnabled());
            this.toggleImage();
        }
    }

    /**
     * Toggle visualization enabled.
     */
    private void toggleEnabled() {
        this.model.setVisualizationEnabled(this.enable.getSelection());
        this.controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, enable.getSelection()));
    }

    /**
     * Toggle image.
     */
    private void toggleImage(){
        if (enable.getSelection()) {
            enable.setImage(enabled);
        } else {
            enable.setImage(disabled);
        }
    }
}
