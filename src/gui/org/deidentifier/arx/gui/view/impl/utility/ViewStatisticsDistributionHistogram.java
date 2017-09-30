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
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
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
 * This view displays a frequency distribution.
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsDistributionHistogram extends ViewStatistics<AnalysisContextDistribution> {

    /** Minimal width of a category label. */
    private static final int MIN_CATEGORY_WIDTH = 10;

    /** The chart. */
    private Chart            chart;
    
    /** Internal stuff. */
    private Composite        root;
    
    /** Internal stuff. */
    private AnalysisManager  manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsDistributionHistogram(final Composite parent,
                                               final Controller controller,
                                               final ModelPart target,
                                               final ModelPart reset) {
        
        super(parent, controller, target, reset, true);
        this.manager = new AnalysisManager(parent.getDisplay());
    }

    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.HISTOGRAM;
    }

    /**
     * Resets the chart
     */
    private void resetChart() {
        
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

        // Update font
        FontData[] fd = chart.getFont().getFontData();
        fd[0].setHeight(8);
        final Font font = new Font(chart.getDisplay(), fd[0]);
        chart.setFont(font);
        chart.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                if (font != null && !font.isDisposed()) {
                    font.dispose();
                }
            } 
        });
        
        // Update title
        ITitle graphTitle = chart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(chart.getFont());
        
        // Set colors
        chart.setBackground(root.getBackground());
        chart.setForeground(root.getForeground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
            int r = chart.getBackground().getRed()-13;
            int g = chart.getBackground().getGreen()-13;
            int b = chart.getBackground().getBlue()-13;
            r = r>0 ? r : 0;
            r = g>0 ? g : 0;
            r = b>0 ? b : 0;
            final Color background = new Color(chart.getDisplay(), r, g, b);
            chart.setBackground(background);
            chart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    if (background != null && !background.isDisposed()) {
                        background.dispose();
                    }
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
        yAxisTitle.setText(Resources.getMessage("ViewRisksClassDistributionPlot.0")); //$NON-NLS-1$
        chart.setEnabled(false);
        updateCategories();
    }

    /**
     * Makes the chart show category labels or not.
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

        // Tool tip
        root.addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (chart != null) {
                    IAxisSet axisSet = chart.getAxisSet();
                    if (axisSet != null) {
                        IAxis xAxis = axisSet.getXAxis(0);
                        if (xAxis != null) {
                            Point cursor = chart.getPlotArea().toControl(Display.getCurrent().getCursorLocation());
                            if (cursor.x >= 0 && cursor.x < chart.getPlotArea().getSize().x && 
                                cursor.y >= 0 && cursor.y < chart.getPlotArea().getSize().y) {
                                String[] series = xAxis.getCategorySeries();
                                ISeries[] data = chart.getSeriesSet().getSeries();
                                if (data != null && data.length>0 && series != null) {
                                    int x = (int) Math.round(xAxis.getDataCoordinate(cursor.x));
                                    if (x >= 0 && x < series.length) {
                                        root.setToolTipText("("+series[x]+", "+SWTUtil.getPrettyString(data[0].getYSeries()[x])+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    root.setToolTipText(null);
                }
            }
        });

        return this.root;
    }

    @Override
    protected AnalysisContextDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextDistribution(context);
    }

    @Override
    protected void doReset() {
        root.setRedraw(false);
        if (this.manager != null) {
            this.manager.stop();
        }
        resetChart();
        root.setRedraw(true);
        setStatusEmpty();
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

                // Update chart
                chart.setRedraw(false);

                ISeriesSet seriesSet = chart.getSeriesSet();
                IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                        Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
                series.getLabel().setVisible(false);
                series.getLabel().setFont(chart.getFont());
                series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                for (int i = 0; i < this.distribution.frequency.length; i++) {
                    this.distribution.frequency[i] *= 100d;
                }
                series.setYSeries(this.distribution.frequency);
                chart.getLegend().setVisible(false);

                IAxisSet axisSet = chart.getAxisSet();

                IAxis yAxis = axisSet.getYAxis(0);
                yAxis.setRange(new Range(0d, 100d));
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
