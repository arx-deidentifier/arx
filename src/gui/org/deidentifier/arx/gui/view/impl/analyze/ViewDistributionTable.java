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
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
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

/**
 * This view displays a frequency distribution
 * @author Fabian Prasser
 */
public class ViewDistributionTable implements IView {
    
    /** Internal stuff */
    private final Composite             parent;
    /** Internal stuff */
    private final ComponentTable        table;
    /** Internal stuff */
    private final ComponentStatus       status;
    
    /** Internal stuff */
    private final ModelPart             reset;
    /** Internal stuff */
    private final Controller            controller;
    /** Cache */
    private final Map<String, double[]> cachedFrequencies = new HashMap<String, double[]>();
    /** Cache */
    private final Map<String, String[]> cachedValues      = new HashMap<String, String[]>();

    /** Internal stuff */
    private String                      attribute;
    /** Internal stuff */
    private Context                     context;
    /** Internal stuff */
    private final ModelPart             target;
    /** Internal stuff */
    private Model                       model;
    /** Internal stuff */
    private AnalysisContext             acontext          = new AnalysisContext();
    /** Internal stuff */
    private AnalysisManager             manager;
    
    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewDistributionTable(final Composite parent,
                            final Controller controller,
                            final ModelPart target,
                            final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VISUALIZATION, this);
        controller.addListener(target, this);
        this.controller = controller;
        
        if (reset != null) {
            controller.addListener(reset, this);
        }
        
        this.reset = reset;
        this.target = target;
        this.parent = parent;
        
        this.parent.setLayout(new StackLayout());
        this.table = new ComponentTable(parent);
        this.status = new ComponentStatus(controller,
                                          parent, 
                                          this.table.getControl());
        this.manager = new AnalysisManager(this.parent.getDisplay());
        reset();
    }

    @Override
    public void dispose() {
        clearCache();
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
            
            clearCache();
            update();
        }

        if (event.part == reset) {
            
            clearCache();
            reset();
            
        } else if (event.part == target) {
            
            clearCache();
            update();
            
        } else if (event.part == ModelPart.MODEL) {
            
            this.model = (Model) event.data;
            this.acontext.setModel(model);
            this.acontext.setTarget(target);
            clearCache();
            reset();

        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {

            this.attribute = (String) event.data;
            update();
            
        } else if (event.part == ModelPart.DATA_TYPE) {

            this.cachedFrequencies.remove((String) event.data);
            this.cachedValues.remove((String) event.data);
            if (this.attribute.equals((String) event.data)) {
                update();
            }
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {

            this.attribute = (String) event.data;
            update();
             
        } else if (event.part == ModelPart.VISUALIZATION) {
            
            update();
            
        } else if (event.part == ModelPart.VIEW_CONFIG) {
            
            clearCache();
            update();
        }
    }

    /**
     * Clears the cache
     */
    private void clearCache() {
        cachedFrequencies.clear();
        cachedValues.clear();
    }

    /**
     * Updates the view
     */
    private void update() {
        
        if (model != null && !model.isVisualizationEnabled()) {
            clearCache();
            reset();
            return;
        }

        // Obtain context
        final Context context = acontext.getContext();
        if (context==null) {
            clearCache();
            reset();
            return;
        }
        if (!context.equals(this.context)) {
            this.cachedFrequencies.clear();
            this.context = context;
        }

        // Check
        if (context.config == null || context.handle == null) { 
            clearCache();
            reset();
            return; 
        }

        // The statistics builder
        final StatisticsBuilder builder = context.handle.getStatistics().clone();
        final Hierarchy hierarchy = acontext.getHierarchy(context, attribute);
        final DataHandle handle = context.handle;
        final int column = handle.getColumnIndexOf(attribute);
        
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            @Override
            public void stop() {
                builder.stop();
            }

            @Override
            public void run() {

                // Update cache
                if (!cachedFrequencies.containsKey(attribute)) {
                    if (column >= 0){
                        StatisticsFrequencyDistribution distribution = builder.getFrequencyDistribution(column, hierarchy);
                        cachedFrequencies.put(attribute, distribution.frequency);
                        cachedValues.put(attribute, distribution.values);
                    }
                }  
            }

            @Override
            public void onFinish() {
                if (cachedFrequencies.isEmpty() || (cachedFrequencies.get(attribute) == null)) {
                    // Reset
                    reset();
                } else {
                    // Update chart

                    // Retrieve
                    final double[] frequencies = cachedFrequencies.get(attribute);
                    final String[] labels = cachedValues.get(attribute);
                    final DecimalFormat format = new DecimalFormat("##0.00000");

                    // Now update the table
                    table.setTable(new IDataProvider() {
                        public int getColumnCount() {
                            return 2;
                        }
                        public Object getDataValue(int arg0, int arg1) {
                            return arg0 == 0 ? labels[arg1] : format.format(frequencies[arg1]*100d)+"%";
                        }
                        public int getRowCount() {
                            return labels.length;
                        }
                        public void setDataValue(int arg0, int arg1, Object arg2) { 
                            /* Ignore */
                        }
                    }, new String[] { "Value", "Frequency" });
                    status.setDone();
                }
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
