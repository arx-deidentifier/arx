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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsClassification;
import org.deidentifier.arx.aggregates.StatisticsClassification.ROCCurve;
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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
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
public abstract class ViewStatisticsLogisticRegressionROCCurves extends ViewStatistics<AnalysisContextClassification> {

    /** Minimal width of a category label. */
    private static final int                   MIN_CATEGORY_WIDTH = 10;

    /** Internal stuff. */
    private AnalysisManager                    manager;

    /** View */
    private DynamicTable                       table;
    /** View */
    private Composite                          root;
    /** View */
    private SashForm                           sash;
    /** View */
    private Chart                              chart;
    /** Widget */
    private Combo                              cmbClassAtt;
    /** Map clazz to class value to ROC */
    private Map<String, Map<String, ROCCurve>> class2Values2ROC;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param part
     */
    public ViewStatisticsLogisticRegressionROCCurves(final Composite parent,
                                            final Controller controller,
                                            final ModelPart part) {

        super(parent, controller, part, null, false);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.SELECTED_FEATURES_OR_CLASSES, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.SELECTED_CLASS_VALUE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
    }
    
    /**
     * Updates the selected class attribute.
     */
    private void actionClassAttChanged(){
        if(cmbClassAtt.getSelectionIndex() >=0){
            String selectedClass = cmbClassAtt.getItem(cmbClassAtt.getSelectionIndex());
            updateTableAndChart(selectedClass);
            getController().update(new ModelEvent(ViewStatisticsLogisticRegressionROCCurves.this,
                                                  ModelPart.SELECTED_ATTRIBUTE,
                                                  selectedClass));
        }
    }

    @Override
    protected Control createControl(Composite parent) {
        
        this.class2Values2ROC   = new HashMap<>();

        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        // Shash
        this.sash = new SashForm(this.root, SWT.VERTICAL);
        
        final Composite composite = new Composite(this.sash, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1));

