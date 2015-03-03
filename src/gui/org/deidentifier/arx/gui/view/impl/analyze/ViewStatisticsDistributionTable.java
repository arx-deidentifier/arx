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
package org.deidentifier.arx.gui.view.impl.analyze;

import java.text.DecimalFormat;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This view displays a frequency distribution.
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsDistributionTable extends ViewStatistics<AnalysisContextVisualizationDistribution> {

    /** Internal stuff. */
    private ComponentTable  table;
    
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
    public ViewStatisticsDistributionTable(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControl(Composite parent) {

        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_TABLE);
        config.setHorizontalAlignment(SWT.CENTER);
        config.setCellSelectionEnabled(false);
        config.setColumnSelectionEnabled(false);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_FILL_EQUAL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_DEFAULT);

        this.table = new ComponentTable(parent, SWT.NONE, config);
        return this.table.getControl();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#createViewConfig(org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext)
     */
    @Override
    protected AnalysisContextVisualizationDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationDistribution(context);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#doReset()
     */
    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        this.table.clear();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#doUpdate(org.deidentifier.arx.gui.view.impl.analyze.AnalysisContextVisualization)
     */
    @Override
    protected void doUpdate(AnalysisContextVisualizationDistribution context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final Hierarchy hierarchy = context.context.getHierarchy(context.context.getContext(), context.attribute);
        final DataHandle handle = context.handle;
        final int column = handle.getColumnIndexOf(context.attribute);
        
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            private boolean                         stopped = false;
            private StatisticsFrequencyDistribution distribution;
            
            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                if (stopped) {
                    return;
                }

                // Retrieve
                final DecimalFormat format = new DecimalFormat("##0.00000");

                // Now update the table
                table.setData(new IDataProvider() {
                    public int getColumnCount() {
                        return 2;
                    }
                    public Object getDataValue(int arg0, int arg1) {
                        return arg0 == 0 ? distribution.values[arg1] : format.format(distribution.frequency[arg1]*100d)+"%";
                    }
                    public int getRowCount() {
                        return distribution.values.length;
                    }
                    public void setDataValue(int arg0, int arg1, Object arg2) { 
                        /* Ignore */
                    }
                }, new String[] { "Value", "Frequency" });
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
                this.distribution = builder.getFrequencyDistribution(column, hierarchy);


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
}
