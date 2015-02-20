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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.risk.RiskEstimator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
 * This view displays information about the equivalence classes
 * 
 * @author Fabian Prasser
 */
public class ViewDistributionPlot implements IView {

    /** Minimal width of a category label. */
    private static final int MIN_CATEGORY_WIDTH = 10;
    
    /** Controller */
    private final Controller controller;

    /** View */
    private final Composite  root;

    /** Model */
    private RiskEstimator    estimator          = null;

    /** Model */
    private Model            model              = null;

    /** The chart. */
    private Chart            chart;

    /** Model */
    private Set<String>      qis                = new HashSet<String>();

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewDistributionPlot(final Composite parent,
                                  final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
        reset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {

        qis.clear();
        
        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(root, SWT.NONE);
        chart.setOrientation(SWT.HORIZONTAL);
        chart.setLayoutData(SWTUtil.createFillGridData());
        
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui
     * .model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
        } else if (event.part == ModelPart.INPUT ||
                   event.part == ModelPart.ATTRIBUTE_TYPE) {
            if (model != null && model.getInputConfig() != null &&
                model.getInputConfig().getInput() != null) {
                
                if (!model.getInputDefinition().getQuasiIdentifyingAttributes().equals(qis)) {
                    qis = model.getInputDefinition().getQuasiIdentifyingAttributes();
                    estimator = model.getInputConfig()
                                     .getInput()
                                     .getHandle()
                                     .getRiskEstimator(model.getInputDefinition());
                    update();
                }
                
            } else {
                reset();
            }
        }
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Sort descending by size
        int[][] distribution = estimator.getEquivalenceClassSizeDistribution();
        Arrays.sort(distribution, new Comparator<int[]>() {
            public int compare(int[] o1, int[] o2) {
                return Integer.compare(o1[0], o2[0]);
            }
        });

        // Create array
        double[] frequencies = new double[distribution.length];
        String[] labels = new String[distribution.length];
        for (int i = 0; i < distribution.length; i++) {
            frequencies[i] = (double) distribution[i][1] /
                             (double) estimator.getNumRows() * 100d;
            labels[i] = String.valueOf(distribution[i][0]);
        }

        // Update chart
        chart.setRedraw(false);

        ISeriesSet seriesSet = chart.getSeriesSet();
        IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(chart.getFont());
        series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        series.setYSeries(frequencies);
        chart.getLegend().setVisible(false);

        IAxisSet axisSet = chart.getAxisSet();

        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));
        yAxis.adjustRange();

        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setCategorySeries(labels);
        xAxis.adjustRange();
        updateCategories();

        chart.updateLayout();
        chart.update();
        chart.setRedraw(true);
        chart.redraw();
        SWTUtil.enable(chart);
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
}
