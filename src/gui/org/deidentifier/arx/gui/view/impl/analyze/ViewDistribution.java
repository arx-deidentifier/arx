/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext.Context;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.Range;

/**
 * This view displays a frequency distribution
 * @author Fabian Prasser
 */
public class ViewDistribution implements IView {

    private Chart                       chart;

    /** Internal stuff */
    private final Composite             parent;
    /** Internal stuff */
    private final ModelPart             reset;
    /** Internal stuff */
    private final Controller            controller;
    /** Internal stuff */
    private final Map<String, double[]> cache    = new HashMap<String, double[]>();

    /** Internal stuff */
    private String                      attribute;
    /** Internal stuff */
    private Context                     context;
    /** Internal stuff */
    private final ModelPart             target;
    /** Internal stuff */
    private Model                       model;
    /** Internal stuff */
    private AnalysisContext             acontext = new AnalysisContext();

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewDistribution(final Composite parent,
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
        reset();
    }

    @Override
    public void dispose() {
        clearCache();
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(parent, SWT.NONE);
        chart.setOrientation(SWT.HORIZONTAL);
        final ITitle graphTitle = chart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(MainWindow.FONT);
        
        chart.setBackground(parent.getBackground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){
        	int r = chart.getBackground().getRed()-13;
        	int g = chart.getBackground().getGreen()-13;
        	int b = chart.getBackground().getBlue()-13;
        	r = r>0 ? r : 0;
        	r = g>0 ? g : 0;
        	r = b>0 ? b : 0;
        	final org.eclipse.swt.graphics.Color c2 = new org.eclipse.swt.graphics.Color(controller.getResources().getDisplay(), r, g, b);
        	chart.setBackground(c2);
        	chart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    c2.dispose();
                } 
            });
        }

        final IAxisSet axisSet = chart.getAxisSet();
        final IAxis yAxis = axisSet.getYAxis(0);
        final IAxis xAxis = axisSet.getXAxis(0);
        final ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(MainWindow.FONT);
        yAxis.getTitle().setFont(MainWindow.FONT);
        xAxis.getTick().setFont(MainWindow.FONT);
        yAxis.getTick().setFont(MainWindow.FONT);

        final ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(""); //$NON-NLS-1$
        chart.setEnabled(false);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            
            if (chart != null) chart.setEnabled(true);
            clearCache();
            update();
        }

        if (event.part == reset) {
            
            clearCache();
            reset();
            
        } else if (event.part == target) {
            
            if (chart != null) chart.setEnabled(true);
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
            if (chart != null) chart.setEnabled(true);
            update();
            
        } else if (event.part == ModelPart.DATA_TYPE) {

            this.cache.remove((String) event.data);
            if (this.attribute.equals((String) event.data)) {
                if (chart != null) chart.setEnabled(true);
                update();
            }
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {

            this.attribute = (String) event.data;
            if (chart != null) chart.setEnabled(true);
            update();
             
        } else if (event.part == ModelPart.VISUALIZATION) {
            
            update();
            
        } else if (event.part == ModelPart.VIEW_CONFIG) {
            
            if (chart != null) chart.setEnabled(true);
            clearCache();
            update();
        }
    }

    /**
     * Clears the cache
     */
    private void clearCache() {
        cache.clear();
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
            this.cache.clear();
            this.context = context;
        }

        // Check
        if (context.config == null || context.handle == null) { 
            clearCache();
            reset();
            return; 
        }

        // Update cache
        if (!cache.containsKey(attribute)) {
            
            DataHandle handle = context.handle;
            int column = handle.getColumnIndexOf(attribute);
            
            if (column >= 0){
                Hierarchy hierarchy = acontext.getHierarchy(context, attribute); 
                double[] frequency = handle.getStatistics().getFrequencyDistribution(column, hierarchy).frequency;
                cache.put(attribute, frequency);
            }
        }
        
        // Check
        if (cache.isEmpty() || (cache.get(attribute) == null)) { return; }

        // Update chart
        chart.setRedraw(false);

        final ISeriesSet seriesSet = chart.getSeriesSet();
        final IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                      Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(MainWindow.FONT);
        series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        series.setYSeries(cache.get(attribute));

        final IAxisSet axisSet = chart.getAxisSet();

        final IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));
        yAxis.adjustRange();

        final IAxis xAxis = axisSet.getXAxis(0);
        xAxis.adjustRange();

        chart.updateLayout();
        chart.update();
        chart.setRedraw(true);
        chart.redraw();
    }
}
