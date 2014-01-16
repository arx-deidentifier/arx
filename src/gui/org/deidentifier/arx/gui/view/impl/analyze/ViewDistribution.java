/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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

public class ViewDistribution extends ViewStatistics implements IView {

    private static final long serialVersionUID = -163862008754422422L;
    
    private Chart                       chart;
    private final Composite             parent;
    private final ModelPart             reset;
    private String                      attribute;
    private final Controller            controller;
    private final Map<String, double[]> cache  = new HashMap<String, double[]>();

    public ViewDistribution(final Composite parent,
                            final Controller controller,
                            final ModelPart target,
                            final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
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

    @Override
    public void dispose() {
        clearCache();
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        if (chart != null) {
            chart.dispose();
        }
        chart = new Chart(parent, SWT.NONE);
        chart.setOrientation(SWT.HORIZONTAL);
        final ITitle graphTitle = chart.getTitle();
        graphTitle.setText(""); //$NON-NLS-1$
        graphTitle.setFont(MainWindow.FONT);
        
        chart.setBackground(parent.getBackground());
        
        // OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){
        	int r = chart.getBackground().getRed()-13;
        	int g = chart.getBackground().getGreen()-13;
        	int b = chart.getBackground().getBlue()-13;
        	r = r>0 ? r : 0;
        	r = g>0 ? g : 0;
        	r = b>0 ? b : 0;
        	final org.eclipse.swt.graphics.Color c2 = new org.eclipse.swt.graphics.Color(controller.getResources().getDisplay(), r, g, b);
        	chart.setBackground(c2);
        	chart.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent arg0) {
                    c2.dispose();
                } 
            });
        }

        final IAxisSet axisSet = chart.getAxisSet();
        final IAxis yAxis = axisSet.getYAxis(0);
        final IAxis xAxis = axisSet.getXAxis(0);
        final ITitle xAxisTitle = xAxis.getTitle();
        xAxisTitle.setText(""); //$NON-NLS-1$
        xAxis.getTitle().setFont(MainWindow.FONT);
        yAxis.getTitle().setFont(MainWindow.FONT);
        xAxis.getTick().setFont(MainWindow.FONT);
        yAxis.getTick().setFont(MainWindow.FONT);

        final ITitle yAxisTitle = yAxis.getTitle();
        yAxisTitle.setText(""); //$NON-NLS-1$
        if (chart != null) {
            chart.setEnabled(false);
        }
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            if (chart != null) chart.setEnabled(true);
            clearCache();
            redraw();
        }

        if (event.part == reset) {
            
            clearCache();
            reset();
            
        } else if (event.part == target) {
            
            if (chart != null) chart.setEnabled(true);
            clearCache();
            redraw();
            
        } else if (event.part == ModelPart.MODEL) {
            
            model = (Model) event.data;
            clearCache();
            reset();

        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {

            attribute = (String) event.data;
            if (chart != null) chart.setEnabled(true);
            redraw();
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {

            attribute = (String) event.data;
            if (chart != null) chart.setEnabled(true);
            redraw();
             
        } else if (event.part == ModelPart.VIEW_CONFIG) {
            
            if (chart != null) chart.setEnabled(true);
            clearCache();
            redraw();
        }
    }

    private void analyze() {

        if (model == null) { return; }

        // Obtain the right config
        ModelConfiguration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        DataHandle data = getHandle();

        // Clear if nothing to draw
        if ((config == null) || (data == null)) {
            reset();
            return;
        }

        // Project onto subset, if possible
        if (data != null && model.getViewConfig().isSubset()){
            data = data.getView();
        }

        final int index = data.getColumnIndexOf(attribute);

        if (index == -1) {
            clearCache();
            reset();
            return;
        }

        if (cache.containsKey(attribute)) { return; }

        // Check if there is a hierarchy
        final AttributeType type = config.getInput()
                                         .getDefinition()
                                         .getAttributeType(attribute);
        Hierarchy hierarchy = null;
        if (type instanceof Hierarchy) {
            hierarchy = (Hierarchy) type;
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            hierarchy = config.getHierarchy(attribute);
        }

        // Count
        final Map<String, Double> map = new HashMap<String, Double>();
        for (int i = 0; i < data.getNumRows(); i++) {
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
        if (hierarchy != null && hierarchy.getHierarchy()!=null && hierarchy.getHierarchy().length != 0) {

            final int level = data.getGeneralization(attribute);
            final List<String> list = new ArrayList<String>();
            final Set<String> done = new HashSet<String>();
            final String[][] h = hierarchy.getHierarchy();
            for (int i = 0; i < h.length; i++) {
                final String val = h[i][level];
                if (map.containsKey(val)) {
                    if (!done.contains(val)) {
                        list.add(val);
                        done.add(val);
                    }
                }
            }
            if (model.getAnonymizer() != null &&
                map.containsKey(model.getAnonymizer().getSuppressionString()) &&
                !done.contains(model.getAnonymizer().getSuppressionString())) {
                
                    list.add(model.getAnonymizer().getSuppressionString());
            }

            dvals = list.toArray(new String[] {});

            // Else sort per data type
        } else {
            final DataType<?> dtype = data.getDataType(attribute);
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
        final double[] distribution = new double[map.size()];
        for (int i = 0; i < dvals.length; i ++) {
            distribution[i] = map.get(dvals[i]) / sum;
        }

        // Cache
        cache.put(attribute, distribution);
    }

    private void clearCache() {
        cache.clear();
    }

    private void redraw() {

        analyze();
        
        if (cache.isEmpty() || (cache.get(attribute) == null)) { return; }

        chart.setRedraw(false);

        final ISeriesSet seriesSet = chart.getSeriesSet();
        final IBarSeries series = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                                      Resources.getMessage("DistributionView.9")); //$NON-NLS-1$
        series.getLabel().setVisible(false);
        series.getLabel().setFont(MainWindow.FONT);
        series.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        series.setYSeries(cache.get(attribute));

        final IAxisSet axisSet = chart.getAxisSet();

        final IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0d, 1d));
        yAxis.adjustRange();

        final IAxis xAxis = axisSet.getXAxis(0);
        xAxis.adjustRange();

        chart.updateLayout();
        chart.update();
        chart.setRedraw(true);
        chart.redraw();
    }
    
}
