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

package org.deidentifier.arx.gui.view.impl.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import cern.colt.GenericSorting;
import cern.colt.Sorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * This class displays a contingency table.
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsContingencyTable extends ViewStatistics<AnalysisContextContingency> {

    /** Internal stuff. */
    private ComponentTable  table;
    
    /** Internal stuff. */
    private AnalysisManager manager;

	/**
     * Creates a new density plot.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsContingencyTable(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {
        
        super(parent, controller, target, reset, true);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.CONTINGENCY_TABLE;
    }

    @Override
    protected Control createControl(Composite parent) {
        
        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_GRID);
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
    protected AnalysisContextContingency createViewConfig(AnalysisContext context) {
        return new AnalysisContextContingency(context);
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
    protected void doUpdate(AnalysisContextContingency context) {

        final int column1 = context.handle.getColumnIndexOf(context.attribute1);
        final int column2 = context.handle.getColumnIndexOf(context.attribute2);
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
            
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean                    stopped = false;
            private StatisticsContingencyTable contingency;
            private int[][]                    outputValues;
            private double[][]                 outputFrequencies;

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
                
                // Check
                if (stopped || !isEnabled()) {
                    return;
                }

                // Set data
                table.setData(new IDataProvider(){

                    @Override
                    public int getColumnCount() {
                        return contingency.values1.length;
                    }

                    @Override
                    public Object getDataValue(int arg0, int arg1) {
                        int index = Sorting.binarySearchFromTo(outputValues[arg0], arg1, 0, outputValues[arg0].length - 1);
                        return SWTUtil.getPrettyString((index >= 0 ? outputFrequencies[arg0][index] : 0)*100d)+"%"; //$NON-NLS-1$
                    }

                    @Override
                    public int getRowCount() {
                        return contingency.values2.length;
                    }

                    @Override
                    public void setDataValue(int arg0, int arg1, Object arg2) {
                        // Ignore
                    }
                }, contingency.values2, contingency.values1);
                
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
                contingency = builder.getContingencyTable(column1, column2);

                @SuppressWarnings("unchecked")
                List<Integer>[] inputValues = new List[contingency.values1.length];
                @SuppressWarnings("unchecked")
                List<Double>[] inputFrequencies = new List[contingency.values1.length];
                for (int i=0; i<inputValues.length; i++){
                    inputValues[i] = new ArrayList<Integer>();
                    inputFrequencies[i] = new ArrayList<Double>();
                    if (stopped) return;
                }
                
                // Fill
                Iterator<Entry> iter = contingency.iterator;
                while (iter.hasNext()) {
                    if (stopped) throw new InterruptedException();
                    Entry p = iter.next();
                    inputValues[p.value1].add(p.value2);
                    inputFrequencies[p.value1].add(p.frequency);
                }
                
                // Convert
                outputValues = new int[inputValues.length][];
                outputFrequencies = new double[inputFrequencies.length][];
                for (int i=0; i<outputValues.length; i++){
                    if (stopped) throw new InterruptedException();
                    List<Integer> rowValuesAsList = inputValues[i];
                    List<Double> rowFrequenciesAsList = inputFrequencies[i];
                    int[] rowValues = new int[rowValuesAsList.size()];
                    double[] rowFrequencies = new double[rowFrequenciesAsList.size()];
                    for (int j=0; j<rowValues.length; j++){
                        if (stopped) throw new InterruptedException();
                        rowValues[j] = inputValues[i].get(j);
                        rowFrequencies[j] = inputFrequencies[i].get(j);
                    }
                    outputValues[i] = rowValues;
                    outputFrequencies[i] = rowFrequencies;
                }
                
                // Sort
                for (int i=0; i<outputValues.length; i++) {
                    if (stopped) throw new InterruptedException();
                    final int[] rowValues = outputValues[i];
                    final double[] rowFrequencies = outputFrequencies[i];
                    try {
                        GenericSorting.quickSort(0, rowValues.length, new IntComparator(){
                            public int compare(int arg0, int arg1) {
                                if (stopped) throw new RuntimeException(new InterruptedException());
                                return rowValues[arg0] - rowValues[arg1];
                            }
                        }, new Swapper(){
                            public void swap(int arg0, int arg1) {
                                int temp = rowValues[arg0];
                                rowValues[arg0] = rowValues[arg1];
                                rowValues[arg1] = temp;
                                double temp2 = rowFrequencies[arg0];
                                rowFrequencies[arg0] = rowFrequencies[arg1];
                                rowFrequencies[arg1] = temp2;
                            }
                        });
                    } catch (RuntimeException e) {
                        if (e.getCause() instanceof InterruptedException){
                            throw (InterruptedException)e.getCause();
                        } else {
                            throw e;
                        }
                    }
                }
                
                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                stopped = true;
                builder.interrupt();
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
