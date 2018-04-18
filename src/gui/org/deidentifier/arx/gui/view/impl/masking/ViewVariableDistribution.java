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

package org.deidentifier.arx.gui.view.impl.masking;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.RandomVariable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;

/**
 * This implements the distribution table plot
 *
 * Code take in part from previous work by Fabian Kloos and Fabian Prasser
 *
 * @author Karol Babioch
 */
public class ViewVariableDistribution implements IView {

    private Controller controller;

    private Chart chart;
    private TableViewer tableViewer;

    public ViewVariableDistribution(final Composite parent, final Controller controller) {

        this.controller = controller;

        this.controller.addListener(ModelPart.MASKING_VARIABLE_SELECTED, this);
        this.controller.addListener(ModelPart.MASKING_VARIABLE_CHANGED, this);

        build(parent);

    }

    private void build(Composite parent) {

        // Create the tab folder
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null); // TODO Assign help id
        folder.setLayoutData(SWTUtil.createFillGridData());

        // Plot view
        Composite compositePlot = folder.createItem("Distribution plot", null);
        compositePlot.setLayout(SWTUtil.createGridLayout(1));

        // Table view
        Composite compositeTable = folder.createItem("Distribution table", null);
        compositeTable.setLayout(SWTUtil.createGridLayout(1));

        // Select distribution plot view by default
        folder.setSelection(0);

        // Create sub-views
        buildPlot(compositePlot);
        buildTable(compositeTable);

    }

    private void buildPlot(Composite composite) {

        // create a chart
        chart = new Chart(composite, SWT.NONE);
        chart.setLayoutData(SWTUtil.createFillGridData());

    }

    private void buildTable(Composite composite) {

        // Create table
        tableViewer = SWTUtil.createTableViewer(composite, SWT.BORDER);
        tableViewer.setContentProvider(new ArrayContentProvider());

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());

        // Column containing X values
        TableViewerColumn tableViewerColumnX = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnX.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return Double.toString(((double[])element)[0]);

            }

        });

        TableColumn columnX = tableViewerColumnX.getColumn();
        columnX.setToolTipText("X values");
        columnX.setText("X");
        columnX.setWidth(100);

        // Column containing Y values
        TableViewerColumn tableViewerColumnY = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnY.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return Double.toString(((double[])element)[1]);

            }

        });

        TableColumn columnY = tableViewerColumnY.getColumn();
        columnY.setToolTipText("P(X=x) values");
        columnY.setText("P(X=x)");
        columnY.setWidth(100);

    }

    @Override
    public void dispose() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(ModelEvent event) {

        // Update chart
        Distribution<Integer> result = ((RandomVariable)event.data).getDistribution();
        ISeriesSet seriesSet = chart.getSeriesSet();
        ISeries series = seriesSet.createSeries(SeriesType.BAR, "values");
        double[] xSeries = getXSeries(result);
        series.setXSeries(xSeries);
        double[] ySeries = getYSeries(xSeries, result);
        series.setYSeries(ySeries);
        chart.getAxisSet().adjustRange();
        chart.getLegend().setVisible(false);
        chart.getTitle().setText("Probability mass function");
        chart.getAxisSet().getYAxis(0).getTitle().setText("P(X=x)");
        chart.getAxisSet().getXAxis(0).getTitle().setText("X");
        chart.redraw();

        // Update table
        tableViewer.setInput(getXY(xSeries, ySeries));

    }

    private List<double[]> getXY(double[] x, double[] y) {

        List<double[]> list = new ArrayList<>();

        for (int i = 0; i  < x.length; i++) {

            list.add(new double[]{x[i], y[i]});

        }

        return list;

    }

    private double[] getXSeries(Distribution<Integer> result) {

        double[] array = new double[result.getMaximum() - result.getMinimum() + 1];
        int index = 0;

        for (int x = result.getMinimum(); x <= result.getMaximum(); x++) {

            array[index++] = x;

        }

        return array;

    }

    private double[] getYSeries(double[] xSeries, Distribution<Integer> result) {

        double[] array = new double[xSeries.length];

        for (int i = 0; i < xSeries.length; i++) {

            array[i] = result.getValue((int) xSeries[i]);

        }

        return array;

    }

}
