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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskMonitor;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskThresholds;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledSeparator;
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleSummary.JournalistRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This view displays risk estimates according to different attacker models
 *
 * @author Fabian Prasser
 */
public class ViewRisksAttackerModels extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private static final String  MESSAGE_CAPTION1 = Resources.getMessage("ViewRisksReIdentification.0"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_CAPTION2 = Resources.getMessage("ViewRisksReIdentification.1"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_CAPTION3 = Resources.getMessage("ViewRisksReIdentification.2"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_LABEL1   = Resources.getMessage("ViewRisksReIdentification.3"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_LABEL2   = Resources.getMessage("ViewRisksReIdentification.4"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_LABEL3   = Resources.getMessage("ViewRisksReIdentification.5"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_SHORT1   = Resources.getMessage("ViewRisksReIdentification.6"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_SHORT2   = Resources.getMessage("ViewRisksReIdentification.7"); //$NON-NLS-1$
    /** View */
    private static final String  MESSAGE_SHORT3   = Resources.getMessage("ViewRisksReIdentification.8"); //$NON-NLS-1$

    /** View */
    private Composite               root;
    /** View */
    private ComponentRiskMonitor    prosecutor1;
    /** View */
    private ComponentRiskMonitor    prosecutor2;
    /** View */
    private ComponentRiskMonitor    prosecutor3;
    /** View */
    private ComponentRiskMonitor    journalist1;
    /** View */
    private ComponentRiskMonitor    journalist2;
    /** View */
    private ComponentRiskMonitor    journalist3;
    /** View */
    private ComponentRiskMonitor    marketer1;
    /** View */
    private ComponentRiskThresholds riskThresholds;

    /** Internal stuff. */
    private AnalysisManager      manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksAttackerModels(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.RISK_THRESHOLD_MAIN, this);
        controller.addListener(ModelPart.RISK_THRESHOLD_DERIVED, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {            
            triggerUpdate();
        }
        if (event.part == ModelPart.RISK_THRESHOLD_MAIN) {
            handleThresholdUpdateInSettings();
            triggerUpdate();
        }
        
        if (event.part == ModelPart.RISK_THRESHOLD_DERIVED) {
            handleThresholdUpdateInSettings();
            handleThresholdUpdateInMonitors();
        }
    }

    /**
     * Handles updates of risk thresholds
     */
    private void handleThresholdUpdateInMonitors() {
        prosecutor1.setThreshold(super.getModel().getRiskModel().getRiskThresholdRecordsAtRisk());
        prosecutor2.setThreshold(super.getModel().getRiskModel().getRiskThresholdHighestRisk());
        prosecutor3.setThreshold(super.getModel().getRiskModel().getRiskThresholdSuccessRate());
        journalist1.setThreshold(super.getModel().getRiskModel().getRiskThresholdRecordsAtRisk());
        journalist2.setThreshold(super.getModel().getRiskModel().getRiskThresholdHighestRisk());
        journalist3.setThreshold(super.getModel().getRiskModel().getRiskThresholdSuccessRate());
        marketer1.setThreshold(super.getModel().getRiskModel().getRiskThresholdSuccessRate());
    }

    /**
     * Handles updates of risk thresholds
     */
    private void handleThresholdUpdateInSettings() {
        if (riskThresholds != null) {
            riskThresholds.setThresholdHighestRisk(super.getModel().getRiskModel().getRiskThresholdHighestRisk());
            riskThresholds.setThresholdRecordsAtRisk(super.getModel().getRiskModel().getRiskThresholdRecordsAtRisk());
            riskThresholds.setThresholdSuccessRate(super.getModel().getRiskModel().getRiskThresholdSuccessRate());
        }
    }

    @Override
    protected Control createControl(Composite parent) {

        GridLayout layout = SWTUtil.createGridLayoutWithEqualWidth(3);
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        layout.makeColumnsEqualWidth = true;
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(layout);
        
        // Prepare
        GridData separatordata = SWTUtil.createFillHorizontallyGridData(true, 3);
        separatordata.verticalIndent = 0;

        // Prosecutor
        ComponentTitledSeparator separator = new ComponentTitledSeparator(root, SWT.NONE);
        separator.setLayoutData(separatordata);
        separator.setText(MESSAGE_CAPTION1);
        separator.setImage(controller.getResources().getManagedImage("prosecutor.png")); //$NON-NLS-1$
        
        prosecutor1 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL1, MESSAGE_SHORT1);
        prosecutor2 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL2, MESSAGE_SHORT2);
        prosecutor3 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL3, MESSAGE_SHORT3);        
        prosecutor1.setLayoutData(SWTUtil.createFillGridData());
        prosecutor2.setLayoutData(SWTUtil.createFillGridData());
        prosecutor3.setLayoutData(SWTUtil.createFillGridData());
        
        // Journalist
        separator = new ComponentTitledSeparator(root, SWT.NONE);
        separator.setLayoutData(separatordata);
        separator.setText(MESSAGE_CAPTION2);
        separator.setImage(controller.getResources().getManagedImage("journalist.png")); //$NON-NLS-1$
        
        journalist1 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL1, MESSAGE_SHORT1);
        journalist2 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL2, MESSAGE_SHORT2);
        journalist3 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL3, MESSAGE_SHORT3);
        journalist1.setLayoutData(SWTUtil.createFillGridData());
        journalist2.setLayoutData(SWTUtil.createFillGridData());
        journalist3.setLayoutData(SWTUtil.createFillGridData());

        // Marketer
        separator = new ComponentTitledSeparator(root, SWT.NONE);
        separator.setLayoutData(separatordata);
        separator.setText(MESSAGE_CAPTION3);
        separator.setImage(controller.getResources().getManagedImage("marketer.png")); //$NON-NLS-1$
        
        marketer1 = new ComponentRiskMonitor(root, controller, MESSAGE_LABEL3, MESSAGE_SHORT3);
        marketer1.setLayoutData(SWTUtil.createFillGridData());
        
        // Slider
        if (isInput()) {
            GridData data = SWTUtil.createFillGridData();
            data.heightHint = 30;
            data.horizontalSpan = 2;
            riskThresholds = new ComponentRiskThresholds(root);
            riskThresholds.setLayoutData(data);
            riskThresholds.addSelectionListenerThresholdHighestRisk(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    if (riskThresholds.getThresholdHighestRisk() != getModel().getRiskModel().getRiskThresholdHighestRisk()) {
                        getModel().getRiskModel().setRiskThresholdHighestRisk(riskThresholds.getThresholdHighestRisk());
                        controller.update(new ModelEvent(this, ModelPart.RISK_THRESHOLD_DERIVED, null));
                        handleThresholdUpdateInMonitors();
                    }
                }
            });
            riskThresholds.addSelectionListenerThresholdHighestRisk(new DelayedChangeListener(1000) {
                @Override
                public void delayedEvent() {
                    getModel().getRiskModel().setRiskThresholdHighestRisk(riskThresholds.getThresholdHighestRisk());
                    controller.update(new ModelEvent(this, ModelPart.RISK_THRESHOLD_MAIN, null));
                    triggerUpdate();
                }
            });
            riskThresholds.addSelectionListenerThresholdRecordsAtRisk(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    if (riskThresholds.getThresholdRecordsAtRisk() != getModel().getRiskModel().getRiskThresholdRecordsAtRisk()) {
                        getModel().getRiskModel().setRiskThresholdRecordsAtRisk(riskThresholds.getThresholdRecordsAtRisk());
                        controller.update(new ModelEvent(this, ModelPart.RISK_THRESHOLD_DERIVED, null));
                        handleThresholdUpdateInMonitors();
                    }
                }
            });
            riskThresholds.addSelectionListenerThresholdSuccessRate(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    if (riskThresholds.getThresholdSuccessRate() != getModel().getRiskModel().getRiskThresholdSuccessRate()) {
                        getModel().getRiskModel().setRiskThresholdSuccessRate(riskThresholds.getThresholdSuccessRate());
                        controller.update(new ModelEvent(this, ModelPart.RISK_THRESHOLD_DERIVED, null));
                        handleThresholdUpdateInMonitors();
                    }
                }
            });
        } else {
            riskThresholds = null;
        }
        return this.root;
    }
    
    @Override
    protected AnalysisContextRisk createViewConfig(AnalysisContext context) {
        return new AnalysisContextRisk(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        setStatusEmpty();
    }


    @Override
    protected void doUpdate(final AnalysisContextRisk context) {

        // Enable/disable
        final RiskEstimateBuilderInterruptible builder = getBuilder(context);
        if (!this.isEnabled() || builder == null) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }
        final Model model = super.getModel();

        // Create an analysis
        Analysis analysis = new Analysis() {

            private boolean        stopped = false;
            private ProsecutorRisk prosecutor;
            private JournalistRisk journalist;
            private MarketerRisk   marketer;
            
            @Override
            public int getProgress() {
                return (int)Math.round((double)builder.getProgress()); 
            }

            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                if (stopped || !isEnabled()) {
                    return;
                }
                
                // Update thresholds
                handleThresholdUpdateInSettings();

                // Update views
                prosecutor1.setRisk(prosecutor.getRecordsAtRisk());
                prosecutor1.setThreshold(model.getRiskModel().getRiskThresholdRecordsAtRisk());
                prosecutor2.setRisk(prosecutor.getHighestRisk());
                prosecutor2.setThreshold(model.getRiskModel().getRiskThresholdHighestRisk());
                prosecutor3.setRisk(prosecutor.getSuccessRate());
                prosecutor3.setThreshold(model.getRiskModel().getRiskThresholdSuccessRate());

                // Update views
                journalist1.setRisk(journalist.getRecordsAtRisk());
                journalist1.setThreshold(model.getRiskModel().getRiskThresholdRecordsAtRisk());
                journalist2.setRisk(journalist.getHighestRisk());
                journalist2.setThreshold(model.getRiskModel().getRiskThresholdHighestRisk());
                journalist3.setRisk(journalist.getSuccessRate());
                journalist3.setThreshold(model.getRiskModel().getRiskThresholdSuccessRate());
                
                // Update views
                marketer1.setRisk(marketer.getSuccessRate());
                marketer1.setThreshold(model.getRiskModel().getRiskThresholdSuccessRate());

                // Layout
                root.layout();
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled() || !isValid()) {
                    setStatusEmpty();
                } else {
                    setStatusWorking();
                }
            }

            @Override
            public void run() throws InterruptedException {

                // Timestamp
                long time = System.currentTimeMillis();

                // Perform work
                RiskModelSampleSummary summary = builder.getSampleBasedRiskSummary(model.getRiskModel().getRiskThresholdHighestRisk());
                prosecutor = summary.getProsecutorRisk();
                journalist = summary.getJournalistRisk();
                marketer = summary.getMarketerRisk();
    
                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped) {
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                if (builder != null) builder.interrupt();
                this.stopped = true;
            }
        };

        this.manager.start(analysis);
    }

    @Override
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return new ComponentStatusLabelProgressProvider(){
            public int getProgress() {
                if (manager == null) {
                    return 0;
                } else {
                    return manager.getProgress();
                }
            }
        };
    }

    @Override
    protected ViewRiskType getViewType() {
        return ViewRiskType.CELL_BASED;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
