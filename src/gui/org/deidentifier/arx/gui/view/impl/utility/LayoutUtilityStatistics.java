/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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

import java.io.IOError;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Layouts the visualization and allows enabling/disabling them.
 *
 * @author Fabian Prasser
 */
public class LayoutUtilityStatistics implements ILayout, IView {

    /** Constant */
    private static final String                         TAB_SUMMARY                 = Resources.getMessage("StatisticsView.6");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_DISTRIBUTION            = Resources.getMessage("StatisticsView.0");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_DISTRIBUTION_TABLE      = Resources.getMessage("StatisticsView.4");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_CONTINGENCY             = Resources.getMessage("StatisticsView.1");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_CONTINGENCY_TABLE       = Resources.getMessage("StatisticsView.5");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_CLASSES_TABLE           = Resources.getMessage("StatisticsView.7");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_PROPERTIES              = Resources.getMessage("StatisticsView.2");             //$NON-NLS-1$

    /** Constant */
    private static final String                         TAB_CLASSIFICATION_ANALYSIS = Resources.getMessage("StatisticsView.9");             //$NON-NLS-1$

    /** View */
    private final ComponentTitledFolder                 folder;

    /** View */
    private final ToolItem                              chkbtnVisualisation;

    /** View */
    private  ToolItem                                   chkbtnUseKFold;


    /** View */
    private  Boolean                                    useKFold                    = true;

    /** View */
    private final Image                                 icnVisEnabled;

    /** View */
    private final Image                                 icnVisDisabled;

    /** View */
    private final Image                                 icnUseKFoldEnabled;

    /** View */
    private final Image                                 icnUseKFoldDisabled;

    /** View */
    private final Map<Composite, String>                helpids                     = new HashMap<Composite, String>();

    /** Controller */
    private final Controller                            controller;

    /** Model */
    private Model                                       model                       = null;

    /** Control to type */
    private Map<Control, LayoutUtility.ViewUtilityType> types                       = new HashMap<Control, LayoutUtility.ViewUtilityType>();

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

        this.icnVisEnabled = controller.getResources().getManagedImage("tick.png"); //$NON-NLS-1$
        this.icnVisDisabled = controller.getResources().getManagedImage("cross.png"); //$NON-NLS-1$

        this.icnUseKFoldEnabled = controller.getResources().getManagedImage("tickKFold.png"); //$NON-NLS-1$
        this.icnUseKFoldDisabled = controller.getResources().getManagedImage("crossKFold.png"); //$NON-NLS-1$

