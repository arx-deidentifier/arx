/* ******************************************************************************
 * Copyright (c) 2014 - 2015 Fabian Prasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Fabian Prasser - initial API and implementation
 ******************************************************************************/

package org.deidentifier.arx.gui.view.impl.explore;

import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * A gradient
 * 
 * @author Fabian Prasser
 */
public class Gradient {
    
    /** Display*/
    private final Display display;
    
    /** The resulting colors*/
    private Color[] colors;
    
    /**
     * Creates a new color gradients
     * @param display Display
     */
    public Gradient(Display display){
        this(display, 100);
    }
    
    /**
     * Creates a new color gradient
     * @param display Display
     * @param steps
     */
    public Gradient(Display display, int steps){
        this.display = display;
        this.colors = getGradient(steps);
    }
    

    /**
     * Dispose all colors
     */
    public void dispose() {
        for (Color c : colors) {
            if (!c.isDisposed()) c.dispose();
        }
    }


    /**
     * Returns the color for a value in [0, 1]
     * @param value
     * @return
     */
    public Color getColor(double value) {
        int index = (int)Math.round((double)(colors.length-1) * value);
        index = index < 0 ? 0 : index;
        index = index > colors.length - 1 ? colors.length - 1 : index;
        return colors[index];
    }

    /**
     * Returns the color array
     * @param colors
     * @param steps
     * @return
     */
    private final Color[] getGradient(int steps) {
                
        Color[] colors = new Color[] {  new Color(display, 0, 0, 255),
                                        new Color(display, 0, 255, 255),
                                        new Color(display, 0, 200, 0),
                                        new Color(display, 255, 255, 0),
                                        new Color(display, 255, 69, 0),
                                        new Color(display, 255, 0, 0) };

        java.awt.Color[] awtcolor = new java.awt.Color[colors.length];
        for (int i=0; i<colors.length; i++){
            Color color = colors[i];
            awtcolor[i] = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        }
        Color[] result = getGradient(awtcolor, steps);
        for (Color c : colors) {
            c.dispose();
        }
        return result;
    }
    
    /**
     * Returns the color array
     * @param colors
     * @param steps
     * @return
     */
    private final Color[] getGradient(java.awt.Color[] colors, int steps) {

        // Draw the gradient to a buffered image
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(1, steps);
        float[] dist = new float[colors.length];
        for (int i=0; i<dist.length; i++){
            dist[i] = (1.0f / (float)dist.length) * (float)i;
        }
        LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
        BufferedImage legend = new BufferedImage(1,steps, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)legend.getGraphics();
        g2d.setPaint(p);
        g2d.drawRect(0,0,1,steps);
        g2d.dispose();
        
        // Convert to color array
        Color[] result = new Color[steps];
        for (int y=0; y<steps; y++){
            int rgb = legend.getRGB(0, y);
            result[y] = new Color(display, (rgb >> 16) & 0x000000ff, (rgb >> 8) & 0x000000ff, (rgb) & 0x000000ff);
        }
        
        // Return
        return result;
    }
}
