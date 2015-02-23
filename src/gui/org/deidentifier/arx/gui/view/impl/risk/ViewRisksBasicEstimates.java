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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.utility.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk.StatisticalModel;
import org.deidentifier.arx.risk.RiskModelSampleBasedReidentificationRisk;
import org.deidentifier.arx.risk.RiskModelSampleBasedUniquenessRisk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksBasicEstimates extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private Composite         root;

    /** View */
    private DynamicTable      table;

    /** View */
    private List<TableItem>   items;

    /** View */
    private List<TableColumn> columns;

    /** View */
    private DecimalFormat     format;

    /** Internal stuff. */
    private AnalysisManager   manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksBasicEstimates(final Composite parent,
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
        item.setText(1, format.format(value * 100d));
        items.add(item);
    }

    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(String label, StatisticalModel value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label);
        item.setText(1, value == null ? "N/A" : value.toString());
        items.add(item);
    }

    @Override
    protected Control createControl(Composite parent) {
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        items   = new ArrayList<TableItem>();
        columns = new ArrayList<TableColumn>();
        format  = new DecimalFormat("##0.00000");
        
        table = new DynamicTable(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.6")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.7")); //$NON-NLS-1$
        columns.add(c);
        for (final TableColumn col : columns) {
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
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        table.setRedraw(true);
    }
    
    @Override
    protected void doUpdate(AnalysisContextRisk context) {

        // The statistics builder
        final RiskEstimateBuilderInterruptible builder = getBuilder(context);
        
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            private boolean          stopped = false;
            private double           lowestRisk;
            private double           fractionOfTuplesAffectedByLowestRisk;
            private double           averageRisk;
            private double           highestRisk;
            private double           fractionOfTuplesAffectedByHighestRisk;
            private double           fractionOfUniqueTuples;
            private double           fractionOfUniqueTuplesDankarWithoutSNB;
            private StatisticalModel dankarModelWithoutSNB;

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

                if (stopped) {
                    return;
                }

                table.setRedraw(false);
                for (final TableItem i : items) {
                    i.dispose();
                }
                items.clear();
                
                createItem(Resources.getMessage("RiskAnalysis.14"), lowestRisk);
                createItem(Resources.getMessage("RiskAnalysis.17"), fractionOfTuplesAffectedByLowestRisk);
                createItem(Resources.getMessage("RiskAnalysis.8"), averageRisk);
                createItem(Resources.getMessage("RiskAnalysis.9"), highestRisk);
                createItem(Resources.getMessage("RiskAnalysis.10"), fractionOfTuplesAffectedByHighestRisk);
                createItem(Resources.getMessage("RiskAnalysis.11"), fractionOfUniqueTuples);
                createItem(Resources.getMessage("RiskAnalysis.12"), fractionOfUniqueTuplesDankarWithoutSNB);
                createItem(Resources.getMessage("RiskAnalysis.18"), dankarModelWithoutSNB);

                table.setRedraw(true);
                
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                setStatusWorking();
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Perform work
                // TODO: This can be made more efficient
                RiskModelSampleBasedReidentificationRisk samReidModel = builder.getSampleBasedReidentificationRisk();
                RiskModelSampleBasedUniquenessRisk samUniqueModel = builder.getSampleBasedUniquenessRisk();
                RiskModelPopulationBasedUniquenessRisk popUniqueModel = builder.getPopulationBasedUniquenessRisk();
                
                lowestRisk = samReidModel.getLowestRisk();
                fractionOfTuplesAffectedByLowestRisk = samReidModel.getFractionOfTuplesAffectedByLowestRisk();
                averageRisk = samReidModel.getAverageRisk();
                highestRisk = samReidModel.getHighestRisk();
                fractionOfTuplesAffectedByHighestRisk = samReidModel.getFractionOfTuplesAffectedByHighestRisk();
                fractionOfUniqueTuples = samUniqueModel.getFractionOfUniqueTuples();
                fractionOfUniqueTuplesDankarWithoutSNB = popUniqueModel.getFractionOfUniqueTuplesDankarWithoutSNB();
                dankarModelWithoutSNB = popUniqueModel.getDankarModelWithoutSNB();

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                builder.interrupt();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
    }

    @Override
    protected ProgressProvider getProgressProvider() {
        return null;
    }
}
