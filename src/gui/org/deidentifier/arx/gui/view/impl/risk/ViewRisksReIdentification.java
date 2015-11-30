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
package org.deidentifier.arx.gui.view.impl.risk;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskMonitor;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskSlider;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledSeparator;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleSummary.JournalistRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksReIdentification extends ViewRisks<AnalysisContextRisk> {

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
    private Composite            root;
    /** View */
    private ComponentRiskMonitor prosecutor1;
    /** View */
    private ComponentRiskMonitor prosecutor2;
    /** View */
    private ComponentRiskMonitor prosecutor3;
    /** View */
    private ComponentRiskMonitor journalist1;
    /** View */
    private ComponentRiskMonitor journalist2;
    /** View */
    private ComponentRiskMonitor journalist3;
    /** View */
    private ComponentRiskMonitor marketer1;

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
    public ViewRisksReIdentification(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            triggerUpdate();
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
        
        prosecutor1 = new ComponentRiskMonitor(root, MESSAGE_LABEL1, MESSAGE_SHORT1);
        prosecutor2 = new ComponentRiskMonitor(root, MESSAGE_LABEL2, MESSAGE_SHORT2);
        prosecutor3 = new ComponentRiskMonitor(root, MESSAGE_LABEL3, MESSAGE_SHORT3);        
        prosecutor1.setLayoutData(SWTUtil.createFillGridData());
        prosecutor2.setLayoutData(SWTUtil.createFillGridData());
        prosecutor3.setLayoutData(SWTUtil.createFillGridData());
        
        // Journalist
        separator = new ComponentTitledSeparator(root, SWT.NONE);
        separator.setLayoutData(separatordata);
        separator.setText(MESSAGE_CAPTION2);
        separator.setImage(controller.getResources().getManagedImage("journalist.png")); //$NON-NLS-1$
        
        journalist1 = new ComponentRiskMonitor(root, MESSAGE_LABEL1, MESSAGE_SHORT1);
        journalist2 = new ComponentRiskMonitor(root, MESSAGE_LABEL2, MESSAGE_SHORT2);
        journalist3 = new ComponentRiskMonitor(root, MESSAGE_LABEL3, MESSAGE_SHORT3);
        journalist1.setLayoutData(SWTUtil.createFillGridData());
        journalist2.setLayoutData(SWTUtil.createFillGridData());
        journalist3.setLayoutData(SWTUtil.createFillGridData());

        // Marketer
        separator = new ComponentTitledSeparator(root, SWT.NONE);
        separator.setLayoutData(separatordata);
        separator.setText(MESSAGE_CAPTION3);
        separator.setImage(controller.getResources().getManagedImage("marketer.png")); //$NON-NLS-1$
        
        marketer1 = new ComponentRiskMonitor(root, MESSAGE_LABEL3, MESSAGE_SHORT3);
        marketer1.setLayoutData(SWTUtil.createFillGridData());
        
        // Slider
        if (isInput()) {
            new Label(root, SWT.NONE);
            GridData data = SWTUtil.createFillGridData();
            data.horizontalAlignment = SWT.RIGHT;
            ComponentRiskSlider slider = new ComponentRiskSlider(root);
            slider.setLayoutData(data);
            
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

                // Update views
                prosecutor1.setValue(prosecutor.getProportionOfRecordsWithRiskAboveThreshold());
                prosecutor2.setValue(prosecutor.getMaximumProbabilityOfReIdentification());
                prosecutor3.setValue(prosecutor.getProportionOfRecordsThatCanBeReIdentifiedOnAverage());

                // Update views
                journalist1.setValue(journalist.getProportionOfRecordsWithRiskAboveThreshold());
                journalist2.setValue(journalist.getMaximumProbabilityOfReIdentification());
                journalist3.setValue(journalist.getProportionOfRecordsThatCanBeReIdentifiedOnAverage());
                
                // Update views
                marketer1.setValue(marketer.getProportionOfRecordsThatCanBeReIdentifiedOnAverage());

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
                RiskModelSampleSummary summary = builder.getSampleBasedRiskSummary(0.2d);
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
