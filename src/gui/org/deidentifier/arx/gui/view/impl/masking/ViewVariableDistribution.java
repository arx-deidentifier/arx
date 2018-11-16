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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.RandomVariable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
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

    /** Chart */
    private Chart      chart;

    /** Controller */
    private Controller controller;

    /**
     * Creates an instance.
     * @param parent
     * @param controller
     */
    public ViewVariableDistribution(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.RANDOM_VARIABLE, this);
        build(parent);

    }

    /**
     * Build component.
     * 
     * @param parent
     */
    private void build(Composite parent) {

        // Create the tab folder
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null); // TODO Assign help id
        folder.setLayoutData(SWTUtil.createFillGridData());

        // Plot view
        Composite compositePlot = folder.createItem(Resources.getMessage("MaskingView.4"), null); //$NON-NLS-1$
        compositePlot.setLayout(SWTUtil.createGridLayout(1));

        // Select distribution plot view by default
        folder.setSelection(0);

        // Create sub-views
        buildPlot(compositePlot);
    }

    /**
     * Build plot.
     * 
     * @param composite
     */
    private void buildPlot(Composite composite) {

        // create a chart
        chart = new Chart(composite, SWT.NONE);
        chart.setLayoutData(SWTUtil.createFillGridData());

    }

    /*
     * @Override(non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    public void dispose() {
        // Nothing to do
    }

    /**
     * Returns a double array containing X values of this distribution.
     * @param result
     * @return
     */
    private double[] getXSeries(Distribution<Integer> result) {

        double[] array = new double[result.getMaximum() - result.getMinimum() + 1];
        int index = 0;

        for (int x = result.getMinimum(); x <= result.getMaximum(); x++) {
            array[index++] = x;
        }

        return array;

    }

    /**
     * Returns a double array containing Y values of this distribution for this X values.
     * @param xSeries
     * @param result
     * @return
     */
    private double[] getYSeries(double[] xSeries, Distribution<Integer> result) {

        double[] array = new double[xSeries.length];

        for (int i = 0; i < xSeries.length; i++) {
            array[i] = result.getValue((int) xSeries[i]);
        }

        return array;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        // Nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.RANDOM_VARIABLE) {

            // Update chart
            RandomVariable data = (RandomVariable) event.data;
            Distribution<Integer> result = data.getDistribution();
            ISeriesSet seriesSet = chart.getSeriesSet();
            ISeries series = seriesSet.createSeries(SeriesType.BAR, "values");
            double[] xSeries = getXSeries(result);
            series.setXSeries(xSeries);
            double[] ySeries = getYSeries(xSeries, result);
            series.setYSeries(ySeries);
            chart.getAxisSet().adjustRange();
            chart.getLegend().setVisible(false);
            chart.getTitle().setText(data.getName());
            chart.getAxisSet().getYAxis(0).getTitle().setText(Resources.getMessage("VariableDistributionView.0")); //$NON-NLS-1$
            chart.getAxisSet().getXAxis(0).getTitle().setText(Resources.getMessage("VariableDistributionView.1")); //$NON-NLS-1$
            chart.redraw();

        }
    }

}
