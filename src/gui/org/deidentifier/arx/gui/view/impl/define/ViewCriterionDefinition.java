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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitleBar;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.define.criteria.ViewDPresence;
import org.deidentifier.arx.gui.view.impl.define.criteria.ViewKAnonymity;
import org.deidentifier.arx.gui.view.impl.define.criteria.ViewLDiversity;
import org.deidentifier.arx.gui.view.impl.define.criteria.ViewTCloseness;
import org.deidentifier.arx.metric.Metric;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ToolItem;

public class ViewCriterionDefinition implements IView {

    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    private static final int       LABEL_HEIGHT    = 20;
    private static final String    LABELS_METRIC[] = { 
            Resources.getMessage("CriterionDefinitionView.0"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.1"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.2"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.3"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.4"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.5"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.52"), //$NON-NLS-1$
    };
    private static final Metric<?> ITEMS_METRIC[]  = { 
            Metric.createHeightMetric(),
            Metric.createPrecisionMetric(),
            Metric.createDMStarMetric(),
            Metric.createEntropyMetric(),
            Metric.createDMMetric(),
            Metric.createNMEntropyMetric(),
            Metric.createAECSMetric()};

    private final Controller       controller;
    private Model                  model           = null;

    private Scale                  sliderOutliers;
    private Label                  labelOutliers;
    private Button                 buttonPracticalMonotonicity;
    private Button                 buttonProtectSensitiveAssociations;
    private Combo                  comboMetric;
    private Composite			   root;
    
    private ComponentTitledFolder           folder;
    private ToolItem               enable;
    private ToolItem               push;
    private ToolItem               pull;
    
    private ViewCriteriaList       clv;
    
    public ViewCriterionDefinition(final Composite parent,
                                   final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.root = build(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        clv.dispose();
    }
    
    @Override
    public void reset() {

        sliderOutliers.setSelection(0);
        labelOutliers.setText("0"); //$NON-NLS-1
        buttonPracticalMonotonicity.setSelection(false);
        comboMetric.select(0);
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            
            model = (Model) event.data;
            root.setRedraw(false);
            sliderOutliers.setSelection(doubleToSlider(0d, 0.999d, model.getInputConfig().getAllowedOutliers()));
            labelOutliers.setText(String.valueOf(model.getInputConfig().getAllowedOutliers()));
            buttonPracticalMonotonicity.setSelection(model.getInputConfig().isPracticalMonotonicity());
            buttonProtectSensitiveAssociations.setSelection(model.getInputConfig().isProtectSensitiveAssociations());
            
            for (int i = 0; i < ITEMS_METRIC.length; i++) {
                if (ITEMS_METRIC[i].getClass().equals(model.getInputConfig().getMetric().getClass())) {
                    comboMetric.select(i);
                    break;
                }
            }
            updateControlls();
            root.setRedraw(true);
        } else if (event.part == ModelPart.INPUT) {
            SWTUtil.enable(root);
            updateControlls();
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.CRITERION_DEFINITION) {
            
            if (model != null){
                updateControlls();
            }
        }
    }

    private Composite build(final Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(2));

        /*
         *  Add tab folder for criteria
         */
        GridData gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        gd1.grabExcessHorizontalSpace = true;
        gd1.horizontalSpan = 2;
        
        ComponentTitleBar bar = new ComponentTitleBar("id-80");
        bar.add(Resources.getMessage("CriterionDefinitionView.59"), 
                controller.getResources().getImage("cross.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionEnable(getSelectedCriterion());
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.57"), 
                controller.getResources().getImage("bullet_arrow_up.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionPush(getSelectedCriterion());
                    }
                });
        
