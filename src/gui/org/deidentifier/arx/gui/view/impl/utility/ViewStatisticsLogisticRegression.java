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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsClassification;
import org.deidentifier.arx.aggregates.StatisticsClassification.PrecisionRecallMatrix;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays a statistics about the performance of logistic regression classifiers
 *
 * @author Fabian Prasser
 */
public abstract class ViewStatisticsLogisticRegression extends ViewStatistics<AnalysisContextClassification> {

    /** Minimal width of a category label. */
    private static final int MIN_CATEGORY_WIDTH = 10;

    /** Internal stuff. */
    private AnalysisManager  manager;

    /** View */
    private DynamicTable     table;
    /** View */
    private Composite        root;
    /** View */
    private SashForm         sash;
    /** View */
    private Chart            chart;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param part
     */
    public ViewStatisticsLogisticRegression(final Composite parent,
                                            final Controller controller,
                                            final ModelPart part) {

        super(parent, controller, part, null, false);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.SELECTED_FEATURES_OR_CLASSES, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
    }
    
    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.LOGISTIC_REGRESSION;
    }

    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.SELECTED_FEATURES_OR_CLASSES ||
            event.part == ModelPart.DATA_TYPE) {
            if (getModel() != null && (getModel().getSelectedFeatures().isEmpty() || getModel().getSelectedClasses().isEmpty())) {
                doReset();
                return;
            } else {
                triggerUpdate();
            }
        }
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            int index = 0;
            for (TableItem item : table.getItems()) {
                if (item.getText(0).equals(super.getModel().getSelectedAttribute())) {
                    table.select(index);
                    if (item.getData() != null && item.getData() instanceof PrecisionRecallMatrix) {
                        setChartSeries((PrecisionRecallMatrix) item.getData());
                    }
                    return;
                }
                index++;
            }
        }
    }

    /**
     * Resets the chart
     */
    private void resetChart() {

        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(this.sash, SWT.NONE);
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

        // Initialize axes
        ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.17")); //$NON-NLS-1$
        xAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.14")); //$NON-NLS-1$
        chart.setEnabled(false);
        updateCategories();
    }
    
    /**
     * Updates the chart with a new matrix
     * @param matrix
     */
    private void setChartSeries(PrecisionRecallMatrix matrix) {
        
        // Init data
        String[] xAxisLabels = new String[matrix.getConfidenceThresholds().length];
        double[] ySeriesPrecision = new double[matrix.getConfidenceThresholds().length];
        double[] ySeriesRecall = new double[matrix.getConfidenceThresholds().length];
        double[] ySeriesFscore = new double[matrix.getConfidenceThresholds().length];
        for (int i = 0; i < xAxisLabels.length; i++) {
            xAxisLabels[i] = SWTUtil.getPrettyString(matrix.getConfidenceThresholds()[i] * 100d);
            ySeriesPrecision[i] = matrix.getPrecision()[i] * 100d;
            ySeriesRecall[i] = matrix.getRecall()[i] * 100d;
            ySeriesFscore[i] = matrix.getFscore()[i] * 100d;
        }
        
        chart.setRedraw(false);

        ISeriesSet seriesSet = chart.getSeriesSet();

        ILineSeries series1 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.15")); //$NON-NLS-1$
        series1.getLabel().setVisible(false);
        series1.getLabel().setFont(chart.getFont());
        series1.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        series1.setYSeries(ySeriesPrecision);
        series1.setAntialias(SWT.ON);
        series1.setSymbolType(PlotSymbolType.NONE);
        series1.enableArea(true);
        
        ILineSeries series2 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.16")); //$NON-NLS-1$
        series2.getLabel().setVisible(false);
        series2.getLabel().setFont(chart.getFont());
        series2.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        series2.setYSeries(ySeriesRecall);
        series2.setSymbolType(PlotSymbolType.NONE);
        series2.enableArea(true);
        
        ILineSeries series3 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.18")); //$NON-NLS-1$
        series3.getLabel().setVisible(false);
        series3.getLabel().setFont(chart.getFont());
        series3.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
        series3.setYSeries(ySeriesFscore);
        series3.setSymbolType(PlotSymbolType.NONE);
        series3.enableArea(true);
        
        seriesSet.bringToFront(Resources.getMessage("ViewStatisticsClassificationInput.16")); //$NON-NLS-1$
        
        chart.getLegend().setVisible(true);
        chart.getLegend().setPosition(SWT.TOP);

        IAxisSet axisSet = chart.getAxisSet();

        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 100d));

        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setCategorySeries(xAxisLabels);
        xAxis.adjustRange();
        updateCategories();

        chart.setRedraw(true);
        chart.updateLayout();
        chart.update();
        chart.redraw();
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

        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        // Shash
        this.sash = new SashForm(this.root, SWT.VERTICAL);

        // Table
        this.table = SWTUtil.createTableDynamic(this.sash, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setMenu(new ClipboardHandlerTable(table).getMenu());

        // Columns
        String[] columns = getColumnHeaders();
        String width = String.valueOf(Math.round(100d / ((double) columns.length + 2) * 100d) / 100d) + "%"; //$NON-NLS-1$
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.0")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$ 
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.2")); //$NON-NLS-1$
        for (String column : columns) {
            c = new DynamicTableColumn(table, SWT.LEFT);
            SWTUtil.createColumnWithBarCharts(table, c);
            c.setWidth(width, "100px"); //$NON-NLS-1$ 
            c.setText(column);
        }
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);

        // Chart and sash
        resetChart();
        this.sash.setWeights(new int[] {2, 2});
        
        // Tool tip
        final StringBuilder builder = new StringBuilder();
        this.sash.addListener(SWT.MouseMove, new Listener() {
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
                                        builder.append(Resources.getMessage("ViewStatisticsClassificationInput.14")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
                                        builder.append(series[x]);
                                        builder.append("%, ").append(Resources.getMessage("ViewStatisticsClassificationInput.15")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(data[0].getYSeries()[x]));
                                        builder.append("%, ").append(Resources.getMessage("ViewStatisticsClassificationInput.16")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(data[1].getYSeries()[x]));
                                        builder.append("%)"); //$NON-NLS-1$
                                        sash.setToolTipText(builder.toString());
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    sash.setToolTipText(null);
                }
            }
        });

        // Update matrix
        table.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                Rectangle clientArea = table.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = table.getTopIndex();
                while (index < table.getItemCount()) {
                    boolean visible = false;
                    TableItem item = table.getItem(index);
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            if (item.getData() != null &&
                                item.getData() instanceof PrecisionRecallMatrix) {
                                setChartSeries((PrecisionRecallMatrix) item.getData());
                            }
                            getModel().setSelectedAttribute(item.getText(0));
                            getController().update(new ModelEvent(ViewStatisticsLogisticRegression.this,
                                                                  ModelPart.SELECTED_ATTRIBUTE,
                                                                  item.getText(0)));
                            return;
                        }
                        if (!visible && rect.intersects(clientArea)) {
                            visible = true;
                        }
                    }
                    if (!visible) return;
                    index++;
                }
            }
        });

        return this.root;
    }
    
    @Override
    protected AnalysisContextClassification createViewConfig(AnalysisContext context) {
        return new AnalysisContextClassification(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
        resetChart();
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextClassification context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final String[] features = context.model.getSelectedFeatures().toArray(new String[0]);
        final String[] classes = context.model.getSelectedClasses().toArray(new String[0]);
        final ARXLogisticRegressionConfiguration config = context.model.getClassificationModel().getARXLogisticRegressionConfiguration();
        
        // Break, if nothing do
        if (context.model.getSelectedFeatures().isEmpty() ||
            context.model.getSelectedClasses().isEmpty()) {
            doReset();
            return;
        }
        
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean                     stopped    = false;
            private List<List<Double>>          values     = new ArrayList<>();
            private List<Integer>               numClasses = new ArrayList<>();
            private List<PrecisionRecallMatrix> matrixes   = new ArrayList<>();
            private int                         progress   = 0;

            @Override
            public int getProgress() {
                
                double result = 0d;
                double perBatch = 100d / (double)classes.length;
                result += (double)progress * perBatch;
                result += (double)builder.getProgress() / 100d * perBatch;
                result = result <= 100d ? result : 100d;
                return (int)result;
            }
            
            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                // Check
                if (stopped || !isEnabled() || getModel().getSelectedFeatures().isEmpty() || getModel().getSelectedClasses().isEmpty()) {
                    setStatusEmpty();
                    return;
                }

                // Update chart
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }

                // Create entries
                for (int i = 0; i < classes.length; i++) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(0, classes[i]);
                    item.setText(1, String.valueOf(numClasses.get(i)));
                    for (int j = 0; j<values.get(i).size(); j++) {
                        item.setData(String.valueOf(2+j), values.get(i).get(j));    
                    }
                    item.setData(matrixes.get(i));
                }

                table.setFocus();
                table.select(0);
                setChartSeries(matrixes.get(0));

                // Status
                root.layout();
                sash.setWeights(new int[] {2, 2});
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled() || getModel().getSelectedFeatures().isEmpty() || getModel().getSelectedClasses().isEmpty()) {
                    setStatusEmpty();
                } else {
                    setStatusWorking();
                }
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Do work
                for (String clazz : classes) {
                    
                    // Compute
                    StatisticsClassification result = builder.getClassificationPerformance(features,
                                                                                           clazz,
                                                                                           config);
                    progress++;
                    if (stopped) {
                        break;
                    }
                    numClasses.add(result.getNumClasses());
                    values.add(getColumnValues(result));
                    matrixes.add(result.getPrecisionRecall());
                }

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
     * Returns all column headers
     * @return
     */
    protected abstract String[] getColumnHeaders();

    /**
     * Returns all values for one row
     * @param result
     * @return
     */
    protected abstract List<Double> getColumnValues(StatisticsClassification result);

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

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
