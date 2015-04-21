/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.text.DecimalFormat;
import java.util.Arrays;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
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
public class ViewRisksPlotUniquenessEstimates extends ViewRisks<AnalysisContextRisk> {

    /** Minimal width of a category label. */
    private static final int           MIN_CATEGORY_WIDTH = 10;

    /** View */
    private static final DecimalFormat FORMAT             = new DecimalFormat("##0.0###################"); //$NON-NLS-1$

    /** Labels for the plot. */
    private static final double[]      POINTS             = getPoints();

    /** Labels for the plot. */
    private static final String[]      LABELS             = getLabels(POINTS);

    /**
     * Creates a set of labels
     * @param points
     * @return
     */
    private static String[] getLabels(double[] points) {
        String[] result = new String[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = FORMAT.format(points[i]);
        }
        return result;
    }
    
    /**
     * Creates an array of points
     * @return
     */
    private static double[] getPoints() {
        return new double[]{0.0000001d, 0.000001d, 0.00001d, 
                            0.0001d, 0.001d, 0.01d, 0.1d, 
                            0.2d, 0.3d, 0.4d, 0.5d, 0.6d, 
                            0.7d, 0.8d, 0.9d};
    }

    /** View */
    private Chart           chart;

    /** View */
    private Composite       root;

    /** View */
    private boolean         showAllModels;