        bar.add(Resources.getMessage("CriterionDefinitionView.58"), 
                controller.getResources().getImage("bullet_arrow_down.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionPull(getSelectedCriterion());
                    }
                });

        folder = new ComponentTitledFolder(group, controller, bar, null);
        folder.setLayoutData(gd1);
        Composite item1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.19"), controller.getResources().getImage("symbol_k.png"));        
        new ViewKAnonymity(item1, controller, model);
        Composite item2 = folder.createItem(Resources.getMessage("CriterionDefinitionView.60"), controller.getResources().getImage("symbol_d.png"));
        new ViewDPresence(item2, controller, model);
        Composite item3 = folder.createItem(Resources.getMessage("CriterionDefinitionView.20"), controller.getResources().getImage("symbol_l.png"));
        new ViewLDiversity(item3, controller, model);
        Composite item4 = folder.createItem(Resources.getMessage("CriterionDefinitionView.21"), controller.getResources().getImage("symbol_t.png"));
        new ViewTCloseness(item4, controller, model);
        folder.setSelection(0);
        
        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                updateControlls();
            }
        });
        
        enable = folder.getBarItem(Resources.getMessage("CriterionDefinitionView.59"));
        push = folder.getBarItem(Resources.getMessage("CriterionDefinitionView.57"));
        pull = folder.getBarItem(Resources.getMessage("CriterionDefinitionView.58"));
        
        enable.setEnabled(false);
        push.setEnabled(false);
        pull.setEnabled(false);
      
        /*
         * Add general view
         */
        gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        gd1.horizontalSpan = 2;
        ComponentTitledFolder folder2 = new ComponentTitledFolder(parent, controller, null, "id-60");
        folder2.setLayoutData(gd1);
        
        // Create general tab
        group = folder2.createItem(Resources.getMessage("CriterionDefinitionView.61"), null);  //$NON-NLS-1$
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(3, false));

        // TODO: Move general tabs content out into a separate view
        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.11")); //$NON-NLS-1$

        labelOutliers = new Label(group, SWT.BORDER | SWT.CENTER);
        GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        d2.heightHint = LABEL_HEIGHT;
        labelOutliers.setLayoutData(d2);
        labelOutliers.setText("0"); //$NON-NLS-1$

        sliderOutliers = new Scale(group, SWT.HORIZONTAL);
        sliderOutliers.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderOutliers.setMaximum(SLIDER_MAX);
        sliderOutliers.setMinimum(0);
        sliderOutliers.setSelection(0);
        sliderOutliers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setAllowedOutliers(sliderToDouble(0d,
                                                            0.999d,
                                                            sliderOutliers.getSelection()));
                labelOutliers.setText(String.valueOf(model.getInputConfig()
                                                             .getAllowedOutliers()));
                if (model.getInputConfig().getAllowedOutliers() != 0) {
                    buttonPracticalMonotonicity.setEnabled(true);
                } else {
                    buttonPracticalMonotonicity.setSelection(false);
                    buttonPracticalMonotonicity.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Create metric combo
        final Label mLabel = new Label(group, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.32")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        mLabel.setLayoutData(d2);
        
        final Composite mBase = new Composite(group, SWT.NONE);
        final GridData d8 = SWTUtil.createFillHorizontallyGridData();
        d8.horizontalSpan = 2;
        mBase.setLayoutData(d8);
        final GridLayout l = new GridLayout();
        l.numColumns = 7;
        l.marginLeft = 0;
        l.marginRight = 0;
        l.marginBottom = 0;
        l.marginTop = 0;
        l.marginWidth = 0;
        l.marginHeight = 0;
        mBase.setLayout(l);

        comboMetric = new Combo(mBase, SWT.READ_ONLY);
        GridData d30 = SWTUtil.createFillHorizontallyGridData();
        d30.verticalAlignment = SWT.CENTER;
        comboMetric.setLayoutData(d30);
        comboMetric.setItems(LABELS_METRIC);
        comboMetric.select(0);
        comboMetric.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (comboMetric.getSelectionIndex() != -1) {
                    selectMetricAction(ITEMS_METRIC[comboMetric.getSelectionIndex()]);
                }
            }
        });


        // Build approximate button
        final Label m2Label = new Label(group, SWT.PUSH);
        m2Label.setText(Resources.getMessage("CriterionDefinitionView.31")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        m2Label.setLayoutData(d2);

        final GridData d82 = SWTUtil.createFillHorizontallyGridData();
        d82.horizontalSpan = 2;
        buttonPracticalMonotonicity = new Button(group, SWT.CHECK);
        buttonPracticalMonotonicity.setText(Resources.getMessage("CriterionDefinitionView.53")); //$NON-NLS-1$
        buttonPracticalMonotonicity.setSelection(false);
        buttonPracticalMonotonicity.setEnabled(false);
        buttonPracticalMonotonicity.setLayoutData(d82);
        buttonPracticalMonotonicity.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(buttonPracticalMonotonicity.getSelection());
            }
        });

        // Build protect sensitive associations button
        final Label m3Label = new Label(group, SWT.PUSH);
        m3Label.setText(Resources.getMessage("CriterionDefinitionView.54")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        m3Label.setLayoutData(d2);

        final GridData d83 = SWTUtil.createFillHorizontallyGridData();
        d83.horizontalSpan = 2;
        buttonProtectSensitiveAssociations = new Button(group, SWT.CHECK);
        buttonProtectSensitiveAssociations.setText(Resources.getMessage("CriterionDefinitionView.55")); //$NON-NLS-1$
        buttonProtectSensitiveAssociations.setSelection(true);
        buttonProtectSensitiveAssociations.setEnabled(false);
        buttonProtectSensitiveAssociations.setLayoutData(d83);
        buttonProtectSensitiveAssociations.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig().
                     setProtectSensitiveAssociations(buttonProtectSensitiveAssociations.getSelection());
            }
        });
        
        // Create overview tab
        Composite c = folder2.createItem(Resources.getMessage("CriterionDefinitionView.62"), null);  //$NON-NLS-1$
        c.setLayout(new FillLayout());
        clv = new ViewCriteriaList(c, controller);
        
        folder2.setSelection(0);

        return group;
    }

    /**
     * Converts the double value to a slider selection
     */
    private int doubleToSlider(final double min,
                               final double max,
                               final double value) {
        double val = ((value - min) / max) * SLIDER_MAX;
        val = Math.round(val * SLIDER_MAX) / (double) SLIDER_MAX;
        if (val < 0) {
            val = 0;
        }
        if (val > SLIDER_MAX) {
            val = SLIDER_MAX;
        }
        return (int) val;
    }

    /**
     * Select metric action
     * @param metric
     */
    private void selectMetricAction(final Metric<?> metric) {
        if (model == null) { return; }
        if (metric != null) {
            model.getInputConfig().setMetric(metric);
            controller.update(new ModelEvent(this, ModelPart.METRIC, metric));
        }
    }

    /**
     * Converts the slider value to a double
     * @param min
     * @param max
     * @param value
     * @return
     */
    private double sliderToDouble(final double min,
                                  final double max,
                                  final int value) {
        double val = ((double) value / (double) SLIDER_MAX) * max;
        val = Math.round(val * SLIDER_MAX) / (double) SLIDER_MAX;
        if (val < min) {
            val = min;
        }
        if (val > max) {
            val = max;
        }
        return val;
    }

    /**
     * Returns the currently selected criterion
     * @return
     */
    private ModelCriterion getSelectedCriterion() {
        ModelCriterion mc = null;
        if (folder.getSelectionIndex()==0){
            mc = model.getKAnonymityModel();
        } else if (folder.getSelectionIndex()==1){
            mc = model.getDPresenceModel();
        } else if (folder.getSelectionIndex()==2){
            mc = model.getLDiversityModel().get(model.getSelectedAttribute());
        } else if (folder.getSelectionIndex()==3){
            mc = model.getTClosenessModel().get(model.getSelectedAttribute());
        }
        return mc;
    }
    
    /**
     * This method adjusts the toolbar attached to the folder with criteria
     * according to the current state of the model
     */
    private void updateControlls(){

        root.setRedraw(false);
        ModelCriterion mc = null;
        
        // K-Anonymity
        if (folder.getSelectionIndex()==0){
            push.setEnabled(false);
            pull.setEnabled(false);
            mc = model.getKAnonymityModel();
            
        // D-Presence
        } else if (folder.getSelectionIndex()==1){
            push.setEnabled(false);
            pull.setEnabled(false);
            mc = model.getDPresenceModel();
            
        // L-Diversity
        } else if (folder.getSelectionIndex()==2){
            mc = model.getLDiversityModel().get(model.getSelectedAttribute());
            if (mc != null && mc.isActive() && mc.isEnabled()){
                push.setEnabled(true);
                pull.setEnabled(true);
            } else {
                push.setEnabled(false);
                pull.setEnabled(false);
            }
            
        // T-Closeness
        } else if (folder.getSelectionIndex()==3){
            mc = model.getTClosenessModel().get(model.getSelectedAttribute());
            push.setEnabled(true);
            pull.setEnabled(true);
            if (mc != null && mc.isActive() && mc.isEnabled()){
                push.setEnabled(true);
                pull.setEnabled(true);
            } else {
                push.setEnabled(false);
                pull.setEnabled(false);
            }
        }
        
        if (mc == null){
            root.setRedraw(true);
            return;
        }
        
        if (mc.isActive()) {
            enable.setEnabled(true);
            if (mc.isEnabled()) {
                enable.setImage(controller.getResources().getImage("tick.png")); //$NON-NLS-1
            } else {
                enable.setImage(controller.getResources().getImage("cross.png")); //$NON-NLS-1
            }
        } else {
            enable.setEnabled(false);
            enable.setImage(controller.getResources().getImage("cross.png")); //$NON-NLS-1
        }
        root.setRedraw(true);
    }
}
