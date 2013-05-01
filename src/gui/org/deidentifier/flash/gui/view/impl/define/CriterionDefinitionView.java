/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl.define;

import org.deidentifier.flash.FLASHConfiguration.Criterion;
import org.deidentifier.flash.FLASHConfiguration.LDiversityCriterion;
import org.deidentifier.flash.FLASHConfiguration.TClosenessCriterion;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.flash.metric.Metric;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class CriterionDefinitionView implements IView {

    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    private static final String    LABELS_METRIC[] = { Resources.getMessage("CriterionDefinitionView.0"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.1"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.2"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.3"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.4"), //$NON-NLS-1$
            Resources.getMessage("CriterionDefinitionView.5") };                                                                                                                                                 //$NON-NLS-1$
    private static final Metric<?> ITEMS_METRIC[]  = { Metric.createHeightMetric(),
            Metric.createPrecisionMetric(),
            Metric.createDMMetric(),
            Metric.createEntropyMetric(),
            Metric.createDMStarMetric(),
            Metric.createNMEntropyMetric()        };

    private static final String    LDIV_CRITERIA[] = { Resources.getMessage("CriterionDefinitionView.6"), Resources.getMessage("CriterionDefinitionView.7"), Resources.getMessage("CriterionDefinitionView.8") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String    TCLO_CRITERIA[] = { Resources.getMessage("CriterionDefinitionView.9"), Resources.getMessage("CriterionDefinitionView.10") };                                                  //$NON-NLS-1$ //$NON-NLS-2$

    private final Controller       controller;
    private Model                  model           = null;

    private Scale                  ldivSliderL;
    private Scale                  ldivSliderC;
    private Scale                  ldivSliderOutliers;
    private Button                 ldivButtonApproximate;
    private Combo                  ldivComboMetric;
    private Combo                  ldivComboCriteria;
    private Label                  ldivLabelOutlier;
    private Label                  ldivLabelC;
    private Label                  ldivLabelL;

    private Scale                  tclosSliderK;
    private Scale                  tclosSliderOutliers;
    private Scale                  tclosSliderT;
    private Button                 tclosButtonApproximate;
    private Combo                  tclosComboMetric;
    private Combo                  tclosComboCriterion;

    private Label                  tclosLabelT;
    private Label                  tclosLabelK;
    private Label                  tclosLabelOutlier;

    private Label                  kanonLabelK;
    private Label                  kanonLabelOutlier;
    private Scale                  kanonSliderK;
    private Scale                  kanonSliderOutlier;
    private Combo                  kanonComboMetric;
    private TabFolder              root;

    public CriterionDefinitionView(final Composite parent,
                                   final Controller controller) {

        this.controller = controller;
        this.controller.addListener(EventTarget.MODEL, this);
        this.controller.addListener(EventTarget.INPUT, this);
        build(parent);
    }

    private void build(final Composite parent) {

        // Prepare parent
        final GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 1;
        parent.setLayout(parentLayout);

        // Create the tab folder
        root = new TabFolder(parent, SWT.NONE);
        final GridData tabData = SWTUtil.createFillGridData();
        final IView outer = this;
        tabData.grabExcessVerticalSpace = false;
        root.setLayoutData(tabData);
        root.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final int index = root.getSelectionIndex();
                switch (index) {
                case 0:
                    if (model != null) {
                        model.getInputConfig()
                             .setCriterion(Criterion.K_ANONYMITY);
                    }
                    break;
                case 1:
                    if (model != null) {
                        model.getInputConfig()
                             .setCriterion(Criterion.L_DIVERSITY);
                    }
                    break;
                case 2:
                    if (model != null) {
                        model.getInputConfig()
                             .setCriterion(Criterion.T_CLOSENESS);
                    }
                    break;
                }
                if (model != null) {
                    controller.update(new ModelEvent(outer,
                                                     EventTarget.ALGORITHM,
                                                     model.getInputConfig()
                                                          .getCriterion()));
                }
            }
        });

        // Create k-anonymity group
        final TabItem tabKAnonymity = new TabItem(root, SWT.NULL);
        tabKAnonymity.setText(Resources.getMessage("CriterionDefinitionView.19")); //$NON-NLS-1$
        tabKAnonymity.setControl(buildKAnonymity(root));

        // Create l-diversity group
        final TabItem tablDiversity = new TabItem(root, SWT.NULL);
        tablDiversity.setText(Resources.getMessage("CriterionDefinitionView.20")); //$NON-NLS-1$
        tablDiversity.setControl(buildLDiversity(root));
        
        // Create t-closeness group
        final TabItem tabTKCloseness = new TabItem(root, SWT.NULL);
        tabTKCloseness.setText(Resources.getMessage("CriterionDefinitionView.21")); //$NON-NLS-1$
        tabTKCloseness.setControl(buildTCloseness(root));
    }

    private Composite buildKAnonymity(final Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create k slider
        final Label kLabel = new Label(group, SWT.NONE);
        kLabel.setText(Resources.getMessage("CriterionDefinitionView.22")); //$NON-NLS-1$

        kanonLabelK = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        kanonLabelK.setLayoutData(d);
        kanonLabelK.setText("2"); //$NON-NLS-1$

        kanonSliderK = new Scale(group, SWT.HORIZONTAL);
        kanonSliderK.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        kanonSliderK.setMaximum(SLIDER_MAX);
        kanonSliderK.setMinimum(0);
        kanonSliderK.setSelection(0);
        kanonSliderK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setK(sliderToInt(2, 100, kanonSliderK.getSelection()));
                kanonLabelK.setText(String.valueOf(model.getInputConfig()
                                                        .getK()));

                tclosLabelK.setText(kanonLabelK.getText());
                tclosSliderK.setSelection(kanonSliderK.getSelection());
            }
        });

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.24")); //$NON-NLS-1$

        kanonLabelOutlier = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        kanonLabelOutlier.setLayoutData(d2);
        kanonLabelOutlier.setText("0"); //$NON-NLS-1$

        kanonSliderOutlier = new Scale(group, SWT.HORIZONTAL);
        kanonSliderOutlier.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        kanonSliderOutlier.setMaximum(SLIDER_MAX);
        kanonSliderOutlier.setMinimum(0);
        kanonSliderOutlier.setSelection(0);
        kanonSliderOutlier.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setRelativeMaxOutliers(sliderToDouble(0d,
                                                            0.999d,
                                                            kanonSliderOutlier.getSelection()));
                kanonLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                              .getRelativeMaxOutliers()));

                tclosLabelOutlier.setText(kanonLabelOutlier.getText());
                ldivLabelOutlier.setText(kanonLabelOutlier.getText());
                tclosSliderOutliers.setSelection(kanonSliderOutlier.getSelection());
                ldivSliderOutliers.setSelection(kanonSliderOutlier.getSelection());

                if (model.getInputConfig().getRelativeMaxOutliers() != 0) {
                    ldivButtonApproximate.setEnabled(true);
                    tclosButtonApproximate.setEnabled(true);
                } else {
                    ldivButtonApproximate.setSelection(false);
                    tclosButtonApproximate.setSelection(false);
                    ldivButtonApproximate.setEnabled(false);
                    tclosButtonApproximate.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Create metric combo
        final Label mLabel = new Label(group, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.26")); //$NON-NLS-1$
        GridData d32 = SWTUtil.createFillVerticallyGridData();
        d32.verticalAlignment = SWT.CENTER;
        mLabel.setLayoutData(d32);

        kanonComboMetric = new Combo(group, SWT.READ_ONLY);
        final GridData d3 = SWTUtil.createFillGridData();
        d3.verticalAlignment = SWT.CENTER;
        d3.horizontalSpan = 2;
        kanonComboMetric.setLayoutData(d3);
        kanonComboMetric.setItems(LABELS_METRIC);
        kanonComboMetric.select(0);
        kanonComboMetric.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (kanonComboMetric.getSelectionIndex() != -1) {
                    selectMetricAction(ITEMS_METRIC[kanonComboMetric.getSelectionIndex()]);
                    ldivComboMetric.select(kanonComboMetric.getSelectionIndex());
                    tclosComboMetric.select(kanonComboMetric.getSelectionIndex());
                }
            }
        });
        
        return group;
    }

    private Composite buildLDiversity(final Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 4;
        group.setLayout(groupInputGridLayout);

        // Create k slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.27")); //$NON-NLS-1$

        ldivLabelL = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        ldivLabelL.setLayoutData(d);
        ldivLabelL.setText("2"); //$NON-NLS-1$

        ldivSliderL = new Scale(group, SWT.HORIZONTAL);
        final GridData d4 = SWTUtil.createFillHorizontallyGridData();
        d4.horizontalSpan = 2;
        ldivSliderL.setLayoutData(d4);
        ldivSliderL.setMaximum(SLIDER_MAX);
        ldivSliderL.setMinimum(0);
        ldivSliderL.setSelection(0);
        ldivSliderL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setL(sliderToInt(2, 100, ldivSliderL.getSelection()));
                ldivLabelL.setText(String.valueOf(model.getInputConfig().getL()));
            }
        });

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.11")); //$NON-NLS-1$

        ldivLabelOutlier = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        ldivLabelOutlier.setLayoutData(d2);
        ldivLabelOutlier.setText("0"); //$NON-NLS-1$

        ldivButtonApproximate = new Button(group, SWT.CHECK);
        ldivButtonApproximate.setText(Resources.getMessage("CriterionDefinitionView.31")); //$NON-NLS-1$
        ldivButtonApproximate.setSelection(false);
        ldivButtonApproximate.setEnabled(false);
        ldivButtonApproximate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(ldivButtonApproximate.getSelection());
                tclosButtonApproximate.setSelection(ldivButtonApproximate.getSelection());
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
                     .setRelativeMaxOutliers(sliderToDouble(0d,
                                                            0.999d,
                                                            ldivSliderOutliers.getSelection()));
                ldivLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                             .getRelativeMaxOutliers()));
                tclosLabelOutlier.setText(ldivLabelOutlier.getText());
                tclosSliderOutliers.setSelection(ldivSliderOutliers.getSelection());
                kanonLabelOutlier.setText(ldivLabelOutlier.getText());
                kanonSliderOutlier.setSelection(ldivSliderOutliers.getSelection());
                if (model.getInputConfig().getRelativeMaxOutliers() != 0) {
                    ldivButtonApproximate.setEnabled(true);
                    tclosButtonApproximate.setEnabled(true);
                } else {
                    ldivButtonApproximate.setSelection(false);
                    tclosButtonApproximate.setSelection(false);
                    ldivButtonApproximate.setEnabled(false);
                    tclosButtonApproximate.setEnabled(false);
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
                    kanonComboMetric.select(ldivComboMetric.getSelectionIndex());
                    tclosComboMetric.select(ldivComboMetric.getSelectionIndex());
                }
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(mBase, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.33")); //$NON-NLS-1$

        ldivComboCriteria = new Combo(mBase, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        ldivComboCriteria.setLayoutData(d31);
        ldivComboCriteria.setItems(LDIV_CRITERIA);
        ldivComboCriteria.select(0);

        // Create c slider
        final Label zLabel = new Label(mBase, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.34")); //$NON-NLS-1$

        ldivLabelC = new Label(mBase, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        ldivLabelC.setLayoutData(d9);
        ldivLabelC.setText("0.001"); //$NON-NLS-1$

        ldivSliderC = new Scale(mBase, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        ldivSliderC.setLayoutData(d6);
        ldivSliderC.setMaximum(SLIDER_MAX);
        ldivSliderC.setMinimum(0);
        ldivSliderC.setSelection(0);
        ldivSliderC.setEnabled(false);
        ldivSliderC.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setC(sliderToDouble(0.001,
                                          100,
                                          ldivSliderC.getSelection()));
                ldivLabelC.setText(String.valueOf(model.getInputConfig().getC()));
            }
        });

        ldivComboCriteria.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (ldivComboCriteria.getSelectionIndex() == 0) {
                    model.getInputConfig()
                         .setLDiversityCriterion(LDiversityCriterion.DISTINCT);
                    ldivSliderC.setEnabled(false);
                } else if (ldivComboCriteria.getSelectionIndex() == 1) {
                    model.getInputConfig()
                         .setLDiversityCriterion(LDiversityCriterion.ENTROPY);
                    ldivSliderC.setEnabled(false);
                } else if (ldivComboCriteria.getSelectionIndex() == 2) {
                    model.getInputConfig()
                         .setLDiversityCriterion(LDiversityCriterion.RECURSIVE);
                    ldivSliderC.setEnabled(true);
                }
            }
        });

        return group;
    }

    private Composite buildTCloseness(final Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 4;
        group.setLayout(groupInputGridLayout);

        // Create k slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.36")); //$NON-NLS-1$

        tclosLabelK = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        tclosLabelK.setLayoutData(d);
        tclosLabelK.setText("2"); //$NON-NLS-1$

        tclosSliderK = new Scale(group, SWT.HORIZONTAL);
        final GridData d4 = SWTUtil.createFillHorizontallyGridData();
        d4.horizontalSpan = 2;
        tclosSliderK.setLayoutData(d4);
        tclosSliderK.setMaximum(SLIDER_MAX);
        tclosSliderK.setMinimum(0);
        tclosSliderK.setSelection(0);
        tclosSliderK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setK(sliderToInt(2, 100, tclosSliderK.getSelection()));
                tclosLabelK.setText(String.valueOf(model.getInputConfig()
                                                        .getK()));
                kanonLabelK.setText(tclosLabelK.getText());
                kanonSliderK.setSelection(tclosSliderK.getSelection());
            }
        });

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.38")); //$NON-NLS-1$

        tclosLabelOutlier = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        tclosLabelOutlier.setLayoutData(d2);
        tclosLabelOutlier.setText("0"); //$NON-NLS-1$

        tclosButtonApproximate = new Button(group, SWT.CHECK);
        tclosButtonApproximate.setText(Resources.getMessage("CriterionDefinitionView.40")); //$NON-NLS-1$
        tclosButtonApproximate.setSelection(false);
        tclosButtonApproximate.setEnabled(false);
        tclosButtonApproximate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(tclosButtonApproximate.getSelection());
                ldivButtonApproximate.setSelection(tclosButtonApproximate.getSelection());
            }
        });

        tclosSliderOutliers = new Scale(group, SWT.HORIZONTAL);
        tclosSliderOutliers.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        tclosSliderOutliers.setMaximum(SLIDER_MAX);
        tclosSliderOutliers.setMinimum(0);
        tclosSliderOutliers.setSelection(0);
        tclosSliderOutliers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setRelativeMaxOutliers(sliderToDouble(0d,
                                                            0.999d,
                                                            tclosSliderOutliers.getSelection()));
                tclosLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                              .getRelativeMaxOutliers()));
                ldivLabelOutlier.setText(tclosLabelOutlier.getText());
                kanonLabelOutlier.setText(tclosLabelOutlier.getText());
                ldivSliderOutliers.setSelection(tclosSliderOutliers.getSelection());
                kanonSliderOutlier.setSelection(tclosSliderOutliers.getSelection());
                if (model.getInputConfig().getRelativeMaxOutliers() != 0) {
                    tclosButtonApproximate.setEnabled(true);
                    ldivButtonApproximate.setEnabled(true);
                } else {
                    ldivButtonApproximate.setSelection(false);
                    tclosButtonApproximate.setSelection(false);
                    ldivButtonApproximate.setEnabled(false);
                    tclosButtonApproximate.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Create metric combo
        final Label mLabel = new Label(group, SWT.PUSH);
        mLabel.setText(Resources.getMessage("CriterionDefinitionView.41")); //$NON-NLS-1$

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

        tclosComboMetric = new Combo(mBase, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        tclosComboMetric.setLayoutData(d31);
        tclosComboMetric.setItems(LABELS_METRIC);
        tclosComboMetric.select(0);
        tclosComboMetric.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (tclosComboMetric.getSelectionIndex() != -1) {
                    selectMetricAction(ITEMS_METRIC[tclosComboMetric.getSelectionIndex()]);
                    kanonComboMetric.select(tclosComboMetric.getSelectionIndex());
                    ldivComboMetric.select(tclosComboMetric.getSelectionIndex());
                }
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(mBase, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.42")); //$NON-NLS-1$

        tclosComboCriterion = new Combo(mBase, SWT.READ_ONLY);
        GridData d32 = SWTUtil.createFillHorizontallyGridData();
        d32.verticalAlignment = SWT.CENTER;
        tclosComboCriterion.setLayoutData(d32);
        tclosComboCriterion.setItems(TCLO_CRITERIA);
        tclosComboCriterion.select(0);
        tclosComboCriterion.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (tclosComboCriterion.getSelectionIndex() == 0) {
                    model.getInputConfig()
                         .setTClosenessCriterion(TClosenessCriterion.EMD_EQUAL);
                } else if (tclosComboCriterion.getSelectionIndex() == 1) {
                    model.getInputConfig()
                         .setTClosenessCriterion(TClosenessCriterion.EMD_HIERARCHICAL);
                }
            }
        });

        // Create c slider
        final Label zLabel = new Label(mBase, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.43")); //$NON-NLS-1$

        tclosLabelT = new Label(mBase, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        tclosLabelT.setLayoutData(d9);
        tclosLabelT.setText("0.001"); //$NON-NLS-1$

        tclosSliderT = new Scale(mBase, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        tclosSliderT.setLayoutData(d6);
        tclosSliderT.setMaximum(SLIDER_MAX);
        tclosSliderT.setMinimum(0);
        tclosSliderT.setSelection(0);
        tclosSliderT.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setT(sliderToDouble(0.001, 1, tclosSliderT.getSelection()));
                tclosLabelT.setText(String.valueOf(model.getInputConfig()
                                                        .getT()));
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

    /**
     * TODO: OK?
     */
    private int intToSlider(final int min, final int max, final int value) {

        int val = (int) Math.round(((double) (value - min) / (double) max) *
                                   SLIDER_MAX);
        if (val < 0) {
            val = 0;
        }
        if (val > SLIDER_MAX) {
            val = SLIDER_MAX;
        }
        return val;
    }

    @Override
    public void reset() {

        root.setSelection(0);

        ldivSliderL.setSelection(0);
        ldivSliderC.setSelection(0);
        ldivSliderOutliers.setSelection(0);
        tclosSliderK.setSelection(0);
        tclosSliderOutliers.setSelection(0);
        tclosSliderT.setSelection(0);
        kanonSliderK.setSelection(0);
        kanonSliderOutlier.setSelection(0);

        ldivLabelC.setText("0.001"); //$NON-NLS-1$
        ldivLabelL.setText("2"); //$NON-NLS-1$
        tclosLabelT.setText("0.001"); //$NON-NLS-1$
        kanonLabelK.setText("2"); //$NON-NLS-1$
        tclosLabelK.setText("2"); //$NON-NLS-1$
        ldivLabelOutlier.setText("0"); //$NON-NLS-1$
        tclosLabelOutlier.setText("0"); //$NON-NLS-1$
        kanonLabelOutlier.setText("0"); //$NON-NLS-1$

        ldivButtonApproximate.setSelection(false);
        tclosButtonApproximate.setSelection(false);

        ldivComboMetric.select(0);
        ldivComboCriteria.select(0);
        tclosComboMetric.select(0);
        tclosComboCriterion.select(0);
        kanonComboMetric.select(0);
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

    private int sliderToInt(final int min, final int max, final int value) {
        int val = (int) Math.round(((double) value / (double) SLIDER_MAX) * max);
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
            switch (model.getInputConfig().getCriterion()) {
            case K_ANONYMITY:
                kanonSliderK.setSelection(intToSlider(2,
                                                      100,
                                                      model.getInputConfig()
                                                           .getK()));
                kanonSliderOutlier.setSelection(doubleToSlider(0d,
                                                               0.999d,
                                                               model.getInputConfig()
                                                                    .getRelativeMaxOutliers()));
                kanonLabelK.setText(String.valueOf(model.getInputConfig()
                                                        .getK()));
                kanonLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                              .getRelativeMaxOutliers()));
                for (int i = 0; i < ITEMS_METRIC.length; i++) {
                    if (ITEMS_METRIC[i].getClass()
                                       .equals(model.getInputConfig()
                                                    .getMetric()
                                                    .getClass())) {
                        kanonComboMetric.select(i);
                        ldivComboMetric.select(i);
                        tclosComboMetric.select(i);
                        break;
                    }
                }
                root.setSelection(0);
                break;
            case L_DIVERSITY:

                ldivSliderL.setSelection(intToSlider(2,
                                                     100,
                                                     model.getInputConfig()
                                                          .getL()));
                ldivSliderC.setSelection(doubleToSlider(0.001,
                                                        100,
                                                        model.getInputConfig()
                                                             .getC()));
                ldivSliderOutliers.setSelection(doubleToSlider(0d,
                                                               0.999d,
                                                               model.getInputConfig()
                                                                    .getRelativeMaxOutliers()));
                ldivLabelC.setText(String.valueOf(model.getInputConfig().getC()));
                ldivLabelL.setText(String.valueOf(model.getInputConfig().getL()));
                ldivLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                             .getRelativeMaxOutliers()));
                ldivButtonApproximate.setSelection(model.getInputConfig()
                                                        .getPracticalMonotonicity());

                for (int i = 0; i < ITEMS_METRIC.length; i++) {
                    if (ITEMS_METRIC[i].getClass()
                                       .equals(model.getInputConfig()
                                                    .getMetric()
                                                    .getClass())) {
                        kanonComboMetric.select(i);
                        ldivComboMetric.select(i);
                        tclosComboMetric.select(i);
                        break;
                    }
                }
                switch (model.getInputConfig().getLDiversityCriterion()) {
                case DISTINCT:
                    ldivComboCriteria.select(0);
                    break;
                case ENTROPY:
                    ldivComboCriteria.select(1);
                    break;
                case RECURSIVE:
                    ldivComboCriteria.select(2);
                    break;
                }

                root.setSelection(1);
                break;
            case T_CLOSENESS:

                tclosSliderK.setSelection(intToSlider(2,
                                                      100,
                                                      model.getInputConfig()
                                                           .getK()));
                tclosSliderT.setSelection(doubleToSlider(0.001,
                                                         1,
                                                         model.getInputConfig()
                                                              .getT()));
                tclosSliderOutliers.setSelection(doubleToSlider(0d,
                                                                0.999d,
                                                                model.getInputConfig()
                                                                     .getRelativeMaxOutliers()));
                tclosLabelT.setText(String.valueOf(model.getInputConfig()
                                                        .getT()));
                tclosLabelK.setText(String.valueOf(model.getInputConfig()
                                                        .getK()));
                tclosLabelOutlier.setText(String.valueOf(model.getInputConfig()
                                                              .getRelativeMaxOutliers()));
                tclosButtonApproximate.setSelection(model.getInputConfig()
                                                         .getPracticalMonotonicity());

                for (int i = 0; i < ITEMS_METRIC.length; i++) {
                    if (ITEMS_METRIC[i].getClass()
                                       .equals(model.getInputConfig()
                                                    .getMetric()
                                                    .getClass())) {
                        kanonComboMetric.select(i);
                        ldivComboMetric.select(i);
                        tclosComboMetric.select(i);
                        break;
                    }
                }
                switch (model.getInputConfig().getTClosenessCriterion()) {
                case EMD_EQUAL:
                    tclosComboCriterion.select(0);
                    break;
                case EMD_HIERARCHICAL:
                    tclosComboCriterion.select(1);
                    break;
                }

                root.setSelection(2);
                break;
            }
            root.setRedraw(true);
        } else if (event.target == EventTarget.INPUT) {
            SWTUtil.enable(root);
        }
    }
}
