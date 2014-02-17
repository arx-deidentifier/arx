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
package org.deidentifier.arx.gui.view.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;

import org.deidentifier.arx.gui.resources.Resources;

public class Splash extends Frame{

    private static final long serialVersionUID = -4661666752999055995L;
    private final Image splash = Resources.getSplash();
    private final String version = Resources.getVersion();

    public Splash() throws IOException{
        
        this.setSize(new Dimension(400,240));
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setAutoRequestFocus(true);
        this.setUndecorated(true);
        this.addComponentListener(new ComponentAdapter(){
        	@Override
        	public void componentResized(ComponentEvent e) {
        		repaint();
			}
        });
    }

    /* (non-Javadoc)
     * @see javax.swing.JWindow#update(java.awt.Graphics)
     */
    @Override
    public void update(Graphics g) {
        paint(g);
    }

    /* (non-Javadoc)
     * @see java.awt.Window#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        int width = getWidth();
        int height = getHeight();
        Dimension offsets = getOffsets(g2d, version);
        
        g2d.drawImage(splash, 0,  0,  width,  height,  this);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.drawString(version, width - (int)offsets.width - 10, height - (int)offsets.height);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    /**
     * Returns the rough size of the given string
     * @param g2
     * @param str
     * @return
     */
    private Dimension getOffsets(Graphics2D g2, String str) {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        Rectangle r = gv.getPixelBounds(null, 20, 20);
        return new Dimension((int)r.getWidth(), (int)r.getHeight());
    }
}
