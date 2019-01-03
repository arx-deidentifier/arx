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
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.deidentifier.arx.risk.RiskModelSampleRisks;
import org.deidentifier.arx.risk.RiskModelSampleUniqueness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksOverview extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private Composite       root;

    /** View */
    private DynamicTable    table;

    /** Internal stuff. */
    private AnalysisManager manager;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksOverview(final Composite parent,
                                          final Controller controller,
                                          final ModelPart target,
                                          final ModelPart reset) {
        
        super(parent, controller, target, reset);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE || event.part == ModelPart.POPULATION_MODEL) {
            triggerUpdate();
        }
    }

    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, double value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setData("1", value);
    }

    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, PopulationUniquenessModel value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setText(1, value == null ? "N/A" : value.toString()); //$NON-NLS-1$
    }
    
    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, String value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setText(1, value);
    }

    @Override
    protected Control createControl(Composite parent) {
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(1));
        
        table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        table.setLayoutData(SWTUtil.createFillGridData());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.6")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.7")); //$NON-NLS-1$
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        return root;
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
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
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
        Analysis analysis = new Analysis(){

            private boolean                   stopped = false;
            private double                    lowestRisk;
            private double                    fractionOfTuplesAffectedByLowestRisk;
            private double                    averageRisk;
            private double                    highestRisk;
            private double                    fractionOfTuplesAffectedByHighestRisk;
            private double                    estimatedProsecutorRisk;
            private double                    estimatedJournalistRisk;
            private double                    estimatedMarketerRisk;
            private double                    fractionOfUniqueTuples;
            private double                    fractionOfUniqueTuplesDankar;
            private PopulationUniquenessModel populationModel;

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

                table.setRedraw(false);
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }
                
                createItem(Resources.getMessage("RiskAnalysis.14"), lowestRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.17"), fractionOfTuplesAffectedByLowestRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.8"), averageRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.9"), highestRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.10"), fractionOfTuplesAffectedByHighestRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.40"), estimatedProsecutorRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.41"), estimatedJournalistRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.42"), estimatedMarketerRisk); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.11"), fractionOfUniqueTuples); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.12"), fractionOfUniqueTuplesDankar); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.18"), populationModel); //$NON-NLS-1$
                createItem(Resources.getMessage("RiskAnalysis.25"), getQuasiIdentifiers(context)); //$NON-NLS-1$

                table.setRedraw(true);
                table.layout();
                
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
                RiskModelSampleRisks samReidModel = builder.getSampleBasedReidentificationRisk();
                RiskModelSampleUniqueness samUniqueModel = builder.getSampleBasedUniquenessRisk();
                RiskModelPopulationUniqueness popUniqueModel = builder.getPopulationBasedUniquenessRisk();
                
                lowestRisk = samReidModel.getLowestRisk();
                fractionOfTuplesAffectedByLowestRisk = samReidModel.getFractionOfTuplesAffectedByLowestRisk();
                averageRisk = samReidModel.getAverageRisk();
                highestRisk = samReidModel.getHighestRisk();
                fractionOfTuplesAffectedByHighestRisk = samReidModel.getFractionOfTuplesAffectedByHighestRisk();
                estimatedProsecutorRisk = samReidModel.getEstimatedProsecutorRisk();
                estimatedJournalistRisk = samReidModel.getEstimatedJournalistRisk();
                estimatedMarketerRisk = samReidModel.getEstimatedMarketerRisk();
                fractionOfUniqueTuples = samUniqueModel.getFractionOfUniqueTuples();
                fractionOfUniqueTuplesDankar = popUniqueModel.getFractionOfUniqueTuplesDankar();
                populationModel = popUniqueModel.getPopulationUniquenessModel();

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
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
        return ViewRiskType.OVERVIEW;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
