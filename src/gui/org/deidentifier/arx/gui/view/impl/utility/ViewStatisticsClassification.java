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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsClassification;
import org.deidentifier.arx.aggregates.StatisticsClassification.PrecisionRecallMatrix;
import org.deidentifier.arx.aggregates.StatisticsClassification.ROCCurve;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
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
public abstract class ViewStatisticsClassification extends ViewStatistics<AnalysisContextClassification> {

    /** Minimal width of a category label. */
    private static final int                   MIN_CATEGORY_WIDTH = 10;

    /** Internal stuff. */
    private AnalysisManager                    manager;

    /** View */
    private ViewStatisticsClassification       other;
    /** View */
    private ComponentTitledFolder              folder;
    /** View */
    private DynamicTable                       precisionRecallTable;
    /** View */
    private Composite                          precisionRecallRoot;
    /** View */
    private SashForm                           precisionRecallSash;
    /** View */
    private Chart                              precisionRecallChart;
    /** View */
    private DynamicTable                       rocTable;
    /** View */
    private Composite                          rocRoot;
    /** View */
    private SashForm                           rocSash;
    /** View */
    private Chart                              rocChart;
    /** Widget */
    private Combo                              rocCombo;

    /** Model */
    private boolean                            isOutput;
    /** Model */
    private Map<String, Map<String, ROCCurve>> rocCurves;
    /** Model */
    private Map<String, Map<String, ROCCurve>> originalRocCurves;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param part
     */
    public ViewStatisticsClassification(final Composite parent,
                                        final Controller controller,
                                        final ModelPart part) {

        super(parent, controller, part, null, false);
        this.manager = new AnalysisManager(parent.getDisplay());
        this.isOutput = part != ModelPart.INPUT;
        this.rocCurves = new HashMap<>();
        this.originalRocCurves = new HashMap<>();
        
        controller.addListener(ModelPart.CLASSIFICATION_CONFIGURATION, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.SELECTED_CLASS_VALUE, this);
    }
    
    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.CLASSIFICATION_PRECISION_RECALL;
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.CLASSIFICATION_CONFIGURATION ||
            event.part == ModelPart.DATA_TYPE) {
            if (getModel() != null && (getModel().getSelectedFeatures().isEmpty() || getModel().getSelectedClasses().isEmpty())) {
                doReset();
                return;
            } else {
                triggerUpdate();
            }
        }
        
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            precisionRecallUpdateSelection();
            rocUpdateSelection();
        }
        
        if (event.part == ModelPart.SELECTED_CLASS_VALUE) {
            rocUpdateSelection();
        }
    }

    /**
     * Returns the value of the given point
     * 
     * @param data
     * @param value
     * @return
     */
    private int getIndex(double[] data, double value){
        int index = Arrays.binarySearch(data, value);
        if (index < 0) {
            index = -index + 1;
        }
        if (index > data.length - 1) {
            index = data.length - 1;
        }
        return index;
    }
    
    /**
     * Returns the index of the given value, 0 if it is not found
     * @param values
     * @param value
     * @return
     */
    private int getIndexOf(String[] values, String value) {
        int index = 0;
        for (String element : values) {
            if (element.equals(value)) {
                return index; 
            }
            index++;
        }
        return 0;
    }
    
    /**
     * Builds the precision/recall view
     * @param parent
     * @return
     */
    private Control precisionRecallCreateControl(Composite parent) {

        // Root
        this.precisionRecallRoot = parent;
        this.precisionRecallRoot.setLayout(new FillLayout());
        
        // Shash
        this.precisionRecallSash = new SashForm(this.precisionRecallRoot, SWT.VERTICAL);

        // Table
        this.precisionRecallTable = SWTUtil.createTableDynamic(this.precisionRecallSash, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.precisionRecallTable.setHeaderVisible(true);
        this.precisionRecallTable.setLinesVisible(true);
        this.precisionRecallTable.setMenu(new ClipboardHandlerTable(precisionRecallTable).getMenu());

        // Columns
        String[] columns = getColumnHeaders();
        String width = String.valueOf(Math.round(100d / ((double) columns.length + 2) * 100d) / 100d) + "%"; //$NON-NLS-1$
        DynamicTableColumn c = new DynamicTableColumn(precisionRecallTable, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.0")); //$NON-NLS-1$
        c = new DynamicTableColumn(precisionRecallTable, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$ 
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.2")); //$NON-NLS-1$
        for (String column : columns) {
            c = new DynamicTableColumn(precisionRecallTable, SWT.LEFT);
            SWTUtil.createColumnWithBarCharts(precisionRecallTable, c);
            c.setWidth(width, "100px"); //$NON-NLS-1$ 
            c.setText(column);
        }
        for (final TableColumn col : precisionRecallTable.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(precisionRecallTable);

        // Chart and sash
        precisionRecallResetChart();
        this.precisionRecallSash.setWeights(new int[] {2, 2});
        
        // Tool tip
        final StringBuilder builder = new StringBuilder();
        this.precisionRecallSash.addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (precisionRecallChart != null) {
                    IAxisSet axisSet = precisionRecallChart.getAxisSet();
                    if (axisSet != null) {
                        IAxis xAxis = axisSet.getXAxis(0);
                        if (xAxis != null) {
                            Point cursor = precisionRecallChart.getPlotArea().toControl(Display.getCurrent().getCursorLocation());
                            if (cursor.x >= 0 && cursor.x < precisionRecallChart.getPlotArea().getSize().x && 
                                cursor.y >= 0 && cursor.y < precisionRecallChart.getPlotArea().getSize().y) {
                                String[] series = xAxis.getCategorySeries();
                                ISeries[] data = precisionRecallChart.getSeriesSet().getSeries();
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
                                        precisionRecallSash.setToolTipText(builder.toString());
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    precisionRecallSash.setToolTipText(null);
                }
            }
        });

        // Update matrix
        precisionRecallTable.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                Rectangle clientArea = precisionRecallTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = precisionRecallTable.getTopIndex();
                while (index < precisionRecallTable.getItemCount()) {
                    boolean visible = false;
                    TableItem item = precisionRecallTable.getItem(index);
                    for (int i = 0; i < precisionRecallTable.getColumnCount(); i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            if (item.getData() != null &&
                                item.getData() instanceof PrecisionRecallMatrix) {
                                precisionRecallSetChartSeries((PrecisionRecallMatrix) item.getData());
                            }
                            getModel().setSelectedAttribute(item.getText(0));
                            rocUpdateSelection();
                            getController().update(new ModelEvent(ViewStatisticsClassification.this,
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

        return this.precisionRecallRoot;
    }

    /**
     * Resets the chart
     */
    private void precisionRecallResetChart() {

        if (precisionRecallChart != null) {
            precisionRecallChart.dispose();
        }
        precisionRecallChart = new Chart(this.precisionRecallSash, SWT.NONE);
        precisionRecallChart.setOrientation(SWT.HORIZONTAL);
        
        // Show/Hide axis
        precisionRecallChart.addControlListener(new ControlAdapter(){
            @Override
            public void controlResized(ControlEvent arg0) {
                updateCategories(precisionRecallChart);
            }
        });

        // Update font
        FontData[] fd = precisionRecallChart.getFont().getFontData();
        fd[0].setHeight(8);
        final Font font = new Font(precisionRecallChart.getDisplay(), fd[0]);
        precisionRecallChart.setFont(font);
        precisionRecallChart.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                if (font != null && !font.isDisposed()) {
                    font.dispose();
                }
            } 
        });
        
        // Update title
        ITitle graphTitle = precisionRecallChart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(precisionRecallChart.getFont());
        
        // Set colors
        precisionRecallChart.setBackground(precisionRecallRoot.getBackground());
        precisionRecallChart.setForeground(precisionRecallRoot.getForeground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
            int r = precisionRecallChart.getBackground().getRed()-13;
            int g = precisionRecallChart.getBackground().getGreen()-13;
            int b = precisionRecallChart.getBackground().getBlue()-13;
            r = r>0 ? r : 0;
            r = g>0 ? g : 0;
            r = b>0 ? b : 0;
            final Color background = new Color(precisionRecallChart.getDisplay(), r, g, b);
            precisionRecallChart.setBackground(background);
            precisionRecallChart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    if (background != null && !background.isDisposed()) {
                        background.dispose();
                    }
                } 
            });
        }

        // Initialize axes
        IAxisSet axisSet = precisionRecallChart.getAxisSet();
        IAxis yAxis = axisSet.getYAxis(0);
        IAxis xAxis = axisSet.getXAxis(0);
        ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(precisionRecallChart.getFont());
        yAxis.getTitle().setFont(precisionRecallChart.getFont());
        xAxis.getTick().setFont(precisionRecallChart.getFont());
        yAxis.getTick().setFont(precisionRecallChart.getFont());
        xAxis.getTick().setForeground(precisionRecallChart.getForeground());
        yAxis.getTick().setForeground(precisionRecallChart.getForeground());
        xAxis.getTitle().setForeground(precisionRecallChart.getForeground());
        yAxis.getTitle().setForeground(precisionRecallChart.getForeground());

        // Initialize axes
        ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.17")); //$NON-NLS-1$
        xAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.14")); //$NON-NLS-1$
        precisionRecallChart.setEnabled(false);
        updateCategories(precisionRecallChart);
    }

    /**
     * Updates the chart with a new matrix
     * @param matrix
     */
    private void precisionRecallSetChartSeries(PrecisionRecallMatrix matrix) {
        
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
        
        precisionRecallChart.setRedraw(false);

        ISeriesSet seriesSet = precisionRecallChart.getSeriesSet();

        ILineSeries series1 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.15")); //$NON-NLS-1$
        series1.getLabel().setVisible(false);
        series1.getLabel().setFont(precisionRecallChart.getFont());
        series1.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        series1.setYSeries(ySeriesPrecision);
        series1.setAntialias(SWT.ON);
        series1.setSymbolType(PlotSymbolType.NONE);
        series1.enableArea(true);
        
        ILineSeries series2 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.16")); //$NON-NLS-1$
        series2.getLabel().setVisible(false);
        series2.getLabel().setFont(precisionRecallChart.getFont());
        series2.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        series2.setYSeries(ySeriesRecall);
        series2.setSymbolType(PlotSymbolType.NONE);
        series2.enableArea(true);
        
        ILineSeries series3 = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, Resources.getMessage("ViewStatisticsClassificationInput.18")); //$NON-NLS-1$
        series3.getLabel().setVisible(false);
        series3.getLabel().setFont(precisionRecallChart.getFont());
        series3.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
        series3.setYSeries(ySeriesFscore);
        series3.setSymbolType(PlotSymbolType.NONE);
        series3.enableArea(true);
        
        seriesSet.bringToFront(Resources.getMessage("ViewStatisticsClassificationInput.16")); //$NON-NLS-1$
        
        precisionRecallChart.getLegend().setVisible(true);
        precisionRecallChart.getLegend().setPosition(SWT.TOP);

        IAxisSet axisSet = precisionRecallChart.getAxisSet();

        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 100d));

        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setCategorySeries(xAxisLabels);
        xAxis.adjustRange();
        updateCategories(precisionRecallChart);

        precisionRecallChart.setRedraw(true);
        precisionRecallChart.updateLayout();
        precisionRecallChart.update();
        precisionRecallChart.redraw();
    }

    /**
     * Update the selection on precision/recall data
     */
    private void precisionRecallUpdateSelection() {
        
        // Update table
        int index = 0;
        for (TableItem item : precisionRecallTable.getItems()) {
            if (item.getText(0).equals(super.getModel().getSelectedAttribute())) {
                precisionRecallTable.select(index);
                if (item.getData() != null && item.getData() instanceof PrecisionRecallMatrix) {
                    precisionRecallSetChartSeries((PrecisionRecallMatrix) item.getData());
                }
                break;
            }
            index++;
        }
    }

    private Control rocCreateControl(Composite parent) {
        
        // Root
        this.rocRoot = parent;
        this.rocRoot.setLayout(new FillLayout());
        
        // Shash
        this.rocSash = new SashForm(this.rocRoot, SWT.VERTICAL);
        
        final Composite composite = new Composite(this.rocSash, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1));

        // Table
        this.rocTable = SWTUtil.createTableDynamic(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.rocTable.setHeaderVisible(true);
        this.rocTable.setLinesVisible(true);
        this.rocTable.setMenu(new ClipboardHandlerTable(rocTable).getMenu());
        this.rocTable.setLayoutData(SWTUtil.createFillGridData(2));

        // Columns
        String width = "50%"; //$NON-NLS-1$
        if (getTarget() == ModelPart.OUTPUT) {
            width = "33%"; //$NON-NLS-1$
        }
        DynamicTableColumn c = new DynamicTableColumn(rocTable, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.22")); //$NON-NLS-1$
        c = new DynamicTableColumn(rocTable, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(rocTable, c);
        c.setWidth(width, "100px"); //$NON-NLS-1$ 
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.23")); //$NON-NLS-1$
        if (getTarget() == ModelPart.OUTPUT) {
            c = new DynamicTableColumn(rocTable, SWT.LEFT);
            SWTUtil.createColumnWithBarCharts(rocTable, c);
            c.setWidth(width, "100px"); //$NON-NLS-1$ 
            c.setText(Resources.getMessage("ViewStatisticsClassificationInput.24")); //$NON-NLS-1$           
        }
        for (final TableColumn col : rocTable.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(rocTable);
        
        // Combo for selecting a class attributes
        final Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        composite2.setLayout(SWTUtil.createGridLayout(2, false));
        final Label lblClassAtt = new Label(composite2, SWT.PUSH);
        lblClassAtt.setText(Resources.getMessage("ViewStatisticsClassificationInput.21")); //$NON-NLS-1$        
        this.rocCombo = new Combo(composite2, SWT.READ_ONLY);
        this.rocCombo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.rocCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if(rocCombo.getSelectionIndex() >=0){
                    String selectedClass = rocCombo.getItem(rocCombo.getSelectionIndex());
                    rocUpdateTableAndChart(selectedClass);
                    getModel().setSelectedAttribute(selectedClass);
                    precisionRecallUpdateSelection();
                    getController().update(new ModelEvent(ViewStatisticsClassification.this,
                                                          ModelPart.SELECTED_ATTRIBUTE,
                                                          selectedClass));
                }
            }
        });
        
        // Chart and sash
        rocResetChart();
        this.rocSash.setWeights(new int[] {2, 2});
        
        // Tool tip
        final StringBuilder builder = new StringBuilder();
        this.rocSash.addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (rocChart != null) {
                    IAxisSet axisSet = rocChart.getAxisSet();
                    if (axisSet != null) {
                        IAxis xAxis = axisSet.getXAxis(0);
                        IAxis yAxis = axisSet.getYAxis(0);
                        if (xAxis != null && yAxis != null) {
                            Point cursor = rocChart.getPlotArea().toControl(Display.getCurrent().getCursorLocation());
                            if (cursor.x >= 0 && cursor.x < rocChart.getPlotArea().getSize().x && cursor.y >= 0 && cursor.y < rocChart.getPlotArea().getSize().y) {
                                ISeries[] data = rocChart.getSeriesSet().getSeries();
                                if (data != null && data.length > 0) {
                                    double[] x;
                                    double[] y;
                                    int index;
                                    if (getTarget() == ModelPart.OUTPUT) {
                                        x = data[1].getXSeries();
                                        y = data[1].getYSeries();
                                    } else {
                                        x = data[0].getXSeries();
                                        y = data[0].getYSeries();
                                    }
                                    index = getIndex(x, xAxis.getDataCoordinate(cursor.x));
                                    if (index >= 0) {
                                        builder.setLength(0);
                                        builder.append("("); //$NON-NLS-1$
                                        builder.append(Resources.getMessage("ViewStatisticsClassificationInput.20")).append(": "); //$NON-NLS-1$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(x[index])).append(", "); //$NON-NLS-1$
                                        builder.append(Resources.getMessage("ViewStatisticsClassificationInput.19")).append(": "); //$NON-NLS-1$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(y[index]));
                                        builder.append(")"); //$NON-NLS-1$
                                        rocSash.setToolTipText(builder.toString());
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    rocSash.setToolTipText(null);
                }
            }
        });

        // Update curve
        rocTable.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                Rectangle clientArea = rocTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = rocTable.getTopIndex();
                while (index < rocTable.getItemCount()) {
                    boolean visible = false;
                    TableItem item = rocTable.getItem(index);
                    for (int i = 0; i < rocTable.getColumnCount(); i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            if (item.getData() != null) {
                                rocSetChartSeries((ROCCurve[]) item.getData());
                            }
                            getModel().setSelectedClassValue(item.getText(0));
                            getController().update(new ModelEvent(ViewStatisticsClassification.this,
                                                                  ModelPart.SELECTED_CLASS_VALUE,
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

        return this.rocRoot;
    }
    
    /**
     * Resets the chart
     */
    private void rocResetChart() {

        if (rocChart != null) {
            rocChart.dispose();
        }
        rocChart = new Chart(this.rocSash, SWT.NONE);
        rocChart.setOrientation(SWT.HORIZONTAL);
        
        // Show/Hide axis
        rocChart.addControlListener(new ControlAdapter(){
            @Override
            public void controlResized(ControlEvent arg0) {
                updateCategories(rocChart);
            }
        });

        // Update font
        FontData[] fd = rocChart.getFont().getFontData();
        fd[0].setHeight(8);
        final Font font = new Font(rocChart.getDisplay(), fd[0]);
        rocChart.setFont(font);
        rocChart.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                if (font != null && !font.isDisposed()) {
                    font.dispose();
                }
            } 
        });
        
        // Update title
        ITitle graphTitle = rocChart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(rocChart.getFont());
        
        // Set colors
        rocChart.setBackground(rocRoot.getBackground());
        rocChart.setForeground(rocRoot.getForeground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
            int r = rocChart.getBackground().getRed()-13;
            int g = rocChart.getBackground().getGreen()-13;
            int b = rocChart.getBackground().getBlue()-13;
            r = r>0 ? r : 0;
            r = g>0 ? g : 0;
            r = b>0 ? b : 0;
            final Color background = new Color(rocChart.getDisplay(), r, g, b);
            rocChart.setBackground(background);
            rocChart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    if (background != null && !background.isDisposed()) {
                        background.dispose();
                    }
                } 
            });
        }

        // Initialize axes
        IAxisSet axisSet = rocChart.getAxisSet();
        IAxis yAxis = axisSet.getYAxis(0);
        IAxis xAxis = axisSet.getXAxis(0);
        ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(rocChart.getFont());
        yAxis.getTitle().setFont(rocChart.getFont());
        xAxis.getTick().setFont(rocChart.getFont());
        yAxis.getTick().setFont(rocChart.getFont());
        xAxis.getTick().setForeground(rocChart.getForeground());
        yAxis.getTick().setForeground(rocChart.getForeground());
        xAxis.getTitle().setForeground(rocChart.getForeground());
        yAxis.getTitle().setForeground(rocChart.getForeground());

        // Initialize axes
        ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.19")); //$NON-NLS-1$
        xAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.20")); //$NON-NLS-1$
        rocChart.setEnabled(false);
        updateCategories(rocChart);
    }

    /**
     * Updates the chart with a new ROC Curve
     * 
     * @param data
     */
    private void rocSetChartSeries(ROCCurve[] data) {
        
        ROCCurve original = data[0];
        ROCCurve output = data[1];

        // Init data
        rocChart.setRedraw(false);

        ISeriesSet seriesSet = rocChart.getSeriesSet();
        ISeries[] seriesArray = seriesSet.getSeries();
        // Clear set
        for (ISeries s : seriesArray) {
            rocChart.getSeriesSet().deleteSeries(s.getId());
        }

        // Original
        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, 
                                                                  Resources.getMessage("ViewStatisticsClassificationInput.25")); // $NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(rocChart.getFont());
        series.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        series.setXSeries(original.getFalsePositiveRate());
        series.setYSeries(original.getTruePositiveRate());
        series.setAntialias(SWT.ON);
        series.setSymbolType(PlotSymbolType.NONE);
        series.enableArea(false);

        // Output
        if (output != null) {
            series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, 
                                                          Resources.getMessage("ViewStatisticsClassificationInput.26")); // $NON-NLS-1$
            series.getLabel().setVisible(false);
            series.getLabel().setFont(rocChart.getFont());
            series.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            series.setXSeries(output.getFalsePositiveRate());
            series.setYSeries(output.getTruePositiveRate());
            series.setAntialias(SWT.ON);
            series.setSymbolType(PlotSymbolType.NONE);
            series.enableArea(false);
        }

        rocChart.getLegend().setVisible(true);
        rocChart.getLegend().setPosition(SWT.TOP);

        IAxisSet axisSet = rocChart.getAxisSet();
        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));
        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setRange(new Range(0d, 1d));
        xAxis.adjustRange();

        rocChart.setRedraw(true);
        rocChart.updateLayout();
        rocChart.update();
        rocChart.redraw();
    }
    
    
    /**
     * Updates the selection on ROC data
     */
    private void rocUpdateSelection() {
        
        // Update table
        int index = 0;
        for (TableItem item : rocTable.getItems()) {
            if (item.getText(0).equals(super.getModel().getSelectedClassValue())) {
                rocTable.select(index);
                if (item.getData() != null) {
                    rocSetChartSeries((ROCCurve[]) item.getData());
                }
                return;
            }
            index++;
        }
        
        // Update combo
        String selectedAttribute = getModel().getSelectedAttribute();
        for (int i = 0; i < rocCombo.getItemCount(); i++) {
            if (rocCombo.getItem(i).equals(selectedAttribute)) {
                rocCombo.select(i);
                rocUpdateTableAndChart(selectedAttribute);
                break;
            }
        }
    }


    /**
     * Updates class values and AUC in table and roc curve according to this clazz.
     * 
     * @param clazz
     */
    private void rocUpdateTableAndChart(String clazz){
        
        // Check
        if (originalRocCurves.isEmpty() || !originalRocCurves.containsKey(clazz)) {
            return;
        }
        
        // Reset chart
        for (final TableItem i : rocTable.getItems()) {
            i.dispose();
        }
        
        // Create entries
        List<String> values = new ArrayList<>(originalRocCurves.get(clazz).keySet());
        Collections.sort(values);
        for(String value : values){
            
            TableItem item = new TableItem(rocTable, SWT.NONE);
            item.setText(0, value);
            ROCCurve[] data = {null, null};
            item.setData(data);
            
            // Original
            ROCCurve c = originalRocCurves.get(clazz).get(value);
            item.setData("1", c.getAUC());
            data[0] = c;
            
            // Output
            if (isOutput) {
                ROCCurve c2 = rocCurves.get(clazz).get(value);
                item.setData("2", c2.getAUC());
                data[1] = c2;
            }
        }
    }

    /**
     * Makes the chart show category labels or not.
     */
    private void updateCategories(Chart chart){
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

        // Create top composite
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new FillLayout());
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar();

        // Add update button
        bar.add(Resources.getMessage("ViewStatisticsClassificationInput.29"), getController().getResources().getManagedImage("arrow_refresh.png"), new Runnable(){ //$NON-NLS-1$ //$NON-NLS-2$ 
            public void run() {
                triggerUpdate();
            }
        });
        
        this.folder = new ComponentTitledFolder(root, null, bar, null, true, false);
        
        
        // Precision and recall
        Composite item1 = folder.createItem(Resources.getMessage("ViewStatisticsClassificationInput.27"), //$NON-NLS-1$
                                            getController().getResources().getManagedImage("precision_recall.png")); //$NON-NLS-1$
        item1.setLayoutData(SWTUtil.createFillGridData());
        this.precisionRecallCreateControl(item1);
        
        // Precision and recall
        Composite item2 = folder.createItem(Resources.getMessage("ViewStatisticsClassificationInput.28"), //$NON-NLS-1$
                                            getController().getResources().getManagedImage("roc.png")); //$NON-NLS-1$
        item2.setLayoutData(SWTUtil.createFillGridData());
        this.rocCreateControl(item2);
        
        // Synchronize
        this.folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (other != null) {
                    other.setSelectionIndex(folder.getSelectionIndex());
                }
            }
        });
        
        // Return
        return root;
    }
    
    @Override
    protected AnalysisContextClassification createViewConfig(AnalysisContext context) {
        return new AnalysisContextClassification(context);
    }
    
    @Override
    protected void doReset() {
        
        // Manager
        if (this.manager != null) {
            this.manager.stop();
        }
        
        // Precision and recall
        precisionRecallTable.setRedraw(false);
        for (final TableItem i : precisionRecallTable.getItems()) {
            i.dispose();
        }
        precisionRecallTable.setRedraw(true);
        precisionRecallResetChart();
        
        // ROC
        rocTable.setRedraw(false);
        for (final TableItem i : rocTable.getItems()) {
            i.dispose();
        }
        rocTable.setRedraw(true);
        if (rocCombo != null && rocCombo.getItemCount() != 0) rocCombo.select(0);
        if (rocCurves != null) {
            rocCurves.clear();
        }
        if (originalRocCurves != null) {
            originalRocCurves.clear();
        }
        rocResetChart();
        
        // Reset view
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextClassification context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final String[] features = context.model.getSelectedFeatures().toArray(new String[0]);
        final String[] classes = context.model.getSelectedClasses().toArray(new String[0]);
        final ARXClassificationConfiguration<?> config = context.model.getClassificationModel().getCurrentConfiguration();
        final ARXFeatureScaling scaling = context.model.getClassificationModel().getFeatureScaling();
        
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
                rocCurves.clear();
                originalRocCurves.clear();
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
                for (final TableItem i : precisionRecallTable.getItems()) {
                    i.dispose();
                }

                // Create entries
                for (int i = 0; i < classes.length; i++) {
                    TableItem item = new TableItem(precisionRecallTable, SWT.NONE);
                    item.setText(0, classes[i]);
                    item.setText(1, String.valueOf(numClasses.get(i)));
                    for (int j = 0; j < values.get(i).size(); j++) {
                        item.setData(String.valueOf(2 + j), values.get(i).get(j));
                    }
                    item.setData(matrixes.get(i));
                }

                // Update precision/recall view
                String clazz = getModel().getSelectedAttribute();
                int index = getIndexOf(classes, clazz);
                precisionRecallTable.setFocus();
                precisionRecallTable.select(index);
                precisionRecallSetChartSeries(matrixes.get(index));
                precisionRecallRoot.layout();
                precisionRecallSash.setWeights(new int[] {2, 2});
                
                // Update combo box
                rocCombo.setItems(classes);
                rocCombo.select(index);
                
                // Update ROC View
                String clazzValue = getModel().getSelectedClassValue();
                rocUpdateTableAndChart(classes[index]);
                rocTable.setFocus();
                index = 0;
                for (int i=0; i<rocTable.getItemCount(); i++) {
                    if (rocTable.getItem(i).getText().equals(clazzValue)) {
                        index = i;
                        break;
                    }
                }
                rocTable.select(index);
                rocSetChartSeries(((ROCCurve[])rocTable.getItem(index).getData()));
                rocRoot.layout();
                rocSash.setWeights(new int[] {2, 2});
                setStatusDone();
                
                // Select first element in folder
                if (folder.getSelectionIndex() == -1) {
                    folder.setSelection(0);
                }
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

                // Clear
                rocCurves.clear();
                originalRocCurves.clear();
                
                // Do work
                for (String clazz : classes) {
                    
                    // Compute
                    StatisticsClassification result = builder.getClassificationPerformance(features,
                                                                                           clazz,
                                                                                           config,
                                                                                           scaling);
                    progress++;
                    if (stopped) {
                        break;
                    }
                    
                    // Collect pecision/recall data
                    numClasses.add(result.getNumClasses());
                    values.add(getColumnValues(result));
                    matrixes.add(result.getPrecisionRecall());
                    

                    // Collect ROC curves
                    if(!originalRocCurves.containsKey(clazz)){
                        originalRocCurves.put(clazz, new HashMap<String, ROCCurve>());
                        rocCurves.put(clazz, new HashMap<String, ROCCurve>());
                    }
                    for (String c : result.getClassValues()) {
                        originalRocCurves.get(clazz).put(c, result.getOriginalROCCurve(c));
                        if (result.getROCCurve(c) != null) {
                            rocCurves.get(clazz).put(c, result.getROCCurve(c));
                        }
                    }
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

    /**
     * Sets the other view for synchronization
     * @param other
     */
    protected void setOtherView(ViewStatisticsClassification other) {
        this.other = other;
    }

    /**
     * Sets the selection index in the underlying folder
     * @param index
     */
    protected void setSelectionIndex(int index) {
        this.folder.setSelection(index);
    }
}
