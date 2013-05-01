/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.gui.view.impl.analyze;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.DataType;
import org.deidentifier.flash.gui.Configuration;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IMainWindow;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.Range;

public class DistributionView implements IView {

    private static final int            MAX_DIMENSION = 500;

    private Chart                       chart;
    private final Composite             parent;
    private final EventTarget           target;
    private final EventTarget           reset;
    private String                      attribute;
    private final Controller            controller;
    private final Map<String, double[]> cachedCounts  = new HashMap<String, double[]>();
    private Model                       model;

    public DistributionView(final Composite parent,
                            final Controller controller,
                            final EventTarget target,
                            final EventTarget reset) {

        // Register
        controller.addListener(EventTarget.SELECTED_ATTRIBUTE, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        this.parent = parent;
        reset();
    }

    private void clear() {
        cachedCounts.clear();
    }

    private void compute() {

        if (model == null) { return; }

        final long time = System.currentTimeMillis();

        // Obtain the right config
        Configuration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        final DataHandle data;
        if (target == EventTarget.INPUT) {
            data = config.getInput().getHandle();
        } else {
            data = model.getOutput();
        }

        // Clear if nothing to draw
        if ((config == null) || (data == null)) {
            reset();
            return;
        }

        final int index = data.getColumnIndexOf(attribute);

        if (index == -1) {
            clear();
            reset();
            return;
        }

        if (cachedCounts.containsKey(attribute)) { return; }

        // Check if there is a hierarchy
        final AttributeType type = config.getInput()
                                         .getDefinition()
                                         .getAttributeType(attribute);
        Hierarchy hierarchy = null;
        if (type instanceof Hierarchy) {
            hierarchy = (Hierarchy) type;
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            hierarchy = config.getSensitiveHierarchy();
        }

        // Count
        boolean suppressed = false;
        final Map<String, Double> map = new HashMap<String, Double>();
        for (int i = 0; i < data.getNumRows(); i++) {
            if (!suppressed) {
                suppressed |= data.isOutlier(i);
            }
            final String val = data.getValue(i, index);
            if (!map.containsKey(val)) {
                map.put(val, 1d);
            } else {
                map.put(val, map.get(val) + 1);
            }
        }

        // Init distribution
        final String[] dvals;

        // Sort by hierarchy if possible
        if (hierarchy != null) {

            final int level = data.getGeneralization(attribute);
            final List<String> list = new ArrayList<String>();
            final Set<String> done = new HashSet<String>();
            final String[][] h = hierarchy.getHierarchy();
            for (int i = 0; i < h.length; i++) {
                final String val = h[i][level];
                if (map.containsKey(val) ||
                    ((model.getAnonymizer() != null) && val.equals(model.getAnonymizer()
                                                                        .getSuppressionString()))) {
                    if (!done.contains(val)) {
                        list.add(val);
                        done.add(val);
                    }
                }
            }
            if (suppressed) {
                if (!done.contains(model.getAnonymizer().getSuppressionString())) {
                    list.add(model.getAnonymizer().getSuppressionString());
                    if (!map.containsKey(list.add(model.getAnonymizer()
                                                       .getSuppressionString()))) {
                        map.put(model.getAnonymizer().getSuppressionString(),
                                0d);
                    }
                }
            }

            dvals = list.toArray(new String[] {});

            // Else sort per data type
        } else {
            final DataType dtype = data.getDataType(attribute);
            final String[] v = new String[map.size()];
            int i = 0;
            for (final String s : map.keySet()) {
                v[i++] = s;
            }
            Arrays.sort(v, new Comparator<String>() {
                @Override
                public int compare(final String arg0, final String arg1) {
                    try {
                        return dtype.compare(arg0, arg1);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            dvals = v;
        }

        // Sum up and divide
        double sum = 0;
        for (final double i : map.values()) {
            sum += i;
        }

        int step = map.size() / MAX_DIMENSION; // Round down
        step = Math.max(step, 1);
        final int length = (int) Math.ceil((double) map.size() / (double) step);

        controller.getResources()
                  .getLogger()
                  .info("length:" + length + " step: " + step + "/" + dvals.length + "/" + MAX_DIMENSION); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        int sindex = 0;
        final double[] distribution = new double[length];
        for (int i = 0; i < dvals.length; i += step) {
            for (int j = 0; j < step; j++) {
                if (sindex < distribution.length) {
                    if ((i + j) < dvals.length) {
                        distribution[sindex] += map.get(dvals[i + j]) / sum;
                    }
                } else {
                    controller.getResources()
                              .getLogger()
                              .warn("Index out of bounds"); //$NON-NLS-1$
                }
            }
            sindex++;
        }

        // Cache
        cachedCounts.put(attribute, distribution);

        controller.getResources()
                  .getLogger()
                  .info("Computed distribution in: " + (System.currentTimeMillis() - time)); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        clear();
        controller.removeListener(this);
    }

    private void redraw() {

        compute();
        if (cachedCounts.isEmpty() || (cachedCounts.get(attribute) == null)) { return; }

        chart.setRedraw(false);

        final ISeriesSet seriesSet = chart.getSeriesSet();
        final IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                      Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(IMainWindow.FONT);
        series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        series.setYSeries(cachedCounts.get(attribute));

        final IAxisSet axisSet = chart.getAxisSet();

        final IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));
        yAxis.adjustRange();

        final IAxis xAxis = axisSet.getXAxis(0);
        // xAxis.setCategorySeries(cachedLabels.get(attribute));
        // xAxis.enableCategory(true);
        xAxis.adjustRange();

        chart.updateLayout();
        chart.update();
        chart.setRedraw(true);
        chart.redraw();
    }

    @Override
    public void reset() {
        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(parent, SWT.NONE);
        chart.setOrientation(SWT.HORIZONTAL);
        // chart.setForeground(new Color(null, 0,0,0));
        final ITitle graphTitle = chart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(IMainWindow.FONT);
        
        chart.setBackground(parent.getBackground());
        
        // TODO: OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){
        	int r = chart.getBackground().getRed()-13;
        	int g = chart.getBackground().getGreen()-13;
        	int b = chart.getBackground().getBlue()-13;
        	r = r>0 ? r : 0;
        	r = g>0 ? g : 0;
        	r = b>0 ? b : 0;
        	org.eclipse.swt.graphics.Color c2 = new org.eclipse.swt.graphics.Color(controller.getResources().getDisplay(), r, g, b);
        	chart.setBackground(c2);
        }

        final IAxisSet axisSet = chart.getAxisSet();
        final IAxis yAxis = axisSet.getYAxis(0);
        final IAxis xAxis = axisSet.getXAxis(0);
        final ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(IMainWindow.FONT);
        yAxis.getTitle().setFont(IMainWindow.FONT);
        xAxis.getTick().setFont(IMainWindow.FONT);
        yAxis.getTick().setFont(IMainWindow.FONT);

        final ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(""); //$NON-NLS-1$
        if (chart != null) {
            chart.setEnabled(false);
        }
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.target == EventTarget.OUTPUT) {
            if (chart != null) {
                chart.setEnabled(true);
            }
            clear();
            redraw();
        }

        // Handle reset target, i.e., e.g. input has changed
        if (event.target == reset) {
            clear();
            reset();
        } else if (event.target == target) {
            if (chart != null) {
                chart.setEnabled(true);
            }
            clear();
            redraw();
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
            clear();
            reset();
            // Handle selected attribute
        } else if (event.target == EventTarget.SELECTED_ATTRIBUTE) {

            attribute = (String) event.data;

            if (chart != null) {
                chart.setEnabled(true);
            }
            redraw();
        }
    }
}