        this.controller = controller;
        
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_UTILITY_VISUALIZATION, this);

       // Create visualization toolbar
        ComponentTitledFolderButtonBar toolbarVis  = new ComponentTitledFolderButtonBar("id-50", helpids); //$NON-NLS-1$

        // Create visualization enable/disable check button
        final String chkbtnVisualisationLabel = Resources.getMessage("StatisticsView.3"); //$NON-NLS-1$
        toolbarVis.add(chkbtnVisualisationLabel, icnVisEnabled, true, new Runnable() { @Override public void run() {
            toggleChkbtnVisualization();
            toggleChkbtnVisualizationIcon(); 
        }});

        // Create useKFold enable/disable check button
        final String chkbtnUseKFoldLabel = Resources.getMessage("StatisticsView.31"); //$NON-NLS-1$
        if (target == ModelPart.OUTPUT) {            
           toolbarVis.add(chkbtnUseKFoldLabel, icnUseKFoldEnabled, true, new Runnable() { @Override public void run() {
                toggleChkbtnUseKFold();
                toggleChkbtnUseKFoldIcon(); 
            }});
        }
        // Create the tab folder
        folder = new ComponentTitledFolder(parent, controller, toolbarVis, null, false, true);
        
        // Register tabs
        this.registerView(new ViewStatisticsSummaryTable(folder.createItem(TAB_SUMMARY, null, true), controller, target, reset), "help.utility.summary"); //$NON-NLS-1$
        this.registerView(new ViewStatisticsDistributionHistogram(folder.createItem(TAB_DISTRIBUTION, null, true), controller, target, reset), "help.utility.distribution"); //$NON-NLS-1$
        this.registerView(new ViewStatisticsDistributionTable(folder.createItem(TAB_DISTRIBUTION_TABLE, null, true), controller, target, reset), "help.utility.distribution"); //$NON-NLS-1$
        this.registerView(new ViewStatisticsContingencyHeatmap(folder.createItem(TAB_CONTINGENCY, null, true), controller, target, reset), "help.utility.contingency"); //$NON-NLS-1$
        this.registerView(new ViewStatisticsContingencyTable(folder.createItem(TAB_CONTINGENCY_TABLE, null, true), controller, target, reset), "help.utility.contingency"); //$NON-NLS-1$
        this.registerView(new ViewStatisticsEquivalenceClassTable(folder.createItem(TAB_CLASSES_TABLE, null, true), controller, target, reset), "help.utility.classes"); //$NON-NLS-1$
        if (target == ModelPart.INPUT) {
            this.registerView(new ViewPropertiesInput(folder.createItem(TAB_PROPERTIES, null, true), controller), "help.utility.inputproperties"); //$NON-NLS-1$
            this.registerView(new ViewStatisticsClassificationAttributes(folder.createItem(TAB_CLASSIFICATION_ANALYSIS, null, false), controller), "help.utility.accuracy"); //$NON-NLS-1$
        } else {
            this.registerView(new ViewPropertiesOutput(folder.createItem(TAB_PROPERTIES, null, true), controller), "help.utility.outputproperties"); //$NON-NLS-1$
            this.registerView(new ViewStatisticsClassificationConfiguration(folder.createItem(TAB_CLASSIFICATION_ANALYSIS, null, false, new StackLayout()), controller), "help.utility.accuracy"); //$NON-NLS-1$
        }
        
        // Init folder
        this.folder.setSelection(0);

        this.chkbtnVisualisation = folder.getButtonItem(chkbtnVisualisationLabel);
        this.chkbtnVisualisation.setEnabled(false);
        
        if ( target == ModelPart.OUTPUT ) {            
            this.chkbtnUseKFold = folder.getButtonItem(chkbtnUseKFoldLabel);
            this.chkbtnUseKFold.setEnabled(true);
            this.chkbtnUseKFold.setToolTipText("Disable K-fold evaluation!");
        };   
        // Set initial visibility
        folder.setVisibleItems(Arrays.asList(new String[] { TAB_SUMMARY,
                                                            TAB_DISTRIBUTION,
                                                            TAB_CONTINGENCY,
                                                            TAB_CLASSES_TABLE,
                                                            TAB_PROPERTIES }));
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
    public ViewUtilityType getSelectedView() {
        return types.get(folder.getSelectedControl());
    }
    
    /**
     * Returns all visible items
     * @return
     */
    public List<String> getVisibleItems() {
        return this.folder.getVisibleItems();
    }

    @Override
    public void reset() {
        model = null;
        chkbtnVisualisation.setSelection(true);
        chkbtnVisualisation.setImage(icnVisEnabled);
        chkbtnVisualisation.setEnabled(false);
    }
    
    /**
     * Sets the according listener
     * @param listener
     */
    public void setItemVisibilityListener(final SelectionListener listener) {
        folder.setItemVisibilityListener(listener);
    }

    /**
     * Sets the selected view type
     * @param type
     */
    public void setSelectedView(ViewUtilityType type) {
        for (Entry<Control, ViewUtilityType> entry : types.entrySet()) {
            if (entry.getValue() == type) {
                this.folder.setSelectedControl(entry.getKey());
                return;
            }
        }
    }
    
    /**
     * Sets all visible items
     * @param items
     */
    public void setVisibleItems(List<String> items) {
        this.folder.setVisibleItems(items);
    }
    
    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.chkbtnVisualisation.setEnabled(true);
            this.chkbtnVisualisation.setSelection(model.isVisualizationEnabled());
            this.toggleChkbtnVisualizationIcon();
        } else if (event.part == ModelPart.SELECTED_UTILITY_VISUALIZATION) {
            this.chkbtnVisualisation.setSelection(model.isVisualizationEnabled());
            this.toggleChkbtnVisualizationIcon();    
       }
    }

    /**
     * Registers a new view
     * @param view
     * @param helpid
     */
    private void registerView(ViewStatistics<?> view, String helpid) {
        types.put(view.getParent(), view.getType());
        helpids.put(view.getParent(), helpid);
    }

    /**
     * Registers a new view
     * @param view
     * @param helpid
     */
    private void registerView(ViewStatisticsBasic view, String helpid) {
        types.put(view.getParent(), view.getType());
        helpids.put(view.getParent(), helpid);
    }

    /**
     * Toggle visualization enabled.
     */
    private void toggleChkbtnVisualization() {
        this.model.setVisualizationEnabled(this.chkbtnVisualisation.getSelection());
        this.controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, chkbtnVisualisation.getSelection()));
    }

    /**
     * Toggle visualization icon.
     */
    private void toggleChkbtnVisualizationIcon(){
        if (chkbtnVisualisation.getSelection()) {
            chkbtnVisualisation.setImage(icnVisEnabled);
        } else {
            chkbtnVisualisation.setImage(icnVisDisabled);
        }
    }

    
    /**
     * Toggle UseKFold enabled.
     */
    private void toggleChkbtnUseKFold() {
            
        // It should work when classification tab is active
        if (folder.getSelectionIndex()==5) {
            DataSubset trainingSubset = DataSubset.create(this.model.getInputConfig().getInput(), this.model.getInputConfig().getResearchSubset());
            
            this.model.setVisualizationEnabled(false);
            this.controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, false));
            
            this.model.getClassificationModel().getCurrentConfiguration().setEvaluateWithKfold(chkbtnUseKFold.getSelection());
            this.controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_KFold, chkbtnUseKFold.getSelection()));
            
            this.model.setVisualizationEnabled(true);
            this.controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION,true));
        }
    }

    /**
     * Toggle UseKFold icon.
     */
    private void toggleChkbtnUseKFoldIcon(){
        if (folder.getSelectionIndex()==5) {
            if (this.chkbtnUseKFold.getSelection()) {
                this.chkbtnUseKFold.setImage(icnUseKFoldEnabled);
            } else {
                this.chkbtnUseKFold.setImage(icnUseKFoldDisabled);
            }
        }
    }
}