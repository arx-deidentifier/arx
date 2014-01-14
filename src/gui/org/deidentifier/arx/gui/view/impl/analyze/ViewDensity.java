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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
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
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;

public class ViewDensity implements IView {

    private final Controller     controller;

    private Model                model;
    private final ModelPart      reset;
    private final ModelPart      target;

    public ViewDensity(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        parent.setLayout(new GridLayout());

     Composite chartComposite = new Composite(parent, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
     chartComposite.setLayoutData(SWTUtil.createFillGridData());

     org.eclipse.swt.graphics.Color backgroundColor = parent.getBackground();

     Frame chartPanel = SWT_AWT.new_Frame(chartComposite);

     chartPanel.setBackground(new java.awt.Color(backgroundColor.getRed(),
                                                 backgroundColor.getGreen(),
                                                 backgroundColor.getBlue()));

     chartPanel.setLayout(new BorderLayout());
     ChartPanel cp = new ChartPanel(createChart(createDataset()));
     cp.setDoubleBuffered(true);
     chartPanel.add(cp, BorderLayout.CENTER); 

        
        // Reset
        reset();
    }
    
    private static JFreeChart createChart(XYZDataset dataset) { 
        DateAxis xAxis = new DateAxis("Date"); 
        xAxis.setLowerMargin(0.0); 
        xAxis.setUpperMargin(0.0); 
        NumberAxis yAxis = new NumberAxis("Hour"); 
        yAxis.setUpperMargin(0.0); 
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
        XYBlockRenderer renderer = new XYBlockRenderer(); 
        renderer.setBlockWidth(1000.0 * 60.0 * 60.0 * 24.0); 
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT); 
        LookupPaintScale paintScale = new LookupPaintScale(); 
        paintScale.add(new Double(1.0), Color.red); 
        paintScale.add(new Double(2.0), Color.green); 
        paintScale.add(new Double(3.0), Color.blue);         
        paintScale.add(new Double(4.0), Color.yellow); 
        renderer.setPaintScale(paintScale); 
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer); 
        plot.setOrientation(PlotOrientation.HORIZONTAL); 
        plot.setBackgroundPaint(Color.lightGray); 
        plot.setRangeGridlinePaint(Color.white); 
        JFreeChart chart = new JFreeChart("", plot); 
        chart.removeLegend(); 
        chart.setBackgroundPaint(Color.white); 
        return chart; 
    } 
     
    /** 
     * Creates a sample dataset. 
     *  
     * @return A sample dataset. 
     */ 
    private static XYZDataset createDataset() { 
        double[] xvalues = new double[2400];     
        double[] yvalues = new double[2400];     
        double[] zvalues = new double[2400]; 
        RegularTimePeriod t = new Day(); 
        for (int days = 0; days < 100; days++) { 
            double value = 1.0; 
            for (int hour = 0; hour < 24; hour++) { 
                if (Math.random() < 0.1) { 
                    value = Math.random() * 4.0; 
                } 
                xvalues[days * 24 + hour] = t.getFirstMillisecond(); 
                yvalues[days * 24 + hour] = hour; 
                zvalues[days * 24 + hour] = value; 
            } 
            t = t.next(); 
        } 
        DefaultXYZDataset dataset = new DefaultXYZDataset(); 
        dataset.addSeries("Series 1",  
                new double[][] { xvalues, yvalues, zvalues }); 
        return dataset; 
    } 

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        resetPlot();
        if (model != null) model.resetAttributePair();
    }
    
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            resetPlot();
            redraw();
        }

        // Handle reset target, i.e., e.g. input has changed
        if (event.part == reset) {
            reset();
            
            // Handle new project
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
            
            // Handle new data
        } else if (event.part == target) {
            resetPlot();
            redraw();
            
            // Handle selected attribute
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.VIEW_CONFIG) {
            if (model.getAttributePair()[0] != null &&
                model.getAttributePair()[1] != null) {
                resetPlot();
                redraw();
            }
        } 
    }

    /**
     * Returns the respective data handle
     * @return
     */
    private DataHandle getData() {
        
        // Obtain the right config
        ModelConfiguration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        DataHandle data;
        if (target == ModelPart.INPUT) {
            data = config.getInput().getHandle();
        } else {
            data = model.getOutput();
        }
        
        // Project onto subset, if possible
        if (data != null && model.getViewConfig().isSubset()){
            data = data.getView();
        }
        
        // Clear if nothing to draw
        if ((config == null) || (data == null)) {
            return null;
        } else {
            return data;
        }
    }

    /**
     * Returns the labels sorted per hierarchy or per data type
     * 
     * @param attribute
     * @return
     */
    private String[] getLabels(final String attribute) {

        // Obtain the right config
        ModelConfiguration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        final DataHandle data;
        if (target == ModelPart.INPUT) {
            data = config.getInput().getHandle();
        } else {
            data = model.getOutput();
        }

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
        final int index = data.getColumnIndexOf(attribute);
        final Set<String> elems = new HashSet<String>();
        for (int i = 0; i < data.getNumRows(); i++) {
            elems.add(data.getValue(i, index));
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
                if (elems.contains(val) && !done.contains(val)) {
                    list.add(val);
                    done.add(val);
                }
            }
            if (model.getAnonymizer() != null &&
                    elems.contains(model.getAnonymizer().getSuppressionString()) &&
                    !done.contains(model.getAnonymizer().getSuppressionString())) {
                    
                        list.add(model.getAnonymizer().getSuppressionString());
                }

            dvals = list.toArray(new String[] {});

            // Else sort per data type
        } else {
            final DataType<?> dtype = data.getDataType(attribute);
            final String[] v = new String[elems.size()];
            int i = 0;
            for (final String s : elems) {
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

        return dvals;
    }

    /**
     * Redraws the plot
     */
    private void redraw() {

        if (model == null) { return; }

        if ((model.getAttributePair()[0] != null) &&
            (model.getAttributePair()[1] != null)) {

            final long time = System.currentTimeMillis();

            DataHandle data = getData();
            if (data == null) {
                reset();
                return;
            }

            // Draw

            final int index1 = data.getColumnIndexOf(model.getAttributePair()[0]);
            final int index2 = data.getColumnIndexOf(model.getAttributePair()[1]);

            if (index1 < 0 || index2 < 0) return;

            final String[] vals1 = getLabels(model.getAttributePair()[0]);
            final String[] vals2 = getLabels(model.getAttributePair()[1]);

            final Map<String, Integer> map1 = new HashMap<String, Integer>();
            final Map<String, Integer> map2 = new HashMap<String, Integer>();

            int index = 0;
            for (int i = 0; i < vals1.length; i ++) {
                map1.put(vals1[i], index++);
            }
            index = 0;
            for (int i = 0; i < vals2.length; i ++) {
                 map2.put(vals2[i], index++);
            }
          
            final short[] heat = new short[vals1.length * vals2.length];

            int max = 0;
            for (int row = 0; row < data.getNumRows(); row++) {
                
                String v1 = data.getValue(row, index1);
                String v2 = data.getValue(row, index2);
                Integer i1 = map1.get(v1);
                Integer i2 = map2.get(v2);

                index = (i2 * vals1.length) + i1;
                heat[index]++;
                max = (heat[index] > max ? heat[index] : max);
            }

            map1.clear();
            map2.clear();

            controller.getResources()
                      .getLogger()
                      .info("Density computed in " + (System.currentTimeMillis() - time)); //$NON-NLS-1$

            // Don't run this asynchronously, because it seems to cause problems on MS Windows
//            intensityGraph.setMax(max);
//            intensityGraph.setMin(0);
//            intensityGraph.setDataHeight(vals2.length);
//            intensityGraph.setDataWidth(vals1.length);
//            intensityGraph.setColorMap(new ColorMap(PredefinedColorMap.JET,
//                                                    true,
//                                                    true));
//            intensityGraph.getXAxis().setTitle(model.getAttributePair()[0]);
//            intensityGraph.getYAxis().setTitle(model.getAttributePair()[1]);
//            intensityGraph.getXAxis().setRange(new Range(0, vals1.length - 1));
//            intensityGraph.getYAxis().setRange(new Range(0, vals2.length - 1));
//
//            intensityGraph.setDataArray(heat);
//            canvas.setRedraw(true);
//            canvas.redraw();
        }
    }

    /**
     * Recreates the plot, to prevent crashes
     */
    private void resetPlot() {
//        canvas.setRedraw(false);
        
//        if (intensityGraph!=null) intensityGraph.dispose();
//        intensityGraph = new IntensityGraphFigure();
//        intensityGraph.getXAxis().setTitleFont(MainWindow.FONT);
//        intensityGraph.getYAxis().setTitleFont(MainWindow.FONT);
//        intensityGraph.getXAxis().setFont(MainWindow.FONT);
//        intensityGraph.getYAxis().setFont(MainWindow.FONT);
//        intensityGraph.setFont(MainWindow.FONT);
//
//        lws.setContents(intensityGraph);
//        intensityGraph.setDataArray(new short[0]);
//        canvas.setRedraw(true);
    }
}
