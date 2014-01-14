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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleAnchor;

public class ViewDensity implements IView {

	private static final LookupPaintScale GRADIENT = getGradient();
	
	private final Controller      controller;
	private final ChartComposite  composite;
	private final ChartPanel      panel;

	private Model                 model;
	private final ModelPart       reset;
	private final ModelPart       target;

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
        composite = new ChartComposite(parent, SWT.BORDER);
        composite.setLayoutData(SWTUtil.createFillGridData());
        composite.setChart(getEmptyChart());
        panel = null;
        
//        parent.setLayout(new GridLayout());
//        Composite chartComposite = new Composite(parent, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
//        chartComposite.setLayoutData(SWTUtil.createFillGridData());
//        org.eclipse.swt.graphics.Color backgroundColor = parent.getBackground();
//        Frame chartPanel = SWT_AWT.new_Frame(chartComposite);
//        chartPanel.setBackground(new java.awt.Color(backgroundColor.getRed(),
//                                                    backgroundColor.getGreen(),
//                                                    backgroundColor.getBlue()));
//        chartPanel.setLayout(new BorderLayout());
//        panel = new ChartPanel(getEmptyChart());
//        panel.setDoubleBuffered(true);
//        chartPanel.add(panel, BorderLayout.CENTER); 
//        composite = null;
        
