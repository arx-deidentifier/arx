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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
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
 * This class displays a contingency table
 * @author Fabian Prasser
 */
public class ViewStatisticsContingencyTable extends ViewStatistics<AnalysisContextVisualizationContingency> {

    /** Internal stuff */
    private ComponentTable  table;
    /** Internal stuff */
    private AnalysisManager manager;

	/**
	 * Creates a new density plot
	 * @param parent
	 * @param controller
	 * @param target
	 * @param reset
	 */
    public ViewStatisticsContingencyTable(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    protected Control createControl(Composite parent) {
        
        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_GRID);
        config.setHorizontalAlignment(SWT.CENTER);
        config.setCellSelectionEnabled(false);
        config.setColumnSelectionEnabled(false);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_EQUAL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_DEFAULT);

        this.table = new ComponentTable(parent, SWT.NONE, config);
        return this.table.getControl();
    }


    @Override
    protected AnalysisContextVisualizationContingency createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationContingency(context);
    }


    @Override
    protected void doReset() {
        this.table.clear();
    }

    @Override
    protected void doUpdate(AnalysisContextVisualizationContingency context) {

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
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {
                
                if (stopped) {
                    return;
                }

                final DecimalFormat format = new DecimalFormat("##0.00000");

                // Set data
                table.setData(new IDataProvider(){

                    @Override
                    public int getColumnCount() {
                        return contingency.values1.length;
                    }

                    @Override
                    public Object getDataValue(int arg0, int arg1) {
                        int index = Sorting.binarySearchFromTo(outputValues[arg0], arg1, 0, outputValues[arg0].length - 1);
                        return format.format((index >= 0 ? outputFrequencies[arg0][index] : 0)*100d)+"%";
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
                setStatusWorking();
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
}
