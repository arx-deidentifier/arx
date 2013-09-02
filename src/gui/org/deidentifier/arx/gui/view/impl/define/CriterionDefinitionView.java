/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.gui.view.impl.define.criteria.DPresenceView;
import org.deidentifier.arx.gui.view.impl.define.criteria.KAnonymityView;
import org.deidentifier.arx.gui.view.impl.define.criteria.LDiversityView;
import org.deidentifier.arx.gui.view.impl.define.criteria.TClosenessView;
import org.deidentifier.arx.metric.Metric;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class CriterionDefinitionView implements IView {

    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    private static final int       LABEL_HEIGHT    = 20;
    private static final String    LABELS_METRIC[] = { Resources.getMessage("CriterionDefinitionView.0"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.1"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.2"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.3"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.4"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.5"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.52"), //$NON-NLS-1$
            };                                                                                                                                                 //$NON-NLS-1$
    private static final Metric<?> ITEMS_METRIC[]  = { Metric.createHeightMetric(),
            Metric.createPrecisionMetric(),
            Metric.createDMMetric(),
            Metric.createEntropyMetric(),
            Metric.createDMStarMetric(),
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
    
    public CriterionDefinitionView(final Composite parent,
                                   final Controller controller) {

        this.controller = controller;
        this.controller.addListener(EventTarget.MODEL, this);
        this.controller.addListener(EventTarget.INPUT, this);
        this.root = build(parent);
    }

    private Composite build(final Composite parent) {
    	

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 2;
        group.setLayout(groupInputGridLayout);

        /*
         *  Add tab folder for criteria
         */
        GridData gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        CTabFolder folder = new CTabFolder(group, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(true);
        folder.setTabHeight(25);
        folder.setLayoutData(gd1);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
        
        // Create k-anonymity tab
        final CTabItem tabKAnon = new CTabItem(folder, SWT.NULL);
        tabKAnon.setText("- Anonymity");
        tabKAnon.setShowClose(false);
        KAnonymityView kanon = new KAnonymityView(folder, controller, model);
        tabKAnon.setControl(kanon.getControl());
        tabKAnon.setImage(controller.getResources().getImage("symbol_k.gif")); //$NON-NLS-1$
        folder.setSelection(tabKAnon);

        // Create d-presence tab
        final CTabItem tabDPres = new CTabItem(folder, SWT.NULL);
        tabDPres.setText("- Presence");
        tabDPres.setShowClose(false);
        tabDPres.setImage(controller.getResources().getImage("symbol_d.gif")); //$NON-NLS-1$
        DPresenceView dpres = new DPresenceView(folder, controller, model);
        tabDPres.setControl(dpres.getControl());
        
        // Create l-diversity tab
        final CTabItem tabLDiversity = new CTabItem(folder, SWT.NULL);
        tabLDiversity.setText("- Diversity");
        tabLDiversity.setShowClose(false);
        tabLDiversity.setImage(controller.getResources().getImage("symbol_l.gif")); //$NON-NLS-1$
        LDiversityView view = new LDiversityView(folder, controller, model);
        tabLDiversity.setControl(view.getControl());

        // Create t-closeness tab
        final CTabItem tabTcloseness = new CTabItem(folder, SWT.NULL);
        tabTcloseness.setText("- Closeness");
        tabTcloseness.setShowClose(false);
        tabTcloseness.setImage(controller.getResources().getImage("symbol_t.gif")); //$NON-NLS-1$
        TClosenessView view2 = new TClosenessView(folder, controller, model);
        tabTcloseness.setControl(view2.getControl());

        /*
         * Add general view
         */
        gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        gd1.horizontalSpan = 2;
        CTabFolder folder2 = new CTabFolder(group, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder2.setUnselectedCloseVisible(false);
        folder2.setSimple(true);
        folder2.setTabHeight(25);
        folder2.setLayoutData(gd1);

        // Prevent closing
        folder2.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });
        
        // Create general tab
        final CTabItem tabGeneral = new CTabItem(folder2, SWT.NULL);
        tabGeneral.setText("General settings");
        tabGeneral.setShowClose(false);

        group = new Composite(folder2, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);
        tabGeneral.setControl(group);
        folder2.setSelection(tabGeneral);

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
        
        return group;
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * TODO: OK?
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

    @Override
    public void reset() {

        sliderOutliers.setSelection(0);
        labelOutliers.setText("0"); //$NON-NLS-1
        buttonPracticalMonotonicity.setSelection(false);
        comboMetric.select(0);
        SWTUtil.disable(root);
    }

    private void selectMetricAction(final Metric<?> metric) {
        if (model == null) { return; }
        if (metric != null) {
            model.getInputConfig().setMetric(metric);
            controller.update(new ModelEvent(this, EventTarget.METRIC, metric));
        }
    }

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

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
            root.setRedraw(false);
                sliderOutliers.setSelection(doubleToSlider(0d,
                                                               0.999d,
                                                               model.getInputConfig()
                                                                    .getAllowedOutliers()));

                labelOutliers.setText(String.valueOf(model.getInputConfig()
                                                             .getAllowedOutliers()));
                buttonPracticalMonotonicity.setSelection(model.getInputConfig()
                                                        .isPracticalMonotonicity());

                for (int i = 0; i < ITEMS_METRIC.length; i++) {
                    if (ITEMS_METRIC[i].getClass()
                                       .equals(model.getInputConfig()
                                                    .getMetric()
                                                    .getClass())) {
                        comboMetric.select(i);
                        break;
                    }
                }
            
            root.setRedraw(true);
        } else if (event.target == EventTarget.INPUT) {
            SWTUtil.enable(root);
        }
    }
}
