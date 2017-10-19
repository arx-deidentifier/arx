/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
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
public class ViewStatisticsDistributionTable extends ViewStatistics<AnalysisContextDistribution> {

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
        
        super(parent, controller, target, reset, true);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.HISTOGRAM_TABLE;
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

    @Override
    protected AnalysisContextDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextDistribution(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        this.table.clear();
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(AnalysisContextDistribution context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final Hierarchy hierarchy = context.context.getHierarchy(context.context.getData(), context.attribute);
        final DataHandle handle = context.handle;
        final int column = handle.getColumnIndexOf(context.attribute);
        
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            private boolean                         stopped = false;
            private StatisticsFrequencyDistribution distribution;

            @Override
            public int getProgress() {
                return builder.getProgress();
            }
            
            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                // Check
                if (stopped || !isEnabled()) {
                    return;
                }

                // Now update the table
                table.setData(new IDataProvider() {
                    public int getColumnCount() {
                        return 2;
                    }
                    public Object getDataValue(int arg0, int arg1) {
                        return arg0 == 0 ? distribution.values[arg1] : SWTUtil.getPrettyString(distribution.frequency[arg1]*100d)+"%"; //$NON-NLS-1$
                    }
                    public int getRowCount() {
                        return distribution.values.length;
                    }
                    public void setDataValue(int arg0, int arg1, Object arg2) { 
                        /* Ignore */
                    }
                }, new String[] { Resources.getMessage("ViewStatisticsDistributionTable.2"), Resources.getMessage("ViewStatisticsDistributionTable.3") }); //$NON-NLS-1$ //$NON-NLS-2$
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled()) {
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
    
    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
