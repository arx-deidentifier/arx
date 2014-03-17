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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ViewDensity extends ViewStatistics implements IView {

    /** Static stuff*/
    private static final long          serialVersionUID  = 5938131772944084967L;
    /** Static stuff*/
    private static final int           MAX_DIMENSION     = 500;
    /** Static stuff*/
    private static final BufferedImage LEGEND            = getLegend();
    /** Static stuff*/
    private static final Color[]       GRADIENT          = getGradient(LEGEND);
    /** Static stuff*/
    private static final Font          FONT              = new Font("Arial", Font.PLAIN, 12); //$NON-NLS-1$

    /** Offset*/
    private static final int           OFFSET_LEFT       = 20;
    /** Offset*/
    private static final int           OFFSET_RIGHT      = 50;
    /** Offset*/
    private static final int           OFFSET_LEGEND     = 10;
    /** Offset*/
    private static final int           OFFSET_TOP        = 20;
    /** Offset*/
    private static final int           OFFSET_BOTTOM     = 20;
    /** Offset*/
    private static final int           OFFSET_TICK       = 5;
    /** Offset*/
    private static final int           OFFSET_TICK_SMALL = 2;

    /**
     * Returns an AWT color
     * @param in
     * @return
     */
    private static Color asAWTColor(final org.eclipse.swt.graphics.Color in) {
        return new Color(in.getRed(), in.getGreen(), in.getBlue());
    }

    /**
     * Create a gradient
     * @return
     */
    private static final Color[] getGradient(BufferedImage legend) {
        
        Color[] result = new Color[100];
        for (int y=0; y<100; y++){
            result[y] = new Color(legend.getRGB(0, y));
        }
        return result;
    }
    
    /**
     * Returns the legend
     * @return
     */
    private static final BufferedImage getLegend() {

        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(1, 100);
        Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
        float[] dist = new float[colors.length];
        for (int i=0; i<dist.length; i++){
            dist[i] = (1.0f / (float)dist.length) * (float)i;
        }
        LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
        BufferedImage legend = new BufferedImage(1,100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)legend.getGraphics();
        g2d.setPaint(p);
        g2d.drawRect(0,0,1,100);
        g2d.dispose();
        return legend;
    }

    /** The bridge */
    private final Composite  bridge;

    /** The bridge */
    private final Frame      frame;

    /** Internal stuff */
    private final Controller controller;
    /** Internal stuff */
    private final ModelPart  reset;

    /** The back buffer for implementing double buffering */
    private BufferedImage    buffer     = null;
    /** The background color*/
    private Color            background = null;
    /** The heatmap buffer */
    private BufferedImage    heatmap    = null;
    
    /** Attribute1 */
    private String           attribute1 = null;
    /** Attribute2 */
    private String           attribute2 = null;
    /** Data */
    private DataHandle       handle     = null;

	/**
	 * Creates a new density plot
	 * @param parent
	 * @param controller
	 * @param target
	 * @param reset
	 */
    public ViewDensity(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        parent.setLayout(new GridLayout());
        background = asAWTColor(parent.getBackground());
        bridge = new Composite(parent, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
        bridge.setLayoutData(SWTUtil.createFillGridData());
        frame = SWT_AWT.new_Frame(bridge);

        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setBackground(Color.WHITE);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent arg0) {
                updateBuffer();
                updatePlot();
                repaint();
            }

            @Override
            public void componentShown(final ComponentEvent arg0) {
                updateBuffer();
                updatePlot();
                repaint();
            }
        });

        // Reset
        updateBuffer();
        updatePlot();
        repaint();
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void paint(final Graphics g) {
        if (buffer != null) {
            g.drawImage(buffer, 0, 0, this);
        } else {
            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }


    @Override
    public void reset() {
        attribute1 = null;
        attribute2 = null;
        handle = null;
        updateData();
        updatePlot();
        repaint();
    }
    
    @Override
    public void update(final Graphics g) {
        paint(g);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            redraw();
        }

        if (event.part == reset) {
            reset();
            
        } else if (event.part == target) {
            redraw();
            
        } else if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.model.resetAttributePair();
            reset();

        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            redraw();
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            redraw();
            
        } else if (event.part == ModelPart.VIEW_CONFIG) {
            redraw();
        }
    }

    /**
     * Utility method which centers a text in a rectangle
     * 
     * @param s1
     * @param g
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void centerText(final String s1,
                            final Graphics g,
                            final int x,
                            final int y,
                            final int w,
                            final int h) {
        final Font f = g.getFont();
        final FontMetrics fm = g.getFontMetrics(f);
        final int ascent = fm.getAscent();
        final int height = fm.getHeight();
        int width1 = 0, x0 = 0, y0 = 0;
        width1 = fm.stringWidth(s1);
        x0 = x + ((w - width1) / 2);
        y0 = y + ((h - height) / 2) + ascent;
        g.drawString(s1, x0, y0);
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
        DataHandle data = getHandle();
        
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

        // Obtain config
        ModelConfiguration config = super.getConfig();
        
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
        final int index = handle.getColumnIndexOf(attribute);
        final Set<String> elems = new HashSet<String>();
        for (int i = 0; i < handle.getNumRows(); i++) {
            elems.add(handle.getValue(i, index));
        }

        // Init distribution
        final String[] dvals;

        // Sort by hierarchy if possible
        if (hierarchy != null && hierarchy.getHierarchy()!=null && hierarchy.getHierarchy().length != 0) {

            final int level = handle.getGeneralization(attribute);
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
            final DataType<?> dtype = handle.getDataType(attribute);
            final String[] v = new String[elems.size()];
            int i = 0;
            for (final String s : elems) {
                v[i++] = s;
            }
            try {
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
            } catch (Exception e){
                // TODO: Make sure that invalid data types can not even be selected
                controller.getResources().getLogger().warn("Invalid data type!");
            }
            dvals = v;
        }

        return dvals;
    }
    
    /**
     * Redraws the plot
     */
    private void redraw() {

        if (model != null &&
            model.getAttributePair() != null &
            model.getAttributePair()[0] != null &&
            model.getAttributePair()[1] != null) {
            
            final DataHandle data = getData();
            if (data == null) {
                reset();
                return;
            }
            
       	    final int index1 = data.getColumnIndexOf(model.getAttributePair()[0]);
            final int index2 = data.getColumnIndexOf(model.getAttributePair()[1]);

            if (index1 < 0 || index2 < 0){
                reset();
                return;
            }
            
            attribute1 = model.getAttributePair()[0];
            attribute2 = model.getAttributePair()[1];
            handle = data;

            updateData();
            updatePlot();
            repaint();
            
        } else {
            reset();
            return; 
        }
    }

    /**
     * Resets the buffer
     */
    private void updateBuffer() {
        if (buffer == null || buffer.getWidth() != this.getWidth() || buffer.getHeight() != this.getHeight()) {
            buffer = new BufferedImage(Math.max(1, getWidth()),
                                       Math.max(1, getHeight()),
                                       BufferedImage.TYPE_INT_RGB);
        }
    }

    /**
     * Recalculates the data array
     */
    private void updateData() {
        
        if (attribute1 == null || attribute2 == null || handle == null){
            heatmap = null;
            return;
        }
        
        final int index1 = handle.getColumnIndexOf(attribute1);
        final int index2 = handle.getColumnIndexOf(attribute2);

        if (index1 < 0 || index2 < 0){
            heatmap = null;
            return;
        }

        final String[] vals1 = getLabels(attribute1);
        final String[] vals2 = getLabels(attribute2);
        
        final Map<String, Integer> map1 = new HashMap<String, Integer>();
        final Map<String, Integer> map2 = new HashMap<String, Integer>();

        int step1 = vals1.length / MAX_DIMENSION; // Round down
        int step2 = vals2.length / MAX_DIMENSION; // Round down
        step1 = Math.max(step1, 1);
        step2 = Math.max(step2, 1);

        int index = 0;
        for (int i = 0; i < vals1.length; i += step1) {
            for (int j = 0; j < step1; j++) {
                if ((i + j) < vals1.length) {
                    map1.put(vals1[i + j], index);
                }
            }
            index++;
        }
        final int size1 = index;

        index = 0;
        for (int i = 0; i < vals2.length; i += step2) {
            for (int j = 0; j < step2; j++) {
                if ((i + j) < vals2.length) {
                    map2.put(vals2[i + j], index);
                }
            }
            index++;
        }
        final int size2 = index;

        double[][] data = new double[size1][size2];
        
        double max = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {
            final String v1 = handle.getValue(row, index1);
            final String v2 = handle.getValue(row, index2);
            final Integer i1 = map1.get(v1);
            final Integer i2 = map2.get(v2);
            if ((i1 == null) || (i2 == null)) {
                // TODO: Dont ignore
            } else {
                data[i1][i2]++;
                max = (data[i1][i2] > max ? data[i1][i2] : max);
            }
        }
        

        BufferedImage heatmap = new BufferedImage(data[0].length, data.length, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)heatmap.getGraphics();
        
        for (int y=0; y<data.length; y++){
            for (int x=0; x<data[y].length; x++){
                g.setColor(GRADIENT[(int)(data[y][x] / max * (GRADIENT.length-1))]);
                g.fillRect(x, y, 1, 1);
            }
        }
        g.dispose();
        this.heatmap = heatmap;
    }
    
    /**
     * Redraws the plot
     */
    private void updatePlot() {

        // Fill background
        Graphics2D g2d = (Graphics2D)buffer.getGraphics();
        g2d.setColor(background);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        // If enough space
        if (buffer.getWidth()>OFFSET_LEFT + OFFSET_RIGHT + 100 && buffer.getHeight() > OFFSET_TOP + OFFSET_BOTTOM){
            
            // Compute size
            int width = this.getWidth() - OFFSET_LEFT - OFFSET_RIGHT;
            int height = this.getHeight() - OFFSET_TOP - OFFSET_BOTTOM;
    
            // If data available
            if (heatmap != null) {
                
                // Draw heatmap
                g2d.drawImage(heatmap, OFFSET_LEFT, OFFSET_TOP, width, height, null);
                g2d.setColor(Color.black);
                g2d.drawRect(OFFSET_LEFT, OFFSET_TOP, width, height);
                
                // Draw xtics
                double tickX = (double) width / (double)heatmap.getWidth();
                if (tickX>=2d){
                    double currX = tickX;
                    while (currX < width-1) {
                        g2d.setColor(Color.darkGray);
                        g2d.drawLine(OFFSET_LEFT + (int)currX, OFFSET_TOP + height, OFFSET_LEFT + (int)currX + 1, OFFSET_TOP + height + OFFSET_TICK_SMALL);
                        currX += tickX;
                    }
                }
                
                // Draw ytics
                double tickY = (double) height / (double)heatmap.getHeight();
                if (tickY>=2d){
                    double currY = tickY;
                    while (currY < height-1) {
                        g2d.setColor(Color.darkGray);
                        g2d.drawLine(OFFSET_LEFT, OFFSET_TOP + (int)currY, OFFSET_LEFT - OFFSET_TICK_SMALL, OFFSET_TOP + (int)currY + 1);
                        currY += tickY;
                    }
                }
                
            // Else show info
            } else {
                g2d.setColor(Color.white);
                g2d.fillRect(OFFSET_LEFT, OFFSET_TOP, width, height);
                g2d.setColor(Color.black);
                g2d.drawRect(OFFSET_LEFT, OFFSET_TOP, width, height);

                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(Color.black);
                centerText("No data", g2d, OFFSET_LEFT, OFFSET_TOP, width, height);
                
            }
            
            // Draw legend
            RenderingHints hints = g2d.getRenderingHints();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int legendWidth = this.getWidth() - width - OFFSET_LEFT - 2 * OFFSET_LEGEND;
            g2d.drawImage(LEGEND, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP + height, legendWidth, -height, null);
            g2d.setRenderingHints(hints);
            g2d.setColor(Color.black);
            g2d.drawRect(OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP, legendWidth, height);
            
            // Draw legend text
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.black);
            centerText("Max", g2d, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP-15, legendWidth, 10);
            centerText("Min", g2d, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP+height+5, legendWidth, 10);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            
            // Draw corner tics
            g2d.drawLine(OFFSET_LEFT, OFFSET_TOP, OFFSET_LEFT - OFFSET_TICK, OFFSET_TOP);
            g2d.drawLine(OFFSET_LEFT, OFFSET_TOP + height, OFFSET_LEFT - OFFSET_TICK, OFFSET_TOP + height);
            g2d.drawLine(OFFSET_LEFT, OFFSET_TOP + height, OFFSET_LEFT, OFFSET_TOP + height + OFFSET_TICK);
            g2d.drawLine(OFFSET_LEFT + width, OFFSET_TOP + height, OFFSET_LEFT + width, OFFSET_TOP + height + OFFSET_TICK);
            
            // Draw axis labels
            if (attribute1 != null && attribute2 != null){
    
                // x label
                g2d.setColor(Color.black);
                g2d.setFont(FONT);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                centerText(attribute2, g2d, OFFSET_LEFT, OFFSET_TOP + height+4, width, 10);
    
                // y label
                g2d.rotate(Math.PI / 2);
                centerText(attribute1, g2d, OFFSET_TOP, -OFFSET_LEFT+4, height, 10);            
                g2d.rotate(-Math.PI / 2);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
        
        // Dispose
        g2d.dispose();
    }
}
