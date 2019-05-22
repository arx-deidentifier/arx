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
package org.deidentifier.arx.gui.view.impl.common;

import java.util.Arrays;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
 * A component for displaying risk profiles
 * 
 * @author Fabian Prasser
 */
public class ComponentRiskProfile {

    /**
     * A specific profile that can be added
     * @author Fabian Prasser
     *
     */
    public static class RiskProfile {

        /** Profile data */
        private double[] yValues;
        /** Profile data */
        private String   title;
        /** Profile data */
        private boolean  showInTable;
        
        /**
         * Creates a new instance
         * @param title
         * @param values
         * @param showInTable
         */
        public RiskProfile(String title, double[] values, boolean showInTable) {
            this.yValues = values;
            this.title = title;
            this.showInTable = showInTable;
            checkSeries(yValues);
        }
        
        /**
         * Internal constructor
         */
        private RiskProfile() {
            // Empty by design
        }
        
        /**
         * Creates a copy prepared for internal use
         * @return
         */
        private RiskProfile prepare() {
            RiskProfile result = new RiskProfile();
            result.showInTable = this.showInTable;
            result.title = this.title;
            double[] values = yValues.clone();
            for (int i = 0; i < values.length; i++) {
                values[i] *= 100d;
            }
            values = insertToBack(values, values[values.length-1]);
            result.yValues = values;
            return result;
        }
    }
    
    /**
     * Checks a given series
     * @param values
     */
    private static void checkSeries(double[] values) {
        for (double value : values) {
            if (value < 0d) {
                throw new IllegalArgumentException("Values must be >= 0 but is " + value);
            }
        }
    }
    
