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

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext.Context;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.layout.FillLayout;
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
        
        this.parent.setLayout(new FillLayout());
        this.table = new ComponentTable(parent);
        
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
        Context context = acontext.getContext();
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

        // Update cache
        if (!cachedFrequencies.containsKey(attribute)) {
            
            DataHandle handle = context.handle;
            int column = handle.getColumnIndexOf(attribute);
            
            if (column >= 0){
                Hierarchy hierarchy = acontext.getHierarchy(context, attribute); 
                StatisticsFrequencyDistribution distribution = handle.getStatistics().getFrequencyDistribution(column, hierarchy);
                cachedFrequencies.put(attribute, distribution.frequency);
                cachedValues.put(attribute, distribution.values);
            }
        }
        
        // Check
        if (cachedFrequencies.isEmpty() || (cachedFrequencies.get(attribute) == null)) { return; }
        
        // Retrieve
        final double[] frequencies = cachedFrequencies.get(attribute);
        final String[] labels = cachedValues.get(attribute);

        // Now update the table
        table.setTable(new IDataProvider() {
            public int getColumnCount() {
                return 2;
            }
            public Object getDataValue(int arg0, int arg1) {
                return arg0 == 0 ? labels[arg1] : frequencies[arg1];
            }
            public int getRowCount() {
                return labels.length;
            }
            public void setDataValue(int arg0, int arg1, Object arg2) { 
                /* Ignore */
            }
        }, new String[] { "Value", "Frequency" });
    }
}
