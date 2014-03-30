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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;

/**
 * This class renders a heatmap
 * @author Fabian Prasser
 *
 */
public class HeatmapRenderer {

    /** Static stuff */
    private static final Font          FONT              = new Font("Arial", Font.PLAIN, 12); //$NON-NLS-1$
    /** Static stuff */
    private static final BufferedImage LEGEND            = getLegend();
    /** Static stuff */
    private static final Color[]       GRADIENT          = getGradient(LEGEND);

    /** Offset */
    private static final int           OFFSET_BOTTOM     = 20;
    /** Offset */
    private static final int           OFFSET_LEFT       = 20;
    /** Offset */
    private static final int           OFFSET_LEGEND     = 10;
    /** Offset */
    private static final int           OFFSET_RIGHT      = 50;
    /** Offset */
    private static final int           OFFSET_TICK       = 5;
    /** Offset */
    private static final int           OFFSET_TICK_SMALL = 2;
    /** Offset */
    private static final int           OFFSET_TOP        = 20;
    
    /**
     * Creates a gradient
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
     * Creates a legend
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

    /** Attribute */
    private String                     attribute1        = null;
    /** Attribute */
    private String                     attribute2        = null;
    /** The background color */
    private Color                      background        = null;
    /** The back buffer for rendering into */
    private BufferedImage              buffer            = null;
    /** The heat map buffer */
    private BufferedImage              heatmap           = null;

    /**
     * Creates a new instance
     * @param background
     */
    public HeatmapRenderer(Color background){
        this.background = background;
    }
    
    /**
     * Returns the buffer
     * @return
     */
    public BufferedImage getBuffer(){
        return this.buffer;
    }

    /**
     * Resets the buffer
     */
    public void updateBuffer(int width, int height) {
        if (buffer == null || 
            buffer.getWidth() != width || 
            buffer.getHeight() != height) {
            buffer = new BufferedImage(Math.max(1, width), 
                                       Math.max(1, height), 
                                       BufferedImage.TYPE_INT_RGB);
        }
    }
    
    /**
     * Redraws the actual heatmap
     */
    public void updateData(String attribute1, String attribute2, StatisticsContingencyTable table) {
        
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        if (attribute1 == null || attribute2 == null || table == null){
            heatmap = null;
            return;
        }
        
        BufferedImage heatmap = new BufferedImage(table.values1.length, 
                                                  table.values2.length, 
                                                  BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = (Graphics2D)heatmap.getGraphics();
        g.setColor(GRADIENT[0]);
        g.fillRect(0, 0, heatmap.getWidth(), heatmap.getHeight());
        
        Iterator<Entry> iterator = table.iterator;
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            g.setColor(GRADIENT[(int)(entry.frequency * (GRADIENT.length-1))]);
            g.fillRect(entry.value1, entry.value2, 1, 1);
        }
        g.dispose();
        this.heatmap = heatmap;
    }
    
    /**
     * Redraws the plot
     */
    public void updatePlot() {

        // Fill background
        Graphics2D g2d = (Graphics2D)buffer.getGraphics();
        g2d.setColor(background);
        g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        
        // If enough space
        if (buffer.getWidth()>OFFSET_LEFT + OFFSET_RIGHT + 100 && buffer.getHeight() > OFFSET_TOP + OFFSET_BOTTOM){
            
            // Compute size
            int width = buffer.getWidth() - OFFSET_LEFT - OFFSET_RIGHT;
            int height = buffer.getHeight() - OFFSET_TOP - OFFSET_BOTTOM;
    
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
                drawText("No data", g2d, OFFSET_LEFT, OFFSET_TOP, width, height);
                
            }
            
            // Draw legend
            RenderingHints hints = g2d.getRenderingHints();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int legendWidth = buffer.getWidth() - width - OFFSET_LEFT - 2 * OFFSET_LEGEND;
            g2d.drawImage(LEGEND, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP + height, legendWidth, -height, null);
            g2d.setRenderingHints(hints);
            g2d.setColor(Color.black);
            g2d.drawRect(OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP, legendWidth, height);
            
            // Draw legend text
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.black);
            drawText("Max", g2d, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP-15, legendWidth, 10);
            drawText("Min", g2d, OFFSET_LEFT + width + OFFSET_LEGEND, OFFSET_TOP+height+5, legendWidth, 10);
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
                drawText(attribute2, g2d, OFFSET_LEFT, OFFSET_TOP + height+4, width, 10);
    
                // y label
                g2d.rotate(Math.PI / 2);
                drawText(attribute1, g2d, OFFSET_TOP, -OFFSET_LEFT+4, height, 10);            
                g2d.rotate(-Math.PI / 2);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
        
        // Dispose
        g2d.dispose();
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
    private void drawText(final String s1,
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
}