    /**
     * Insert item to back
     * @param array
     * @param value
     * @return
     */
    private static double[] insertToBack(double[] array, double value) {
        double[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = value;
        return result;
    }
    /**
     * Insert item to front
     * @param array
     * @param value
     * @return
     */
    private static double[] insertToFront(double[] array, double value) {
        double[] result = Arrays.copyOf(array, array.length + 1);
        for (int i = result.length - 1; i > 0; i--) {
            result[i] = result[i - 1];
        }
        result[0] = value;
        return result;
    }
    
    /** View */
    private Color[] colors = new Color[]{
        Display.getDefault().getSystemColor(SWT.COLOR_RED),
        Display.getDefault().getSystemColor(SWT.COLOR_BLUE),
        Display.getDefault().getSystemColor(SWT.COLOR_BLACK)
    };

    /** View */
    private ComponentRiskProfile  other;
    /** View */
    private Composite             root;
    /** View */
    private ComponentTitledFolder folder;
    /** View */
    private DynamicTable          table;
    /** View */
    private Composite             chartRoot;
    /** View */
    private Chart                 chart;
    /** View */
    private String                yAxisTitle;
    /** View */
    private String                xAxisTitle;

    /** Model */
    private RiskProfile[]         profiles;
    /** Model */
    private double[]              xValues;
    /** Model */
    private String[]              labels;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public ComponentRiskProfile(final Composite parent,
                                final Controller controller) {
        
        // Create top composite
        this.root = new Composite(parent, SWT.NONE);
        root.setLayout(new FillLayout());
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar();

        // Add update button
        bar.add(Resources.getMessage("ViewRiskProfile.4"), controller.getResources().getManagedImage("toggle_log_scale.png"), new Runnable(){ //$NON-NLS-1$ //$NON-NLS-2$ 
            public void run() {
                toggleLogScale();
            }
        });
        

        this.folder = new ComponentTitledFolder(root, null, bar, null, true, false);
        
        // Table
        Composite item1 = folder.createItem(Resources.getMessage("ViewRiskProfile.2"), //$NON-NLS-1$
                                            controller.getResources().getManagedImage("roc.png")); //$NON-NLS-1$
        item1.setLayoutData(SWTUtil.createFillGridData());
        this.createPlot(item1);
        
        // Plot
        Composite item2 = folder.createItem(Resources.getMessage("ViewRiskProfile.3"), //$NON-NLS-1$
                                            controller.getResources().getManagedImage("precision_recall.png")); //$NON-NLS-1$
        item2.setLayoutData(SWTUtil.createFillGridData());
        this.createTable(item2);
        
        // Synchronize
        this.folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (other != null) {
                    other.setSelectionIndex(folder.getSelectionIndex());
                }
            }
        });
        
        // Init
        this.folder.setSelection(0);
    }
    
    /**
     * Returns the root composite
     * @return
     */
    public Composite getControl() {
        return this.root;
    }
    
    /**
     * Resets the component
     */
    public void reset() {
        resetChart();
        resetTable();
    }

    /**
     * Adds another synchronized profile
     * @param other
     */
    public void setOtherProfile(ComponentRiskProfile other) {
        this.other = other;
    }
    /**
     * Sets a set of profiles
     * @param lower Lower bounds of x-buckets
     * @param upper Upper bounds of x-buckets
     * @param profiles
     */
    public void setProfiles(double[] lower, double[] upper, RiskProfile... profiles) {
        
        // Check
        checkSeries(lower);
        checkSeries(upper);
        
        // Prepare profiles
        this.profiles = new RiskProfile[profiles.length];
        for (int i = 0; i < profiles.length; i++) {
            this.profiles[i] = profiles[i].prepare();
        }
        
        // Prepare xValues
        this.xValues = lower.clone();
        for (int i = 0; i < xValues.length; i++) {
            this.xValues[i] *= 100d;
        } 
        this.xValues = insertToBack(this.xValues, upper[upper.length - 1] * 100d);
        
        // Prepare labels
        this.labels = new String[lower.length];
        for (int i = 0; i < lower.length; i++) {
            this.labels[i] = "]" + String.valueOf(SWTUtil.getPrettyString(lower[i] * 100d)) +  //$NON-NLS-1$
                             ", " + String.valueOf(SWTUtil.getPrettyString(upper[i] * 100d)) + "]"; //$NON-NLS-1$ $NON-NLS-2$
        }
        
        // Update
        updateTable();
        updateChart();
        
        // Layout new
        root.layout();
    }

    /**
     * Sets the x-axis title
     * @param title
     */
    public void setXAxisTitle(String title) {
        this.xAxisTitle = title;
    }
    
    /**
     * Sets the y-axis title
     * @param title
     */
    public void setYAxisTitle(String title) {
        this.yAxisTitle = title;
    }
    
    /**
     * Creates the plot
     * @param root
     */
    private void createPlot(final Composite root) {

        // Create root
        root.setLayout(new FillLayout());
        this.chartRoot = root;
        
        // Add tooltip
        final StringBuilder builder = new StringBuilder();
        root.addListener(SWT.MouseMove, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (chart != null) {
                    IAxisSet axisSet = chart.getAxisSet();
                    if (axisSet != null) {
                        IAxis xAxis = axisSet.getXAxis(0);
                        IAxis yAxis = axisSet.getYAxis(0);
                        if (xAxis != null && yAxis != null) {
                            Point cursor = chart.getPlotArea().toControl(Display.getCurrent().getCursorLocation());
                            double x = xAxis.getDataCoordinate(cursor.x);
                            double y = yAxis.getDataCoordinate(cursor.y);
                            builder.setLength(0);
                            builder.append(Resources.getMessage("ViewRisksRiskDistributionPlot.12")); //$NON-NLS-1$
                            builder.append(": ("); //$NON-NLS-1$
                            builder.append(Resources.getMessage("ViewRisksRiskDistributionPlot.10")); //$NON-NLS-1$
                            builder.append(": "); //$NON-NLS-1$
                            builder.append(SWTUtil.getPrettyString(x));
                            builder.append("%, "); //$NON-NLS-1$
                            builder.append(Resources.getMessage("ViewRisksRiskDistributionPlot.11")); //$NON-NLS-1$
                            builder.append(": "); //$NON-NLS-1$
                            builder.append(SWTUtil.getPrettyString(y));
                            builder.append("%)"); //$NON-NLS-1$
                            root.setToolTipText(builder.toString());
                            return;
                        }
                    }
                    root.setToolTipText(null);
                }
            }
        });
        
        // Context menu
        Menu menu = new Menu(root);
        MenuItem item1 = new MenuItem(menu, SWT.CASCADE);
        item1.setText(Resources.getMessage("ViewRiskProfile.0")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                enableLogScale(true);
            }
        });
        MenuItem item2 = new MenuItem(menu, SWT.NONE);
        item2.setText(Resources.getMessage("ViewRiskProfile.1")); //$NON-NLS-1$
        item2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                enableLogScale(false);
            }
        });
        root.setMenu(menu);
    }
    /**
     * Creates the table
     * @param root
     */
    private void createTable(Composite root) {
        
        root.setLayout(new FillLayout());
        table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        SWTUtil.createGenericTooltip(table);
    }

    /**
     * Toggles the log scale setting
     * @param enable
     */
    private void toggleLogScale() {
        if (chart != null) {
            IAxis[] axes = chart.getAxisSet().getXAxes();
            if (axes != null && axes.length > 0 && axes[0] != null) {
                this.enableLogScale(!axes[0].isLogScaleEnabled());
            }
        }
    }
    
    /**
     * Enables log scale
     * @param enable
     */
    private void enableLogScale(boolean enable) {
        if (chart != null) {
            IAxis[] axes = chart.getAxisSet().getXAxes();
            if (axes != null && axes.length > 0 && axes[0] != null) {
                
                // Nothing to do
                if (enable == axes[0].isLogScaleEnabled()) {
                    return;
                }
                
                // Prepare data
                ISeries[] series = chart.getSeriesSet().getSeries();
                if (series != null && series.length > 0 && series[0] != null) {
                 
                    // Prepare series
                    for (ISeries iSeries : series) {
                        ILineSeries iLineSeries = (ILineSeries)iSeries;
                        double[] xSeries = iLineSeries.getXSeries();
                        if (enable) {
                            xSeries = Arrays.copyOfRange(xSeries, 1, xSeries.length);
                        } else {
                            xSeries = insertToFront(xSeries, 0d);
                        }
                        iLineSeries.setXSeries(xSeries);
                    }
                }
                
                // Do it
                axes[0].enableLogScale(enable);
                chart.redraw();
                
                // Synchronize
                if (other != null) {
                    other.enableLogScale(enable);
                }
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
        chart = new Chart(chartRoot, SWT.DOUBLE_BUFFERED);
        chart.setOrientation(SWT.HORIZONTAL);
        
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
        chart.setBackground(chartRoot.getBackground());
        chart.setForeground(chartRoot.getForeground());
        
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
        final IAxis yAxis = axisSet.getYAxis(0);
        final IAxis xAxis = axisSet.getXAxis(0);
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
        if (this.yAxisTitle != null) yAxisTitle.setText(this.yAxisTitle);
        if (this.xAxisTitle != null) xAxisTitle.setText(this.xAxisTitle);
        chart.setEnabled(false);
    }
    
    /**
     * Resets the table
     */
    private void resetTable() {
        if (table == null) {
            return;
        }
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.clearAll();
        table.setRedraw(true);
    }

    /**
     * Sets the selection index in the underlying folder
     * @param index
     */
    private void setSelectionIndex(int index) {
        this.folder.setSelection(index);
    }
    
    /**
     * Updates the chart
     */
    private void updateChart() {

        // Reset
        resetChart();
        
        // Update chart
        chart.setRedraw(false);
        
        // Add each profile
        int count = 0;
        ISeriesSet seriesSet = chart.getSeriesSet();
        for (RiskProfile p : profiles) {

            ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, p.title);
            series.getLabel().setVisible(false);
            series.getLabel().setFont(chart.getFont());
            series.setLineColor(colors[count % 3]);
            series.setYSeries(p.yValues);
            series.setXSeries(xValues);
            series.setAntialias(SWT.ON);
            series.setSymbolType(PlotSymbolType.NONE);
            series.enableStep(true);
            series.enableArea(true);
            seriesSet.bringToFront(p.title);
            count++;
        }
        
        // Configure legend
        chart.getLegend().setVisible(true);
        chart.getLegend().setPosition(SWT.TOP);

        // Update axes
        IAxisSet axisSet = chart.getAxisSet();
        axisSet.getYAxis(0).setRange(new Range(0d, 100d));
        axisSet.getXAxis(0).adjustRange();
        
        // Enable log scale, synchronize with other
        if (other != null && other.chart != null && other.chart.getAxisSet() != null) {
            if (other.chart.getAxisSet().getXAxes() != null && other.chart.getAxisSet().getXAxes().length > 0) {
                enableLogScale(other.chart.getAxisSet().getXAxes()[0].isLogScaleEnabled());
            }
        }

        // Layout
        chart.updateLayout();
        chart.update();
        chart.setRedraw(true);
    }
    
    /**
     * Update table
     */
    private void updateTable() {
        
        // Reset
        resetTable();
        
        // Disable redraw
        table.setRedraw(false);
        
        // Calculate width
        int columns = 1;
        for (RiskProfile p : profiles) {
            if (p.showInTable) columns++;
        }
        int width = (int)Math.round(100d / (double)columns);
        
        // Add column for labels
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(width + "%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        if (xAxisTitle != null) c.setText(xAxisTitle);
        
        // Add column for profiles
        for (RiskProfile p : profiles) {
            if (p.showInTable) {
                c = new DynamicTableColumn(table, SWT.LEFT);
                SWTUtil.createColumnWithBarCharts(table, c);
                c.setWidth(width + "%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
                c.setText(p.title); //$NON-NLS-1$
            }
        }
        
        // Pack
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        
        // Update chart
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }

        // Create entries
        for (int i = labels.length-1; i >=0 ; i--) {
            
            // Create item
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, labels[i]);
            
            // Add values for profiles
            int index = 1;
            for (RiskProfile p : profiles) {
                if (p.showInTable) {
                    item.setData(String.valueOf(index), p.yValues[i] / 100d);
                    index++;
                }
            }
        }

        // Enable drawing and redraw
        table.setRedraw(true);
        table.redraw();
        
    }
}