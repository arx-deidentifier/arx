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
package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsSummary;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays summary statistics.
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsSummaryTable extends ViewStatistics<AnalysisContextVisualizationDistribution> {
    
    /** View */
    private Composite                      root;

    /** View */
    private DynamicTable                   table;

    /** Internal stuff. */
    private AnalysisManager                manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsSummaryTable(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    protected Control createControl(Composite parent) {

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        this.table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("SummaryStatistics.0")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("SummaryStatistics.1")); //$NON-NLS-1$
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        
        SWTUtil.createGenericTooltip(table);
        return root;
    }

    @Override
    protected AnalysisContextVisualizationDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationDistribution(context);
    }

    @Override
    protected void doReset() {
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
    }

    @Override
    protected void doUpdate(AnalysisContextVisualizationDistribution context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final String attribute = context.attribute;
        
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean           stopped = false;
            private StatisticsSummary summary;

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

                // Now update the table
                table.setRedraw(false);
                table.removeAll();
                
                createItem("Scale of measure", String.valueOf(summary.getScale()));
                createItem("Number of measures", String.valueOf(summary.getNumberOfMeasures()));
                
                if (summary.isModeAvailable()) createItem("Mode", summary.getMode());
                if (summary.isMedianAvailable()) createItem("Median", summary.getMedian());
                if (summary.isMinAvailable()) createItem("Min", summary.getMin());
                if (summary.isMaxAvailable()) createItem("Max", summary.getMax());                
                if (summary.isArithmeticMeanAvailable()) createItem("Arithmetic mean", summary.getArithmeticMean());
                if (summary.isSampleVarianceAvailable()) createItem("Sample variance", summary.getSampleVariance());
                if (summary.isPopulationVarianceAvailable()) createItem("Population variance", summary.getPopulationVariance());
                if (summary.isRangeAvailable()) createItem("Range", summary.getRange());
                if (summary.isKurtosisAvailable()) createItem("Kurtosis", summary.getKurtosis());
                if (summary.isGeometricMeanAvailable()) createItem("Geometric mean", summary.getGeometricMean());

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
                // TODO: This view computes the statistics for all attributes, each time the selected attribute is changed
                // TODO: This is done because of list-wise deletion, could be implemented more efficient anyways, however
                this.summary = builder.getSummaryStatistics(true).get(attribute);

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

    /**
     * Creates a table item
     * @param key
     * @param value
     */
    private void createItem(String key,
                            String value) {
        
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, key);
        item.setText(1, value);
    }
}
