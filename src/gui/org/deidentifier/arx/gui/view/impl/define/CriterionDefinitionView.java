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
import org.deidentifier.arx.gui.Model;
import org.deidentifier.arx.gui.SWTUtil;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.metric.Metric;
import org.eclipse.swt.SWT;
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

    private Scale                  ldivSliderOutliers;
    private Label                  ldivLabelOutlier;
    private Button                 ldivButtonApproximate;
    private Combo                  ldivComboMetric;
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
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 4;
        group.setLayout(groupInputGridLayout);

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.11")); //$NON-NLS-1$

        ldivLabelOutlier = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        ldivLabelOutlier.setLayoutData(d2);
        ldivLabelOutlier.setText("0"); //$NON-NLS-1$

        // Build approximate button
        ldivButtonApproximate = new Button(group, SWT.CHECK);
        ldivButtonApproximate.setText(Resources.getMessage("CriterionDefinitionView.31")); //$NON-NLS-1$
        ldivButtonApproximate.setSelection(false);
        ldivButtonApproximate.setEnabled(false);
        ldivButtonApproximate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(ldivButtonApproximate.getSelection());
            }
        });

        ldivSliderOutliers = new Scale(group, SWT.HORIZONTAL);
        ldivSliderOutliers.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        ldivSliderOutliers.setMaximum(SLIDER_MAX);
        ldivSliderOutliers.setMinimum(0);
        ldivSliderOutliers.setSelection(0);
        ldivSliderOutliers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setAllowedOutliers(sliderToDouble(0d,
                                                            0.999d,
                                                            ldivSliderOutliers.getSelection()));
                ldivLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                             .getAllowedOutliers()));
                if (model.getInputConfig().getAllowedOutliers() != 0) {
                    ldivButtonApproximate.setEnabled(true);
                } else {
                    ldivButtonApproximate.setSelection(false);
                    ldivButtonApproximate.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Create metric combo
        final Label mLabel = new Label(group, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.32")); //$NON-NLS-1$

        final Composite mBase = new Composite(group, SWT.NONE);
        final GridData d8 = SWTUtil.createFillHorizontallyGridData();
        d8.horizontalSpan = 3;
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

        ldivComboMetric = new Combo(mBase, SWT.READ_ONLY);
        GridData d30 = SWTUtil.createFillHorizontallyGridData();
        d30.verticalAlignment = SWT.CENTER;
        ldivComboMetric.setLayoutData(d30);
        ldivComboMetric.setItems(LABELS_METRIC);
        ldivComboMetric.select(0);
        ldivComboMetric.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (ldivComboMetric.getSelectionIndex() != -1) {
                    selectMetricAction(ITEMS_METRIC[ldivComboMetric.getSelectionIndex()]);
                }
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

        ldivSliderOutliers.setSelection(0);
        ldivLabelOutlier.setText("0"); //$NON-NLS-1
        ldivButtonApproximate.setSelection(false);
        ldivComboMetric.select(0);
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
                ldivSliderOutliers.setSelection(doubleToSlider(0d,
                                                               0.999d,
                                                               model.getInputConfig()
                                                                    .getAllowedOutliers()));

                ldivLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                             .getAllowedOutliers()));
                ldivButtonApproximate.setSelection(model.getInputConfig()
                                                        .isPracticalMonotonicity());

                for (int i = 0; i < ITEMS_METRIC.length; i++) {
                    if (ITEMS_METRIC[i].getClass()
                                       .equals(model.getInputConfig()
                                                    .getMetric()
                                                    .getClass())) {
                        ldivComboMetric.select(i);
                        break;
                    }
                }
            
            root.setRedraw(true);
        } else if (event.target == EventTarget.INPUT) {
            SWTUtil.enable(root);
        }
    }
}
