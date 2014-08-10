/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.gui.view.impl.analyze;

import java.text.DecimalFormat;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This view displays a frequency distribution
 * @author Fabian Prasser
 */
public class ViewStatisticsDistributionTable extends ViewStatistics<AnalysisContextVisualizationDistribution> {

    /** Internal stuff */
    private ComponentTable  table;
    /** Internal stuff */
    private AnalysisManager manager;

    /**
     * Creates a new instance
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
    
    @Override
    protected Control createControl(Composite parent) {
        this.table = new ComponentTable(parent, SWT.NONE);
        return this.table.getControl();
    }

    @Override
    protected AnalysisContextVisualizationDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationDistribution(context);
    }

    @Override
    protected void doReset() {
        this.table.setEmpty();
    }

    @Override
    protected void doUpdate(AnalysisContextVisualizationDistribution context) {

        // The statistics builder
        final StatisticsBuilder builder = context.handle.getStatistics().clone();
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

                // Retrieve
                final DecimalFormat format = new DecimalFormat("##0.00000");

                // Now update the table
                table.setTable(new IDataProvider() {
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
                setStatusEmpty();
            }

            @Override
            public void run() {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Perform work
                this.distribution = builder.getFrequencyDistribution(column, hierarchy);

                // Our users are patient
                while (System.currentTimeMillis() - time < ViewStatistics.MINIMAL_WORKING_TIME && !stopped){
                    try { Thread.sleep(10); } 
                    catch (InterruptedException e) { /* Ignore*/}
                }
            }

            @Override
            public void stop() {
                builder.stop();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
    }
}
