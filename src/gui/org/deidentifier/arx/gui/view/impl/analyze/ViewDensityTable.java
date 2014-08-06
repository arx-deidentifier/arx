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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext.Context;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;

import cern.colt.GenericSorting;
import cern.colt.Sorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * This class displays a contingency table
 * @author Fabian Prasser
 */
public class ViewDensityTable implements IView {

    /** Internal stuff */
    private AnalysisContext       context = new AnalysisContext();
    /** Internal stuff */
    private final Controller      controller;
    /** Internal stuff */
    private Model                 model;
    /** Internal stuff */
    private final ModelPart       reset;
    /** Internal stuff */
    private final ModelPart       target;
    /** Internal stuff */
    private final ComponentTable  table;
    /** Internal stuff */
    private final ComponentStatus status;
    /** Internal stuff */
    private AnalysisManager       manager;

	/**
	 * Creates a new density plot
	 * @param parent
	 * @param controller
	 * @param target
	 * @param reset
	 */
    public ViewDensityTable(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.VISUALIZATION, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        // Create controls
        parent.setLayout(new StackLayout());
        this.table = new ComponentTable(parent);
        this.status = new ComponentStatus(controller,
                                          parent, 
                                          table.getControl());
        this.manager = new AnalysisManager(parent.getDisplay());
        
        reset();
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        this.table.setEmpty();
        this.status.setEmpty();
    }
    
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            update();
        }

        if (event.part == reset) {
            reset();
            
        } else if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.model.resetAttributePair();
            this.context.setModel(model);
            this.context.setTarget(target);
            reset();

        } else if (event.part == target ||
                   event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.VIEW_CONFIG ||
                   event.part == ModelPart.VISUALIZATION) {
            
            update();
        }
    }

    /**
     * Redraws the plot
     */
    private void update() {

        if (!this.status.isVisible()) return;
        
        if (model != null && !model.isVisualizationEnabled()) {
            reset();
            return;
        }
        
        if (model == null ||
            model.getAttributePair() == null ||
            model.getAttributePair()[0] == null ||
            model.getAttributePair()[1] == null) {
            reset();
            return;
        }

        // Obtain the right handle
        final Context context = this.context.getContext();
        if (context==null) {
            reset();
            return;
        }
        DataHandle handle = context.handle;
        if (handle == null) {
            reset();
            return;
        }
        
        String attribute1 = model.getAttributePair()[0];
        String attribute2 = model.getAttributePair()[1];
        final int column1 = handle.getColumnIndexOf(attribute1);
        final int column2 = handle.getColumnIndexOf(attribute2);
        final StatisticsBuilder builder = handle.getStatistics().clone();
            
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            private boolean stopped = false;
            private StatisticsContingencyTable contingency; 
            private int[][] outputValues;
            private double[][] outputFrequencies;

            @Override
            public void stop() {
                stopped = true;
                builder.stop();
            }

            @Override
            public void run() {

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
                    if (stopped) return;
                    Entry p = iter.next();
                    inputValues[p.value1].add(p.value2);
                    inputFrequencies[p.value1].add(p.frequency);
                }
                
                // Convert
                outputValues = new int[inputValues.length][];
                outputFrequencies = new double[inputFrequencies.length][];
                for (int i=0; i<outputValues.length; i++){
                    if (stopped) return;
                    List<Integer> rowValuesAsList = inputValues[i];
                    List<Double> rowFrequenciesAsList = inputFrequencies[i];
                    int[] rowValues = new int[rowValuesAsList.size()];
                    double[] rowFrequencies = new double[rowFrequenciesAsList.size()];
                    for (int j=0; j<rowValues.length; j++){
                        if (stopped) return;
                        rowValues[j] = inputValues[i].get(j);
                        rowFrequencies[j] = inputFrequencies[i].get(j);
                    }
                    outputValues[i] = rowValues;
                    outputFrequencies[i] = rowFrequencies;
                }
                
                // Sort
                for (int i=0; i<outputValues.length; i++) {
                    if (stopped) return;
                    final int[] rowValues = outputValues[i];
                    final double[] rowFrequencies = outputFrequencies[i];
                    GenericSorting.quickSort(0, rowValues.length, new IntComparator(){
                        public int compare(int arg0, int arg1) {
                            if (stopped) throw new RuntimeException("Interrupted");
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
                }
            }

            @Override
            public void onFinish() {

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
                
                status.setDone();
            }

            @Override
            public void onError() {
                status.setEmpty();
            }

            @Override
            public void onInterrupt() {
                status.setEmpty();
            }
        };
        
        this.status.setWorking();
        this.manager.start(analysis);

    }
}
