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
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class displays a contingency table as a heatmap
 * @author Fabian Prasser
 */
public class ViewDensity extends Panel implements IView {

    /** Static stuff*/
    private static final int           MAX_SIZE     = 500;
    /** Static stuff*/
    private static final long          serialVersionUID  = 5938131772944084967L;
    /** The bridge */
    private final Composite     bridge;
    /** The bridge */
    private final Frame         frame;
    /** Internal stuff */
    private final Controller    controller;
    /** Internal stuff */
    private final ModelPart     reset;
    /** Internal stuff */
    private final ModelPart     target;
    /** Internal stuff */
    private Model               model;
    /** Internal stuff */
    private AnalysisContext     context          = new AnalysisContext();
    /** The background color */
    private Color               background       = null;
    /** Renderer */
    private HeatmapRenderer renderer         = null;

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
        background = getAWTColor(parent.getBackground());
        bridge = new Composite(parent, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
        bridge.setLayoutData(SWTUtil.createFillGridData());
        frame = SWT_AWT.new_Frame(bridge);

        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setBackground(Color.WHITE);
        
        renderer = new HeatmapRenderer(background);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent arg0) {
                renderer.updateBuffer(getWidth(), getHeight());
                renderer.updatePlot();
                repaint();
            }

            @Override
            public void componentShown(final ComponentEvent arg0) {
                renderer.updateBuffer(getWidth(), getHeight());
                renderer.updatePlot();
                repaint();
            }
        });

        // Reset
        renderer.updateBuffer(this.getWidth(), this.getHeight());
        renderer.updatePlot();
        repaint();
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void paint(final Graphics g) {
        if (renderer.getBuffer() != null) {
            g.drawImage(renderer.getBuffer(), 0, 0, this);
        } else {
            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }


    @Override
    public void reset() {
        renderer.updateData(null, null, null);
        renderer.updatePlot();
        repaint();
    }
    
    @Override
    public void update(final Graphics g) {
        paint(g);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            update();
        }

        if (event.part == reset) {
            reset();
            
        } else if (event.part == target) {
            update();
            
        } else if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.model.resetAttributePair();
            this.context.setModel(model);
            this.context.setTarget(target);
            reset();

        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            update();
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            update();
            
        } else if (event.part == ModelPart.VIEW_CONFIG) {
            update();
        }
    }

    /**
     * Returns an AWT color
     * @param in
     * @return
     */
    private Color getAWTColor(final org.eclipse.swt.graphics.Color in) {
        return new Color(in.getRed(), in.getGreen(), in.getBlue());
    }

    /**
     * Redraws the plot
     */
    private void update() {

        if (model != null &&
            model.getAttributePair() != null &&
            model.getAttributePair()[0] != null &&
            model.getAttributePair()[1] != null) {

            // Obtain the right handle
            DataHandle handle = this.context.getContext().handle;
            if (handle == null) {
                reset();
                return;
            }
            
            String attribute1 = model.getAttributePair()[0];
            String attribute2 = model.getAttributePair()[1];
            int column1 = handle.getColumnIndexOf(attribute1);
            int column2 = handle.getColumnIndexOf(attribute2);
            
            renderer.updateData(model.getAttributePair()[0],
                                model.getAttributePair()[1],
                                handle.getStatistics().getContingencyTable(column1, MAX_SIZE, 
                                                                           column2, MAX_SIZE));
            renderer.updatePlot();
            repaint();
            
        } else {
            reset();
            return; 
        }
    }
}