        // Table
        this.table = SWTUtil.createTableDynamic(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setMenu(new ClipboardHandlerTable(table).getMenu());
        this.table.setLayoutData(SWTUtil.createFillGridData(2));

        // Columns
        String[] columns = getColumnHeaders();
        String width = String.valueOf(Math.round(100d / ((double) columns.length + 2) * 100d) / 100d) + "%"; //$NON-NLS-1$
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.22")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(width, "100px"); //$NON-NLS-1$ 
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.23")); //$NON-NLS-1$
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        
        final Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        composite2.setLayout(SWTUtil.createGridLayout(2, false));

        final Label lblClassAtt = new Label(composite2, SWT.PUSH);
        lblClassAtt.setText(Resources.getMessage("ViewStatisticsClassificationInput.21"));
        this.cmbClassAtt = new Combo(composite2, SWT.READ_ONLY);
        this.cmbClassAtt.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.cmbClassAtt.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionClassAttChanged();
            }
        });
        
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
                        IAxis yAxis = axisSet.getYAxis(0);
                        if (xAxis != null && yAxis != null) {
                            Point cursor = chart.getPlotArea().toControl(Display.getCurrent().getCursorLocation());
                            if (cursor.x >= 0 && cursor.x < chart.getPlotArea().getSize().x && cursor.y >= 0 && cursor.y < chart.getPlotArea().getSize().y) {
                                ISeries[] data = chart.getSeriesSet().getSeries();
                                if (data != null && data.length > 0) {
                                    double x = getClosestValue(data[0].getXSeries(), xAxis.getDataCoordinate(cursor.x));
                                    double y = getClosestValue(data[0].getYSeries(), yAxis.getDataCoordinate(cursor.y));

                                    if (x >= 0 && y >= 0) {
                                        builder.setLength(0);
                                        builder.append("("); //$NON-NLS-1$
                                        builder.append(Resources.getMessage("ViewStatisticsClassificationInput.20")) //$NON-NLS-1$
                                               .append(": "); //$NON-NLS-1$ //$NON-NLS-3$
                                        builder.append(SWTUtil.getPrettyString(x));
                                        builder.append(", ") //$NON-NLS-1$
                                               .append(Resources.getMessage("ViewStatisticsClassificationInput.19")) //$NON-NLS-1$
                                               .append(": "); //$NON-NLS-1$
                                        builder.append(SWTUtil.getPrettyString(y));
                                        builder.append(")"); //$NON-NLS-1$
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
                                item.getData() instanceof ROCCurve) {
                                setChartSeries(item.getText(0), (ROCCurve) item.getData());
                            }
                            getModel().setSelectedClassValue(item.getText(0));
                            getController().update(new ModelEvent(ViewStatisticsLogisticRegressionROCCurves.this,
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
        if (cmbClassAtt != null && cmbClassAtt.getItemCount() != 0) cmbClassAtt.select(0);
        class2Values2ROC.clear();
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
        final ARXFeatureScaling scaling = context.model.getClassificationModel().getFeatureScaling();
        
        // Break, if nothing do
        if (context.model.getSelectedFeatures().isEmpty() ||
            context.model.getSelectedClasses().isEmpty()) {
            doReset();
            return;
        }
        
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean                     stopped     = false;
            private int                         progress    = 0;

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
                class2Values2ROC.clear();
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                // Check
                if (stopped || !isEnabled() || getModel().getSelectedFeatures().isEmpty() || getModel().getSelectedClasses().isEmpty()) {
                    setStatusEmpty();
                    return;
                }
                
                // Update combobox
                cmbClassAtt.setItems(classes);
                cmbClassAtt.select(0);
                // Update table and chart
                updateTableAndChart(classes[0]);

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
                
                class2Values2ROC.clear();
                
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
                    
                    // Init map
                    if(!class2Values2ROC.containsKey(clazz)){
                        class2Values2ROC.put(clazz, new HashMap<>());
                    }
                    // collect clazz -> class value -> ROC
                    for (String c : result.getClassValues()) {
                        class2Values2ROC.get(clazz).put(c, result.getROCCurve(c));
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
     * Returns the value contained in the series that is closest to valueClicked while being greater.
     * 
     * @param series
     * @param valueClicked
     * @return
     */
    private double getClosestValue(double[] series, double valueClicked){
        double closest = Double.MAX_VALUE;
        for(int i = 0; i < series.length; i++){
            double value = series[i];
            if(valueClicked > value && Math.abs(value - valueClicked) < Math.abs(closest - valueClicked)){
                closest = value;
            }
        }
        return closest;
    }
    
    /**
     * Returns all column headers
     * @return
     */
    protected String[] getColumnHeaders(){
        return new String[] {
                             Resources.getMessage("ViewStatisticsClassificationInput.22"), //$NON-NLS-1$
                             Resources.getMessage("ViewStatisticsClassificationInput.23"), //$NON-NLS-1$
                     };
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
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.LOGISTIC_REGRESSION;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
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
        yAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.19")); //$NON-NLS-1$
        xAxisTitle.setText(Resources.getMessage("ViewStatisticsClassificationInput.20")); //$NON-NLS-1$
        chart.setEnabled(false);
        updateCategories();
    }
    
    /**
     * Updates the chart with a new rocCurve
     * 
     * @param matrix
     */
    private void setChartSeries(String classValue, ROCCurve rocCurve) {

        // Init data
        double[] xSeries = rocCurve.getFalsePositiveRate();
        double[] ySeries = rocCurve.getTruePositiveRate();

        chart.setRedraw(false);

        ISeriesSet seriesSet = chart.getSeriesSet();
        ISeries[] seriesArray = seriesSet.getSeries();
        // Clear set
        for (ISeries s : seriesArray) {
            chart.getSeriesSet().deleteSeries(s.getId());
        }

        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, classValue); // $NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(chart.getFont());
        series.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        series.setXSeries(xSeries);
        series.setYSeries(ySeries);
        series.setAntialias(SWT.ON);
        series.setSymbolType(PlotSymbolType.CIRCLE);
        series.setSymbolColor(GUIHelper.getColor(255, 32, 32));
        series.enableArea(true);

        chart.getLegend().setVisible(true);
        chart.getLegend().setPosition(SWT.TOP);

        IAxisSet axisSet = chart.getAxisSet();

        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));

        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setRange(new Range(0d, 1d));
        xAxis.adjustRange();

        chart.setRedraw(true);
        chart.updateLayout();
        chart.update();
        chart.redraw();
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
        else if (event.part == ModelPart.SELECTED_CLASS_VALUE) {
            int index = 0;
            for (TableItem item : table.getItems()) {
                if (item.getText(0).equals(super.getModel().getSelectedClassValue())) {
                    table.select(index);
                    if (item.getData() != null && item.getData() instanceof ROCCurve) {
                        setChartSeries(item.getText(0), (ROCCurve) item.getData());
                    }
                    return;
                }
                index++;
            }
        }
        else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            final String selectedAttribute = (String) event.data;
            // Update class attribute combo selection
            for (int i = 0; i < cmbClassAtt.getItemCount(); i++) {
                if (cmbClassAtt.getItem(i).equals(selectedAttribute)) {
                    cmbClassAtt.select(i);
                    updateTableAndChart(selectedAttribute);
                    break;
                }
            }
        }
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

    /**
     * Updates class values and AUC in table and roc curve according to this clazz.
     * 
     * @param clazz
     */
    private void updateTableAndChart(String clazz){
        
        // Check
        if (class2Values2ROC.isEmpty() || !class2Values2ROC.containsKey(clazz)) {
            return;
        }
        
        // Reset chart
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        
        Map<String, ROCCurve> value2ROC = class2Values2ROC.get(clazz);
        List<String> values = new ArrayList<>(value2ROC.keySet());
        Collections.sort(values);
        
        // Create entries
        for(String value : values){
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, value);
            ROCCurve c = value2ROC.get(value);
            item.setText(1, String.valueOf(c.getAUC()));
            item.setData(c);
        }

        table.setFocus();
        table.select(0);
        setChartSeries(values.get(0), value2ROC.get(values.get(0)));
    }
}
