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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public class ViewStatisticsDistributionHistogram extends ViewStatistics<AnalysisContextVisualizationDistribution> {

    /** Minimal width of a category label */
    private static final int MIN_CATEGORY_WIDTH = 10;

    /** The chart */
    private Chart            chart;
    /** Internal stuff */
    private Composite        root;
    /** Internal stuff */
    private AnalysisManager  manager;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsDistributionHistogram(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
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

    @Override
    protected Control createControl(Composite parent) {
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        return this.root;
    }

    @Override
    protected AnalysisContextVisualizationDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationDistribution(context);
    }

    @Override
    protected void doReset() {
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
        
        // Tool tip
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
            final Color c2 = new Color(chart.getDisplay(), r, g, b);
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

                // Update chart
                chart.setRedraw(false);

                ISeriesSet seriesSet = chart.getSeriesSet();
                IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                        Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
                series.getLabel().setVisible(false);
                series.getLabel().setFont(chart.getFont());
                series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                series.setYSeries(this.distribution.frequency);
                chart.getLegend().setVisible(false);

                IAxisSet axisSet = chart.getAxisSet();

                IAxis yAxis = axisSet.getYAxis(0);
                yAxis.setRange(new Range(0d, 1d));
                yAxis.adjustRange();

                IAxis xAxis = axisSet.getXAxis(0);
                xAxis.setCategorySeries(this.distribution.values);
                xAxis.adjustRange();
                updateCategories();

                chart.updateLayout();
                chart.update();
                chart.setRedraw(true);
                chart.redraw();
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
