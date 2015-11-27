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
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskMonitor;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleSummary.JournalistRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksReIdentification extends ViewRisks<AnalysisContextRisk> {

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

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(1));
        
        Group group = new Group(root, SWT.SHADOW_ETCHED_IN);
        group.setText("Prosecutor");
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(3));
        
        final String LABEL1 = "Proportion of records with risk higher than treshold";
        final String LABEL2 = "Highest risk of a single record";
        final String LABEL3 = "Proportion of records than can be re-identified on average";
        
        prosecutor1 = new ComponentRiskMonitor(group, LABEL1);
        prosecutor2 = new ComponentRiskMonitor(group, LABEL2);
        prosecutor3 = new ComponentRiskMonitor(group, LABEL3);
        
        prosecutor1.setLayoutData(SWTUtil.createFillGridData());
        prosecutor2.setLayoutData(SWTUtil.createFillGridData());
        prosecutor3.setLayoutData(SWTUtil.createFillGridData());
        
        group = new Group(root, SWT.SHADOW_ETCHED_IN);
        group.setText("Journalist");
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(3));

        journalist1 = new ComponentRiskMonitor(group, LABEL1);
        journalist2 = new ComponentRiskMonitor(group, LABEL2);
        journalist3 = new ComponentRiskMonitor(group, LABEL3);

        journalist1.setLayoutData(SWTUtil.createFillGridData());
        journalist2.setLayoutData(SWTUtil.createFillGridData());
        journalist3.setLayoutData(SWTUtil.createFillGridData());
        
        group = new Group(root, SWT.SHADOW_ETCHED_IN);
        group.setText("Marketer");
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(3));
        
        marketer1 = new ComponentRiskMonitor(group, LABEL3);

        marketer1.setLayoutData(SWTUtil.createFillGridData());
        
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