    /** Internal stuff. */
    private AnalysisManager manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksPlotUniquenessEstimates(final Composite parent,
                                            final Controller controller,
                                            final ModelPart target,
                                            final ModelPart reset,
                                            final boolean showAllModels) {

        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        this.showAllModels = showAllModels;
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE || event.part == ModelPart.POPULATION_MODEL) {
            triggerUpdate();
        }
    }
    
    /**
     * Creates a series
     * @param seriesSet
     * @param data
     * @param label
     * @param symbol
     * @param color
     */
    private void createSeries(ISeriesSet seriesSet, double[] data, String label, PlotSymbolType symbol, Color color) {
        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, label); //$NON-NLS-1$
        series.setAntialias(SWT.ON);
        series.getLabel().setVisible(false);
        series.getLabel().setFont(chart.getFont());
        series.setYSeries(data);
        series.setSymbolType(symbol);
        series.setSymbolColor(color);
        series.setLineColor(color);
        series.setXAxisId(0);
        series.setYAxisId(0);
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
        
        // TODO: Seems to not work on GTK although it did before
        chart.getPlotArea().addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                IAxisSet axisSet = chart.getAxisSet();
                StringBuilder builder = new StringBuilder();
                if (axisSet != null) {
                    IAxis xAxis = axisSet.getXAxis(0);
                    if (xAxis != null) {
                        String[] series = xAxis.getCategorySeries();
                        ISeries[] data = chart.getSeriesSet().getSeries();
                        int x = (int) Math.round(xAxis.getDataCoordinate(event.x));
                        if (x >= 0 && x < series.length) {
                            for (int i = 0; i < data.length; i++) {
                                ISeries yseries = data[i];
                                builder.append(yseries.getId());
                                builder.append("("); //$NON-NLS-1$
                                builder.append(series[x]);
                                builder.append(", "); //$NON-NLS-1$
                                builder.append(yseries.getYSeries()[x]);
                                builder.append(")"); //$NON-NLS-1$
                                if (i < data.length - 1) {
                                    builder.append(", "); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
                if (builder.length() != 0) {
                    chart.getPlotArea().setToolTipText(builder.toString());
                } else {
                    chart.getPlotArea().setToolTipText(null);
                }
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
        if (System.getProperty("os.name").toLowerCase().contains("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
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
        yAxisTitle.setText(Resources.getMessage("ViewRisksPlotUniquenessEstimates.0")); //$NON-NLS-1$
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


        // The statistics builder
        final RiskEstimateBuilderInterruptible baseBuilder = getBuilder(context);
        
        // Enable/disable
        if (!this.isEnabled() || baseBuilder == null) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }
        
        // Create an analysis
        Analysis analysis = new Analysis() {
            
            private RiskEstimateBuilderInterruptible builder = baseBuilder;
            private boolean  stopped = false;
            private double[] dataPitman;
            private double[] dataZayatz;
            private double[] dataSNB;
            private double[] dataDankar;
            private int idx;

            @Override
            public int getProgress() {
                return (int)Math.round(idx * 100d + (double)baseBuilder.getProgress()) / POINTS.length; 
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
                resetChart();

                ISeriesSet seriesSet = chart.getSeriesSet();
                if (showAllModels) {
                    createSeries(seriesSet, dataPitman, "Pitman", PlotSymbolType.CIRCLE, GUIHelper.COLOR_BLACK); //$NON-NLS-1$
                    createSeries(seriesSet, dataZayatz, "Zayatz", PlotSymbolType.CROSS, GUIHelper.COLOR_BLUE); //$NON-NLS-1$
                    createSeries(seriesSet, dataSNB, "SNB", PlotSymbolType.DIAMOND, GUIHelper.COLOR_RED); //$NON-NLS-1$
                    createSeries(seriesSet, dataDankar, "Dankar", PlotSymbolType.SQUARE, GUIHelper.COLOR_DARK_GRAY); //$NON-NLS-1$
                    chart.getLegend().setVisible(true);
                } else {
                    createSeries(seriesSet, dataDankar, "Dankar", PlotSymbolType.SQUARE, GUIHelper.COLOR_BLACK); //$NON-NLS-1$
                    chart.getLegend().setVisible(false);
                }
                
                IAxisSet axisSet = chart.getAxisSet();

                IAxis yAxis = axisSet.getYAxis(0);
                yAxis.setRange(new Range(0d, 1d));

                IAxis xAxis = axisSet.getXAxis(0);
                xAxis.setRange(new Range(0d, LABELS.length));
                xAxis.setCategorySeries(LABELS);

                chart.updateLayout();
                chart.update();
                updateCategories();
                chart.layout();
                chart.setRedraw(true);
                chart.redraw();
                
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
                dataDankar = new double[POINTS.length];
                if (showAllModels) {
                    dataPitman = new double[POINTS.length];
                    dataZayatz = new double[POINTS.length];
                    dataSNB = new double[POINTS.length];
                }
                for (idx = 0; idx < POINTS.length; idx++) {
                    if (stopped) {
                        throw new InterruptedException();
                    }
                    
                    builder = getBuilder(context, ARXPopulationModel.create(POINTS[idx]), builder.getEquivalenceClassModel());
                    
                    if (idx == 0 && builder.getSampleBasedUniquenessRisk().getFractionOfUniqueTuples() == 0.0d) {
                        Arrays.fill(dataDankar, 0.0d);
                        if (showAllModels) {
                            Arrays.fill(dataPitman, 0.0d);
                            Arrays.fill(dataZayatz, 0.0d);
                            Arrays.fill(dataSNB, 0.0d);
                        }
                        break;
                    }
                    RiskModelPopulationUniqueness populationBasedModel = builder.getPopulationBasedUniquenessRisk();
                    dataDankar[idx] = populationBasedModel.getFractionOfUniqueTuplesDankar();
                    if (showAllModels) {
                        dataPitman[idx] = populationBasedModel.getFractionOfUniqueTuplesPitman();
                        dataZayatz[idx] = populationBasedModel.getFractionOfUniqueTuplesZayatz();
                        dataSNB[idx] = populationBasedModel.getFractionOfUniqueTuplesSNB();
                    }
                }

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                if (baseBuilder != null) baseBuilder.interrupt();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
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
    protected ViewRiskType getViewType() {
        if (this.showAllModels) {
            return ViewRiskType.UNIQUES_ALL;
        } else {
            return ViewRiskType.UNIQUES_DANKAR;
        }
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
