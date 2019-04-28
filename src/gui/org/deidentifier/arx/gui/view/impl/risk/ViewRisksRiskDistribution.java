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
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskProfile;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskProfile.RiskProfile;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.reliability.ParameterTranslation;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelSampleRiskDistribution;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksRiskDistribution extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private ComponentRiskProfile profile;

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
    public ViewRisksRiskDistribution(final Composite parent,
                                   final Controller controller,
                                   final ModelPart target,
                                   final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }
    
    /**
     * Returns the risk profile
     */
    public ComponentRiskProfile getRiskProfile() {
        return this.profile;
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
        this.profile = new ComponentRiskProfile(parent, super.controller);
        this.profile.setYAxisTitle(Resources.getMessage("ViewRisksClassDistributionPlot.0")); //$NON-NLS-1$
        this.profile.setXAxisTitle(Resources.getMessage("ViewRisksClassDistributionPlot.1")); //$NON-NLS-1$
        return this.profile.getControl();
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
        profile.reset();
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

            private boolean  stopped = false;
            private double[] frequencies;
            private double[] cumulative;
            private double[] threshold;
            private double[] lower;
            private double[] upper;

            @Override
            public int getProgress() {
                return 0;
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
                
                // Update
                profile.setProfiles(lower, upper, 
                                    new RiskProfile(Resources.getMessage("ViewRisksClassDistributionPlot.3"), cumulative, true),
                                    new RiskProfile(Resources.getMessage("ViewRisksClassDistributionPlot.2"), frequencies, true),
                                    new RiskProfile(Resources.getMessage("ViewRisksClassDistributionPlot.4"), threshold, false));
                
                // Set status
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
                RiskModelSampleRiskDistribution model = builder.getSampleBasedRiskDistribution();

                // Create arrays
                frequencies = model.getFractionOfRecordsForRiskThresholds().clone();
                cumulative = model.getFractionOfRecordsForCumulativeRiskThresholds().clone();
                threshold = new double[frequencies.length];
                lower = model.getAvailableLowerRiskThresholds().clone();
                upper = model.getAvailableUpperRiskThresholds().clone();
                double enforced = model.getRiskThreshold();
                for (int i = 0; i < frequencies.length; i++) {
                    if (enforced != 1d && ParameterTranslation.getEffectiveRiskThreshold(enforced) < ParameterTranslation.getEffectiveRiskThreshold(upper[i])) {
                        threshold[i] = 1d;
                    }
                }

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
        return null;
    }

    @Override
    protected ViewRiskType getViewType() {
        return ViewRiskType.CLASSES_TABLE;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
