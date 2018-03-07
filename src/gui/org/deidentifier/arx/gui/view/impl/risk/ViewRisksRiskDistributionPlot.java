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
package org.deidentifier.arx.gui.view.impl.risk;

import java.util.Arrays;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.reliability.ParameterTranslation;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelSampleRiskDistribution;
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
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.Range;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksRiskDistributionPlot extends ViewRisks<AnalysisContextRisk> {

    /** Minimal width of a category label. */
    private static final int MIN_CATEGORY_WIDTH = 18;

    /** View */
    private Chart            chart;

    /** View */
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
    public ViewRisksRiskDistributionPlot(final Composite parent,
                                          final Controller controller,
                                          final ModelPart target,
                                          final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            triggerUpdate();
        }
    }

    /**
     * Insert item to back
     * @param array
     * @param value
     * @return
     */
    private double[] insertToBack(double[] array, double value) {
        double[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = value;
        return result;
    }

    /**
     *Insert item to back
     * @param array
     * @param value
     * @return
     */
    private String[] insertToBack(String[] array, String value) {
        String[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = value;
        return result;
    }

    /**
     * Resets the chart
     */
    private void resetChart() {

        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(root, SWT.DOUBLE_BUFFERED);
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
            int r = chart.getBackground().getRed() - 13;
            int g = chart.getBackground().getGreen() - 13;
            int b = chart.getBackground().getBlue() - 13;
            r = r > 0 ? r : 0;
            r = g > 0 ? g : 0;
            r = b > 0 ? b : 0;
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

        // Initialize axes
        ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(Resources.getMessage("ViewRisksClassDistributionPlot.0")); //$NON-NLS-1$
        xAxisTitle.setText(Resources.getMessage("ViewRisksClassDistributionPlot.1")); //$NON-NLS-1$
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
                        root.setRedraw(false);
                        boolean enoughSpace = chart.getPlotArea().getSize().x / series.length >= MIN_CATEGORY_WIDTH;
                        xAxis.enableCategory(true);
                        xAxis.getTick().setVisible(true);
                        xAxis.getTick().setTickLabelAngle(enoughSpace ? 45 : 90);
                        root.setRedraw(true);
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
        final StringBuilder builder = new StringBuilder();
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
                                    if (x >= 0 && x < series.length && !series[x].equals("")) {
                                        builder.setLength(0);
                                        builder.append("("); //$NON-NLS-1$
                                        builder.append(Resources.getMessage("ViewRisksRiskDistributionPlot.1")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
                                        builder.append(x != series.length - 1 ? series[x] : series[series.length - 2]);
                                        builder.append("%, ").append(Resources.getMessage("ViewRisksRiskDistributionPlot.4")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(data[1].getYSeries()[x]));
                                        builder.append("%, ").append(Resources.getMessage("ViewRisksRiskDistributionPlot.7")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(data[0].getYSeries()[x]));
                                        builder.append("%)"); //$NON-NLS-1$
                                        root.setToolTipText(builder.toString());
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
    protected AnalysisContextRisk createViewConfig(AnalysisContext context) {
        return new AnalysisContextRisk(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        resetChart();
        setStatusEmpty();
    }
    @Override
    protected void doUpdate(final AnalysisContextRisk context) {

        // Enable/disable
        final RiskEstimateBuilderInterruptible builder = getBuilder(context);
        if (!this.isEnabled() || builder == null) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }
        
        // Create an analysis
        Analysis analysis = new Analysis() {
            
            private boolean  stopped = false;
            private double[] frequencies;
            private double[] cumulative;
            private double[] threshold;
            private String[] labels;

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

                if (stopped || !isEnabled()) {
                    return;
                }

                // Update chart
                chart.setRedraw(false);

                ISeriesSet seriesSet = chart.getSeriesSet();

                ILineSeries series1 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewRisksClassDistributionPlot.3")); //$NON-NLS-1$
                series1.getLabel().setVisible(false);
                series1.getLabel().setFont(chart.getFont());
                series1.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                series1.setYSeries(cumulative);
                series1.setAntialias(SWT.ON);
                series1.setSymbolType(PlotSymbolType.NONE);
                series1.enableStep(true);
                series1.enableArea(true);
                
                ILineSeries series2 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewRisksClassDistributionPlot.2")); //$NON-NLS-1$
                series2.getLabel().setVisible(false);
                series2.getLabel().setFont(chart.getFont());
                series2.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
                series2.setYSeries(frequencies);
                series2.setSymbolType(PlotSymbolType.NONE);
                series2.enableStep(true);
                series2.enableArea(true);

                ILineSeries series3 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewRisksClassDistributionPlot.4")); //$NON-NLS-1$
                series3.getLabel().setVisible(false);
                series3.getLabel().setFont(chart.getFont());
                series3.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                series3.setYSeries(threshold);
                series3.setAntialias(SWT.ON);
                series3.setSymbolColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                series3.setSymbolType(PlotSymbolType.NONE);
                series3.enableStep(true);
                series3.enableArea(true);
                
                seriesSet.bringToFront(Resources.getMessage("ViewRisksClassDistributionPlot.2")); //$NON-NLS-1$
                seriesSet.bringToFront(Resources.getMessage("ViewRisksClassDistributionPlot.4")); //$NON-NLS-1$
                
                chart.getLegend().setVisible(true);
                chart.getLegend().setPosition(SWT.TOP);

                IAxisSet axisSet = chart.getAxisSet();

                IAxis yAxis = axisSet.getYAxis(0);
                yAxis.setRange(new Range(0d, 100d));

                IAxis xAxis = axisSet.getXAxis(0);
                xAxis.setCategorySeries(labels);
                xAxis.getTick().setTickLabelAngle(45);
                xAxis.getTick().setTickMarkStepHint(300);
                xAxis.adjustRange();
                updateCategories();

                chart.updateLayout();
                chart.update();
                chart.setRedraw(true);
                
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled() || !isValid()) {
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
                RiskModelSampleRiskDistribution model = builder.getSampleBasedRiskDistribution();

                // Create array
                frequencies = model.getFractionOfRecordsForRiskThresholds();
                cumulative = model.getFractionOfRecordsForCumulativeRiskThresholds();
                threshold = new double[frequencies.length];
                double enforced = model.getRiskThreshold();
                labels = new String[frequencies.length];
                for (int i = 0; i < frequencies.length; i++) {
                    frequencies[i] *= 100d;
                    cumulative[i] *= 100d;
                    if (enforced != 1d && ParameterTranslation.getEffectiveRiskThreshold(enforced) < ParameterTranslation.getEffectiveRiskThreshold(model.getAvailableUpperRiskThresholds()[i])) {
                        threshold[i] = 100d;
                    }
                    labels[i] = "]" + String.valueOf(SWTUtil.getPrettyString(model.getAvailableLowerRiskThresholds()[i] * 100d)) + //$NON-NLS-1$
                                ", " + String.valueOf(SWTUtil.getPrettyString(model.getAvailableUpperRiskThresholds()[i] * 100d)) + "]"; //$NON-NLS-1$ $NON-NLS-2$
                }
                
                // TODO: Ugly hack
                frequencies = insertToBack(frequencies, frequencies[frequencies.length-1]);
                cumulative = insertToBack(cumulative, cumulative[cumulative.length-1]);
                threshold = insertToBack(threshold, threshold[threshold.length-1]);
                labels = insertToBack(labels, " "); //$NON-NLS-1$

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                if (builder != null) builder.interrupt();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
    }

    @Override
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return null;
    }

    @Override
    protected ViewRiskType getViewType() {
        return ViewRiskType.CLASSES_PLOT;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
