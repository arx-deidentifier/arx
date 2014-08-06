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
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext.Context;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.Range;

/**
 * This view displays a frequency distribution
 * @author Fabian Prasser
 */
public class ViewDistribution implements IView {
    
    /** Minimal width of a category label*/
    private static final int MIN_CATEGORY_WIDTH = 10;

    /** The chart*/
    private Chart                       chart;

    /** Internal stuff */
    private final Composite             root;
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
    private AnalysisContext             acontext           = new AnalysisContext();
    /** Internal stuff */
    private AnalysisManager             manager;
    /** Internal stuff */
    private final ComponentStatus       status;

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
        
        this.manager = new AnalysisManager(parent.getDisplay());
        this.reset = reset;
        this.target = target;
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        parent.setLayout(new StackLayout());
        this.status = new ComponentStatus(controller,
                                          parent, 
                                          this.root);

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
        chart = new Chart(root, SWT.NONE);
        chart.setOrientation(SWT.HORIZONTAL);
        
        // Show/Hide axis
        chart.addControlListener(new ControlAdapter(){
            @Override
            public void controlResized(ControlEvent arg0) {
                updateCategories();
            }
        });
        
        // Tooltip
        chart.getPlotArea().addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                IAxisSet axisSet = chart.getAxisSet();
                if (axisSet != null) {
                    IAxis xAxis = axisSet.getXAxis(0);
                    if (xAxis != null) {
                        String[] series = xAxis.getCategorySeries();
                        ISeries[] data = chart.getSeriesSet().getSeries();
                        if (data != null && data.length>0 && series != null) {
                            int x = (int) Math.round(xAxis.getDataCoordinate(event.x));
                            if (x >= 0 && x < series.length) {
                                chart.getPlotArea().setToolTipText("("+series[x]+", "+data[0].getYSeries()[x]+")");
                                return;
                            }
                        }
                    }
                }
                chart.getPlotArea().setToolTipText(null);
            }
        });

        // Update font
        FontData[] fd = chart.getFont().getFontData();
        fd[0].setHeight(8);
        chart.setFont(new Font(chart.getDisplay(), fd[0]));
        
        // Update title
        ITitle graphTitle = chart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(chart.getFont());
        
        // Set colors
        chart.setBackground(root.getBackground());
        chart.setForeground(root.getForeground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){
        	int r = chart.getBackground().getRed()-13;
        	int g = chart.getBackground().getGreen()-13;
        	int b = chart.getBackground().getBlue()-13;
        	r = r>0 ? r : 0;
        	r = g>0 ? g : 0;
        	r = b>0 ? b : 0;
        	final Color c2 = new Color(controller.getResources().getDisplay(), r, g, b);
        	chart.setBackground(c2);
        	chart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    c2.dispose();
                } 
            });
        }

        // Initialize axes
        IAxisSet axisSet = chart.getAxisSet();
        IAxis yAxis = axisSet.getYAxis(0);
        IAxis xAxis = axisSet.getXAxis(0);
        ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(chart.getFont());
        yAxis.getTitle().setFont(chart.getFont());
        xAxis.getTick().setFont(chart.getFont());
        yAxis.getTick().setFont(chart.getFont());
        xAxis.getTick().setForeground(chart.getForeground());
        yAxis.getTick().setForeground(chart.getForeground());
        xAxis.getTitle().setForeground(chart.getForeground());
        yAxis.getTitle().setForeground(chart.getForeground());

        // Initialize y-axis
        ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText("Frequency"); //$NON-NLS-1$
        chart.setEnabled(false);
        updateCategories();
        status.setEmpty();
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

            this.cachedFrequencies.remove((String) event.data);
            this.cachedValues.remove((String) event.data);
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
        cachedFrequencies.clear();
        cachedValues.clear();
    }

    /**
     * Updates the view
     */
    private void update() {

        if (!this.status.isVisible()) return;
        
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
                    chart.setRedraw(false);

                    ISeriesSet seriesSet = chart.getSeriesSet();
                    IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                            Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
                    series.getLabel().setVisible(false);
                    series.getLabel().setFont(chart.getFont());
                    series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                    series.setYSeries(cachedFrequencies.get(attribute));
                    chart.getLegend().setVisible(false);

                    IAxisSet axisSet = chart.getAxisSet();

                    IAxis yAxis = axisSet.getYAxis(0);
                    yAxis.setRange(new Range(0d, 1d));
                    yAxis.adjustRange();

                    IAxis xAxis = axisSet.getXAxis(0);
                    xAxis.setCategorySeries(cachedValues.get(attribute));
                    xAxis.adjustRange();
                    updateCategories();

                    chart.updateLayout();
                    chart.update();
                    chart.setRedraw(true);
                    chart.redraw();
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
    
    /**
     * Makes the chart show category labels or not
     */
    private void updateCategories(){
        if (chart != null){
            IAxisSet axisSet = chart.getAxisSet();
            if (axisSet != null) {
                IAxis xAxis = axisSet.getXAxis(0);
                if (xAxis != null) {
                    String[] series = xAxis.getCategorySeries();
                    if (series != null) {
                        boolean enoughSpace = chart.getPlotArea().getSize().x / series.length >= MIN_CATEGORY_WIDTH;
                        xAxis.enableCategory(enoughSpace);
                        xAxis.getTick().setVisible(enoughSpace);
                    }
                }
            }
        }
    }
}