        // Reset
        reset();
    }
    
    /**
     * Creates an empty chart
     * @return
     */
    private JFreeChart getEmptyChart(){
    	XYZDataset dataset = new DefaultXYZDataset();
    	NumberAxis xAxis = new NumberAxis("");
    	NumberAxis yAxis = new NumberAxis("");
    	XYBlockRenderer renderer = new XYBlockRenderer(); 
    	XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer); 
    	JFreeChart chart = new JFreeChart("", plot);
    	return chart;
    }
    
    /**
     * Returns a chart for the given attributes
     * @param data
     * @param attribute1
     * @param attribute2
     * @return
     */
    private JFreeChart getChart(DataHandle data, String attribute1, String attribute2) { 
    	
    	// Obtain data
    	 final int index1 = data.getColumnIndexOf(attribute1);
         final int index2 = data.getColumnIndexOf(attribute2);
         final String[] vals1 = getLabels(attribute1);
         final String[] vals2 = getLabels(attribute2);
         final Map<String, Integer> map1 = new HashMap<String, Integer>();
         final Map<String, Integer> map2 = new HashMap<String, Integer>();

         // Build maps
         int index = 0;
         for (int i = 0; i < vals1.length; i ++) {
             map1.put(vals1[i], index++);
         }
         index = 0;
         for (int i = 0; i < vals2.length; i ++) {
              map2.put(vals2[i], index++);
         }
         
         // Build initial heatmap
         double[][] heat = new double[vals1.length][vals2.length];
         for (int row = 0; row < data.getNumRows(); row++) {
             
             String v1 = data.getValue(row, index1);
             String v2 = data.getValue(row, index2);
             Integer i1 = map1.get(v1);
             Integer i2 = map2.get(v2);
             heat[i1][i2]++;
         }
         map1.clear();
         map2.clear();
         
         // Scale down
         heat = getScaledDown(heat);

         // Compute max
         double max = 0;
         for (int x=0; x<heat.length; x++){
        	 for (int y=0; y<heat[0].length; y++){
        		 max = heat[x][y] > max ? heat[x][y] : max;
        	 }
         }
         
         // Normalize
         for (int x=0; x<heat.length; x++){
        	 for (int y=0; y<heat[0].length; y++){
        		 heat[x][y] /= max;
        	 }
         }
         
         // Create  series
         MatrixSeries matrix = new MatrixSeries("", heat.length, heat[0].length);
         for (int x=0; x<heat.length; x++){
        	 for (int y=0; y<heat[0].length; y++){
        		 matrix.update(x, y, heat[x][y]);
        	 }
         }
         MatrixSeriesCollection collection = new MatrixSeriesCollection(matrix);

    	// Create axes
        NumberAxis xAxis = new NumberAxis(attribute2);
        xAxis.setAutoRange(true);
        NumberAxis yAxis = new NumberAxis(attribute1);
        yAxis.setAutoRange(true);

        // Create renderer
        XYBlockRenderer renderer = new XYBlockRenderer(); 
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setPaintScale(GRADIENT);
        
        XYPlot plot = new XYPlot(collection, xAxis, yAxis, renderer); 
        plot.setOrientation(PlotOrientation.HORIZONTAL); 
        plot.setBackgroundPaint(Color.lightGray); 
        plot.setRangeGridlinePaint(Color.white); 
        JFreeChart chart = new JFreeChart("", plot); 
        chart.removeLegend(); 
        chart.setBackgroundPaint(Color.white); 
        
        return chart; 
    } 
    
    /**
     * Downsample the given heatmap
     * @param heat
     * @return
     */
    private double[][] getScaledDown(double[][] heat) {
    	
		int MAX = 100;
		
		// Find xFactor
		int factorX = 1;
		double lengthX = heat[0].length;
		while(lengthX > MAX) {
			factorX++;
			lengthX = (double)heat[0].length / (double)factorX;
		}

		// Find yFactor
		int factorY = 1;
		double lengthY = heat.length;
		while(lengthY > MAX) {
			factorY++;
			lengthY = (double)heat.length / (double)factorY;
		}
		
		// Nothing to do
		if (factorX==1 && factorY==1) return heat;
		
		// Scale down
		// TODO: Might loose some data points here
		double[][] result = new double[heat.length / factorY][heat[0].length / factorX];
		
		for (int x=0; x<heat[0].length; x+=factorX) {
			for (int y=0; y<heat.length; y+=factorY) {
				
				int oX = x / factorX;
				int oY = y / factorY;
				
				double val = 0;
				for (int dX=x; dX<x+factorX; dX++) {
					for (int dY=y; dY<y+factorY; dY++) {
						if (dY<heat.length && dX<heat[0].length) val+=heat[dY][dX];
					}
				}
				
				if (oY<result.length && oX<result[0].length) result[oY][oX] = val;
			}
		}
		
		return result;
	}

	/**
     * Create a gradient
     * @return
     */
    private static LookupPaintScale getGradient() {
    	
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(1, 100);
        Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
        float[] dist = new float[colors.length];
        for (int i=0; i<dist.length; i++){
        	dist[i] = (1.0f / (float)dist.length) * (float)i;
        }
        LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);

        BufferedImage image = new BufferedImage(1,100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setPaint(p);
        g2d.drawRect(0,0,1,100);
        g2d.dispose();
        
        Color[] result = new Color[100];
        for (int y=0; y<100; y++){
        	result[y] = new Color(image.getRGB(0, y));
        }
        
        LookupPaintScale scale = new LookupPaintScale(0d, 1d, Color.white);
        for (int i=0; i<100; i++){
        	scale.add((double)i / 100d, result[i]);
        }
        return scale;
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
            redraw();
            
            // Handle selected attribute
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.VIEW_CONFIG) {
            if (model.getAttributePair()[0] != null &&
                model.getAttributePair()[1] != null) {
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

            final DataHandle data = getData();
            if (data == null) {
                reset();
                return;
            }

       	    final int index1 = data.getColumnIndexOf(model.getAttributePair()[0]);
            final int index2 = data.getColumnIndexOf(model.getAttributePair()[1]);

            if (index1 < 0 || index2 < 0) return;

            if (panel == null) {
            	final JFreeChart chart = getChart(data, model.getAttributePair()[0], model.getAttributePair()[1]);
				controller.getResources().getDisplay()
						.asyncExec(new Runnable() {
							public void run() {
								composite.setRedraw(false);
								composite.setChart(chart);
								composite.setRedraw(true);
							}
						});

        	} else {
        		panel.setChart(getChart(data, model.getAttributePair()[0], model.getAttributePair()[1]));
        	}
        }
    }

    /**
     * Recreates the plot, to prevent crashes
     */
    private void resetPlot() {
    	
    	if (panel == null) {
    		composite.setRedraw(false);
            composite.setChart(getEmptyChart());
            composite.setRedraw(true);
    	} else {
    		panel.setChart(getEmptyChart());
    	}
    }
}
